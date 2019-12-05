package edu.calbaptist.bible_ly

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.event_detailed_fragment.view.*
import kotlinx.android.synthetic.main.list_bible_item.view.*
import kotlinx.android.synthetic.main.note_detailed_fragment.view.*
import java.util.*
import android.widget.AdapterView
import androidx.core.view.GravityCompat
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.calbaptist.Comment_ly.adapter.CommentMutableListAdapter
import edu.calbaptist.bible_ly.ui.bible.BibleViewModel
import android.app.Activity
import android.view.WindowManager
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.text.FieldPosition
import android.view.Gravity
import android.widget.LinearLayout







private lateinit var note:Note
private var notePath:String? = ""
private var book:String? = ""
private var bookTitle:String? = ""
private var verseChapter:String? = ""
private var verseNum:String? = ""
private var verseText:String? = ""
private var isNew:Boolean? = true
private lateinit var myView: View

private lateinit var noteDialogViewModel: NoteDialogViewModel

private lateinit var commentsRecyclerView: RecyclerView
lateinit var linearLayoutManager: LinearLayoutManager
lateinit var adapter: CommentMutableListAdapter
var currentItemPosition =0


private var mapofClasses:Map<String,String>? = null


class NoteDialog: DialogFragment(), CommentMutableListAdapter.OnCommentItemMoreSelectedListener {
    override fun OnCommentItemMoreSelectedListener(v:View,item: CommentCardViewItem,position: Int) {
        if(item.user!!.email == MainActivity.user.email){
            currentItemPosition = position
            showNotePopup(v,item)
        }
    }
    override fun onDismiss(dialog: DialogInterface?) {
        super.onDismiss(dialog)
//        val activity = activity
//        if (activity is DialogInterface.OnDismissListener) {
//            (activity as DialogInterface.OnDismissListener).onDismiss(dialog)
//        }
        MainActivity.currentNoteID = ""
    }
    private fun showNotePopup(view: View,cmnt :CommentCardViewItem) {
        var popup: PopupMenu? = null;
        popup = PopupMenu(view.context, view)
        popup.inflate(R.menu.menu_note_comment_item_more)




        popup.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item: MenuItem? ->

            when (item!!.itemId) {

                R.id.note_comment_more_edit-> {
                    var et = EditText(context)
                    et.setText(cmnt.text)
                    var dialoge = AlertDialog.Builder(requireContext())
                        .setCancelable(false)
                        .setTitle("Edit Comment")
                        .setNegativeButton("Close", DialogInterface.OnClickListener { dialog, which ->
                            //Action goes here
                        })
                        .setPositiveButton("Submit", DialogInterface.OnClickListener { dialog, which ->
                            if(et.text.toString()!="")
                                FirestoreRepository().editComment(cmnt.path,et.text.toString())

                        })
                        .create()
                    dialoge.setView(et)

                    dialoge.show()

                }



                R.id.note_comment_more_delete-> {
                    var dialoge = AlertDialog.Builder(requireContext())
                        .setCancelable(false)
                        .setTitle("Are you sure you want to delete this Comment?")
                        .setNegativeButton("No", DialogInterface.OnClickListener { dialog, which ->
                            //Action goes here
                        })
                        .setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, which ->
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
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

       // val rootView = inflater.inflate(R.layout.fraglayout, container)
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

        myView.tv_note_frag_verse_num.text = "$bookTitle: Chapter $verseChapter - $verseNum"
        myView.tv_note_frag_verse_text.text = verseText
        myView.tv_note_frag_date.text = Date().toLocalDateString(false)



        myView.sw_note_frag_share.setOnCheckedChangeListener{_,isChecked ->
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


            commentsRecyclerView = myView.findViewById(R.id.rv_note_comments)
            linearLayoutManager = LinearLayoutManager(context)
            commentsRecyclerView.layoutManager = linearLayoutManager
            adapter= CommentMutableListAdapter(this)
            commentsRecyclerView.adapter = adapter

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
                                map[cmnt.user!!.email] = "User ${++i}"

                        }
                        list2.add(0,CommentCardViewItem(cmnt.path,map[cmnt.user!!.email]!!,cmnt.user!!,cmnt.text,cmnt.date))
                    }
                    adapter.submitList(list2)
                    if(list.isNotEmpty()){
                        doAsync {
                            Thread.sleep(200)
                            uiThread {
                                commentsRecyclerView.layoutManager!!.scrollToPosition(currentItemPosition)
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
            if( myView.et_note_frag_comment_text.text.toString()!="") {
                FirestoreRepository().addComment(
                    notePath!!,
                    myView.et_note_frag_comment_text.text.toString()
                )
                //send notification to the note's creator
                if (note!!.user!!.email != MainActivity.user.email)
                    sendNotification(
                        "SendToUser_" + note!!.user!!.email.replace("@", "_"),
                        "New Comment",
                        myView.et_note_frag_comment_text.text.toString(),
                        requireContext(),
                        note.noteID
                    )

                //send notification to every body in a note's comments section
                FirestoreRepository().getCommentInvolvedUsers(note!!.noteID) { involvedUserEmailList ->
                    involvedUserEmailList.forEach { email ->
                        if (email != MainActivity.user.email && note!!.user!!.email != email)
                            sendNotification(
                                "SendToUser_" + email.replace("@", "_"),
                                "New Comment",
                                myView.et_note_frag_comment_text.text.toString(),
                                requireContext(),
                                note.noteID
                            )


                    }
                    // commentsRecyclerView.layoutManager!!.scrollToPosition(0)
                }
                myView.et_note_frag_comment_text.setText("")
                currentItemPosition = 0
            }
        }
    }
    private fun createDialog():Dialog{
      val  view =LayoutInflater.from(requireContext()).inflate(R.layout.note_detailed_fragment, null)
        myView = view
   //     val view = layoutInflater.inflate(R.layout.event_detailed_fragment, null)
//        val lptv = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
//        myView.layoutParams = lptv




        var dialogeBuilder = AlertDialog.Builder(requireContext(),R.style.full_screen_dialog)
           // .setTitle("New Event")
            .setNegativeButton("Close", DialogInterface.OnClickListener { dialog, which ->
                //Action goes here
            })
            .setPositiveButton("Save",DialogInterface.OnClickListener { dialog, which ->
                //Action goes here
            })
           .setCancelable(false)
            .setView(view)//.create()



        var dialoge = dialogeBuilder.create()

        dialoge.show()
        (dialoge as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            if (view.et_note_frag_note.text.toString().isEmpty()) {
                Toast.makeText(requireContext(), "Note is required.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            FirestoreRepository().saveNote(
                isNew!!,book!!, verseNum!!, verseChapter!!, verseText!!
                , myView.et_note_frag_note.text.toString(), myView.et_note_frag_title.text.toString(),
                notePath!!, myView.sw_note_frag_share.isChecked,
                if( myView.sw_note_frag_share.isChecked)  mapofClasses!!.filterValues { it == myView.sp_note_frag_note_class.selectedItem.toString() }.keys.first() else ""
            )

            dialoge.dismiss()
        }
        if(isNew!!)
            updateui()
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