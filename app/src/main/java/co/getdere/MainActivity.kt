package co.getdere

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import co.getdere.Interfaces.SharedViewModelCurrentUser
import co.getdere.Interfaces.SharedViewModelImage
import co.getdere.Interfaces.SharedViewModelRandomUserId
import co.getdere.Models.Users
import co.getdere.RegisterLogin.RegisterActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    var currentUser: Users? = null
    lateinit var mToolbar : Toolbar
    lateinit var mBottomNav : BottomNavigationView
    lateinit var sharedViewModelCurrentUser: SharedViewModelCurrentUser


//    private lateinit var viewPager: ViewPager
//    private lateinit var pagerAdapter: PagesPagerAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sharedViewModelCurrentUser = ViewModelProviders.of(this).get(SharedViewModelCurrentUser::class.java)

        val sharedViewModelImages = ViewModelProviders.of(this).get(SharedViewModelImage::class.java)

        val sharedViewModelUsers = ViewModelProviders.of(this).get(SharedViewModelRandomUserId::class.java)

        FirebaseApp.initializeApp(this)

        checkIfLoggedIn()
        fetchCurrentUser()
        val navController = Navigation.findNavController(this, R.id.nav_host_fragment)

        setupBottomNavMenu(navController)
        setupActionBar()



    }

    private fun setupBottomNavMenu(navController: NavController) {

        mBottomNav = findViewById(R.id.bottom_nav)

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
        }

    }

    private fun fetchCurrentUser() {

        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {

                sharedViewModelCurrentUser.currentUserObject = p0.getValue(Users::class.java)!!
            }

        })
    }

    private fun setupActionBar() {

        mToolbar = findViewById<Toolbar>(R.id.my_toolbar)
        setSupportActionBar(mToolbar)
    }

    companion object {
        fun newInstance(): MainActivity = MainActivity()
    }
}

