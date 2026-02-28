package io.chaekpool.common.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@Tag(name = "Common", description = "공통 API")
class CommonController {

    @Operation(
        summary = "robots.txt 반환",
        description = "검색 엔진 크롤러 제어를 위한 robots.txt를 반환합니다",
        security = []
    )
    @GetMapping("/robots.txt", produces = [MediaType.TEXT_PLAIN_VALUE])
    fun robots(): String = "User-agent: *\nDisallow: /"
}
