package co.getdere.fragments


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import co.getdere.R
import com.stripe.android.view.CardInputWidget
import android.widget.Toast
import com.stripe.android.Stripe
import com.stripe.android.TokenCallback
import com.stripe.android.model.Card
import com.stripe.android.model.Token
import kotlinx.android.synthetic.main.fragment_buy_itinerary.*
import java.lang.Exception


class BuyItineraryFragment : Fragment() {

    private val publishableKey = "pk_test_55ugQVW4tgPwwEyxvSUXZX19"
    private val stripe = Stripe(this.context!!, publishableKey)
    lateinit var mCardInputWidget: CardInputWidget

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(co.getdere.R.layout.fragment_buy_itinerary, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mCardInputWidget = card_input_widget as CardInputWidget

        pay()




    }

    private fun pay() {

        val cardToSave = mCardInputWidget.card

        if (cardToSave != null) {
            stripe.createToken(cardToSave, object : TokenCallback {
                override fun onSuccess(result: Token) {

                }

                override fun onError(e: Exception) {
                }

            })
        }

    }
}
