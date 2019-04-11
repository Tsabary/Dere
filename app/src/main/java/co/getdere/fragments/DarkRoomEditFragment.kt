package co.getdere.fragments


import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import co.getdere.CameraActivity
import co.getdere.MainActivity

import co.getdere.R
import co.getdere.models.Images
import co.getdere.otherClasses.MyCircleProgressBar
import co.getdere.roomclasses.LocalImagePost
import co.getdere.roomclasses.LocalImageViewModel
import co.getdere.viewmodels.SharedViewModelLocalImagePost
import co.getdere.viewmodels.SharedViewModelRandomUser
import co.getdere.viewmodels.SharedViewModelTags
import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_dark_room_edit.*
import me.echodev.resizer.Resizer
import java.io.File
import java.util.*

class DarkRoomEditFragment : Fragment() {

    lateinit var localImagePost: LocalImagePost
    lateinit var sharedViewModelLocalImagePost: SharedViewModelLocalImagePost
    lateinit var sharedViewModelTags: SharedViewModelTags


    val tagsFiltredAdapter = GroupAdapter<ViewHolder>()
    lateinit var imageChipGroup: ChipGroup
    val tagsRef = FirebaseDatabase.getInstance().getReference("/tags")

    lateinit var imageLocationInput: TextView
    lateinit var imageUrl: TextView
    var imageTagsList: MutableList<String> = mutableListOf()
    var imagePrivacy = false

    lateinit var progressBar: MyCircleProgressBar
    private lateinit var shareButton : TextView

    private lateinit var localImageViewModel: LocalImageViewModel


    override fun onAttach(context: Context) {
        super.onAttach(context)

        activity?.let {
            sharedViewModelLocalImagePost = ViewModelProviders.of(it).get(SharedViewModelLocalImagePost::class.java)
            sharedViewModelTags = ViewModelProviders.of(it).get(SharedViewModelTags::class.java)
            localImageViewModel =  ViewModelProviders.of(it).get(LocalImageViewModel::class.java)
        }


        tagsRef.addChildEventListener(object : ChildEventListener {

            var tags: MutableList<SingleTagForList> = mutableListOf()


            override fun onChildAdded(p0: DataSnapshot, p1: String?) {

                val tagName = p0.key.toString()

                val count = p0.childrenCount.toInt()

                tags.add(SingleTagForList(tagName, count))

                sharedViewModelTags.tagList = tags

            }

            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildRemoved(p0: DataSnapshot) {
            }

        })

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dark_room_edit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val imageVertical = dark_room_edit_image_vertical
        val imageHorizontal = dark_room_edit_image_horizontal
        val addTagButton = dark_room_edit_add_tag_button
        val imageTagsInput = dark_room_edit_tag_input
        imageChipGroup = dark_room_edit_chip_group
        imageLocationInput = dark_room_edit_location_input
        imageUrl = dark_room_edit_url

        val removeButton = dark_room_edit_remove

        val deleteMessage = dark_room_edit_delete_message
        val deleteButton = dark_room_edit_delete_remove_button
        val cancelButton = dark_room_edit_delete_cancel_button


        removeButton.setOnClickListener {

            removeButton.visibility = View.GONE

            deleteMessage.visibility = View.VISIBLE
            deleteButton.visibility = View.VISIBLE
            cancelButton.visibility = View.VISIBLE
        }

        cancelButton.setOnClickListener {

            removeButton.visibility = View.VISIBLE

            deleteMessage.visibility = View.GONE
            deleteButton.visibility = View.GONE
            cancelButton.visibility = View.GONE
        }

        deleteButton.setOnClickListener {
            localImageViewModel.delete(localImagePost)
        }



        val saveButton = view.findViewById<TextView>(R.id.dark_room_edit_save)
        shareButton = dark_room_edit_share

        progressBar = dark_room_edit_progress_bar

        sharedViewModelLocalImagePost.sharedImagePostObject.observe(this, Observer {
            it?.let { localImageObject ->
                Glide.with(this).load(localImageObject.imageUri).into(imageHorizontal)
                localImagePost = localImageObject

                imageLocationInput.text = localImageObject.details
                imageUrl.text = localImageObject.url


            }
        })


        val tagSuggestionRecycler =
            view.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.dark_room_edit_tag_recycler)
        val tagSuggestionLayoutManager = androidx.recyclerview.widget.LinearLayoutManager(this.context)
        tagSuggestionRecycler.layoutManager = tagSuggestionLayoutManager
        tagSuggestionRecycler.adapter = tagsFiltredAdapter


