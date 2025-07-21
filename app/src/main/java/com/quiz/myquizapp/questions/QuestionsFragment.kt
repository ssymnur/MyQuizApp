package com.quiz.myquizapp.questions

import android.app.AlertDialog
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.quiz.myquizapp.R
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
            }else{
                Toast.makeText(requireContext(),"Son sorudasınız!" , Toast.LENGTH_LONG).show()
            }

        }

        binding.buttonPrevious.setOnClickListener {
            if (currentQuestionIndex > 0) {
                currentQuestionIndex--
                displayQuestion(currentQuestionIndex)
            }else{
                Toast.makeText(requireContext() , "İlk sorudasınız!" , Toast.LENGTH_SHORT).show()
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
        selectedButton.setBackgroundColor(resources.getColor(android.R.color.holo_blue_dark))
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

        //progress bar
        binding.progressBar.max = questionsList.size
        binding.progressBar.progress = index + 1

        binding.textViewQuestionProgress.text = "Soru ${index + 1} / ${questionsList.size}"


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
        if (!isAdded) return
        val context = context ?: return

        val correctAnswers = calculateCorrectAnswers()

        if (correctAnswers >= 6) {
            //Toast.makeText(context, "Tebrikler! 1 puan kazandınız.", Toast.LENGTH_SHORT).show()
            updateUserScoreInFirestore {
                if (!isAdded) return@updateUserScoreInFirestore
                requireActivity().runOnUiThread {
                    showResultDialog(correctAnswers)
                }
            }
        } else {
            showResultDialog(correctAnswers)
        }
    }


    private fun updateUserScoreInFirestore(onComplete: () -> Unit) {
        val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        if (user == null) {
            onComplete()
            return
        }

        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        val userRef = db.collection("users").document(user.uid)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(userRef)
            val currentPoint = snapshot.getLong("point") ?: 0
            val currentLevel = snapshot.getLong("level") ?: 1
            val newPoint = currentPoint + 1

            if (newPoint >= 10) {
                // Seviye atla ve puanı sıfırla
                transaction.update(userRef, "level", currentLevel + 1)
                transaction.update(userRef, "point", 0)
            } else {
                // Sadece puan artır
                transaction.update(userRef, "point", newPoint)
            }
        }.addOnSuccessListener {
            onComplete()
        }.addOnFailureListener {
            if (isAdded) {
                Toast.makeText(requireContext(), "Puan güncellenemedi!", Toast.LENGTH_SHORT).show()
            }
            onComplete()
        }
    }






    private fun showResultDialog(correctCount: Int) {
        if (!isAdded) return

        val total = questionsList.size
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.dialog_result, null)

        val imageResult = view.findViewById<ImageView>(R.id.imageResult)
        val textResultMessage = view.findViewById<TextView>(R.id.textResultMessage)

        if (correctCount >= 6) {
            imageResult.setImageResource(R.drawable.balloon)
            textResultMessage.text = "Tebrikler! $total sorudan $correctCount tanesini doğru cevapladınız 🎉"
        } else {
            imageResult.setImageResource(R.drawable.sad)
            textResultMessage.text = "Daha iyisini yapabilirsin! $total sorudan sadece $correctCount doğru 🙁"
        }

        AlertDialog.Builder(requireContext())
            .setView(view)
            .setPositiveButton("Kategoriye Dön") { _, _ ->
                if (isAdded) {
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
            }
            .setCancelable(false)
            .create()
            .show()
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

    private fun getUrlForCategory(categoryName: String): String {
        val category = QuestionCategory.fromDisplayName(categoryName)
        return category.url
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (::countDownTimer.isInitialized) {
            countDownTimer.cancel()
        }
        _binding = null
    }
}
