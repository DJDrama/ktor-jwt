package com.dj.repository

import User
import java.util.*

class UserRepository {

    private val users = mutableListOf(
        User(
            id = UUID.randomUUID(),
            username = "admin",
            password = "password",
            role = "ADMIN"
        )
    )

    fun findAll(): List<User> = users

    fun findById(id: UUID): User? = users.find { it.id == id }

    fun findByUsername(username: String): User? = users.find { it.username == username }

    fun save(user: User): Boolean = users.add(user)

    fun delete(user: User): Boolean = users.remove(user)

}