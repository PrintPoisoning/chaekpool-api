package io.chaekpool.common.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/common")
class CommonController {

    @GetMapping("/healthy")
    fun healthCheck(): ResponseEntity<String> = ResponseEntity.ok("healthy")

    @GetMapping("/test")
    fun test(): ResponseEntity<String> = ResponseEntity.ok("test")
}
