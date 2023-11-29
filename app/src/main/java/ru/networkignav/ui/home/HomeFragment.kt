package ru.networkignav.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.networkignav.adapter.PostLoadingStateAdapter
import ru.networkignav.util.MyDialog
import ru.networkignav.viewmodel.AuthViewModel
import ru.networkignav.viewmodel.PostViewModel
import ru.networkignav.R
import ru.networkignav.adapter.OnInteractionListener
import ru.networkignav.adapter.PostsAdapter
import ru.networkignav.auth.AppAuth
import ru.networkignav.databinding.FragmentHomeBinding
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment() {

    @OptIn(ExperimentalCoroutinesApi::class)
    private val viewModel: PostViewModel by activityViewModels()
    private val authViewModel: AuthViewModel by viewModels()

    @Inject
    lateinit var appUser: AppAuth

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {

        val binding = FragmentHomeBinding.inflate(inflater, container, false)
        var currentAuthMenuProvider: MenuProvider? = null
        val dialog = MyDialog()

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
                            //AppAuth.getInstance().setAuth(5, "x-token")
                            true
                        }

                        R.id.signUp -> {
                            findNavController().navigate(R.id.action_navigation_home_to_signUpFragment)
                            //AppAuth.getInstance().setAuth(5, "x-token")
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


        val adapter = PostsAdapter(object : OnInteractionListener{

        })
        binding.newsFeedRecyclerView.adapter = adapter.withLoadStateHeaderAndFooter(
            header = PostLoadingStateAdapter {

                adapter.retry()
            },
            footer = PostLoadingStateAdapter {
                adapter.retry()
            }
        )

        //lifecycleScope.launch {
        //    repeatOnLifecycle(Lifecycle.State.CREATED) {
        //        adapter.loadStateFlow.collectLatest {
        //            binding.swiperefresh.isRefreshing =
        //                it.refresh is LoadState.Loading
        //        }
        //    }
        //}
        //binding.swiperefresh.setOnRefreshListener {
        //    adapter.refresh()
        //}

        viewModel.state.observe(viewLifecycleOwner) { state ->
            //nding.progress.isVisible = state.loading
            //nding.errorGroup.isVisible = state.error
            //nding.swiperefresh.isRefreshing = false

            // (state.error) {
            //  val message = if (state.response.code == 0) {
            //      getString(R.string.error_loading)
            //  } else {
            //      getString(
            //          R.string.error_response,
            //          state.response.message.toString(),
            //          state.response.code
            //      )
            //  }
            //  Snackbar.make(
            //      binding.root,
            //      message,
            //      Snackbar.LENGTH_LONG
            //  ).setAction(android.R.string.ok) {
            //      return@setAction
            //  }.show()
            //}
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.data.collectLatest {
                    adapter.submitData(it)
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                //viewModel.newerCount.collectLatest {
                //    if (it == 0) {
                //        binding.fabNewer.hide()
                //    } else {
                //        binding.fabNewer.text = getString(R.string.newer, it)
                //        binding.fabNewer.show()
                //    }
                //}
            }
        }

        authViewModel.data.observe(viewLifecycleOwner) {
            adapter.refresh()
        }


        //binding.retryButton.setOnClickListener {
        //    adapter.refresh()
        //}

        //binding.fab.setOnClickListener {
        //    if (appAuth.isUserValid()) {
        //        findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
        //    } else {
        //        dialog.show(parentFragmentManager.beginTransaction(), "dialog")
        //        findNavController().navigate(R.id.action_feedFragment_to_signInFragment)
        //    }
        //}

        //binding.fabNewer.setOnClickListener {
        //    viewModel.loadNewPosts()
        //    binding.fabNewer.hide()
        //}

        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                if (positionStart == 0) {
                    binding.newsFeedRecyclerView.smoothScrollToPosition(0)
                }
            }
        })

        return binding.root
    }
}