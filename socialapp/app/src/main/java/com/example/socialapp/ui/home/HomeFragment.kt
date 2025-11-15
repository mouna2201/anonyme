package com.example.socialapp.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.socialapp.databinding.FragmentHomeBinding
import com.example.socialapp.data.models.Post
import com.example.socialapp.data.repository.SocialRepository
import com.example.socialapp.ui.post.CreatePostActivity
import com.example.socialapp.ui.post.PostDetailActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val repository = SocialRepository()
    private var postsAdapter: PostsAdapter? = null
    private var currentUserId: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupListeners()
        loadUserProfile()
    }

    private fun setupRecyclerView() {
        // Initialiser avec un adapter vide en attendant le currentUserId
        postsAdapter = PostsAdapter(
            currentUserId = currentUserId,
            onLikeClick = { post -> likePost(post) },
            onCommentClick = { post -> openPostDetail(post) },
            onMoreClick = { post -> showMoreOptions(post) }
        )

        binding.rvPosts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = postsAdapter
        }
    }

    private fun setupListeners() {
        binding.swipeRefresh.setOnRefreshListener {
            loadPosts()
        }

        binding.fabCreatePost.setOnClickListener {
            startActivity(Intent(requireContext(), CreatePostActivity::class.java))
        }
    }

    private fun loadUserProfile() {
        if (!isAdded || _binding == null) return
        
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            val result = repository.getUserProfile()
            
            if (!isAdded || _binding == null) return@launch
            
            result.onSuccess { user ->
                if (!isAdded || _binding == null) return@onSuccess
                
                currentUserId = user._id
                postsAdapter = PostsAdapter(
                    currentUserId = currentUserId,
                    onLikeClick = { post -> likePost(post) },
                    onCommentClick = { post -> openPostDetail(post) },
                    onMoreClick = { post -> showMoreOptions(post) }
                )
                binding.rvPosts.adapter = postsAdapter
                loadPosts()
            }.onFailure { error ->
                if (isAdded && _binding != null) {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(
                        requireContext(),
                        "Erreur: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun loadPosts() {
        if (!isAdded || _binding == null) return
        
        lifecycleScope.launch {
            val result = repository.getPosts()

            if (!isAdded || _binding == null) return@launch
            
            binding.swipeRefresh.isRefreshing = false
            binding.progressBar.visibility = View.GONE

            result.onSuccess { response ->
                if (!isAdded || _binding == null) return@onSuccess
                
                if (response.posts.isEmpty()) {
                    binding.tvEmptyState.visibility = View.VISIBLE
                    binding.rvPosts.visibility = View.GONE
                } else {
                    binding.tvEmptyState.visibility = View.GONE
                    binding.rvPosts.visibility = View.VISIBLE
                    postsAdapter?.submitList(response.posts)
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

    private fun likePost(post: Post) {
        if (!isAdded || _binding == null) return
        
        lifecycleScope.launch {
            val result = repository.likePost(post._id)

            if (!isAdded || _binding == null) return@launch

            result.onSuccess { response ->
                // Mettre à jour la liste
                postsAdapter?.let { adapter ->
                    val currentList = adapter.currentList.toMutableList()
                    val index = currentList.indexOfFirst { it._id == post._id }
                    if (index != -1) {
                        currentList[index] = response.post
                        adapter.submitList(currentList)
                    }
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

    private fun openPostDetail(post: Post) {
        if (!isAdded) return
        val intent = Intent(requireContext(), PostDetailActivity::class.java)
        intent.putExtra("POST_ID", post._id)
        startActivity(intent)
    }

    private fun showMoreOptions(post: Post) {
        if (!isAdded) return
        
        val options = if (post.userId?._id == currentUserId) {
            arrayOf("Supprimer")
        } else {
            arrayOf("Signaler")
        }

        MaterialAlertDialogBuilder(requireContext())
            .setItems(options) { _, which ->
                if (!isAdded) return@setItems
                when (which) {
                    0 -> {
                        if (post.userId?._id == currentUserId) {
                            deletePost(post)
                        } else {
                            // Logique de signalement
                            Toast.makeText(
                                requireContext(),
                                "Fonctionnalité à venir",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
            .show()
    }

    private fun deletePost(post: Post) {
        if (!isAdded || _binding == null) return
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Supprimer le post")
            .setMessage("Voulez-vous vraiment supprimer ce post ?")
            .setPositiveButton("Supprimer") { _, _ ->
                if (!isAdded || _binding == null) return@setPositiveButton
                
                lifecycleScope.launch {
                    val result = repository.deletePost(post._id)

                    if (!isAdded || _binding == null) return@launch

                    result.onSuccess {
                        if (isAdded) {
                            Toast.makeText(
                                requireContext(),
                                "Post supprimé",
                                Toast.LENGTH_SHORT
                            ).show()
                            loadPosts()
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
            .setNegativeButton("Annuler", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        if (currentUserId.isNotEmpty()) {
            loadPosts()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}