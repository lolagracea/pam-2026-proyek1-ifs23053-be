package org.delcom.services

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.delcom.data.AppException
import org.delcom.data.DataResponse
import org.delcom.data.JobRequest
import org.delcom.data.ResponseJobAdd
import org.delcom.helpers.ServiceHelper
import org.delcom.helpers.ValidatorHelper
import org.delcom.repositories.IJobRepository
import org.delcom.repositories.IUserRepository
import java.io.File
import java.util.*

class JobService(
    private val userRepo: IUserRepository,
    private val jobRepo: IJobRepository
) {

    // Mengambil semua daftar lowongan dengan filter dan pagination (publik)
    suspend fun getAll(call: ApplicationCall) {
        val search = call.request.queryParameters["search"] ?: ""
        val isActiveParam = call.request.queryParameters["isActive"]
        val isActive = if (isActiveParam != null) isActiveParam.toBooleanStrictOrNull() else null
        val location = call.request.queryParameters["location"]
        val company = call.request.queryParameters["company"]
        val offset = call.request.queryParameters["offset"]?.toIntOrNull() ?: 0
        val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 10

        val jobs = jobRepo.getAll(
            userId = null,
            search = search,
            isActive = isActive,
            location = location,
            company = company,
            offset = offset,
            limit = limit
        )
        val response = DataResponse(
            "success",
            "Berhasil mengambil daftar lowongan kerja",
            mapOf("jobs" to jobs)
        )
        call.respond(response)
    }

    // Mengambil lowongan berdasarkan id (publik)
    suspend fun getById(call: ApplicationCall) {
        val jobId = call.parameters["id"] ?: throw AppException(400, "Data lowongan tidak valid!")
        val job = jobRepo.getById(jobId) ?: throw AppException(404, "Lowongan tidak ditemukan")
        val response = DataResponse(
            "success",
            "Berhasil mengambil data lowongan",
            mapOf("job" to job)
        )
        call.respond(response)
    }

    // Menambahkan lowongan baru (hanya user yang login)
    suspend fun post(call: ApplicationCall) {
        val user = ServiceHelper.getAuthUser(call, userRepo)
        val request = call.receive<JobRequest>()
        request.userId = user.id

        val validator = ValidatorHelper(request.toMap())
        validator.required("title", "Judul lowongan tidak boleh kosong")
        validator.required("description", "Deskripsi tidak boleh kosong")
        validator.required("company", "Nama perusahaan tidak boleh kosong")
        validator.required("location", "Lokasi tidak boleh kosong")
        validator.validate()

        val jobId = jobRepo.create(request.toEntity())
        val response = DataResponse(
            "success",
            "Berhasil menambahkan lowongan kerja",
            ResponseJobAdd(jobId)
        )
        call.respond(response)
    }

    // Mengubah data lowongan (hanya pemilik)
    suspend fun put(call: ApplicationCall) {
        val jobId = call.parameters["id"] ?: throw AppException(400, "Data lowongan tidak valid!")
        val user = ServiceHelper.getAuthUser(call, userRepo)

        val request = call.receive<JobRequest>()
        request.userId = user.id

        val validator = ValidatorHelper(request.toMap())
        validator.required("title", "Judul lowongan tidak boleh kosong")
        validator.required("description", "Deskripsi tidak boleh kosong")
        validator.required("company", "Nama perusahaan tidak boleh kosong")
        validator.required("location", "Lokasi tidak boleh kosong")
        validator.required("isActive", "Status aktif tidak boleh kosong")
        validator.validate()

        val oldJob = jobRepo.getById(jobId)
        if (oldJob == null || oldJob.userId != user.id) {
            throw AppException(404, "Data lowongan tidak tersedia!")
        }

        request.logo = oldJob.logo

        val isUpdated = jobRepo.update(user.id, jobId, request.toEntity())
        if (!isUpdated) {
            throw AppException(400, "Gagal memperbarui data lowongan!")
        }

        val response = DataResponse("success", "Berhasil mengubah data lowongan", null)
        call.respond(response)
    }

    // Mengubah logo lowongan
    suspend fun putLogo(call: ApplicationCall) {
        val jobId = call.parameters["id"] ?: throw AppException(400, "Data lowongan tidak valid!")
        val user = ServiceHelper.getAuthUser(call, userRepo)

        var newLogo: String? = null
        val multipartData = call.receiveMultipart(formFieldLimit = 1024 * 1024 * 5)
        multipartData.forEachPart { part ->
            when (part) {
                is PartData.FileItem -> {
                    val ext = part.originalFileName
                        ?.substringAfterLast('.', "")
                        ?.let { if (it.isNotEmpty()) ".$it" else "" }
                        ?: ""
                    val fileName = UUID.randomUUID().toString() + ext
                    val filePath = "uploads/jobs/$fileName"
                    val file = File(filePath)
                    file.parentFile.mkdirs()
                    part.provider().copyAndClose(file.writeChannel())
                    newLogo = filePath
                }
                else -> {}
            }
            part.dispose()
        }

        if (newLogo == null) {
            throw AppException(404, "Logo lowongan tidak tersedia!")
        }

        val oldJob = jobRepo.getById(jobId)
        if (oldJob == null || oldJob.userId != user.id) {
            throw AppException(404, "Data lowongan tidak tersedia!")
        }

        val newJob = oldJob.copy(
            logo = newLogo,
            updatedAt = kotlinx.datetime.Clock.System.now()
        )
        val isUpdated = jobRepo.update(user.id, jobId, newJob)
        if (!isUpdated) {
            throw AppException(400, "Gagal memperbarui logo lowongan!")
        }

        // Hapus logo lama jika ada
        oldJob.logo?.let { oldLogo ->
            File(oldLogo).takeIf { it.exists() }?.delete()
        }

        val response = DataResponse("success", "Berhasil mengubah logo lowongan", null)
        call.respond(response)
    }

    // Menghapus lowongan (hanya pemilik)
    suspend fun delete(call: ApplicationCall) {
        val jobId = call.parameters["id"] ?: throw AppException(400, "Data lowongan tidak valid!")
        val user = ServiceHelper.getAuthUser(call, userRepo)

        val oldJob = jobRepo.getById(jobId)
        if (oldJob == null || oldJob.userId != user.id) {
            throw AppException(404, "Data lowongan tidak tersedia!")
        }

        val isDeleted = jobRepo.delete(user.id, jobId)
        if (!isDeleted) {
            throw AppException(400, "Gagal menghapus data lowongan!")
        }

        // Hapus file logo jika ada
        oldJob.logo?.let { logoPath ->
            File(logoPath).takeIf { it.exists() }?.delete()
        }

        val response = DataResponse("success", "Berhasil menghapus data lowongan", null)
        call.respond(response)
    }

    // Mengambil logo lowongan
    suspend fun getLogo(call: ApplicationCall) {
        val jobId = call.parameters["id"] ?: throw AppException(400, "Data lowongan tidak valid!")
        val job = jobRepo.getById(jobId) ?: throw AppException(404, "Lowongan tidak ditemukan")
        val logoPath = job.logo ?: throw AppException(404, "Lowongan belum memiliki logo")
        val file = File(logoPath)
        if (!file.exists()) {
            throw AppException(404, "Logo lowongan tidak tersedia")
        }
        call.respondFile(file)
    }
}