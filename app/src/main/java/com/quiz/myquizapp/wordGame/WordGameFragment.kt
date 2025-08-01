package com.quiz.myquizapp.wordGame

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.quiz.myquizapp.R
import com.quiz.myquizapp.databinding.FragmentWordGameBinding
import com.quiz.myquizapp.questions.QuestionsFragmentDirections
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@AndroidEntryPoint
class WordGameFragment : Fragment() {

    private var _binding: FragmentWordGameBinding? = null
    private val binding get() = _binding!!

    private val userAnswers = mutableListOf<String?>()
    private val viewModel: WordViewModel by viewModels()
    private val args: WordGameFragmentArgs by navArgs()

    private lateinit var level: String
    private lateinit var categoryName: String

    private var wordList = listOf<WordModel>()
    private var currentIndex = 0
    private var correctCount = 0

    private lateinit var countDownTimer: CountDownTimer
    private var timeLeftInMillis: Long = 600_000
    private var hasWarnedUser = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentWordGameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val lottieFeedback = binding.lottieFeedback

        level = args.difficultyLevel
        categoryName = args.category

        setTimeByLevel()
        fetchWords()

        binding.buttonCheck.setOnClickListener {
            if (currentIndex >= wordList.size) return@setOnClickListener

            val userAnswer = normalizeText(binding.editTextAnswer.text.toString())
            userAnswers.add(userAnswer)
            val correctAnswer = normalizeText(wordList[currentIndex].answer)

            if (userAnswer == correctAnswer) {
                correctCount++
                binding.textViewResult.text = "✅ Doğru!"

                lottieFeedback.setAnimation("like.json")
                lottieFeedback.visibility = View.VISIBLE
                lottieFeedback.playAnimation()
            } else {
                binding.textViewResult.text = "❌ Yanlış! Doğru cevap: $correctAnswer"

                lottieFeedback.setAnimation("error.json")
                lottieFeedback.visibility = View.VISIBLE
                lottieFeedback.playAnimation()
            }


            binding.buttonCheck.isEnabled = false

            binding.root.postDelayed({
                lottieFeedback.cancelAnimation()
                lottieFeedback.visibility = View.GONE

                currentIndex++

                if (currentIndex < wordList.size) {
                    displayCurrentWord()
                    binding.buttonCheck.isEnabled = true
                } else {
                    if (::countDownTimer.isInitialized) countDownTimer.cancel()
                    finishGame()
                }
            }, 2000)

            binding.editTextAnswer.text.clear()
        }

