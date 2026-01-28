package com.reynanwq.kotlin_microservices.repository

import com.reynanwq.kotlin_microservices.model.entity.Priority
import com.reynanwq.kotlin_microservices.model.entity.Task
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface TaskRepository : JpaRepository<Task, Long> {

    // Query methods do Spring Data JPA
    fun findByCompleted(completed: Boolean): List<Task>

    fun findByPriority(priority: Priority): List<Task>

    fun findByTitleContainingIgnoreCase(title: String): List<Task>

    // Query personalizada com JPQL
    @Query("SELECT t FROM Task t WHERE t.completed = false AND t.priority IN ('HIGH', 'URGENT')")
    fun findPendingHighPriorityTasks(): List<Task>

    // Conta tarefas por status
    fun countByCompleted(completed: Boolean): Long
}