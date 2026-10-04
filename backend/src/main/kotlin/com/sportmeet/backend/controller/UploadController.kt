package com.sportmeet.backend.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.UUID

@RestController
@RequestMapping("/api/upload")
class UploadController {

    private val uploadDirPath = "uploads/posters"

    init {
        val dir = File(uploadDirPath)
        if (!dir.exists()) {
            dir.mkdirs()
        }
    }

    @PostMapping
    fun uploadPoster(@RequestParam("file") file: MultipartFile): ResponseEntity<Map<String, String>> {
        if (file.isEmpty) {
            return ResponseEntity.badRequest().body(mapOf("error" to "Uploaded file is empty"))
        }

        val originalFilename = file.originalFilename ?: "poster.jpg"
        val extension = originalFilename.substringAfterLast('.', "jpg").lowercase()
        val safeFilename = "${UUID.randomUUID()}.$extension"
        val destinationPath = Paths.get(uploadDirPath).resolve(safeFilename)

        Files.copy(file.inputStream, destinationPath, StandardCopyOption.REPLACE_EXISTING)

        val relativeUrl = "/uploads/posters/$safeFilename"
        return ResponseEntity.ok(
            mapOf(
                "url" to relativeUrl,
                "filename" to safeFilename,
                "message" to "Image uploaded successfully"
            )
        )
    }
}
