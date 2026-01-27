package com.reynanwq.kotlin_microservices.model.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "tasks")
data class Task(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, length = 100)
    var title: String = "",

    @Column(length = 500)
    var description: String? = null,

    @Column(nullable = false)
    var completed: Boolean = false,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var priority: Priority = Priority.MEDIUM,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    @PreUpdate
    fun preUpdate() {
        updatedAt = LocalDateTime.now()
    }
}

enum class Priority {
    LOW, MEDIUM, HIGH, URGENT
}