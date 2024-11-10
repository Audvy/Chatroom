package org.example.db

import org.example.Constants
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

object MessageTable : IntIdTable("messages") {
    val text = text("text").nullable()
    val user = text("user").nullable()
    val timestamp = long("timestamp")
    val flag = customEnumeration("flags", toDb = {f -> Constants.toString(f)}, fromDb = {f -> Constants.fromString(f as String)})
}

class MessageEntity(id: EntityID<Int>) : IntEntity(id) {
    var text: String? by MessageTable.text
    var user: String? by MessageTable.user
    var timestamp: Long by MessageTable.timestamp
    var flag: Constants.MESSAGE_FLAGS by MessageTable.flag

    companion object : IntEntityClass<MessageEntity>(MessageTable)
}