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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import ru.networkignav.R
import ru.networkignav.auth.AppAuth
import ru.networkignav.databinding.FragmentNewPostBinding
import ru.networkignav.model.PhotoModel
import ru.networkignav.util.AndroidUtils
import ru.networkignav.util.StringArg
import ru.networkignav.viewmodel.PostViewModel
import javax.inject.Inject

@AndroidEntryPoint
@OptIn(ExperimentalCoroutinesApi::class)
class NewPostFragment : Fragment() {

    companion object {
        var Bundle.textArg: String? by StringArg
    }

    private val viewModel: PostViewModel by activityViewModels()
    @Inject
    lateinit var appAuth: AppAuth
    private val photoLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ){
        if (it.resultCode == Activity.RESULT_OK){
            val uri = requireNotNull( it.data?.data)

            viewModel.setPhoto(PhotoModel(uri, uri.toFile()))
        } else{
            Toast.makeText(requireContext(), "Pick photo error", Toast.LENGTH_SHORT)
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val binding = FragmentNewPostBinding.inflate(
            inflater,
            container,
            false
        )

        arguments?.textArg
            ?.let(binding.edit::setText)

        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.new_post_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                when (menuItem.itemId) {
                    R.id.save -> {
                        viewModel.changeContent(binding.edit.text.toString())
                        viewModel.save()
                        AndroidUtils.hideKeyboard(requireView())
                        findNavController().navigateUp()
                        true
                    }
                    else -> false
                }
        }, viewLifecycleOwner)

        //binding.takePhoto.setOnClickListener {
        //    ImagePicker.with(this)
        //        .cameraOnly()
        //        .crop()
        //        .compress(2048)
        //        .createIntent(photoLauncher::launch)
        //}
        //binding.gallery.setOnClickListener {
        //    ImagePicker.with(this)
        //        .galleryOnly()
        //        .crop()
        //        .compress(2048)
        //        .createIntent(photoLauncher::launch)
        //}
        binding.clear.setOnClickListener {
            viewModel.clearPhoto()
        }

        viewModel.photo.observe(viewLifecycleOwner){photo->
            if (photo == null){
                binding.previewContainer.isGone = true
                return@observe
            }

            binding.previewContainer.isVisible = true
            binding.preview.setImageURI(photo.uri)
        }

        viewModel.postCreated.observe(viewLifecycleOwner) {
            lifecycleScope.launchWhenCreated {
                appAuth.state.collectLatest {
                    it?.let { findNavController().navigateUp() }
                }
            }
        }
        return binding.root
    }
}