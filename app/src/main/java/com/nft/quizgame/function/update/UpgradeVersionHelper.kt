package com.nft.quizgame.function.update

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.text.TextUtils
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentActivity
import com.base.services.version.Version
import com.nft.quizgame.common.PackageNames
import com.nft.quizgame.common.download.DownloadListenerAdapter
import com.nft.quizgame.common.download.DownloadManager
import com.nft.quizgame.common.download.DownloadStatus
import com.nft.quizgame.common.download.DownloadTask
import com.nft.quizgame.common.pref.PrivatePreference
import com.nft.quizgame.common.statistic.BaseSeq103OperationStatistic
import com.nft.quizgame.common.statistic.BaseSeq103OperationStatistic.APK_INSTALL_REMIND
import com.nft.quizgame.common.statistic.BaseSeq103OperationStatistic.DOWNLOAD_APK_SUCCESS
import com.nft.quizgame.common.statistic.BaseSeq103OperationStatistic.DOWNLOAD_PROGRESSBAR_SHOW
import com.nft.quizgame.common.statistic.BaseSeq103OperationStatistic.HIDE_BUTTON_CLICK
import com.nft.quizgame.common.statistic.BaseSeq103OperationStatistic.IGNORE_BUTTON_CLICK
import com.nft.quizgame.common.statistic.BaseSeq103OperationStatistic.INSTALL_BUTTON_CLICK
import com.nft.quizgame.common.statistic.BaseSeq103OperationStatistic.UPGRADE_BUTTON_CLICK
import com.nft.quizgame.common.statistic.BaseSeq103OperationStatistic.VERSION_UPGRADE_ALERT
import com.nft.quizgame.common.utils.FileUtils
import com.nft.quizgame.common.utils.Logcat
import com.nft.quizgame.common.utils.NetworkHelper
import com.nft.quizgame.dialog.QuizDownloadFinishDialog
import com.nft.quizgame.dialog.QuizDownloadingDialog
import com.nft.quizgame.dialog.QuizUpdateDialog
import com.nft.quizgame.function.signin.SignInManager
import java.io.File
import java.lang.ref.WeakReference

class UpgradeVersionHelper private constructor() {

    private var downloadingDialogWeak: WeakReference<QuizDownloadingDialog>? = null
    private var networkHelper: NetworkHelper? = null
    private lateinit var apkFile: File
    private lateinit var versionInfo: Version

    companion object {
        private const val FILE_PROVIDER: String = "funny.quizgame.fileprovider"
        private var mInstance: UpgradeVersionHelper? = null
        fun getInstance(): UpgradeVersionHelper {
            if (mInstance == null) {
                synchronized(UpgradeVersionHelper::class.java) {
                    if (mInstance == null) {
                        mInstance = UpgradeVersionHelper()
                    }
                }
            }
            return mInstance!!
        }
    }

    private fun restartDownload() {
        if (::apkFile.isInitialized && ::versionInfo.isInitialized) {
            Logcat.i("reStartDownload", "断网重连")
            DownloadManager.startDownload(versionInfo.url, apkFile.parent, apkFile.name, downloadListener)
        }
    }

    fun registerNetworkStateChange(context: Context) {
        if (networkHelper == null) {
            networkHelper = NetworkHelper(context)
            networkHelper?.init()
            networkHelper?.setNetWorkChangeListener(networkStateChangeListener)
        }
    }

    private fun unregisterNetWorkStateChange() {
        networkHelper?.release()
        networkHelper = null
    }

    fun startUpgradeAndDownloading(activity: FragmentActivity, version: Version) {
        versionInfo = version
        apkFile = getAppFileDir(activity)
        when (DownloadManager.getDownloadStatus(versionInfo.url, apkFile.parent, apkFile.name)) {
            DownloadStatus.STATUS_COMPLETED -> {
                if (AppUpdateManger.mDownloadFinishData.value == null) {
                    AppUpdateManger.mDownloadFinishData.value = VersionRecord(true, version.versionNumber)
                }
                return
            }
            DownloadStatus.STATUS_DOWNLOADING -> {
                return
            }
        }
        val updateDialog = QuizUpdateDialog(activity, versionInfo)
        updateDialog.setConfirmButtonCallback {
            BaseSeq103OperationStatistic.uploadData(obj = versionInfo.versionNumber.toString(), optionCode = UPGRADE_BUTTON_CLICK)
            startDownloadApk(activity)
        }
        updateDialog.setCancelButtonCallback {
            PrivatePreference.getPreference().putValue(VersionUpdateConst.KEY_SP_APP_VERSION, version.versionNumber).apply()
            BaseSeq103OperationStatistic.uploadData(obj = versionInfo.versionNumber.toString(), optionCode = IGNORE_BUTTON_CLICK)
        }
        updateDialog.setOnShowListener {
            BaseSeq103OperationStatistic.uploadData(obj = versionInfo.versionNumber.toString(), optionCode = VERSION_UPGRADE_ALERT)
        }
        updateDialog.show()
    }

    private fun startDownloadApk(activity: FragmentActivity) {
        var downloadingDialog = downloadingDialogWeak?.get()
        if (downloadingDialog == null) {
            downloadingDialog = QuizDownloadingDialog(activity)
            downloadingDialogWeak = WeakReference(downloadingDialog)
        }
        downloadingDialog.setCloseCallback {
            BaseSeq103OperationStatistic.uploadData(obj = versionInfo.versionNumber.toString(), optionCode = HIDE_BUTTON_CLICK)
        }
        downloadingDialog.setOnShowListener {
            BaseSeq103OperationStatistic.uploadData(obj = versionInfo.versionNumber.toString(), optionCode = DOWNLOAD_PROGRESSBAR_SHOW)
        }
        downloadingDialog.show()
        DownloadManager.startDownload(versionInfo.url, apkFile.parent, apkFile.name, downloadListener)
    }

