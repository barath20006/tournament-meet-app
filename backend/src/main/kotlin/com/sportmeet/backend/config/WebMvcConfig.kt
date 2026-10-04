package com.sportmeet.backend.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.io.File

@Configuration
class WebMvcConfig : WebMvcConfigurer {
    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        val uploadsDir = File("uploads").absoluteFile
        if (!uploadsDir.exists()) {
            uploadsDir.mkdirs()
        }
        val uploadPathUri = uploadsDir.toURI().toString()
        registry.addResourceHandler("/uploads/**")
            .addResourceLocations(uploadPathUri)
    }
}
