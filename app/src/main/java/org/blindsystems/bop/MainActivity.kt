package org.blindsystems.bop

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import org.blindsystems.bop.ui.MainScreen
import org.blindsystems.bop.ui.theme.BopTheme
import org.blindsystems.bop.infra.UpdateManager

class MainActivity : ComponentActivity() {

    private val viewModel: BopViewModel by viewModels()
    private val updateManager by lazy { UpdateManager(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Initialize In-App Update
        updateManager.registerLauncher(this)
        updateManager.checkForUpdates()

        setContent {
            val state by viewModel.uiState.collectAsState()
            BopTheme(theme = state.theme) {
                MainScreen(vm = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateManager.registerListener()
        updateManager.resumeUpdate()
    }

    override fun onPause() {
        super.onPause()
        updateManager.unregisterListener()
    }
}
