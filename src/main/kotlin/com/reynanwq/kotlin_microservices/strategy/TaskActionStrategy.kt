package com.reynanwq.kotlin_microservices.strategy

import com.reynanwq.kotlin_microservices.model.entity.Task
import com.reynanwq.kotlin_microservices.model.dto.TaskResponse

interface TaskActionStrategy {
    fun canHandle(action: String): Boolean
    fun execute(task: Task, payload: Map<String, Any>?): TaskResponse
    fun validate(task: Task, payload: Map<String, Any>?)
}