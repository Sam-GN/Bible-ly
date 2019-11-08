package edu.calbaptist.bible_ly.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import edu.calbaptist.bible_ly.Class
import edu.calbaptist.bible_ly.Event
import edu.calbaptist.bible_ly.MainActivity
import edu.calbaptist.bible_ly.R
import kotlinx.android.synthetic.main.list_class_item_class.view.*
import kotlinx.android.synthetic.main.list_event_item_board.view.*
import java.io.IOException





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
       /* getSnapshot(position).reference.collection("students").whereEqualTo("email",MainActivity.user.email).get().addOnSuccessListener {
            if(!it.isEmpty)

        }
*/      holder.bind(getSnapshot(position), listener)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(
            snapshot: DocumentSnapshot,
            listener: OnClassItemSelectedListener?
        ) {
          /*  if(!snapshot.reference.collection("students").whereEqualTo("email",MainActivity.user.email).get().isSuccessful) {
                return
            }*/
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
            var ivLogo = itemView.findViewById<ImageView>(R.id.iv_class_item_logo)
            if(biblelyClass.classLogo != "") {
                try {
                    Glide.with(ivLogo.context)

                        .applyDefaultRequestOptions( RequestOptions()
                            .placeholder(R.drawable.ic_class_logo_default2)
                            .error(R.drawable.ic_class_logo_default2)
                            )
                        //.applyDefaultRequestOptions( RequestOptions())
                        //.setDefaultRequestOptions(RequestOptions())
                        .load(biblelyClass.classLogo)
                        .apply(RequestOptions().transforms(CenterCrop(), RoundedCorners(30)))
                        //.apply(RequestOptions.centerCropTransform())
//                         .apply(RequestOptions.circleCropTransform())

                        .into(ivLogo)

                }catch (e: IOException){
                    Log.e("BoardAdapter",e.message)
                    ivLogo.setImageResource(R.drawable.ic_class_logo_default2)
                }

            } else {
                ivLogo.setImageResource(R.drawable.ic_class_logo_default2)
            }
            itemView.tv_class_item_title.text = biblelyClass.name
            itemView.tv_class_item_teacher.text = biblelyClass.teacher?.userName

            // Click listener
            itemView.setOnClickListener {
                listener?.onClassItemSelected(snapshot)
            }
        }
    }
}
