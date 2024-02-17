package ru.networkignav.ui.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.networkignav.adapter.PostLoadingStateAdapter
import ru.networkignav.util.MyDialog
import ru.networkignav.viewmodel.AuthViewModel
import ru.networkignav.viewmodel.PostViewModel
import ru.networkignav.R
import ru.networkignav.adapter.EventsAdapter
import ru.networkignav.adapter.OnInteractionListener
import ru.networkignav.adapter.PostsAdapter
import ru.networkignav.auth.AppAuth
import ru.networkignav.databinding.FragmentHomeBinding
import ru.networkignav.dto.Event
import ru.networkignav.dto.Post
import ru.networkignav.ui.NewPostFragment.Companion.textArg
import ru.networkignav.util.DataType
import ru.networkignav.viewmodel.EventViewModel
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment(), OnInteractionListener {

    @OptIn(ExperimentalCoroutinesApi::class)
    private val viewModel: PostViewModel by activityViewModels()
    @OptIn(ExperimentalCoroutinesApi::class)
    private val eventViewModel: EventViewModel by activityViewModels()
    private val authViewModel: AuthViewModel by viewModels()


    @Inject
    lateinit var appUser: AppAuth


    override fun onEdit(post: Post) {
        findNavController().navigate(
            R.id.action_navigation_home_to_newPostFragment,
            Bundle().apply {
                textArg = post.content
            })
        viewModel.edit(post)
    }

    override fun onViewImage(post: Post) {

    }

    override fun onLike(post: Post) {

    }

    override fun onRemove(post: Post) {
        viewModel.removeById(post.id)
    }


    override fun onShare(post: Post) {

    }
    override fun onEditEvent(event: Event) {
        findNavController().navigate(
            R.id.action_navigation_home_to_newEventFragment,
            Bundle().apply {
                textArg = event.content
            })
        eventViewModel.edit(event)
    }

    override fun onRemoveEvent(event: Event) {
        eventViewModel.removeById(event.id)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onAuthorClick(post: Post) {
        viewLifecycleOwner.lifecycleScope.launch {
            val bundle = Bundle()

            bundle.putString("userId", post.authorId.toString())
            viewModel.updateUserId(post.authorId.toString())
            viewModel.loadWallByUserId(post.authorId.toString())

            findNavController().navigate(R.id.action_navigation_home_to_wallFragment, bundle)
        }
    }

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
        viewModel.setDataType(DataType.POSTS)
        var adapter = when (viewModel.dataType.value) {
            DataType.POSTS -> PostsAdapter(requireContext(), this)
            DataType.EVENTS -> EventsAdapter(requireContext(), this)
            else -> throw IllegalArgumentException("Unsupported data type ${viewModel.dataType.value}")
        }

        binding.newsFeedRecyclerView.adapter = adapter.withLoadStateHeaderAndFooter(
            header = PostLoadingStateAdapter {
                adapter.retry()
            },
            footer = PostLoadingStateAdapter {
                adapter.retry()
            }
        )
        viewModel.dataType.observe(viewLifecycleOwner) { dataType ->
            Log.d("MYLOG", "Setting data type: $dataType")
            when (dataType) {
                DataType.POSTS -> Log.d("MYLOG", "Setting data type 1 : $dataType ")
                DataType.EVENTS -> Log.d("MYLOG", "Setting data type 2: $dataType ")
                else -> Log.d("MYLOG", "NO $dataType")
            }
            adapter = when (dataType) {
                DataType.POSTS -> PostsAdapter(requireContext(), this)
                DataType.EVENTS -> EventsAdapter(requireContext(), this)
                else -> throw IllegalArgumentException("Unsupported data type")
            }

            binding.newsFeedRecyclerView.adapter = adapter
            adapter.refresh()

            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.CREATED) {
                    when (viewModel.dataType.value) {
                        DataType.POSTS -> viewModel.data.collectLatest {
                            adapter.submitData(it)
                        }

                        DataType.EVENTS -> eventViewModel.data.collectLatest {
                            adapter.submitData(it)
                        }

                        else -> throw IllegalArgumentException("Unsupported data type ${viewModel.dataType.value}")
                    }
                }
            }

        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                adapter.loadStateFlow.collectLatest {
                    binding.swiperefresh.isRefreshing =
                        it.refresh is LoadState.Loading
                }
            }
        }
        binding.swiperefresh.setOnRefreshListener {
            adapter.refresh()
        }



        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                when (viewModel.dataType.value) {
                    DataType.POSTS -> viewModel.data.collectLatest {
                        adapter.submitData(it)
                    }

                    DataType.EVENTS -> eventViewModel.data.collectLatest {
                        adapter.submitData(it)
                    }

                    else -> throw IllegalArgumentException("Unsupported data type ${viewModel.dataType.value}")
                }
            }
        }



        authViewModel.data.observe(viewLifecycleOwner) {

            adapter.refresh()
        }






        binding.fabNewer.setOnClickListener {
            viewModel.loadNewPosts()
            binding.fabNewer.hide()
        }

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