package org.delcom.repositories

import org.delcom.entities.Job

interface IJobRepository {
    suspend fun getAll(
        search: String = "",
        isActive: Boolean? = null,
        location: String? = null,
        company: String? = null,
        offset: Int = 0,
        limit: Int = 10
    ): List<Job>

    suspend fun getById(jobId: String): Job?
    suspend fun create(job: Job): String
    suspend fun update(userId: String, jobId: String, newJob: Job): Boolean
    suspend fun delete(userId: String, jobId: String): Boolean
}