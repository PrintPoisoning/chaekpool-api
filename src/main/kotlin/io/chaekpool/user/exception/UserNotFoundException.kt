package io.chaekpool.user.exception

import io.chaekpool.common.exception.internal.NotFoundException

class UserNotFoundException : NotFoundException(
    message = "사용자를 찾을 수 없습니다",
    errorCode = "USER_NOT_FOUND"
)
