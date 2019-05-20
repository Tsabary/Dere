package co.getdere.fragments


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import co.getdere.MainActivity

import co.getdere.R
import co.getdere.viewmodels.SharedViewModelItinerary
import com.bumptech.glide.Glide
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerListener
import kotlinx.android.synthetic.main.fragment_itinerary.*


class ItineraryFragment : Fragment() {

    private lateinit var sharedViewModelItinerary: SharedViewModelItinerary


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

        val coverImage = itinerary_cover_image
        val title = itinerary_title
        val description = itinerary_description
        val youtubePlayer = itinerary_youtube_player
        lifecycle.addObserver(youtubePlayer)


//        youtubePlayer.addYouTubePlayerListener(object : AbstractYouTubePlayerListener(){
//
//            override fun onReady(youTubePlayer: YouTubePlayer) {
////                            youTubePlayer.loadVideo(if(itinerary.video.isNotEmpty()){itinerary.video}else{getString(R.string.dummy_youtube_video)}, 0f)
//                youTubePlayer.cueVideo("S0Q4gqBUs7c", 0f)
//            }
//        })

        activity.let {
            sharedViewModelItinerary = ViewModelProviders.of(it).get(SharedViewModelItinerary::class.java)

            youtubePlayer.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {

                override fun onReady(youTubePlayer: YouTubePlayer) {

                    sharedViewModelItinerary.itinerary.observe(activity, Observer { itineraries ->
                        itineraries?.let { itinerary ->
                            Glide.with(activity).load(
                                if (itinerary.coverimage.isNotEmpty()) {
                                    itinerary.coverimage
                                } else {
                                    R.drawable.dummy_photo
                                }
                            ).into(coverImage)
                            title.text = itinerary.title
                            description.text = if (itinerary.description.isNotEmpty()) {
                                itinerary.description
                            } else {
                                getString(R.string.dummy_itinerary_description)
                            }

                            youTubePlayer.cueVideo(
                                if (itinerary.video.isNotEmpty()) {
                                    itinerary.video
                                } else {
                                    getString(R.string.dummy_youtube_video)
                                }, 0f
                            )

                        }
                    })


                }
            })


        }

    }
}
