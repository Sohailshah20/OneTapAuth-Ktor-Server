package com.example.routes

import com.example.domain.model.ApiResponse
import com.example.domain.model.Endpoint
import com.example.domain.model.UserSession
import com.example.domain.model.UserUpdate
import com.example.domain.repository.UserDataSource
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*
import org.apache.http.impl.auth.BasicScheme.authenticate

fun Route.updateUserInfoRoute(
    app : Application,
    userDataSource: UserDataSource
){
    authenticate("auth_session"){
        put(Endpoint.UpdateUserInfo.path) {
            val userSession = call.principal<UserSession>()
            val userUpdate = call.receive<UserUpdate>()
            if (userSession == null){
                app.log.info("Invalid session")
                call.respondRedirect(Endpoint.Unauthorized.path)
            }else{
                try {
                    updateUserInfo(
                        app = app,
                        userId = userSession.id,
                        userUpdate = userUpdate,
                        userDataSource = userDataSource
                    )
                }catch (e: Exception){
                    app.log.info("updating user info error : ${e.message}")
                    call.respondRedirect(Endpoint.Unauthorized.path)
                }
            }
        }
    }
}

private suspend fun PipelineContext<Unit, ApplicationCall>.updateUserInfo(
    app : Application,
    userId : String,
    userUpdate: UserUpdate,
    userDataSource: UserDataSource
){
    val result = userDataSource.updateUserInfo(
        userId = userId,
        firstName = userUpdate.firstName,
        lastName = userUpdate.lastName,
    )
    if (result){
        call.respond(
            message = ApiResponse(
                success = true,
                message = "successfully updated details"
            ),
            status = HttpStatusCode.OK
        )
    }else{
        app.log.info("error updating info")
        call.respond(
            message = ApiResponse(success = false),
            status = HttpStatusCode.BadRequest
        )
    }
}