package io.chaekpool.user.service

import io.chaekpool.common.exception.internal.NotFoundException
import io.chaekpool.common.util.UUIDv7
import io.chaekpool.generated.jooq.enums.UserStatusType
import io.chaekpool.generated.jooq.enums.UserVisibilityType
import io.chaekpool.generated.jooq.tables.pojos.Users
import io.chaekpool.user.exception.UserNotFoundException
import io.chaekpool.user.repository.UserRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import org.springframework.http.HttpStatus

class UserServiceTest : BehaviorSpec({

    lateinit var userRepository: UserRepository
    lateinit var userService: UserService

    beforeTest {
        userRepository = mockk()
        userService = UserService(userRepository)
    }

    Given("존재하는 userId가 주어졌을 때") {
        val userId = UUIDv7.generate()

        When("getUser를 호출하면") {
            Then("사용자 정보를 반환한다") {
                val userPojo = Users(
                    id = userId,
                    email = "test@example.com",
                    nickname = "testuser",
                    handle = "user_abc12345",
                    profileImageUrl = "https://example.com/profile.jpg",
                    thumbnailImageUrl = "https://example.com/thumb.jpg",
                    visibility = UserVisibilityType.PUBLIC,
                    status = UserStatusType.ACTIVE,
                    lastLoginAt = null,
                    createdAt = null,
                    updatedAt = null
                )

                every { userRepository.findById(userId) } returns userPojo

                val result = userService.getUser(userId)

                result.email shouldBe "test@example.com"
                result.nickname shouldBe "testuser"
                result.handle shouldBe "user_abc12345"
                result.profileImageUrl shouldBe "https://example.com/profile.jpg"
                result.thumbnailImageUrl shouldBe "https://example.com/thumb.jpg"
                result.visibility shouldBe "PUBLIC"
                result.status shouldBe "ACTIVE"
            }
        }
    }

    Given("존재하지 않는 userId가 주어졌을 때") {
        val userId = UUIDv7.generate()

        When("getUser를 호출하면") {
            Then("NotFoundException이 발생한다") {
                every { userRepository.findById(userId) } returns null

                val exception = shouldThrow<UserNotFoundException> {
                    userService.getUser(userId)
                }
                exception.shouldBeInstanceOf<NotFoundException>()
                exception.httpStatus shouldBe HttpStatus.NOT_FOUND
                exception.errorCode shouldBe "USER_NOT_FOUND"
                exception.message shouldBe "사용자를 찾을 수 없습니다"
            }
        }
    }
})
