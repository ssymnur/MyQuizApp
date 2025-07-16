package com.quiz.myquizapp.fragment.entry

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.quiz.myquizapp.R
import com.quiz.myquizapp.databinding.FragmentEntryBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EntryFragment : Fragment() {

    private var _binding: FragmentEntryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EntryViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEntryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonRegister.setOnClickListener {
            val email = binding.editTextEmail.text.toString().trim()
            val password = binding.editTextPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                viewModel.register(email, password)
            } else {
                Toast.makeText(requireContext(), "Email ve şifre boş olamaz!", Toast.LENGTH_SHORT).show()
            }
        }

        binding.buttonLogin.setOnClickListener {
            val email = binding.editTextEmail.text.toString().trim()
            val password = binding.editTextPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                viewModel.login(email, password)
            } else {
                Toast.makeText(requireContext(), "Email ve şifre boş olamaz!", Toast.LENGTH_SHORT).show()
            }
        }

        // Giriş/Kayıt sonucu gözlemi
        viewModel.authResult.observe(viewLifecycleOwner) { success ->
            success?.let {
                if (it) {
                    Toast.makeText(requireContext(), "Başarılı giriş/kayıt", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_entryFragment_to_listFragment)
                }
                // İşlem sonrası LiveData’yı resetle
                viewModel.resetAuthResult()
            }
        }

        // Hata mesajları gözlemi
        viewModel.error.observe(viewLifecycleOwner) { errorMsg ->
            errorMsg?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                // Hata gösterildikten sonra resetle
                viewModel.resetError()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
