package co.getdere.fragments


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import co.getdere.R
import com.stripe.android.view.CardInputWidget
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_buy_itinerary.*


class BuyItineraryFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(co.getdere.R.layout.fragment_buy_itinerary, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val mCardInputWidget = card_input_widget as CardInputWidget

        val cardToSave = mCardInputWidget.card
        if (cardToSave == null) {
            Toast.makeText(this.context, "Invalid Card Data", Toast.LENGTH_SHORT).show()
        }
    }


}
