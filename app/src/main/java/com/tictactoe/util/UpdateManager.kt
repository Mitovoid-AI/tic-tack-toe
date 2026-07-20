package com.tictactoe.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.concurrent.TimeUnit

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

    private val downloadClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    suspend fun checkForUpdate(context: Context): UpdateInfo? =
        withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder().url(REPO_API).build()
                val response = client.newCall(request).execute()
                if (!response.isSuccessful) return@withContext null

                val body = response.body?.string() ?: return@withContext null
                val release = json.decodeFromString<GitHubRelease>(body)

                val remoteTag = release.tag_name  // e.g. "v1.19"
                val currentName = getVersionName(context) // e.g. "1.19"
                val remoteName = remoteTag.removePrefix("v") // e.g. "1.19"

                val isNewer = compareVersions(remoteName, currentName) > 0

                if (!isNewer) return@withContext null

                val apkAsset = release.assets.firstOrNull {
                    it.name.endsWith(".apk")
                } ?: return@withContext null

                // Parse release notes: max 3 bullet points, no links
                val notes = parseReleaseNotes(release.body)

                UpdateInfo(
                    version = remoteTag,
                    releaseName = release.name,
                    releaseNotes = notes,
                    downloadUrl = apkAsset.browser_download_url
                )
            } catch (_: Exception) {
                null
            }
        }

    private fun parseReleaseNotes(raw: String): String {
        if (raw.isBlank()) return ""
        // Extract bullet points (lines starting with - or *)
        val bullets = raw.lines()
            .map { it.trim() }
            .filter { it.startsWith("-") || it.startsWith("*") }
            .map { it.removePrefix("-").removePrefix("*").trim() }
            .filter { it.isNotBlank() }
            // Remove markdown links [text](url)
            .map { it.replace(Regex("\\[.*?\\]\\(.*?\\)"), "").trim() }
            // Remove bare URLs
            .map { it.replace(Regex("https?://\\S+"), "").trim() }
            .filter { it.isNotBlank() }
            .take(3)
        return bullets.joinToString("\n")
    }

    private fun compareVersions(v1: String, v2: String): Int {
        val parts1 = v1.split(".").map { it.toIntOrNull() ?: 0 }
        val parts2 = v2.split(".").map { it.toIntOrNull() ?: 0 }
        val maxLen = maxOf(parts1.size, parts2.size)
        for (i in 0 until maxLen) {
            val p1 = parts1.getOrElse(i) { 0 }
            val p2 = parts2.getOrElse(i) { 0 }
            if (p1 != p2) return p1 - p2
        }
        return 0
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
     * Downloads APK using OkHttp directly to app's cache directory.
     * Returns the downloaded File, or null on failure.
     */
    suspend fun downloadApk(context: Context, url: String): File? =
        withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder().url(url).build()
                val response = downloadClient.newCall(request).execute()
                if (!response.isSuccessful) return@withContext null

                val apkFile = File(context.cacheDir, "update.apk")
                response.body?.byteStream()?.use { input ->
                    apkFile.outputStream().use { output ->
                        input.copyTo(output, bufferSize = 8192)
                    }
                }
                if (apkFile.exists() && apkFile.length() > 0) apkFile else null
            } catch (_: Exception) {
                null
            }
        }

    fun installApk(context: Context, apkFile: File) {
        val uri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )
        } else {
            Uri.fromFile(apkFile)
        }

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
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
