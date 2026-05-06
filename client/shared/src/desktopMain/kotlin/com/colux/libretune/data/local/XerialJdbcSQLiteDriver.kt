package com.colux.libretune.data.local

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.SQLiteDriver
import androidx.sqlite.SQLiteStatement
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Types

// SQLite column type constants (mirrors androidx.sqlite.SQLite.SQLITE_DATA_*)
private const val SQLITE_INTEGER = 1
private const val SQLITE_FLOAT   = 2
private const val SQLITE_TEXT    = 3
private const val SQLITE_BLOB    = 4
private const val SQLITE_NULL    = 5

/**
 * [SQLiteDriver] implementation backed by [org.xerial:sqlite-jdbc].
 * Used on desktop instead of [BundledSQLiteDriver] which crashes on some
 * Linux/JBR configurations due to a JNI C++ stdlib ABI conflict.
 */
internal class XerialJdbcSQLiteDriver : SQLiteDriver {
    init {
        Class.forName("org.sqlite.JDBC")
    }

    override fun open(fileName: String): SQLiteConnection {
        val url = if (fileName.startsWith("jdbc:sqlite:")) fileName
                  else "jdbc:sqlite:$fileName"
        val conn = DriverManager.getConnection(url)
        // Enable WAL mode for concurrent reader/writer access
        conn.createStatement().use { it.execute("PRAGMA journal_mode=WAL") }
        return XerialJdbcSQLiteConnection(conn)
    }
}

private class XerialJdbcSQLiteConnection(
    private val conn: java.sql.Connection
) : SQLiteConnection {
    override fun prepare(sql: String): SQLiteStatement =
        XerialJdbcSQLiteStatement(conn.prepareStatement(sql))

    override fun close() = conn.close()
}

private class XerialJdbcSQLiteStatement(
    private val stmt: PreparedStatement,
) : SQLiteStatement {

    private var resultSet: ResultSet? = null
    private var executed = false

    // --- bind ---

    override fun bindBlob(index: Int, value: ByteArray) = stmt.setBytes(index, value)
    override fun bindDouble(index: Int, value: Double) = stmt.setDouble(index, value)
    override fun bindLong(index: Int, value: Long) = stmt.setLong(index, value)
    override fun bindText(index: Int, value: String) = stmt.setString(index, value)
    override fun bindNull(index: Int) = stmt.setNull(index, Types.NULL)

    // --- get ---

    override fun getBlob(index: Int): ByteArray = resultSet!!.getBytes(index + 1) ?: ByteArray(0)
    override fun getDouble(index: Int): Double = resultSet!!.getDouble(index + 1)
    override fun getLong(index: Int): Long = resultSet!!.getLong(index + 1)
    override fun getText(index: Int): String = resultSet!!.getString(index + 1) ?: ""
    override fun isNull(index: Int): Boolean = resultSet!!.getObject(index + 1) == null

    override fun getColumnCount(): Int {
        if (resultSet != null) return resultSet!!.metaData.columnCount
        return try { stmt.metaData?.columnCount ?: 0 } catch (_: Exception) { 0 }
    }

    override fun getColumnName(index: Int): String {
        if (resultSet != null) return resultSet!!.metaData.getColumnLabel(index + 1) ?: ""
        return try { stmt.metaData?.getColumnLabel(index + 1) ?: "" } catch (_: Exception) { "" }
    }

    override fun getColumnType(index: Int): Int {
        val rs = resultSet ?: return SQLITE_NULL
        return when (rs.getObject(index + 1)) {
            null                               -> SQLITE_NULL
            is Long, is Int, is Short, is Byte -> SQLITE_INTEGER
            is Double, is Float                -> SQLITE_FLOAT
            is ByteArray                       -> SQLITE_BLOB
            else                               -> SQLITE_TEXT
        }
    }

    // --- lifecycle ---

    override fun step(): Boolean {
        if (!executed) {
            executed = true
            val isQuery = stmt.execute()
            resultSet = if (isQuery) stmt.resultSet else null
            if (resultSet == null) return false
        }
        return resultSet?.next() ?: false
    }

    override fun reset() {
        resultSet?.close()
        resultSet = null
        executed = false
    }

    override fun clearBindings() = stmt.clearParameters()

    override fun close() {
        resultSet?.close()
        stmt.close()
    }
}
