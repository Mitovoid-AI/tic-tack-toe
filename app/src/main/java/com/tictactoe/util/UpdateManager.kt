package com.tictactoe.util

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume

@Serializable
data class GitHubRelease(
    val tag_name: String = "",
    val name: String = "",
    val body: String = "",
    val assets: List<GitHubAsset> = emptyList()
)

@Serializable
data class GitHubAsset(
    val name: String = "",
    val browser_download_url: String = ""
)

object UpdateManager {

    private const val REPO_API =
        "https://api.github.com/repos/Mitovoid-AI/tic-tack-toe/releases/latest"

    private val json = Json { ignoreUnknownKeys = true }

    private val client = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()

    /**
     * Returns a [UpdateInfo] if a newer version is available, or null if
     * already up-to-date or the check fails.
     */
    suspend fun checkForUpdate(context: Context): UpdateInfo? =
        withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder().url(REPO_API).build()
                val response = client.newCall(request).execute()
                if (!response.isSuccessful) return@withContext null

                val body = response.body?.string() ?: return@withContext null
                val release = json.decodeFromString<GitHubRelease>(body)

                val currentVersion = getVersionCode(context)
                val remoteTag = release.tag_name          // e.g. "v1.23"
                val remoteVersion = remoteTag
                    .removePrefix("v")
                    .substringBefore(".")
                    .toIntOrNull() ?: return@withContext null

                if (remoteVersion <= currentVersion) return@withContext null

                val apkAsset = release.assets.firstOrNull {
                    it.name.endsWith(".apk")
                } ?: return@withContext null

                UpdateInfo(
                    version = remoteTag,
                    releaseName = release.name,
                    releaseNotes = release.body,
                    downloadUrl = apkAsset.browser_download_url
                )
            } catch (_: Exception) {
                null
            }
        }

    fun getVersionCode(context: Context): Int = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager
                .getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
                .longVersionCode.toInt()
        } else {
            @Suppress("DEPRECATION")
            context.packageManager
                .getPackageInfo(context.packageName, 0)
                .versionCode
        }
    } catch (_: Exception) { 0 }

    fun getVersionName(context: Context): String = try {
        context.packageManager
            .getPackageInfo(context.packageName, 0)
            .versionName ?: "1.0.0"
    } catch (_: Exception) { "1.0.0" }

    /**
     * Downloads the APK via Android DownloadManager and returns the local Uri
     * once complete so the caller can fire an install intent.
     */
    suspend fun downloadApk(context: Context, url: String): Uri? {
        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle("Tic Tac Toe Update")
            .setDescription("Downloading update...")
            .setNotificationVisibility(
                DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
            )
            .setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                "tic-tac-toe-update.apk"
            )
            .setMimeType("application/vnd.android.package-archive")

        val downloadId = dm.enqueue(request)

        // Suspend until download completes
        return suspendCancellableCoroutine { cont ->
            val receiver = object : BroadcastReceiver() {
                override fun onReceive(ctx: Context?, intent: Intent?) {
                    val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                    if (id == downloadId) {
                        context.unregisterReceiver(this)
                        val uri = dm.getUriForDownloadedFile(downloadId)
                        if (cont.isActive) cont.resume(uri)
                    }
                }
            }

            val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
            } else {
                context.registerReceiver(receiver, filter)
            }

            cont.invokeOnCancellation {
                try { context.unregisterReceiver(receiver) } catch (_: Exception) {}
            }
        }
    }

    fun installApk(context: Context, apkUri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}

data class UpdateInfo(
    val version: String,
    val releaseName: String,
    val releaseNotes: String,
    val downloadUrl: String
)
