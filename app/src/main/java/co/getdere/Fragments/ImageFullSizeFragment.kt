package co.getdere.Fragments


import android.content.Context
import android.os.Bundle
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import co.getdere.Adapters.OpenPhotoPagerAdapter
import co.getdere.Interfaces.SharedViewModelImage
import co.getdere.MainActivity
import co.getdere.Models.Images
import co.getdere.R
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_image_full_size.view.*


class ImageFullSizeFragment : androidx.fragment.app.Fragment() {


    var viewPagerPosition = 0

    lateinit var sharedViewModelForImage: SharedViewModelImage

    lateinit var refImage: DatabaseReference
    lateinit var imageObject: Images
    lateinit var boxPagerAdapter: OpenPhotoPagerAdapter

    lateinit var goToMapBtn : ImageButton
    lateinit var goToSocial : ImageButton
    lateinit var viewPager : ViewPager


    override fun onAttach(context: Context) {
        super.onAttach(context)

        activity?.let {
            sharedViewModelForImage = ViewModelProviders.of(it).get(SharedViewModelImage::class.java)
        }

        arguments?.let {
            val safeArgs = ImageFullSizeFragmentArgs.fromBundle(it)

            val imageId = safeArgs.imageId

            refImage = FirebaseDatabase.getInstance().getReference("/images/feed/$imageId")

            refImage.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(p0: DataSnapshot) {

                    imageObject = p0.getValue(Images::class.java)!!

                    sharedViewModelForImage.sharedImageObject = imageObject
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

        (activity as MainActivity).mToolbar.visibility = View.GONE
        (activity as MainActivity).mBottomNav.visibility = View.GONE
        //hide the bottom nav and the tool bar when on full screen mode

        val mainImage = view.findViewById<ImageView>(R.id.image_full_image)
        goToMapBtn = view.findViewById<ImageButton>(R.id.image_full_go_to_map_btn)
        goToSocial = view.findViewById<ImageButton>(R.id.image_full_go_to_social)
setUpViewPager()

        goToMapBtn.setOnClickListener {
            changeFragmentInPager()
        }

        goToSocial.setOnClickListener {
            changeFragmentInPager()
        }


        arguments?.let {
            val safeArgs = ImageFullSizeFragmentArgs.fromBundle(it)

            val imageId = safeArgs.imageId

            refImage = FirebaseDatabase.getInstance().getReference("/images/feed/$imageId")

            refImage.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(p0: DataSnapshot) {

                    imageObject = p0.getValue(Images::class.java)!!
                    Picasso.get().load(Uri.parse(imageObject.image)).into(mainImage)

                }


            })

        }

    }

    private fun changeFragmentInPager(){

        if (viewPagerPosition==0){
            viewPager.currentItem = 1
            goToMapBtn.visibility = View.GONE
            goToSocial.visibility = View.VISIBLE
            viewPagerPosition = 1


        } else{
            viewPager.currentItem = 0
            goToMapBtn.visibility = View.VISIBLE
            goToSocial.visibility = View.GONE
            viewPagerPosition = 0
        }
    }

    override fun onResume() {
        super.onResume()
        setUpViewPager()
    }

    private fun setUpViewPager(){
        boxPagerAdapter = OpenPhotoPagerAdapter((activity as MainActivity).supportFragmentManager)
        viewPager = view!!.image_full_view_pager
        viewPager.adapter = boxPagerAdapter
    }


    override fun onDetach() {
        super.onDetach()

        (activity as MainActivity).mToolbar.visibility = View.VISIBLE
        (activity as MainActivity).mBottomNav.visibility = View.VISIBLE
    }
}
