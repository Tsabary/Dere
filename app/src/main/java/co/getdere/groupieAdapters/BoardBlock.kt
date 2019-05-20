package co.getdere.groupieAdapters

import androidx.lifecycle.ViewModelProviders
import co.getdere.MainActivity
import co.getdere.models.Question
import co.getdere.R
import co.getdere.models.Users
import co.getdere.viewmodels.SharedViewModelCurrentUser
import co.getdere.viewmodels.SharedViewModelQuestion
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.board_single_block.view.*
import kotlinx.android.synthetic.main.board_single_row.view.*
import org.ocpsoft.prettytime.PrettyTime
import java.util.*

class BoardBlock(
    val question: Question, val activity: MainActivity
) : Item<ViewHolder>() {

    val uid = FirebaseAuth.getInstance().uid

    override fun getLayout(): Int {
        return R.layout.board_single_block
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {

        activity.let {
            val currentUser = ViewModelProviders.of(it).get(SharedViewModelCurrentUser::class.java).currentUserObject

            Glide.with(viewHolder.root.context).load(if (currentUser.image.isNotEmpty()){currentUser.image}else{R.drawable.user_profile}).into(viewHolder.itemView.board_block_current_user_photo)
        }


        val refAnswers = FirebaseDatabase.getInstance().getReference("/questions/${question.id}/answers")

        refAnswers.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {

                val count = p0.childrenCount

                val date = PrettyTime().format(Date(question.timestamp))


                viewHolder.itemView.board_block_question.text = question.title
                viewHolder.itemView.board_block_tags.text = question.tags.joinToString()
                viewHolder.itemView.board_block_timestamp.text = date
                viewHolder.itemView.board_block_answers.text = "$count answers"
                viewHolder.itemView.board_block_content.text = question.details

            }
        })

        val authorRef = FirebaseDatabase.getInstance().getReference("/users/${question.author}/profile")

        authorRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {

                val user = p0.getValue(Users::class.java)
                if (user != null) {
                    Glide.with(viewHolder.root.context).load(if(user.image.isNotEmpty()){user.image}else{R.drawable.user_profile})
                        .into(viewHolder.itemView.board_block_author_image)
                    viewHolder.itemView.board_block_author_name.text = user.name
                }
            }
        })


    }

}