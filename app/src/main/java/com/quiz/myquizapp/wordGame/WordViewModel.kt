package com.quiz.myquizapp.wordGame

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quiz.myquizapp.questions.Question
import com.quiz.myquizapp.questions.QuestionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class WordViewModel @Inject constructor(
    private val repository: QuestionRepository
): ViewModel() {

    val questions = MutableLiveData<List<Question>>()
    val error = MutableLiveData<String>()

    fun fetchWordForCategory(url: String) {
        viewModelScope.launch {
            try {
                var list = repository.getQuestions(url)
                // Karıştır
                list = list.shuffled()
                questions.value = list
            } catch (e: Exception) {
                error.value = e.message
            }
        }
    }
}