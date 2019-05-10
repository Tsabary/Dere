package co.getdere

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProviders
import co.getdere.fragments.*
import co.getdere.interfaces.DereMethods
import co.getdere.models.Answers
import co.getdere.models.Images
import co.getdere.models.Question
import co.getdere.models.Users
import co.getdere.registerLogin.RegisterActivity
import co.getdere.viewmodels.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import io.branch.indexing.BranchUniversalObject
import io.branch.referral.Branch
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.subcontents_main.*
import androidx.core.view.ViewCompat.getRotation
import android.content.Context.WINDOW_SERVICE
import android.view.WindowManager
import android.view.Display
import com.google.firebase.analytics.FirebaseAnalytics
import com.yalantis.ucrop.UCrop


class MainActivity : AppCompatActivity(), DereMethods {

    lateinit var mBottomNav: BottomNavigationView

    lateinit var sharedViewModelCurrentUser: SharedViewModelCurrentUser
    lateinit var sharedViewModelImage: SharedViewModelImage
    lateinit var sharedViewModelRandomUser: SharedViewModelRandomUser
    lateinit var sharedViewModelQuestion: SharedViewModelQuestion
    lateinit var sharedViewModelCollection: SharedViewModelCollection

    lateinit var onBoardingFragment: OnBoardingFragment
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
    lateinit var bucketGalleryFragment: BucketGalleryFragment
    lateinit var addImageToAnswer: AddImageToAnswerFragment
    lateinit var imagePostEditFragment: ImagePostEditFragment
    lateinit var webViewFragment: WebViewFragment
    lateinit var editQuestionFragment: EditQuestionFragment
    lateinit var editAnswerFragment: EditAnswerFragment
    lateinit var editInterestsFragment: EditInterestsFragment
    lateinit var collectionMapView: CollectionMapViewFragment

    lateinit var mainFrame: FrameLayout
    lateinit var subFrame: FrameLayout

    val fm = supportFragmentManager
    val subFm = supportFragmentManager

    lateinit var active: Fragment
    lateinit var subActive: Fragment

    var answerObject = Answers()
    var boardNotificationsCount = MutableLiveData<Int>()
    var feedNotificationsCount = MutableLiveData<Int>()

    var isBucketGalleryActive = false
    var isOpenedQuestionActive = false
    var isEditAnswerActive = false
    var isFeedNotificationsActive = false
    var isBoardNotificationsActive = false
    var isFeedActive = false
    var isRandomUserProfileActive = false

    lateinit var currentUser: Users

    lateinit var firebaseAnalytics: FirebaseAnalytics


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        firebaseAnalytics = FirebaseAnalytics.getInstance(this)



        checkIfLoggedIn()

        sharedViewModelCurrentUser = ViewModelProviders.of(this).get(SharedViewModelCurrentUser::class.java)
        sharedViewModelQuestion = ViewModelProviders.of(this).get(SharedViewModelQuestion::class.java)

        FirebaseApp.initializeApp(this)


        sharedViewModelImage = ViewModelProviders.of(this).get(SharedViewModelImage::class.java)
        sharedViewModelImage.sharedImageObject.postValue(Images())

        sharedViewModelRandomUser = ViewModelProviders.of(this).get(SharedViewModelRandomUser::class.java)
        sharedViewModelRandomUser.randomUserObject.postValue(Users())

        sharedViewModelCollection = ViewModelProviders.of(this).get(SharedViewModelCollection::class.java)

        subFrame = feed_subcontents_frame_container

        mainFrame = feed_frame_container

