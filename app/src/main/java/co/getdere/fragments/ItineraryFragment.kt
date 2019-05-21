package co.getdere.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import co.getdere.MainActivity
import co.getdere.R
import co.getdere.interfaces.DereMethods
import co.getdere.models.Images
import co.getdere.models.Itineraries
import co.getdere.models.ItineraryReview
import co.getdere.models.Users
import co.getdere.viewmodels.SharedViewModelItinerary
import co.getdere.viewmodels.SharedViewModelRandomUser
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.feed_single_photo.view.*
import kotlinx.android.synthetic.main.fragment_itinerary.*
import kotlinx.android.synthetic.main.review_layout.view.*
import org.ocpsoft.prettytime.PrettyTime
import java.util.*


class ItineraryFragment : Fragment(), DereMethods {

    private lateinit var sharedViewModelItinerary: SharedViewModelItinerary

    val sampleImagesAdapter = GroupAdapter<ViewHolder>()
    val lastReviewsAdapter = GroupAdapter<ViewHolder>()
    val allReviewsAdapter = GroupAdapter<ViewHolder>()

    val reviewsList = mutableListOf<SingleItineraryReview>()

    lateinit var itineraryObject: Itineraries

    val uid = FirebaseAuth.getInstance().uid

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


        val coverImage = itinerary_cover_image
        val title = itinerary_title
        val description = itinerary_description
        val location = itinerary_location
        val price = itinerary_price
        val youtubePlayer = itinerary_youtube_player
        val sampleImagesRecycler = itinerary_sample_photos_recycler
        val imageCount = itinerary_photo_count

        val includesFood = itinerary_includes_food_text
        val includesDrinks = itinerary_includes_drinks_text
        val includesActivities = itinerary_includes_activities_text
        val includesNature = itinerary_includes_nature_text
        val includesAccommodation = itinerary_includes_accommodation_text
        val includesTransportation = itinerary_includes_transportation_text

        val leaveReview = itinerary_leave_review
        val reviewContainer = itinerary_review_container
        val reviewCancel = itinerary_review_cancel
        val reviewSubmit = itinerrary_review_submit
        val reviewInput = itinerary_review_input
        val reviewStars = itinerary_rating_bar

        val readAllReviews = itinerary_read_more_reviews
        val dismissAllReviews = itinerary_all_reviews_dismiss
        val allReviewsContainer = itinerary_all_reviews_container

        val latestReviewsRecycler = itinerary_last_reviews_recycler
        val allReviewsRecycler = itinerary_all_reviews_recycler

        starBarBg = itinerary_1_star_background_bar
        starBar1Fg = itinerary_1_star_filling_bar
        starBar2Fg = itinerary_2_star_filling_bar
        starBar3Fg = itinerary_3_star_filling_bar
        starBar4Fg = itinerary_4_star_filling_bar
        starBar5Fg = itinerary_5_star_filling_bar

        latestReviewsRecycler.adapter = lastReviewsAdapter
        val lastReviewLayoutManager = LinearLayoutManager(this.context)
        latestReviewsRecycler.layoutManager = lastReviewLayoutManager

        allReviewsRecycler.adapter = allReviewsAdapter
        val allReviewLayoutManager = LinearLayoutManager(this.context)
        allReviewsRecycler.layoutManager = allReviewLayoutManager

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

        reviewSubmit.setOnClickListener {
            if (reviewInput.text.isNotEmpty() && reviewStars.rating.toInt() != 0) {
                val reviewRef =
                    FirebaseDatabase.getInstance().getReference("/itineraries/${itineraryObject.id}/reviews").push()
                val newReview = ItineraryReview(
                    reviewRef.key!!,
                    reviewInput.text.toString(),
                    reviewStars.rating.toInt(),
                    uid!!,
                    System.currentTimeMillis()
                )

                reviewRef.child("body").setValue(newReview).addOnSuccessListener {
                    reviewInput.text.clear()
                    reviewStars.rating = 0f
                    reviewContainer.visibility = View.GONE
                }

                closeKeyboard(activity)
            }
        }


        lifecycle.addObserver(youtubePlayer)


