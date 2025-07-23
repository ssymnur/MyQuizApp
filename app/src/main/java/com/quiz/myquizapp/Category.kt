package com.quiz.myquizapp

data class Category(
    val id: String,
    val name: String,
    val imageUrl: String
)

data class CategoryResponse(
    val categories: List<Category>
)



