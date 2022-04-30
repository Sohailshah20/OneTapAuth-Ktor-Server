package com.example.routes

import com.example.domain.model.ApiResponse
import com.example.domain.model.Endpoint
import com.example.domain.model.UserSession
import com.example.domain.repository.UserDataSource
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.util.pipeline.*

fun Route.deleteUserRoute(
    app: Application,
    userDataSource : UserDataSource
){
    authenticate("auth_session") {
        delete(Endpoint.DeleteUser.path){
            val userSession = call.principal<UserSession>()
            if(userSession == null){
                app.log.info("Invalid session")
                call.respondRedirect(Endpoint.Unauthorized.path)
            }else{
                try{
                    call.sessions.clear<UserSession>()
                    deleteUserFromDb(
                        app = app,
                        userId = userSession.id,
                        userDataSource = userDataSource
                    )
                }catch (e: Exception){
                    app.log.info("deleting user error : ${e.message}")
                    call.respondRedirect(Endpoint.Unauthorized.path)

                }
            }
        }
    }
}

private suspend fun PipelineContext<Unit, ApplicationCall>.deleteUserFromDb(
    app : Application,
    userId : String,
    userDataSource: UserDataSource
){
    val result = userDataSource.deleteUser(userId = userId)
    if (result){
        app.log.info("Account deleted successfully ")
        call.respond(
            message = ApiResponse(success = true),
            status = HttpStatusCode.OK

        )
    }else{
        app.log.info("Error deleting user")
        call.respond(
            message = ApiResponse(success = false),
            status = HttpStatusCode.BadRequest
        )
    }
}