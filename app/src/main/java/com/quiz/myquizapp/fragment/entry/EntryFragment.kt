package com.quiz.myquizapp.fragment.entry

import android.content.Context
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

    private var selectedAvatar: Int = R.drawable.default_avatar // default
    private var isRegistering = false
    private var currentEmail: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEntryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.layoutProfile.visibility = View.GONE

        binding.buttonRegister.setOnClickListener {
            val email = binding.editTextEmail.text.toString().trim()
            val password = binding.editTextPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                isRegistering = true
                currentEmail = email
                viewModel.register(email, password)
            } else {
                Toast.makeText(requireContext(), "Email ve şifre boş olamaz!", Toast.LENGTH_SHORT).show()
            }
        }

        binding.buttonLogin.setOnClickListener {
            val email = binding.editTextEmail.text.toString().trim()
            val password = binding.editTextPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                isRegistering = false
                currentEmail = email
                viewModel.login(email, password)
            } else {
                Toast.makeText(requireContext(), "Email ve şifre boş olamaz!", Toast.LENGTH_SHORT).show()
            }
        }

        binding.avatar1.setOnClickListener {
            selectedAvatar = R.drawable.avatar_male
            Toast.makeText(requireContext(), "Avatar 1 seçildi", Toast.LENGTH_SHORT).show()
        }

        binding.avatar2.setOnClickListener {
            selectedAvatar = R.drawable.avatar_female
            Toast.makeText(requireContext(), "Avatar 2 seçildi", Toast.LENGTH_SHORT).show()
        }

        binding.avatar3.setOnClickListener {
            selectedAvatar = R.drawable.avatar_female2
            Toast.makeText(requireContext(), "Avatar 3 seçildi", Toast.LENGTH_SHORT).show()
        }

        binding.avatar4.setOnClickListener {
            selectedAvatar = R.drawable.avatar_male2
            Toast.makeText(requireContext(), "Avatar 4 seçildi", Toast.LENGTH_SHORT).show()
        }

        binding.buttonContinue.setOnClickListener {
            val nickname = binding.editTextNickname.text.toString().trim()
            if (nickname.isNotEmpty()) {
                val emailKey = currentEmail.replace(".", "_")
                val sharedPref = requireContext().getSharedPreferences("UserProfile_$emailKey", Context.MODE_PRIVATE)
                sharedPref.edit().apply {
                    putString("nickname", nickname)
                    putInt("avatar", selectedAvatar)
                    apply()
                }
                findNavController().navigate(R.id.action_entryFragment_to_listFragment)
            } else {
                Toast.makeText(requireContext(), "Lütfen bir nickname giriniz", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.authResult.observe(viewLifecycleOwner) { success ->
            success?.let {
                if (it) {
                    Toast.makeText(requireContext(), "Başarılı giriş/kayıt", Toast.LENGTH_SHORT).show()

                    val emailKey = currentEmail.replace(".", "_")
                    val sharedPref = requireContext().getSharedPreferences("UserProfile_$emailKey", Context.MODE_PRIVATE)
                    val nickname = sharedPref.getString("nickname", null)
                    val avatar = sharedPref.getInt("avatar", -1)

                    if (isRegistering) {
                        // Yeni kullanıcı → Profil oluşturma ekranı
                        binding.layoutLogin.visibility = View.GONE
                        binding.layoutProfile.visibility = View.VISIBLE
                    } else {
                        // Daha önce kayıtlı kullanıcı
                        if (nickname != null && avatar != -1) {
                            findNavController().navigate(R.id.action_entryFragment_to_listFragment)
                        } else {
                            // Eğer nickname veya avatar eksikse profil oluşturma ekranı göster
                            binding.layoutLogin.visibility = View.GONE
                            binding.layoutProfile.visibility = View.VISIBLE
                        }
                    }
                }
                viewModel.resetAuthResult()
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMsg ->
            errorMsg?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel.resetError()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
