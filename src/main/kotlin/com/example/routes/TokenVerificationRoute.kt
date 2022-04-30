package com.example.routes

import com.example.domain.model.ApiRequest
import com.example.domain.model.Endpoint
import com.example.domain.model.User
import com.example.domain.model.UserSession
import com.example.domain.repository.UserDataSource
import com.example.util.Constants.AUDIENCE
import com.example.util.Constants.ISSUER
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.util.pipeline.*

fun Route.tokenVerificationRoute(
    app : Application,
    userDataSource : UserDataSource
){
    post(Endpoint.TokenVerification.path){
        val request = call.receive<ApiRequest>()
        if(request.tokenId.isNotEmpty()){
            val result = verifyGoogleTokenId(request.tokenId)
            if (result != null){
                saverUserToDatabase(
                    app = app,
                    result = result,
                    userDataSource = userDataSource
                )
            }else{
                app.log.info("Token not Verified")
                call.respondRedirect(Endpoint.Unauthorized.path)
            }
        }else{
            app.log.info("Token not found")
            call.respondRedirect(Endpoint.Unauthorized.path)
        }
    }
}

fun verifyGoogleTokenId(tokenId: String) : GoogleIdToken?{
    return try {
        val verifier = GoogleIdTokenVerifier.Builder(NetHttpTransport(),GsonFactory())
            .setAudience(listOf(AUDIENCE))
            .setIssuer(ISSUER)
            .build()
        verifier.verify(tokenId)
    }catch (e: Exception){
        null
    }
}

private suspend fun PipelineContext<Unit, ApplicationCall>.saverUserToDatabase(
    app : Application,
    result : GoogleIdToken,
    userDataSource : UserDataSource
){
    val sub = result.payload["sub"].toString()
    val profilePhoto = result.payload["picture"].toString()
    val name = result.payload["name"].toString()
    val emailAddress = result.payload["email"].toString()
    val user = User(
        id = sub,
        name = name,
        emailAddress = emailAddress,
        profilePicture = profilePhoto
    )
    val response = userDataSource.saveUserInfo(user = user)
    if (response){
        call.sessions.set(UserSession(id = sub , name = name))
        call.respondRedirect(Endpoint.Authorized.path)
    }else{
        call.respondRedirect(Endpoint.Unauthorized.path)
    }


}