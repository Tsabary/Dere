package co.getdere.fragments


import android.app.Activity
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import co.getdere.MainActivity
import co.getdere.R
import co.getdere.interfaces.DereMethods
import co.getdere.models.*
import co.getdere.viewmodels.SharedViewModelItinerary
import co.getdere.viewmodels.SharedViewModelRandomUser
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.peekandpop.shalskar.peekandpop.PeekAndPop
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener

import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.budget_includes_layout.view.*
import kotlinx.android.synthetic.main.feed_single_photo.view.*
import kotlinx.android.synthetic.main.fragment_itinerary.*
import kotlinx.android.synthetic.main.image_peek_itinerary.view.*
import kotlinx.android.synthetic.main.review_layout.view.*
import org.ocpsoft.prettytime.PrettyTime
import java.util.*


class ItineraryFragment : Fragment(), DereMethods {

    private lateinit var sharedViewModelItinerary: SharedViewModelItinerary

    val sampleImagesAdapter = GroupAdapter<ViewHolder>()
    val lastReviewsAdapter = GroupAdapter<ViewHolder>()
    val allReviewsAdapter = GroupAdapter<ViewHolder>()
    val budgetIncludesAdapter = GroupAdapter<ViewHolder>()

    val reviewsList = mutableListOf<SingleItineraryReview>()
    lateinit var allReviewsContainer: ConstraintLayout
    lateinit var itineraryBody: ItineraryBody
    lateinit var itineraryListing: ItineraryListing
    lateinit var itineraryContent: ItineraryInformational
    lateinit var itineraryBudget: ItineraryBudget

    val uid = FirebaseAuth.getInstance().uid

    var sumAllReviews = 0f
    var numAllReviews = 0f
    var oneStarReviews = 0f
    var twoStarReviews = 0f
    var threeStarReviews = 0f
    var fourStarReviews = 0f
    var fiveStarReviews = 0f

    lateinit var starBarBg: ConstraintLayout
    lateinit var starBar1Fg: ConstraintLayout
    lateinit var starBar2Fg: ConstraintLayout
    lateinit var starBar3Fg: ConstraintLayout
    lateinit var starBar4Fg: ConstraintLayout
    lateinit var starBar5Fg: ConstraintLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_itinerary, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = activity as MainActivity

        val dummyDescription = getString(R.string.dummy_itinerary_description)
        val dummyYoutubeVideo = getString(R.string.dummy_youtube_video)

        val purchaseBtn = itinerary_buy_cta_container
        val purchaseBtn2 = itinerary_buy_cta_container_2

//        val coverImage = itinerary_cover_image
        val title = itinerary_title
        val description = itinerary_description
        val location = itinerary_location
        val price = itinerary_price
        val priceBottom = itinerary_buy_price_bottom
        val youtubePlayer = itinerary_youtube_player
        val sampleImagesRecycler = itinerary_sample_photos_recycler
        val imageCount = itinerary_photo_count

        val includesFood = itinerary_includes_food_text
        val includesNightlife = itinerary_includes_nightlife_text
        val includesActivities = itinerary_includes_activities_text
        val includesNature = itinerary_includes_nature_text
        val includesAccommodation = itinerary_includes_accommodation_text
        val includesTransportation = itinerary_includes_transportation_text

        val foodContainer = itinerary_includes_food_container
        val nightlifeContainer = itinerary_includes_nightlife_container
        val natureContainer = itinerary_includes_nature_container
        val activitiesContainer = itinerary_includes_activities_container
        val accommodationContainer = itinerary_includes_accommodation_container
        val transportationContainer = itinerary_includes_transportation_container

        val budget = itinerary_budget
        val budgetAlsoIncludes = itinerary_budget_also_includes_text
        val budgetAlsoIncludesTitle = itinerary_budget_also_includes

        val aboutGuide = itinerary_guide_about

        val leaveReview = itinerary_leave_review
        val reviewContainer = itinerary_review_container
        val reviewCancel = itinerary_review_cancel
        val reviewSubmit = itinerrary_review_submit
        val reviewInput = itinerary_review_input
        val reviewStars = itinerary_rating_bar

