package com.dnd.helper.domain.common

import kotlin.test.Test
import kotlin.test.assertEquals

class AppErrorTest {

    @Test
    fun testToUserMessage() {
        assertEquals("Network error. Check your connection.", AppError.Network.toUserMessage())
        assertEquals("Not found.", AppError.NotFound.toUserMessage())
        assertEquals("Unauthorized.", AppError.Unauthorized.toUserMessage())
        assertEquals("Something went wrong", AppError.Unknown("Something went wrong").toUserMessage())
    }
}
