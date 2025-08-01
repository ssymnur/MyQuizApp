package com.quiz.myquizapp

import android.animation.Animator
import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.airbnb.lottie.LottieAnimationView
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

    private var selectedCategoryButton: Int = R.id.btnSelectCategory

    private var isSecondGameMode = false
    private var isCategoryVisible = true
    private var previousLevel: Long = 1
    private var isInitialLoad = true
    private var selectedDifficulty: String = "easy"

    private lateinit var categoryAdapter: CategoryAdapter
    private val viewModel: ListViewModel by viewModels()

    private var userDocListener: ListenerRegistration? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // --- Butonlar ilk durumda ayarlansın ---
        selectedCategoryButton = R.id.btnSelectCategory
        isSecondGameMode = false
        updateSelectedGameModeUI()
        binding.btnSelectCategory.background = ContextCompat.getDrawable(requireContext(), R.drawable.gradient_blue)
        binding.btnSelectCategory.backgroundTintList = null
        binding.btnSelectCategory2.background = ContextCompat.getDrawable(requireContext(), R.drawable.solid_lacivert)
        binding.btnSelectCategory2.backgroundTintList = null

        val args = arguments?.let { ListFragmentArgs.fromBundle(it) }
        if (args?.leveledUp == true) {
            showConfettiDialog()
            parentFragmentManager.beginTransaction().run {
                replace(id, this@ListFragment)
                commit()
            }
        }

        binding.btnSelectCategory.setOnClickListener {
            selectedCategoryButton = R.id.btnSelectCategory
            isSecondGameMode = false
            setupCategoryAdapter()
            showCategoryList()
            updateSelectedGameModeUI()

            // Arka planı setle ve tint'i temizle
            binding.btnSelectCategory.background = ContextCompat.getDrawable(requireContext(), R.drawable.gradient_blue)
            binding.btnSelectCategory.backgroundTintList = null

            binding.btnSelectCategory2.background = ContextCompat.getDrawable(requireContext(), R.drawable.solid_lacivert)
            binding.btnSelectCategory2.backgroundTintList = null
        }

        binding.btnSelectCategory2.setOnClickListener {
            selectedCategoryButton = R.id.btnSelectCategory2
            isSecondGameMode = true
            setupCategoryAdapter()
            showCategoryList()
            updateSelectedGameModeUI()

            binding.btnSelectCategory2.background = ContextCompat.getDrawable(requireContext(), R.drawable.gradient_blue)
            binding.btnSelectCategory2.backgroundTintList = null

            binding.btnSelectCategory.background = ContextCompat.getDrawable(requireContext(), R.drawable.solid_lacivert)
            binding.btnSelectCategory.backgroundTintList = null
        }



        updateDifficultyUI()

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

                    binding.textViewWelcome.text = "Hoş geldin $nickname!\nHaydi şimdi soruları çözmeye başlayalım!"
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

                if (isInitialLoad) {
                    previousLevel = level
                    isInitialLoad = false
                } else if (level > previousLevel) {
                    showConfettiDialog()
                    previousLevel = level
                } else {
                    previousLevel = level
                }

                if (isAdded && _binding != null) {
                    binding.textViewStats.text = "Puan: $point  |  Seviye: $level"
                }
            }
        }

        binding.ListRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        setupCategoryAdapter()

        viewModel.getCategories()
        viewModel.categories.observe(viewLifecycleOwner) { list ->
            categoryAdapter.updateList(list)
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMsg ->
            Toast.makeText(requireContext(), "Hata: $errorMsg", Toast.LENGTH_SHORT).show()
        }

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
            AlertDialog.Builder(requireContext())
                .setTitle("Çıkış Yap")
                .setMessage("Çıkış yapmak istediğinize emin misiniz?")
                .setPositiveButton("Evet") { _, _ ->
                    FirebaseAuth.getInstance().signOut()
                    findNavController().navigate(
                        R.id.action_listFragment_to_entryFragment,
                        null,
                        androidx.navigation.NavOptions.Builder()
                            .setPopUpTo(R.id.entryFragment, inclusive = true)
                            .build()
                    )
                }
                .setNegativeButton("Hayır") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }


        binding.imageViewProfile.setOnClickListener {
            findNavController().navigate(R.id.action_listFragment_to_profileFragment)
        }

        binding.dailyGiftButton.setOnClickListener {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener
            val userDocRef = Firebase.firestore.collection("users").document(userId)

            userDocRef.get().addOnSuccessListener { document ->
                val now = System.currentTimeMillis()
                val lastGiftTime = document.getLong("lastGiftTimestamp") ?: 0
                val twentyFourHoursMillis = 24 * 60 * 60 * 1000

                if (now - lastGiftTime >= twentyFourHoursMillis) {
                    val randomPoint = (1..2).random()
                    val currentPoint = document.getLong("point") ?: 0
                    val newPoint = currentPoint + randomPoint

                    userDocRef.update(
                        mapOf(
                            "point" to newPoint,
                            "lastGiftTimestamp" to now
                        )
                    ).addOnSuccessListener {
                        showGiftAnimation(randomPoint)
                    }
                } else {
                    val remainingMillis = twentyFourHoursMillis - (now - lastGiftTime)
                    val hoursLeft = remainingMillis / (60 * 60 * 1000)
                    val minutesLeft = (remainingMillis / (60 * 1000)) % 60
                    Toast.makeText(
                        requireContext(),
                        "🎁 Tekrar açmak için: $hoursLeft saat $minutesLeft dakika kaldı",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun showCategoryList() {
        isCategoryVisible = true
        binding.ListRecyclerView.visibility = View.VISIBLE
    }


    private fun setupCategoryAdapter() {
        categoryAdapter = CategoryAdapter(viewModel.categories.value ?: listOf()) { selectedCategory ->
            if (isSecondGameMode) {
                val action = ListFragmentDirections.actionListFragmentToWordGameFragment(selectedCategory.name)
                findNavController().navigate(action)
            } else {
                val action = ListFragmentDirections.actionListFragmentToQuestionsFragmentWithLevel(
                    selectedCategory = selectedCategory.name,
                    difficultyLevel = selectedDifficulty
                )
                findNavController().navigate(action)
            }
        }
        binding.ListRecyclerView.adapter = categoryAdapter
        binding.ListRecyclerView.visibility = View.VISIBLE
    }

    private fun showGiftAnimation(points: Int) {
        binding.giftOverlay.visibility = View.VISIBLE
        binding.giftPointText.visibility = View.GONE
        binding.giftAnimationView.setAnimation(R.raw.gift_box)
        binding.giftAnimationView.playAnimation()

        binding.giftAnimationView.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationEnd(animation: Animator) {
                binding.giftPointText.text = "🎉 $points Puan Kazandınız! 🎉"
                binding.giftPointText.visibility = View.VISIBLE
                binding.giftAnimationView.removeAllAnimatorListeners()
            }

            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })

        binding.giftOverlay.setOnClickListener {
            binding.giftOverlay.visibility = View.GONE
            binding.giftAnimationView.cancelAnimation()
        }
    }

    private fun updateSelectedGameModeUI() {
        if (selectedCategoryButton == R.id.btnSelectCategory) {
            // Birinci buton gradient olsun
            binding.btnSelectCategory.setBackgroundResource(R.drawable.gradient_blue)

            // İkinci buton düz lacivert
            binding.btnSelectCategory2.background = ContextCompat.getDrawable(requireContext(), R.drawable.solid_lacivert)
        } else {
            binding.btnSelectCategory2.setBackgroundResource(R.drawable.gradient_blue)
            binding.btnSelectCategory.background = ContextCompat.getDrawable(requireContext(), R.drawable.solid_lacivert)
        }
    }






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
            .setPositiveButton("Hazırım") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun updateDifficultyUI() {
        binding.easyButton.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.gray_unselected))
        binding.mediumButton.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.gray_unselected))
        binding.hardButton.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.gray_unselected))

        when (selectedDifficulty) {
            "easy" -> binding.easyButton.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.green_selected))
            "medium" -> binding.mediumButton.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.orange_selected))
            "hard" -> binding.hardButton.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.red_selected))
        }
    }



    private fun showConfettiDialog() {
        binding.confettiOverlay.visibility = View.VISIBLE
        binding.confettiAnimationView.playAnimation()

        binding.confettiAnimationView.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationEnd(animation: Animator) {
                binding.confettiOverlay.visibility = View.GONE
            }

            override fun onAnimationCancel(animation: Animator) {
                binding.confettiOverlay.visibility = View.GONE
            }

            override fun onAnimationRepeat(animation: Animator) {}
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        userDocListener?.remove()
        userDocListener = null
        _binding = null
    }
}
