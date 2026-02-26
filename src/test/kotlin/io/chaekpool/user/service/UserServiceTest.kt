package io.chaekpool.user.service

import io.chaekpool.common.exception.internal.NotFoundException
import io.chaekpool.generated.jooq.enums.UserStatusType
import io.chaekpool.generated.jooq.enums.UserVisibilityType
import io.chaekpool.generated.jooq.tables.pojos.Users
import io.chaekpool.user.repository.UserRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import java.util.UUID

class UserServiceTest : BehaviorSpec({

    lateinit var userRepository: UserRepository
    lateinit var userService: UserService

    beforeTest {
        userRepository = mockk()
        userService = UserService(userRepository)
    }

    Given("존재하는 userId가 주어졌을 때") {
        When("getUser를 호출하면") {
            Then("사용자 정보를 반환한다") {
                val userId = UUID.randomUUID()
                val userPojo = Users(
                    id = userId,
                    email = "test@example.com",
                    username = "testuser",
                    profileImageUrl = "https://example.com/profile.jpg",
                    visibility = UserVisibilityType.PUBLIC,
                    status = UserStatusType.ACTIVE,
                    lastLoginAt = null,
                    createdAt = null,
                    updatedAt = null
                )

                every { userRepository.findById(userId) } returns userPojo

                val result = userService.getUser(userId)

                result.email shouldBe "test@example.com"
                result.username shouldBe "testuser"
                result.profileImageUrl shouldBe "https://example.com/profile.jpg"
                result.visibility shouldBe "PUBLIC"
                result.status shouldBe "ACTIVE"
            }
        }
    }

    Given("존재하지 않는 userId가 주어졌을 때") {
        When("getUser를 호출하면") {
            Then("NotFoundException이 발생한다") {
                val userId = UUID.randomUUID()

                every { userRepository.findById(userId) } returns null

                val exception = shouldThrow<NotFoundException> {
                    userService.getUser(userId)
                }
                exception.message shouldBe "사용자를 찾을 수 없습니다."
            }
        }
    }
})
