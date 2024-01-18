package ru.networkignav.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.networkignav.R
import ru.networkignav.adapter.OnInteractionListener
import ru.networkignav.adapter.PostLoadingStateAdapter
import ru.networkignav.adapter.PostsAdapter
import ru.networkignav.auth.AppAuth
import ru.networkignav.databinding.FragmentHomeBinding
import ru.networkignav.databinding.FragmentProfileBinding
import ru.networkignav.dto.Post
import ru.networkignav.util.MyDialog
import ru.networkignav.viewmodel.AuthViewModel
import ru.networkignav.viewmodel.PostViewModel
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
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentProfileBinding.inflate(inflater, container, false)
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


        val adapter = PostsAdapter(requireContext(), object : OnInteractionListener {
            override fun onEdit(post: Post) {

            }

            override fun onViewImage(post: Post) {

            }

            override fun onLike(post: Post) {

            }

            override fun onRemove(post: Post) {

            }


            override fun onShare(post: Post) {

            }
        })
        binding.newsFeedRecyclerView.adapter = adapter.withLoadStateHeaderAndFooter(
            header = PostLoadingStateAdapter {

                adapter.retry()
            },
            footer = PostLoadingStateAdapter {
                adapter.retry()
            }
        )

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