package org.delcom.repositories

import org.delcom.dao.JobDAO
import org.delcom.entities.Job
import org.delcom.helpers.jobDAOToModel
import org.delcom.helpers.suspendTransaction
import org.delcom.tables.JobTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.lowerCase
import org.jetbrains.exposed.sql.or
import java.util.*

class JobRepository(private val baseUrl: String) : IJobRepository {

    override suspend fun getAll(
        userId: String?,
        search: String,
        isActive: Boolean?,
        location: String?,
        company: String?,
        offset: Int,
        limit: Int
    ): List<Job> = suspendTransaction {
        // Start with all rows
        var query = JobTable.selectAll()

        // Apply filters one by one using andWhere (safe even if none)
        if (userId != null) {
            query = query.andWhere { JobTable.userId eq UUID.fromString(userId) }
        }
        if (isActive != null) {
            query = query.andWhere { JobTable.isActive eq isActive }
        }
        if (!location.isNullOrBlank()) {
            query = query.andWhere { JobTable.location.lowerCase() like "%${location.lowercase()}%" }
        }
        if (!company.isNullOrBlank()) {
            query = query.andWhere { JobTable.company.lowerCase() like "%${company.lowercase()}%" }
        }
        if (search.isNotBlank()) {
            val keyword = "%${search.lowercase()}%"
            query = query.andWhere {
                (JobTable.title.lowerCase() like keyword) or
                        (JobTable.company.lowerCase() like keyword) or
                        (JobTable.location.lowerCase() like keyword)
            }
        }

        // Order and pagination
        query.orderBy(JobTable.createdAt to SortOrder.DESC)
            .limit(limit)
            .offset(offset.toLong())
            .map { row ->
                val dao = JobDAO.wrapRow(row)
                jobDAOToModel(dao, baseUrl)
            }
    }


    override suspend fun getById(jobId: String): Job? = suspendTransaction {
        JobDAO.find { JobTable.id eq UUID.fromString(jobId) }
            .limit(1)
            .map { jobDAOToModel(it, baseUrl) }
            .firstOrNull()
    }

    override suspend fun create(job: Job): String = suspendTransaction {
        val jobDAO = JobDAO.new {
            userId = UUID.fromString(job.userId)
            title = job.title
            description = job.description
            company = job.company
            location = job.location
            salary = job.salary
            isActive = job.isActive
            logo = job.logo
            createdAt = job.createdAt
            updatedAt = job.updatedAt
        }
        jobDAO.id.value.toString()
    }

    override suspend fun update(userId: String, jobId: String, newJob: Job): Boolean = suspendTransaction {
        val jobDAO = JobDAO.find {
            (JobTable.id eq UUID.fromString(jobId)) and
                    (JobTable.userId eq UUID.fromString(userId))
        }.limit(1).firstOrNull()

        if (jobDAO != null) {
            jobDAO.title = newJob.title
            jobDAO.description = newJob.description
            jobDAO.company = newJob.company
            jobDAO.location = newJob.location
            jobDAO.salary = newJob.salary
            jobDAO.isActive = newJob.isActive
            jobDAO.logo = newJob.logo
            jobDAO.updatedAt = newJob.updatedAt
            true
        } else {
            false
        }
    }

    override suspend fun delete(userId: String, jobId: String): Boolean = suspendTransaction {
        val rowsDeleted = JobTable.deleteWhere {
            (JobTable.id eq UUID.fromString(jobId)) and
                    (JobTable.userId eq UUID.fromString(userId))
        }
        rowsDeleted >= 1
    }
}