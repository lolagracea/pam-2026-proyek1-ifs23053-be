package org.delcom.data

import kotlinx.serialization.Serializable

@Serializable
data class ResponseJobAdd(
    val jobId: String
)