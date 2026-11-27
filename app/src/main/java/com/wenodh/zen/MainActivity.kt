package com.wenodh.zen

import android.Manifest
import android.accessibilityservice.AccessibilityServiceInfo
import android.app.TimePickerDialog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.content.Intent
import android.widget.Toast
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.NotificationManagerCompat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wenodh.zen.blocking.BlockedAppsStore
import com.wenodh.zen.blocking.InstalledApp
import com.wenodh.zen.blocking.InstalledAppCatalog
import com.wenodh.zen.blocking.ZenAccessibilityService
import com.wenodh.zen.focus.AmbientSoundEngine
import com.wenodh.zen.focus.AmbientSoundStore
import com.wenodh.zen.focus.FocusAchievements
import com.wenodh.zen.focus.ExitChallenge
import com.wenodh.zen.focus.MemoryChallenge
import com.wenodh.zen.focus.ReflectionChallenge
import com.wenodh.zen.focus.FocusChallenge
import com.wenodh.zen.focus.FocusDndController
import com.wenodh.zen.focus.FocusProfile
import com.wenodh.zen.focus.FocusProfileStore
import com.wenodh.zen.focus.FocusSessionService
import com.wenodh.zen.focus.FocusSessionStore
import com.wenodh.zen.schedule.FocusScheduleStore
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

