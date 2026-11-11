package app.plugins


//fun Application.configureErrorHandling() {
//    install(StatusPages) {
//        exception<AuthenticationException> { call, cause ->
//            call.respond(
//                status = HttpStatusCode.Unauthorized,
//                message = BaseResponse<Any>(data = null, error = cause.localizedMessage)
//            )
//        }
//
//        exception<TooManyRequestsException> { call, cause ->
//            call.respond(
//                status = HttpStatusCode.TooManyRequests,
//                message = BaseResponse<Any>(data = null, error = cause.localizedMessage)
//            )
//        }
//
//        exception<IllegalArgumentException> { call, cause ->
//            call.respond(
//                status = HttpStatusCode.BadRequest,
//                message = BaseResponse<Any>(data = null, error = cause.localizedMessage)
//            )
//        }
//
//        exception<IllegalStateException> { call, cause ->
//            call.respond(
//                status = HttpStatusCode.ServiceUnavailable,
//                message = BaseResponse<Any>(data = null, error = cause.localizedMessage)
//            )
//        }
//
//        exception<Throwable> { call, cause ->
//            call.application.environment.log.error("Unhandled exception", cause)
//            call.respond(
//                status = HttpStatusCode.InternalServerError,
//                message = BaseResponse<Any>(data = null, error = "Unexpected error: ${cause.localizedMessage}")
//            )
//        }
//    }
//}
//
//class AuthenticationException(message: String) : RuntimeException(message)
//class TooManyRequestsException(message: String) : RuntimeException(message)
