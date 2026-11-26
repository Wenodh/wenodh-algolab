package com.wenodh.zen.blocking

import android.content.Intent
import android.os.Bundle
import com.wenodh.zen.MainActivity
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class BlockedAppActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME).orEmpty()
        val label = runCatching {
            packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageName, 0)).toString()
        }.getOrDefault("This app")
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("Focus session active", style = MaterialTheme.typography.headlineSmall)
                        Text("$label is on your blocked list.", modifier = Modifier.padding(vertical = 16.dp))
                        Text("You can return to Wenodh Zen or end your focus session there.")
                        Button(
                            onClick = {
                                startActivity(
                                    Intent(this@BlockedAppActivity, MainActivity::class.java)
                                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                                )
                                finish()
                            },
                            modifier = Modifier.padding(top = 20.dp)
                        ) { Text("Return to Wenodh Zen") }
                    }
                }
            }
        }
    }

    companion object {
        const val EXTRA_PACKAGE_NAME = "blocked_package_name"
    }
}
