package com.reynanwq.kotlin_microservices.strategy

import com.reynanwq.kotlin_microservices.model.entity.Task
import com.reynanwq.kotlin_microservices.model.dto.TaskResponse
import com.reynanwq.kotlin_microservices.model.dto.toResponse
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class ArchiveTaskStrategy : TaskActionStrategy {

    override fun canHandle(action: String): Boolean = action == "ARCHIVE"

    override fun validate(task: Task, payload: Map<String, Any>?) {
        // Não precisa de validação específica
    }

    override fun execute(task: Task, payload: Map<String, Any>?): TaskResponse {
        if (!task.completed) {
            task.completed = true
        }
        task.title = "[ARQUIVADA] ${task.title}"
        task.updatedAt = LocalDateTime.now()

        return task.toResponse()
    }
}