        val readAllReviews = itinerary_read_more_reviews
        val dismissAllReviews = itinerary_all_reviews_dismiss
        allReviewsContainer = itinerary_all_reviews_container

        val latestReviewsRecycler = itinerary_last_reviews_recycler
        val allReviewsRecycler = itinerary_all_reviews_recycler
        val budgetIncludesRecycler = itinerary_budget_includes_recycler

        starBarBg = itinerary_1_star_background_bar
        starBar1Fg = itinerary_1_star_filling_bar
        starBar2Fg = itinerary_2_star_filling_bar
        starBar3Fg = itinerary_3_star_filling_bar
        starBar4Fg = itinerary_4_star_filling_bar
        starBar5Fg = itinerary_5_star_filling_bar

        sampleImagesRecycler.adapter = sampleImagesAdapter
        sampleImagesRecycler.layoutManager = GridLayoutManager(this.context, 4)

        latestReviewsRecycler.adapter = lastReviewsAdapter
        latestReviewsRecycler.layoutManager = LinearLayoutManager(this.context)

        allReviewsRecycler.adapter = allReviewsAdapter
        allReviewsRecycler.layoutManager = LinearLayoutManager(this.context)

        budgetIncludesRecycler.adapter = budgetIncludesAdapter
        budgetIncludesRecycler.layoutManager = GridLayoutManager(this.context, 2)

        readAllReviews.setOnClickListener {
            allReviewsContainer.visibility = View.VISIBLE
        }

        dismissAllReviews.setOnClickListener {
            allReviewsContainer.visibility = View.GONE
        }

        leaveReview.setOnClickListener {
            reviewContainer.visibility = View.VISIBLE
        }

        reviewCancel.setOnClickListener {
            reviewContainer.visibility = View.GONE
        }

        purchaseBtn.setOnClickListener {
            purchaseItinerary(activity)
        }

        purchaseBtn2.setOnClickListener {
            purchaseItinerary(activity)
        }

        reviewSubmit.setOnClickListener {
            if (reviewInput.text.isNotEmpty() && reviewStars.rating.toInt() != 0) {
                val reviewRef =
                    FirebaseDatabase.getInstance().getReference("/itineraries/${itineraryBody.id}/reviews").push()
                val newReview = ItineraryReview(
                    reviewRef.key!!,
                    reviewInput.text.toString(),
                    reviewStars.rating.toInt(),
                    uid!!,
                    System.currentTimeMillis()
                )

                reviewRef.child("body").setValue(newReview).addOnSuccessListener {

                    numAllReviews++
                    sumAllReviews += reviewStars.rating
                    val newRating = sumAllReviews / numAllReviews
                    FirebaseDatabase.getInstance().getReference("/itineraries/${itineraryBody.id}/listing/rating")
                        .setValue(newRating).addOnSuccessListener {
                            reviewInput.text.clear()
                            reviewStars.rating = 0f
                            reviewContainer.visibility = View.GONE
                            closeKeyboard(activity)

                        }
                }

            }
        }


        lifecycle.addObserver(youtubePlayer)

