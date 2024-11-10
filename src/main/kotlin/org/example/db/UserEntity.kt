package org.example.db

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable

object UserTable : LongIdTable("users", columnName = "uuid") {
    val name = text("username").index()
}

class UserEntity(id: EntityID<Long>) : LongEntity(id) {
    var name: String by UserTable.name

    companion object : LongEntityClass<UserEntity>(UserTable)
}