package org.delcom.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import org.delcom.entities.Job

@Serializable
data class JobRequest(
    var userId: String = "",
    var title: String = "",
    var description: String = "",
    var company: String = "",
    var location: String = "",
    var salary: String? = null,
    @SerialName("is_active") var isActive: Boolean = true,
    var logo: String? = null,
    var urlLogo: String = ""
) {
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
            urlLogo = urlLogo,
            // createdAt dan updatedAt akan diisi otomatis oleh default di entitas
        )
    }

    fun toMap(): Map<String, Any?> = mapOf(
        "title" to title,
        "description" to description,
        "company" to company,
        "location" to location,
        "salary" to salary,
        "isActive" to isActive,
        "logo" to logo,
        "urlLogo" to urlLogo
    )
}