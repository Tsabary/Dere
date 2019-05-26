package co.getdere.roomclasses

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import co.getdere.database.DereRoomDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class LocalImageViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ImagePostsRepository
    val allImagePosts: LiveData<List<LocalImagePost>>



    private var parentJob = Job()

    private val coroutineContext: CoroutineContext
        get() = parentJob + Dispatchers.Main

    private val scope = CoroutineScope(coroutineContext)

    init {
        val imagesDao = DereRoomDatabase.getDatabase(application, scope).imageDao()
        repository = ImagePostsRepository(imagesDao)
        allImagePosts = repository.allImagePosts
    }

    fun insert(imagePost: LocalImagePost) = scope.launch(Dispatchers.IO) {
        repository.insert(imagePost)
    }

    fun update(imagePost: LocalImagePost) = scope.launch(Dispatchers.IO) {
        repository.update(imagePost)
    }

    fun delete(imagePost: LocalImagePost) = scope.launch(Dispatchers.IO) {
        repository.delete(imagePost)
    }

    override fun onCleared() {
        super.onCleared()
        parentJob.cancel()
    }

}
