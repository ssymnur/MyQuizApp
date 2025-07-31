package com.quiz.myquizapp.wordGame

data class WordGameResponse(
    val words: List<WordModel>
)

data class WordModel(
    val question: String,
    val answer: String,
    val image: String?
)
