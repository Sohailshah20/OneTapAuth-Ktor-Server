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
import io.opencensus.stats.Stats

fun Route.getUserInfoRoute(
    app : Application,
    userDataSource: UserDataSource
){
    authenticate("auth_session"){
        get(Endpoint.GetUserInfo.path){
            val userSession = call.principal<UserSession>()
            if (userSession == null){
                app.log.info("Invalid session")
                call.respondRedirect(Endpoint.Unauthorized.path)
            }else{
                try {
                    call.respond(
                        message = ApiResponse(
                            success = true,
                            user = userDataSource.getUserInfo(userId = userSession.id)
                        ),
                    status = HttpStatusCode.OK
                    )
                }catch (e: Exception){
                    app.log.info("can't get user info : ${e.message}")
                    call.respondRedirect(Endpoint.Unauthorized.path)
                }
            }
        }
    }
}