        binding.buttonFinish.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Oyunu Bitir")
                .setMessage("Oyunu bitirmek istediğine emin misin?")
                .setPositiveButton("Evet") { _, _ ->
                    val action = WordGameFragmentDirections.actionWordGameFragmentToListFragment(false)
                    findNavController().navigate(action)
                }
                .setNegativeButton("Oyuna devam et") { dialog, _ -> dialog.dismiss() }
                .show()
        }

        binding.buttonNext.setOnClickListener {
            if (currentIndex >= wordList.size) return@setOnClickListener
            userAnswers.add(null)
            currentIndex++

            if (currentIndex < wordList.size) {
                displayCurrentWord()
                binding.buttonCheck.isEnabled = true
            } else {
                if (::countDownTimer.isInitialized) countDownTimer.cancel()
                finishGame()
            }

            binding.editTextAnswer.text.clear()
        }
    }

    private fun setTimeByLevel() {
        timeLeftInMillis = when (level) {
            "easy" -> 10 * 60 * 1000
            "medium" -> 5 * 60 * 1000
            "hard" -> 10 * 60 * 1000
            else -> 10 * 60 * 1000
        }
    }


    private fun fetchWords() {
        val category = WordCategory.fromDisplayName(categoryName)
        val retrofit = Retrofit.Builder()
            .baseUrl("https://raw.githubusercontent.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val service = retrofit.create(WordGameService::class.java)

        lifecycleScope.launch {
            try {
                val allWords = service.getWords(category.url).words
                wordList = when (level) {
                    "easy" -> allWords.shuffled().take(10)
                    "medium" -> allWords.shuffled().take(15)
                    "hard" -> allWords.shuffled().take(20)
                    else -> allWords.shuffled().take(10)
                }


                currentIndex = 0
                displayCurrentWord()
                startTimer()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Veri alınamadı: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun displayCurrentWord() {
        val current = wordList[currentIndex]
        binding.textViewScrambledWord.text = current.question
        binding.textViewResult.text = ""
        binding.editTextAnswer.text.clear()

        if (!current.image.isNullOrEmpty()) {
            binding.imageViewWord.visibility = View.VISIBLE
            Glide.with(requireContext()).load(current.image).into(binding.imageViewWord)
        } else {
            binding.imageViewWord.setImageResource(R.drawable.questionmark)
        }

        val total = wordList.size
        val currentNumber = currentIndex + 1
        binding.textViewQuestionProgress.text = "$currentNumber/$total"

        val progressPercent = (currentNumber.toFloat() / total.toFloat()) * 100
        binding.progressBar.progress = progressPercent.toInt()
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
                finishGame()
            }
        }.start()
    }

    private fun updateTimerText() {
        val minutes = (timeLeftInMillis / 1000) / 60
        val seconds = (timeLeftInMillis / 1000) % 60
        binding.textViewTimer.text = String.format("%02d:%02d", minutes, seconds)
    }

    private fun startWarningAnimation() {
        val blinkAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.blink)
        binding.textViewTimer.startAnimation(blinkAnim)
    }

    private fun finishGame() {
        val minCorrect = when (level) {
            "easy" -> 6
            "medium" -> 9
            "hard" -> 14
            else -> 6
        }
        val isSuccess = correctCount >= minCorrect
        showResultDialog(correctCount, isSuccess)
    }

    private fun showResultDialog(correctCount: Int, isSuccess: Boolean) {
        if (!isAdded) return

        val total = wordList.size
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
                    updateUserScoreInFirestore(1) { leveledUp ->
                        if (isAdded) {
                            val action = QuestionsFragmentDirections.actionQuestionsFragmentToListFragment(leveledUp)
                            findNavController().navigate(action)
                        }
                    }
                } else {
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
            }
            .setNeutralButton("Cevapları Göster") { _, _ ->
                showAnswersDialog()
            }
            .setCancelable(false)
            .create()
            .show()
    }

    private fun showAnswersDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Sorular ve Cevaplar")

        val message = StringBuilder()

        for ((index, question) in wordList.withIndex()) {
            val correctAnswer = question.answer
            val userAnswer = userAnswers.getOrNull(index) ?: "Boş bırakıldı"
            val isCorrect = normalizeText(userAnswer) == normalizeText(correctAnswer)

            message.append("🔹 Soru ${index + 1}: ${question.question}\n")
            message.append("✔️ Doğru Cevap: $correctAnswer\n")
            message.append("🧍 Senin Cevabın: $userAnswer ${if (isCorrect) "✅" else "❌"}\n\n")
        }

        builder.setMessage(message.toString())
        builder.setPositiveButton("Kapat") { dialog, _ -> dialog.dismiss() }
        builder.show()
    }

    private fun updateUserScoreInFirestore(points: Int, onComplete: (Boolean) -> Unit) {
        val user = FirebaseAuth.getInstance().currentUser ?: return onComplete(false)
        val userRef = Firebase.firestore.collection("users").document(user.uid)

        Firebase.firestore.runTransaction { transaction ->
            val snapshot = transaction.get(userRef)
            val currentPoint = snapshot.getLong("point") ?: 0
            val currentLevel = snapshot.getLong("level") ?: 1
            val newPoint = currentPoint + points

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

    private fun normalizeText(text: String): String {
        return text
            .trim()
            .lowercase()
            .replace("ç", "c")
            .replace("ğ", "g")
            .replace("ı", "i")
            .replace("ö", "o")
            .replace("ş", "s")
            .replace("ü", "u")
    }

    override fun onDestroyView() {
        if (::countDownTimer.isInitialized) {
            countDownTimer.cancel()
        }
        _binding = null
        super.onDestroyView()
    }
}
