package co.getdere.Fragments


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import co.getdere.MainActivity

import co.getdere.R
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder

class BoardFragment : Fragment() {
    val questionsRecyclerAdapter = GroupAdapter<ViewHolder>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val mainActivity = activity as MainActivity

        val myView = inflater.inflate(R.layout.fragment_board, container, false)

        val questionsRecycler = myView.findViewById<RecyclerView>(R.id.board_question_feed)

        val questionRecyclerLayoutMananger = LinearLayoutManager (this.context)
        val divItemDecor = DividerItemDecoration(this.context, questionRecyclerLayoutMananger.orientation)
        questionsRecycler.addItemDecoration(divItemDecor)
        questionsRecycler.adapter = questionsRecyclerAdapter

        questionsRecycler.layoutManager = questionRecyclerLayoutMananger

        questionsRecyclerAdapter.add(singleQuestion())
        questionsRecyclerAdapter.add(singleQuestion())
        questionsRecyclerAdapter.add(singleQuestion())
        questionsRecyclerAdapter.add(singleQuestion())
        questionsRecyclerAdapter.add(singleQuestion())
        questionsRecyclerAdapter.add(singleQuestion())
        questionsRecyclerAdapter.add(singleQuestion())
        questionsRecyclerAdapter.add(singleQuestion())
        questionsRecyclerAdapter.add(singleQuestion())
        questionsRecyclerAdapter.add(singleQuestion())
        questionsRecyclerAdapter.add(singleQuestion())
        questionsRecyclerAdapter.add(singleQuestion())
        questionsRecyclerAdapter.add(singleQuestion())
        questionsRecyclerAdapter.add(singleQuestion())
        questionsRecyclerAdapter.add(singleQuestion())
        questionsRecyclerAdapter.add(singleQuestion())
        questionsRecyclerAdapter.add(singleQuestion())
        questionsRecyclerAdapter.add(singleQuestion())


        return myView
    }


    companion object {
        fun newInstance(): BoardFragment = BoardFragment()
    }
}

class singleQuestion() : Item<ViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.board_single_row
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {

    }

}
