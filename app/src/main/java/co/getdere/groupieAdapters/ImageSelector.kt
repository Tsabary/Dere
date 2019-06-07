package co.getdere.groupieAdapters

import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import co.getdere.MainActivity
import co.getdere.models.Images
import co.getdere.R
import co.getdere.viewmodels.SharedViewModelAnswerImages
import co.getdere.viewmodels.SharedViewModelItineraryDayStrings
import co.getdere.viewmodels.SharedViewModelItineraryImages
import com.bumptech.glide.Glide
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.feed_single_photo.view.*
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions


class ImageSelector(val image: Images, val activity: MainActivity, val source : String, val day : Int) : Item<ViewHolder>() {

    private lateinit var sharedViewModelAnswerImages : SharedViewModelAnswerImages
    private lateinit var sharedViewModelItineraryImages: SharedViewModelItineraryImages
    private lateinit var sharedViewModelItineraryDayStrings: SharedViewModelItineraryDayStrings

    override fun getLayout(): Int {
        return R.layout.feed_single_photo
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {

        val whiteCover = viewHolder.itemView.feed_single_photo_chosen
//        val photo = viewHolder.itemView.feed_single_photo_photo

        activity.let {
            sharedViewModelAnswerImages = ViewModelProviders.of(it).get(SharedViewModelAnswerImages::class.java)
            sharedViewModelItineraryImages = ViewModelProviders.of(it).get(SharedViewModelItineraryImages::class.java)
            sharedViewModelItineraryDayStrings = ViewModelProviders.of(it).get(SharedViewModelItineraryDayStrings::class.java)

            when(source){
                "answer"-> {
                    sharedViewModelAnswerImages.imageList.observe(activity, Observer { mutableList ->
                        mutableList?.let { existingImageList ->

                            if (checkExistence(existingImageList)){
                                whiteCover.visibility = View.VISIBLE
                            } else {
                                whiteCover.visibility = View.GONE
                            }


                        }
                    })
                }

                "itinerary"-> {
                    sharedViewModelItineraryImages.imageList.observe(activity, Observer { mutableList ->
                        mutableList?.let { existingImageList ->

                            if (checkExistence(existingImageList)){
                                whiteCover.visibility = View.VISIBLE
                            } else {
                                whiteCover.visibility = View.GONE
                            }


                        }
                    })
                }

                "itineraryDay"-> {
                    sharedViewModelItineraryDayStrings.daysList.observe(activity, Observer { mutableList ->
                        mutableList?.let { existingImageList ->

                            if (existingImageList[day].containsKey(image.id)){
                                whiteCover.visibility = View.VISIBLE
                            } else {
                                whiteCover.visibility = View.GONE
                            }


                        }
                    })
                }
            }


        }


        val requestOption = RequestOptions()
            .placeholder(R.color.gray500).centerCrop()

        Glide.with(viewHolder.root.context).load(image.imageSmall).transition(DrawableTransitionOptions.withCrossFade())
            .apply(requestOption).into(viewHolder.itemView.feed_single_photo_photo)
    }

    private fun checkExistence(list : MutableList<String>) : Boolean{
        var imageMatch = 0

        for (singleImage in list){
            if (singleImage == image.id)
                imageMatch ++
        }

        return imageMatch != 0
    }
}


//                    whiteCover.setOnClickListener {
//
////                        val iterator = existingImageList.listIterator()
////
////                        while (iterator.hasNext()){
////                            val currentImage = iterator.next()
////                            if (currentImage.id == image.id){
////                                existingImageList.remove(currentImage)
////                                sharedViewModelAnswerImages.daysList.postValue(existingImageList)
////                            }
////                        }
//
////                        for (singleImage in existingImageList){
////                            if (singleImage.id == image.id){
////                                existingImageList.remove(singleImage)
////                                sharedViewModelAnswerImages.daysList.postValue(existingImageList)
////                            }
////                        }
//
//                        activity.subFm.beginTransaction().hide(activity.subActive).show(activity.editAnswerFragment)
//                            .commit()
//                        activity.subActive = activity.editAnswerFragment
//                    }
//
//                    photo.setOnClickListener {
//                        existingImageList.add(image.id)
//                        sharedViewModelAnswerImages.daysList.postValue(existingImageList)
//                        activity.subFm.beginTransaction().hide(activity.subActive).show(activity.editAnswerFragment)
//                            .commit()
//                        activity.subActive = activity.editAnswerFragment
//                    }

//                    viewHolder.itemView.setOnClickListener {
//
//                        if (existingImageList.isEmpty()){
//                            existingImageList.add(image)
//                            sharedViewModelAnswerImages.daysList.postValue(existingImageList)
//                            Log.d("daysList", existingImageList.toString())
//
//                        } else {
//                            if (imageMatch > 0){
//                                existingImageList.remove(image)
//                                sharedViewModelAnswerImages.daysList.postValue(existingImageList)
//                                Log.d("daysList", existingImageList.toString())
//                            } else {
//                                existingImageList.add(image)
//                                sharedViewModelAnswerImages.daysList.postValue(existingImageList)
//                                Log.d("daysList", existingImageList.toString())
//                            }
//
////                            if (existingImageList.contains(image)){
////                                existingImageList.remove(image)
////                                sharedViewModelAnswerImages.daysList.postValue(existingImageList)
////                                Log.d("daysList", existingImageList.toString())
////                            } else {
////                                existingImageList.add(image)
////                                sharedViewModelAnswerImages.daysList.postValue(existingImageList)
////                                Log.d("daysList", existingImageList.toString())
////                            }
//                        }
//
//                        activity.subFm.beginTransaction().hide(activity.subActive).show(activity.answerFragment)
//                            .commit()
//                        activity.subActive = activity.answerFragment
//                    }
