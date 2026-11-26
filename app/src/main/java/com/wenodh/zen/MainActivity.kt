package com.wenodh.zen

import android.Manifest
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wenodh.zen.blocking.BlockedAppsStore
import com.wenodh.zen.blocking.InstalledApp
import com.wenodh.zen.blocking.InstalledAppCatalog
import com.wenodh.zen.blocking.ZenAccessibilityService
import com.wenodh.zen.focus.FocusSessionService
import com.wenodh.zen.focus.FocusSessionStore
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

class MainActivity : ComponentActivity() {
    private val notificationPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { }
    private var accessibilityEnabled by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        val sessionStore = FocusSessionStore(this)
        val blockedAppsStore = BlockedAppsStore(this)
        if (sessionStore.hasPendingSession()) FocusSessionService.start(this)
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
                        onStart = { minutes ->
                            sessionStore.start(minutes)
                            FocusSessionService.start(this)
                        },
                        onStop = { FocusSessionService.stop(this) },
                        onManageBlockedApps = { showBlockingSettings = true },
                        serviceEnabled = accessibilityEnabled
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
    }
}

@Composable
private fun ZenHome(
    store: FocusSessionStore,
    onStart: (Int) -> Unit,
    onStop: () -> Unit,
    onManageBlockedApps: () -> Unit,
    serviceEnabled: Boolean
) {
    var selectedMinutes by remember { mutableIntStateOf(25) }
    var remaining by remember { mutableStateOf(store.remainingMillis()) }
    var active by remember { mutableStateOf(store.isActive()) }
    var todayMinutes by remember { mutableIntStateOf(store.todayCompletedMinutes()) }
    var todaySessions by remember { mutableIntStateOf(store.todayCompletedCount()) }

    LaunchedEffect(Unit) {
        while (isActive) {
            val nowActive = store.isActive()
            if (nowActive != active) {
                todayMinutes = store.todayCompletedMinutes()
                todaySessions = store.todayCompletedCount()
            }
            active = nowActive
            remaining = store.remainingMillis()
            delay(1_000L)
        }
    }

    val seconds = (remaining / 1000L).coerceAtLeast(0L)
    val timeText = "%02d:%02d".format(seconds / 60L, seconds % 60L)

    Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFFF5F7F5)) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 36.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text("WENODH ZEN", color = Color(0xFF176B55), fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            Text("Disconnect. Focus. Be present.", style = MaterialTheme.typography.bodyLarge, color = Color(0xFF65736C))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(if (active) "FOCUS IN PROGRESS" else "READY TO FOCUS", color = Color(0xFF65736C), fontSize = 12.sp, letterSpacing = 1.5.sp)
                    Text(if (active) timeText else "%02d:00".format(selectedMinutes), fontSize = 54.sp, fontWeight = FontWeight.Light, color = Color(0xFF183B30))
                    Text(if (active) "Stay with one thing." else "Choose a duration and begin.", color = Color(0xFF65736C))
                    if (active) {
                        Button(onClick = onStop, modifier = Modifier.fillMaxWidth()) { Text("End session") }
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(18.dp)) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Today's focus", fontWeight = FontWeight.SemiBold, color = Color(0xFF183B30))
                    Text("$todayMinutes minutes · $todaySessions completed sessions", color = Color(0xFF52645A), style = MaterialTheme.typography.bodyMedium)
                }
            }
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFE7EFEA)), shape = RoundedCornerShape(18.dp)) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("App restrictions", fontWeight = FontWeight.SemiBold, color = Color(0xFF183B30))
                    Text(
                        if (serviceEnabled) "Accessibility monitoring is enabled." else "Enable the service to enforce your selected blocked apps.",
                        color = Color(0xFF52645A), style = MaterialTheme.typography.bodyMedium
                    )
                    OutlinedButton(onClick = onManageBlockedApps, modifier = Modifier.fillMaxWidth()) { Text("Manage blocked apps") }
                }
            }
            Spacer(Modifier.height(4.dp))
            Text("LOCAL-FIRST · NO ACCOUNT REQUIRED", color = Color(0xFF829088), fontSize = 10.sp, letterSpacing = 1.sp)
        }
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
