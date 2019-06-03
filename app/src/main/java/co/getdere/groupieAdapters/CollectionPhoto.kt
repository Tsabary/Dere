package co.getdere.groupieAdapters

import android.app.Activity
import android.view.View
import android.widget.ImageButton
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import co.getdere.MainActivity
import co.getdere.models.Images
import co.getdere.R
import co.getdere.viewmodels.SharedViewModelAnswerImages
import co.getdere.viewmodels.SharedViewModelItineraryDayImages
import co.getdere.viewmodels.SharedViewModelItineraryImages
import com.bumptech.glide.Glide
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.answer_photo.view.*


class CollectionPhoto(val image: Images, val activity: Activity, private val source : String, val dayNumber : Int) : Item<ViewHolder>() {

    lateinit var sharedViewModelAnswerImages: SharedViewModelAnswerImages
    lateinit var sharedViewModelItineraryImages: SharedViewModelItineraryImages
    lateinit var sharedViewModelItineraryDayImages: SharedViewModelItineraryDayImages

    override fun getLayout(): Int {
        return R.layout.answer_photo
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {

        activity.let {
            sharedViewModelAnswerImages =
                ViewModelProviders.of(activity as MainActivity).get(SharedViewModelAnswerImages::class.java)

            sharedViewModelItineraryImages =
                ViewModelProviders.of(activity as MainActivity).get(SharedViewModelItineraryImages::class.java)

            sharedViewModelItineraryDayImages =
                ViewModelProviders.of(activity as MainActivity).get(SharedViewModelItineraryDayImages::class.java)
        }

        val requestOption = RequestOptions()
            .placeholder(R.color.gray500).centerCrop()

        Glide.with(viewHolder.root.context).load(image.imageSmall).transition(DrawableTransitionOptions.withCrossFade())
            .apply(requestOption).into(viewHolder.itemView.answer_photo_photo)

        when(source){
            "answer" -> {
                sharedViewModelAnswerImages.imageList.observe(activity as MainActivity, Observer {
                    it?.let { existingImageList ->

                        viewHolder.itemView.answer_photo_remove.setOnClickListener {
                            existingImageList.remove(image.id)
                            sharedViewModelAnswerImages.imageList.postValue(existingImageList)
                        }
                    }
                })
            }

            "itinerary" -> {
                sharedViewModelItineraryImages.imageList.observe(activity as MainActivity, Observer {
                    it?.let { existingImageList ->

                        viewHolder.itemView.answer_photo_remove.setOnClickListener {
                            existingImageList.remove(image.id)
                            sharedViewModelItineraryImages.imageList.postValue(existingImageList)
                        }
                    }
                })
            }

            "itineraryDay" -> {
                sharedViewModelItineraryDayImages.imageList.observe(activity as MainActivity, Observer {
                    it?.let { existingImageList ->

                        viewHolder.itemView.answer_photo_remove.setOnClickListener {
                            existingImageList[dayNumber].remove(image.id)
                            sharedViewModelItineraryDayImages.imageList.postValue(existingImageList)
                        }
                    }
                })
            }
        }
    }
}
