package com.dnd.helper.di

import org.koin.core.module.Module

expect val platformModule: Module

expect val isDesktop: Boolean

expect fun openUrl(url: String)

expect fun pickFile(title: String, allowedExtensions: List<String>): String?

expect fun readFileContent(path: String): String?
