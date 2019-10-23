package edu.calbaptist.bible_ly.ui.bible

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import edu.calbaptist.bible_ly.R

class BibleFragment : Fragment() {

    private lateinit var bibleViewModel: BibleViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        bibleViewModel =
            ViewModelProviders.of(this).get(BibleViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_bible, container, false)
        val textView: TextView = root.findViewById(R.id.text_slideshow)
        bibleViewModel.text.observe(this, Observer {
            textView.text = it
        })
        return root
    }
}