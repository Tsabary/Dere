package co.getdere.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import co.getdere.R
import co.getdere.models.Users
import co.getdere.viewmodels.SharedViewModelCurrentUser
import co.getdere.viewmodels.SharedViewModelImage
import com.bumptech.glide.Glide
import com.google.firebase.database.FirebaseDatabase
import io.branch.indexing.BranchUniversalObject
import io.branch.referral.util.ContentMetadata
import io.branch.referral.util.LinkProperties
import kotlinx.android.synthetic.main.fragment_image.*
import org.ocpsoft.prettytime.PrettyTime


class ImageFragment : Fragment() {

    lateinit var sharedViewModelForImage: SharedViewModelImage
    lateinit var currentUser: Users


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_image, container, false)



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.let {
            sharedViewModelForImage = ViewModelProviders.of(it).get(SharedViewModelImage::class.java)
            currentUser = ViewModelProviders.of(it).get(SharedViewModelCurrentUser::class.java).currentUserObject
        }

        val imagePrivacy = image_privacy_text
        val imagePrivacyContainer = image_privacy_container

        sharedViewModelForImage.sharedImageObject.observe(this, Observer {
            it?.let { image ->
                Glide.with(this).load(image.imageBig).into(image_frame_image)

                if (image.photographer == currentUser.uid) {
                    imagePrivacyContainer.visibility = View.VISIBLE

                    if (image.private) {
                        imagePrivacy.text = getString(R.string.private_text)
                    } else {
                        imagePrivacy.text = getString(R.string.public_text)
                    }

                    imagePrivacyContainer.setOnClickListener {
                        val imagePrivacyRef = FirebaseDatabase.getInstance().getReference("/images/${image.id}/body/private")

                        if (imagePrivacy.text == "Private"){
                            imagePrivacy.text = getString(R.string.public_text)
                            imagePrivacyRef.setValue(false)

                        } else {
                            imagePrivacy.text = getString(R.string.private_text)
                            imagePrivacyRef.setValue(true)
                        }
                    }
                }
            }
        })


    }

    companion object {
        fun newInstance(): ImageFragment = ImageFragment()
    }


}
