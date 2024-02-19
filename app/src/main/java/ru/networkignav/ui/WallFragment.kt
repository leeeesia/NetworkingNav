package ru.networkignav.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.PagingData
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.networkignav.adapter.JobsAdapter
import ru.networkignav.adapter.OnInteractionListener
import ru.networkignav.adapter.PostLoadingStateAdapter
import ru.networkignav.adapter.PostsAdapter
import ru.networkignav.databinding.FragmentWallBinding
import ru.networkignav.dto.Post
import ru.networkignav.ui.profile.ProfileViewModel


@ExperimentalCoroutinesApi
class WallFragment: Fragment() {
    private val profileViewModel: ProfileViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val userId: String? = arguments?.getString("userId")
        val binding = FragmentWallBinding.inflate(inflater, container, false)
        profileViewModel.getUser(userId ?: "")

        val postAdapter = PostsAdapter(requireContext(), object : OnInteractionListener {
            override fun onEdit(post: Post) {}
            override fun onRemove(post: Post) {}
        })

        val jobAdapter = JobsAdapter(object : OnInteractionListener {
            override fun onEdit(post: Post) {}
            override fun onRemove(post: Post) {}
        }, false)
        binding.newsFeedRecyclerView.adapter = postAdapter.withLoadStateHeaderAndFooter(
            header = PostLoadingStateAdapter {

                postAdapter.retry()
            },
            footer = PostLoadingStateAdapter {
                postAdapter.retry()
            }
        )

        binding.jobRecyclerView.adapter = jobAdapter


        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                profileViewModel.dataUser.collectLatest {
                    val pagingData = PagingData.from(it)
                    postAdapter.submitData(pagingData)
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                profileViewModel.dataJob.collectLatest {
                    val pagingData = PagingData.from(it)
                    jobAdapter.submitData(pagingData)
                }
            }
        }

        binding.apply {
            toggleJobsButton.setOnClickListener {
                jobRecyclerView.visibility = if (jobRecyclerView.visibility == View.VISIBLE) {
                    View.GONE
                } else {
                    View.VISIBLE
                }
            }

            profileViewModel.wuser.observe(viewLifecycleOwner) { userProfile ->
                profileTitle.text = profileViewModel.wuser.value?.name
                val url = profileViewModel.wuser.value?.avatar
                    ?: "https://ob-kassa.ru/content/front/buhoskol_tmp1/images/reviews-icon.jpg"
                Glide.with(profileAvatar)
                    .load(url)
                    .circleCrop()
                    .timeout(10_000)
                    .into(profileAvatar)
            }
        }

        postAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                if (positionStart == 0) {
                    binding.newsFeedRecyclerView.smoothScrollToPosition(0)
                }
            }
        })

        jobAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                if (positionStart == 0) {
                    binding.newsFeedRecyclerView.smoothScrollToPosition(0)
                }
            }
        })

        return binding.root
    }
}