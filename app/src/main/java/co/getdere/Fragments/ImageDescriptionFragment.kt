package co.getdere.Fragments


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import co.getdere.R


class ImageDescriptionFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_image_description, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val nextButton = view.findViewById<TextView>(R.id.image_description_next)

        nextButton.setOnClickListener {



        }
    }




    companion object {
        fun newInstance(): ImageDescriptionFragment = ImageDescriptionFragment()
    }


}
