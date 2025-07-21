package com.quiz.myquizapp

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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

    private lateinit var categoryAdapter: CategoryAdapter
    private val viewModel: ListViewModel by viewModels()

    private var userDocListener: ListenerRegistration? = null  // Listener referansı

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val userDocRef = Firebase.firestore.collection("users").document(userId)

            // Belge yoksa oluştur
            userDocRef.get().addOnSuccessListener { document ->
                if (!document.exists()) {
                    val initialData = hashMapOf("point" to 0, "level" to 1)
                    userDocRef.set(initialData)
                }
            }

            // Puan ve seviye güncellemesi için listener kaydet
            userDocListener = userDocRef.addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener
                val point = snapshot.getLong("point") ?: 0
                val level = snapshot.getLong("level") ?: 1

                // binding null değil ve fragment view hâlâ var mı kontrolü
                if (isAdded && _binding != null) {
                    binding.textViewStats.text = "Puan: $point  |  Seviye: $level"
                }
            }
        }

        // SharedPreferences'ten kullanıcı bilgilerini çekme
        val currentUser = FirebaseAuth.getInstance().currentUser
        val emailKey = currentUser?.email?.replace(".", "_") ?: "default"
        val sharedPref = requireContext().getSharedPreferences("UserProfile_$emailKey", Context.MODE_PRIVATE)

        val nickname = sharedPref.getString("nickname", "Kullanıcı")
        val avatarResId = sharedPref.getInt("avatar", R.drawable.default_avatar)

        binding.textViewWelcome.text = "Hoş geldin $nickname!\nHaydi şimdi soruları çözmeye başlayalım!"
        binding.imageViewAvatar.setImageResource(avatarResId)

        // Kategori listesi
        binding.ListRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        categoryAdapter = CategoryAdapter(listOf()) { selectedCategory ->
            val action = ListFragmentDirections.actionListFragmentToQuestionsFragment(selectedCategory)
            findNavController().navigate(action)
        }
        binding.ListRecyclerView.adapter = categoryAdapter

        viewModel.getCategories()

        viewModel.categories.observe(viewLifecycleOwner) { list ->
            val names = list.map { it.name }
            categoryAdapter.updateList(names)
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMsg ->
            Toast.makeText(requireContext(), "Hata: $errorMsg", Toast.LENGTH_SHORT).show()
        }

        // Çıkış yap butonu
        binding.buttonLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            findNavController().navigate(
                R.id.action_listFragment_to_entryFragment,
                null,
                androidx.navigation.NavOptions.Builder()
                    .setPopUpTo(R.id.entryFragment, inclusive = true)
                    .build()
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Listener iptali, böylece view yokken callback olmaz
        userDocListener?.remove()
        userDocListener = null
        _binding = null
    }
}
