package ru.networkignav

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.networkignav.auth.AppAuth
import ru.networkignav.databinding.ActivityMainBinding
import ru.networkignav.ui.profile.ProfileViewModel
import ru.networkignav.util.DataType
import ru.networkignav.util.MyDialog
import ru.networkignav.viewmodel.AuthViewModel
import ru.networkignav.viewmodel.PostViewModel
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val viewModel: AuthViewModel by viewModels()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val postViewModel: PostViewModel by viewModels()
    private val profileViewModel: ProfileViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding

    @Inject
    lateinit var appAuth: AppAuth

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dialog = MyDialog()
        viewModel.data.observe(this) {
            invalidateOptionsMenu()
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        val appBarConfiguration = AppBarConfiguration(
            setOf(R.id.navigation_home,  R.id.navigation_profile)
        )


        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {

                R.id.navigation_home -> {
                    binding.tabLayout.visibility = View.VISIBLE
                }

                else -> {
                    binding.tabLayout.visibility = View.GONE
                }
            }

        }

        navView.setupWithNavController(navController)


        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val position = tab?.position ?: return

                when (position) {
                    0 -> {
                        postViewModel.setDataType(DataType.POSTS)
                        profileViewModel.setDataType(DataType.POSTS)
                    }

                    1 ->{
                        postViewModel.setDataType(DataType.EVENTS)
                        profileViewModel.setDataType(DataType.EVENTS)
                    }
                    else -> throw IllegalArgumentException("Unsupported tab position")
                }

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                // Не используется
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                // Не используется
            }
        })


    }


}