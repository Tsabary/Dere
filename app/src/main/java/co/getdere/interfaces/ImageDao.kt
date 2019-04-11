package co.getdere.interfaces

import androidx.lifecycle.LiveData
import androidx.room.*
import co.getdere.roomclasses.LocalImagePost

@Dao
interface ImageDao {

    @Insert
    fun insert(image: LocalImagePost)

    @Query("SELECT * from image_posts ORDER BY timestamp ASC")
    fun getAllWords(): LiveData<List<LocalImagePost>>


    @Query("DELETE FROM image_posts")
    fun deleteAll()

    @Update
    fun update(image: LocalImagePost)

    @Delete
    fun delete(image: LocalImagePost)
}