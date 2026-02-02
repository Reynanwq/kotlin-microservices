package com.reynanwq.kotlin_microservices.strategy

import com.reynanwq.kotlin_microservices.model.entity.Task
import com.reynanwq.kotlin_microservices.model.dto.TaskResponse
import com.reynanwq.kotlin_microservices.exception.InvalidTaskException
import com.reynanwq.kotlin_microservices.model.dto.toResponse
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class DuplicateTaskStrategy(
    private val taskRepository: com.reynanwq.kotlin_microservices.repository.TaskRepository
) : TaskActionStrategy {

    override fun canHandle(action: String): Boolean = action == "DUPLICATE_TASK"

    override fun validate(task: Task, payload: Map<String, Any>?) {
        val suffix = payload?.get("suffix") as? String ?: "(Cópia)"
        val newTitle = "${task.title} $suffix"

        if (newTitle.trim().length < 3) {
            throw InvalidTaskException("Título deve ter pelo menos 3 caracteres")
        }
    }

    @Transactional
    override fun execute(task: Task, payload: Map<String, Any>?): TaskResponse {
        val suffix = payload?.get("suffix") as? String ?: "(Cópia)"
        val newTitle = "${task.title} $suffix"

        val newTask = Task(
            title = newTitle,
            description = task.description,
            completed = false,
            priority = task.priority
        )

        val savedTask = taskRepository.save(newTask)
        return savedTask.toResponse()
    }
}