        activity.let {
            sharedViewModelItinerary = ViewModelProviders.of(it).get(SharedViewModelItinerary::class.java)

            youtubePlayer.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {

                override fun onReady(youTubePlayer: YouTubePlayer) {

                    sharedViewModelItinerary.itinerary.observe(activity, Observer { itineraries ->
                        itineraries?.let { itinerarySnapshot ->
                            lastReviewsAdapter.clear()
                            allReviewsAdapter.clear()
                            reviewsList.clear()

                            itineraryBody = itinerarySnapshot.child("body").getValue(ItineraryBody::class.java)!!
                            itineraryListing =
                                itinerarySnapshot.child("listing").getValue(ItineraryListing::class.java)!!
                            itineraryContent =
                                itinerarySnapshot.child("content").getValue(ItineraryInformational::class.java)!!
                            itineraryBudget =
                                itinerarySnapshot.child("budget").getValue(ItineraryBudget::class.java)!!

                            leaveReview.visibility = if (itinerarySnapshot.hasChild("buyers/$uid")) {
                                View.VISIBLE
                            } else {
                                View.GONE
                            }

                            if (itineraryListing != null) {
                                sampleImagesAdapter.clear()

                                youTubePlayer.cueVideo(
                                    if (itineraryListing.video.isNotEmpty()) {
                                        itineraryListing.video
                                    } else {
                                        dummyYoutubeVideo
                                    }, 0f
                                )

                                price.text = "($${itineraryListing.price})"
                                priceBottom.text = "($${itineraryListing.price})"

                                if (itineraryListing.sampleImages.isNotEmpty()) {
                                    sampleImagesRecycler.visibility = View.VISIBLE
                                    for (imagePath in itineraryListing.sampleImages) {
                                        val imagesRef =
                                            FirebaseDatabase.getInstance().getReference("/images/${imagePath.key}/body")

                                        imagesRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                            override fun onCancelled(p0: DatabaseError) {
                                            }

                                            override fun onDataChange(p0: DataSnapshot) {

                                                val image =
                                                    p0.getValue(Images::class.java)
                                                sampleImagesAdapter.add(SampleImages(image!!, activity))

                                            }
                                        })
                                    }
                                } else {
                                    sampleImagesRecycler.visibility = View.GONE
                                }

                            }

                            if (itineraryBody != null) {
                                itineraryBody = itineraryBody
                                listenToLastReviews()

                                title.text = itineraryBody.title

                                description.text = if (itineraryBody.description.isNotEmpty()) {
                                    itineraryBody.description
                                } else {
                                    dummyDescription
                                }

                                location.text = if (itineraryBody.locationName.isNotEmpty()) {
                                    itineraryBody.locationName
                                } else {
                                    "Dark Side, Moon"
                                }

                                imageCount.text = "${itineraryBody.images.size} locations"
                            }

                            if (itineraryContent != null) {
                                if (itineraryContent.aboutFood.isNotEmpty()) {
                                    foodContainer.visibility = View.VISIBLE
                                    includesFood.text = itineraryContent.aboutFood
                                } else {
                                    foodContainer.visibility = View.GONE
                                }

                                if (itineraryContent.aboutNightlife.isNotEmpty()) {
                                    nightlifeContainer.visibility = View.VISIBLE
                                    includesNightlife.text = itineraryContent.aboutNightlife
                                } else {
                                    nightlifeContainer.visibility = View.GONE
                                }

                                if (itineraryContent.aboutActivities.isNotEmpty()) {
                                    activitiesContainer.visibility = View.VISIBLE
                                    includesActivities.text = itineraryContent.aboutActivities
                                } else {
                                    activitiesContainer.visibility = View.GONE
                                }

                                if (itineraryContent.aboutNature.isNotEmpty()) {
                                    natureContainer.visibility = View.VISIBLE
                                    includesNature.text = itineraryContent.aboutNature
                                } else {
                                    natureContainer.visibility = View.GONE
                                }

                                if (itineraryContent.aboutAccommodation.isNotEmpty()) {
                                    accommodationContainer.visibility = View.VISIBLE
                                    includesAccommodation.text = itineraryContent.aboutAccommodation
                                } else {
                                    accommodationContainer.visibility = View.GONE
                                }

                                if (itineraryContent.aboutTransportation.isNotEmpty()) {
                                    transportationContainer.visibility = View.VISIBLE
                                    includesTransportation.text = itineraryContent.aboutTransportation
                                } else {
                                    transportationContainer.visibility = View.GONE
                                }

                                aboutGuide.text = itineraryContent.aboutAuthor
                            }

                            if (itineraryBudget != null) {

                                budgetIncludesAdapter.clear()

                                budget.text = "$${itineraryBudget.budget}"

                                if (itineraryBudget.food) {
                                    budgetIncludesAdapter.add(BudgetIncludes("Food"))
                                }
                                if (itineraryBudget.nightlife) {
                                    budgetIncludesAdapter.add(BudgetIncludes("Nightlife"))
                                }
                                if (itineraryBudget.activities) {
                                    budgetIncludesAdapter.add(BudgetIncludes("Activities"))
                                }
                                if (itineraryBudget.accommodation) {
                                    budgetIncludesAdapter.add(BudgetIncludes("Accommodation"))
                                }
                                if (itineraryBudget.transportation) {
                                    budgetIncludesAdapter.add(BudgetIncludes("Transportation"))
                                }

                                if (itineraryBudget.other.isNotEmpty()) {
                                    budgetAlsoIncludes.visibility = View.VISIBLE
                                    budgetAlsoIncludesTitle.visibility = View.VISIBLE
                                    budgetAlsoIncludes.text = itineraryBudget.other
                                } else {
                                    budgetAlsoIncludes.visibility = View.GONE
                                    budgetAlsoIncludesTitle.visibility = View.GONE
                                }
                            }
                        }
                    })
                }
            })
        }
    }


    private fun purchaseItinerary(activity: MainActivity) {
        if (itineraryBody.creator != uid) {

            if (itineraryListing.price > 0) {
                activity.subFm.beginTransaction().hide(activity.subActive)
                    .add(R.id.feed_subcontents_frame_container, activity.buyItineraryFragment, "buyItineraryFragment")
                    .addToBackStack("buyItineraryFragment").commit()
                activity.subActive = activity.buyItineraryFragment
            } else {
                FirebaseDatabase.getInstance().getReference("/itineraries/${itineraryBody.id}/buyers/$uid")
                    .setValue(true).addOnSuccessListener {

                        val newPurchasedItineraryRef =
                            FirebaseDatabase.getInstance().getReference("/sharedItineraries").push()



                        if (uid != null && newPurchasedItineraryRef.key != null) {

                            val newPurchasedItinerary = SharedItineraryBody(
                                itineraryBody.id,
                                newPurchasedItineraryRef.key!!,
                                false,
                                itineraryBody.creator,
                                mapOf(uid to true),
                                itineraryBody.title,
                                itineraryBody.description,
                                itineraryBody.images,
                                itineraryBody.days,
                                itineraryBody.images,
                                itineraryBody.days,
                                itineraryBody.startDay,
                                itineraryBody.locationId,
                                itineraryBody.locationName,
                                itineraryListing.price
                            )


                            newPurchasedItineraryRef.child("body").setValue(newPurchasedItinerary)
                                .addOnSuccessListener {

                                    val userSharedItinerariesRef = FirebaseDatabase.getInstance()
                                        .getReference("/users/$uid/sharedItineraries/${newPurchasedItineraryRef.key}")

                                    userSharedItinerariesRef.setValue(true).addOnSuccessListener {
                                        Toast.makeText(
                                            this.context,
                                            "Itinerary purchased successfully",
                                            Toast.LENGTH_SHORT
                                        )
                                            .show()
                                        activity.marketplacePurchasedFragment.listenToItineraries()
                                        activity.subFm.beginTransaction().show(activity.marketplacePurchasedFragment)
                                            .commit()
                                        activity.subFm.popBackStack(
                                            "itineraryFragment",
                                            FragmentManager.POP_BACK_STACK_INCLUSIVE
                                        )
                                    }
                                }
                        }

                    }
            }
        } else {
            Toast.makeText(this.context, "Can't buy your own itinerary", Toast.LENGTH_SHORT).show()
        }
    }

    private fun listenToLastReviews() {

        allReviewsAdapter.clear()
        lastReviewsAdapter.clear()

        oneStarReviews = 0f
        twoStarReviews = 0f
        threeStarReviews = 0f
        fourStarReviews = 0f
        fiveStarReviews = 0f

        val reviewsRef = FirebaseDatabase.getInstance().getReference("/itineraries/${itineraryBody.id}/reviews")
        reviewsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {

                if (p0.hasChildren()) {
                    for (review in p0.children) {
                        val reviewObject = review.child("body").getValue(ItineraryReview::class.java)
                        if (reviewObject != null) {
                            reviewsList.add(SingleItineraryReview(reviewObject, activity as MainActivity))

                            when (reviewObject.rating) {
                                1 -> {
                                    oneStarReviews++
                                    sumAllReviews += 1f
                                }
                                2 -> {
                                    twoStarReviews++
                                    sumAllReviews += 2f
                                }
                                3 -> {
                                    threeStarReviews++
                                    sumAllReviews += 3f
                                }
                                4 -> {
                                    fourStarReviews++
                                    sumAllReviews += 4f
                                }
                                5 -> {
                                    fiveStarReviews++
                                    sumAllReviews += 5f
                                }
                            }
                        }
                    }

                    numAllReviews =
                        oneStarReviews + twoStarReviews + threeStarReviews + fourStarReviews + fiveStarReviews

                    if (numAllReviews > 0) {

                        val oneStarPercentage = oneStarReviews / numAllReviews
                        val twoStarPercentage = twoStarReviews / numAllReviews
                        val threeStarPercentage = threeStarReviews / numAllReviews
                        val fourStarPercentage = fourStarReviews / numAllReviews
                        val fiveStarPercentage = fiveStarReviews / numAllReviews

                        if (oneStarReviews > 0) {
                            (starBar1Fg.layoutParams as ConstraintLayout.LayoutParams).width =
                                (starBarBg.width * oneStarPercentage).toInt()
                            starBar1Fg.visibility = View.VISIBLE
                        } else {
                            starBar1Fg.visibility = View.GONE
                        }

                        if (twoStarReviews > 0) {
                            (starBar2Fg.layoutParams as ConstraintLayout.LayoutParams).width =
                                (starBarBg.width * twoStarPercentage).toInt()
                            starBar2Fg.visibility = View.VISIBLE
                        } else {
                            starBar2Fg.visibility = View.GONE
                        }

                        if (threeStarReviews > 0) {
                            (starBar3Fg.layoutParams as ConstraintLayout.LayoutParams).width =
                                (starBarBg.width * threeStarPercentage).toInt()
                            starBar3Fg.visibility = View.VISIBLE

                        } else {
                            starBar3Fg.visibility = View.GONE
                        }

                        if (fourStarReviews > 0) {
                            (starBar4Fg.layoutParams as ConstraintLayout.LayoutParams).width =
                                (starBarBg.width * fourStarPercentage).toInt()
                            starBar4Fg.visibility = View.VISIBLE
                        } else {
                            starBar4Fg.visibility = View.GONE
                        }

                        if (fiveStarReviews > 0) {
                            (starBar5Fg.layoutParams as ConstraintLayout.LayoutParams).width =
                                (starBarBg.width * fiveStarPercentage).toInt()
                            starBar5Fg.visibility = View.VISIBLE
                        } else {
                            starBar5Fg.visibility = View.GONE
                        }

                        itinerary_1_star_percentage.text = "${(oneStarPercentage * 100).toInt()}%"
                        itinerary_2_star_percentage.text = "${(twoStarPercentage * 100).toInt()}%"
                        itinerary_3_star_percentage.text = "${(threeStarPercentage * 100).toInt()}%"
                        itinerary_4_star_percentage.text = "${(fourStarPercentage * 100).toInt()}%"
                        itinerary_5_star_percentage.text = "${(fiveStarPercentage * 100).toInt()}%"

                    }

                    var numOfReviews = 0
                    if (reviewsList.size > 3) {
                        numOfReviews = 2
                        itinerary_read_more_reviews.visibility = View.VISIBLE
                    } else {
                        numOfReviews = reviewsList.size - 1
                        itinerary_read_more_reviews.visibility = View.GONE
                    }

                    for (index in 0..numOfReviews) {
                        reviewsList.reversed()
                        val review = reviewsList[index]
                        lastReviewsAdapter.add(review)
                    }
                    allReviewsAdapter.clear()
                    allReviewsAdapter.addAll(reviewsList.reversed())


                }

            }
        })
    }

    companion object {
        fun newInstance(): ItineraryFragment = ItineraryFragment()
    }
}

