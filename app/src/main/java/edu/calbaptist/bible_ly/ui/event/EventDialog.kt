package edu.calbaptist.bible_ly.ui.event

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.DatePicker
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.firebase.firestore.FirebaseFirestore
import edu.calbaptist.bible_ly.*
import kotlinx.android.synthetic.main.event_detailed_fragment.view.*
import java.util.*

private lateinit var clss: BiblelyClass
private lateinit var event: Event
private var classPath:String? = ""
private var eventPath:String? = ""
private var isNew:Boolean? = true
private var isTeacher:Boolean? = true
private lateinit var myView: View


class EventDialog: DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
       // val rootView = inflater.inflate(R.layout.fraglayout, container)
        classPath = arguments?.getString("classPath")
        eventPath = arguments?.getString("eventPath")
        isNew = arguments?.getBoolean("isNew")
        isTeacher = arguments?.getBoolean("isTeacher")
        FirestoreRepository()
            .getClass(classPath!!){ clazz ->
            clss = clazz
            if(!isNew!!)
                FirestoreRepository().getEvent(eventPath!!){
                    event = it
                    updateui()
                }
            else
                updateui()
        }
        return createDialog()
    }
    private fun updateui(){
        myView.tf_event_frag_className.editText!!.setText(clss!!.name)
        myView.tf_event_frag_className.editText!!.isFocusable = false
        if(!isNew!!){
            myView.et_event_frag_title.setText( event.name)
            myView.btn_event_frag_date.text =( event.date?.toLocalDateString(true))
            myView.btn_event_frag_date.tag = Calendar.getInstance().apply { time= event.date}
            myView.et_event_frag_description.setText(event.description)
            if(!isTeacher!!){
                myView.et_event_frag_title.isFocusable = false
                myView.btn_event_frag_date.isClickable = false
                myView.et_event_frag_description.isFocusable = false
            }


        }
    }
    private fun createDialog():Dialog{
      val  view =LayoutInflater.from(requireContext()).inflate(R.layout.event_detailed_fragment, null)
        myView = view
   //     val view = layoutInflater.inflate(R.layout.event_detailed_fragment, null)


        view.btn_event_frag_date.setOnClickListener {
            val datePicker = DatePicker(requireContext())

            val dialoge2 = AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.pick_date))
                .setCancelable(false)
                .setNegativeButton(getString(R.string.close), DialogInterface.OnClickListener { dialog, which ->
                    //Action goes here
                })
                .setPositiveButton(getString(R.string.select), DialogInterface.OnClickListener { dialog, which ->
                    //view.btn_event_frag_date.tag = datePicker.getCalendarFromDatePicker()
                    getTime(
                        view.btn_event_frag_date,
                        requireContext(),
                        datePicker.getCalendarFromDatePicker()
                    )

                })
                .setView(datePicker).create()

            dialoge2.show()
        }

        val dialogeBuilder = AlertDialog.Builder(requireContext())
           // .setTitle("New Event")

            .setNegativeButton(getString(R.string.close), DialogInterface.OnClickListener { dialog, which ->
                //Action goes here
            })
           .setCancelable(false)
            .setView(view)//.create()

        if(isTeacher!!)
            dialogeBuilder.setPositiveButton(if(isNew!!) getString(
                R.string.create
            ) else getString(R.string.save), DialogInterface.OnClickListener { dialog, which ->

            })
        var dialoge = dialogeBuilder.create()
        dialoge.show()
        (dialoge as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            if(view.et_event_frag_title.text.toString().isEmpty()){
                Toast.makeText(requireContext(),getString(R.string.title_required), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if(view.btn_event_frag_date.tag==null){
                Toast.makeText(requireContext(),getString(R.string.date_required), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if(isNew!!) {
                var ref = FirebaseFirestore.getInstance().document(classPath!!).collection("events").document()
               // var ref = FirebaseFirestore.getInstance().collection("Event").document()
                var ev = Event(
                    ref.path,
                    view.et_event_frag_title.text.toString(),
                    (view.btn_event_frag_date.tag as Calendar).time
                    ,
                    view.et_event_frag_description.text.toString()
                    ,
                    clss,
                    Date()
                )

                ref.set(ev)
                sendNotification(
                    classPath!!.split(
                        "/"
                    )[1],
                    getString(R.string.notification_new_event),
                    getString(R.string.notification_new_event_text, view.et_event_frag_title.text),
                    requireContext(),
                    ref.path
                )
            }else{
                var ref = FirebaseFirestore.getInstance().document(eventPath!!)
                var ev = Event(
                    ref.path,
                    view.et_event_frag_title.text.toString(),
                    (view.btn_event_frag_date.tag as Calendar).time
                    ,
                    view.et_event_frag_description.text.toString()
                    ,
                    clss
                )
                ref.set(ev)
            }
            dialoge.dismiss()
        }
        return dialoge
    }
   companion object{
       fun newInstance(isNew:Boolean,isTeacher:Boolean,classPath:String, eventPath:String):DialogFragment{
           val frag = EventDialog()
           val args = Bundle()
           args.putBoolean("isNew", isNew)
           args.putBoolean("isTeacher", isTeacher)
           args.putString("classPath",classPath)
           args.putString("eventPath",eventPath)
           frag.arguments = args
           return frag
       }
   }
}