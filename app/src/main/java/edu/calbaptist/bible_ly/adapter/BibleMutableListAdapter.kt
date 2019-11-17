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
import edu.calbaptist.bible_ly.Class
import edu.calbaptist.bible_ly.R
import edu.calbaptist.bible_ly.Verse
import kotlinx.android.synthetic.main.list_bible_item.view.*
import java.io.IOException





open class BibleMutableListAdapter (private val listener: OnBibleItemSelectedListener,private val listenerLong: OnBibleItemLongSelectedListener)
    : ListAdapter<Verse, BibleMutableListAdapter.BibleViewHolder>(DiffCallback()) {
    interface OnBibleItemSelectedListener {

        fun onBibleItemSelected(item: Verse)
    }
    interface OnBibleItemLongSelectedListener {

        fun onBibleItemLongSelected(v:View,book:String, verseNum: String,
                                    verseChapter: String,
                                     verseText: String)
    }

    override fun onBindViewHolder(holder: BibleViewHolder, position: Int) {
        holder.bind(getItem(position),listener,listenerLong)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BibleViewHolder {

        val inflater = LayoutInflater.from(parent.context)
        // if(viewType==0)
        return BibleViewHolder(
            inflater.inflate(
                R.layout.list_bible_item,
                parent,
                false
            )
        )


    }

    class BibleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(
            verse: Verse,
            listener: OnBibleItemSelectedListener?,
            listenerLong: OnBibleItemLongSelectedListener?
        ) {

            itemView.tv_bible_verse.text = verse.text
            itemView.tv_bible_verse_num.text = verse.verse.toString()
            // Click listener
            itemView.setOnClickListener {
                listener?.onBibleItemSelected(verse)
            }
            itemView.setOnLongClickListener {
                listenerLong?.onBibleItemLongSelected(it,verse.book,verse.verse, verse.chapter,verse.text)
                return@setOnLongClickListener true
            }
        }
    }
        class DiffCallback : DiffUtil.ItemCallback<Verse>() {
            override fun areItemsTheSame(oldItem: Verse, newItem: Verse): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: Verse, newItem: Verse): Boolean {
                return oldItem == newItem
            }
        }
}