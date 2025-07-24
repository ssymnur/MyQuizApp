package com.quiz.myquizapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.quiz.myquizapp.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private var selectedAvatarIndex = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val nickname = document.getString("nickname") ?: ""
                    val avatar = document.getLong("avatar")?.toInt() ?: -1
                    val level = document.getLong("level")?.toInt() ?: 1

                    binding.editTextNickname.setText(nickname)
                    selectedAvatarIndex = avatar
                    setAvatarSelection(avatar)

                    setupAvatarClicks(level)
                }
            }

        binding.buttonSave.setOnClickListener {
            val newNickname = binding.editTextNickname.text.toString().trim()
            if (newNickname.isEmpty() || selectedAvatarIndex == -1) {
                Toast.makeText(requireContext(), "Lütfen nickname girin ve avatar seçin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val updates = mapOf(
                "nickname" to newNickname,
                "avatar" to selectedAvatarIndex
            )

            db.collection("users").document(userId)
                .update(updates)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Bilgiler güncellendi", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Güncelleme başarısız", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun setupAvatarClicks(level: Int) {
        // İlk 4 avatar her zaman açık
        binding.avatar1.setOnClickListener { selectAvatar(0) }
        binding.avatar2.setOnClickListener { selectAvatar(1) }
        binding.avatar3.setOnClickListener { selectAvatar(2) }
        binding.avatar4.setOnClickListener { selectAvatar(3) }

        // 5-8: 5. seviyeden sonra açılır
        val level5Locked = level < 5
        val avatarViewsLevel5 = listOf(
            binding.avatar5 to 4,
            binding.avatar6 to 5,
            binding.avatar7 to 6,
            binding.avatar8 to 7
        )

        for ((avatarView, index) in avatarViewsLevel5) {
            if (level5Locked) {
                avatarView.setColorFilter(0x99000000.toInt())
                avatarView.setOnClickListener {
                    Toast.makeText(requireContext(), "Bu avatar seviye 5 ve üzeri için!", Toast.LENGTH_SHORT).show()
                }
            } else {
                avatarView.clearColorFilter()
                avatarView.setOnClickListener { selectAvatar(index) }
            }
        }

        // 9-12: 10. seviyeden sonra açılır
        val level10Locked = level < 10
        val avatarViewsLevel10 = listOf(
            binding.avatar9 to 8,
            binding.avatar10 to 9,
            binding.avatar11 to 10,
            binding.avatar12 to 11
        )

        for ((avatarView, index) in avatarViewsLevel10) {
            if (level10Locked) {
                avatarView.setColorFilter(0x99000000.toInt())
                avatarView.setOnClickListener {
                    Toast.makeText(requireContext(), "Bu avatar seviye 10 ve üzeri için!", Toast.LENGTH_SHORT).show()
                }
            } else {
                avatarView.clearColorFilter()
                avatarView.setOnClickListener { selectAvatar(index) }
            }
        }
    }



    private fun selectAvatar(index: Int) {
        selectedAvatarIndex = index
        setAvatarSelection(index)
    }

    private fun setAvatarSelection(index: Int) {
        val selected = ContextCompat.getDrawable(requireContext(), R.drawable.avatar_selected_border)
        val unselected = ContextCompat.getDrawable(requireContext(), R.drawable.avatar_unselected_background)

        val avatarViews = listOf(
            binding.avatar1, binding.avatar2, binding.avatar3, binding.avatar4,
            binding.avatar5, binding.avatar6, binding.avatar7, binding.avatar8,
            binding.avatar9, binding.avatar10, binding.avatar11, binding.avatar12

        )

        avatarViews.forEachIndexed { i, view ->
            view.background = if (i == index) selected else unselected
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
