package io.chaekpool.user.service

import io.chaekpool.auth.token.service.TokenService
import io.chaekpool.common.exception.internal.GoneException
import io.chaekpool.common.exception.internal.NotFoundException
import io.chaekpool.common.util.UUIDv7
import io.chaekpool.generated.jooq.enums.UserStatusType
import io.chaekpool.generated.jooq.enums.UserVisibilityType
import io.chaekpool.generated.jooq.tables.pojos.Users
import io.chaekpool.user.exception.UserLeavedException
import io.chaekpool.user.exception.UserNotFoundException
import io.chaekpool.user.repository.UserRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.springframework.http.HttpStatus

class UserServiceTest : BehaviorSpec({

    lateinit var userRepository: UserRepository
    lateinit var tokenService: TokenService
    lateinit var userService: UserService

    beforeTest {
        userRepository = mockk()
        tokenService = mockk()
        userService = UserService(userRepository, tokenService)
    }

    Given("존재하는 활성 userId가 주어졌을 때") {
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
                    status = UserStatusType.ACTIVE
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
            Then("UserNotFoundException이 발생한다") {
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

    Given("탈퇴한 userId가 주어졌을 때") {
        val userId = UUIDv7.generate()

        When("getUser를 호출하면") {
            Then("UserLeavedException이 발생한다") {
                val leavedUser = Users(
                    id = userId,
                    handle = "user_abc12345",
                    visibility = UserVisibilityType.PUBLIC,
                    status = UserStatusType.LEAVED
                )
                every { userRepository.findById(userId) } returns leavedUser

                val exception = shouldThrow<UserLeavedException> {
                    userService.getUser(userId)
                }
                exception.shouldBeInstanceOf<GoneException>()
                exception.httpStatus shouldBe HttpStatus.GONE
                exception.errorCode shouldBe "USER_LEAVED"
                exception.message shouldBe "탈퇴한 사용자입니다"
            }
        }
    }

    Given("활성 사용자가 탈퇴를 요청할 때") {
        val userId = UUIDv7.generate()
        val accessToken = "access-token"
        val refreshToken = "refresh-token"

        When("leave를 호출하면") {
            Then("users를 LEAVED로 전환하고 모든 토큰을 폐기한다") {
                val activeUser = Users(
                    id = userId,
                    handle = "user_abc12345",
                    visibility = UserVisibilityType.PUBLIC,
                    status = UserStatusType.ACTIVE
                )
                every { userRepository.findById(userId) } returns activeUser
                every { userRepository.leaveById(userId) } returns 1
                every { tokenService.deactivate(userId, accessToken, refreshToken) } just runs
                every { tokenService.deactivateAll(userId) } just runs

                userService.leave(userId, accessToken, refreshToken)

                verify(exactly = 1) { userRepository.leaveById(userId) }
                verify(exactly = 1) { tokenService.deactivate(userId, accessToken, refreshToken) }
                verify(exactly = 1) { tokenService.deactivateAll(userId) }
            }
        }
    }

    Given("이미 탈퇴한 사용자가 탈퇴를 재요청할 때") {
        val userId = UUIDv7.generate()

        When("leave를 호출하면") {
            Then("UserLeavedException이 발생한다") {
                val leavedUser = Users(
                    id = userId,
                    handle = "user_abc12345",
                    visibility = UserVisibilityType.PUBLIC,
                    status = UserStatusType.LEAVED
                )
                every { userRepository.findById(userId) } returns leavedUser

                val exception = shouldThrow<UserLeavedException> {
                    userService.leave(userId, "at", "rt")
                }
                exception.shouldBeInstanceOf<GoneException>()
                exception.httpStatus shouldBe HttpStatus.GONE
                exception.errorCode shouldBe "USER_LEAVED"
                exception.message shouldBe "탈퇴한 사용자입니다"
            }
        }
    }

    Given("존재하지 않는 userId로 탈퇴 요청 시") {
        val userId = UUIDv7.generate()

        When("leave를 호출하면") {
            Then("UserNotFoundException이 발생한다") {
                every { userRepository.findById(userId) } returns null

                val exception = shouldThrow<UserNotFoundException> {
                    userService.leave(userId, "at", "rt")
                }
                exception.shouldBeInstanceOf<NotFoundException>()
                exception.httpStatus shouldBe HttpStatus.NOT_FOUND
                exception.errorCode shouldBe "USER_NOT_FOUND"
            }
        }
    }

    Given("leaveById가 0건을 반환할 때") {
        val userId = UUIDv7.generate()

        When("leave를 호출하면") {
            Then("UserLeavedException이 발생한다") {
                val activeUser = Users(
                    id = userId,
                    handle = "user_abc12345",
                    visibility = UserVisibilityType.PUBLIC,
                    status = UserStatusType.ACTIVE
                )
                every { userRepository.findById(userId) } returns activeUser
                every { userRepository.leaveById(userId) } returns 0

                val exception = shouldThrow<UserLeavedException> {
                    userService.leave(userId, "at", "rt")
                }
                exception.httpStatus shouldBe HttpStatus.GONE
                exception.errorCode shouldBe "USER_LEAVED"
            }
        }
    }
})
