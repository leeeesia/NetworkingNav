package ru.networkignav

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import ru.networkignav.auth.AppAuth
import ru.networkignav.databinding.ActivityMainBinding
import ru.networkignav.viewmodel.AuthViewModel
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity(R.layout.activity_main) {
    private val viewModel: AuthViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding
    @Inject
    lateinit var appAuth: AppAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.data.observe(this) {
            invalidateOptionsMenu()
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_profile
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }


}