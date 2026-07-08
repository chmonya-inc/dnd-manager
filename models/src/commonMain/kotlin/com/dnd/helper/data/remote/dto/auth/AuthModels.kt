package com.dnd.helper.data.remote.dto.auth

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val username: String,
    val password: String
)

@Serializable
data class RegisterRequest(
    val username: String,
    val password: String,
    val role: String = "PLAYER" // MASTER or PLAYER
)

@Serializable
data class RefreshRequest(
    val refreshToken: String
)

@Serializable
data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val user: UserDto
)

@Serializable
data class UserDto(
    val id: String,
    val username: String,
    val role: String = "PLAYER" // MASTER or PLAYER
)

@Serializable
data class AssignCharacterRequest(
    val characterId: String,
    val sessionId: String,
    val ownerUserId: String? // null to unassign
)

@Serializable
data class AssignByUsernameRequest(
    val characterId: String,
    val sessionId: String,
    val username: String? // null to unassign
)

@Serializable
data class MyCharacterDto(
    val character: com.dnd.helper.domain.model.Character,
    val sessionId: String,
    val campaignName: String?
)

@Serializable
data class CampaignDto(
    val id: String,
    val name: String,
    val ownerId: String,
    val sessionId: String
)
