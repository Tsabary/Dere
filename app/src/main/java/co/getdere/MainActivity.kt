package co.getdere

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import co.getdere.fragments.*
import co.getdere.interfaces.DereMethods
import co.getdere.models.Answers
import co.getdere.models.Images
import co.getdere.models.Question
import co.getdere.models.Users
import co.getdere.viewmodels.*
import com.etebarian.meowbottomnavigation.MeowBottomNavigation
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import io.branch.indexing.BranchUniversalObject
import io.branch.referral.Branch
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.subcontents_main.*


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
    lateinit var marketplaceFragment: MarketplaceFragment
    lateinit var profileLoggedInUserFragment: ProfileLoggedInUserFragment

    lateinit var imageFullSizeFragment: ImageFullSizeFragment
    lateinit var secondImageFullSizeFragment: SecondImageFullSizeFragment
    lateinit var profileRandomUserFragment: ProfileRandomUserFragment
    lateinit var profileSecondRandomUserFragment: ProfileSecondRandomUserFragment
    lateinit var addToBucketFragment: AddToBucketFragment
    lateinit var openedQuestionFragment: OpenedQuestionFragment
    lateinit var feedNotificationsFragment: FeedNotificationsFragment
    lateinit var boardNotificationsFragment: BoardNotificationsFragment
    lateinit var savedQuestionFragment: SavedQuestionsFragment
    lateinit var answerCommentFragment: AnswerCommentFragment
    lateinit var answerFragment: AnswerFragment
    lateinit var newQuestionFragment: NewQuestionFragment
    lateinit var editProfileFragment: EditProfileFragment
    lateinit var collectionGalleryFragment: CollectionGalleryFragment
    lateinit var addImageToAnswer: AddImageToAnswerFragment
    lateinit var imagePostEditFragment: ImagePostEditFragment
    lateinit var webViewFragment: WebViewFragment
    lateinit var editQuestionFragment: EditQuestionFragment
    lateinit var editAnswerFragment: EditAnswerFragment
    lateinit var editInterestsFragment: EditInterestsFragment
    lateinit var collectionMapView: CollectionMapViewFragment
    lateinit var addToItineraryFragment: AddToItineraryFragment
    lateinit var itineraryFragment: ItineraryFragment
    lateinit var itineraryEditFragment: ItineraryEditFragment
    lateinit var addImagesToItineraryFragment: AddImagesToItineraryFragment
    lateinit var marketplacePurchasedFragment: MarketplacePurchasedFragment
    lateinit var buyItineraryFragment: BuyItineraryFragment
    lateinit var addImagesToItineraryDayFragment: AddImagesToItineraryDayFragment
    lateinit var dayMapViewFragment: DayMapViewFragment
    lateinit var bucketAndSharedItineraryPagerFragment: BucketAndSharedItineraryPagerFragment
    lateinit var joinSharedItineraryFragment: JoinSharedItineraryFragment
    lateinit var sharedViewModelTags: SharedViewModelTags


    lateinit var mainFrame: FrameLayout
    lateinit var subFrame: FrameLayout

    val fm = supportFragmentManager
    val subFm = supportFragmentManager


    lateinit var active: Fragment
    lateinit var subActive: Fragment

    var boardNotificationsCount = MutableLiveData<Int>()
    var feedNotificationsCount = MutableLiveData<Int>()

    var isCollectionMapViewActive = false
    var isCollectionGalleryActive = false
    var isOpenedQuestionActive = false
    var isEditAnswerActive = false
    var isFeedNotificationsActive = false
    var isBoardNotificationsActive = false
    var isFeedActive = false
    var isRandomUserProfileActive = false
    var isSecondRandomUserProfileActive = false
    var isItineraryActive = false
    var isSavedItinerariesActive = false


    var fragmentsHaveBeenInitialized = false

    lateinit var currentUser: Users

    lateinit var firebaseAnalytics: FirebaseAnalytics

    var answerObject = Answers()

    var tags: MutableList<SingleTagForList> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        FirebaseApp.initializeApp(this)
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)

        checkIfLoggedIn()

        sharedViewModelTags = ViewModelProviders.of(this).get(SharedViewModelTags::class.java)

        sharedViewModelCurrentUser = ViewModelProviders.of(this).get(SharedViewModelCurrentUser::class.java)
        sharedViewModelQuestion = ViewModelProviders.of(this).get(SharedViewModelQuestion::class.java)


        sharedViewModelImage = ViewModelProviders.of(this).get(SharedViewModelImage::class.java)
        sharedViewModelImage.sharedImageObject.postValue(Images())

        sharedViewModelRandomUser = ViewModelProviders.of(this).get(SharedViewModelRandomUser::class.java)
        sharedViewModelRandomUser.randomUserObject.postValue(Users())

        sharedViewModelCollection = ViewModelProviders.of(this).get(SharedViewModelCollection::class.java)

        subFrame = feed_subcontents_frame_container

        mainFrame = feed_frame_container

        switchVisibility(0)
    }


    private fun checkIfLoggedIn() {

        val uid = FirebaseAuth.getInstance().uid
        if (uid == null) {
            val intent = Intent(this, RegisterLoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        } else {
            fetchCurrentUser(uid)
        }
    }

    private fun fetchCurrentUser(uid: String) {
        FirebaseDatabase.getInstance().getReference("/users/$uid/profile")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {}
                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.getValue(Users::class.java) != null) {
                        currentUser = p0.getValue(Users::class.java)!!
                        sharedViewModelCurrentUser.currentUserObject = currentUser
                        addFragmentsToFragmentManagers()
                    }
                }
            })
    }

    fun addFragmentsToFragmentManagers() {
        //main container
        onBoardingFragment = OnBoardingFragment()
        feedFragment = FeedFragment()
        boardFragment = BoardFragment()
        marketplaceFragment = MarketplaceFragment()
        profileLoggedInUserFragment = ProfileLoggedInUserFragment()

        FirebaseDatabase.getInstance().getReference("/users/${currentUser.uid}/interests")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {}

                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.hasChildren()) {
                        fm.beginTransaction().add(R.id.feed_frame_container, feedFragment, "feedFragment")
                            .commitAllowingStateLoss()

                        active = feedFragment

                    } else {
                        fm.beginTransaction().add(R.id.feed_frame_container, feedFragment, "feedFragment")
                            .hide(feedFragment).commitAllowingStateLoss()
                        fm.beginTransaction().add(R.id.feed_frame_container, onBoardingFragment, "onBoardingFragment")
                            .commitAllowingStateLoss()

                        active = onBoardingFragment
                    }
                    fragmentsHaveBeenInitialized = true
                }
            })

        fm.beginTransaction().add(R.id.feed_frame_container, boardFragment, "boardFragment").hide(boardFragment)
            .commitAllowingStateLoss()
        fm.beginTransaction().add(R.id.feed_frame_container, marketplaceFragment, "marketplaceFragment")
            .hide(marketplaceFragment)
            .commitAllowingStateLoss()
        fm.beginTransaction()
            .add(R.id.feed_frame_container, profileLoggedInUserFragment, "profileLoggedInUserFragment")
            .hide(profileLoggedInUserFragment).commitAllowingStateLoss()

        //sub container
        imageFullSizeFragment = ImageFullSizeFragment()
        profileRandomUserFragment = ProfileRandomUserFragment()
        profileSecondRandomUserFragment = ProfileSecondRandomUserFragment()
        addToBucketFragment = AddToBucketFragment()
        openedQuestionFragment = OpenedQuestionFragment()
        feedNotificationsFragment = FeedNotificationsFragment()
        boardNotificationsFragment = BoardNotificationsFragment()
        savedQuestionFragment = SavedQuestionsFragment()
        answerCommentFragment = AnswerCommentFragment()
        answerFragment = AnswerFragment()
        newQuestionFragment = NewQuestionFragment()
        editProfileFragment = EditProfileFragment()
        collectionGalleryFragment = CollectionGalleryFragment()
        addImageToAnswer = AddImageToAnswerFragment()
        imagePostEditFragment = ImagePostEditFragment()
        webViewFragment = WebViewFragment()
        editQuestionFragment = EditQuestionFragment()
        editAnswerFragment = EditAnswerFragment()
        editInterestsFragment = EditInterestsFragment()
        collectionMapView = CollectionMapViewFragment()
        secondImageFullSizeFragment = SecondImageFullSizeFragment()
        addToItineraryFragment = AddToItineraryFragment()
        itineraryFragment = ItineraryFragment()
        itineraryEditFragment = ItineraryEditFragment()
        addImagesToItineraryFragment = AddImagesToItineraryFragment()
        marketplacePurchasedFragment = MarketplacePurchasedFragment()
        buyItineraryFragment = BuyItineraryFragment()
        addImagesToItineraryDayFragment = AddImagesToItineraryDayFragment()
        dayMapViewFragment = DayMapViewFragment()
        bucketAndSharedItineraryPagerFragment = BucketAndSharedItineraryPagerFragment()
        joinSharedItineraryFragment = JoinSharedItineraryFragment()

        subFm.beginTransaction()
            .add(R.id.feed_subcontents_frame_container, feedNotificationsFragment, "feedNotificationsFragment")
            .hide(feedNotificationsFragment).commitAllowingStateLoss()
        subFm.beginTransaction()
            .add(R.id.feed_subcontents_frame_container, boardNotificationsFragment, "boardNotificationsFragment")
            .hide(boardNotificationsFragment).commitAllowingStateLoss()
        subFm.beginTransaction()
            .add(R.id.feed_subcontents_frame_container, savedQuestionFragment, "savedQuestionFragment")
            .hide(savedQuestionFragment).commitAllowingStateLoss()
        subFm.beginTransaction()
            .add(R.id.feed_subcontents_frame_container, marketplacePurchasedFragment, "marketplacePurchasedFragment")
            .hide(marketplacePurchasedFragment).commitAllowingStateLoss()

        setupBottomNav()
    }

    private fun setupBottomNav() {
        val bottomNavigation = main_activity_bottom_nav
        bottomNavigation.add(MeowBottomNavigation.Model(1, R.drawable.footprint))
        bottomNavigation.add(MeowBottomNavigation.Model(2, R.drawable.signpost))
        bottomNavigation.add(MeowBottomNavigation.Model(3, R.drawable.itinerary))
        bottomNavigation.add(MeowBottomNavigation.Model(4, R.drawable.astronaut))

        bottomNavigation.setOnClickMenuListener {
            when (it.id) {
                1 -> navigateToFeed()
                2 -> navigateToBoard()
                3 -> navigateToMarketplace()
                4 -> navigateToProfile()
            }
        }

        bottomNavigation.show(1, true)

        boardNotificationsCount.observe(this, Observer {
            it?.let { notCount ->
                bottomNavigation.setCount(2, notCount.toString())
                if(notCount == 0){
                    bottomNavigation.setCount(2, "empty")
                }
            }
        })

        feedNotificationsCount.observe(this, Observer {
            it?.let { notCount ->
                bottomNavigation.setCount(1, notCount.toString())
                if(notCount == 0){
                    bottomNavigation.setCount(1, "empty")
                }
            }
        })
    }

