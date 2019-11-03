package edu.calbaptist.bible_ly

import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.DatePicker
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.event_detailed_fragment.view.*
import kotlinx.android.synthetic.main.student_detailed_fragment.view.*
import java.io.IOException
import java.util.*


private var studentPath:String? = ""
private var student:User? = null
private lateinit var myView: View


class StudentDialog: DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
       // val rootView = inflater.inflate(R.layout.fraglayout, container)
        studentPath = arguments?.getString("studentPath")

        getUser(studentPath!!){
            student = it
            updateui()
        }
        return createDialog()
    }
    private fun updateui(){
        myView.tv_student_frag_username.setText(student!!.userName)
        myView.tv_student_frag_email.setText(student!!.email)
        myView.tv_student_frag_lastname.setText(student!!.lastName)
        myView.tv_student_frag_name.setText(student!!.firstName)
        myView.iv_student_frag_logo.setGlide(student!!.photoID)
        myView.ib_student_frag_email.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("mailto:") // only email apps should handle this
            intent.putExtra(Intent.EXTRA_EMAIL, student!!.email)
            intent.putExtra(Intent.EXTRA_SUBJECT,"")
            if (intent.resolveActivity(activity!!.packageManager) != null) {
                startActivity(intent)
            }
        }

    }
    private fun createDialog():Dialog{
      val  view =LayoutInflater.from(requireContext()).inflate(R.layout.student_detailed_fragment, null)
        myView = view
   //     val view = layoutInflater.inflate(R.layout.event_detailed_fragment, null)


        var dialogeBuilder = AlertDialog.Builder(requireContext())
           // .setTitle("New Event")

            .setNegativeButton("Close", DialogInterface.OnClickListener { dialog, which ->
                //Action goes here
            })
           .setCancelable(false)
            .setView(view)//.create()


        var dialoge = dialogeBuilder.create()
        dialoge.show()

        return dialoge
    }
   companion object{
       fun newInstance(studentPath:String):DialogFragment{
           val frag = StudentDialog()
           val args = Bundle()
        /*   args.putBoolean("isNew", isNew)
           args.putBoolean("isTeacher", isTeacher)
           args.putString("classPath",classPath)*/
           args.putString("studentPath",studentPath)
           frag.arguments = args
           return frag
       }
   }
}