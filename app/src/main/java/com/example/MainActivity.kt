package com.example

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.example.ui.LauncherShell
import com.example.ui.LauncherViewModel
import com.example.ui.theme.MadarLauncherTheme

class MainActivity : ComponentActivity() {

    private val launcherViewModel: LauncherViewModel by viewModels()
    private var hasPromptedDefaultLauncherThisSession = false

    private val defaultLauncherRequest = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        launcherViewModel.refreshDefaultLauncherStatus(showFeedbackIfGranted = true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        setContent {
            MadarLauncherTheme {
                LauncherShell(
                    viewModel = launcherViewModel,
                    onRequestDefaultLauncher = { launchDefaultLauncherPrompt() }
                )
            }
        }

        // Automatically prompt to become the default Home Launcher as soon as the app opens
        if (savedInstanceState == null && !hasPromptedDefaultLauncherThisSession) {
            hasPromptedDefaultLauncherThisSession = true
            window.decorView.post {
                if (!isFinishing && !isDestroyed && !launcherViewModel.isDefaultHomeLauncher()) {
                    launchDefaultLauncherPrompt()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        launcherViewModel.refreshDefaultLauncherStatus()
        launcherViewModel.reloadInstalledApps()
    }

    private fun launchDefaultLauncherPrompt() {
        val intent = launcherViewModel.createDefaultLauncherRequestIntent()
        val launched = runCatching {
            defaultLauncherRequest.launch(intent)
            true
        }.getOrDefault(false)

        if (!launched) {
            val fallback = Intent(Settings.ACTION_HOME_SETTINGS)
            runCatching {
                defaultLauncherRequest.launch(fallback)
            }.onFailure {
                launcherViewModel.openDefaultLauncherSettings()
            }
        }
    }
}
