package edu.calbaptist.bible_ly.ui.bible

import android.content.DialogInterface
import android.content.Intent
import android.content.res.Configuration
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
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import android.widget.LinearLayout
import kotlin.concurrent.thread

public const val TAG = "BibleFragment"

var selectedNote :NoteCardViewItem? = null

class BibleFragment : Fragment(), BibleMutableListAdapter.OnBibleItemSelectedListener,
    BibleMutableListAdapter.OnBibleItemLongSelectedListener ,
    NoteMutableListAdapter.OnNoteItemSelectedListener,NoteMutableListAdapter.OnNoteItemLongSelectedListener,
    NoteDialog.Callback{
    override fun onDismissed(isRotating: Boolean) {
        if(!isRotating){
            selectedNote = null
        }

    }


    override fun onNoteItemLongSelected(v:View,item: NoteCardViewItem) {
        showNotePopup(v,item)
    }

    override fun onNoteItemSelected(item: NoteCardViewItem) {

        selectedNote = item

        NoteDialog.newInstance(false, item.noteID,item.book,item.verseNum,item.verseChapter,item.verseText,books[bookNum-1].name).apply {
            setTargetFragment(this@BibleFragment, 0)
            show(this@BibleFragment.requireFragmentManager(), "NoteDialog")
        }

//        var d = NoteDialog.newInstance(false, item.noteID,item.book,item.verseNum,item.verseChapter,item.verseText,books[bookNum-1].name)
//        val fm = requireActivity().supportFragmentManager
//        d.show(fm,"NoteDialog")
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
                    var d = NoteDialog.newInstance(true, "",book,verseNum,verseChapter,verseText,books[bookNum-1].name)
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

        if(note.shared&& note.user!!.email != MainActivity.user.email){
            popup.menu.removeItem(R.id.bible_note_delete_note)
        }


        popup.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item: MenuItem? ->

            when (item!!.itemId) {

                R.id.bible_note_go_verse-> {
                    chapter = note.verseChapter.toInt()
                    bookNum=note.book.toInt()
                    reloadChapter( note.verseNum.toInt()-1)
                    MainActivity.drawerLayout.closeDrawer(GravityCompat.END)

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
    override fun onConfigurationChanged(newConfig: Configuration) {
        //(findViewById(R.id.webviewPlace) as LinearLayout).removeAllViews()
        /*val intent = Intent(requireContext(), MainActivity::class.java)
        intent.putExtra("currentDestination", R.id.nav_bible)
        startActivity(intent)*/
        //this.finish()

        if(selectedNote!=null){
            doAsync {
                Thread.sleep(200)
                uiThread {
                    NoteDialog.newInstance(false, selectedNote!!.noteID,selectedNote!!.book,selectedNote!!.verseNum,selectedNote!!.verseChapter,selectedNote!!.verseText,books[bookNum-1].name).apply {
                        setTargetFragment(this@BibleFragment, 0)
                        show(this@BibleFragment.requireFragmentManager(), "NoteDialog")
                        fragmentManager!!.popBackStack()
                    }

                }
            }
        } else {
            fragmentManager!!.popBackStack()
        }



        //
        MainActivity.navigateDrawer()

        super.onConfigurationChanged(newConfig)

        //initUI()
    }


    private lateinit var bibleViewModel: BibleViewModel

    private lateinit var bibleRecyclerView: RecyclerView
    lateinit var linearLayoutManager: LinearLayoutManager
    lateinit var adapter: BibleMutableListAdapter

    private lateinit var noteRecyclerView: RecyclerView
    lateinit var noteLinearLayoutManager: LinearLayoutManager
    lateinit var noteAdapter: NoteMutableListAdapter
    lateinit var noNoteTV: TextView

    var chapter = 1
    var bookNum = 1

    var books = listOf<BibleKey>()
    var allNotes = listOf<NoteCardViewItem>()
    lateinit var myRoot:View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        var bookNumBundle = arguments?.getString("bookNum") ?: ""

        if(bookNumBundle!= ""){
            bookNum = bookNumBundle.toInt()
        }


        bibleViewModel =
            ViewModelProviders.of(this).get(BibleViewModel::class.java)
         myRoot = inflater.inflate(R.layout.fragment_bible, container, false)

        bibleRecyclerView = myRoot.findViewById(R.id.rv_bible)
        linearLayoutManager = LinearLayoutManager(context)
        bibleRecyclerView.layoutManager = linearLayoutManager
        adapter= BibleMutableListAdapter(this,this)

        val orientation = resources.configuration.orientation
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            noteRecyclerView = myRoot.findViewById(R.id.rv_bible_nav_notes_land)
            noNoteTV = myRoot.findViewById(R.id.tv_bible_nav_no_notes_land)

        } else {
            noteRecyclerView = MainActivity.navView2.findViewById(R.id.rv_bible_nav_notes)
            noNoteTV = MainActivity.navView2.findViewById(R.id.tv_bible_nav_no_notes)

        }
        noteLinearLayoutManager = LinearLayoutManager(context)
        noteRecyclerView.layoutManager = noteLinearLayoutManager
        noteAdapter= NoteMutableListAdapter(this,this)
        noteRecyclerView.adapter = noteAdapter
//        val textView: TextView = root.findViewById(R.id.text_slideshow)
//        bibleViewModel.text.observe(this, Observer {
//            textView.text = it
//        })
        reloadBook() // Note: this initializes the bible with book 1 chapter 1
        //reloadChapter(1, 0)

        bibleViewModel.getNotes().observe(this, Observer {list ->

            allNotes = list
            reloadNotes( )


        })
        setRecyclerViewScrollListener()
        myRoot.ib_bible_next_chapter.setOnClickListener {
            if(chapter!=books[bookNum-1].chapterCount) {
                chapter++
                reloadChapter( 0)
            }
        }
        myRoot.ib_bible_previous_chapter.setOnClickListener {
            if(chapter!=1) {
                chapter--
                reloadChapter(0)
            }
        }
        myRoot.btn_bible_chapter.setOnClickListener {
            selectChapterDialog()
        }
        myRoot.btn_bible_book.setOnClickListener{
            selectBookDialog()
        }
        bibleViewModel.getBibleKeys().observe(this, Observer {books ->
            this.books = books
            myRoot.btn_bible_book.text = books[bookNum -1].name //"Book "+ book.toString()
        })

        return myRoot
    }
    private fun reloadNotes(){
        if(allNotes==null)
            allNotes = listOf()
        var list = allNotes.filter { note -> note.book == bookNum.toString() }
        var list2 = mutableListOf<NoteCardViewItem>()
        if(list.isEmpty()){
            noNoteTV.visibility=View.VISIBLE
            noteRecyclerView.visibility =View.GONE
        } else {
            noNoteTV.visibility=View.GONE
            noteRecyclerView.visibility=View.VISIBLE
        }
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
    private fun reloadChapter(  verseNum: Int){
        bibleViewModel.getVerses(bookNum.toString(), chapter.toString()).observe(this, Observer { list ->
            reloadBible( list, verseNum) // note: always start with 1st verse
        })

        myRoot.btn_bible_chapter.text = "Chapter "+ chapter.toString()
    }
    private fun reloadBook(){
        if(books.isNotEmpty())
            myRoot.btn_bible_book.text = books[bookNum -1].name
//        bibleViewModel.getVerses(bookNum.toString(),"1").observe(this, Observer { verses ->
//            reloadBible( verses, 0) // note: always start at the beginning
//        })
        chapter=1
        reloadChapter(0)
        reloadNotes()
    }
    private fun reloadBible( verseList:List<Verse>, verseNum:Int){
        //Toast.makeText(requireContext(),eventList.size.toString(), Toast.LENGTH_SHORT).show()
        adapter.submitList(verseList)
        //adapter2.notifyDataSetChanged()
        bibleRecyclerView.adapter = adapter


        doAsync {
            //Execute all the long running tasks here
                try {
                    Thread.sleep(400)
                    uiThread {
                        bibleRecyclerView.smoothSnapToPosition(verseNum)
                    }
                }catch (e: InterruptedException) {
                    // do something if thread doesn't work
                }

            }


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
        for (i in 1..books[bookNum-1].chapterCount)
            listItems.add("Chapter "+ i)

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, listItems)
        var list = ListView(requireContext())
        list.adapter = adapter
        list.setOnItemClickListener { parent, view, position, id ->
            chapter = position + 1
            reloadChapter( 0)
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
        var listItems = books.filter { it.bookNumber <= 19 }.map { it.name }

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, listItems)
        var list = ListView(requireContext())
        list.adapter = adapter
        list.setOnItemClickListener { parent, view, position, id ->
            bookNum = position + 1
            reloadBook()
            dialoge.dismiss()
        }

        dialoge.setView(list)
        dialoge.show()
    }



    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main, menu);

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
