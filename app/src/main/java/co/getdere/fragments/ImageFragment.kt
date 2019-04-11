package co.getdere.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import co.getdere.R
import co.getdere.viewmodels.SharedViewModelImage
import com.bumptech.glide.Glide
import io.branch.indexing.BranchUniversalObject
import io.branch.referral.util.ContentMetadata
import io.branch.referral.util.LinkProperties
import kotlinx.android.synthetic.main.fragment_image.*
import org.ocpsoft.prettytime.PrettyTime


class ImageFragment : Fragment() {

    lateinit var sharedViewModelForImage: SharedViewModelImage


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_image, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        activity?.let {
            sharedViewModelForImage = ViewModelProviders.of(it).get(SharedViewModelImage::class.java)
        }




        sharedViewModelForImage.sharedImageObject.observe(this, Observer {
            it?.let { image ->
                    Glide.with(this).load(image.imageBig).into(image_frame_image)
            }
        })


    }

    companion object {
        fun newInstance(): ImageFragment = ImageFragment()
    }


}
