package org.example

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.example.db.MessageEntity
import org.example.db.MessageTable
import org.example.db.UserEntity
import org.example.db.UserTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.DatabaseConfig
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class DBConnection{
    private var db: Database
    private var hikari: HikariDataSource

    init {

        val hikariConfig: HikariConfig = HikariConfig().apply {
            val dbPort = 3306
            val dbHost = "44.203.47.64"
            val dbName = "chatroomdb"

            jdbcUrl = "jdbc:mariadb://$dbHost:$dbPort/$dbName"
            driverClassName = "org.mariadb.jdbc.Driver"
            username = "ubhack"
            password = "hacking"
        }

        val dbConfig = DatabaseConfig {
            useNestedTransactions = true
        }

        hikari = HikariDataSource(hikariConfig)
        db = Database.connect(datasource = hikari, databaseConfig = dbConfig)

        println("connected")
    }

    fun usernameTaken(name: String): Boolean {
        return transaction(db) {
            !UserTable.selectAll().where { UserTable.name eq name }.empty()
        }
    }

    fun removeUsername(name: String) {
        transaction(db) {
            UserEntity.find { UserTable.name eq name }.single().delete()
        }
    }

    fun addMessage(m: Message) {
        transaction(db) {
            MessageEntity.new {
                flag = m.FLAG
                user = m.USERNAME ?: "[[Unnamed User]]"
                text = m.MESSAGE
                timestamp = m.TIMESTAMP
            }
        }
    }

    fun backlog(): List<Message> {
        return transaction(db) {
            MessageTable.selectAll().orderBy(MessageTable.timestamp).map {
                Message(it[MessageTable.flag], it[MessageTable.text], it[MessageTable.user], it[MessageTable.timestamp])
            }
        }
    }
}