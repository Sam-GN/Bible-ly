package edu.calbaptist.bible_ly.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import edu.calbaptist.bible_ly.BiblelyClass
import edu.calbaptist.bible_ly.R
import edu.calbaptist.bible_ly.setGlide
import edu.calbaptist.bible_ly.toLocalDateString
import kotlinx.android.synthetic.main.list_class_item_class.view.*
import kotlinx.android.synthetic.main.list_event_item_board.view.*
import java.io.IOException


open class ClassMutableListAdapter (private val listener: OnBiblelyClassItemSelectedListener)
    : ListAdapter<BiblelyClass, ClassMutableListAdapter.BiblelyClassViewHolder>(DiffCallback()) {
    interface OnBiblelyClassItemSelectedListener {

        fun onBiblelyClassItemSelected(classItem: BiblelyClass)
    }

    override fun onBindViewHolder(holder: BiblelyClassViewHolder, position: Int) {
        holder.bind(getItem(position),listener)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BiblelyClassViewHolder {

        val inflater = LayoutInflater.from(parent.context)
        // if(viewType==0)
        return BiblelyClassViewHolder(
            inflater.inflate(
                R.layout.list_class_item_class,
                parent,
                false
            )
        )


    }

    class BiblelyClassViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(
            biblelyClass: BiblelyClass,
            listener: OnBiblelyClassItemSelectedListener?
        ) {


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
                        .apply(RequestOptions().transforms(CenterCrop(), RoundedCorners(20)))
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
                listener?.onBiblelyClassItemSelected(biblelyClass)
            }
        }


    }
    class DiffCallback : DiffUtil.ItemCallback<BiblelyClass>() {
        override fun areItemsTheSame(oldItem: BiblelyClass, newItem: BiblelyClass): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: BiblelyClass, newItem: BiblelyClass): Boolean {
            return oldItem == newItem
        }
    }
}