package co.getdere

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import co.getdere.Fragments.FeedFragment
import co.getdere.Fragments.ImageFullSizeFragment
import co.getdere.Fragments.ProfileRandomUserFragment
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
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.subcontents.*


class FeedActivity : AppCompatActivity() {

    lateinit var mToolbar: Toolbar
    lateinit var mBottomNav: BottomNavigationView

    lateinit var sharedViewModelCurrentUser: SharedViewModelCurrentUser
    lateinit var sharedViewModelImage: SharedViewModelImage
    lateinit var sharedViewModelRandomUser: SharedViewModelRandomUser

    lateinit var feedNavHostFragment: Fragment

    var imageFullSizeFragment = ImageFullSizeFragment()
    var feedFragment = FeedFragment()
    var randomProfileNavHostFragment = ProfileRandomUserFragment()


    lateinit var mainFrame: FrameLayout
    lateinit var subFrame: FrameLayout

    val fm = supportFragmentManager
    val subFm = supportFragmentManager

    var active: Fragment = feedFragment
    var subActive: Fragment = imageFullSizeFragment




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feed)

        FirebaseApp.initializeApp(this)
        checkIfLoggedIn()

        sharedViewModelCurrentUser = ViewModelProviders.of(this).get(SharedViewModelCurrentUser::class.java)

        sharedViewModelImage = ViewModelProviders.of(this).get(SharedViewModelImage::class.java)
        sharedViewModelImage.sharedImageObject.postValue(Images())

        sharedViewModelRandomUser = ViewModelProviders.of(this).get(SharedViewModelRandomUser::class.java)
        sharedViewModelRandomUser.randomUserObject.postValue(Users())

        mainFrame = feed_frame_container
        subFrame = feed_subcontents_frame_container

        mToolbar = findViewById(R.id.my_toolbar)
        setSupportActionBar(mToolbar)

        mBottomNav = findViewById(co.getdere.R.id.feed_bottom_nav)

        mBottomNav.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {

                co.getdere.R.id.destination_board -> {
//
//                    fm.beginTransaction().hide(active).show(boardNavHostFragment).commit()
//                    active = boardNavHostFragment

                    val intent = Intent(this, BoardActivity::class.java)
                    startActivity(intent)
                }
                R.id.destination_profile_logged_in_user -> {

//                    fm.beginTransaction().hide(active).show(profileNavHostFragment).commit()
//                    active = profileNavHostFragment

                    val intent = Intent(this, ProfileActivity::class.java)
                    startActivity(intent)
                }

                R.id.destination_feed -> {

//                    fm.beginTransaction().hide(active).show(feedNavHostFragment).commit()
//                    active = feedNavHostFragment

//                    val intent = Intent(this, ProfileActivity::class.java)
//                    startActivity(intent)
                }
            }
            false
        }

//
//        fm.beginTransaction().add(R.id.feed_frame_container, imageFullSizeFragment, "imageFullSizeFragment").hide(imageFullSizeFragment).commit()
//        fm.beginTransaction().add(R.id.feed_frame_container, randomProfileNavHostFragment, "randomProfileNavHostFragment").hide(randomProfileNavHostFragment).commit()
        fm.beginTransaction().add(R.id.feed_frame_container, feedFragment, "feedFragment").commit()

        subFm.beginTransaction()
            .add(R.id.feed_subcontents_frame_container, imageFullSizeFragment, "imageFullSizeFragment").commit()


    }


    fun switchVisibility(case: Int) {

        if (case == 0) {
            mainFrame.visibility = View.VISIBLE
            subFrame.visibility = View.GONE
        } else {
            mainFrame.visibility = View.GONE
            subFrame.visibility = View.VISIBLE
        }


    }



    override fun onBackPressed() {
//        super.onBackPressed()

        if (mainFrame.visibility == View.GONE){

            switchVisibility(0)

        }

//        when (active){
//
//            imageFullSizeFragment -> {
//                fm.beginTransaction().hide(active).show(feedFragment).commit()
//                active = feedFragment
//            } else -> {}
//
//        }


    }


//
//    private fun setupBottomNavMenu(navController: NavController) {
//
//
//        bottom_nav.let {
//            NavigationUI.setupWithNavController(it, navController)
//        }
//    }


//    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
//
//
//
////        val navController = Navigation.findNavController(this, R.id.feed_nav_host_fragment)
////        val navigated = NavigationUI.onNavDestinationSelected(item!!, navController)
////        return navigated || super.onOptionsItemSelected(item)
////
//
//
//    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(co.getdere.R.menu.feed_navigation, menu)
        return super.onCreateOptionsMenu(menu)
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

//                setupNavController()
            }

        })
    }

//    private fun setupNavController() {
//        Log.d("checkLocation", "setupNavController")
//
//
//        val myNavHostFragment: NavHostFragment = feedNavHostFragment as NavHostFragment
//        val inflater = myNavHostFragment.navController.navInflater
//        val graph = inflater.inflate(R.navigation.feed_nav_graph)
//        myNavHostFragment.navController.graph = graph
//
//
//        val navController = Navigation.findNavController(this, R.id.feed_nav_host_fragment)
////        setupBottomNavMenu(navController)
//        this.findNavController(R.id.feed_nav_host_fragment)
//    }


    companion object {
        fun newInstance(): FeedActivity = FeedActivity()
    }
}

