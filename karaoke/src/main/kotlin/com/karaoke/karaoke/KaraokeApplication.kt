package com.karaoke.karaoke

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class KaraokeApplication

fun main(args: Array<String>) {
    runApplication<KaraokeApplication>(*args)
}
