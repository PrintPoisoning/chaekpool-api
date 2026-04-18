package io.chaekpool.user.exception

import io.chaekpool.common.exception.internal.GoneException

class UserLeavedException : GoneException(
    message = "탈퇴한 사용자입니다",
    errorCode = "USER_LEAVED"
)
