package co.getdere.Fragments


import android.os.Bundle
import android.app.Fragment
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import co.getdere.R
import com.squareup.picasso.Picasso
import java.net.URI


class ImageFullSizeFragment : androidx.fragment.app.Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_image_full_size, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mainImage = view.findViewById<ImageView>(R.id.image_full_size_image)

        arguments?.let {
            val safeArgs = ImageFullSizeFragmentArgs.fromBundle(it)

            val imageUri = Uri.parse(safeArgs.imageId)

            Picasso.get().load(imageUri).into(mainImage)
        }


    }
}