        switchVisibility(0)
    }

    private fun setupBottomNav() {

        mBottomNav = feed_bottom_nav

        mBottomNav.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {

                R.id.destination_feed -> {
                    navigateToFeed()
                    Log.d("bottomNav", "feed")
                }

                R.id.destination_board -> {
                    navigateToBoard()
                    Log.d("bottomNav", "board")

                }
                R.id.destination_profile_logged_in_user -> {
                    navigateToProfile()
                    Log.d("bottomNav", "profile")

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

    private fun resetImageExpended() {
        sharedViewModelImage.sharedImageObject.postValue(Images())
        imageFullSizeFragment.actionsContainer.visibility = View.INVISIBLE
        imageFullSizeFragment.optionsContainer.visibility = View.GONE
        imageFullSizeFragment.deleteEditContainer.visibility = View.VISIBLE
        imageFullSizeFragment.deleteContainer.visibility = View.GONE
    }

    override fun onBackPressed() {

        if (mainFrame.visibility == View.GONE) { // subframe is active

            when (subActive) {

                imageFullSizeFragment -> {
                    when {
                        isBucketGalleryActive -> {
                            subFm.beginTransaction().hide(subActive).attach(bucketGalleryFragment).commit()
                            subActive = bucketGalleryFragment
                            bucketGalleryFragment.galleryViewPager.currentItem = 1
                            resetImageExpended()
                        }
                        isOpenedQuestionActive -> {
                            subFm.beginTransaction().hide(subActive).show(openedQuestionFragment).commit()
                            subActive = openedQuestionFragment
                            resetImageExpended()
                        }
                        isFeedNotificationsActive -> {
                            subFm.beginTransaction().hide(subActive).show(feedNotificationsFragment).commit()
                            subActive = feedNotificationsFragment
                            resetImageExpended()
                        }
                        isBoardNotificationsActive -> {
                            subFm.beginTransaction().hide(subActive).show(boardNotificationsFragment).commit()
                            subActive = boardNotificationsFragment
                            resetImageExpended()
                        }
                        else -> {
                            switchVisibility(0)
                            resetImageExpended()
                        }
                    }
                }
                bucketFragment -> {

                    if (isFeedActive) {
                        subFm.beginTransaction().remove(subActive).show(imageFullSizeFragment).commit()
                        subActive = imageFullSizeFragment
                        switchVisibility(0)
                    }

                    fm.beginTransaction().detach(profileLoggedInUserFragment).attach(profileLoggedInUserFragment)
                        .commit()

                    subActive = imageFullSizeFragment
                }

                profileRandomUserFragment -> {
                    if (isFeedActive) {
                        navigateToFeed()
                        isFeedActive = false
                    } else {
                        imageFullSizeFragment.layoutScroll.fullScroll(View.FOCUS_UP)
                        subFm.beginTransaction().hide(subActive).show(imageFullSizeFragment).commit()
                        subActive = imageFullSizeFragment

                        sharedViewModelRandomUser.randomUserObject.postValue(Users())
                    }
                }

                feedNotificationsFragment -> {
                    switchVisibility(0)
                }

                openedQuestionFragment -> {
                    switchVisibility(0)
                    sharedViewModelQuestion.questionObject.postValue(Question())
                    isOpenedQuestionActive = false
                    openedQuestionFragment.deleteBox.visibility = View.GONE
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

                bucketGalleryFragment -> {
                    switchVisibility(0)
                    isBucketGalleryActive = false
//                    sharedViewModelCollection.imageCollection.postValue(DataSnapshot(DatabaseReference(),""))
                }

                editProfileFragment -> {
                    switchVisibility(0)
                }

                imagePostEditFragment -> {
                    subFm.beginTransaction().hide(subActive).show(imageFullSizeFragment).commit()
                    subActive = imageFullSizeFragment
                }

                webViewFragment -> {
                    subFm.beginTransaction().hide(subActive).show(imageFullSizeFragment).commit()
                    subActive = imageFullSizeFragment
                }

                editQuestionFragment -> {
                    subFm.beginTransaction().hide(subActive).show(openedQuestionFragment).commit()
                    subActive = openedQuestionFragment
                }

                editAnswerFragment -> {
                    subFm.beginTransaction().hide(subActive).show(openedQuestionFragment).commit()
                    subActive = openedQuestionFragment
                    isEditAnswerActive = false
                }

                addImageToAnswer -> {
                    if (isEditAnswerActive) {
                        subFm.beginTransaction().hide(subActive).show(editAnswerFragment).commit()
                        subActive = editAnswerFragment
                    } else {
                        subFm.beginTransaction().hide(subActive).show(answerFragment).commit()
                        subActive = answerFragment
                    }
                }

                editInterestsFragment -> {
                    switchVisibility(0)
                }

                collectionMapView -> {

                    if (isRandomUserProfileActive) {
                        subFm.beginTransaction().hide(subActive).show(editAnswerFragment).commit()
                        subActive = editAnswerFragment
                        isRandomUserProfileActive = false
                    } else {
                        switchVisibility(0)
                    }
                }
            }


        } else {

            when (active) { // main frame is active

                onBoardingFragment -> {
                    when (onBoardingFragment.viewPager.currentItem) {
                        0 -> {
                        }

                        1 -> {
                            onBoardingFragment.setUpItem1()
                        }

                        2 -> {
                            onBoardingFragment.setUpItem2()
                        }

                        3 -> {
                            onBoardingFragment.setUpItem3()
                        }

                        4 -> {
                            onBoardingFragment.setUpItem4()
                        }

                        5 -> {
                            onBoardingFragment.setUpItem5()
                        }
                    }

                }

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

    private fun resetFragments() {
        closeKeyboard(this)

        openedQuestionFragment.deleteBox.visibility = View.GONE
        bucketGalleryFragment.mapButton.setImageResource(R.drawable.world)

        isBucketGalleryActive = false
        isOpenedQuestionActive = false
        isEditAnswerActive = false
        isFeedNotificationsActive = false
        isBoardNotificationsActive = false

        sharedViewModelImage.sharedImageObject.postValue(Images())
        sharedViewModelRandomUser.randomUserObject.postValue(Users())
        sharedViewModelQuestion.questionObject.postValue(Question())

        if (mainFrame.visibility == View.GONE) {
            switchVisibility(0)
        }

        imageFullSizeFragment.showLocation.setImageResource(R.drawable.location)

        subFm.beginTransaction().remove(bucketFragment).commit()
    }

    private fun checkIfLoggedIn() {

        val uid = FirebaseAuth.getInstance().uid
        if (uid == null) {
            val intent = Intent(this, RegisterActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        } else {
            fetchCurrentUser(uid)
        }
    }

    private fun fetchCurrentUser(uid: String) {

        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid/profile")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {

                sharedViewModelCurrentUser.currentUserObject = p0.getValue(Users::class.java)!!

                addFragmentsToFragmentManagers()

            }

        })
    }


    fun addFragmentsToFragmentManagers() {

        //main container
        onBoardingFragment = OnBoardingFragment()
        feedFragment = FeedFragment()
        boardFragment = BoardFragment()
        profileLoggedInUserFragment = ProfileLoggedInUserFragment()


        val uid = FirebaseAuth.getInstance().uid
        val interestsRef = FirebaseDatabase.getInstance().getReference("/users/$uid/interests")
        interestsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.hasChildren()) {
                    fm.beginTransaction().add(R.id.feed_frame_container, feedFragment, "feedFragment").commit()

                    active = feedFragment

                } else {
                    fm.beginTransaction().add(R.id.feed_frame_container, feedFragment, "feedFragment")
                        .hide(feedFragment).commit()
                    fm.beginTransaction().add(R.id.feed_frame_container, onBoardingFragment, "onBoardingFragment")
                        .commit()

                    active = onBoardingFragment
                }
            }
        })

        fm.beginTransaction().add(R.id.feed_frame_container, boardFragment, "boardFragment").hide(boardFragment)
            .commit()
        fm.beginTransaction()
            .add(R.id.feed_frame_container, profileLoggedInUserFragment, "profileLoggedInUserFragment")
            .hide(profileLoggedInUserFragment).commit()


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
        bucketGalleryFragment = BucketGalleryFragment()
        addImageToAnswer = AddImageToAnswerFragment()
        imagePostEditFragment = ImagePostEditFragment()
        webViewFragment = WebViewFragment()
        editQuestionFragment = EditQuestionFragment()
        editAnswerFragment = EditAnswerFragment()
        editInterestsFragment = EditInterestsFragment()
        collectionMapView = CollectionMapViewFragment()


        subFm.beginTransaction()
            .add(R.id.feed_subcontents_frame_container, imageFullSizeFragment, "imageFullSizeFragment").commit()
        subFm.beginTransaction()
            .add(R.id.feed_subcontents_frame_container, profileRandomUserFragment, "profileRandomUserFragment")
            .hide(profileRandomUserFragment).commit()
//        subFm.beginTransaction()
//            .add(R.id.feed_subcontents_frame_container, bucketFragment, "bucketFragment").hide(bucketFragment).commit()
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
        fm.beginTransaction().add(R.id.feed_subcontents_frame_container, bucketGalleryFragment, "bucketGalleryFragment")
            .hide(bucketGalleryFragment).commit()
        fm.beginTransaction().add(R.id.feed_subcontents_frame_container, addImageToAnswer, "addImageToAnswer")
            .hide(addImageToAnswer).commit()
        fm.beginTransaction().add(R.id.feed_subcontents_frame_container, imagePostEditFragment, "imagePostEditFragment")
            .hide(imagePostEditFragment).commit()
        fm.beginTransaction().add(R.id.feed_subcontents_frame_container, webViewFragment, "webViewFragment")
            .hide(webViewFragment).commit()
        fm.beginTransaction().add(R.id.feed_subcontents_frame_container, editQuestionFragment, "editQuestionFragment")
            .hide(editQuestionFragment).commit()
        fm.beginTransaction().add(R.id.feed_subcontents_frame_container, editAnswerFragment, "editAnswerFragment")
            .hide(editAnswerFragment).commit()
        fm.beginTransaction().add(R.id.feed_subcontents_frame_container, editInterestsFragment, "editInterestsFragment")
            .hide(editInterestsFragment).commit()
        fm.beginTransaction().add(R.id.feed_subcontents_frame_container, collectionMapView, "collectionMapView")
            .hide(collectionMapView).commit()

        subActive = imageFullSizeFragment

        setupBottomNav()
    }

    private fun branchInitSession() {
        Branch.getInstance().initSession({ branchUniversalObject, referringParams, error ->

            if (error == null) {
                println("branch no error")


                if (branchUniversalObject != null) {

                    val type = branchUniversalObject.contentMetadata.customMetadata["type"]

                    when (type) {

                        "image" -> collectImage(branchUniversalObject)

                        "question" -> collectQuestion(branchUniversalObject)

                        "user" -> collectProfile(branchUniversalObject)
                    }


                }


            } else {
                println("branch definitely error" + error.message)

            }


        }, this.intent.data, this)
    }

    override fun onResume() {
        super.onResume()
        branchInitSession()
    }

    override fun onStart() {
        super.onStart()
        branchInitSession()
    }

    private fun navigateToFeed() {
        fm.beginTransaction().hide(active).show(feedFragment).commit()
        active = feedFragment
        val menuItem = mBottomNav.menu.findItem(R.id.destination_feed)
        menuItem.isChecked = true
        subFm.beginTransaction().hide(subActive).show(imageFullSizeFragment).commit()
        subActive = imageFullSizeFragment
        resetFragments()
    }

    private fun navigateToBoard() {
        fm.beginTransaction().hide(active).show(boardFragment).commit()
        active = boardFragment
        val menuItem = mBottomNav.menu.findItem(R.id.destination_board)
        menuItem.isChecked = true
        subFm.beginTransaction().hide(subActive).show(openedQuestionFragment).commit()
        subActive = openedQuestionFragment
        resetFragments()
    }


    private fun navigateToProfile() {
        fm.beginTransaction().hide(active).show(profileLoggedInUserFragment).commit()
        active = profileLoggedInUserFragment
        val menuItem = mBottomNav.menu.findItem(R.id.destination_profile_logged_in_user)
        menuItem.isChecked = true
        subFm.beginTransaction().hide(subActive).show(imageFullSizeFragment).commit()
        subActive = imageFullSizeFragment
        resetFragments()
    }

    private fun collectProfile(branchUniversalObject: BranchUniversalObject) {
        val uid = FirebaseAuth.getInstance().uid

        val profileIdentifier = branchUniversalObject.canonicalIdentifier

        if (uid == profileIdentifier) {
            navigateToProfile()
            Log.d("whosProfile", "Your profile")
        } else {
            Log.d("whosProfile", "Stranger profile")

            val profileRef = FirebaseDatabase.getInstance().getReference("/users/$profileIdentifier/profile")

            profileRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                }

                override fun onDataChange(p0: DataSnapshot) {
                    val user = p0.getValue(Users::class.java)

                    sharedViewModelRandomUser.randomUserObject.postValue(user)
                    subFm.beginTransaction().hide(subActive).show(profileRandomUserFragment).commit()
                    switchVisibility(1)
                    subActive = profileRandomUserFragment

                    val menuItem = mBottomNav.menu.findItem(R.id.destination_profile_logged_in_user)
                    menuItem.isChecked = true
                }
            })
        }
    }


    private fun collectImage(branchUniversalObject: BranchUniversalObject) {

        val imageUrl = branchUniversalObject.canonicalIdentifier

        val refImage = FirebaseDatabase.getInstance().getReference("/images/$imageUrl/body")

        refImage.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {

                val image = p0.getValue(Images::class.java)

                sharedViewModelImage.sharedImageObject.postValue(image)

                val refUser = FirebaseDatabase.getInstance().getReference("/users/${image!!.photographer}/profile")

                refUser.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {
                    }

                    override fun onDataChange(p0: DataSnapshot) {

                        sharedViewModelRandomUser.randomUserObject.postValue(p0.getValue(Users::class.java))

                        subFm.beginTransaction().hide(subActive).show(imageFullSizeFragment).commit()
                        switchVisibility(1)
                        subActive = imageFullSizeFragment

                        val menuItem = mBottomNav.menu.findItem(R.id.destination_feed)
                        menuItem.isChecked = true
                    }

                })
            }


        })

    }


    private fun collectQuestion(branchUniversalObject: BranchUniversalObject) {

        val questionUrl = branchUniversalObject.canonicalIdentifier

        val refImage = FirebaseDatabase.getInstance().getReference("/questions/$questionUrl/main/body")

        refImage.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {

                val question = p0.getValue(Question::class.java)

                Log.d("doYouKnow", question!!.title)

                sharedViewModelQuestion.questionObject.postValue(question)

                val refUser = FirebaseDatabase.getInstance().getReference("/users/${question!!.author}/profile")

                refUser.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {
                    }

                    override fun onDataChange(p0: DataSnapshot) {

                        sharedViewModelRandomUser.randomUserObject.postValue(p0.getValue(Users::class.java))

                        subFm.beginTransaction().hide(subActive).show(openedQuestionFragment).commit()

                        switchVisibility(1)

                        fm.beginTransaction().hide(active).show(boardFragment).commit()

                        subActive = openedQuestionFragment
                        active = boardFragment

                        val menuItem = mBottomNav.menu.findItem(R.id.destination_board)
                        menuItem.isChecked = true
                    }

                })
            }


        })

    }


    public override fun onNewIntent(intent: Intent) {
        this.intent = intent
        super.onNewIntent(intent)//this is not sure!!!!!! If there are problems this might be the source of it, instructions didn't say to add  the super but studio has
    }


    companion object {
        fun newInstance(): MainActivity = MainActivity()
    }

}

