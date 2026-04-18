package io.chaekpool.auth.oauth2.exception

import io.chaekpool.common.exception.internal.NotFoundException

class RejoinTicketNotFoundException : NotFoundException(
    message = "재가입 티켓을 찾을 수 없습니다",
    errorCode = "REJOIN_TICKET_NOT_FOUND"
)