//    private fun setupBottomNav() {
//
//        mBottomNav = feed_bottom_nav
//
//        mBottomNav.setOnNavigationItemSelectedListener { item ->
//            when (item.itemId) {
//
//                R.id.destination_feed -> {
//                    navigateToFeed()
//                }
//
//                R.id.destination_board -> {
//                    navigateToBoard()
//                }
//                R.id.destination_marketplace -> {
//                    navigateToMarketplace()
//                }
//                R.id.destination_profile_logged_in_user -> {
//                    navigateToProfile()
//                }
//            }
//            false
//        }
//        addTagsToViewModel()
//    }

    private fun addTagsToViewModel() {
        FirebaseDatabase.getInstance().getReference("/tags")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {
                    for (tag in p0.children) {
                        val tagName = tag.key.toString()
                        val count = tag.childrenCount.toInt()
                        tags.add(SingleTagForList(tagName, count))
                        sharedViewModelTags.tagList = tags
                    }
                }

                override fun onCancelled(p0: DatabaseError) {}
            })
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
        when {
            mainFrame.visibility == View.GONE -> // subframe is active

                when (subActive) {

                    imageFullSizeFragment -> {
                        subFm.popBackStack("imageFullSizeFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE)
                        when {
                            isCollectionMapViewActive -> subActive = collectionMapView
                            isCollectionGalleryActive -> subActive = collectionGalleryFragment
                            else -> switchVisibility(0)
                        }
                    }
                    addToBucketFragment -> {
                        if (isFeedActive) {
                            switchVisibility(0)
                            isFeedActive = false
                        }
                        subFm.popBackStack("addToBucketFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE)
                        subActive = imageFullSizeFragment
                    }

                    profileRandomUserFragment -> {
                        subFm.popBackStack("profileRandomUserFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE)
                        isRandomUserProfileActive = false

                        when {
//                            isFeedActive -> {
//                                navigateToFeed()
//                                isFeedActive = false
//                            }
                            isFeedNotificationsActive -> {
                                subActive = feedNotificationsFragment
                            }

                            isBoardNotificationsActive -> {
                                subActive = boardNotificationsFragment
                            }
                            isOpenedQuestionActive -> {
                                subActive = openedQuestionFragment
                            }

                            isItineraryActive -> {
                                subActive = itineraryFragment
                            }
                            else -> {
                                subActive = imageFullSizeFragment
                            }
                        }
                    }
                    profileSecondRandomUserFragment -> {
                        subFm.popBackStack("profileSecondRandomUserFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE)

                        if (isOpenedQuestionActive) {
                            subActive = openedQuestionFragment
                        } else {
                            subActive = imageFullSizeFragment
                        }
                    }

                    feedNotificationsFragment -> {
                        subFm.beginTransaction().hide(feedNotificationsFragment).commit()
                        switchVisibility(0)
                        isFeedNotificationsActive = false
                    }

                    openedQuestionFragment -> {
                        if (isBoardNotificationsActive) {
                            subActive = boardNotificationsFragment
                        } else {
                            switchVisibility(0)
                        }
                        subFm.popBackStack("openedQuestionFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE)
                        isOpenedQuestionActive = false
                    }

                    boardNotificationsFragment -> {
                        subFm.beginTransaction().hide(boardNotificationsFragment).commit()
                        switchVisibility(0)
                    }

                    savedQuestionFragment -> {
                        subFm.beginTransaction().hide(savedQuestionFragment).commit()
                        switchVisibility(0)
                    }

                    answerCommentFragment -> {
                        subFm.popBackStack("answerCommentFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE)
                        subActive = openedQuestionFragment
                    }

                    answerFragment -> {
                        subFm.popBackStack("answerFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE)
                        subActive = openedQuestionFragment
                    }

                    newQuestionFragment -> {
                        subFm.popBackStack("newQuestionFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE)
                        switchVisibility(0)
                    }

                    collectionGalleryFragment -> {
                        if (collectionGalleryFragment.viewPagerPosition == 1) {
                            collectionGalleryFragment.switchImageAndMap()
                        } else {
                            if (isSavedItinerariesActive) {
                                subActive = marketplacePurchasedFragment
                            } else {
                                switchVisibility(0)
                                isCollectionGalleryActive = false
                            }
                            subFm.popBackStack("collectionGalleryFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE)
                        }
                    }

                    editProfileFragment -> {
                        subFm.popBackStack("editProfileFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE)
                        switchVisibility(0)
                    }

                    imagePostEditFragment -> {
                        subFm.popBackStack("imagePostEditFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE)
                        subActive = imageFullSizeFragment
                    }

                    webViewFragment -> {
                        subFm.popBackStack("webViewFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE)
                        subActive = imageFullSizeFragment
                    }

                    editQuestionFragment -> {
                        subFm.popBackStack("editQuestionFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE)
                        subActive = openedQuestionFragment
                    }

                    editAnswerFragment -> {
                        subFm.popBackStack("editAnswerFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE)
                        subActive = openedQuestionFragment
                        isEditAnswerActive = false
                    }

                    addImageToAnswer -> {
                        if (isEditAnswerActive) {
                            subActive = editAnswerFragment
                        } else {
                            subActive = answerFragment
                        }
                        subFm.popBackStack("addImageToAnswer", FragmentManager.POP_BACK_STACK_INCLUSIVE)
                    }

                    editInterestsFragment -> {
                        subFm.popBackStack("editInterestsFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE)
                        switchVisibility(0)
                    }

                    collectionMapView -> {
                        when {
                            isRandomUserProfileActive -> {
                                subActive = profileRandomUserFragment
                                isRandomUserProfileActive = false
                            }
                            isSecondRandomUserProfileActive -> {
                                subActive = profileSecondRandomUserFragment
                                isSecondRandomUserProfileActive = false
                            }
                            else -> switchVisibility(0)
                        }
                        isCollectionMapViewActive = false
                        subFm.popBackStack("collectionMapView", FragmentManager.POP_BACK_STACK_INCLUSIVE)
                    }

                    secondImageFullSizeFragment -> {
                        subFm.popBackStack("secondImageFullSizeFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE)
                        subActive = profileSecondRandomUserFragment
                    }

                    addToItineraryFragment -> {
                        if (isFeedActive) {
                            switchVisibility(0)
                            isFeedActive = false
                        }
                        subFm.popBackStack("addToItineraryFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE)
                        subActive = imageFullSizeFragment
                    }

                    itineraryFragment -> {
                        if (itineraryFragment.allReviewsContainer.visibility == View.VISIBLE) {
                            itineraryFragment.allReviewsContainer.visibility = View.GONE
                        } else {
                            switchVisibility(0)
                            isItineraryActive = false
                            subFm.popBackStack("itineraryFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE)
                            subActive = imageFullSizeFragment
                        }
                    }

                    itineraryEditFragment -> {
                        subFm.popBackStack("itineraryEditFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE)
                        subActive = collectionGalleryFragment
                        itineraryEditFragment.step = 0
                    }

                    addImagesToItineraryFragment -> {
                        subFm.popBackStack("addImagesToItineraryFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE)
                        subActive = itineraryEditFragment
                    }

                    marketplacePurchasedFragment -> {
                        subFm.beginTransaction().hide(marketplacePurchasedFragment).commit()
                        switchVisibility(0)
                        isSavedItinerariesActive = false
                    }
                    buyItineraryFragment -> {
                        subFm.popBackStack("buyItineraryFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE)
                        subActive = itineraryFragment
                    }

                    addImagesToItineraryDayFragment -> {
                        subFm.popBackStack("addImagesToItineraryDayFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE)
                        subActive = collectionGalleryFragment
                    }

                    dayMapViewFragment -> {
                        subFm.popBackStack("dayMapViewFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE)
                        subActive = collectionGalleryFragment
                    }
                    bucketAndSharedItineraryPagerFragment -> {
                        subFm.popBackStack(
                            "bucketAndSharedItineraryPagerFragment",
                            FragmentManager.POP_BACK_STACK_INCLUSIVE
                        )
                        subActive = imageFullSizeFragment
                    }

                    joinSharedItineraryFragment -> {
                        subFm.popBackStack(
                            "joinSharedItineraryFragment",
                            FragmentManager.POP_BACK_STACK_INCLUSIVE
                        )
                        subActive = imageFullSizeFragment
                    }
                }

            fragmentsHaveBeenInitialized -> when (active) { // main frame is active

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
                    navigateToFeed()
                }

                boardFragment -> {
                    navigateToFeed()
                }

                marketplaceFragment -> {
                    navigateToFeed()
                }

                feedFragment -> super.onBackPressed()
            }
            else -> super.onBackPressed()
        }
    }


    private fun resetFragments() {
        closeKeyboard(this)

        isCollectionMapViewActive = false
        isCollectionGalleryActive = false
        isOpenedQuestionActive = false
        isEditAnswerActive = false
        isFeedNotificationsActive = false
        isBoardNotificationsActive = false
        isRandomUserProfileActive = false
        isSecondRandomUserProfileActive = false
        isFeedActive = false
        isItineraryActive = false
        isSavedItinerariesActive = false

        if (mainFrame.visibility == View.GONE) {
            switchVisibility(0)
        }

        for (i in 0 until subFm.backStackEntryCount) {
            subFm.popBackStack()
        }

        subFm.beginTransaction().hide(savedQuestionFragment).hide(boardNotificationsFragment)
            .hide(feedNotificationsFragment).hide(marketplacePurchasedFragment).commit()
    }


    private fun branchInitSession() {
        Branch.getInstance().initSession({ branchUniversalObject, _, error ->

            if (error == null) {
                if (branchUniversalObject != null) {

                    when (branchUniversalObject.contentMetadata.customMetadata["type"]) {
                        "user" -> collectProfile(branchUniversalObject)

                        "image" -> collectImage(branchUniversalObject)

                        "question" -> collectQuestion(branchUniversalObject)

                        "sharedItinerary" -> collectSharedItinerary(branchUniversalObject)
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
//        val menuItem = mBottomNav.menu.findItem(R.id.destination_feed)
//        menuItem.isChecked = true
        resetFragments()
    }

    private fun navigateToBoard() {
        fm.beginTransaction().hide(active).show(boardFragment).commit()
        active = boardFragment
//        val menuItem = mBottomNav.menu.findItem(R.id.destination_board)
//        menuItem.isChecked = true
        resetFragments()
        isFeedActive = false
    }

    private fun navigateToMarketplace() {
        fm.beginTransaction().hide(active).show(marketplaceFragment).commit()
        active = marketplaceFragment
//        val menuItem = mBottomNav.menu.findItem(R.id.destination_marketplace)
//        menuItem.isChecked = true
        resetFragments()
        isFeedActive = false
        isItineraryActive = false
    }


    fun navigateToProfile() {
        fm.beginTransaction().hide(active).show(profileLoggedInUserFragment).commit()
        active = profileLoggedInUserFragment
//        val menuItem = mBottomNav.menu.findItem(R.id.destination_profile_logged_in_user)
//        menuItem.isChecked = true
        resetFragments()
        isFeedActive = false
        profileLoggedInUserFragment.scrollView.fullScroll(View.FOCUS_UP)
    }

    private fun collectProfile(branchUniversalObject: BranchUniversalObject) {
        val uid = FirebaseAuth.getInstance().uid

        val profileId = branchUniversalObject.canonicalIdentifier

        if (uid == profileId) {
            navigateToProfile()
        } else {
            FirebaseDatabase.getInstance().getReference("/users/$profileId/profile")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {}

                    override fun onDataChange(p0: DataSnapshot) {
                        val user = p0.getValue(Users::class.java)
                        if (user != null) {
                            sharedViewModelRandomUser.randomUserObject.postValue(user)
                            subFm.beginTransaction().add(
                                R.id.feed_subcontents_frame_container,
                                profileRandomUserFragment,
                                "profileRandomUserFragment"
                            ).addToBackStack("profileRandomUserFragment").commit()
                            subActive = profileRandomUserFragment
                            switchVisibility(1)
                        }
                    }
                })
        }
    }


    private fun collectImage(branchUniversalObject: BranchUniversalObject) {

        FirebaseDatabase.getInstance().getReference("/images/${branchUniversalObject.canonicalIdentifier}/body")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {}

                override fun onDataChange(p0: DataSnapshot) {

                    val image = p0.getValue(Images::class.java)

                    if (image != null) {
                        sharedViewModelImage.sharedImageObject.postValue(image)

                        FirebaseDatabase.getInstance().getReference("/users/${image.photographer}/profile")
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onCancelled(p0: DatabaseError) {}

                                override fun onDataChange(p0: DataSnapshot) {
                                    val randomUser = p0.getValue(Users::class.java)
                                    if (randomUser != null) {
                                        sharedViewModelRandomUser.randomUserObject.postValue(randomUser)
                                        navigateToFeed()
                                        subFm.beginTransaction().add(
                                            R.id.feed_subcontents_frame_container,
                                            imageFullSizeFragment,
                                            "imageFullSizeFragment"
                                        ).addToBackStack("imageFullSizeFragment").commit()
                                        subActive = imageFullSizeFragment
                                        switchVisibility(1)
                                    }
                                }
                            })
                    }
                }
            })
    }


    private fun collectQuestion(branchUniversalObject: BranchUniversalObject) {

        FirebaseDatabase.getInstance().getReference("/questions/${branchUniversalObject.canonicalIdentifier}/main/body")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {}

                override fun onDataChange(p0: DataSnapshot) {

                    val question = p0.getValue(Question::class.java)

                    if (question != null) {
                        sharedViewModelQuestion.questionObject.postValue(question)

                        FirebaseDatabase.getInstance().getReference("/users/${question.author}/profile")
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onCancelled(p0: DatabaseError) {}

                                override fun onDataChange(p0: DataSnapshot) {
                                    val randomUser = p0.getValue(Users::class.java)
                                    if (randomUser != null) {
                                        sharedViewModelRandomUser.randomUserObject.postValue(randomUser)
                                        navigateToBoard()

                                        subFm.beginTransaction().add(
                                            R.id.feed_subcontents_frame_container,
                                            openedQuestionFragment,
                                            "openedQuestionFragment"
                                        )
                                            .addToBackStack("openedQuestionFragment").commit()
                                        subActive = openedQuestionFragment
                                        switchVisibility(1)
                                    }
                                }
                            })
                    }
                }
            })
    }

    private fun collectSharedItinerary(branchUniversalObject: BranchUniversalObject) {
        active = feedFragment

        FirebaseDatabase.getInstance().getReference("/sharedItineraries/${branchUniversalObject.canonicalIdentifier}")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {}

                override fun onDataChange(p0: DataSnapshot) {

                    sharedViewModelCollection.imageCollection.postValue(p0)
                    navigateToMarketplace()

                    subFm.beginTransaction().show(marketplacePurchasedFragment).commit()
                    isSavedItinerariesActive = true

                    if (p0.hasChild("/body/contributors/${currentUser.uid}") || p0.child("/body/contributors").childrenCount < 2) {

                        subFm.beginTransaction().add(
                            R.id.feed_subcontents_frame_container,
                            collectionGalleryFragment,
                            "collectionGalleryFragment"
                        ).addToBackStack("collectionGalleryFragment").commit()

                        subActive = collectionGalleryFragment
                    } else {
                        subFm.beginTransaction().add(
                            R.id.feed_subcontents_frame_container,
                            joinSharedItineraryFragment,
                            "joinSharedItineraryFragment"
                        ).addToBackStack("joinSharedItineraryFragment").commit()
                        subActive = joinSharedItineraryFragment
                    }
                    switchVisibility(1)
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