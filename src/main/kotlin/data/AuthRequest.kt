package org.delcom.data

import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import org.delcom.entities.User

@Serializable
data class AuthRequest(
    var name: String = "",
    var username: String = "",
    var password: String = "",
    var newPassword: String = "",
    var bio: String? = null,               // ← tambahan
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "name" to name,
            "username" to username,
            "password" to password,
            "newPassword" to newPassword,
            "bio" to bio
        )
    }

    fun toEntity(): User {
        return User(
            name = name,
            username = username,
            password = password,
            bio = bio,                     // ← tambahan
            updatedAt = Clock.System.now()
        )
    }
}