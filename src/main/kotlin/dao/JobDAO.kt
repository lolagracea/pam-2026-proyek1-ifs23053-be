package org.delcom.dao

import org.delcom.tables.JobTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import java.util.UUID

class JobDAO(id: EntityID<UUID>) : Entity<UUID>(id) {
    companion object : EntityClass<UUID, JobDAO>(JobTable)

    var userId by JobTable.userId
    var title by JobTable.title
    var description by JobTable.description
    var company by JobTable.company
    var location by JobTable.location
    var salary by JobTable.salary
    var isActive by JobTable.isActive
    var logo by JobTable.logo
    var createdAt by JobTable.createdAt
    var updatedAt by JobTable.updatedAt
}