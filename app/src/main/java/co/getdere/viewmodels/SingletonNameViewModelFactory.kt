package co.getdere.viewmodels

import androidx.lifecycle.ViewModelProvider


class SingletonNameViewModelFactory : ViewModelProvider.NewInstanceFactory() {


    internal var t: SharedViewModelTags? = null

    fun create(modelClass: Class<SharedViewModelTags>): SharedViewModelTags? {
        return t
    }
}//  t = provideNameViewModelSomeHowUsingDependencyInjection