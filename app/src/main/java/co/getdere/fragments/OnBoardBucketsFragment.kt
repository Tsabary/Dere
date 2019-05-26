package co.getdere.fragments


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import co.getdere.R
import kotlinx.android.synthetic.main.fragment_on_boarding_screens.*


class OnBoardBucketsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_on_boarding_screens, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        on_boarding_image.setImageResource(R.drawable.bucket_white)
    }

    companion object {
        fun newInstance(): OnBoardBucketsFragment = OnBoardBucketsFragment()
    }
}
