package edu.calbaptist.bible_ly.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import edu.calbaptist.bible_ly.Event
import edu.calbaptist.bible_ly.R
import kotlinx.android.synthetic.main.list_class_single_item_event.view.*



open class ClassSingleEventAdapter(query: Query, private val listener: OnClassSingleEventItemSelectedListener) :
    FirestoreAdapter<ClassSingleEventAdapter.ViewHolder>(query) {

    interface OnClassSingleEventItemSelectedListener {

        fun onClassSingleEventItemSelected(ClassSingleEventItem: DocumentSnapshot)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
       // if(viewType==0)
            return ViewHolder(inflater.inflate(R.layout.list_class_single_item_event, parent, false))
       // else
         //   return ViewHolder(inflater.inflate(R.layout.list_notification_item_ClassSingleEvent, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getSnapshot(position), listener)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(
            snapshot: DocumentSnapshot,
            listener: OnClassSingleEventItemSelectedListener?
        ) {

            val event = snapshot.toObject(Event::class.java) ?: return

            val resources = itemView.resources

            // Load image
           /* Glide.with(itemView.restaurantItemImage.context)
                .load(restaurant.photo)
                .into(itemView.restaurantItemImage)*/

           /* val numRatings: Int = event.numRatings

            itemView.restaurantItemName.text = event.name
            itemView.restaurantItemRating.rating = event.avgRating.toFloat()
            itemView.restaurantItemCity.text = event.city
            itemView.restaurantItemCategory.text = event.category
            itemView.restaurantItemNumRatings.text = resources.getString(
                R.string.fmt_num_ratings,
                numRatings)
            itemView.restaurantItemPrice.text = RestaurantUtil.getPriceString(event)*/

            Log.i("ClassSingleEventAdapter",event.name)
            itemView.tv_class_event_name.text = event.name
            itemView.tv_class_event_description.text = event.description
            // Click listener
            itemView.setOnClickListener {
                listener?.onClassSingleEventItemSelected(snapshot)
            }
        }
    }
}
