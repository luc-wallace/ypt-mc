package me.lucwallace.yptmc

import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.util.UUID

class Database(val name: String) {
    private var conn: Connection = DriverManager.getConnection("jdbc:sqlite:$name")

    init {
        conn.createStatement().execute(
            """
            CREATE TABLE IF NOT EXISTS users (
                uuid TEXT PRIMARY KEY,
                ypt_id INTEGER NOT NULL,
                total_minutes INTEGER,
                today_minutes INTEGER,
                server_minutes INTEGER,
                is_studying INTEGER
            )
        """.trimMargin()
        )
    }

    private fun scanUser(res: ResultSet): User {
        return User(
            UUID.fromString(res.getString("uuid")),
            res.getInt("ypt_id"),
            res.getInt("total_minutes"),
            res.getInt("today_minutes"),
            res.getInt("server_minutes"),
            res.getInt("is_studying") == 1
        )
    }

    fun getUsers(): List<User> {
        val res = conn.createStatement().executeQuery("SELECT * FROM users")
        val users = mutableListOf<User>()
        while (res.next()) {
            users.add(scanUser(res))
        }
        return users
    }

    fun getUserByUUID(uuid: UUID): User? {
        val stmt = conn.prepareStatement("SELECT * FROM users WHERE uuid = ?")
        stmt.setString(1, uuid.toString())
        val res = stmt.executeQuery()

        if (!res.next()) {
            return null
        }
        return scanUser(res)
    }

    fun getUserByYPTID(yptID: Int): User? {
        val stmt = conn.prepareStatement("SELECT * FROM users WHERE ypt_id = ?")
        stmt.setInt(1, yptID)
        val res = stmt.executeQuery()

        if (!res.next()) {
            return null
        }
        return scanUser(res)
    }

    fun updateUserStatus(yptID: Int, newMinutes: Int, isStudying: Boolean) {
        val stmt = conn.prepareStatement(
            """
            UPDATE users
            SET total_minutes = total_minutes + ? - today_minutes,
                today_minutes = ?,
                is_studying = ?
            WHERE ypt_id = ?""".trimIndent()
        )
        stmt.setInt(1, newMinutes)
        stmt.setInt(2, newMinutes)
        stmt.setInt(3, if (isStudying) 1 else 0)
        stmt.setInt(4, yptID)

        stmt.execute()
    }

    fun resetTodayMinutes() {
        conn.createStatement().execute("UPDATE users SET today_minutes = 0, server_minutes = 0")
    }

    fun tickPlayer(uuid: UUID, minutes: Int) {
        val stmt = conn.prepareStatement(
            """
            UPDATE users
            SET total_minutes = MAX(total_minutes - ?, 0),
                server_minutes = server_minutes + 1
            WHERE uuid = ?
        """.trimIndent()
        )
        stmt.setInt(1, minutes)
        stmt.setString(2, uuid.toString())
        stmt.executeUpdate()
    }

    fun registerUser(uuid: UUID, yptID: Int) {
        val stmt = conn.prepareStatement("INSERT INTO users VALUES (?, ?, 0, 0, 0, 0)")
        stmt.setString(1, uuid.toString())
        stmt.setInt(2, yptID)
        stmt.execute()
    }

    fun close() {
        conn.close()
    }
}