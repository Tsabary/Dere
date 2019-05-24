package co.getdere

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
import co.getdere.viewmodels.*
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

    lateinit var mainFrame: FrameLayout
    lateinit var subFrame: FrameLayout

    val fm = supportFragmentManager
    val fmTransaction = fm.beginTransaction()
    val subFm = supportFragmentManager
    val subFmTransaction = subFm.beginTransaction()


    lateinit var active: Fragment
    lateinit var subActive: Fragment

    var answerObject = Answers()
    var boardNotificationsCount = MutableLiveData<Int>()
    var feedNotificationsCount = MutableLiveData<Int>()

    var isCollectionMapViewActive = false
    var isBucketGalleryActive = false
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
                }

                R.id.destination_board -> {
                    navigateToBoard()
                }
                R.id.destination_marketplace -> {
                    navigateToMarketplace()
                }
                R.id.destination_profile_logged_in_user -> {
                    navigateToProfile()
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
        imageFullSizeFragment.showLocation.setImageResource(R.drawable.location)
        imageFullSizeFragment.layoutScroll.fullScroll(View.FOCUS_UP)

    }

    private fun resetSecondImageExpended() {
        sharedViewModelImage.sharedImageObject.postValue(Images())
        secondImageFullSizeFragment.actionsContainer.visibility = View.INVISIBLE
        secondImageFullSizeFragment.optionsContainer.visibility = View.GONE
        secondImageFullSizeFragment.deleteEditContainer.visibility = View.VISIBLE
        secondImageFullSizeFragment.deleteContainer.visibility = View.GONE
        secondImageFullSizeFragment.showLocation.setImageResource(R.drawable.location)
        secondImageFullSizeFragment.layoutScroll.fullScroll(View.FOCUS_UP)
    }

    override fun onBackPressed() {

        if (mainFrame.visibility == View.GONE) { // subframe is active

            when (subActive) {

                imageFullSizeFragment -> {
                    when {
                        isCollectionMapViewActive && isBucketGalleryActive -> {
                            subFm.beginTransaction().hide(subActive).show(collectionGalleryFragment).commit()
                            subActive = collectionGalleryFragment
                            collectionGalleryFragment.galleryViewPager.currentItem = 1
                            resetImageExpended()
                            isCollectionMapViewActive = false
                        }

                        isCollectionMapViewActive && !isBucketGalleryActive -> {
                            subFm.beginTransaction().hide(subActive).show(collectionMapView).commit()
                            subActive = collectionMapView
                            resetImageExpended()
                            isCollectionMapViewActive = false
                        }

                        isBucketGalleryActive && !isCollectionMapViewActive -> {
                            subFm.beginTransaction().hide(subActive).show(collectionGalleryFragment).commit()
                            subActive = collectionGalleryFragment
                            collectionGalleryFragment.galleryViewPager.currentItem = 0
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
                        isRandomUserProfileActive -> {
                            subFm.beginTransaction().hide(subActive).show(profileRandomUserFragment).commit()
                            subActive = profileRandomUserFragment
                            resetImageExpended()
                        }

                        else -> {
                            switchVisibility(0)
                            resetImageExpended()
                        }
                    }
                }
                addToBucketFragment -> {
                    if (isFeedActive) {
                        switchVisibility(0)
                        isFeedActive = false
                    }
                    subFm.beginTransaction().remove(subActive).show(imageFullSizeFragment).commit()
                    subActive = imageFullSizeFragment
                }

                profileRandomUserFragment -> {
                    when {
                        isFeedActive -> {
                            navigateToFeed()
                            isFeedActive = false
                            isRandomUserProfileActive = false
                        }
                        isFeedNotificationsActive -> {
                            subFm.beginTransaction().hide(subActive).show(feedNotificationsFragment).commit()
                            subActive = feedNotificationsFragment
                            isRandomUserProfileActive = false
                        }

                        isBoardNotificationsActive -> {
                            subFm.beginTransaction().hide(subActive).show(boardNotificationsFragment).commit()
                            subActive = boardNotificationsFragment
                            isRandomUserProfileActive = false
                        }
                        isOpenedQuestionActive -> {
                            subFm.beginTransaction().hide(subActive).show(openedQuestionFragment).commit()
                            subActive = openedQuestionFragment
                            isRandomUserProfileActive = false
                        }

                        isItineraryActive -> {
                            subFm.beginTransaction().hide(subActive).show(itineraryFragment).commit()
                            subActive = itineraryFragment
                        }
                        else -> {
                            imageFullSizeFragment.layoutScroll.fullScroll(View.FOCUS_UP)
                            subFm.beginTransaction().hide(subActive).show(imageFullSizeFragment).commit()
                            subActive = imageFullSizeFragment

//                            sharedViewModelSecondRandomUser.randomUserObject.postValue(Users())
                            isRandomUserProfileActive = false
                        }
                    }
                }
                profileSecondRandomUserFragment -> {
                    if (isOpenedQuestionActive) {
                        subFm.beginTransaction().hide(subActive).show(openedQuestionFragment).commit()
                        subActive = openedQuestionFragment
                    } else {
                        subFm.beginTransaction().hide(subActive).show(imageFullSizeFragment).commit()
                        subActive = imageFullSizeFragment
                    }
                }

                feedNotificationsFragment -> {
                    switchVisibility(0)
                    isFeedNotificationsActive = false
                }

                openedQuestionFragment -> {
                    if (isBoardNotificationsActive) {
                        subFm.beginTransaction().hide(subActive).show(boardNotificationsFragment).commit()
                        subActive = boardNotificationsFragment
                        isOpenedQuestionActive = false
                        openedQuestionFragment.deleteBox.visibility = View.GONE
                    } else {
                        switchVisibility(0)
                        isOpenedQuestionActive = false
                        openedQuestionFragment.deleteBox.visibility = View.GONE
                    }
                }

                boardNotificationsFragment -> {
                    switchVisibility(0)
                }

                savedQuestionFragment -> {
                    switchVisibility(0)
                }

                answerCommentFragment -> {
                    subFm.beginTransaction().remove(subActive).show(openedQuestionFragment).commit()
                    subActive = openedQuestionFragment
                }

                answerFragment -> {
                    subFm.beginTransaction().hide(subActive).show(openedQuestionFragment).commit()
                    subActive = openedQuestionFragment
                }

                newQuestionFragment -> {
                    switchVisibility(0)
                }

                collectionGalleryFragment -> {
//                    subFm.beginTransaction().hide(collectionGalleryFragment).show(imageFullSizeFragment).commit()
//                    subActive = imageFullSizeFragment
//                    collectionGalleryFragment.pagerAdapter.notifyDataSetChanged()
                    if (isSavedItinerariesActive){
                        subFm.beginTransaction().hide(subActive).show(marketplacePurchasedFragment).commit()
                        subActive = marketplacePurchasedFragment
                    } else {
                        switchVisibility(0)
                        isBucketGalleryActive = false
                        collectionGalleryFragment.mapButton.setImageResource(R.drawable.world)
                    }
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

                    when {
                        isRandomUserProfileActive -> {
                            subFm.beginTransaction().hide(subActive).show(profileRandomUserFragment).commit()
                            subActive = profileRandomUserFragment
                            isRandomUserProfileActive = false
                        }
                        isSecondRandomUserProfileActive -> {
                            subFm.beginTransaction().hide(subActive).show(profileSecondRandomUserFragment).commit()
                            subActive = profileSecondRandomUserFragment
                            isSecondRandomUserProfileActive = false
                        }
                        else -> switchVisibility(0)
                    }

                }

                secondImageFullSizeFragment -> {
                    subFm.beginTransaction().hide(secondImageFullSizeFragment).show(profileRandomUserFragment).commit()
                    subActive = profileRandomUserFragment
                    resetSecondImageExpended()
                }

                addToItineraryFragment -> {
                    if (isFeedActive) {
                        switchVisibility(0)
                        isFeedActive = false
                    }
                    subFm.beginTransaction().remove(subActive).show(imageFullSizeFragment).commit()
                    subActive = imageFullSizeFragment
                }

                itineraryFragment -> {
                    switchVisibility(0)
                    isItineraryActive = false
                    subFm.beginTransaction().remove(subActive).show(imageFullSizeFragment).commit()
                    subActive = imageFullSizeFragment
                }

                itineraryEditFragment -> {
                    subFm.beginTransaction().remove(subActive).remove(addImagesToItineraryFragment)
                        .show(collectionGalleryFragment).commit()
                    subActive = collectionGalleryFragment

                    //need to save the data to the itinerary
                }

                addImagesToItineraryFragment -> {
                    subFm.beginTransaction().hide(subActive).show(itineraryEditFragment).commit()
                    subActive = itineraryEditFragment
                }

                marketplacePurchasedFragment -> {
                    subFm.beginTransaction().remove(marketplacePurchasedFragment).commit()
                    subActive = imageFullSizeFragment
                    switchVisibility(0)
                    isSavedItinerariesActive = false
                }

            }
        } else if (fragmentsHaveBeenInitialized) {

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

        } else {
            super.onBackPressed()
        }

    }

    private fun resetFragments() {
        closeKeyboard(this)

        openedQuestionFragment.deleteBox.visibility = View.GONE
        collectionGalleryFragment.mapButton.setImageResource(R.drawable.world)

        isCollectionMapViewActive = false
        isBucketGalleryActive = false
        isOpenedQuestionActive = false
        isEditAnswerActive = false
        isFeedNotificationsActive = false
        isBoardNotificationsActive = false
        isRandomUserProfileActive = false
        isSecondRandomUserProfileActive = false
        isFeedActive = false
        isItineraryActive = false
        isSavedItinerariesActive = false


        sharedViewModelImage.sharedImageObject.postValue(Images())
        sharedViewModelRandomUser.randomUserObject.postValue(Users())
        sharedViewModelQuestion.questionObject.postValue(Question())

        if (mainFrame.visibility == View.GONE) {
            switchVisibility(0)
        }

        imageFullSizeFragment.showLocation.setImageResource(R.drawable.location)

        subFm.beginTransaction().remove(addToBucketFragment).commit()
        subFm.beginTransaction().remove(addToItineraryFragment).commit()
        subFm.beginTransaction().remove(itineraryFragment).commit()
        subFm.beginTransaction().remove(marketplacePurchasedFragment).commit()
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

        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid/profile")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {

                if (p0.getValue(Users::class.java) != null) {
                    sharedViewModelCurrentUser.currentUserObject = p0.getValue(Users::class.java)!!
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


        val uid = FirebaseAuth.getInstance().uid
        val interestsRef = FirebaseDatabase.getInstance().getReference("/users/$uid/interests")
        interestsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.hasChildren()) {
                    fm.beginTransaction().add(R.id.feed_frame_container, feedFragment, "feedFragment")
                        .commitAllowingStateLoss()

                    active = feedFragment

                    fragmentsHaveBeenInitialized = true

                } else {
                    fm.beginTransaction().add(R.id.feed_frame_container, feedFragment, "feedFragment")
                        .hide(feedFragment).commitAllowingStateLoss()
                    fm.beginTransaction().add(R.id.feed_frame_container, onBoardingFragment, "onBoardingFragment")
                        .commitAllowingStateLoss()

                    active = onBoardingFragment

                    fragmentsHaveBeenInitialized = true

                }
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



        subFm.beginTransaction()
            .add(R.id.feed_subcontents_frame_container, imageFullSizeFragment, "imageFullSizeFragment")
            .commitAllowingStateLoss()
        subFm.beginTransaction()
            .add(R.id.feed_subcontents_frame_container, profileRandomUserFragment, "profileRandomUserFragment")
            .hide(profileRandomUserFragment).commitAllowingStateLoss()
        subFm.beginTransaction()
            .add(R.id.feed_subcontents_frame_container, profileSecondRandomUserFragment, "profileRandomUserFragment")
            .hide(profileSecondRandomUserFragment).commitAllowingStateLoss()
//        subFm.beginTransaction()
//            .add(R.id.feed_subcontents_frame_container, addToBucketFragment, "addToBucketFragment").hide(addToBucketFragment).commit()
        subFm.beginTransaction()
            .add(R.id.feed_subcontents_frame_container, openedQuestionFragment, "openedQuestionFragment")
            .hide(openedQuestionFragment).commitAllowingStateLoss()
        subFm.beginTransaction()
            .add(R.id.feed_subcontents_frame_container, feedNotificationsFragment, "feedNotificationsFragment")
            .hide(feedNotificationsFragment).commitAllowingStateLoss()
        subFm.beginTransaction()
            .add(R.id.feed_subcontents_frame_container, boardNotificationsFragment, "boardNotificationsFragment")
            .hide(boardNotificationsFragment).commitAllowingStateLoss()
        subFm.beginTransaction()
            .add(R.id.feed_subcontents_frame_container, savedQuestionFragment, "savedQuestionFragment")
            .hide(savedQuestionFragment).commitAllowingStateLoss()
//        subFm.beginTransaction()
//            .add(R.id.feed_subcontents_frame_container, answerCommentFragment, "answerCommentFragment")
//            .hide(answerCommentFragment).commitAllowingStateLoss()
        subFm.beginTransaction()
            .add(R.id.feed_subcontents_frame_container, answerFragment, "answerFragment")
            .hide(answerFragment).commitAllowingStateLoss()
        subFm.beginTransaction()
            .add(R.id.feed_subcontents_frame_container, newQuestionFragment, "newQuestionFragment")
            .hide(newQuestionFragment).commitAllowingStateLoss()
        fm.beginTransaction().add(R.id.feed_subcontents_frame_container, editProfileFragment, "editProfileFragment")
            .hide(editProfileFragment).commitAllowingStateLoss()
        fm.beginTransaction()
            .add(R.id.feed_subcontents_frame_container, collectionGalleryFragment, "collectionGalleryFragment")
            .hide(collectionGalleryFragment).commitAllowingStateLoss()
        fm.beginTransaction().add(R.id.feed_subcontents_frame_container, addImageToAnswer, "addImageToAnswer")
            .hide(addImageToAnswer).commitAllowingStateLoss()
        fm.beginTransaction().add(R.id.feed_subcontents_frame_container, imagePostEditFragment, "imagePostEditFragment")
            .hide(imagePostEditFragment).commitAllowingStateLoss()
        fm.beginTransaction().add(R.id.feed_subcontents_frame_container, webViewFragment, "webViewFragment")
            .hide(webViewFragment).commitAllowingStateLoss()
        fm.beginTransaction().add(R.id.feed_subcontents_frame_container, editQuestionFragment, "editQuestionFragment")
            .hide(editQuestionFragment).commitAllowingStateLoss()
        fm.beginTransaction().add(R.id.feed_subcontents_frame_container, editAnswerFragment, "editAnswerFragment")
            .hide(editAnswerFragment).commitAllowingStateLoss()
        fm.beginTransaction().add(R.id.feed_subcontents_frame_container, editInterestsFragment, "editInterestsFragment")
            .hide(editInterestsFragment).commitAllowingStateLoss()
        fm.beginTransaction().add(R.id.feed_subcontents_frame_container, collectionMapView, "collectionMapView")
            .hide(collectionMapView).commitAllowingStateLoss()
        fm.beginTransaction()
            .add(R.id.feed_subcontents_frame_container, secondImageFullSizeFragment, "collectionMapView")
            .hide(secondImageFullSizeFragment).commitAllowingStateLoss()



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
        isFeedActive = false
    }

    private fun navigateToMarketplace() {
        fm.beginTransaction().hide(active).show(marketplaceFragment).commit()
        active = marketplaceFragment
        val menuItem = mBottomNav.menu.findItem(R.id.destination_marketplace)
        menuItem.isChecked = true
//            subFm.beginTransaction().hide(subActive).show(itineraryFragment).commit()
//            subActive = openedQuestionFragment
        resetFragments()
        isFeedActive = false
        isItineraryActive = false


        subFm.beginTransaction().hide(subActive).show(imageFullSizeFragment).commit()
        subActive = imageFullSizeFragment
    }


    fun navigateToProfile() {
        fm.beginTransaction().hide(active).show(profileLoggedInUserFragment).commit()
        active = profileLoggedInUserFragment
        val menuItem = mBottomNav.menu.findItem(R.id.destination_profile_logged_in_user)
        menuItem.isChecked = true
        subFm.beginTransaction().hide(subActive).show(imageFullSizeFragment).commit()
        subActive = imageFullSizeFragment
        resetFragments()
        isFeedActive = false
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

