package edu.calbaptist.bible_ly.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import edu.calbaptist.bible_ly.User
import edu.calbaptist.bible_ly.Event
import edu.calbaptist.bible_ly.MainActivity
import edu.calbaptist.bible_ly.R
import kotlinx.android.synthetic.main.list_class_single_item_student.view.*
import kotlinx.android.synthetic.main.list_event_item_board.view.*
import java.io.IOException




open class StudentAdapter (query: Query, private val listener: OnStudentItemSelectedListener, private val listenerMore: OnMoreItemSelectedListener) :
    FirestoreAdapter<StudentAdapter.ViewHolder>(query) {


    interface OnStudentItemSelectedListener {

        fun onStudentItemSelected(StudentItem: DocumentSnapshot)
    }
    interface OnMoreItemSelectedListener {

        fun onMoreItemSelected(view: View,StudentItem: DocumentSnapshot)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        // if(viewType==0)
        return ViewHolder(inflater.inflate(R.layout.list_class_single_item_student, parent, false))
        // else
        //   return ViewHolder(inflater.inflate(R.layout.list_notification_item_board, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getSnapshot(position), listener,listenerMore)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(
            snapshot: DocumentSnapshot,
            listener: OnStudentItemSelectedListener?,
            listenerMore:OnMoreItemSelectedListener?
        ) {

            val biblelyStudent = snapshot.toObject(User::class.java) ?: return


           // Log.i("BoardAdapter",biblelyStudent.name)
            var ivLogo = itemView.findViewById<ImageView>(R.id.iv_class_students)
            if(biblelyStudent.photoID != "") {
                try {
                    Glide.with(ivLogo.context)

                        .applyDefaultRequestOptions( RequestOptions()
                            .placeholder(R.mipmap.ic_launcher2_round)
                            .error(R.mipmap.ic_launcher2_round)
                            )
                        //.applyDefaultRequestOptions( RequestOptions())
                        //.setDefaultRequestOptions(RequestOptions())
                        .load(biblelyStudent.photoID)
                       // .apply(RequestOptions().transforms(CenterCrop(), RoundedCorners(30)))
                        //.apply(RequestOptions.centerCropTransform())
                         .apply(RequestOptions.circleCropTransform())

                        .into(ivLogo)

                }catch (e: IOException){
                    ivLogo.setImageResource(R.mipmap.ic_launcher2_round)
                }

            } else {
                ivLogo.setImageResource(R.mipmap.ic_launcher2_round)
            }
            itemView.tv_class_student_name.text = biblelyStudent.userName
            itemView.tv_class_student_email.text = biblelyStudent.email

            // Click listener
            itemView.setOnClickListener {
                listener?.onStudentItemSelected(snapshot)
            }
            itemView.ib_class_students_options.setOnClickListener {
                listenerMore?.onMoreItemSelected(itemView.ib_class_students_options, snapshot)
            }
        }
    }
}
