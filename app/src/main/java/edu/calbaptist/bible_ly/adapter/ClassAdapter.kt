package edu.calbaptist.bible_ly.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import edu.calbaptist.bible_ly.Class
import edu.calbaptist.bible_ly.Event
import edu.calbaptist.bible_ly.R
import kotlinx.android.synthetic.main.list_class_item_class.view.*
import kotlinx.android.synthetic.main.list_event_item_board.view.*

open class ClassAdapter (query: Query, private val listener: OnClassItemSelectedListener) :
    FirestoreAdapter<ClassAdapter.ViewHolder>(query) {

    interface OnClassItemSelectedListener {

        fun onClassItemSelected(classItem: DocumentSnapshot)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        // if(viewType==0)
        return ViewHolder(inflater.inflate(R.layout.list_class_item_class, parent, false))
        // else
        //   return ViewHolder(inflater.inflate(R.layout.list_notification_item_board, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getSnapshot(position), listener)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(
            snapshot: DocumentSnapshot,
            listener: OnClassItemSelectedListener?
        ) {

            val biblelyClass = snapshot.toObject(Class::class.java) ?: return

            val resources = itemView.resources

            // Load image
 /*Glide.with(itemView.restaurantItemImage.context)
                 .load(restaurant.photo)
                 .into(itemView.restaurantItemImage)*/

/*
 val numRatings: Int = event.numRatings
 
             itemView.restaurantItemName.text = event.name
             itemView.restaurantItemRating.rating = event.avgRating.toFloat()
             itemView.restaurantItemCity.text = event.city
             itemView.restaurantItemCategory.text = event.category
             itemView.restaurantItemNumRatings.text = resources.getString(
                 R.string.fmt_num_ratings,
                 numRatings)
             itemView.restaurantItemPrice.text = RestaurantUtil.getPriceString(event)*/


            Log.i("BoardAdapter",biblelyClass.name)
            itemView.tv_class_item_title.text = biblelyClass.name
            itemView.tv_class_item_teacher.text = biblelyClass.teacher?.userName
            // Click listener
            itemView.setOnClickListener {
                listener?.onClassItemSelected(snapshot)
            }
        }
    }
}
