package com.quiz.myquizapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.quiz.myquizapp.databinding.FragmentListBinding
import com.quiz.myquizapp.di.ListViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ListFragment : Fragment() {

    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!

    private lateinit var categoryAdapter: CategoryAdapter
    private val viewModel: ListViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ListRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Adapterı boş liste ve click listener ile başlat
        categoryAdapter = CategoryAdapter(listOf()) { selectedCategory ->
            val action = ListFragmentDirections.actionListFragmentToQuestionsFragment(selectedCategory)
            findNavController().navigate(action)
        }
        binding.ListRecyclerView.adapter = categoryAdapter

        // Kategorileri çağır
        viewModel.getCategories()

        // Gelen kategoriler gözlemleniyor, adapter güncelleniyor
        viewModel.categories.observe(viewLifecycleOwner) { list ->
            val names = list.map { it.name }
            categoryAdapter.updateList(names)
        }

        // Hata varsa Toast göster
        viewModel.error.observe(viewLifecycleOwner) { errorMsg ->
            Toast.makeText(requireContext(), "Hata: $errorMsg", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