class SampleImages(val image: Images, val activity: Activity) : Item<ViewHolder>() {

    lateinit var peekAndPop: PeekAndPop
    lateinit var peekView: View


    override fun getLayout(): Int {
        return R.layout.feed_single_photo
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {

        val dip = 8f
        val r = viewHolder.root.resources
        val px = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dip,
            r.displayMetrics
        )

        viewHolder.itemView.feed_single_photo_card.radius = px

        peekAndPop = PeekAndPop.Builder(activity)
            .peekLayout(R.layout.image_peek_itinerary)
            .longClickViews(viewHolder.itemView)
            .build();

        peekView = peekAndPop.peekView
        val imageView = peekView.image_peek_itinerary_image
        peekAndPop.setOnGeneralActionListener(object : PeekAndPop.OnGeneralActionListener {
            override fun onPop(p0: View?, p1: Int) {

            }

            override fun onPeek(p0: View?, p1: Int) {
                (imageView.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio = image.ratio
                Glide.with(viewHolder.root.context).load(image.imageBig).into(imageView)
            }

        })


        Glide.with(viewHolder.root.context).load(image.imageSmall).into(viewHolder.itemView.feed_single_photo_photo)
    }
}

class SingleItineraryReview(private val review: ItineraryReview, val activity: MainActivity) : Item<ViewHolder>(),
    DereMethods {

    lateinit var sharedViewModelRandomUser: SharedViewModelRandomUser

    val authorRef = FirebaseDatabase.getInstance().getReference("/users/${review.author}/profile")
    val uid = FirebaseAuth.getInstance().uid


    override fun getLayout(): Int {
        return R.layout.review_layout
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {

        activity.let {
            sharedViewModelRandomUser = ViewModelProviders.of(activity).get(SharedViewModelRandomUser::class.java)
        }

        viewHolder.itemView.itinerary_review_content.text = review.content
        viewHolder.itemView.itinerary_review_timestamp.text = PrettyTime().format(Date(review.timestamp))


        val authorImage = viewHolder.itemView.itinerary_review_author_image
        val authorName = viewHolder.itemView.itinerary_review_author_name
        val authorReputation = viewHolder.itemView.itinerary_review_author_reputation


        val star1 = viewHolder.itemView.itinerary_review_stars_1
        val star2 = viewHolder.itemView.itinerary_review_stars_2
        val star3 = viewHolder.itemView.itinerary_review_stars_3
        val star4 = viewHolder.itemView.itinerary_review_stars_4
        val star5 = viewHolder.itemView.itinerary_review_stars_5

        when (review.rating) {

            1 -> {
                star5.visibility = View.VISIBLE
                star4.visibility = View.GONE
                star3.visibility = View.GONE
                star2.visibility = View.GONE
                star1.visibility = View.GONE
            }

            2 -> {
                star5.visibility = View.VISIBLE
                star4.visibility = View.VISIBLE
                star3.visibility = View.GONE
                star2.visibility = View.GONE
                star1.visibility = View.GONE
            }

            3 -> {
                star5.visibility = View.VISIBLE
                star4.visibility = View.VISIBLE
                star3.visibility = View.VISIBLE
                star2.visibility = View.GONE
                star1.visibility = View.GONE
            }

            4 -> {
                star5.visibility = View.VISIBLE
                star4.visibility = View.VISIBLE
                star3.visibility = View.VISIBLE
                star2.visibility = View.VISIBLE
                star1.visibility = View.GONE
            }

            5 -> {
                star5.visibility = View.VISIBLE
                star4.visibility = View.VISIBLE
                star3.visibility = View.VISIBLE
                star2.visibility = View.VISIBLE
                star1.visibility = View.VISIBLE
            }
        }



        authorRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {

                val author = p0.getValue(Users::class.java)
                Glide.with(viewHolder.root.context).load(author!!.image).into(authorImage)
                authorName.text = author.name
                authorReputation.text = "(${numberCalculation(author.reputation)})"

                authorImage.setOnClickListener {
                    goToProfile(author)
                }

                authorName.setOnClickListener {
                    goToProfile(author)
                }

                authorReputation.setOnClickListener {
                    goToProfile(author)
                }

            }
        })
    }

    fun goToProfile(user: Users) {
        if (user.uid != uid) {
            sharedViewModelRandomUser.randomUserObject.postValue(user)
            activity.subFm.beginTransaction().add(
                R.id.feed_subcontents_frame_container,
                activity.profileRandomUserFragment,
                "profileRandomUserFragment"
            ).addToBackStack("profileRandomUserFragment")
                .commit()
            activity.subActive = activity.profileRandomUserFragment
        } else {
            activity.navigateToProfile()
        }
    }
}

class BudgetIncludes(val includes: String) : Item<ViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.budget_includes_layout
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.budget_includes_text.text = includes
    }

}