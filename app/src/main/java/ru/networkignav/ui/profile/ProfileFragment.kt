package ru.networkignav.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.paging.PagingData
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.networkignav.R
import ru.networkignav.adapter.JobsAdapter
import ru.networkignav.adapter.OnInteractionListener
import ru.networkignav.adapter.PostLoadingStateAdapter
import ru.networkignav.adapter.PostsAdapter
import ru.networkignav.auth.AppAuth
import ru.networkignav.databinding.FragmentProfileBinding
import ru.networkignav.dto.Job
import ru.networkignav.dto.Post
import ru.networkignav.ui.AddJobFragment.Companion.finishArg
import ru.networkignav.ui.AddJobFragment.Companion.nameArg
import ru.networkignav.ui.AddJobFragment.Companion.positionArg
import ru.networkignav.ui.AddJobFragment.Companion.startArg
import ru.networkignav.ui.NewPostFragment.Companion.textArg
import ru.networkignav.util.AuthDialog
import ru.networkignav.viewmodel.AuthViewModel
import javax.inject.Inject

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    @OptIn(ExperimentalCoroutinesApi::class)
    private val viewModel: ProfileViewModel by activityViewModels()
    private val authViewModel: AuthViewModel by viewModels()

    @Inject
    lateinit var appUser: AppAuth

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {

        val binding = FragmentProfileBinding.inflate(inflater, container, false)
        var currentAuthMenuProvider: MenuProvider? = null
        val dialog = AuthDialog()

        authViewModel.data.observe(viewLifecycleOwner) { authModel ->

            currentAuthMenuProvider?.let(requireActivity()::removeMenuProvider)

            requireActivity().addMenuProvider(object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.menu_auth, menu)
                    menu.setGroupVisible(R.id.authorized, authViewModel.isAutificated)
                    menu.setGroupVisible(R.id.unauthorized, !authViewModel.isAutificated)
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    return when (menuItem.itemId) {
                        R.id.signIn -> {
                            findNavController().navigate(R.id.action_navigation_home_to_signInFragment)
                            true
                        }

                        R.id.signUp -> {
                            findNavController().navigate(R.id.action_navigation_home_to_signUpFragment)
                            true
                        }

                        R.id.logout -> {
                            dialog.show(parentFragmentManager.beginTransaction(), "logout")
                            true
                        }

                        else -> false
                    }
                }

            }.also { currentAuthMenuProvider = it }, viewLifecycleOwner)
        }

        val adapter = PostsAdapter(requireContext(), object : OnInteractionListener {
            override fun onEdit(post: Post) {
                findNavController().navigate(
                    R.id.action_navigation_profile_to_newPostFragment,
                    Bundle().apply {
                        textArg = post.content
                    })
                viewModel.edit(post)

            }

            override fun onRemove(post: Post) {
                viewModel.removeById(post.id)
            }
        })

        val jobAdapter = JobsAdapter(object : OnInteractionListener {

            override fun onEditJob(job: Job) {
                findNavController().navigate(
                    R.id.action_navigation_profile_to_addJobFragment,
                    Bundle().apply {
                        nameArg = job.name
                        positionArg = job.position
                        startArg = job.start
                        finishArg = job.finish
                    })
                viewModel.editJob(job)
            }

            override fun onRemoveJob(job: Job) {
                viewModel.removeJobById(job)
                update()
            }

        }, true)

        binding.apply {
            viewModel.user.observe(viewLifecycleOwner) {
                profileTitle.text = viewModel.user.value?.name
                val url = viewModel.user.value?.avatar
                    ?: "https://ob-kassa.ru/content/front/buhoskol_tmp1/images/reviews-icon.jpg"
                Glide.with(profileAvatar)
                    .load(url)
                    .circleCrop()
                    .timeout(10_000)
                    .into(profileAvatar)
            }

        }
        binding.newsFeedRecyclerView.adapter = adapter.withLoadStateHeaderAndFooter(
            header = PostLoadingStateAdapter {

                adapter.retry()
            },
            footer = PostLoadingStateAdapter {
                adapter.retry()
            }
        )

        binding.jobRecyclerView.adapter = jobAdapter

        if (authViewModel.isAutificated) with(binding) {
            newsFeedRecyclerView.visibility = View.VISIBLE
            toggleJobsButton.setOnClickListener {
                jobRecyclerView.visibility = if (jobRecyclerView.visibility == View.VISIBLE) {
                    View.GONE
                } else {
                    View.VISIBLE
                }
            }
            fab.setOnClickListener {
                showPopupMenu(it)
            }

        } else {
            binding.newsFeedRecyclerView.visibility = View.GONE
        }

        if (!appUser.isUserValid()) {
            findNavController().navigate(R.id.action_navigation_profile_to_signInFragment)
        }


        viewModel.state.observe(viewLifecycleOwner) { state ->

            if (state.error) {
                val message = if (state.response.code == 0) {
                    getString(R.string.error_loading)
                } else {
                    getString(
                        R.string.error_response,
                        state.response.message.toString(),
                        state.response.code
                    )
                }
                Snackbar.make(
                    binding.root,
                    message,
                    Snackbar.LENGTH_LONG
                ).setAction(android.R.string.ok) {
                    return@setAction
                }.show()
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                if (appUser.isUserValid()) {
                    viewModel.data.collectLatest {
                        val pagingData = PagingData.from(it)
                        adapter.submitData(pagingData)
                    }
                }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                if (appUser.isUserValid()) {
                    viewModel.job.collectLatest {
                        val pagingData = PagingData.from(it)
                        jobAdapter.submitData(pagingData)

                    }
                }

            }

        }

        authViewModel.data.observe(viewLifecycleOwner) {
            adapter.refresh()
        }


        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
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

    private fun showPopupMenu(view: View) {
        val popupMenu = PopupMenu(context, view)
        popupMenu.menuInflater.inflate(R.menu.add_menu, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.create_post -> {
                    findNavController().navigate(R.id.action_navigation_profile_to_newPostFragment)
                    true
                }

                R.id.create_event -> {
                    findNavController().navigate(R.id.action_navigation_profile_to_newEventFragment)
                    true
                }

                R.id.create_job -> {
                    findNavController().navigate(R.id.action_navigation_profile_to_addJobFragment)
                    true
                }

                else -> false
            }
        }
        popupMenu.show()
    }
    private fun update() {
        findNavController().navigate(R.id.navigation_profile)
    }
}