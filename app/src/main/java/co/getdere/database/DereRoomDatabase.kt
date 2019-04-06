package co.getdere.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import co.getdere.interfaces.ImageDao
import co.getdere.roomclasses.LocalImagePost
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@Database(entities = [LocalImagePost::class], version = 1)
public abstract class DereRoomDatabase : RoomDatabase()  {

    abstract fun imageDao(): ImageDao

    companion object {
        @Volatile
        private var INSTANCE: DereRoomDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): DereRoomDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DereRoomDatabase::class.java,
                    "Image_posts_database"
                ).addCallback(LocalImageDatabaseCallback(scope)).build()
                INSTANCE = instance
                return instance
            }
        }
    }


    private class LocalImageDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {

        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
//                    populateDatabase(database.imageDao())
                }
            }
        }

//        fun populateDatabase(imageDao: ImageDao) {
//            imageDao.deleteAll()
//
////            var word = LocalImagePost(2342, "this is a uri")
////            imageDao.insert(word)
////            word = LocalImagePost(343,"World!")
////            imageDao.insert(word)
//        }


    }
}