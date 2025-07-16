package com.quiz.myquizapp.questions

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.quiz.myquizapp.databinding.FragmentQuestionsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class QuestionsFragment : Fragment() {

    private var _binding: FragmentQuestionsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: QuizViewModel by viewModels()
    private val args: QuestionsFragmentArgs by navArgs()

    private var currentQuestionIndex = 0
    private var questionsList = listOf<Question>()

    private lateinit var countDownTimer: CountDownTimer
    private var timeLeftInMillis: Long = 600_000 // 10 dakika

    private val userAnswers = mutableMapOf<Int, String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuestionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val category = args.selectedCategory
        val url = getUrlForCategory(category)
        viewModel.fetchQuestionsForCategory(url)

        viewModel.questions.observe(viewLifecycleOwner) { list ->
            if (!list.isNullOrEmpty()) {
                questionsList = list
                currentQuestionIndex = 0
                displayQuestion(currentQuestionIndex)
                startTimer()
            }
        }

        // Cevap butonları
        binding.buttonOptionA.setOnClickListener { handleAnswerClick(binding.buttonOptionA) }
        binding.buttonOptionB.setOnClickListener { handleAnswerClick(binding.buttonOptionB) }
        binding.buttonOptionC.setOnClickListener { handleAnswerClick(binding.buttonOptionC) }
        binding.buttonOptionD.setOnClickListener { handleAnswerClick(binding.buttonOptionD) }

        // İleri geri
        binding.buttonNext.setOnClickListener {
            if (currentQuestionIndex < questionsList.size - 1) {
                currentQuestionIndex++
                displayQuestion(currentQuestionIndex)
            }
        }

        binding.buttonPrevious.setOnClickListener {
            if (currentQuestionIndex > 0) {
                currentQuestionIndex--
                displayQuestion(currentQuestionIndex)
            }
        }

        // Bitir
        binding.buttonFinish.setOnClickListener {
            finishQuiz()
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMsg ->
            Toast.makeText(requireContext(), "Hata: $errorMsg", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleAnswerClick(selectedButton: View) {
        val selectedOption = when (selectedButton) {
            binding.buttonOptionA -> binding.buttonOptionA.text.toString()
            binding.buttonOptionB -> binding.buttonOptionB.text.toString()
            binding.buttonOptionC -> binding.buttonOptionC.text.toString()
            binding.buttonOptionD -> binding.buttonOptionD.text.toString()
            else -> ""
        }
        userAnswers[currentQuestionIndex] = selectedOption
        resetOptionButtonsColor()

        // Seçilen butonu pembe yap
        selectedButton.setBackgroundColor(resources.getColor(android.R.color.holo_purple))
    }

    private fun resetOptionButtonsColor() {
        val defaultColor = resources.getColor(com.google.android.material.R.color.design_default_color_primary)
        binding.buttonOptionA.setBackgroundColor(defaultColor)
        binding.buttonOptionB.setBackgroundColor(defaultColor)
        binding.buttonOptionC.setBackgroundColor(defaultColor)
        binding.buttonOptionD.setBackgroundColor(defaultColor)
    }

    private fun displayQuestion(index: Int) {
        val question = questionsList[index]

        binding.textViewQuestion.text = question.question

        if (!question.image.isNullOrEmpty()) {
            binding.imageViewQuestion.visibility = View.VISIBLE
            Glide.with(requireContext()).load(question.image).into(binding.imageViewQuestion)
        } else {
            binding.imageViewQuestion.visibility = View.GONE
        }

        binding.buttonOptionA.text = question.options[0]
        binding.buttonOptionB.text = question.options[1]
        binding.buttonOptionC.text = question.options[2]
        binding.buttonOptionD.text = question.options[3]

        // Seçili cevabı geri getir
        val selectedAnswer = userAnswers[index]
        resetOptionButtonsColor()
        if (selectedAnswer != null) {
            when (selectedAnswer) {
                binding.buttonOptionA.text -> binding.buttonOptionA.setBackgroundColor(resources.getColor(android.R.color.holo_purple))
                binding.buttonOptionB.text -> binding.buttonOptionB.setBackgroundColor(resources.getColor(android.R.color.holo_purple))
                binding.buttonOptionC.text -> binding.buttonOptionC.setBackgroundColor(resources.getColor(android.R.color.holo_purple))
                binding.buttonOptionD.text -> binding.buttonOptionD.setBackgroundColor(resources.getColor(android.R.color.holo_purple))
            }
        }
    }

    private fun calculateCorrectAnswers(): Int {
        var correctCount = 0
        for ((index, userAnswer) in userAnswers) {
            val correctAnswer = questionsList[index].answer
            if (userAnswer == correctAnswer) {
                correctCount++
            }
        }
        return correctCount
    }

    private fun finishQuiz() {
        countDownTimer.cancel()
        val correct = calculateCorrectAnswers()
        showResultDialog(correct)
    }

    private fun showResultDialog(correctCount: Int) {
        val total = questionsList.size
        val dialog = android.app.AlertDialog.Builder(requireContext())
            .setTitle("Quiz Tamamlandı")
            .setMessage("Tebrikler! $total sorudan $correctCount tanesini doğru yaptınız!")
            .setPositiveButton("Kategoriye Dön") { _, _ ->
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
            .setCancelable(false)
            .create()
        dialog.show()
    }

    private fun startTimer() {
        countDownTimer = object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                updateTimerText()
            }

            override fun onFinish() {
                val correct = calculateCorrectAnswers()
                showResultDialog(correct)
            }
        }.start()
    }

    private fun updateTimerText() {
        val minutes = (timeLeftInMillis / 1000) / 60
        val seconds = (timeLeftInMillis / 1000) % 60
        binding.textViewTimer.text = String.format("%02d:%02d", minutes, seconds)
    }

    private fun getUrlForCategory(category: String): String {
        return when (category) {
            "Sanat" -> "https://raw.githubusercontent.com/ikrabu/Quiz/refs/heads/main/questions_art"
            "Bilim" -> "https://raw.githubusercontent.com/ikrabu/Quiz/refs/heads/main/questions_science"
            "Tarih" -> "https://raw.githubusercontent.com/ikrabu/Quiz/refs/heads/main/questions_history"
            "Coğrafya" -> "https://raw.githubusercontent.com/ikrabu/Quiz/refs/heads/main/questions_geography"
            "Edebiyat" -> "https://raw.githubusercontent.com/ikrabu/Quiz/refs/heads/main/questions_literature"
            "Sinema" -> "https://raw.githubusercontent.com/ikrabu/Quiz/refs/heads/main/questions_movies"
            "Spor" -> "https://raw.githubusercontent.com/ikrabu/Quiz/refs/heads/main/questions_sports"
            else -> "https://raw.githubusercontent.com/ikrabu/Quiz/refs/heads/main/questions_technology"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (::countDownTimer.isInitialized) {
            countDownTimer.cancel()
        }
        _binding = null
    }
}
