package edu.calbaptist.bible_ly.ui.bible

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import edu.calbaptist.Note_ly.adapter.NoteMutableListAdapter
import edu.calbaptist.bible_ly.*
import edu.calbaptist.bible_ly.adapter.BibleMutableListAdapter
import kotlinx.android.synthetic.main.fragment_bible.view.*
//import org.jetbrains.anko.doAsync
//import org.jetbrains.anko.uiThread

public const val TAG = "BibleFragment"

class BibleFragment : Fragment(), BibleMutableListAdapter.OnBibleItemSelectedListener,
    BibleMutableListAdapter.OnBibleItemLongSelectedListener ,
    NoteMutableListAdapter.OnNoteItemSelectedListener,NoteMutableListAdapter.OnNoteItemLongSelectedListener {
    override fun onNoteItemLongSelected(v:View,item: NoteCardViewItem) {
        showNotePopup(v,item)
    }

    override fun onNoteItemSelected(item: NoteCardViewItem) {

    }

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
                R.id.bible_verse_share ->{
                    Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT,"$book: chapter:$verseChapter-verse: $verseNum\n" +
                                "$verseText")
                        putExtra(Intent.EXTRA_SUBJECT, "")
                    }.also { intent -> val chooserIntent = Intent.createChooser(intent,"Share Verse")
                        startActivity(chooserIntent)
                    }
                }

            }

            true
        })

        popup.show()
    }

    private fun showNotePopup(view: View,note :NoteCardViewItem) {
        var popup: PopupMenu? = null;
        popup = PopupMenu(view.context, view)
        popup.inflate(R.menu.menu_bible_note_item_more)

        if(note.type == "For Class" && note.user!!.email != MainActivity.user.email){
            popup.menu.removeItem(R.id.bible_note_edit_note)
            popup.menu.removeItem(R.id.bible_note_delete_note)
        }


        popup.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item: MenuItem? ->

            when (item!!.itemId) {

                R.id.bible_note_go_verse-> {
                    chapter = note.verseChapter.toInt()
                    reloadChapter(note.book.toInt(), note.verseNum.toInt()-1)
                    MainActivity.drawerLayout.closeDrawer(GravityCompat.END)

                }


                R.id.bible_note_edit_note-> {
                    var d = NoteDialog.newInstance(false, note.noteID,note.book,note.verseNum,note.verseChapter,note.verseText)
                    val fm = requireActivity().supportFragmentManager
                    d.show(fm,"NoteDialog")
                }

                R.id.bible_note_delete_note-> {
                    var dialoge = AlertDialog.Builder(requireContext())
                        .setCancelable(false)
                        .setTitle("Are you sure you want to delete this note?")
                        .setNegativeButton("No", DialogInterface.OnClickListener { dialog, which ->
                            //Action goes here
                        })
                        .setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, which ->
                            FirestoreRepository().deleteNote(note.noteID)

                        })
                        .create()

                    dialoge.show()

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

    private lateinit var noteRecyclerView: RecyclerView
    lateinit var noteLinearLayoutManager: LinearLayoutManager
    lateinit var noteAdapter: NoteMutableListAdapter

    var chapter = 1
    var book = 1

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


        noteRecyclerView = MainActivity.navView2.findViewById(R.id.rv_bible_nav_notes)
        noteLinearLayoutManager = LinearLayoutManager(context)
        noteRecyclerView.layoutManager = noteLinearLayoutManager
        noteAdapter= NoteMutableListAdapter(this,this)
        noteRecyclerView.adapter = noteAdapter
//        val textView: TextView = root.findViewById(R.id.text_slideshow)
//        bibleViewModel.text.observe(this, Observer {
//            textView.text = it
//        })
        reloadBook(1) // Note: this initializes the bible with book 1 chapter 1
        //reloadChapter(1, 0)

        bibleViewModel.getNotes().observe(this, Observer {list ->


            if(list!=null)
                reloadNotes( list)


        })
        setRecyclerViewScrollListener()
        myRoot.ib_bible_next_chapter.setOnClickListener {
            if(chapter!=50) {
                chapter++
                reloadChapter(book, chapter)
            }
        }
        myRoot.ib_bible_previous_chapter.setOnClickListener {
            if(chapter!=0) {
                chapter--
                reloadChapter(book, chapter)
            }
        }
        myRoot.btn_bible_chapter.setOnClickListener {
            selectChapterDialog()
        }
        myRoot.btn_bible_book.setOnClickListener{
            selectBookDialog()
        }

        return myRoot
    }
    fun reloadNotes(list:List<NoteCardViewItem>){

        var list2 = mutableListOf<NoteCardViewItem>()
        var chapter = ""

        for (note in  list.sortedBy { a -> a.verseChapter}){
            if(chapter != note.verseChapter){
                list2.add(note.copy(isHeader = true))
                chapter = note.verseChapter
            }
            list2.add(note.copy(isHeader = false))
        }

        noteAdapter.submitList(list2)
        //adapter2.notifyDataSetChanged()
        ////noteRecyclerView.adapter = noteAdapter
    }
    fun reloadChapter(bookNum:Int, chapterNum: Int){
        bibleViewModel.getVerses(bookNum.toString(), chapterNum.toString()).observe(this, Observer { list ->
            reloadBible(chapterNum, list.sortedBy { it.getVerseAsInt() }, 1) // note: always start with 1st verse
        })

        myRoot.btn_bible_chapter.text = "Chapter "+ chapter.toString()
    }
    fun reloadBook(bookNum: Int){
        bibleViewModel.getBibleKeys()
        bibleViewModel.getBibleBook(bookNum.toString()).observe(this, Observer { bible ->
            reloadBible(1, bible.verses.sortedBy { it.getVerseAsInt() }, 1) // note: always start at the beginning
        })

        Log.d(TAG, book.toString())
        myRoot.btn_bible_book.text = "Book "+ book.toString()
    }
    fun reloadBible(chapterNum: Int, verseList:List<Verse>, verseNum:Int){
        //Toast.makeText(requireContext(),eventList.size.toString(), Toast.LENGTH_SHORT).show()
        adapter.submitList(verseList)
        //adapter2.notifyDataSetChanged()
        bibleRecyclerView.adapter = adapter

        bibleRecyclerView.smoothSnapToPosition(verseNum)

//        doAsync {
//            //Execute all the long running tasks here
//                try {
//                    Thread.sleep(400)
//                    uiThread {
//                        bibleRecyclerView.smoothSnapToPosition(verseNum)
//                    }
//                }catch (e: InterruptedException) {
//                    // do something if thread doesn't work
//                }
//
//            }


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

        var listItems = mutableListOf<String>() // bibleViewModel.verses.value!! //.distinctBy { it.chapter }
        for (i in 1..bibleViewModel.bible.value!!.numOfChapters)
            listItems.add("Chapter "+ i)

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, listItems)
        var list = ListView(requireContext())
        list.adapter = adapter
        list.setOnItemClickListener { parent, view, position, id ->
            chapter = position + 1
            reloadChapter(book, chapter)
            dialoge.dismiss()
        }

        dialoge.setView(list)
        dialoge.show()
    }

    private fun selectBookDialog() {

        var dialoge = AlertDialog.Builder(requireContext())
            .setCancelable(false)
            .setTitle("Select Book")
            .setNegativeButton("Close", DialogInterface.OnClickListener { dialog, which ->
                //Action goes here
            })
            .create()

        // NOTE: hardcoded to only get first 19 books. We don't have the rest of the bible in firebase
        var listItems = bibleViewModel.books.value!!.filter { it.bookNumber <= 19 }.map { it.name }

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, listItems)
        var list = ListView(requireContext())
        list.adapter = adapter
        list.setOnItemClickListener { parent, view, position, id ->
            book = position + 1
            reloadBook(book)
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

    fun RecyclerView.smoothSnapToPosition(position: Int, snapMode: Int = LinearSmoothScroller.SNAP_TO_START) {
        val smoothScroller = object: LinearSmoothScroller(this.context) {
            override fun getVerticalSnapPreference(): Int {
                return snapMode
            }

            override fun getHorizontalSnapPreference(): Int {
                return snapMode
            }
        }
        smoothScroller.targetPosition = position
        layoutManager?.startSmoothScroll(smoothScroller)
    }
}
