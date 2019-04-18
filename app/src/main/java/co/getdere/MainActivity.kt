package co.getdere

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import co.getdere.fragments.*
import co.getdere.models.Answers
import co.getdere.models.Images
import co.getdere.models.Question
import co.getdere.models.Users
import co.getdere.registerLogin.RegisterActivity
import co.getdere.viewmodels.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.pusher.pushnotifications.PushNotifications
import com.pusher.pushnotifications.PushNotificationsInstance
import io.branch.indexing.BranchUniversalObject
import io.branch.referral.Branch
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    lateinit var mBottomNav: BottomNavigationView

    lateinit var sharedViewModelCurrentUser: SharedViewModelCurrentUser
    lateinit var sharedViewModelImage: SharedViewModelImage
    lateinit var sharedViewModelRandomUser: SharedViewModelRandomUser
    lateinit var sharedViewModelQuestion: SharedViewModelQuestion
    lateinit var sharedViewModelBucket: SharedViewModelBucket

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


    lateinit var mainFrame: FrameLayout
    lateinit var subFrame: FrameLayout

    val fm = supportFragmentManager
    val subFm = supportFragmentManager

    lateinit var active: Fragment
    lateinit var subActive: Fragment

    var answerObject = Answers()

    var isBucketGalleryActive = false
    var isOpenedQuestionActive = false


//    lateinit var notificationManager: NotificationManager
//    lateinit var notificationChannel: NotificationChannel
//    lateinit var builder: Notification.Builder
//    val channelId = "co.getdere"
//    var description = "My notification"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        PushNotifications.start(applicationContext, "8286cd5e-eaaa-4d81-a57b-0f4d985b0a47")
        PushNotifications.addDeviceInterest("hello")

