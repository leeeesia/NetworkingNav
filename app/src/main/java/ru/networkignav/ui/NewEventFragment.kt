package ru.networkignav.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.networkignav.R
import ru.networkignav.auth.AppAuth
import ru.networkignav.databinding.FragmentNewEventBinding
import ru.networkignav.util.AndroidUtils
import ru.networkignav.util.Formatter
import ru.networkignav.util.StringArg
import ru.networkignav.viewmodel.EventViewModel
import javax.inject.Inject

@AndroidEntryPoint
@OptIn(ExperimentalCoroutinesApi::class)
class NewEventFragment : Fragment() {

    companion object {
        var Bundle.textArg: String? by StringArg
    }

    private val viewModel: EventViewModel by activityViewModels()

    @Inject
    lateinit var appAuth: AppAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val binding = FragmentNewEventBinding.inflate(
            inflater,
            container,
            false
        )

        arguments?.textArg
            ?.let(binding.content::setText)

        binding.apply {
            date.setOnClickListener {
                context?.let { Formatter.showDatePicker(date, it) }
            }

            save.setOnClickListener {
                if (content.text.isNotBlank() && date.text.isNotBlank()) {
                    viewModel.changeContent(
                        content.text.toString(),
                        date.text.toString(),
                    )
                    viewModel.save()
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
                findNavController().navigateUp()
            }
        }
        return binding.root
    }
}