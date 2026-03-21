package org.delcom.repositories

import org.delcom.entities.Job

interface IJobRepository {
    suspend fun getAll(
        userId: String?,
        search: String,
        isActive: Boolean?,
        location: String?,
        company: String?,
        offset: Int,
        limit: Int
    ): List<Job>

    suspend fun getById(jobId: String): Job?
    suspend fun create(job: Job): String
    suspend fun update(userId: String, jobId: String, newJob: Job): Boolean
    suspend fun delete(userId: String, jobId: String): Boolean
}