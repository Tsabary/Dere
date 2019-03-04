package co.getdere.Fragments


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import co.getdere.R

class BoardFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_board, container, false)

    companion object {
        fun newInstance(): BoardFragment = BoardFragment()
    }
}
