package com.quiz.myquizapp.questions

import jakarta.inject.Inject

class QuestionRepository @Inject constructor(
    private val api: QuestionApiService
) {
    suspend fun getQuestions(url: String): List<Question> {
        val response = api.getQuestions(url)
        return response.questions
    }
}