//        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//        val intent = Intent(applicationContext, MainActivity::class.java)
//        val pendingIntent = PendingIntent.getActivity(this, 0 , intent, PendingIntent.FLAG_UPDATE_CURRENT)
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            notificationChannel = NotificationChannel(channelId, description, NotificationManager.IMPORTANCE_HIGH)
//            notificationChannel.enableLights(true)
//            notificationChannel.lightColor = Color.GREEN
//            notificationChannel.enableVibration(true)
//            notificationManager.createNotificationChannel(notificationChannel)
//
//            builder = Notification.Builder(this, channelId)
//                .setContentTitle("My title youtube")
//                .setContentText("My text youtube")
//                .setSmallIcon(R.drawable.pin_icon)
//                .setContentIntent(pendingIntent)
//        } else {
//            builder = Notification.Builder(this)
//                .setContentTitle("My title youtube")
//                .setContentText("My text youtube")
//                .setSmallIcon(R.drawable.pin_icon)
//                .setContentIntent(pendingIntent)
//        }
//
//        notificationManager.notify(0, builder.build())






        val instanceId = "YOUR_INSTANCE_ID_HERE"
        val secretKey = "YOUR_SECRET_KEY_HERE"

        val beamsClient = PushNotifications(instanceId, secretKey)

        val interests = listOf("donuts", "pizza")
        val publishRequest = hashMapOf(
            "fcm" to hashMapOf("notification" to hashMapOf("title" to "hello", "body" to "Hello world"))
        )

        beamsClient.publish(interests, publishRequest)






        mBottomNav = feed_bottom_nav

        mBottomNav.isClickable = false

        sharedViewModelCurrentUser = ViewModelProviders.of(this).get(SharedViewModelCurrentUser::class.java)
        sharedViewModelQuestion = ViewModelProviders.of(this).get(SharedViewModelQuestion::class.java)

        FirebaseApp.initializeApp(this)
        checkIfLoggedIn()


        sharedViewModelImage = ViewModelProviders.of(this).get(SharedViewModelImage::class.java)
        sharedViewModelImage.sharedImageObject.postValue(Images())

        sharedViewModelRandomUser = ViewModelProviders.of(this).get(SharedViewModelRandomUser::class.java)
        sharedViewModelRandomUser.randomUserObject.postValue(Users())

        sharedViewModelBucket = ViewModelProviders.of(this).get(SharedViewModelBucket::class.java)

        subFrame = findViewById(R.id.feed_subcontents_frame_container)

        mainFrame = findViewById(R.id.feed_frame_container)

        switchVisibility(0)

    }

    private fun setupBottomNav() {

        mBottomNav.isClickable = true

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

                    isOpenedQuestionActive = false
                    isBucketGalleryActive = false

                    sharedViewModelImage.sharedImageObject.postValue(Images())
                    sharedViewModelRandomUser.randomUserObject.postValue(Users())
                }

                R.id.destination_board -> {

                    fm.beginTransaction().hide(active).show(boardFragment).commit()
                    active = boardFragment

                    val menuItem = mBottomNav.menu.findItem(R.id.destination_board)
                    menuItem.isChecked = true


                    if (mainFrame.visibility == View.GONE) {
                        switchVisibility(0)
                    }

                    subFm.beginTransaction().hide(subActive).show(openedQuestionFragment).commit()
                    subActive = openedQuestionFragment

                    isOpenedQuestionActive = false
                    isBucketGalleryActive = false

                    sharedViewModelImage.sharedImageObject.postValue(Images())
                    sharedViewModelRandomUser.randomUserObject.postValue(Users())

                }
                R.id.destination_profile_logged_in_user -> {

                    fm.beginTransaction().hide(active).show(profileLoggedInUserFragment).commit()
                    active = profileLoggedInUserFragment

                    val menuItem = mBottomNav.menu.findItem(R.id.destination_profile_logged_in_user)
                    menuItem.isChecked = true


                    if (mainFrame.visibility == View.GONE) {
                        switchVisibility(0)
                    }

                    subFm.beginTransaction().hide(subActive).show(imageFullSizeFragment).commit()
                    subActive = imageFullSizeFragment

                    isOpenedQuestionActive = false
                    isBucketGalleryActive = false

                    sharedViewModelImage.sharedImageObject.postValue(Images())
                    sharedViewModelRandomUser.randomUserObject.postValue(Users())

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
                    if (isBucketGalleryActive) {
                        subFm.beginTransaction().hide(subActive).show(bucketGalleryFragment).commit()
                        subActive = bucketGalleryFragment
                        sharedViewModelImage.sharedImageObject.postValue(Images())

                    } else if (isOpenedQuestionActive) {
                        subFm.beginTransaction().hide(subActive).show(openedQuestionFragment).commit()
                        subActive = openedQuestionFragment
                        sharedViewModelImage.sharedImageObject.postValue(Images())
                    } else {
                        switchVisibility(0)
                        sharedViewModelImage.sharedImageObject.postValue(Images())
                    }
                }
                bucketFragment -> {

                    subFm.beginTransaction().hide(subActive).show(imageFullSizeFragment).commit()
                    fm.beginTransaction().detach(profileLoggedInUserFragment).attach(profileLoggedInUserFragment)
                        .commit()

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
                    isOpenedQuestionActive = false
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
//                    sharedViewModelBucket.sharedBucketId.postValue(DataSnapshot(DatabaseReference(),""))
                }

                editProfileFragment -> {
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

                setupBottomNav()
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
        bucketGalleryFragment = BucketGalleryFragment()
        addImageToAnswer = AddImageToAnswerFragment()


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
        fm.beginTransaction().add(R.id.feed_subcontents_frame_container, bucketGalleryFragment, "bucketGalleryFragment")
            .hide(bucketGalleryFragment).commit()
        fm.beginTransaction().add(R.id.feed_subcontents_frame_container, addImageToAnswer, "addImageToAnswer")
            .hide(addImageToAnswer).commit()


        subActive = imageFullSizeFragment

    }


    override fun onStart() {
        super.onStart()

        println("branch on start")


        Branch.getInstance().initSession({ branchUniversalObject, referringParams, error ->

            if (error == null) {
                println("branch no error")


                if (branchUniversalObject != null) {

                    val type = branchUniversalObject.contentMetadata.customMetadata["type"]

                    when (type) {

                        "image" -> collectImage(branchUniversalObject)

                        "question" -> collectQuestion(branchUniversalObject)

                    }


                }


            } else {
                println("branch definitely error" + error.message)

            }


        }, this.intent.data, this)


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
                    }

                })
            }


        })


    }

    fun sendNotification(){

    }


    public override fun onNewIntent(intent: Intent) {
        this.intent = intent
    }


    companion object {
        fun newInstance(): MainActivity = MainActivity()
    }

}

