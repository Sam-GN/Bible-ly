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
import edu.calbaptist.bible_ly.toLocalDateString
import kotlinx.android.synthetic.main.list_event_item_board.view.*
import kotlinx.android.synthetic.main.list_event_item_board.view.notification_date
import kotlinx.android.synthetic.main.list_event_item_board.view.notification_title
import kotlinx.android.synthetic.main.list_notification_item_board.view.*


open class BoardAdapter(query: Query, private val listener: OnBoardItemSelectedListener) :
    FirestoreAdapter<BoardAdapter.ViewHolder>(query) {

    interface OnBoardItemSelectedListener {

        fun onBoardItemSelected(boardItem: DocumentSnapshot)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
       // if(viewType==0)
            return ViewHolder(inflater.inflate(R.layout.list_event_item_board, parent, false))
       // else
         //   return ViewHolder(inflater.inflate(R.layout.list_notification_item_board, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getSnapshot(position), listener)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(
            snapshot: DocumentSnapshot,
            listener: OnBoardItemSelectedListener?
        ) {

//            val event = snapshot.toObject(Event::class.java) ?: return
//
//            val resources = itemView.resources
//
//            // Load image
//           /* Glide.with(itemView.restaurantItemImage.context)
//                .load(restaurant.photo)
//                .into(itemView.restaurantItemImage)*/
//
//           /* val numRatings: Int = event.numRatings
//
//            itemView.restaurantItemName.text = event.name
//            itemView.restaurantItemRating.rating = event.avgRating.toFloat()
//            itemView.restaurantItemCity.text = event.city
//            itemView.restaurantItemCategory.text = event.category
//            itemView.restaurantItemNumRatings.text = resources.getString(
//                R.string.fmt_num_ratings,
//                numRatings)
//            itemView.restaurantItemPrice.text = RestaurantUtil.getPriceString(event)*/
//
//            Log.i("BoardAdapter",event.name)
//            itemView.notification_title.text = event.name
//            itemView.notification_date.text = event.date!!.toLocalDateString(true)
//            itemView.notification_class.text = event.clss!!.name
//            // Click listener
//            itemView.setOnClickListener {
//                listener?.onBoardItemSelected(snapshot)
//            }
        }
    }
}
