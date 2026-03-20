package org.delcom.tables

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object JobTable : UUIDTable("jobs") {
    val userId = uuid("user_id")
    val title = varchar("title", 200)
    val description = text("description")
    val company = varchar("company", 200)
    val location = varchar("location", 200)
    val salary = varchar("salary", 100).nullable()
    val isActive = bool("is_active")
    val logo = text("logo").nullable()
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}