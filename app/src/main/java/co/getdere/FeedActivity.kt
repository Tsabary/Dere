package co.getdere

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import co.getdere.Fragments.*
import co.getdere.Models.Answers
import co.getdere.Models.Images
import co.getdere.Models.Question
import co.getdere.Models.Users
import co.getdere.RegisterLogin.RegisterActivity
import co.getdere.ViewModels.SharedViewModelCurrentUser
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
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.subcontents_main.*


class FeedActivity : AppCompatActivity() {

    lateinit var mToolbar: Toolbar
    lateinit var mBottomNav: BottomNavigationView

    lateinit var sharedViewModelCurrentUser: SharedViewModelCurrentUser
    lateinit var sharedViewModelImage: SharedViewModelImage
    lateinit var sharedViewModelRandomUser: SharedViewModelRandomUser
    lateinit var sharedViewModelQuestion: SharedViewModelQuestion

//    lateinit var feedNavHostFragment: Fragment

    lateinit var feedFragment: FeedFragment
    lateinit var boardFragment: BoardFragment
    lateinit var profileLoggedInUserFragment: ProfileLoggedInUserFragment

    lateinit var imageFullSizeFragment: ImageFullSizeFragment
    lateinit var profileRandomUserFragment: ProfileRandomUserFragment
    lateinit var bucketFragment: AddToBucketFragment
    lateinit var openedQuestionFragment: OpenedQuestionFragment
    lateinit var feedNotificationsFragment: FeedNotificationsFragment
    lateinit var boardNotificationsFragment: BoardNotificationsFragment
    lateinit var savedQuestionFragment: SavedQuestionsFragment
    lateinit var answerCommentFragment: AnswerCommentFragment
    lateinit var answerFragment: AnswerFragment
    lateinit var newQuestionFragment: NewQuestionFragment
    lateinit var editProfileFragment: EditProfileFragment


    lateinit var mainFrame: FrameLayout
    lateinit var subFrame: FrameLayout

    val fm = supportFragmentManager
    val subFm = supportFragmentManager

    lateinit var active: Fragment
    lateinit var subActive: Fragment

    var answerObject = Answers()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feed)

        sharedViewModelCurrentUser = ViewModelProviders.of(this).get(SharedViewModelCurrentUser::class.java)
        sharedViewModelQuestion = ViewModelProviders.of(this).get(SharedViewModelQuestion::class.java)

        FirebaseApp.initializeApp(this)
        checkIfLoggedIn()



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


                R.id.destination_feed -> {

                    fm.beginTransaction().hide(active).show(feedFragment).commit()
                    active = feedFragment

                    val menuItem = mBottomNav.menu.findItem(R.id.destination_feed)
                    menuItem.isChecked = true


                    subFm.beginTransaction().hide(subActive).show(imageFullSizeFragment).commit()
                    subActive = imageFullSizeFragment

                    if (mainFrame.visibility == View.GONE) {
                        switchVisibility(0)
                    }


                }

                co.getdere.R.id.destination_board -> {

                    fm.beginTransaction().hide(active).show(boardFragment).commit()
                    active = boardFragment

                    val menuItem = mBottomNav.menu.findItem(R.id.destination_board)
                    menuItem.isChecked = true


                    if (mainFrame.visibility == View.GONE) {
                        switchVisibility(0)
                    }

                    subFm.beginTransaction().hide(subActive).show(openedQuestionFragment).commit()
                    subActive = openedQuestionFragment


                }
                R.id.destination_profile_logged_in_user -> {

                    fm.beginTransaction().hide(active).show(profileLoggedInUserFragment).commit()
                    active = profileLoggedInUserFragment

                    val menuItem = mBottomNav.menu.findItem(R.id.destination_profile_logged_in_user)
                    menuItem.isChecked = true


                    if (mainFrame.visibility == View.GONE) {
                        switchVisibility(0)
                    }

                    subFm.beginTransaction().hide(subActive).show(editProfileFragment).commit()
                    subActive = editProfileFragment
                }
            }
            false
        }


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

        if (mainFrame.visibility == View.GONE) { // subframe is active

            when (subActive) {

                imageFullSizeFragment -> {
                    switchVisibility(0)
                    sharedViewModelImage.sharedImageObject.postValue(Images())
                }
                bucketFragment -> {

                    subFm.beginTransaction().hide(subActive).show(imageFullSizeFragment).commit()
//                    subFm.beginTransaction().remove(bucketFragment).commit()

                    subActive = imageFullSizeFragment
                }

                profileRandomUserFragment -> {

                    imageFullSizeFragment.layoutScroll.fullScroll(View.FOCUS_UP)
                    subFm.beginTransaction().hide(subActive).show(imageFullSizeFragment).commit()
                    subActive = imageFullSizeFragment

                    sharedViewModelRandomUser.randomUserObject.postValue(Users())
                }

                feedNotificationsFragment -> {
                    switchVisibility(0)
                }

                openedQuestionFragment -> {
                    switchVisibility(0)
                    sharedViewModelQuestion.questionObject.postValue(Question())
                }

                boardNotificationsFragment -> {
                    switchVisibility(0)
                }

                savedQuestionFragment -> {
                    switchVisibility(0)
                }

                answerCommentFragment -> {
                    subFm.beginTransaction().hide(subActive).show(openedQuestionFragment).commit()
                    subActive = openedQuestionFragment
                }

                answerFragment -> {
                    subFm.beginTransaction().hide(subActive).show(openedQuestionFragment).commit()
                    subActive = openedQuestionFragment
                }

                newQuestionFragment -> {
                    switchVisibility(0)
                }

            }


        } else {

            when (active) { // main frame is active

                profileLoggedInUserFragment -> {

                    fm.beginTransaction().hide(active).show(feedFragment).commit()
                    active = feedFragment

                    subFm.beginTransaction().hide(subActive).show(imageFullSizeFragment).commit()
                    subActive = imageFullSizeFragment

                    val menuItem = mBottomNav.menu.findItem(R.id.destination_feed)
                    menuItem.isChecked = true

                    switchVisibility(0)

                }

                boardFragment -> {

                    fm.beginTransaction().hide(active).show(feedFragment).commit()
                    active = feedFragment

                    subFm.beginTransaction().hide(subActive).show(imageFullSizeFragment).commit()
                    subActive = imageFullSizeFragment

                    val menuItem = mBottomNav.menu.findItem(R.id.destination_feed)
                    menuItem.isChecked = true

                    switchVisibility(0)

                }

                feedFragment -> super.onBackPressed()
            }

        }

    }