    fun alreadyDownload(activity: FragmentActivity) {
        if (!::versionInfo.isInitialized) {
            return
        }
        downloadFinish(activity, getAppFileDir(activity))
    }

    private fun downloadFinish(activity: FragmentActivity, file: File) {
        val downloadFinishDialog = QuizDownloadFinishDialog(activity, versionInfo)
        downloadFinishDialog.setConfirmCallback {
            BaseSeq103OperationStatistic.uploadData(obj = versionInfo.versionNumber.toString(), optionCode = INSTALL_BUTTON_CLICK)
            installApk(activity, file)
        }
        downloadFinishDialog.setOnShowListener {
            BaseSeq103OperationStatistic.uploadData(obj = versionInfo.versionNumber.toString(), optionCode = APK_INSTALL_REMIND)
        }
        downloadFinishDialog.show()
    }


    private fun installApk(context: Context, file: File) {
        registerAppInstanceReceiver(context)
        val contentUri: Uri
        val intent = Intent(Intent.ACTION_VIEW)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                contentUri = FileProvider.getUriForFile(context, FILE_PROVIDER, file)
                startInstallActivity(context, contentUri, intent)
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> {
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                contentUri = FileProvider.getUriForFile(context, FILE_PROVIDER, file)
                startInstallActivity(context, contentUri, intent)
            }
            else -> {
                contentUri = Uri.fromFile(file)
                startInstallActivity(context, contentUri, intent)
            }
        }
    }

    private fun startInstallActivity(context: Context, contentUri: Uri, intent: Intent) {
        intent.setDataAndType(contentUri, "application/vnd.android.package-archive")
        context.startActivity(intent)
    }

    private var appInstallSuccessReceiver: AppInstallSuccessReceiver? = null

    private fun registerAppInstanceReceiver(context: Context) {
        if (appInstallSuccessReceiver == null) {
            appInstallSuccessReceiver = AppInstallSuccessReceiver()
            val intentFilter = IntentFilter(Intent.ACTION_PACKAGE_ADDED)
            intentFilter.addDataScheme("package")
            context.registerReceiver(appInstallSuccessReceiver, intentFilter)
        }
    }

    private fun unRegisterAppInstanceReceiver(context: Context) {
        appInstallSuccessReceiver?.let {
            context.unregisterReceiver(it)
        }
        appInstallSuccessReceiver = null
    }

    private fun getAppFileDir(context: Context): File {
        val folder = File(context.filesDir.path, "apk")
        if (!folder.exists()) folder.mkdirs()
        return File(folder, "quiz_game_${versionInfo.versionName}.apk")
    }

    private fun deleteApkFile() {
        if (::apkFile.isInitialized && ::versionInfo.isInitialized) {
            val listFiles = apkFile.parentFile.listFiles()
            listFiles.forEach { file ->
                if (file.exists() && file.name != apkFile.name) {
                    try {
                        val delete = FileUtils.deleteFile(file)
                        Logcat.i("delete", "success delete file : $delete")
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    private val networkStateChangeListener: NetworkHelper.NetWorkChangeListener =
            NetworkHelper.NetWorkChangeListener { _, isConnective, preNetworkStateIsNone ->
                if (isConnective && preNetworkStateIsNone) {
                    restartDownload()
                }
            }

    private val downloadListener = object : DownloadListenerAdapter() {

        override fun progress(task: DownloadTask) {
            val currentProgress = ((task.currentProgress / task.totalLength.toFloat()) * 100).toInt()
            val dialog = downloadingDialogWeak?.get()
            dialog?.let {
                if (it.isShowing) {
                    it.setProgress(currentProgress)
                }
            }
        }

        override fun completed(task: DownloadTask) {
            BaseSeq103OperationStatistic.uploadData(obj = versionInfo.versionNumber.toString(), optionCode = DOWNLOAD_APK_SUCCESS)
            AppUpdateManger.mDownloadFinishData.value = VersionRecord(true, versionInfo.versionNumber)
            downloadingDialogWeak?.get()?.dismiss()
            deleteApkFile()
        }

        override fun error(task: DownloadTask) {
            Logcat.i("error", "task.id:${task.id}")
            downloadingDialogWeak?.get()?.downloadError()
        }
    }

    inner class AppInstallSuccessReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            try {
                if (intent?.action.equals(Intent.ACTION_PACKAGE_ADDED)) {
                    val packageName = intent?.data?.schemeSpecificPart
                    if (!TextUtils.isEmpty(packageName)
                            && packageName!! == PackageNames.PACKAGE_NAME) {
                        val isDelete = FileUtils.deleteFile(getAppFileDir(context!!))
                        Logcat.i("delete", "delete is $isDelete")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    data class VersionRecord(var isDownloadFinish: Boolean, var versionCode: Int)


    fun release(context: Context) {
        unregisterNetWorkStateChange()
        unRegisterAppInstanceReceiver(context)
        stopDownload()
    }

    private fun stopDownload() {
        if (::versionInfo.isInitialized && ::apkFile.isInitialized) {
            DownloadManager.pauseDownload(versionInfo.url, apkFile.parent, apkFile.name)
        }
    }

}