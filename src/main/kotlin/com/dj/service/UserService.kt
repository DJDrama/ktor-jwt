package com.dj.service

import User
import com.dj.repository.UserRepository
import java.util.*

class UserService(
    private val repository: UserRepository
) {
    fun findAll(): List<User> = repository.findAll()

    fun findById(id: String): User? = repository.findById(id = UUID.fromString(id))

    fun findByUsername(username: String): User? = repository.findByUsername(username = username)

    fun save(user: User): User? {
        val foundUser = findByUsername(user.username)
        return if (foundUser == null) {
            repository.save(user = user)
            user
        } else
            null
    }

}