package ru.networkignav.ui.dashboard

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.networkignav.R
import ru.networkignav.adapter.EventsAdapter
import ru.networkignav.adapter.JobsAdapter
import ru.networkignav.adapter.OnInteractionListener
import ru.networkignav.adapter.PostLoadingStateAdapter
import ru.networkignav.adapter.PostsAdapter
import ru.networkignav.auth.AppAuth
import ru.networkignav.databinding.FragmentHomeBinding
import ru.networkignav.databinding.FragmentJobBinding
import ru.networkignav.databinding.FragmentProfileBinding
import ru.networkignav.dto.Post
import ru.networkignav.ui.profile.ProfileViewModel
import ru.networkignav.util.DataType
import ru.networkignav.util.MyDialog
import ru.networkignav.viewmodel.AuthViewModel
import ru.networkignav.viewmodel.EventViewModel
import ru.networkignav.viewmodel.PostViewModel
import javax.inject.Inject

@AndroidEntryPoint
class JobFragment : Fragment() {
    @OptIn(ExperimentalCoroutinesApi::class)
    private val viewModel: JobViewModel by activityViewModels()
    private val authViewModel: AuthViewModel by viewModels()

    @Inject
    lateinit var appUser: AppAuth
    private var _binding: FragmentJobBinding? = null

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








        viewModel.state.observe(viewLifecycleOwner) { state ->
            binding.progress.isVisible = state.loading
            binding.swiperefresh.isRefreshing = false

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

        //lifecycleScope.launch {
        //    repeatOnLifecycle(Lifecycle.State.CREATED) {
        //        viewModel.data.collectLatest {
        //            adapter.submitData(it)
        //        }
//
        //    }
        //}



        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}