package edu.calbaptist.bible_ly.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import edu.calbaptist.bible_ly.Event
import edu.calbaptist.bible_ly.R
import edu.calbaptist.bible_ly.toLocalDateString
import kotlinx.android.synthetic.main.list_event_item_board.view.*


open class EventMutableListAdapter (private val listener: OnEventItemSelectedListener)
    : ListAdapter<Event, EventMutableListAdapter.EventViewHolder>(DiffCallback()) {
    interface OnEventItemSelectedListener {

        fun onEventItemSelected(classItem: Event)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(getItem(position),listener)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {

        val inflater = LayoutInflater.from(parent.context)
        // if(viewType==0)
        return EventViewHolder(
            inflater.inflate(
                R.layout.list_event_item_board,
                parent,
                false
            )
        )


    }

    class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(
            event: Event,
            listener: OnEventItemSelectedListener?
        ) {
            /*  if(!snapshot.reference.collection("students").whereEqualTo("email",MainActivity.user.email).get().isSuccessful) {
                  return
              }*/
//            val biblelyClass = snapshot.toObject(BiblelyClass::class.java) ?: return
//
//            val resources = itemView.resources

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


            //val event = snapshot.toObject(Event::class.java) ?: return

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


            itemView.notification_title.text = event.name
            itemView.notification_date.text = event.date!!.toLocalDateString(true)
            itemView.notification_class.text = event.clss!!.name
            // Click listener
            itemView.setOnClickListener {
                listener?.onEventItemSelected(event)
            }
        }
    }
        class DiffCallback : DiffUtil.ItemCallback<Event>() {
            override fun areItemsTheSame(oldItem: Event, newItem: Event): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean {
                return oldItem == newItem
            }
        }
}