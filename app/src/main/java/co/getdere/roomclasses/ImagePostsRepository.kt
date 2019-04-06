package co.getdere.roomclasses

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import co.getdere.interfaces.ImageDao

class ImagePostsRepository (private val imageDao : ImageDao) {

    val allImagePosts: LiveData<List<LocalImagePost>> = imageDao.getAllWords()

    @WorkerThread
    suspend fun insert(imagePost: LocalImagePost) {
        imageDao.insert(imagePost)
    }
}