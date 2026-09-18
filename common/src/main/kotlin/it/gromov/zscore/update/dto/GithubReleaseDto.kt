package it.gromov.zscore.update.dto

import com.google.gson.annotations.SerializedName

class GithubReleaseDto {
    @SerializedName("tag_name")
    var tagName: String? = null

    var assets: List<GithubAssetDto> = emptyList()
}