//    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
//
//        val id = item!!.getItemId()
//
//        //noinspection SimplifiableIfStatement
//        if (id == co.getdere.R.id.destination_camera) {
//            Toast.makeText(this, "Action clicked", Toast.LENGTH_LONG).show()
//            return true
//        } else {
//            Toast.makeText(this, "Action clicked 2", Toast.LENGTH_LONG).show()
//
//
//        }
//
//        return super.onOptionsItemSelected(item)
//
//
//    }

//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
////        menuInflater.inflate(co.getdere.R.menu.feed_navigation, menu)
//        return super.onCreateOptionsMenu(menu)
//    }

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

                addFragmentsToFragmentManagers()

//                setupNavController()
            }

        })
    }


    fun addFragmentsToFragmentManagers() {

        //main container

        feedFragment = FeedFragment()
        boardFragment = BoardFragment()
        profileLoggedInUserFragment = ProfileLoggedInUserFragment()

        fm.beginTransaction().add(R.id.feed_frame_container, feedFragment, "feedFragment").commit()

        fm.beginTransaction().add(R.id.feed_frame_container, boardFragment, "boardFragment").hide(boardFragment)
            .commit()
        fm.beginTransaction()
            .add(R.id.feed_frame_container, profileLoggedInUserFragment, "profileLoggedInUserFragment")
            .hide(profileLoggedInUserFragment).commit()

        active = feedFragment



        //sub container
        imageFullSizeFragment = ImageFullSizeFragment()
        profileRandomUserFragment = ProfileRandomUserFragment()
        bucketFragment = AddToBucketFragment()
        openedQuestionFragment = OpenedQuestionFragment()
        feedNotificationsFragment = FeedNotificationsFragment()
        boardNotificationsFragment = BoardNotificationsFragment()
        savedQuestionFragment = SavedQuestionsFragment()
        answerCommentFragment = AnswerCommentFragment()
        answerFragment = AnswerFragment()
        newQuestionFragment = NewQuestionFragment()
        editProfileFragment = EditProfileFragment()


        subFm.beginTransaction()
            .add(R.id.feed_subcontents_frame_container, imageFullSizeFragment, "imageFullSizeFragment").commit()
        subFm.beginTransaction()
            .add(R.id.feed_subcontents_frame_container, profileRandomUserFragment, "profileRandomUserFragment")
            .hide(profileRandomUserFragment).commit()
        subFm.beginTransaction()
            .add(R.id.feed_subcontents_frame_container, bucketFragment, "bucketFragment").hide(bucketFragment).commit()
        subFm.beginTransaction()
            .add(R.id.feed_subcontents_frame_container, openedQuestionFragment, "openedQuestionFragment")
            .hide(openedQuestionFragment).commit()
        subFm.beginTransaction()
            .add(R.id.feed_subcontents_frame_container, feedNotificationsFragment, "feedNotificationsFragment")
            .hide(feedNotificationsFragment).commit()
        subFm.beginTransaction()
            .add(R.id.feed_subcontents_frame_container, boardNotificationsFragment, "boardNotificationsFragment")
            .hide(boardNotificationsFragment).commit()
        subFm.beginTransaction()
            .add(R.id.feed_subcontents_frame_container, savedQuestionFragment, "savedQuestionFragment")
            .hide(savedQuestionFragment).commit()
        subFm.beginTransaction()
            .add(R.id.feed_subcontents_frame_container, answerCommentFragment, "answerCommentFragment")
            .hide(answerCommentFragment).commit()
        subFm.beginTransaction()
            .add(R.id.feed_subcontents_frame_container, answerFragment, "answerFragment")
            .hide(answerFragment).commit()
        subFm.beginTransaction()
            .add(R.id.feed_subcontents_frame_container, newQuestionFragment, "newQuestionFragment")
            .hide(newQuestionFragment).commit()
        fm.beginTransaction().add(R.id.feed_subcontents_frame_container, editProfileFragment, "editProfileFragment")
            .hide(editProfileFragment).commit()


        subActive = imageFullSizeFragment


    }


    companion object {
        fun newInstance(): FeedActivity = FeedActivity()
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


}

