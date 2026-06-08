package com.dnd.helper.domain.common

sealed interface AppError {
    data object Network : AppError
    data object Unauthorized : AppError
    data object NotFound : AppError
    data class Unknown(val message: String) : AppError
}

fun AppError.toUserMessage(): String = when (this) {
    AppError.Network -> "Network error. Check your connection."
    AppError.NotFound -> "Not found."
    AppError.Unauthorized -> "Unauthorized."
    is AppError.Unknown -> message
}
