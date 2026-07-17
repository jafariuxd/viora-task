package com.example.model

import androidx.annotation.DrawableRes

data class User(
    val id: String,
    val name: String,
    val username: String,
    @DrawableRes val avatarRes: Int? = null,
    val defaultDeadlineDays: Int
)

data class Team(
    val id: String,
    val name: String,
    val ownerId: String,
    val defaultDeadlineDays: Int? = null
)

data class TaskList(
    val id: String,
    val name: String,
    val teamId: String,
    val defaultDeadlineDays: Int? = null
)

