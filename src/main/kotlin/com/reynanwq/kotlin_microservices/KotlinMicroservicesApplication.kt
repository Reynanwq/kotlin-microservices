package com.reynanwq.kotlin_microservices

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class KotlinMicroservicesApplication

fun main(args: Array<String>) {
	runApplication<KotlinMicroservicesApplication>(*args)
}
