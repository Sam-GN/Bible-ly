package edu.calbaptist.bible_ly.ui.dialoge

import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import edu.calbaptist.bible_ly.FirestoreRepository
import edu.calbaptist.bible_ly.R
import edu.calbaptist.bible_ly.User
import edu.calbaptist.bible_ly.setGlide
import kotlinx.android.synthetic.main.student_detailed_fragment.view.*


private var studentPath:String? = ""
private var student: User? = null
private lateinit var myView: View


class StudentDialog: DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
       // val rootView = inflater.inflate(R.layout.fraglayout, container)
        studentPath = arguments?.getString("studentPath")

        FirestoreRepository()
            .getUser(studentPath!!){
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
        myView.iv_student_frag_logo.setGlide(student!!.photoID,true)
        myView.ib_student_frag_email.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("mailto:"+ student!!.email) // only email apps should handle this
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

            .setNegativeButton(getString(R.string.close), DialogInterface.OnClickListener { dialog, which ->
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