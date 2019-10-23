package edu.calbaptist.bible_ly.ui

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText

import edu.calbaptist.bible_ly.R


private const val ARG_EVENT_ID = "eventID"

class EventFragment : Fragment() {

//    companion object {
//        fun newInstance(eventID: Int) : EventFragment{
//            val args = Bundle().apply{
//                putSerializable(ARG_EVENT_ID,eventID)
//            }
//            return EventFragment().apply {
//                arguments = args
//            }
//        }
//    }



    private lateinit var viewModel: EventViewModel
    private lateinit var titleEditText: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.event_detailed_fragment, container, false)

        titleEditText = view.findViewById(R.id.et_event_frag_title)
        val myValue = this.arguments!!.getInt("eventID")
        titleEditText.setText(myValue.toString())
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(EventViewModel::class.java)

    }

}
