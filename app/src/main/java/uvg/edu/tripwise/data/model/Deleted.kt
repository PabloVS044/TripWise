package uvg.edu.tripwise.data.model

import com.google.gson.annotations.SerializedName

data class Deleted(
    @SerializedName("is") val isDeleted: Boolean,
    val at: String? = null
)