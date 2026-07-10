package com.dnd.helper.data.remote.dto.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    @SerialName("username") val username: String,
    @SerialName("password") val password: String
)

@Serializable
data class RegisterRequest(
    @SerialName("username") val username: String,
    @SerialName("password") val password: String,
    @SerialName("role") val role: String = "PLAYER" // MASTER or PLAYER
)

@Serializable
data class PasswordRecoveryRequest(
    @SerialName("username") val username: String,
    @SerialName("oldPasswordOrCode") val oldPasswordOrCode: String,
    @SerialName("newPassword") val newPassword: String
)

@Serializable
data class RefreshRequest(
    @SerialName("refreshToken") val refreshToken: String
)

@Serializable
data class AuthResponse(
    @SerialName("accessToken") val accessToken: String,
    @SerialName("refreshToken") val refreshToken: String,
    @SerialName("user") val user: UserDto,
    @SerialName("recoverCode") val recoverCode: String? = null
)

@Serializable
data class UserDto(
    @SerialName("id") val id: String,
    @SerialName("username") val username: String,
    @SerialName("role") val role: String = "PLAYER" // MASTER or PLAYER
)

@Serializable
data class AssignCharacterRequest(
    @SerialName("characterId") val characterId: String,
    @SerialName("sessionId") val sessionId: String,
    @SerialName("ownerUserId") val ownerUserId: String? // null to unassign
)

@Serializable
data class AssignByUsernameRequest(
    @SerialName("characterId") val characterId: String,
    @SerialName("sessionId") val sessionId: String,
    @SerialName("username") val username: String? // null to unassign
)

@Serializable
data class MyCharacterDto(
    @SerialName("character") val character: com.dnd.helper.domain.model.Character,
    @SerialName("sessionId") val sessionId: String,
    @SerialName("campaignName") val campaignName: String?
)

@Serializable
data class CampaignDto(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("ownerId") val ownerId: String,
    @SerialName("sessionId") val sessionId: String
)

@Serializable
data class PendingAssignmentDto(
    @SerialName("assignmentId") val assignmentId: String,
    @SerialName("character") val character: com.dnd.helper.domain.model.Character,
    @SerialName("sessionId") val sessionId: String,
    @SerialName("campaignName") val campaignName: String?,
    @SerialName("status") val status: String,
    @SerialName("masterUsername") val masterUsername: String? = null
)

@Serializable
data class CreateAssignmentRequest(
    @SerialName("characterId") val characterId: String,
    @SerialName("sessionId") val sessionId: String,
    @SerialName("playerUsername") val playerUsername: String
)

@Serializable
data class RespondAssignmentRequest(
    @SerialName("assignmentId") val assignmentId: String,
    @SerialName("accept") val accept: Boolean
)

@Serializable
data class AssignmentStatusDto(
    @SerialName("assignmentId") val assignmentId: String,
    @SerialName("characterId") val characterId: String,
    @SerialName("characterName") val characterName: String,
    @SerialName("sessionId") val sessionId: String,
    @SerialName("status") val status: String,
    @SerialName("playerUsername") val playerUsername: String? = null
)
