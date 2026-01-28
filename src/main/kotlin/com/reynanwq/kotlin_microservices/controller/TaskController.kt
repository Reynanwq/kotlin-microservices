package com.reynanwq.kotlin_microservices.controller

import com.reynanwq.kotlin_microservices.model.dto.TaskRequest
import com.reynanwq.kotlin_microservices.model.dto.TaskResponse
import com.reynanwq.kotlin_microservices.model.dto.TaskUpdateRequest
import com.reynanwq.kotlin_microservices.model.entity.Priority
import com.reynanwq.kotlin_microservices.service.TaskService
import com.reynanwq.kotlin_microservices.service.TaskStatistics
import com.reynanwq.kotlinmicroservices.service.TaskService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

class TaskController(
    private val taskService : TaskService
)  {

}