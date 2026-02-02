package com.reynanwq.kotlin_microservices.strategy

import com.reynanwq.kotlin_microservices.model.entity.Task
import com.reynanwq.kotlin_microservices.model.dto.TaskResponse
import com.reynanwq.kotlin_microservices.model.entity.Priority
import com.reynanwq.kotlin_microservices.exception.InvalidTaskException
import com.reynanwq.kotlin_microservices.model.dto.toResponse
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Component
class ChangePriorityWithDeadlineStrategy : TaskActionStrategy {

    override fun canHandle(action: String): Boolean = action == "CHANGE_PRIORITY_WITH_DEADLINE"

    override fun validate(task: Task, payload: Map<String, Any>?) {
        // Validações se necessário
    }

    override fun execute(task: Task, payload: Map<String, Any>?): TaskResponse {
        val newPriority = payload?.get("priority") as? String
        val deadline = payload?.get("deadline") as? LocalDateTime

        if (newPriority != null) {
            task.priority = when (newPriority.uppercase()) {
                "LOW" -> Priority.LOW
                "MEDIUM" -> Priority.MEDIUM
                "HIGH" -> Priority.HIGH
                "URGENT" -> Priority.URGENT
                else -> throw InvalidTaskException("Prioridade inválida: $newPriority")
            }
        }

        if (deadline != null) {
            val deadlineInfo = "Prazo: ${deadline.format(DateTimeFormatter.ISO_LOCAL_DATE)}"
            task.description = if (task.description.isNullOrEmpty()) {
                deadlineInfo
            } else {
                "${task.description}\n$deadlineInfo"
            }
        }
        task.updatedAt = LocalDateTime.now()

        return task.toResponse()
    }
}