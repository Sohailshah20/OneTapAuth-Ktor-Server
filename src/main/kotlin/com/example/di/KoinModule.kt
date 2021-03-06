package com.example.di

import com.example.data.UserDataSourceImpl
import com.example.domain.repository.UserDataSource
import com.example.util.Constants.DATABASE_NAME
import org.koin.dsl.module
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

val koinModule = module {
    single {
        KMongo.createClient(System.getenv("MONGODB_URI"))
            .coroutine
            .getDatabase(DATABASE_NAME)
    }
    single<UserDataSource>{
        UserDataSourceImpl(get())
    }
}