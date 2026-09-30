package com.sportmeet.backend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SportMeetApplication

fun main(args: Array<String>) {
    runApplication<SportMeetApplication>(*args)
}
