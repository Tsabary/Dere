package co.getdere.roomclasses

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "image_posts")
data class LocalImagePost(
    @PrimaryKey @ColumnInfo(name = "timestamp") val timestamp: Long, @ColumnInfo(name = "locationLong") val locationLong: Double, @ColumnInfo(name = "locationLat") val locationLat: Double , @ColumnInfo(
        name = "imageUri") val imageUri: String, @ColumnInfo(name = "details") val details: String, @ColumnInfo(name = "url") val url: String
) {
}