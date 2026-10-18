package com.example.util

import android.content.Context
import com.example.model.User
import java.util.concurrent.ConcurrentHashMap

object UserAvatarRegistry {
    private val avatarMap = ConcurrentHashMap<String, String>()
    @Volatile
    private var currentUserAvatar: String? = null
    @Volatile
    private var currentUserName: String? = null
    @Volatile
    private var currentUserHandle: String? = null

    fun init(context: Context) {
        try {
            val prefs = context.getSharedPreferences("viora_auth_prefs", Context.MODE_PRIVATE)
            val rawAvatar = prefs.getString("user_avatar_uri", null)
            val username = prefs.getString("user_username", null)
            val name = prefs.getString("user_name", null)
            val resolved = ImageUtil.resolveAvatarUrl(rawAvatar)
            setCurrentUser(name, username, resolved)
        } catch (_: Exception) {}
    }

    fun setCurrentUser(name: String?, username: String?, avatarUrl: String?, userId: String? = null) {
        val resolved = ImageUtil.resolveAvatarUrl(avatarUrl)
        if (!resolved.isNullOrEmpty()) {
            currentUserAvatar = resolved
            username?.let {
                avatarMap[it] = resolved
                avatarMap[it.lowercase()] = resolved
            }
            userId?.let {
                avatarMap[it] = resolved
                avatarMap[it.lowercase()] = resolved
            }
            name?.let {
                avatarMap[it] = resolved
                avatarMap[it.lowercase()] = resolved
            }
            avatarMap["user1"] = resolved
            avatarMap["me"] = resolved
            avatarMap["user"] = resolved
        }
        currentUserName = name
        currentUserHandle = username
    }

    fun register(userId: String?, username: String?, avatarUrl: String?, fullName: String? = null) {
        val resolved = ImageUtil.resolveAvatarUrl(avatarUrl) ?: return
        if (resolved.isNotEmpty()) {
            userId?.let {
                avatarMap[it] = resolved
                avatarMap[it.lowercase()] = resolved
            }
            username?.let {
                avatarMap[it] = resolved
                avatarMap[it.lowercase()] = resolved
            }
            fullName?.let {
                avatarMap[it] = resolved
                avatarMap[it.lowercase()] = resolved
            }
        }
    }

    fun register(user: User) {
        register(user.id, user.username, user.avatarUrl, user.name)
    }

    fun registerAll(users: Collection<User>) {
        users.forEach { register(it) }
    }

    fun getAvatarUrl(key: String?): String? {
        if (key.isNullOrBlank()) return null
        val trimmed = key.trim()

        // 1. If key is directly a URL, path, content or data URI
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://") ||
            trimmed.startsWith("/uploads/") || trimmed.startsWith("uploads/") ||
            trimmed.startsWith("content://") || trimmed.startsWith("file://") ||
            trimmed.startsWith("data:image/") ||
            (trimmed.startsWith("/") && trimmed.contains("."))
        ) {
            return ImageUtil.resolveAvatarUrl(trimmed)
        }

        // 2. Direct lookup in map
        avatarMap[trimmed]?.let { return it }
        avatarMap[trimmed.lowercase()]?.let { return it }

        // Strip leading '@' if present
        if (trimmed.startsWith("@")) {
            val handle = trimmed.substring(1)
            avatarMap[handle]?.let { return it }
            avatarMap[handle.lowercase()]?.let { return it }
        }

        // 3. Current user check
        val lowerKey = trimmed.lowercase()
        if (lowerKey == "me" || lowerKey == "user1" || lowerKey == "user" ||
            lowerKey == currentUserHandle?.lowercase() ||
            lowerKey == currentUserName?.lowercase() ||
            lowerKey == "@${currentUserHandle?.lowercase()}"
        ) {
            return currentUserAvatar
        }

        return null
    }

    fun getAllAvatars(): Map<String, String> = avatarMap
}
