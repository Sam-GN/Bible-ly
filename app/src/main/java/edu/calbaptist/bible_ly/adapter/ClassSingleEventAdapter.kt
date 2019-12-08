package edu.calbaptist.bible_ly.adapter

import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import edu.calbaptist.bible_ly.*
import edu.calbaptist.bible_ly.activity.MainActivity
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

            itemView.tv_class_event_name.text = event.name
            itemView.tv_class_event_description.text = event.description
            itemView.tv_class_event_date.text = event.date!!.toLocalDateString(true)
            // Click listener
            itemView.setOnClickListener {
                listener?.onClassSingleEventItemSelected(snapshot)
            }
            if(event.clss!!.teacher!!.email== MainActivity.user.email){
                itemView.ib_class_event_options.visibility = View.VISIBLE
                itemView.ib_class_event_options.setOnClickListener {
                    showPopup(it,snapshot)
                }
            }

        }
        private fun showPopup(view: View,snapshot: DocumentSnapshot) {
            var popup: PopupMenu? = null;
            popup = PopupMenu(view.context, view)
            popup.inflate(R.menu.menu_events_item_more)

            popup.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item: MenuItem? ->

                when (item!!.itemId) {
                    R.id.events_item_action_delete -> {

                        var dialoge = AlertDialog.Builder(view.context)
                            .setCancelable(false)
                            .setTitle(R.string.delete_event)
                            .setNegativeButton(R.string.no, DialogInterface.OnClickListener { dialog, which ->
                                //Action goes here
                            })
                            .setPositiveButton(R.string.yes, DialogInterface.OnClickListener { dialog, which ->
                                snapshot.reference.delete()
                                Toast.makeText(view.context,  R.string.event_deleted, Toast.LENGTH_SHORT).show()
                               // this.finish()
                            })
                            .create()

                        dialoge.show()

                    }
                   /* R.id.header2 -> {
                        Toast.makeText(this@MainActivity, item.title, Toast.LENGTH_SHORT).show();
                    }
                    R.id.header3 -> {
                        Toast.makeText(this@MainActivity, item.title, Toast.LENGTH_SHORT).show();
                    }*/
                }

                true
            })

            popup.show()
        }
    }
}
