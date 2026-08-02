package com.zafar.ichatai.model

data class UpdateInfo(
    val latestVersionCode: Int,
    val latestVersionName: String,
    val releaseDate: String,
    val isForceUpdate: Boolean,
    val changelog: List<String>,
    val platforms: List<String>,
    val seeMoreUrl: String,
)
