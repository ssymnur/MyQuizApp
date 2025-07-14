package com.quiz.myquizapp.di



import javax.inject.Inject

class CategoryRepository @Inject constructor(
    private val api: CategoryApiService
) {
    suspend fun getCategories() = api.getCategories()
}
