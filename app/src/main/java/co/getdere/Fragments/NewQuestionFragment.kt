package co.getdere.Fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import co.getdere.MainActivity
import co.getdere.R


class NewQuestionFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val mainActivity = activity as MainActivity

        val myView = inflater.inflate(R.layout.fragment_new_question, container, false)



        return myView
    }


    companion object {
        fun newInstance(): BoardFragment = BoardFragment()
    }



}