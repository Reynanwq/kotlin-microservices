package com.reynanwq.kotlin_microservices.strategy

import com.reynanwq.kotlin_microservices.model.entity.Task
import com.reynanwq.kotlin_microservices.model.dto.TaskResponse
import com.reynanwq.kotlin_microservices.model.dto.toResponse
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class CompleteWithCommentStrategy : TaskActionStrategy {

    override fun canHandle(action: String): Boolean = action == "COMPLETE_WITH_COMMENT"

    override fun validate(task: Task, payload: Map<String, Any>?) {
        // Validação específica desta estratégia
    }

    override fun execute(task: Task, payload: Map<String, Any>?): TaskResponse {
        task.completed = true
        val comment = payload?.get("comment") as? String

        if (comment != null && comment.isNotBlank()) {
            val newDescription = if (task.description.isNullOrEmpty()) {
                "Comentário: $comment"
            } else {
                "${task.description}\nComentário: $comment"
            }
            task.description = newDescription
        }
        task.updatedAt = LocalDateTime.now()

        return task.toResponse()
    }
}