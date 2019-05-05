package co.getdere.fragments


import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import co.getdere.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_waiting_verification.*

class WaitingVerificationFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_waiting_verification, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val resendVerification = resend_email_verification
        val user = FirebaseAuth.getInstance().currentUser
        resendVerification.setOnClickListener {
            user!!.sendEmailVerification()
        }

        Log.d("waitingcomplete","waiting verification loaded")
    }

    companion object {
        fun newInstance(): WaitingVerificationFragment = WaitingVerificationFragment()
    }


}
