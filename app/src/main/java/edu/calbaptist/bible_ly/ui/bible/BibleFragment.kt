package edu.calbaptist.bible_ly.ui.bible

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import edu.calbaptist.bible_ly.*
import edu.calbaptist.bible_ly.adapter.BibleMutableListAdapter
import edu.calbaptist.bible_ly.adapter.EventMutableListAdapter
import kotlinx.android.synthetic.main.fragment_bible.*
import kotlinx.android.synthetic.main.fragment_bible.view.*

class BibleFragment : Fragment(), BibleMutableListAdapter.OnBibleItemSelectedListener,
    BibleMutableListAdapter.OnBibleItemLongSelectedListener {
    override fun onBibleItemLongSelected(
        v: View,
        book:String,
        verseNum: String,
        verseChapter: String,
        verseText: String
    ) {
        showPopup(v,book,verseNum,verseChapter,verseText)
    }


    private fun showPopup(view: View,book:String,verseNum: String,
                          verseChapter: String,
                          verseText: String) {
        var popup: PopupMenu? = null;
        popup = PopupMenu(view.context, view)
        popup.inflate(R.menu.menu_bible_item_more)


        popup.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item: MenuItem? ->

            when (item!!.itemId) {
                R.id.bible_verse_add_note-> {
                    var d = NoteDialog.newInstance(true, "",book,verseNum,verseChapter,verseText)
                    val fm = requireActivity().supportFragmentManager
                    d.show(fm,"NoteDialog")
                }

            }

            true
        })

        popup.show()
    }

    override fun onBibleItemSelected(item: Verse) {

    }



    private lateinit var bibleViewModel: BibleViewModel

    private lateinit var bibleRecyclerView: RecyclerView
    lateinit var linearLayoutManager: LinearLayoutManager
    lateinit var adapter: BibleMutableListAdapter

    var chapter = 1

    lateinit var myRoot:View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        MainActivity.mainMenu!!.getItem(0).isVisible = true
        MainActivity.mainMenu!!.getItem(1).isVisible = true
        bibleViewModel =
            ViewModelProviders.of(this).get(BibleViewModel::class.java)
         myRoot = inflater.inflate(R.layout.fragment_bible, container, false)

        bibleRecyclerView = myRoot.findViewById(R.id.rv_bible)
        linearLayoutManager = LinearLayoutManager(context)
        bibleRecyclerView.layoutManager = linearLayoutManager
        adapter= BibleMutableListAdapter(this,this)

//        val textView: TextView = root.findViewById(R.id.text_slideshow)
//        bibleViewModel.text.observe(this, Observer {
//            textView.text = it
//        })
        bibleViewModel.getVerses(chapter.toString()).observe(this, Observer {list ->


           reloadBible( list.sortedBy { it.getVerseAsInt() })
            myRoot.btn_bible_chapter.text = "Chapter "+chapter.toString()

        })
        setRecyclerViewScrollListener()
        myRoot.ib_bible_next_chapter.setOnClickListener {
            if(chapter!=50) {
                chapter++
                bibleViewModel.getVerses(chapter.toString()).observe(this, Observer { list ->
                    reloadBible(list.sortedBy { it.getVerseAsInt() })

                })
                myRoot.btn_bible_chapter.text = "Chapter "+chapter.toString()
            }
        }
        myRoot.ib_bible_previous_chapter.setOnClickListener {
            if(chapter!=0) {
                chapter--
                bibleViewModel.getVerses(chapter.toString()).observe(this, Observer { list ->
                    reloadBible(list.sortedBy { it.getVerseAsInt() })
                })
                myRoot.btn_bible_chapter.text = "Chapter "+chapter.toString()
            }
        }
        myRoot.btn_bible_chapter.setOnClickListener {
            selectChapterDialog()
        }

        return myRoot
    }
    fun reloadBible(verseList:List<Verse>){
        //Toast.makeText(requireContext(),eventList.size.toString(), Toast.LENGTH_SHORT).show()
        adapter.submitList(verseList)
        //adapter2.notifyDataSetChanged()
        bibleRecyclerView.adapter = adapter
    }
    private fun setRecyclerViewScrollListener() {
        bibleRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                val totalItemCount = recyclerView.layoutManager!!.itemCount

                 //  Toast.makeText(requireContext(),newState.toString(),Toast.LENGTH_LONG).show()

            }
        })
    }

    private fun selectChapterDialog(){

        var dialoge = AlertDialog.Builder(requireContext())
            .setCancelable(false)
            .setTitle("Select chapter")
            .setNegativeButton("Close", DialogInterface.OnClickListener { dialog, which ->
                //Action goes here
            })

            .create()
        var listItems= mutableListOf<String>()
        for (i in 1..50)
            listItems.add("Chapter "+i)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, listItems)
        var list = ListView(requireContext())
        list.adapter = adapter
        list.setOnItemClickListener { parent, view, position, id ->
            chapter=position+1
            bibleViewModel.getVerses(chapter.toString()).observe(this, Observer { list ->
                reloadBible(list.sortedBy { it.getVerseAsInt() })
            })
            myRoot.btn_bible_chapter.text = "Chapter "+chapter.toString()
            dialoge.dismiss()
        }
        dialoge.setView(list)
        dialoge.show()
    }

    private val lastVisibleItemPosition: Int
        get() = linearLayoutManager.findLastVisibleItemPosition()


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main, menu);
        menu.getItem(R.id.action_settings).isVisible = false
        super.onCreateOptionsMenu(menu, inflater)
    }
}
