package co.getdere

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import co.getdere.Fragments.BoardFragment
import co.getdere.Fragments.FeedFragment
import co.getdere.Fragments.ProfileLoggedInUserFragment
import co.getdere.Models.Images
import co.getdere.Models.Question
import co.getdere.ViewModels.SharedViewModelCurrentUser
import co.getdere.Models.Users
import co.getdere.RegisterLogin.RegisterActivity
import co.getdere.ViewModels.SharedViewModelImage
import co.getdere.ViewModels.SharedViewModelQuestion
import co.getdere.ViewModels.SharedViewModelRandomUser
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.pusher.pushnotifications.PushNotifications
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    lateinit var mToolbar: Toolbar
    lateinit var mBottomNav: BottomNavigationView

    lateinit var sharedViewModelCurrentUser: SharedViewModelCurrentUser
    lateinit var sharedViewModelImage: SharedViewModelImage
    lateinit var sharedViewModelRandomUser: SharedViewModelRandomUser

//    val fragment1: Fragment = FeedFragment()
//    val fragment2: Fragment = BoardFragment()
//    val fragment3: Fragment = ProfileLoggedInUserFragment()
//    val fm = supportFragmentManager
//    var active : Fragment = fragment1

    lateinit var navHostFragment: Fragment


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//
//        fm.beginTransaction().add(R.id.nav_host_fragment, fragment3, "3").hide(fragment3).commit()
//        fm.beginTransaction().add(R.id.nav_host_fragment, fragment2, "2").hide(fragment2).commit()
//        fm.beginTransaction().add(R.id.nav_host_fragment, fragment1, "1").commit()

//        PushNotifications.start(getApplicationContext(), "8286cd5e-eaaa-4d81-a57b-0f4d985b0a47");
//        PushNotifications.subscribe("hello");

        sharedViewModelCurrentUser = ViewModelProviders.of(this).get(SharedViewModelCurrentUser::class.java)

        sharedViewModelImage = ViewModelProviders.of(this).get(SharedViewModelImage::class.java)
        sharedViewModelImage.sharedImageObject.postValue(Images())

        sharedViewModelRandomUser = ViewModelProviders.of(this).get(SharedViewModelRandomUser::class.java)
        sharedViewModelRandomUser.randomUserObject.postValue(Users())

        FirebaseApp.initializeApp(this)
        checkIfLoggedIn()
        setupActionBar()

        navHostFragment = nav_host_fragment
        mBottomNav = findViewById(R.id.bottom_nav)


//        mBottomNav.setOnNavigationItemSelectedListener { item ->
//
//            when (item.itemId) {
//
//                R.id.destination_feed -> {
//                    println(active)
//                    fm.beginTransaction().hide(active).show(fragment1).commit()
//                    active = fragment1
//                    println(active)
//                    return@setOnNavigationItemSelectedListener true
//                }
//
//                R.id.destination_board -> {
//                    println(active)
//                    fm.beginTransaction().hide(active).show(fragment2).commit()
//                    active = fragment2
//                    println(active)
//
//                    return@setOnNavigationItemSelectedListener true
//
//                }
//
//                R.id.destination_profile_logged_in_user -> {
//                    println(active)
//                    fm.beginTransaction().hide(active).show(fragment3).commit()
//                    active = fragment3
//                    println(active)
//
//                    return@setOnNavigationItemSelectedListener true
//
//                }
//
//                else -> {
//                    fm.beginTransaction().hide(active).show(fragment1).commit();
//                    active = fragment1
//                }
//
//            }
//
//            return@setOnNavigationItemSelectedListener false
//
//        }


    }

    private fun setupBottomNavMenu(navController: NavController) {


        bottom_nav.let {
            NavigationUI.setupWithNavController(it, navController)
        }
    }


    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val navController = Navigation.findNavController(this, R.id.nav_host_fragment)
        val navigated = NavigationUI.onNavDestinationSelected(item!!, navController)
        return navigated || super.onOptionsItemSelected(item)
    }

    private fun checkIfLoggedIn() {

        val uid = FirebaseAuth.getInstance().uid
        if (uid == null) {
            val intent = Intent(this, RegisterActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        } else {
            fetchCurrentUser()
        }

    }

    private fun fetchCurrentUser() {

        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid/profile")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {

                sharedViewModelCurrentUser.currentUserObject = p0.getValue(Users::class.java)!!
                Log.d("checkLocation", "fetchCurrentUser")

                setupNavController()
            }

        })
    }

    private fun setupNavController() {
        Log.d("checkLocation", "setupNavController")


        val myNavHostFragment: NavHostFragment = navHostFragment as NavHostFragment
        val inflater = myNavHostFragment.navController.navInflater
        val graph = inflater.inflate(R.navigation.feed_nav_graph)
        myNavHostFragment.navController.graph = graph


        val navController = Navigation.findNavController(this, R.id.nav_host_fragment)
        setupBottomNavMenu(navController)
        this.findNavController(R.id.nav_host_fragment)
    }

    private fun setupActionBar() {

        mToolbar = findViewById(R.id.my_toolbar)
        setSupportActionBar(mToolbar)
    }

    companion object {
        fun newInstance(): MainActivity = MainActivity()
    }
}

