package com.quiz.myquizapp.questions

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class QuizViewModel @Inject constructor(
    private val repository: QuestionRepository
): ViewModel() {

    val questions = MutableLiveData<List<Question>>()
    val error = MutableLiveData<String>()

    fun fetchQuestionsForCategory(url: String) {
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
