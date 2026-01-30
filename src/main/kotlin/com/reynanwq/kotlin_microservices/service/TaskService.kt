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
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class TaskService(
    private val taskRepository: TaskRepository
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

        // Múltiplos ifs para processar diferentes ações
        if (action == "COMPLETE_WITH_COMMENT") {
            // Marcar como completa e adicionar comentário
            task.completed = true
            val comment = payload?.get("comment") as? String
            if (comment != null && comment.isNotBlank()) {
                // Adicionar comentário à descrição
                val newDescription = if (task.description.isNullOrEmpty()) {
                    "Comentário: $comment"
                } else {
                    "${task.description}\nComentário: $comment"
                }
                task.description = newDescription
            }
            task.updatedAt = LocalDateTime.now()

        } else if (action == "CHANGE_PRIORITY_WITH_DEADLINE") {
            // Alterar prioridade e definir prazo
            val newPriority = payload?.get("priority") as? String
            val deadline = payload?.get("deadline") as? LocalDateTime

            if (newPriority != null) {
                val priority = when (newPriority.uppercase()) {
                    "LOW" -> Priority.LOW
                    "MEDIUM" -> Priority.MEDIUM
                    "HIGH" -> Priority.HIGH
                    "URGENT" -> Priority.URGENT
                    else -> throw InvalidTaskException("Prioridade inválida: $newPriority")
                }
                task.priority = priority
            }

            if (deadline != null) {
                // Adicionar informação de prazo na descrição
                val deadlineInfo = "Prazo: ${deadline.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)}"
                task.description = if (task.description.isNullOrEmpty()) {
                    deadlineInfo
                } else {
                    "${task.description}\n$deadlineInfo"
                }
            }
            task.updatedAt = LocalDateTime.now()

        } else if (action == "DUPLICATE_TASK") {
            // Criar uma cópia da tarefa
            val suffix = payload?.get("suffix") as? String ?: "(Cópia)"
            val newTitle = "${task.title} $suffix"

            if (newTitle.trim().length < 3) {
                throw InvalidTaskException("Título deve ter pelo menos 3 caracteres")
            }

            val newTask = Task(
                title = newTitle,
                description = task.description,
                completed = false,
                priority = task.priority
            )
            taskRepository.save(newTask)
            return newTask.toResponse()

        } else if (action == "SPLIT_TASK") {
            // Dividir tarefa em subtarefas
            val subtaskCount = payload?.get("subtaskCount") as? Int ?: 2

            if (subtaskCount <= 0 || subtaskCount > 10) {
                throw InvalidTaskException("Número de subtarefas deve estar entre 1 e 10")
            }

            val subtasks = mutableListOf<Task>()
            for (i in 1..subtaskCount) {
                val subtask = Task(
                    title = "${task.title} - Parte $i/$subtaskCount",
                    description = if (i == 1) task.description else null,
                    completed = false,
                    priority = if (task.priority == Priority.URGENT) Priority.HIGH else task.priority
                )
                subtasks.add(subtask)
            }

            // Marcar tarefa original como completa
            task.completed = true
            task.updatedAt = LocalDateTime.now()

            // Salvar subtarefas
            taskRepository.saveAll(subtasks)
            return task.toResponse()

        } else if (action == "ADD_TAGS") {
            // Adicionar tags à descrição
            val tags = payload?.get("tags") as? List<String>

            if (tags.isNullOrEmpty()) {
                throw InvalidTaskException("Pelo menos uma tag deve ser fornecida")
            }

            val tagsText = "Tags: ${tags.joinToString(", ")}"
            task.description = if (task.description.isNullOrEmpty()) {
                tagsText
            } else {
                "${task.description}\n$tagsText"
            }
            task.updatedAt = LocalDateTime.now()

        } else if (action == "ARCHIVE") {
            // Arquivar tarefa (marcar como completa e mudar título)
            if (!task.completed) {
                task.completed = true
            }
            task.title = "[ARQUIVADA] ${task.title}"
            task.updatedAt = LocalDateTime.now()

        } else if (action == "CHANGE_STATUS_BASED_ON_PRIORITY") {
            // Lógica complexa baseada na prioridade
            when (task.priority) {
                Priority.URGENT -> {
                    // Tarefas urgentes sempre marcar como não completas se não tiverem descrição
                    if (task.description.isNullOrBlank()) {
                        task.completed = false
                        task.description = "URGENTE: precisa de descrição detalhada"
                    }
                }
                Priority.HIGH -> {
                    // Tarefas de alta prioridade com mais de 30 dias são marcadas como urgentes
                    val thirtyDaysAgo = LocalDateTime.now().minusDays(30)
                    if (task.createdAt.isBefore(thirtyDaysAgo) && !task.completed) {
                        task.priority = Priority.URGENT
                    }
                }
                Priority.MEDIUM -> {
                    // Tarefas médias com descrição vazia são rebaixadas
                    if (task.description.isNullOrBlank()) {
                        task.priority = Priority.LOW
                    }
                }
                Priority.LOW -> {
                    // Tarefas baixas completas são mantidas, senão verificamos a idade
                    if (!task.completed) {
                        val sixtyDaysAgo = LocalDateTime.now().minusDays(60)
                        if (task.createdAt.isBefore(sixtyDaysAgo)) {
                            // Tarefas muito antigas são automaticamente completadas
                            task.completed = true
                            task.description = if (task.description.isNullOrEmpty()) {
                                "Completada automaticamente por inatividade"
                            } else {
                                "${task.description}\nCompletada automaticamente por inatividade"
                            }
                        }
                    }
                }
            }
            task.updatedAt = LocalDateTime.now()

        } else {
            throw InvalidTaskException("Ação desconhecida: $action")
        }

        val updated = taskRepository.save(task)
        return updated.toResponse()
    }
}

data class TaskStatistics(
    val total: Long,
    val completed: Long,
    val pending: Long,
    val completionRate: Double
)

