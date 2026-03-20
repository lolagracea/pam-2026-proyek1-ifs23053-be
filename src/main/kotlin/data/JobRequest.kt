package org.delcom.data

import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import org.delcom.entities.Job
import java.util.UUID

@Serializable
data class JobRequest(
    var userId: String = "",
    var title: String = "",
    var description: String = "",
    var company: String = "",
    var location: String = "",
    var salary: String? = null,
    var isActive: Boolean = true,
    var logo: String? = null,
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "title" to title,
            "description" to description,
            "company" to company,
            "location" to location,
            "salary" to salary,
            "isActive" to isActive
        )
    }

    fun toEntity(): Job {
        return Job(
            userId = userId,
            title = title,
            description = description,
            company = company,
            location = location,
            salary = salary,
            isActive = isActive,
            logo = logo,
            updatedAt = Clock.System.now()
        )
    }
}