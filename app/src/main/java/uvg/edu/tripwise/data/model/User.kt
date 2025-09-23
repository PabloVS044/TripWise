package uvg.edu.tripwise.data.model

import androidx.compose.ui.graphics.Color
import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("_id") val id: String,
    val name: String,
    val email: String,
    val deleted: Deleted?
)

data class Deleted(
    @SerializedName("is") val isDeleted: Boolean,
    val at: String? = null
)