class MainActivity : ComponentActivity() {
    private val notificationPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { }
    private var accessibilityEnabled by mutableStateOf(false)
    private var notificationsEnabled by mutableStateOf(true)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        val sessionStore = FocusSessionStore(this)
        val blockedAppsStore = BlockedAppsStore(this)
        val profileStore = FocusProfileStore(this)
        val ambientStore = AmbientSoundStore(this)
        val dndController = FocusDndController(this)
        val scheduleStore = FocusScheduleStore(this)
        sessionStore.finishIfExpired()
        if (sessionStore.hasPendingSession()) {
            runCatching { FocusSessionService.start(this) }.onFailure {
                Toast.makeText(this, "Focus timer is saved, but Android did not restart its notification service.", Toast.LENGTH_LONG).show()
            }
        }
        setContent {
            MaterialTheme(colorScheme = lightColorScheme(
                primary = Color(0xFF176B55),
                secondary = Color(0xFF5B746B),
                background = Color(0xFFF5F7F5),
                surface = Color.White
            )) {
                var showBlockingSettings by remember { mutableStateOf(false) }
                if (showBlockingSettings) {
                    AppBlockingSettings(
                        apps = remember { InstalledAppCatalog.load(this) },
                        store = blockedAppsStore,
                        serviceEnabled = accessibilityEnabled,
                        onBack = { showBlockingSettings = false },
                        onOpenAccessibilitySettings = {
                            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                        }
                    )
                } else {
                    ZenHome(
                        store = sessionStore,
                        profiles = profileStore,
                        ambientStore = ambientStore,
                        dndController = dndController,
                        scheduleStore = scheduleStore,
                        onStart = { minutes ->
                            runCatching {
                                sessionStore.start(minutes)
                                FocusSessionService.start(this)
                            }.onFailure {
                                runCatching { sessionStore.interrupt() }
                                Toast.makeText(this, "Could not start the focus timer. Please try again.", Toast.LENGTH_LONG).show()
                            }
                        },
                        onChallengePassed = {
                            sessionStore.endWithChallenge()
                            FocusSessionService.stop(this)
                        },
                        onAmbientChange = { FocusSessionService.setAmbientSound(this, it) },
                        onOpenDndSettings = {
                            startActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
                        },
                        onManageBlockedApps = { showBlockingSettings = true },
                        serviceEnabled = accessibilityEnabled,
                        notificationsEnabled = notificationsEnabled,
                        onOpenNotificationSettings = {
                            startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, packageName))
                        }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val manager = getSystemService(AccessibilityManager::class.java)
        accessibilityEnabled = manager
            ?.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
            ?.any {
                it.resolveInfo.serviceInfo.packageName == packageName &&
                    it.resolveInfo.serviceInfo.name == ZenAccessibilityService::class.java.name
            } == true
        notificationsEnabled = NotificationManagerCompat.from(this).areNotificationsEnabled() &&
            (Build.VERSION.SDK_INT < 33 || checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED)
    }
}

@Composable
private fun ZenHome(
    store: FocusSessionStore,
    profiles: FocusProfileStore,
    ambientStore: AmbientSoundStore,
    dndController: FocusDndController,
    scheduleStore: FocusScheduleStore,
    onStart: (Int) -> Unit,
    onChallengePassed: () -> Unit,
    onAmbientChange: (AmbientSoundEngine.Sound?) -> Unit,
    onOpenDndSettings: () -> Unit,
    onManageBlockedApps: () -> Unit,
    serviceEnabled: Boolean,
    notificationsEnabled: Boolean,
    onOpenNotificationSettings: () -> Unit
) {
    val context = LocalContext.current
    var selectedProfile by remember { mutableStateOf(profiles.selectedProfile()) }
    var selectedMinutes by remember { mutableIntStateOf(selectedProfile.durationMinutes) }
    var remaining by remember { mutableStateOf(store.remainingMillis()) }
    var active by remember { mutableStateOf(store.isActive()) }
    var todayMinutes by remember { mutableIntStateOf(store.todayCompletedMinutes()) }
    var todaySessions by remember { mutableIntStateOf(store.todayCompletedCount()) }
    var blockedAttempts by remember { mutableIntStateOf(store.todayBlockedAttemptCount()) }
    var weeklyMinutes by remember { mutableIntStateOf(store.last7DaysCompletedMinutes()) }
    var weeklySessions by remember { mutableIntStateOf(store.last7DaysCompletedCount()) }
    var recentSessions by remember { mutableStateOf(store.recentSessions()) }
    var challenge by remember { mutableStateOf<ExitChallenge?>(null) }
    var revealMemory by remember { mutableStateOf(false) }
    var challengeAnswer by remember { mutableStateOf("") }
    var challengeError by remember { mutableStateOf(false) }
    var showEmergencyExitConfirmation by remember { mutableStateOf(false) }
    var customProfileDialog by remember { mutableStateOf(false) }
    var customName by remember { mutableStateOf("My focus") }
    var customDuration by remember { mutableStateOf("30") }
    var customProfileError by remember { mutableStateOf(false) }
    var selectedAmbient by remember { mutableStateOf(ambientStore.selected()) }
    var dndEnabled by remember { mutableStateOf(dndController.isEnabledForFocus()) }
    var dndAccess by remember { mutableStateOf(dndController.hasAccess()) }
    var scheduleEnabled by remember { mutableStateOf(scheduleStore.isEnabled()) }
    var scheduleTime by remember { mutableStateOf("%02d:%02d".format(scheduleStore.hour(), scheduleStore.minute())) }

    LaunchedEffect(challenge) {
        revealMemory = challenge is MemoryChallenge
        if (challenge is MemoryChallenge) {
            delay(2_500L)
            revealMemory = false
        }
    }

    LaunchedEffect(Unit) {
        while (isActive) {
            val nowActive = store.isActive()
            if (nowActive != active) {
                todayMinutes = store.todayCompletedMinutes()
                todaySessions = store.todayCompletedCount()
                weeklyMinutes = store.last7DaysCompletedMinutes()
                weeklySessions = store.last7DaysCompletedCount()
                recentSessions = store.recentSessions()
            }
            active = nowActive
            remaining = store.remainingMillis()
            blockedAttempts = store.todayBlockedAttemptCount()
            dndAccess = dndController.hasAccess()
            delay(5_000L)
        }
    }

    val seconds = (remaining / 1000L).coerceAtLeast(0L)
    val timeText = "%02d:%02d".format(seconds / 60L, seconds % 60L)
    val achievements = FocusAchievements.evaluate(
        completedSessions = store.allTimeCompletedCount(),
        completedMinutes = store.allTimeCompletedMinutes(),
        challengeExits = store.challengeExitCount(),
        blockedAttempts = store.allBlockedAttemptCount()
    )

    Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFFF5F7F5)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("WENODH ZEN", color = Color(0xFF176B55), fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                    Text("Disconnect. Focus. Be present.", style = MaterialTheme.typography.bodyLarge, color = Color(0xFF65736C))
                }
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(if (active) "FOCUS IN PROGRESS · ${selectedProfile.label.uppercase()}" else "READY TO FOCUS · ${selectedProfile.label.uppercase()}", color = Color(0xFF65736C), fontSize = 11.sp, letterSpacing = 1.sp)
                        Text(if (active) timeText else "%02d:00".format(selectedMinutes), fontSize = 52.sp, fontWeight = FontWeight.Light, color = Color(0xFF183B30))
                        Text(if (active) "Stay with one thing." else "Choose a profile and begin.", color = Color(0xFF65736C))
                        if (active) {
                            OutlinedButton(
                                onClick = {
                                    challenge = FocusChallenge.create(level = selectedProfile.challengeLevel)
                                    challengeAnswer = ""
                                    challengeError = false
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("End session · complete a challenge") }
                        } else {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(profiles.profiles(), key = FocusProfile::id) { profile ->
                                    OutlinedButton(
                                        onClick = {
                                            profiles.select(profile.id)
                                            selectedProfile = profile
                                            selectedMinutes = profile.durationMinutes
                                        },
                                        shape = RoundedCornerShape(12.dp)
                                    ) { Text(if (profile.id == selectedProfile.id) "✓ ${profile.label}" else profile.label) }
                                }
                                item {
                                    OutlinedButton(onClick = { customProfileDialog = true }, shape = RoundedCornerShape(12.dp)) { Text("+ Custom") }
                                }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                                listOf(15, 25, 45, 60).forEach { minutes ->
                                    OutlinedButton(onClick = { selectedMinutes = minutes }, shape = RoundedCornerShape(12.dp)) {
                                        Text("$minutes")
                                    }
                                }
                            }
                            Button(onClick = { onStart(selectedMinutes) }, modifier = Modifier.fillMaxWidth()) { Text("Start focus") }
                        }
                    }
                }
            }
            item {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(18.dp)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text("Today's focus", fontWeight = FontWeight.SemiBold, color = Color(0xFF183B30))
                        Text("$todayMinutes minutes · $todaySessions completed sessions", color = Color(0xFF52645A), style = MaterialTheme.typography.bodyMedium)
                        Text("$blockedAttempts blocked-app attempts", color = Color(0xFF52645A), style = MaterialTheme.typography.bodySmall)
                        Text("Last 7 days: $weeklyMinutes minutes · $weeklySessions sessions", color = Color(0xFF52645A), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            if (!notificationsEnabled) {
                item {
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1DB)), shape = RoundedCornerShape(18.dp)) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Notifications are disabled", fontWeight = FontWeight.SemiBold)
                            Text("The focus timer and reminder notifications may not appear. Enable notifications in Android settings.")
                            OutlinedButton(onClick = onOpenNotificationSettings) { Text("Open notification settings") }
                        }
                    }
                }
            }
            item {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFE7EFEA)), shape = RoundedCornerShape(18.dp)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("App restrictions", fontWeight = FontWeight.SemiBold, color = Color(0xFF183B30))
                        Text(
                            if (serviceEnabled) "Accessibility monitoring is enabled." else "App blocking is inactive until Accessibility access is enabled.",
                            color = if (serviceEnabled) Color(0xFF52645A) else Color(0xFF9A5A18),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        OutlinedButton(onClick = onManageBlockedApps, modifier = Modifier.fillMaxWidth()) { Text("Manage blocked apps") }
                    }
                }
            }
            item {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(18.dp)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Ambient sound", fontWeight = FontWeight.SemiBold)
                        Text("Generated on-device. No tracks or data are downloaded.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(
                                listOf(
                                    "Off" to null,
                                    "White" to AmbientSoundEngine.Sound.WHITE,
                                    "Pink" to AmbientSoundEngine.Sound.PINK,
                                    "Brown" to AmbientSoundEngine.Sound.BROWN
                                ),
                                key = { it.first }
                            ) { (label, sound) ->
                                OutlinedButton(onClick = {
                                    selectedAmbient = sound
                                    onAmbientChange(sound)
                                }) { Text(if (selectedAmbient == sound) "✓ $label" else label) }
                            }
                        }
                    }
                }
            }
            item {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(18.dp)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Do Not Disturb", fontWeight = FontWeight.SemiBold)
                        Text(
                            if (!dndAccess) "Grant Notification Policy Access before enabling DND during focus."
                            else if (dndEnabled) "Priority interruptions will be enabled during focus and restored afterwards."
                            else "Optional. DND is currently off for focus sessions.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = onOpenDndSettings) { Text(if (dndAccess) "DND access settings" else "Grant DND access") }
                            OutlinedButton(onClick = {
                                if (dndAccess) {
                                    dndEnabled = !dndEnabled
                                    dndController.setEnabledForFocus(dndEnabled)
                                    if (active) {
                                        if (dndEnabled) dndController.applyForSession()
                                        else dndController.restoreAfterSession()
                                    }
                                } else onOpenDndSettings()
                            }) { Text(if (dndEnabled) "Disable for focus" else "Enable for focus") }
                        }
                    }
                }
            }
            item {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(18.dp)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Daily focus reminder", fontWeight = FontWeight.SemiBold)
                        Text(if (scheduleEnabled) "Repeats daily at $scheduleTime (may be delayed by Android battery optimizations)." else "A gentle daily reminder. It never starts a session automatically.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = {
                                TimePickerDialog(
                                    context,
                                    { _, hour, minute ->
                                        scheduleStore.configure(true, hour, minute)
                                        scheduleEnabled = true
                                        scheduleTime = "%02d:%02d".format(hour, minute)
                                    },
                                    scheduleStore.hour(),
                                    scheduleStore.minute(),
                                    true
                                ).show()
                            }) { Text("Set time") }
                            OutlinedButton(onClick = {
                                scheduleEnabled = !scheduleEnabled
                                scheduleStore.configure(scheduleEnabled)
                            }) { Text(if (scheduleEnabled) "Turn off" else "Turn on") }
                        }
                    }
                }
            }
            item {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFE7EFEA)), shape = RoundedCornerShape(18.dp)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Achievements", fontWeight = FontWeight.SemiBold)
                        achievements.forEach { achievement ->
                            Text(
                                (if (achievement.unlocked) "✓ " else "○ ") + achievement.title + " — " + achievement.description,
                                color = if (achievement.unlocked) Color(0xFF176B55) else Color(0xFF758078),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
            item {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(18.dp)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Recent sessions", fontWeight = FontWeight.SemiBold)
                        if (recentSessions.isEmpty()) {
                            Text("Your completed and interrupted sessions will appear here.", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                        } else {
                            recentSessions.forEach { session ->
                                Text(
                                    "${formatSessionTime(session.startedAt)} · ${session.durationMinutes} min · ${session.status.lowercase().replace('_', ' ')} · ${session.blockedAttempts} app attempts",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF52645A)
                                )
                            }
                        }
                    }
                }
            }
            item {
                Text("LOCAL-FIRST · NO ACCOUNT REQUIRED", color = Color(0xFF829088), fontSize = 10.sp, letterSpacing = 1.sp)
            }
        }
    }

    if (challenge != null) {
        AlertDialog(
            onDismissRequest = { challenge = null },
            title = { Text("A quick pause before you leave") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Choose a short exit challenge:")
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedButton(onClick = {
                            challenge = FocusChallenge.create(level = selectedProfile.challengeLevel)
                            challengeAnswer = ""
                            challengeError = false
                        }) { Text("Math") }
                        OutlinedButton(onClick = {
                            challenge = FocusChallenge.createMemory()
                            challengeAnswer = ""
                            challengeError = false
                        }) { Text("Memory") }
                        OutlinedButton(onClick = {
                            challenge = FocusChallenge.createReflection()
                            challengeAnswer = ""
                            challengeError = false
                        }) { Text("Reflect") }
                    }
                    Text(challenge!!.prompt, style = MaterialTheme.typography.bodyLarge)
                    if (challenge is MemoryChallenge && revealMemory) {
                        Text((challenge as MemoryChallenge).sequence, style = MaterialTheme.typography.headlineMedium)
                    } else {
                        OutlinedTextField(
                            value = challengeAnswer,
                            onValueChange = { challengeAnswer = it; challengeError = false },
                            label = { Text(if (challenge is ReflectionChallenge) "Your reflection" else "Your answer") },
                            singleLine = challenge !is ReflectionChallenge
                        )
                    }
                    if (challenge is MemoryChallenge && revealMemory) Text("Memorize it — the sequence will hide in a moment.")
                    if (challengeError) Text("That answer isn't correct. Try again.", color = MaterialTheme.colorScheme.error)
                    Text("If you close this challenge, your focus session continues.")
                }
            },
            confirmButton = {
                Button(
                    enabled = challenge !is MemoryChallenge || !revealMemory,
                    onClick = {
                    val current = challenge ?: return@Button
                    if (FocusChallenge.isCorrect(challengeAnswer, current)) {
                        challenge = null
                        onChallengePassed()
                    } else challengeError = true
                }) { Text("Confirm and end") }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { challenge = null }) { Text("Keep focusing") }
                    TextButton(onClick = { showEmergencyExitConfirmation = true }) { Text("Emergency exit") }
                }
            }
        )
    }

    if (showEmergencyExitConfirmation) {
        AlertDialog(
            onDismissRequest = { showEmergencyExitConfirmation = false },
            title = { Text("End this session now?") },
            text = {
                Text("This bypasses the exit challenge and records the session as interrupted. Use it if you need to stop immediately.")
            },
            confirmButton = {
                Button(onClick = {
                    store.interrupt()
                    FocusSessionService.stop(context)
                    challenge = null
                    showEmergencyExitConfirmation = false
                }) { Text("End session") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showEmergencyExitConfirmation = false }) { Text("Go back") }
            }
        )
    }

    if (customProfileDialog) {
        AlertDialog(
            onDismissRequest = { customProfileDialog = false },
            title = { Text("Create a focus profile") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = customName, onValueChange = { customName = it }, label = { Text("Profile name") }, singleLine = true)
                    OutlinedTextField(value = customDuration, onValueChange = { customDuration = it.filter(Char::isDigit).take(3); customProfileError = false }, label = { Text("Duration in minutes (5–180)") }, singleLine = true)
                    if (customProfileError) Text("Enter a name and duration between 5 and 180 minutes.", color = MaterialTheme.colorScheme.error)
                }
            },
            confirmButton = {
                Button(onClick = {
                    val duration = customDuration.toIntOrNull()
                    if (customName.isNotBlank() && duration != null && duration in 5..180) {
                        profiles.saveCustom(customName, duration)
                        selectedProfile = profiles.selectedProfile()
                        selectedMinutes = selectedProfile.durationMinutes
                        customProfileDialog = false
                        customProfileError = false
                    } else customProfileError = true
                }) { Text("Save profile") }
            },
            dismissButton = { OutlinedButton(onClick = { customProfileDialog = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun AppBlockingSettings(
    apps: List<InstalledApp>,
    store: BlockedAppsStore,
    serviceEnabled: Boolean,
    onBack: () -> Unit,
    onOpenAccessibilitySettings: () -> Unit
) {
    var blockedPackages by remember { mutableStateOf(store.blockedPackages()) }
    var query by remember { mutableStateOf("") }
    var disclosureAccepted by remember { mutableStateOf(store.hasAcceptedDisclosure()) }
    val visibleApps = remember(apps, query) {
        apps.filter { it.label.contains(query, ignoreCase = true) || it.packageName.contains(query, ignoreCase = true) }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFFF5F7F5)) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onBack) { Text("Back") }
                Text("Blocked apps", style = MaterialTheme.typography.headlineSmall)
            }
            Text("Selected apps are interrupted only while a focus session is active. Changes are stored on this device.")
            OutlinedTextField(value = query, onValueChange = { query = it }, label = { Text("Search apps") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(if (serviceEnabled) "Enforcement service: enabled" else "Enforcement service: not enabled", fontWeight = FontWeight.SemiBold)
                    Text(
                        "Wenodh Zen observes only which app package is in the foreground while a focus session is active. It does not read screen text, keystrokes, notifications, or send this activity anywhere."
                    )
                    Text("Android lets you turn this service off at any time. Blocking is best-effort and is not a device lock.")
                    Button(
                        onClick = {
                            if (!disclosureAccepted) {
                                store.acceptDisclosure()
                                disclosureAccepted = true
                            }
                            onOpenAccessibilitySettings()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(if (disclosureAccepted) "Open Accessibility settings" else "I understand — continue") }
                    if (!disclosureAccepted) {
                        Text("Your tap records consent to this separate Accessibility disclosure before Android Settings opens.")
                    }
                }
            }
            Text("Choose apps to block", fontWeight = FontWeight.SemiBold)
            if (visibleApps.isEmpty()) {
                Text("No matching launcher apps were found.")
            } else {
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    items(visibleApps, key = InstalledApp::packageName) { app ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(app.label, fontWeight = FontWeight.Medium)
                                Text(app.packageName, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                            Checkbox(
                                checked = app.packageName in blockedPackages,
                                onCheckedChange = { checked ->
                                    store.setBlocked(app.packageName, checked)
                                    blockedPackages = store.blockedPackages()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}


private fun formatSessionTime(timestamp: Long): String =
    SimpleDateFormat("EEE, d MMM · h:mm a", Locale.getDefault()).format(Date(timestamp))
