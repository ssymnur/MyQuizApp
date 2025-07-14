package com.quiz.myquizapp.service

import com.quiz.myquizapp.CategoryResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url

interface CatalogAPI {
    //https://raw.githubusercontent.com/ikrabu/Quiz/refs/heads/main/categories
    //BASE URL -> https://raw.githubusercontent.com/
    //ENDPOINT -> ikrabu/Quiz/refs/heads/main/categories

    /*@GET
    fun getCategories(@Url url: String): Call<CategoryResponse>*/
    @GET("ikrabu/Quiz/main/categories") // veya json dosya tam adı eğer varsa
    fun getCategories(): Call<CategoryResponse>



}