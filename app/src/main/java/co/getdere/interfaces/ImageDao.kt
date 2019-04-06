package co.getdere.interfaces

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import co.getdere.roomclasses.LocalImagePost

@Dao
interface ImageDao {

    @Insert
    fun insert(image: LocalImagePost)

    @Query("SELECT * from image_posts ORDER BY timestamp ASC")
    fun getAllWords(): LiveData<List<LocalImagePost>>


    @Query("DELETE FROM image_posts")
    fun deleteAll()



}