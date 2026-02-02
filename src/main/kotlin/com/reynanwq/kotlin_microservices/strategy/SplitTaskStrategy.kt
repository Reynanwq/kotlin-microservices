package com.reynanwq.kotlin_microservices.strategy
import com.reynanwq.kotlin_microservices.model.entity.Task
import com.reynanwq.kotlin_microservices.model.dto.TaskResponse
import com.reynanwq.kotlin_microservices.model.entity.Priority
import com.reynanwq.kotlin_microservices.exception.InvalidTaskException
import com.reynanwq.kotlin_microservices.model.dto.toResponse
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Component
class SplitTaskStrategy(
    private val taskRepository: com.reynanwq.kotlin_microservices.repository.TaskRepository
) : TaskActionStrategy {

    override fun canHandle(action: String): Boolean = action == "SPLIT_TASK"

    override fun validate(task: Task, payload: Map<String, Any>?) {
        val subtaskCount = payload?.get("subtaskCount") as? Int ?: 2

        if (subtaskCount <= 0 || subtaskCount > 10) {
            throw InvalidTaskException("Número de subtarefas deve estar entre 1 e 10")
        }
    }

    @Transactional
    override fun execute(task: Task, payload: Map<String, Any>?): TaskResponse {
        val subtaskCount = payload?.get("subtaskCount") as? Int ?: 2

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

        task.completed = true
        task.updatedAt = LocalDateTime.now()

        taskRepository.saveAll(subtasks)
        return task.toResponse()
    }
}