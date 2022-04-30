package com.example.plugins

import com.example.domain.repository.UserDataSource
import com.example.routes.*
import io.ktor.server.routing.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import org.koin.java.KoinJavaComponent.inject

fun Application.configureRouting() {

    routing {

        val userDataSource : UserDataSource by inject(UserDataSource::class.java)

        rootRoute()
        tokenVerificationRoute(application, userDataSource)
        getUserInfoRoute(application, userDataSource)
        updateUserInfoRoute(application, userDataSource)
        deleteUserRoute(application, userDataSource)
        SignOutRoute()
        authorizedRoute()
        unauthorizedRoute()
    }
}
