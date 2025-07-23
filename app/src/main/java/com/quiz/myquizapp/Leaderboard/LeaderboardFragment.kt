package com.quiz.myquizapp.Leaderboard

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.quiz.myquizapp.R
import com.quiz.myquizapp.databinding.FragmentLeaderboardBinding
import com.google.firebase.firestore.Query



class LeaderboardFragment : Fragment() {

    private lateinit var binding: FragmentLeaderboardBinding
    private lateinit var adapter: LeaderboardAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLeaderboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = LeaderboardAdapter()
        binding.leaderboardRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.leaderboardRecyclerView.adapter = adapter

        loadLeaderboardData()

        binding.buttonBackToCategory.setOnClickListener {
            findNavController().navigate(
                R.id.action_leaderboardFragment_to_listFragment,
                null,
                androidx.navigation.NavOptions.Builder()
                    .setPopUpTo(R.id.entryFragment, inclusive = true)
                    .build()
            )
        }
    }

    private fun loadLeaderboardData() {
        val db = FirebaseFirestore.getInstance()
        db.collection("users")
            .orderBy("level", Query.Direction.DESCENDING)
            .limit(10)
            .get()
            .addOnSuccessListener { result ->
                val leaderboardList = mutableListOf<UserModel>()
                for (document in result) {
                    val nickname = document.getString("nickname") ?: ""
                    val avatarIndex = document.getLong("avatar")?.toInt() ?: 0
                    val level = document.getLong("level")?.toInt() ?: 0
                    val point = document.getLong("point")?.toInt() ?: 0

                    leaderboardList.add(UserModel(nickname, avatarIndex, level, point))
                }
                adapter.submitList(leaderboardList)
            }
            .addOnFailureListener {
                Toast.makeText(context, "Lider tablosu yüklenemedi", Toast.LENGTH_SHORT).show()
            }
    }
}