        addTagButton.setOnClickListener {

            if (!imageTagsInput.text.isEmpty()){

                var tagsMatchCount = 0

                for (i in 0 until imageChipGroup.childCount) {
                    val chip = imageChipGroup.getChildAt(i) as Chip
                    if (chip.text.toString() == imageTagsInput.text.toString()) {
                        tagsMatchCount += 1
                    }
                }

                if (tagsMatchCount == 0) {
                    if (imageChipGroup.childCount < 5) {
                        onTagSelected(imageTagsInput.text.toString())
                        imageTagsInput.text.clear()
                    } else {
                        Toast.makeText(this.context, "Maximum 5 tags", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this.context, "Tag had already been added", Toast.LENGTH_LONG).show()
                }

            }



        }



        tagsFiltredAdapter.setOnItemClickListener { item, _ ->
            val row = item as SingleTagSuggestion
            if (imageChipGroup.childCount < 5) {
                onTagSelected(row.tag.tagString)
                imageTagsInput.text.clear()
            } else {
                Toast.makeText(this.context, "Maximum 5 tags", Toast.LENGTH_LONG).show()
            }
        }


        imageTagsInput.addTextChangedListener(object : TextWatcher {

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                tagsFiltredAdapter.clear()

                val userInput = s.toString()

                if (userInput == "") {
                    tagSuggestionRecycler.visibility = View.GONE

                } else {
                    val relevantTags: List<SingleTagForList> =
                        sharedViewModelTags.tagList.filter { it.tagString.contains(userInput) }

                    for (t in relevantTags) {

//                        tagSuggestionRecycler.visibility = View.VISIBLE
//                        tagsFilteredAdapter.add(SingleTagSuggestion(t))
                        var countTagMatches = 0
                        for (i in 0 until imageChipGroup.childCount) {
                            val chip = imageChipGroup.getChildAt(i) as Chip

                            if (t.tagString == chip.text.toString()) {

                                countTagMatches += 1

                            }

                        }


                        if (countTagMatches == 0) {
                            tagSuggestionRecycler.visibility = View.VISIBLE
                            tagsFiltredAdapter.add(SingleTagSuggestion(t))

                        }
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

        })





        shareButton.setOnClickListener {

            shareButton.isClickable = false

            for (i in 0 until imageChipGroup.childCount) {
                val chip = imageChipGroup.getChildAt(i) as Chip
                imageTagsList.add(chip.text.toString())
            }

            uploadPhotoToStorage()
        }


        saveButton.setOnClickListener {

            val locationInput = imageLocationInput.text.toString()
            val url = imageUrl.text.toString()


            val updatedImage = LocalImagePost(localImagePost.timestamp, localImagePost.locationLong, localImagePost.locationLat, localImagePost.imageUri, locationInput, url)

            localImageViewModel.update(updatedImage).invokeOnCompletion {
                Toast.makeText(this.context, "Photo details saved successfully", Toast.LENGTH_LONG).show()
            }

        }

    }


    private fun uploadPhotoToStorage() {

        progressBar.visibility = View.VISIBLE
        progressBar.progress = 0f

        val randomName = UUID.randomUUID().toString()
        val storagePath = Environment.getExternalStorageDirectory().absolutePath
        val path =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + File.separator + "Dere"

        val refBigImage = FirebaseStorage.getInstance().getReference("/images/feed/$randomName/big")
        val refSmallImage = FirebaseStorage.getInstance().getReference("/images/feed/$randomName/small")

        refSmallImage.putFile(
            Uri.fromFile(
                Resizer(this.context)
                    .setTargetLength(200)
                    .setQuality(100)
                    .setOutputFormat("PNG")
                    .setOutputFilename(randomName + "Small")
                    .setOutputDirPath(path)
                    .setSourceImage(File(localImagePost.imageUri))
                    .resizedFile
            )
        ).addOnSuccessListener {
            progressBar.progress = 25f

            Log.d("UploadActivity", "Successfully uploaded image ${it.metadata?.path}")

            refSmallImage.downloadUrl.addOnSuccessListener { smallUri ->

                progressBar.progress = 35f

                Log.d("UploadActivity", "File location: $smallUri")

                val smallImageUri = smallUri.toString()


                refBigImage.putFile(
                    Uri.fromFile(
                        Resizer(this.context)
                            .setTargetLength(800)
                            .setQuality(100)
                            .setOutputFormat("PNG")
                            .setOutputFilename(randomName + "Big")
                            .setOutputDirPath(path)
                            .setSourceImage(File(localImagePost.imageUri))
                            .resizedFile
                    )
                ).addOnSuccessListener {

                    progressBar.progress = 60f
                    Log.d("UploadActivity", "Successfully uploaded image ${it.metadata?.path}")

                    refBigImage.downloadUrl.addOnSuccessListener { bigUri ->
                        progressBar.progress = 70f
                        Log.d("UploadActivity", "File location: $bigUri")

                        val bigImageUri = bigUri.toString()


                        addImageToFirebaseDatabase(bigImageUri, smallImageUri)

                    }.addOnFailureListener {
                        uploadFail()
                    }


                }.addOnFailureListener {
                    uploadFail()
                    Log.d("RegisterActivity", "Failed to upload image to server $it")
//                    progress.visibility = View.GONE
                }


            }.addOnFailureListener {
                uploadFail()
            }


        }.addOnFailureListener {
            uploadFail()
            Log.d("RegisterActivity", "Failed to upload image to server $it")
//            progress.visibility = View.GONE

        }
    }


    private fun addImageToFirebaseDatabase(bigImage: String, smallImage: String) {

        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/images/").push()
        val imageBodyRef = FirebaseDatabase.getInstance().getReference("/images/${ref.key}/body")

        val newImage = Images(ref.key!!, bigImage, smallImage, false, uid!!, imageUrl.text.toString(), imageLocationInput.text.toString(), mutableListOf(localImagePost.locationLat, localImagePost.locationLong), localImagePost.timestamp, System.currentTimeMillis(), imageTagsList)


        imageBodyRef.setValue(newImage)
            .addOnSuccessListener {
                progressBar.progress = 85f
                Log.d("imageToDatabase", "image saved to feed successfully: ${ref.key}")
                val refToUsersDatabase = FirebaseDatabase.getInstance().getReference("/users/$uid/images")

                refToUsersDatabase.setValue(mapOf(ref.key to true))
                    .addOnSuccessListener {
                        progressBar.progress = 95f
                        Log.d("imageToDatabaseByUser", "image saved to byUser successfully: ${ref.key}")

                        for (t in imageTagsList){
                            val refTag = FirebaseDatabase.getInstance().getReference("/tags/$t/${ref.key}")
                            val refUserTags = FirebaseDatabase.getInstance().getReference("users/$uid/interests/$t")

                            refTag.setValue("image")
                            refUserTags.setValue(true)
                            progressBar.progress = 100f
                        }

                        localImageViewModel.delete(localImagePost)


                        val backToFeed = Intent((activity as CameraActivity), MainActivity::class.java)
                        startActivity(backToFeed)

                    }
                    .addOnFailureListener {
                        uploadFail()
                        Log.d("imageToDatabaseByUser", "image did not save to byUser")
                    }
            }
            .addOnFailureListener {
                uploadFail()
                Log.d("imageToDatabase", "image did not save to feed")
            }
    }


    private fun uploadFail(){
        progressBar.visibility = View.GONE
        shareButton.isClickable = true
    }

    private fun onTagSelected(selectedTag: String) {

        val chip = Chip(this.context)
        chip.text = selectedTag
        chip.isCloseIconVisible = true
        chip.isCheckable = false
        chip.isClickable = false
        chip.setChipBackgroundColorResource(R.color.green700)
        chip.setTextAppearance(R.style.ChipSelectedStyle)
        chip.setOnCloseIconClickListener {
            imageChipGroup.removeView(it)
        }

        imageChipGroup.addView(chip)
        imageChipGroup.visibility = View.VISIBLE

    }


}
