package com.quiz.myquizapp

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.quiz.myquizapp.databinding.FragmentListBinding
import com.quiz.myquizapp.di.ListViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ListFragment : Fragment() {

    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!

    private var selectedDifficulty: String = "easy" // Default seviye: kolay

    private lateinit var categoryAdapter: CategoryAdapter
    private val viewModel: ListViewModel by viewModels()

    private var userDocListener: ListenerRegistration? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateDifficultyUI()

        // Zorluk seviyeleri butonları ve bilgilendirme dialogları
        binding.easyButton.setOnClickListener {
            selectedDifficulty = "easy"
            updateDifficultyUI()
            showDifficultyInfo("Kolay")
        }

        binding.mediumButton.setOnClickListener {
            selectedDifficulty = "medium"
            updateDifficultyUI()
            showDifficultyInfo("Orta")
        }

        binding.hardButton.setOnClickListener {
            selectedDifficulty = "hard"
            updateDifficultyUI()
            showDifficultyInfo("Zor")
        }



        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val userDocRef = Firebase.firestore.collection("users").document(userId)

            userDocRef.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val nickname = document.getString("nickname") ?: "Kullanıcı"
                    val avatarIndex = document.getLong("avatar")?.toInt() ?: -1
                    val avatarResId = when (avatarIndex) {
                        0 -> R.drawable.avatar_male
                        1 -> R.drawable.avatar_female
                        2 -> R.drawable.avatar_female2
                        3 -> R.drawable.avatar_male2
                        4 -> R.drawable.avatar_male3
                        5 -> R.drawable.avatar_female3
                        6 -> R.drawable.avatar_female4
                        7 -> R.drawable.avatar_male4
                        8 -> R.drawable.fruit_icons2
                        9 -> R.drawable.fruit_icons3
                        10 -> R.drawable.fruit_icons4
                        11 -> R.drawable.fruit_icons
                        else -> R.drawable.default_avatar
                    }

                    binding.textViewWelcome.text =
                        "Hoş geldin $nickname!\nHaydi şimdi soruları çözmeye başlayalım!"
                    binding.imageViewAvatar.setImageResource(avatarResId)
                }
            }

            userDocRef.get().addOnSuccessListener { document ->
                if (!document.exists()) {
                    val initialData = hashMapOf("point" to 0, "level" to 1)
                    userDocRef.set(initialData)
                }
            }

            userDocListener = userDocRef.addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener
                val point = snapshot.getLong("point") ?: 0
                val level = snapshot.getLong("level") ?: 1
                if (isAdded && _binding != null) {
                    binding.textViewStats.text = "Puan: $point  |  Seviye: $level"
                }
            }
        }

        // Kategori listesi - Tek sütun
        binding.ListRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        categoryAdapter = CategoryAdapter(listOf()) { selectedCategory ->
            val action = ListFragmentDirections.actionListFragmentToQuestionsFragmentWithLevel(
                selectedCategory = selectedCategory.name,
                difficultyLevel = selectedDifficulty // seçilen zorluk seviyesi
            )
            findNavController().navigate(action)
        }
        binding.ListRecyclerView.adapter = categoryAdapter

        viewModel.getCategories()
        viewModel.categories.observe(viewLifecycleOwner) { list ->
            categoryAdapter.updateList(list)
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMsg ->
            Toast.makeText(requireContext(), "Hata: $errorMsg", Toast.LENGTH_SHORT).show()
        }

        // Footer ikonlarına tıklama
        binding.imageViewLeaderboard.setOnClickListener {
            findNavController().navigate(
                R.id.action_listFragment_to_leaderboardFragment,
                null,
                androidx.navigation.NavOptions.Builder()
                    .setPopUpTo(R.id.entryFragment, inclusive = true)
                    .build()
            )
        }

        binding.imageViewLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            findNavController().navigate(
                R.id.action_listFragment_to_entryFragment,
                null,
                androidx.navigation.NavOptions.Builder()
                    .setPopUpTo(R.id.entryFragment, inclusive = true)
                    .build()
            )
        }

        binding.imageViewProfile.setOnClickListener {
            findNavController().navigate(R.id.action_listFragment_to_profileFragment)
        }
    }

    // Seviye bilgisi gösteren AlertDialog fonksiyonu
    private fun showDifficultyInfo(level: String) {
        val message = when (level) {
            "Kolay" -> "Kolay modda 10 dakika içerisinde 10 sorudan en az 6'sını doğru cevaplayarak 1 puan kazanabilirsin."
            "Orta" -> "Orta modda 5 dakika içinde 10 sorudan en az 6'sını doğru yaparsan 2 puan kazanacaksın."
            "Zor" -> "Zor modda 10 dakika içerisinde 20 sorudan en az 10 tanesini doğru cevaplayarak tam 3 puan kazanabilirsin!"
            else -> ""
        }

        AlertDialog.Builder(requireContext())
            .setTitle("$level Seviye Seçildi")
            .setMessage(message)
            .setPositiveButton("Hazırım") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun updateDifficultyUI() {
        // Tüm butonları gri yap
        binding.easyButton.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.gray_unselected))
        binding.mediumButton.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.gray_unselected))
        binding.hardButton.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.gray_unselected))

        // Seçilen butonu renklendir
        when (selectedDifficulty) {
            "easy" -> binding.easyButton.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.green_selected))
            "medium" -> binding.mediumButton.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.orange_selected))
            "hard" -> binding.hardButton.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.red_selected))
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        userDocListener?.remove()
        userDocListener = null
        _binding = null
    }
}
