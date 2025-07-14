package com.quiz.myquizapp.di

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quiz.myquizapp.Category


import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ListViewModel @Inject constructor(
    private val repository: CategoryRepository
) : ViewModel() {

    val categories = MutableLiveData<List<Category>>()
    val error = MutableLiveData<String>()

    fun getCategories() {
        viewModelScope.launch {
            try {
                val response = repository.getCategories()
                categories.value = response.categories
            } catch (e: Exception) {
                error.value = e.message
            }
        }
    }
}
