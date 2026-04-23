package com.youxiang8727.heroadventure

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform