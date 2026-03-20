package org.delcom.repositories

import org.delcom.dao.JobDAO
import org.delcom.entities.Job
import org.delcom.helpers.suspendTransaction
import org.delcom.helpers.jobDAOToModel
import org.delcom.tables.JobTable
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.lowerCase
import org.jetbrains.exposed.sql.or
import java.util.*

class JobRepository(private val baseUrl: String) : IJobRepository {

    override suspend fun getAll(search: String): List<Job> = suspendTransaction {
        if (search.isBlank()) {
            JobDAO.all()
                .map { jobDAOToModel(it, baseUrl) }
        } else {
            val keyword = "%${search.lowercase()}%"
            JobDAO.find {
                (JobTable.title.lowerCase() like keyword) or
                        (JobTable.company.lowerCase() like keyword) or
                        (JobTable.location.lowerCase() like keyword)
            }
                .map { jobDAOToModel(it, baseUrl) }
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