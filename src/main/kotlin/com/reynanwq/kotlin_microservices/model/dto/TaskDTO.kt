package com.reynanwq.kotlin_microservices.model.dto

import com.reynanwq.kotlin_microservices.model.entity.Priority
import com.reynanwq.kotlin_microservices.model.entity.Task
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

//DTO para CRIAR tarefas
data class TaskRequest(
    @field:NotBlank(message = "Título é obrigatório")
    @field:Size(min = 3, max = 100, message = "Título deve ter entre 3 e 100 caracteres")
    val title: String,

    @field:Size(max = 500, message = "Descrição não pode ter mais de 500 caracteres")
    val description: String? = null,

    val completed: Boolean = false,

    val priority: Priority = Priority.MEDIUM
)

// DTO para RETORNAR tarefas
data class TaskResponse(
    val id: Long,
    val title: String,
    val description: String?,
    val completed: Boolean,
    val priority: Priority,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

//DTO para ATUALIZAR tarefas
data class TaskUpdateRequest(
    @field:Size(min = 3, max = 100, message = "Título deve ter entre 3 e 100 caracteres")
    val title: String? = null,

    @field:Size(max = 500, message = "Descrição não pode ter mais de 500 caracteres")
    val description: String? = null,

    val completed: Boolean? = null,

    val priority: Priority? = null
)

// Extension functions para conversão
fun Task.toResponse() = TaskResponse(
    id = id!!,
    title = title,
    description = description,
    completed = completed,
    priority = priority,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun TaskRequest.toEntity() = Task(
    title = title,
    description = description,
    completed = completed,
    priority = priority
)

