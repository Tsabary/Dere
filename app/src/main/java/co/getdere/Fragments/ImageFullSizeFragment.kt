package co.getdere.Fragments


import android.os.Bundle
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.navigation.fragment.findNavController
import co.getdere.Adapters.OpenPhotoPagerAdapter
import co.getdere.MainActivity
import co.getdere.Models.Images
import co.getdere.Models.Users
import co.getdere.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_image_full_size.view.*
import org.ocpsoft.prettytime.PrettyTime
import java.util.*


class ImageFullSizeFragment : androidx.fragment.app.Fragment() {


    lateinit var refImage: DatabaseReference
    lateinit var imageObject: Images
    lateinit var boxPagerAdapter: OpenPhotoPagerAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_image_full_size, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as MainActivity).mToolbar.visibility = View.GONE
        (activity as MainActivity).mBottomNav.visibility = View.GONE
        //hide the bottom nav and the tool bar when on full screen mode

        val mainImage = view.findViewById<ImageView>(R.id.image_full_image)
        boxPagerAdapter = OpenPhotoPagerAdapter((activity as MainActivity).supportFragmentManager)
        val viewPager = view.image_full_view_pager
        viewPager.adapter = boxPagerAdapter





        arguments?.let {
            val safeArgs = ImageFullSizeFragmentArgs.fromBundle(it)

            val imageId = safeArgs.imageId

            refImage = FirebaseDatabase.getInstance().getReference("/images/feed/$imageId")


            refImage.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(p0: DataSnapshot) {

                    val image = p0.getValue(Images::class.java)
                    setImageObjectFromListener(image!!)

                    Picasso.get().load(Uri.parse(imageObject.image)).into(mainImage)

                }


            })

        }


    }

    fun setImageObjectFromListener(image: Images) {
        imageObject = image
    }

    override fun onDetach() {
        super.onDetach()

        (activity as MainActivity).mToolbar.visibility = View.VISIBLE
        (activity as MainActivity).mBottomNav.visibility = View.VISIBLE
    }
}
