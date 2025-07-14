package com.quiz.myquizapp.di


import com.quiz.myquizapp.CategoryResponse
import retrofit2.http.GET

interface CategoryApiService {

    @GET("ikrabu/Quiz/main/categories")

    suspend fun getCategories(): CategoryResponse
}