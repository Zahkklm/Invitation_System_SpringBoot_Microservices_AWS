package com.digitopia.invitation

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient

@SpringBootApplication
@EnableDiscoveryClient
class InvitationServiceApplication

fun main(args: Array<String>) {
    runApplication<InvitationServiceApplication>(*args)
}