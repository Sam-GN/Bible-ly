package edu.calbaptist.bible_ly.ui.notes

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.note_detailed_fragment.view.*
import java.util.*
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.calbaptist.Comment_ly.adapter.CommentMutableListAdapter
import android.content.res.Configuration
import android.content.res.Resources
import android.view.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import android.widget.LinearLayout
import edu.calbaptist.bible_ly.*
import edu.calbaptist.bible_ly.activity.MainActivity


private lateinit var note: Note
private var notePath:String? = ""
private var book:String? = ""
private var bookTitle:String? = ""
private var verseChapter:String? = ""
private var verseNum:String? = ""
private var verseText:String? = ""
private var isNew:Boolean? = true
@SuppressLint("StaticFieldLeak")
private lateinit var myView: View
private var prevComment = ""
private lateinit var noteDialogViewModel: NoteDialogViewModel
private lateinit var commentsRecyclerView: RecyclerView
lateinit var linearLayoutManager: LinearLayoutManager
lateinit var adapter: CommentMutableListAdapter
var currentItemPosition =0
private var mapofClasses:Map<String,String>? = null

class NoteDialog: DialogFragment(), CommentMutableListAdapter.OnCommentItemMoreSelectedListener {
    override fun OnCommentItemMoreSelectedListener(v:View, item: CommentCardViewItem, position: Int) {

        currentItemPosition = position
        showNotePopup(v,item)

    }
    override fun onDismiss(dialog: DialogInterface?) {
        super.onDismiss(dialog)
        MainActivity.currentNoteID = ""
    }
    private fun showNotePopup(view: View,cmnt : CommentCardViewItem) {
        var popup: PopupMenu?
        popup = PopupMenu(view.context, view)
        popup.inflate(R.menu.menu_note_comment_item_more)
        if(note.user!!.email == MainActivity.user.email && cmnt.user!!.email!= MainActivity.user.email){
            popup.menu.removeItem(R.id.note_comment_more_edit)
        }
        popup.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item: MenuItem? ->

            when (item!!.itemId) {
                R.id.note_comment_more_edit -> {
                    val ll = LinearLayout(context)
                    val et = EditText(context)
                    val param = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT)
                    param.setMargins(10.toDp(Resources.getSystem().displayMetrics),0,10.toDp(Resources.getSystem().displayMetrics),0)
                    et.layoutParams =param
                    ll.addView(et)
                    et.setText(cmnt.text)
                    val dialoge = AlertDialog.Builder(requireContext())
                        .setCancelable(false)
                        .setTitle(getString(R.string.comment_dialog_title))
                        .setNegativeButton(getString(R.string.close), DialogInterface.OnClickListener { dialog, which ->
                            //Action goes here
                        })
                        .setPositiveButton(getString(R.string.save), DialogInterface.OnClickListener { dialog, which ->
                            if(et.text.toString()!="")
                                FirestoreRepository().editComment(cmnt.path,et.text.toString())

                        })
                        .create()
                    dialoge.setView(ll)

                    dialoge.show()

                }



                R.id.note_comment_more_delete -> {
                    val dialoge = AlertDialog.Builder(requireContext())
                        .setCancelable(false)
                        .setTitle(getString(R.string.comment_delete_title))
                        .setNegativeButton(getString(R.string.no), DialogInterface.OnClickListener { dialog, which ->
                            //Action goes here
                        })
                        .setPositiveButton(getString(R.string.yes), DialogInterface.OnClickListener { dialog, which ->
                            FirestoreRepository().deleteComment(cmnt.path)

                        })
                        .create()

                    dialoge.show()

                }

            }

