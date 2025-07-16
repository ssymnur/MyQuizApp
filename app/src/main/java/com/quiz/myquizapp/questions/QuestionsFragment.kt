package com.quiz.myquizapp.questions

import android.os.Bundle
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

        viewModel.questions.observe(viewLifecycleOwner) { questionsList ->
            if (!questionsList.isNullOrEmpty()) {
                val question = questionsList.first()
                binding.textViewQuestion.text = question.question

                if (question.image != null) {
                    binding.imageViewQuestion.visibility = View.VISIBLE
                    Glide.with(requireContext())
                        .load(question.image)
                        .into(binding.imageViewQuestion)
                } else {
                    binding.imageViewQuestion.visibility = View.GONE
                }

                binding.buttonOptionA.text = question.options[0]
                binding.buttonOptionB.text = question.options[1]
                binding.buttonOptionC.text = question.options[2]
                binding.buttonOptionD.text = question.options[3]
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMsg ->
            Toast.makeText(requireContext(), "Hata: $errorMsg", Toast.LENGTH_SHORT).show()
        }
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
        _binding = null
    }
}
