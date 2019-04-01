package co.getdere

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import co.getdere.Models.Images
import co.getdere.Models.Users
import co.getdere.RegisterLogin.RegisterActivity
import co.getdere.ViewModels.SharedViewModelCurrentUser
import co.getdere.ViewModels.SharedViewModelImage
import co.getdere.ViewModels.SharedViewModelRandomUser
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_board.*
import kotlinx.android.synthetic.main.activity_feed.*


class BoardActivity : AppCompatActivity() {

    lateinit var mToolbar: Toolbar
    lateinit var mBottomNav: BottomNavigationView

    //    lateinit var sharedViewModelCurrentUser: SharedViewModelCurrentUser
//    lateinit var sharedViewModelImage: SharedViewModelImage
    lateinit var sharedViewModelRandomUser: SharedViewModelRandomUser

    lateinit var navHostFragment: Fragment


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_board)

        val sharedViewModelCurrentUser = ViewModelProviders.of(this).get(SharedViewModelCurrentUser::class.java)


        val uid = FirebaseAuth.getInstance().uid
        val userRef = FirebaseDatabase.getInstance().getReference("/users/$uid/profile")

        userRef.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                sharedViewModelCurrentUser.currentUserObject = p0.getValue(Users::class.java)!!
            }


        })



//        sharedViewModelImage = ViewModelProviders.of(this).get(SharedViewModelImage::class.java)
//        sharedViewModelImage.sharedImageObject.postValue(Images())
//
        sharedViewModelRandomUser = ViewModelProviders.of(this).get(SharedViewModelRandomUser::class.java)
        sharedViewModelRandomUser.randomUserObject.postValue(Users())

        navHostFragment = board_nav_host_fragment


        FirebaseApp.initializeApp(this)
        setupActionBar()
        setupNavController()


        mBottomNav = findViewById(co.getdere.R.id.board_bottom_nav)
        mBottomNav.selectedItemId = R.id.destination_board

        mBottomNav.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {

                co.getdere.R.id.destination_feed -> {
                    val intent = Intent(this, FeedActivity::class.java)
                    startActivity(intent)
                }
                R.id.destination_profile_logged_in_user -> {
                    val intent = Intent(this, ProfileActivity::class.java)
                    startActivity(intent)
                }
            }
            false
        }


    }


    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val navController = Navigation.findNavController(this, R.id.board_nav_host_fragment)
        val navigated = NavigationUI.onNavDestinationSelected(item!!, navController)
        return navigated || super.onOptionsItemSelected(item)
    }


    private fun setupNavController() {
        Log.d("checkLocation", "setupNavController")


        val myNavHostFragment: NavHostFragment = navHostFragment as NavHostFragment
        val inflater = myNavHostFragment.navController.navInflater
        val graph = inflater.inflate(R.navigation.board_nav_graph)
        myNavHostFragment.navController.graph = graph


        val navController = Navigation.findNavController(this, R.id.board_nav_host_fragment)
//        setupBottomNavMenu(navController)
        this.findNavController(R.id.board_nav_host_fragment)
    }

    private fun setupActionBar() {

        mToolbar = findViewById(R.id.my_toolbar)
        setSupportActionBar(mToolbar)
    }

    companion object {
        fun newInstance(): BoardActivity = BoardActivity()
    }
}

