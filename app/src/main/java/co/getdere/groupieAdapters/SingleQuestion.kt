package co.getdere.groupieAdapters

import co.getdere.models.Question
import co.getdere.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.board_single_row.view.*
import org.ocpsoft.prettytime.PrettyTime
import java.util.*

class SingleQuestion(
    val question : Question
) : Item<ViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.board_single_row
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {

        val refAnswers = FirebaseDatabase.getInstance().getReference("/questions/${question.id}/answers")

        refAnswers.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {

                val count = p0.childrenCount

                val stampMills = question.timestamp
                val pretty = PrettyTime()
                val date = pretty.format(Date(stampMills))


                viewHolder.itemView.board_question.text = "${question.title}?"
                viewHolder.itemView.board_tags.text = question.tags.joinToString()
                viewHolder.itemView.board_timestamp.text = date
                viewHolder.itemView.board_answers.text = count.toString()

            }
        })

    }

}