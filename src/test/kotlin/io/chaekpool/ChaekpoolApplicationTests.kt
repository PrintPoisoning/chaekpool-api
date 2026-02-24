package io.chaekpool

import io.chaekpool.support.TestcontainersConfig
import io.kotest.core.spec.style.BehaviorSpec
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import

@SpringBootTest
@Import(TestcontainersConfig::class)
class ChaekpoolApplicationTests : BehaviorSpec({
    Given("Spring Boot 애플리케이션에서") {
        When("컨텍스트를 로드하면") {
            Then("정상적으로 기동된다") {}
        }
    }
})
