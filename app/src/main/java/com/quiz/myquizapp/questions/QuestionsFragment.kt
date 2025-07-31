package com.quiz.myquizapp.questions

import android.app.AlertDialog
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
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
    private var hasWarnedUser = false

    private val userAnswers = mutableMapOf<Int, String>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentQuestionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val category = args.selectedCategory
        val difficulty = args.difficultyLevel

        when (difficulty) {
            "easy" -> timeLeftInMillis = 10 * 60 * 1000 // 10 dakika
            "medium" -> timeLeftInMillis = 5 * 60 * 1000 // 5 dakika
            "hard" -> timeLeftInMillis = 10 * 60 * 1000
        }

        viewModel.fetchQuestionsForCategory(getUrlForCategory(category))

        viewModel.questions.observe(viewLifecycleOwner) { list ->
            if (!list.isNullOrEmpty()) {
                questionsList = if (difficulty == "hard") {
                    list.take(20) // zor modda 20 soru
                } else {
                    list.take(10) // kolay/orta modda 10 soru
                }
                currentQuestionIndex = 0
                displayQuestion(currentQuestionIndex)
                startTimer()
            }
        }

        binding.buttonOptionA.setOnClickListener { handleAnswerClick(binding.buttonOptionA) }
        binding.buttonOptionB.setOnClickListener { handleAnswerClick(binding.buttonOptionB) }
        binding.buttonOptionC.setOnClickListener { handleAnswerClick(binding.buttonOptionC) }
        binding.buttonOptionD.setOnClickListener { handleAnswerClick(binding.buttonOptionD) }

        binding.buttonNext.setOnClickListener {
            if (currentQuestionIndex < questionsList.size - 1) {
                currentQuestionIndex++
                displayQuestion(currentQuestionIndex)
            } else {
                Toast.makeText(requireContext(), "Son sorudasınız!", Toast.LENGTH_LONG).show()
            }
        }

        binding.buttonPrevious.setOnClickListener {
            if (currentQuestionIndex > 0) {
                currentQuestionIndex--
                displayQuestion(currentQuestionIndex)
            } else {
                Toast.makeText(requireContext(), "İlk sorudasınız!", Toast.LENGTH_SHORT).show()
            }
        }

        binding.buttonFinish.setOnClickListener { finishQuiz() }

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
        selectedButton.setBackgroundColor(resources.getColor(android.R.color.holo_blue_dark))
    }

    private fun resetOptionButtonsColor() {
        val defaultColor = resources.getColor(R.color.lacivert)
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
            binding.imageViewQuestion.setImageResource(R.drawable.questionmark)
            binding.imageViewQuestion.visibility = View.VISIBLE
        }

        binding.progressBar.max = questionsList.size
        binding.progressBar.progress = index + 1
        binding.textViewQuestionProgress.text = "Soru ${index + 1} / ${questionsList.size}"

        binding.buttonOptionA.text = question.options[0]
        binding.buttonOptionB.text = question.options[1]
        binding.buttonOptionC.text = question.options[2]
        binding.buttonOptionD.text = question.options[3]

        val selectedAnswer = userAnswers[index]
        resetOptionButtonsColor()
        selectedAnswer?.let {
            when (it) {
                binding.buttonOptionA.text -> binding.buttonOptionA.setBackgroundColor(resources.getColor(android.R.color.holo_purple))
                binding.buttonOptionB.text -> binding.buttonOptionB.setBackgroundColor(resources.getColor(android.R.color.holo_purple))
                binding.buttonOptionC.text -> binding.buttonOptionC.setBackgroundColor(resources.getColor(android.R.color.holo_purple))
                binding.buttonOptionD.text -> binding.buttonOptionD.setBackgroundColor(resources.getColor(android.R.color.holo_purple))
            }
        }
    }

    private fun calculateCorrectAnswers(): Int {
        return userAnswers.count { (index, answer) ->
            answer == questionsList[index].answer
        }
    }

    private fun finishQuiz() {
        if (!isAdded) return

        val correctAnswers = calculateCorrectAnswers()
        val difficulty = args.difficultyLevel
        val minCorrect = when (difficulty) {
            "easy", "medium" -> 6
            "hard" -> 10
            else -> 6
        }

        difficulty

        if (correctAnswers >= minCorrect) {
            showResultDialog(correctAnswers, isSuccess = true) // 🎈 Balloon dialog göster
        } else {
            showResultDialog(correctAnswers, isSuccess = false) // 🙁 Sad dialog
        }

    }


    private fun updateUserScoreInFirestore(pointToAdd: Int, onComplete: (leveledUp: Boolean) -> Unit) {
        val user = FirebaseAuth.getInstance().currentUser ?: return onComplete(false)

        val db = Firebase.firestore
        val userRef = db.collection("users").document(user.uid)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(userRef)
            val currentPoint = snapshot.getLong("point") ?: 0
            val currentLevel = snapshot.getLong("level") ?: 1
            val newPoint = currentPoint + pointToAdd

            val leveledUp = newPoint >= 10
            if (leveledUp) {
                transaction.update(userRef, "level", currentLevel + 1)
                transaction.update(userRef, "point", 0)
            } else {
                transaction.update(userRef, "point", newPoint)
            }
            leveledUp
        }.addOnSuccessListener { leveledUp -> onComplete(leveledUp) }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Puan güncellenemedi!", Toast.LENGTH_SHORT).show()
                onComplete(false)
            }
    }

    private fun showResultDialog(correctCount: Int, isSuccess: Boolean) {
        if (!isAdded) return

        val total = questionsList.size
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_result, null)

        val imageResult = view.findViewById<ImageView>(R.id.imageResult)
        val textResultMessage = view.findViewById<TextView>(R.id.textResultMessage)

        if (isSuccess) {
            imageResult.setImageResource(R.drawable.balloon)
            textResultMessage.text = "Tebrikler! $total sorudan $correctCount tanesini doğru cevapladınız 🎉"
        } else {
            imageResult.setImageResource(R.drawable.sad)
            textResultMessage.text = "Daha iyisini yapabilirsin! $total sorudan sadece $correctCount doğru 🙁"
        }

        AlertDialog.Builder(requireContext())
            .setView(view)
            .setPositiveButton("Kategoriye Dön") { _, _ ->
                if (isSuccess) {
                    updateUserScoreInFirestore(/* puan eklenecek */1) { leveledUp ->
                        if (isAdded) {
                            val action = QuestionsFragmentDirections.actionQuestionsFragmentToListFragment(leveledUp)
                            findNavController().navigate(action)
                        }
                    }
                } else {
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
            }
            .setCancelable(false)
            .create()
            .show()
    }






    private fun startWarningAnimation() {
        val blinkAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.blink)
        binding.textViewTimer.startAnimation(blinkAnim)
    }

    private fun startTimer() {
        countDownTimer = object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                updateTimerText()

                if (millisUntilFinished <= 300_000 && !hasWarnedUser) {
                    hasWarnedUser = true
                    startWarningAnimation()
                }
            }

            override fun onFinish() {
                val correct = calculateCorrectAnswers()

                val minCorrect = when (args.difficultyLevel) {
                    "easy", "medium" -> 6
                    "hard" -> 10
                    else -> 6
                }

                val isSuccess = correct >= minCorrect

                showResultDialog(correct, isSuccess)
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