        activity.let {
            sharedViewModelItinerary = ViewModelProviders.of(it).get(SharedViewModelItinerary::class.java)

            youtubePlayer.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {

                override fun onReady(youTubePlayer: YouTubePlayer) {

                    sharedViewModelItinerary.itinerary.observe(activity, Observer { itineraries ->
                        itineraries?.let { itinerary ->
                            itineraryObject = itinerary
                            listenToLastReviews()

                            Glide.with(activity).load(
                                if (itinerary.coverImage.isNotEmpty()) {
                                    itinerary.coverImage
                                } else {
                                    R.drawable.dummy_photo
                                }
                            ).into(coverImage)

                            title.text = itinerary.title

                            description.text = if (itinerary.description.isNotEmpty()) {
                                itinerary.description
                            } else {
                                dummyDescription
                            }

                            location.text = if (itinerary.location.isNotEmpty()) {
                                itinerary.location
                            } else {
                                "Dark Side, Moon"
                            }

                            imageCount.text = "${itinerary.images.size} locations"

                            price.text = "$${itinerary.price}"

                            youTubePlayer.cueVideo(
                                if (itinerary.video.isNotEmpty()) {
                                    itinerary.video
                                } else {
                                    dummyYoutubeVideo
                                }, 0f
                            )

                            if (itinerary.sampleImages.isNotEmpty()) {
                                sampleImagesRecycler.visibility = View.VISIBLE

                                val imagesRef = FirebaseDatabase.getInstance().getReference("/images")

                                imagesRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onCancelled(p0: DatabaseError) {
                                    }

                                    override fun onDataChange(p0: DataSnapshot) {


                                        for (imagePath in itinerary.sampleImages) {

                                            val image = p0.child("$imagePath/body").getValue(Images::class.java)
                                            sampleImagesAdapter.add(SampleImages(image!!))
                                        }
                                    }
                                })
                            } else {
                                sampleImagesRecycler.visibility = View.GONE
                            }

                            includesFood.text = "Best street food you can find in anatalia it so good and cheap"
                            includesDrinks.text = "Best street food you can find in anatalia it so good and cheap"
                            includesActivities.text = "Best street food you can find in anatalia it so good and cheap"
                            includesNature.text = "Best street food you can find in anatalia it so good and cheap"
                            includesAccommodation.text = "Best street food you can find in anatalia it so good and cheap"
                            includesTransportation.text = "Best street food you can find in anatalia it so good and cheap"
                        }
                    })
                }
            })
        }
    }

    private fun listenToLastReviews() {

        allReviewsAdapter.clear()
        lastReviewsAdapter.clear()
        sampleImagesAdapter.clear()

        oneStarReviews = 0f
        twoStarReviews = 0f
        threeStarReviews = 0f
        fourStarReviews = 0f
        fiveStarReviews = 0f

        val reviewsRef = FirebaseDatabase.getInstance().getReference("/itineraries/${itineraryObject.id}/reviews")
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
                                1 -> oneStarReviews++
                                2 -> twoStarReviews++
                                3 -> threeStarReviews++
                                4 -> fourStarReviews++
                                5 -> fiveStarReviews++
                            }
                        }
                    }

                    val numAllReviews =
                        oneStarReviews + twoStarReviews + threeStarReviews + fourStarReviews + fiveStarReviews

                    if (numAllReviews > 0) {

                        val oneStarPercentage = oneStarReviews / numAllReviews
                        val twoStarPercentage = twoStarReviews / numAllReviews
                        val threeStarPercentage = threeStarReviews / numAllReviews
                        val fourStarPercentage = fourStarReviews / numAllReviews
                        val fiveStarPercentage = fiveStarReviews / numAllReviews

                        (starBar1Fg.layoutParams as ConstraintLayout.LayoutParams).width = if (oneStarReviews > 0) {
                            (starBarBg.width * oneStarPercentage).toInt()
                        } else {
                            1
                        }

                        (starBar2Fg.layoutParams as ConstraintLayout.LayoutParams).width = if (twoStarReviews > 0) {
                            (starBarBg.width * twoStarPercentage).toInt()
                        } else {
                            1
                        }

                        (starBar3Fg.layoutParams as ConstraintLayout.LayoutParams).width = if (threeStarReviews > 0) {
                            (starBarBg.width * threeStarPercentage).toInt()
                        } else {
                            1
                        }

                        (starBar4Fg.layoutParams as ConstraintLayout.LayoutParams).width = if (fourStarReviews > 0) {
                            (starBarBg.width * fourStarPercentage).toInt()
                        } else {
                            1
                        }

                        (starBar5Fg.layoutParams as ConstraintLayout.LayoutParams).width = if (fiveStarReviews > 0) {
                            (starBarBg.width * fiveStarPercentage).toInt()
                        } else {
                            1
                        }

                        itinerary_1_star_percentage.text = "${(oneStarPercentage * 100).toInt()}%"
                        itinerary_2_star_percentage.text = "${(twoStarPercentage * 100).toInt()}%"
                        itinerary_3_star_percentage.text = "${(threeStarPercentage * 100).toInt()}%"
                        itinerary_4_star_percentage.text = "${(fourStarPercentage * 100).toInt()}%"
                        itinerary_5_star_percentage.text = "${(fiveStarPercentage * 100).toInt()}%"

                    }

                    val numOfReviews = if (reviewsList.size > 3) {
                        2
                    } else {
                        reviewsList.size - 1
                    }

                    for (index in 0..numOfReviews) {
                        reviewsList.reversed()
                        val review = reviewsList[index]
                        lastReviewsAdapter.add(review)
                    }

                    allReviewsAdapter.addAll(reviewsList.reversed())


                }

            }
        })
    }
}

class SampleImages(val image: Images) : Item<ViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.feed_single_photo
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        Glide.with(viewHolder.root.context).load(image.imageSmall).into(viewHolder.itemView.feed_single_photo_photo)
    }
}

class SingleItineraryReview(private val review: ItineraryReview, val activity: MainActivity) : Item<ViewHolder>() {

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
                authorReputation.text = "(${author.reputation})"

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
            activity.subFm.beginTransaction().hide(activity.subActive).show(activity.profileRandomUserFragment).commit()
            activity.subActive = activity.profileRandomUserFragment
        } else {
            activity.navigateToProfile()
        }
    }
}
