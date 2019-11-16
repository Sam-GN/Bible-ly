package edu.calbaptist.bible_ly

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
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




private lateinit var note:Note
private var notePath:String? = ""
private var book:String? = ""
private var verseChapter:String? = ""
private var verseNum:String? = ""
private var verseText:String? = ""
private var isNew:Boolean? = true
private lateinit var myView: View


private var mapofClasses:Map<String,String>? = null


class NoteDialog: DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
       // val rootView = inflater.inflate(R.layout.fraglayout, container)
        book = arguments?.getString("book")
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
        myView.tv_note_frag_verse_num.text = "$book: Chapter $verseChapter - $verseNum"
        myView.tv_note_frag_verse_text.text = verseText
        myView.tv_note_frag_date.text = Date().toLocalDateString(false)


        myView.sp_note_frag_note_type.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
               if(position==0){
                   myView.sp_note_frag_note_class.visibility = View.GONE
               }
                else
                   myView.sp_note_frag_note_class.visibility = View.VISIBLE
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
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
            myView.et_note_frag_note.setText( note.noteText)
            myView.et_note_frag_title.setText( note.noteTitle)
            myView.sp_note_frag_note_type.setSelection(
                when (note.type){
                    "For Self" -> 0
                    "For Class" -> 1
                    else -> -1
                }
            )
            if(note.type==
                "For Self"){
                myView.sp_note_frag_note_class.visibility = View.GONE
            }

        }
    }
    private fun createDialog():Dialog{
      val  view =LayoutInflater.from(requireContext()).inflate(R.layout.note_detailed_fragment, null)
        myView = view
   //     val view = layoutInflater.inflate(R.layout.event_detailed_fragment, null)




        var dialogeBuilder = AlertDialog.Builder(requireContext())
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
                notePath!!, myView.sp_note_frag_note_type.selectedItem.toString(),
                if( myView.sp_note_frag_note_type.selectedItemPosition !=0)  mapofClasses!!.filterValues { it == myView.sp_note_frag_note_class.selectedItem.toString() }.keys.first() else ""
            )

            dialoge.dismiss()
        }
        if(isNew!!)
            updateui()
        return dialoge
    }
   companion object{
       fun newInstance(isNew:Boolean,notePath:String,book:String,verseNum: String,verseChapter: String,verseText: String):DialogFragment{
           val frag = NoteDialog()
           val args = Bundle()
           args.putBoolean("isNew", isNew)
           args.putString("notePath",notePath)
           args.putString("book",book)
           args.putString("verseNum",verseNum)
           args.putString("verseChapter",verseChapter)
           args.putString("verseText",verseText)
           frag.arguments = args
           return frag
       }
   }
}