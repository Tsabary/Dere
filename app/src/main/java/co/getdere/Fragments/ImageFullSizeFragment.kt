package co.getdere.Fragments


import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import co.getdere.Adapters.OpenPhotoPagerAdapter
import co.getdere.ViewModels.SharedViewModelImage
import co.getdere.ViewModels.SharedViewModelRandomUser
import co.getdere.MainActivity
import co.getdere.Models.Images
import co.getdere.Models.Users
import co.getdere.OtherClasses.SwipeLockableViewPager
import co.getdere.R
import com.bumptech.glide.Glide
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_image_full_size.view.*


class ImageFullSizeFragment : androidx.fragment.app.Fragment() {


    var viewPagerPosition = 0

    lateinit var sharedViewModelForImage: SharedViewModelImage
    lateinit var sharedViewModelForRandomUser: SharedViewModelRandomUser

    lateinit var refImage: DatabaseReference
    lateinit var boxPagerAdapter: OpenPhotoPagerAdapter

    lateinit var goToMapBtn: ImageButton
    lateinit var goToSocial: ImageButton
    lateinit var viewPager: SwipeLockableViewPager


    override fun onAttach(context: Context) {
        super.onAttach(context)

        activity?.let {
            sharedViewModelForImage = ViewModelProviders.of(it).get(SharedViewModelImage::class.java)

            sharedViewModelForRandomUser = ViewModelProviders.of(it).get(SharedViewModelRandomUser::class.java)
        }

        arguments?.let {
            val safeArgs = ImageFullSizeFragmentArgs.fromBundle(it)

            val imageId = safeArgs.imageId

            refImage = FirebaseDatabase.getInstance().getReference("/images/feed/$imageId")

            refImage.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(p0: DataSnapshot) {

                    val imageObject = p0.getValue(Images::class.java)!!

                    sharedViewModelForImage.sharedImageObject.postValue(imageObject)

                    val refRandomUser =
                        FirebaseDatabase.getInstance().getReference("/users/${imageObject.photographer}/profile")

                    refRandomUser.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onCancelled(p0: DatabaseError) {
                        }

                        override fun onDataChange(p0: DataSnapshot) {

                            val randomUserObject = p0.getValue(Users::class.java)!!

                            sharedViewModelForRandomUser.randomUserObject.postValue(randomUserObject)
                        }

                    })
                }
            })
        }

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_image_full_size, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mainImage = view.findViewById<ImageView>(R.id.image_full_image)
        goToMapBtn = view.findViewById<ImageButton>(R.id.image_full_go_to_map_btn)
        goToSocial = view.findViewById<ImageButton>(R.id.image_full_go_to_social)
        setUpViewPager()



        sharedViewModelForImage.sharedImageObject.observe(this, Observer {
            it?.let { image ->
                Glide.with(this).load(image.imageBig).into(mainImage)
            }
        }
        )

        (activity as MainActivity).mToolbar.visibility = View.GONE
        (activity as MainActivity).mBottomNav.visibility = View.GONE
        //hide the bottom nav and the tool bar when on full screen mode



        goToMapBtn.setOnClickListener {
            changeFragmentInPager()
        }

        goToSocial.setOnClickListener {
            changeFragmentInPager()
        }


    }

    private fun changeFragmentInPager() {

        if (viewPagerPosition == 0) {
            viewPager.currentItem = 1
            goToMapBtn.visibility = View.GONE
            goToSocial.visibility = View.VISIBLE
            viewPagerPosition = 1


        } else {
            viewPager.currentItem = 0
            goToMapBtn.visibility = View.VISIBLE
            goToSocial.visibility = View.GONE
            viewPagerPosition = 0
        }
    }


    private fun setUpViewPager() {
        boxPagerAdapter = OpenPhotoPagerAdapter((activity as MainActivity).supportFragmentManager)
        val adapterOBJ= OpenPhotoPagerAdapter(childFragmentManager)
        viewPager = view!!.image_full_view_pager
        viewPager.adapter = adapterOBJ
        viewPager.setSwipePagingEnabled(false)

    }


    override fun onDetach() {
        super.onDetach()

        (activity as MainActivity).mToolbar.visibility = View.VISIBLE
        (activity as MainActivity).mBottomNav.visibility = View.VISIBLE

        val emptyUser = Users()
        val emptyImage = Images()

        sharedViewModelForRandomUser.randomUserObject.postValue(emptyUser)
        sharedViewModelForImage.sharedImageObject.postValue(emptyImage)

    }
}
