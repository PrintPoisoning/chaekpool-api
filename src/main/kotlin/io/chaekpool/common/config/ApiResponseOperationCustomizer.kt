package io.chaekpool.common.config

import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.media.IntegerSchema
import io.swagger.v3.oas.models.media.ObjectSchema
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.media.StringSchema
import org.springdoc.core.customizers.OperationCustomizer
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod

@Component
class ApiResponseOperationCustomizer : OperationCustomizer {

    override fun customize(operation: Operation, handlerMethod: HandlerMethod): Operation {
        operation.responses?.forEach { (code, apiResponse) ->
            val codeInt = code.toIntOrNull() ?: return@forEach
            if (codeInt in 200..299) {
                val content = apiResponse.content?.get("application/json") ?: return@forEach
                val originalSchema = content.schema ?: return@forEach
                content.schema(wrapSchema(originalSchema))
            }
        }
        return operation
    }

    private fun wrapSchema(dataSchema: Schema<*>): Schema<*> =
        ObjectSchema()
            .addProperty("trace_id", StringSchema())
            .addProperty("span_id", StringSchema())
            .addProperty("status", IntegerSchema())
            .addProperty("data", dataSchema)
}
