package com.reynanwq.kotlin_microservices.service

import com.reynanwq.kotlin_microservices.repository.TaskRepository
import com.reynanwq.kotlin_microservices.exception.InvalidTaskException
import com.reynanwq.kotlin_microservices.exception.TaskNotFoundException
import com.reynanwq.kotlin_microservices.model.dto.TaskRequest
import com.reynanwq.kotlin_microservices.model.dto.TaskResponse
import com.reynanwq.kotlin_microservices.model.dto.TaskUpdateRequest
import com.reynanwq.kotlin_microservices.model.dto.toEntity
import com.reynanwq.kotlin_microservices.model.dto.toResponse
import com.reynanwq.kotlin_microservices.model.entity.Priority
import com.reynanwq.kotlin_microservices.model.entity.Task
import com.reynanwq.kotlin_microservices.strategy.TaskActionStrategy
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class TaskService(
    private val taskRepository: TaskRepository,
    private val taskActionStrategies: List<TaskActionStrategy>
) {

    fun findAll(): List<TaskResponse> {
        return taskRepository.findAll().map { it.toResponse() }
    }

    fun findById(id: Long): TaskResponse {
        val task = taskRepository.findByIdOrNull(id)
            ?: throw TaskNotFoundException(id)
        return task.toResponse()
    }

    fun findByCompleted(completed: Boolean): List<TaskResponse> {
        return taskRepository.findByCompleted(completed).map { it.toResponse() }
    }

    fun findByPriority(priority: Priority): List<TaskResponse> {
        return taskRepository.findByPriority(priority).map { it.toResponse() }
    }

    fun searchByTitle(title: String): List<TaskResponse> {
        if (title.isBlank()) {
            throw InvalidTaskException("Termo de busca não pode ser vazio")
        }
        return taskRepository.findByTitleContainingIgnoreCase(title).map { it.toResponse() }
    }

    fun findPendingHighPriorityTasks(): List<TaskResponse> {
        return taskRepository.findPendingHighPriorityTasks().map { it.toResponse() }
    }

    fun getStatistics(): TaskStatistics {
        val total = taskRepository.count()
        val completed = taskRepository.countByCompleted(true)
        val pending = taskRepository.countByCompleted(false)

        return TaskStatistics(
            total = total,
            completed = completed,
            pending = pending,
            completionRate = if (total > 0) (completed.toDouble() / total * 100) else 0.0
        )
    }

    @Transactional
    fun create(request: TaskRequest): TaskResponse {
        // Validação customizada
        if (request.title.trim().length < 3) {
            throw InvalidTaskException("Título deve ter pelo menos 3 caracteres")
        }

        val task = request.toEntity()
        val saved = taskRepository.save(task)
        return saved.toResponse()
    }

    @Transactional
    fun update(id: Long, request: TaskUpdateRequest): TaskResponse {
        val task = taskRepository.findByIdOrNull(id)
            ?: throw TaskNotFoundException(id)

        // Apply pattern do Kotlin - muito útil para atualizações parciais
        task.apply {
            request.title?.let {
                if (it.trim().length >= 3) title = it
                else throw InvalidTaskException("Título deve ter pelo menos 3 caracteres")
            }
            request.description?.let { description = it }
            request.completed?.let { completed = it }
            request.priority?.let { priority = it }
            updatedAt = LocalDateTime.now()
        }

        val updated = taskRepository.save(task)
        return updated.toResponse()
    }

    @Transactional
    fun toggleCompleted(id: Long): TaskResponse {
        val task = taskRepository.findByIdOrNull(id)
            ?: throw TaskNotFoundException(id)

        task.completed = !task.completed
        task.updatedAt = LocalDateTime.now()

        val updated = taskRepository.save(task)
        return updated.toResponse()
    }

    @Transactional
    fun delete(id: Long) {
        if (!taskRepository.existsById(id)) {
            throw TaskNotFoundException(id)
        }
        taskRepository.deleteById(id)
    }

    @Transactional
    fun deleteCompleted(): Int {
        val completedTasks = taskRepository.findByCompleted(true)
        taskRepository.deleteAll(completedTasks)
        return completedTasks.size
    }

    @Transactional
    fun processTaskAction(id: Long, action: String, payload: Map<String, Any>?): TaskResponse {
        val task = taskRepository.findByIdOrNull(id)
            ?: throw TaskNotFoundException(id)

        // Encontra a estratégia adequada
        val strategy = taskActionStrategies.find { it.canHandle(action) }
            ?: throw InvalidTaskException("Ação desconhecida: $action")

        // Executa validação e execução
        strategy.validate(task, payload)

        // Note: algumas estratégias já são @Transactional
        return strategy.execute(task, payload).also {
            // Para estratégias que não salvam automaticamente
            if (action !in listOf("DUPLICATE_TASK", "SPLIT_TASK")) {
                taskRepository.save(task)
            }
        }
    }


}

data class TaskStatistics(
    val total: Long,
    val completed: Long,
    val pending: Long,
    val completionRate: Double
)

