package com.reynanwq.kotlin_microservices.controller

import com.reynanwq.kotlin_microservices.model.dto.TaskRequest
import com.reynanwq.kotlin_microservices.model.dto.TaskResponse
import com.reynanwq.kotlin_microservices.model.dto.TaskUpdateRequest
import com.reynanwq.kotlin_microservices.model.entity.Priority
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import com.reynanwq.kotlin_microservices.service.TaskService

@RestController
@RequestMapping("/api/tasks")
class TaskController(
    private val taskService : TaskService
)  {
    @GetMapping
    fun getAllTask(): ResponseEntity<List<TaskResponse>>{
        return ResponseEntity.ok(taskService.findAll())
    }

    @GetMapping("/{id}")
    fun getTaskId(@PathVariable id: Long): ResponseEntity<TaskResponse>{
        return ResponseEntity.ok(taskService.findById(id))
    }

    @GetMapping("/completed/{completed}")
    fun getTasksByCompletion(@PathVariable completed: Boolean): ResponseEntity<List<TaskResponse>> {
        return ResponseEntity.ok(taskService.findByCompleted(completed))
    }

    @GetMapping("/priority/{priority}")
    fun getTasksByPriority(@PathVariable priority: Priority): ResponseEntity<List<TaskResponse>> {
        return ResponseEntity.ok(taskService.findByPriority(priority))
    }

    @GetMapping("/search")
    fun searchTasksByTitle(@RequestParam title: String): ResponseEntity<List<TaskResponse>> {
        return ResponseEntity.ok(taskService.searchByTitle(title))
    }

    @GetMapping("/pending-high-priority")
    fun getPendingHighPriorityTasks(): ResponseEntity<List<TaskResponse>> {
        return ResponseEntity.ok(taskService.findPendingHighPriorityTasks())
    }

    @PostMapping
    fun createTask(@RequestBody request: TaskRequest): ResponseEntity<TaskResponse> {
        val createdTask = taskService.create(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTask)
    }

    @PutMapping("/{id}")
    fun updateTask(@PathVariable id: Long, @RequestBody request: TaskUpdateRequest): ResponseEntity<TaskResponse> {
        return ResponseEntity.ok(taskService.update(id, request))
    }

    @PatchMapping("/{id}/toggle-completed")
    fun toggleTaskCompleted(@PathVariable id: Long): ResponseEntity<TaskResponse> {
        return ResponseEntity.ok(taskService.toggleCompleted(id))
    }

    @DeleteMapping("/{id}")
    fun deleteTask(@PathVariable id: Long): ResponseEntity<Void> {
        taskService.delete(id)
        return ResponseEntity.noContent().build()
    }

    @DeleteMapping("/completed")
    fun deleteCompletedTasks(): ResponseEntity<Map<String, Int>> {
        val deletedCount = taskService.deleteCompleted()
        return ResponseEntity.ok(mapOf("deletedCount" to deletedCount))
    }

}