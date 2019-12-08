package edu.calbaptist.bible_ly.ui.bible


import android.content.res.Configuration
import android.os.Bundle
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


var selectedNote :NoteCardViewItem? = null

class BibleFragment : Fragment(), BibleMutableListAdapter.OnBibleItemSelectedListener,
    BibleMutableListAdapter.OnBibleItemLongSelectedListener ,
    NoteMutableListAdapter.OnNoteItemSelectedListener,NoteMutableListAdapter.OnNoteItemLongSelectedListener,
    NoteDialog.Callback{

    private lateinit var bibleViewModel: BibleViewModel

    private lateinit var bibleRecyclerView: RecyclerView
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var adapter: BibleMutableListAdapter

    private lateinit var noteRecyclerView: RecyclerView
    private lateinit var noteLinearLayoutManager: LinearLayoutManager
    private lateinit var noteAdapter: NoteMutableListAdapter
    private lateinit var noNoteTV: TextView

    var chapter = 1
    private var bookNum = 1

    private var books = listOf<BibleKey>()
    private var allNotes = listOf<NoteCardViewItem>()
    private lateinit var myRoot:View


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
    }

    override fun onBibleItemLongSelected(
        v: View,
        book:String,
        verseNum: String,
        verseChapter: String,
        verseText: String
    ) {
        showBiblePopup(v,book,verseNum,verseChapter,verseText)
    }


    private fun showBiblePopup(view: View, book:String, verseNum: String,
                               verseChapter: String,
                               verseText: String) {
        val popup: PopupMenu?
        popup = PopupMenu(view.context, view)
        popup.inflate(R.menu.menu_bible_item_more)
        popup.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item: MenuItem? ->

            when (item!!.itemId) {
                R.id.bible_verse_add_note-> {
                    val d = NoteDialog.newInstance(true, "",book,verseNum,verseChapter,verseText,books[bookNum-1].name)
                    val fm = requireActivity().supportFragmentManager
                    d.show(fm,"NoteDialog")
                }
                R.id.bible_verse_share ->{
                    shareIntent(requireContext(),"${books[bookNum].name}: ${getString(R.string.chapter)}:$verseChapter-${getString(R.string.verse)}: $verseNum\n$verseText",
                        "",getString(R.string.share_verse))

                }

            }

            true
        })

        popup.show()
    }

    private fun showNotePopup(view: View,note :NoteCardViewItem) {
        val popup: PopupMenu?
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
                    val dialoge = AlertDialog.Builder(requireContext())
                        .setCancelable(false)
                        .setTitle(getString(R.string.delete_note_title))
                        .setNegativeButton(getString(R.string.no)) { _, _ ->

                        }
                        .setPositiveButton(getString(R.string.yes)) { _, _ ->
                            FirestoreRepository().deleteNote(note.noteID)
                        }
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
        MainActivity.navigateDrawer()
        super.onConfigurationChanged(newConfig)
    }



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val bookNumBundle = arguments?.getString("bookNum") ?: ""

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

        reloadBook()


        bibleViewModel.getNotes().observe(this, Observer {list ->

            allNotes = list
            reloadNotes( )


        })
        myRoot.ib_bible_next_chapter.setOnClickListener {
            if(books.isNotEmpty() && chapter!=books[bookNum-1].chapterCount) {
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
            myRoot.btn_bible_book.text = books[bookNum -1].name
        })

        return myRoot
    }
    private fun reloadNotes(){
        val list = allNotes.filter { note -> note.book == bookNum.toString() }
        val list2 = mutableListOf<NoteCardViewItem>()
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
    }
    private fun reloadChapter(  verseNum: Int){
        bibleViewModel.getVerses(bookNum.toString(), chapter.toString()).observe(this, Observer { list ->
            reloadBible( list, verseNum)
        })
        myRoot.btn_bible_chapter.text = getString(R.string.note_chapter_num,chapter.toString()) .replace(":","")
    }
    private fun reloadBook(){
        if(books.isNotEmpty())
            myRoot.btn_bible_book.text = books[bookNum -1].name
        chapter=1
        reloadChapter(0)
        reloadNotes()
    }
    private fun reloadBible( verseList:List<Verse>, verseNum:Int) {
        adapter.submitList(verseList)
        bibleRecyclerView.adapter = adapter

        doAsync {
            try {
                Thread.sleep(400)
                uiThread {
                    bibleRecyclerView.smoothSnapToPosition(verseNum)
                }
            } catch (e: InterruptedException) {

            }
        }
    }


    private fun selectChapterDialog(){

        val dialoge = AlertDialog.Builder(requireContext())
            .setCancelable(false)
            .setTitle(getString(R.string.select_chapter))
            .setNegativeButton(getString(R.string.close)) { _, _ ->

            }
            .create()

        val listItems = mutableListOf<String>()
        for (i in 1..books[bookNum-1].chapterCount)
            listItems.add(getString(R.string.chapter)+" "+ i)

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, listItems)
        val list = ListView(requireContext())
        list.adapter = adapter
        list.setOnItemClickListener { _, _, position, _ ->
            chapter = position + 1
            reloadChapter( 0)
            dialoge.dismiss()
        }

        dialoge.setView(list)
        dialoge.show()
    }

    private fun selectBookDialog() {

        val dialoge = AlertDialog.Builder(requireContext())
            .setCancelable(false)
            .setTitle(getString(R.string.select_book))
            .setNegativeButton(getString(R.string.close)) { _, _ ->

            }
            .create()

        // NOTE: hardcoded to only get first 19 books. We don't have the rest of the bible in firebase
        val listItems = books.filter { it.bookNumber <= 19 }.map { it.name }

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, listItems)
        val list = ListView(requireContext())
        list.adapter = adapter
        list.setOnItemClickListener { _, _, position, _ ->
            bookNum = position + 1
            reloadBook()
            dialoge.dismiss()
        }
        dialoge.setView(list)
        dialoge.show()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun RecyclerView.smoothSnapToPosition(position: Int, snapMode: Int = LinearSmoothScroller.SNAP_TO_START) {
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
