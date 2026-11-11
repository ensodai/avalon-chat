package app.domain.utils

import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

suspend inline fun <reified T : Any> RoutingContext.respondResult(result: ServiceResult<T>) {
    when (result) {

        is ServiceResult.Success -> {
            val response = BaseResponse(data = result.data)
            call.respond(HttpStatusCode.OK, response)
        }

        is ServiceResult.Error -> {
            val statusCode = when (result.type) {
                ErrorType.NOT_FOUND -> HttpStatusCode.NotFound
                ErrorType.FORBIDDEN -> HttpStatusCode.Forbidden
                ErrorType.BAD_REQUEST -> HttpStatusCode.BadRequest
                ErrorType.CONFLICT -> HttpStatusCode.Conflict
                ErrorType.SERVER_ERROR -> HttpStatusCode.InternalServerError
            }

            val response = BaseResponse<T>(
                data = null,
                error = result.message ?: result.type.name
            )

            call.respond(statusCode, response)
        }
    }
}