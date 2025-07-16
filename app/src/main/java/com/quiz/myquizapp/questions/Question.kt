package com.quiz.myquizapp.questions

data class Question(
    val question: String,
    val image: String?,
    val options: List<String>,
    val answer: String
)

data class QuestionsResponse(
    val questions: List<Question>
)
