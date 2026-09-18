package it.gromov.zscore.update.dto

import com.google.gson.annotations.SerializedName

class GithubAssetDto {
    var name: String = ""

    @SerializedName("browser_download_url")
    var browserDownloadUrl: String = ""
}
