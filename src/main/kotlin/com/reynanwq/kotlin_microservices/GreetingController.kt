package com.reynanwq.kotlin_microservices

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.concurrent.atomic.AtomicLong

@RestController
class GreetingController {

    val counter = AtomicLong()

    @GetMapping("/greeting")
    fun greeting(): Greeting {
        return Greeting(
            id = counter.incrementAndGet(),
            content = "Hello, Kotlin!"
        )
    }
}