package com.wenodh.zen

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.lifecycle.lifecycleScope
import com.wenodh.zen.focus.FocusSessionService
import com.wenodh.zen.focus.FocusSessionStore
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val notificationPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        val store = FocusSessionStore(this)
        setContent {
            MaterialTheme(colorScheme = lightColorScheme(
                primary = Color(0xFF176B55),
                secondary = Color(0xFF5B746B),
                background = Color(0xFFF5F7F5),
                surface = Color.White
            )) {
                ZenHome(
                    store = store,
                    onStart = { minutes ->
                        store.start(minutes)
                        FocusSessionService.start(this)
                    },
                    onStop = { FocusSessionService.stop(this) }
                )
            }
        }
    }
}

@Composable
private fun ZenHome(
    store: FocusSessionStore,
    onStart: (Int) -> Unit,
    onStop: () -> Unit
) {
    var selectedMinutes by remember { mutableIntStateOf(25) }
    var remaining by remember { mutableStateOf(store.remainingMillis()) }
    var active by remember { mutableStateOf(store.isActive()) }

    LaunchedEffect(Unit) {
        while (isActive) {
            active = store.isActive()
            remaining = store.remainingMillis()
            store.finishIfExpired()
            delay(500L)
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
                                OutlinedButton(
                                    onClick = { selectedMinutes = minutes },
                                    shape = RoundedCornerShape(12.dp)
                                ) { Text("$minutes") }
                            }
                        }
                        Button(onClick = { onStart(selectedMinutes) }, modifier = Modifier.fillMaxWidth()) {
                            Text("Start focus")
                        }
                    }
                }
            }
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE7EFEA)),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("First things first", fontWeight = FontWeight.SemiBold, color = Color(0xFF183B30))
                    Text("Your focus timer is saved on this device. App blocking, profiles and schedules are coming in the next milestones.", color = Color(0xFF52645A), style = MaterialTheme.typography.bodyMedium)
                }
            }
            Spacer(Modifier.height(4.dp))
            Text("LOCAL-FIRST · NO ACCOUNT REQUIRED", color = Color(0xFF829088), fontSize = 10.sp, letterSpacing = 1.sp)
        }
    }
}
