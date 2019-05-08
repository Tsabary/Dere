package co.getdere.interfaces

import android.app.Activity
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

interface Analytics {

    fun event(firebaseAnalytics : FirebaseAnalytics, name : String){
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, name)
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
    }

}