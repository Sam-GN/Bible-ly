package edu.calbaptist.bible_ly

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
import kotlinx.android.synthetic.main.event_detailed_fragment.view.*
import java.util.*

private lateinit var clss:Class
private lateinit var event:Event
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
        getClass(classPath!!){ clazz ->
            clss = clazz
            if(!isNew!!)
                getEvent(eventPath!!){
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
            if(clss.teacher!!.email != MainActivity.user.email){
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
            var datePicker = DatePicker(requireContext())

            var dialoge2 = AlertDialog.Builder(requireContext())
                .setTitle("Pick Date")
                .setCancelable(false)
                .setNegativeButton("Close", DialogInterface.OnClickListener { dialog, which ->
                    //Action goes here
                })
                .setPositiveButton("Select", DialogInterface.OnClickListener { dialog, which ->
                    //view.btn_event_frag_date.tag = datePicker.getCalendarFromDatePicker()
                    getTime(view.btn_event_frag_date,requireContext(),datePicker.getCalendarFromDatePicker())

                })
                .setView(datePicker).create()

            dialoge2.show()
        }

        var dialogeBuilder = AlertDialog.Builder(requireContext())
           // .setTitle("New Event")

            .setNegativeButton("Close", DialogInterface.OnClickListener { dialog, which ->
                //Action goes here
            })
           .setCancelable(false)
            .setView(view)//.create()

        if(isTeacher!!)
            dialogeBuilder.setPositiveButton(if(isNew!!) "Create" else "Update", DialogInterface.OnClickListener { dialog, which ->

            })
        var dialoge = dialogeBuilder.create()
        dialoge.show()
        (dialoge as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            if(view.et_event_frag_title.text.toString().isEmpty()){
                Toast.makeText(requireContext(),"Title is required.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if(view.btn_event_frag_date.tag==null){
                Toast.makeText(requireContext(),"Date is required.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            var ev = Event(view.et_event_frag_title.text.toString(),(view.btn_event_frag_date.tag as Calendar).time
                ,view.et_event_frag_description.text.toString()
                ,clss)
            if(isNew!!) {
                var ref = FirebaseFirestore.getInstance().collection("Event").document()
                ref.set(ev)
            }else{
                var ref = FirebaseFirestore.getInstance().document(eventPath!!)
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