            true
        })

        popup.show()
    }
    interface Callback {
        fun onDismissed(isRotating:Boolean)
    }
    override fun onConfigurationChanged(newConfig: Configuration) {

        super.onConfigurationChanged(newConfig)

        targetFragment?.let { fragment ->
            (fragment as Callback).onDismissed(true)

        }

        dismiss()

    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        retainInstance = true
        book = arguments?.getString("book")
        bookTitle = arguments?.getString("bookTitle")
        verseChapter = arguments?.getString("verseChapter")
        verseNum = arguments?.getString("verseNum")
        verseText = arguments?.getString("verseText")
        notePath = arguments?.getString("notePath")
        isNew = arguments?.getBoolean("isNew")

        if(!isNew!!)
            FirestoreRepository().getNote(notePath!!){ notee ->
                note = notee
                updateui()
            }

        return createDialog()
    }
    @SuppressLint("SetTextI18n")
    private fun updateui(){

        myView.tv_note_frag_verse_num.text = "$bookTitle:"+getString(
            R.string.chapter
        ) +"$verseChapter - $verseNum"
        myView.tv_note_frag_verse_text.text =
            verseText
        myView.tv_note_frag_date.text = (if(isNew!!) Date() else note.date!!).toLocalDateString(false)



        myView.sw_note_frag_share.setOnCheckedChangeListener{ _, isChecked ->
            if(!isChecked){
                myView.sp_note_frag_note_class.visibility = View.GONE
            }
            else
                myView.sp_note_frag_note_class.visibility = View.VISIBLE
        }



        FirestoreRepository().getClassesList { list ->
            mapofClasses = list
            var list2 = mutableListOf<String>()
            for (aa in list){
                list2.add(aa.value)
            }
            val aa = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, list2)
            // Set layout to use when the list of choices appear
            aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            myView.sp_note_frag_note_class.adapter = aa
            if(!isNew!!){
                var i =0
                for( c in list) {
                    if(c.key == note.clss){
                        myView.sp_note_frag_note_class.setSelection(i)
                        break
                    }
                    i++

                }
            }
        }
        if(!isNew!!){
            noteDialogViewModel =
                ViewModelProviders.of(this).get(NoteDialogViewModel::class.java)


            commentsRecyclerView = myView.findViewById(
                R.id.rv_note_comments
            )
            linearLayoutManager = LinearLayoutManager(context)
            commentsRecyclerView.layoutManager =
                linearLayoutManager
            adapter = CommentMutableListAdapter(this)
            commentsRecyclerView.adapter =
                adapter

            noteDialogViewModel.getComments(notePath!!).observe(this, androidx.lifecycle.Observer {
                list ->
                if(list != null){
                    var i =0
                    var map = mutableMapOf<String,String>()
                    var list2 = mutableListOf<CommentCardViewItem>()
                        for (cmnt in list){
                        if(map[cmnt.user!!.email] == null){
                           /* if(cmnt.user!!.email == MainActivity.user.email)
                                map[cmnt.user!!.email] = "Me"
                            else*/
                                map[cmnt.user!!.email] = getString(R.string.user)+" ${++i}"

                        }
                        list2.add(0,
                            CommentCardViewItem(
                                cmnt.path,
                                map[cmnt.user!!.email]!!,
                                cmnt.user!!,
                                cmnt.text,
                                cmnt.date
                            )
                        )
                    }
                    adapter.submitList(list2)
                    if(list.isNotEmpty()){
                        doAsync {
                            Thread.sleep(200)
                            uiThread {
                                commentsRecyclerView.layoutManager!!.scrollToPosition(
                                    currentItemPosition
                                )
                            }
                         }
                    }
                }
            })


            if(MainActivity.user.email!= note.user!!.email){
                myView.et_note_frag_title.isFocusable = false
                myView.et_note_frag_note.isFocusable = false
                myView.sw_note_frag_share.isEnabled = false
                myView.sp_note_frag_note_class.isEnabled = false
            }
            myView.et_note_frag_note.setText( note.noteText)
            myView.et_note_frag_title.setText( note.noteTitle)
            myView.sw_note_frag_share.isChecked = note.shared
            if(!note.shared){
                myView.sp_note_frag_note_class.visibility = View.GONE
            }

        }
        else{
            myView.tv_note_frag_comment_label.visibility = View.GONE
            myView.ll_note_frag_comment.visibility = View.GONE
        }
        myView.ib_note_frag_send.setOnClickListener {
            prevComment = myView.et_note_frag_comment_text.text.toString()
            if( myView.et_note_frag_comment_text.text.toString()!="") {
                FirestoreRepository().addComment(
                    notePath!!,
                    myView.et_note_frag_comment_text.text.toString()
                )
                //send notification to the note's creator
                if (note!!.user!!.email != MainActivity.user.email)
                    sendNotification(
                        "SendToUser_" + note!!.user!!.email.replace(
                            "@",
                            "_"
                        ),
                        getString(R.string.comment_new),
                        prevComment,
                        requireContext(),
                        note.noteID
                    )

                //send notification to every body in a note's comments section
                FirestoreRepository()
                    .getCommentInvolvedUsers(note!!.noteID) { involvedUserEmailList ->
                    involvedUserEmailList.forEach { email ->
                        if (email != MainActivity.user.email && note!!.user!!.email != email)
                            sendNotification(
                                "SendToUser_" + email.replace("@", "_"),
                                getString(R.string.comment_new),
                                prevComment,
                                requireContext(),
                                note.noteID
                            )


                    }
                }

                myView.et_note_frag_comment_text.setText("")
                currentItemPosition = 0
            }
        }
    }
    private fun createDialog():Dialog{
      val  view =LayoutInflater.from(requireContext()).inflate(R.layout.note_detailed_fragment, null)
        myView = view
        var dialogeBuilder = AlertDialog.Builder(requireContext(),
            R.style.full_screen_dialog
        )
           // .setTitle("New Event")
            .setNegativeButton(getString(R.string.close), DialogInterface.OnClickListener { dialog, which ->
                targetFragment?.let { fragment ->
                    (fragment as Callback).onDismissed(false)

                }
            })
            .setPositiveButton(getString(R.string.save),DialogInterface.OnClickListener { dialog, which ->
                //Action goes here
            })
           .setCancelable(false)
            .setView(view)//.create()

        var dialoge = dialogeBuilder.create()

        dialoge.show()
        (dialoge as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            if (view.et_note_frag_note.text.toString().isEmpty()) {
                Toast.makeText(requireContext(), getString(R.string.note_required), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            FirestoreRepository().saveNote(
                isNew!!,
                book!!, verseNum!!, verseChapter!!, verseText!!
                , myView.et_note_frag_note.text.toString(), myView.et_note_frag_title.text.toString(),
                notePath!!, myView.sw_note_frag_share.isChecked,
                if( myView.sw_note_frag_share.isChecked)  mapofClasses!!.filterValues { it == myView.sp_note_frag_note_class.selectedItem.toString() }.keys.first() else ""
            )
            targetFragment?.let { fragment ->
                (fragment as Callback).onDismissed(false)

            }
            dialoge.dismiss()
        }
        if(isNew!!)
            updateui()
        dialoge.setOnKeyListener { dialog, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK &&
                event.action == KeyEvent.ACTION_UP
            ) {
                targetFragment?.let { fragment ->
                    (fragment as Callback).onDismissed(false)

                }

                 true
            }
             false
        }
        return dialoge
    }
   companion object{
       fun newInstance(isNew:Boolean,notePath:String,book:String,verseNum: String,verseChapter: String,verseText: String,bookTitle:String):DialogFragment{
           MainActivity.currentNoteID = notePath
           val frag = NoteDialog()
           val args = Bundle()
           args.putBoolean("isNew", isNew)
           args.putString("notePath",notePath)
           args.putString("book",book)
           args.putString("bookTitle",bookTitle)
           args.putString("verseNum",verseNum)
           args.putString("verseChapter",verseChapter)
           args.putString("verseText",verseText)
           frag.arguments = args
           return frag
       }

   }

}