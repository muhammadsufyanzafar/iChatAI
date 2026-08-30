package com.zafar.ichatai.network

import com.google.gson.annotations.SerializedName
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

data class GitHubIssueRequest(
    @SerializedName("title")
    val title: String,

    @SerializedName("body")
    val body: String,

    @SerializedName("labels")
    val labels: List<String> = emptyList()
)

interface GitHubApiService {
    @POST("repos/{owner}/{repo}/issues")
    suspend fun createIssue(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Header("Authorization") token: String,
        @Body request: GitHubIssueRequest
    ): Response<ResponseBody>
}
