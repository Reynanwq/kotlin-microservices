package com.reynanwq.kotlin_microservices.exception

class TaskNotFoundException(id: Long) : RuntimeException("Tarefa com ID $id não encontrada")

class InvalidTaskException(message: String) : RuntimeException(message)

class DuplicateTaskException(title: String) : RuntimeException("Já existe uma tarefa com o título: $title")
