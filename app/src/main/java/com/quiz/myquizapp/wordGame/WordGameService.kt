package com.quiz.myquizapp.wordGame

import android.telecom.Call
import retrofit2.http.GET
import retrofit2.http.Url

interface WordGameService {
    @GET
    suspend fun getWords(@Url url: String): WordGameResponse
}

