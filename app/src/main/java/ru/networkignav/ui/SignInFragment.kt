package ru.networkignav.ui


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import ru.networkignav.viewmodel.SignInViewModel
import ru.networkignav.R
import ru.networkignav.auth.AppAuth
import ru.networkignav.databinding.FragmentSigninBinding
import javax.inject.Inject

@AndroidEntryPoint
class SignInFragment : Fragment() {

    private val viewModel: SignInViewModel by viewModels()
    @Inject
    lateinit var appAuth: AppAuth
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val binding = FragmentSigninBinding.inflate(inflater, container, false)

        binding.signIn.setOnClickListener {
            val login = binding.login.text.toString()
            val password = binding.password.text.toString()
            if (login.isBlank() || password.isBlank()) {
                Snackbar.make(
                    binding.root,
                    getString(R.string.error_empty_content),
                    Snackbar.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            viewModel.signIn(login, password)

            lifecycleScope.launchWhenCreated {
                appAuth.state.collectLatest {
                    it?.let { findNavController().navigateUp() }
                }
            }
        }

        return binding.root
    }
}
