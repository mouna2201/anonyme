package com.example.socialapp.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.socialapp.R
import com.example.socialapp.databinding.FragmentProfileBinding
import com.example.socialapp.data.repository.SocialRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val repository = SocialRepository()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadProfile()
        setupListeners()
    }

    private fun setupListeners() {
        binding.btnEditProfile.setOnClickListener {
            Toast.makeText(
                requireContext(),
                "Fonctionnalité à venir",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun loadProfile() {
        if (!isAdded || _binding == null) return
        
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            val result = repository.getUserProfile()

            if (!isAdded || _binding == null) return@launch

            binding.progressBar.visibility = View.GONE

            result.onSuccess { user ->
                if (!isAdded || _binding == null) return@onSuccess
                
                binding.tvDisplayName.text = user.displayName
                binding.tvUsername.text = "@${user.username}"
                binding.tvEmail.text = user.email

                // Format date
                try {
                    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                    sdf.timeZone = TimeZone.getTimeZone("UTC")
                    val date = sdf.parse(user.createdAt)
                    val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    binding.tvMemberSince.text = if (date != null) outputFormat.format(date) else "N/A"
                } catch (e: Exception) {
                    binding.tvMemberSince.text = "N/A"
                }

                // Profile picture
                if (user.profilePicture.isNotEmpty()) {
                    Glide.with(requireContext())
                        .load(user.profilePicture)
                        .placeholder(R.drawable.ic_avatar_placeholder)
                        .circleCrop()
                        .into(binding.ivProfilePicture)
                } else {
                    binding.ivProfilePicture.setImageResource(R.drawable.ic_avatar_placeholder)
                }
            }.onFailure { error ->
                if (isAdded) {
                    Toast.makeText(
                        requireContext(),
                        "Erreur: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}