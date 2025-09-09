package uvg.edu.tripwise.data.model

import androidx.compose.ui.graphics.Color

data class User(
    val id: String,
    val name: String,
    val email: String,
    val initial: String,
    val isActive: Boolean,
    val avatarColor: Color
)