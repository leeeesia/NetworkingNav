package ru.networkignav.ui

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.core.view.MenuProvider
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import ru.networkignav.R
import ru.networkignav.auth.AppAuth
import ru.networkignav.databinding.FragmentAddJobBinding
import ru.networkignav.databinding.FragmentNewPostBinding
import ru.networkignav.model.PhotoModel
import ru.networkignav.ui.NewPostFragment.Companion.textArg
import ru.networkignav.ui.dashboard.JobViewModel
import ru.networkignav.util.AndroidUtils
import ru.networkignav.util.Formatter
import ru.networkignav.util.StringArg
import ru.networkignav.viewmodel.PostViewModel
import javax.inject.Inject

@AndroidEntryPoint
@OptIn(ExperimentalCoroutinesApi::class)
class AddJobFragment : Fragment() {

    companion object {
        var Bundle.nameArg: String? by StringArg
        var Bundle.positionArg: String? by StringArg
        var Bundle.startArg: String? by StringArg
        var Bundle.finishArg: String? by StringArg
    }

    private val viewModel: JobViewModel by activityViewModels()

    @Inject
    lateinit var appAuth: AppAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val binding = FragmentAddJobBinding.inflate(
            inflater,
            container,
            false
        )

        arguments?.nameArg
            ?.let(binding.companyName::setText)

        arguments?.positionArg
            ?.let(binding.position::setText)
        arguments?.startArg
            ?.let(binding.startWork::setText)
        arguments?.finishArg
            ?.let(binding.finishWork::setText)

        binding.apply {
            startWork.setOnClickListener {
                context?.let { Formatter.showDatePicker(startWork, it) }
            }
            finishWork.setOnClickListener {
                context?.let { Formatter.showDatePicker(finishWork, it) }
            }
            save.setOnClickListener {
                if (companyName.text.isNotBlank() && position.text.isNotBlank() && startWork.text.isNotBlank() && finishWork.text.isNotBlank()) {
                    viewModel.changeContent(
                        companyName.text.toString(),
                        position.text.toString(),
                        startWork.text.toString(),
                        finishWork.text.toString(),
                        ""
                    )
                    viewModel.saveJob()
                    AndroidUtils.hideKeyboard(requireView())
                    true
                } else {
                    Snackbar.make(
                        root,
                        getString(R.string.error_empty_content),
                        Snackbar.LENGTH_LONG
                    ).show()
                    false
                }
                AndroidUtils.hideKeyboard(requireView())
                findNavController().navigate(R.id.navigation_profile)
            }
        }




        return binding.root
    }
}