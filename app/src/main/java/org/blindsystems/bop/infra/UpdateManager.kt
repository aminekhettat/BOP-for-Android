package org.blindsystems.bop.infra

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability

/**
 * Helper class to manage Google Play In-App Updates.
 * Handles both Flexible and Immediate update flows.
 */
class UpdateManager(private val context: Context) {

    private val appUpdateManager: AppUpdateManager = AppUpdateManagerFactory.create(context)
    private var updateLauncher: ActivityResultLauncher<IntentSenderRequest>? = null

    /**
     * Listener for flexible update progress.
     */
    private val installListener = InstallStateUpdatedListener { state ->
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            Log.d("UpdateManager", "Update downloaded. Ready to install.")
            // You could trigger a UI notification here to ask the user to restart
            // For BOP, we might just notify the ViewModel
        }
    }

    /**
     * Registers the activity result launcher. Must be called in Activity.onCreate.
     */
    fun registerLauncher(activity: ComponentActivity) {
        updateLauncher = activity.registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->
            if (result.resultCode != Activity.RESULT_OK) {
                Log.e("UpdateManager", "Update flow failed! Result code: ${result.resultCode}")
            }
        }
    }

    /**
     * Checks for available updates and starts the flow if found.
     * For BOP, we use FLEXIBLE update to not interrupt the user's practice session.
     */
    fun checkForUpdates() {
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo

        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
            ) {
                startUpdateFlow(appUpdateInfo, AppUpdateType.FLEXIBLE)
            }
        }
    }

    /**
     * Resumes an update that is already in progress.
     */
    fun resumeUpdate() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                // Update downloaded but not installed
                completeUpdate()
            } else if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                // Immediate update in progress
                startUpdateFlow(appUpdateInfo, AppUpdateType.IMMEDIATE)
            }
        }
    }

    private fun startUpdateFlow(appUpdateInfo: AppUpdateInfo, updateType: Int) {
        val options = AppUpdateOptions.newBuilder(updateType).build()
        
        appUpdateManager.startUpdateFlowForResult(
            appUpdateInfo,
            updateLauncher ?: return,
            options
        )
    }

    /**
     * Completes the update by restarting the app.
     */
    fun completeUpdate() {
        appUpdateManager.completeUpdate()
    }

    /**
     * Register the listener for flexible updates.
     */
    fun registerListener() {
        appUpdateManager.registerListener(installListener)
    }

    /**
     * Unregister the listener to avoid memory leaks.
     */
    fun unregisterListener() {
        appUpdateManager.unregisterListener(installListener)
    }
}
