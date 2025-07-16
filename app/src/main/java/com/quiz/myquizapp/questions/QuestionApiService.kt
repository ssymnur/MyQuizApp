package com.quiz.myquizapp.questions

import retrofit2.http.GET
import retrofit2.http.Url

interface QuestionApiService {
    @GET
    suspend fun getQuestions(@Url url: String): QuestionsResponse
}
