package edu.calbaptist.bible_ly.ui.board

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.calbaptist.bible_ly.*
import edu.calbaptist.bible_ly.adapter.EventMutableListAdapter
import edu.calbaptist.bible_ly.ui.event.EventDialog
import kotlinx.android.synthetic.main.fragment_board.*


class BoardFragment : Fragment(),EventMutableListAdapter.OnEventItemSelectedListener {
    override fun onEventItemSelected(classItem: Event) {
        var d = EventDialog.newInstance(false, false , classItem.clss!!.classID,classItem.eventID)
        val fm = requireActivity().supportFragmentManager
        d.show(fm,"EventDialog")
    }


    private lateinit var boardViewModel: BorardViewModel

    private lateinit var eventRecyclerView: RecyclerView


    lateinit var adapter: EventMutableListAdapter


    private lateinit var linearLayoutManager: LinearLayoutManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        boardViewModel = ViewModelProviders.of(this).get(BorardViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_board, container, false)

        eventRecyclerView = root.findViewById(R.id.rv_board_frag_events)
        linearLayoutManager = LinearLayoutManager(context)
        eventRecyclerView.layoutManager = linearLayoutManager
        adapter= EventMutableListAdapter( this)
        eventRecyclerView.adapter = adapter

        boardViewModel.getEvents().observe(this, Observer {list ->

            if(list!=null)
                reloadEvents( list)

        })


        return root
    }
    private fun reloadEvents(list:List<Event>){

        if(list.isEmpty())
            tv_board_frag_noEvent.visibility = View.VISIBLE
        else
            tv_board_frag_noEvent.visibility = View.GONE

        adapter.submitList(list)

    }










}