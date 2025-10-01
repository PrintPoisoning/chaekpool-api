package io.chaekpool.common.exception.external

enum class ExternalSystem(
    private val label: String,
    private val endPoint: String
) {

    UNKNOWN_SERVICE("EXTERNAL SERVICE", "UNKNOWN"),
    UNKNOWN_API("EXTERNAL REST API", "UNKNOWN"),

    KAKAO_AUTH("카카오 OAuth", "https://kauth.kakao.com"),
    KAKAO_API("카카오 API", "https://kapi.kakao.com");

    fun info(): String = "${name}(${label}, ${endPoint})"

    override fun toString(): String = name

}
