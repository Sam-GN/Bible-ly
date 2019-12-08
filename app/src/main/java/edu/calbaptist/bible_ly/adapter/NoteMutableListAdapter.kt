package edu.calbaptist.Note_ly.adapter

import android.content.res.Resources
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import edu.calbaptist.bible_ly.MainActivity
import edu.calbaptist.bible_ly.NoteCardViewItem
import edu.calbaptist.bible_ly.R
import edu.calbaptist.bible_ly.adapter.BibleMutableListAdapter
import edu.calbaptist.bible_ly.toLocalDateString
import io.opencensus.resource.Resource
import kotlinx.android.synthetic.main.list_bible_note_item.view.*
import kotlinx.android.synthetic.main.list_bible_note_item_header.view.*


open class NoteMutableListAdapter (private val listener: OnNoteItemSelectedListener, private val listenerLong: OnNoteItemLongSelectedListener)
    : ListAdapter<NoteCardViewItem, NoteMutableListAdapter.NoteViewHolder>(DiffCallback()) {
    interface OnNoteItemSelectedListener {

        fun onNoteItemSelected(item: NoteCardViewItem)
    }

    interface OnNoteItemLongSelectedListener {

        fun onNoteItemLongSelected(v:View,item: NoteCardViewItem)
    }


    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(getItem(position),listener,listenerLong)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {

        val inflater = LayoutInflater.from(parent.context)
        if(viewType==0)
            return NoteViewHolder(
                inflater.inflate(
                   R.layout.list_bible_note_item,
                    parent,
                    false
                )
            ) else {

            return NoteViewHolder(
                inflater.inflate(
                    R.layout.list_bible_note_item_header,
                    parent,
                    false
                )
            )
        }


    }

    class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(
            note: NoteCardViewItem,
            listener: OnNoteItemSelectedListener,
            listenerLong: OnNoteItemLongSelectedListener
        ) {

            if(itemViewType==0){
                itemView.tv_bible_note_verse_num.text =itemView.context.getString(R.string.note_verse_num, note.verseNum)
                itemView.tv_bible_note_text.text = note.noteText
                itemView.tv_bible_note_title.text = note.noteTitle
                itemView.tv_bible_note_date.text = note.date!!.toLocalDateString(false)
                if(note.hasComment)
                    itemView.iv_bible_note_hasComment.visibility = View.VISIBLE
                else
                    itemView.iv_bible_note_hasComment.visibility = View.INVISIBLE

                if(note.shared) {
                    if(note.user!!.email==MainActivity.user.email){
                        itemView.iv_bible_note_type.setImageResource(R.drawable.ic_question_answer_black_24dp)
                    } else {
                        itemView.iv_bible_note_type.setImageResource(R.drawable.ic_question_answer_accent_24dp)
                    }
                    itemView.iv_bible_note_type.visibility=View.VISIBLE
                 } else
                    {
                        itemView.iv_bible_note_type.visibility=View.INVISIBLE
                    }
                // Click listener
                itemView.setOnClickListener {
                    listener?.onNoteItemSelected(note)
                }
                itemView.setOnLongClickListener {
                    listenerLong?.onNoteItemLongSelected(it,note)
                    return@setOnLongClickListener true
                }
                itemView.iv_bible_note_type.setOnClickListener {
                    if(note.shared) {
                        if(note.user!!.email==MainActivity.user.email){
                            Toast.makeText(itemView.context,R.string.you_shared_note,Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(itemView.context,R.string.others_shared_note,Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                itemView.iv_bible_note_hasComment.setOnClickListener {
                    Toast.makeText(itemView.context,R.string.note_has_comment,Toast.LENGTH_SHORT).show()
                }

            }

            if(itemViewType==1) {
                itemView.tv_bible_note_chapter_num.text =itemView.context.getString(R.string.note_chapter_num,note.verseChapter)

            }



        }
    }

    override fun getItemViewType(position: Int): Int {
        return if(getItem(position).isHeader)
            1
        else
            0
    }
        class DiffCallback : DiffUtil.ItemCallback<NoteCardViewItem>() {
            override fun areItemsTheSame(oldItem: NoteCardViewItem, newItem: NoteCardViewItem): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: NoteCardViewItem, newItem: NoteCardViewItem): Boolean {
                return oldItem == newItem
            }
        }
}