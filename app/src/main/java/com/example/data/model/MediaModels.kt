package com.example.data.model

data class MediaItem(
    val id: String,
    val title: String,
    val uriString: String,
    val isVideo: Boolean,
    val durationMs: Long,
    val sizeBytes: Long,
    val format: String,
    val author: String = "Swordfish Audio/Video",
    val rawResId: Int? = null,
    val dateAddedMs: Long = System.currentTimeMillis()
)

data class MediaDownloadTask(
    val id: String,
    val url: String,
    val title: String,
    val format: String,
    val isVideo: Boolean,
    val progress: Float = 0f,
    val status: DownloadStatus = DownloadStatus.QUEUED,
    val errorMessage: String? = null
)

enum class DownloadStatus {
    QUEUED,
    DOWNLOADING,
    COMPLETED,
    FAILED
}

data class CliToolInfo(
    val name: String,
    val version: String,
    val path: String,
    val role: String,
    val status: String = "Active & Ready"
)
