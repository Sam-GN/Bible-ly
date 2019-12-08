package edu.calbaptist.Comment_ly.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import edu.calbaptist.bible_ly.*
import edu.calbaptist.bible_ly.activity.MainActivity
import kotlinx.android.synthetic.main.list_comment_item.view.*



open class CommentMutableListAdapter (private val listener: OnCommentItemMoreSelectedListener)
    : ListAdapter<CommentCardViewItem, CommentMutableListAdapter.CommentViewHolder>(DiffCallback()) {
    interface OnCommentItemMoreSelectedListener {

        fun OnCommentItemMoreSelectedListener(v:View,item: CommentCardViewItem,position: Int)
    }
    

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bind(getItem(position),listener)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {

        val inflater = LayoutInflater.from(parent.context)
        // if(viewType==0)
        return CommentViewHolder(
            inflater.inflate(
                R.layout.list_comment_item,
                parent,
                false
            )
        )


    }

    class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(
            comment: CommentCardViewItem,
            listener: OnCommentItemMoreSelectedListener?
            
        ) {

            itemView.tv_note_comment_text.text = comment.text
            FirestoreRepository().getNote("Note/"+comment.path.split("/")[1]){
                if(comment.user!!.email== MainActivity.user.email || it.user!!.email == MainActivity.user.email){
                    itemView.iv_note_comment_more.visibility = View.VISIBLE
                }
                else{
                    itemView.iv_note_comment_more.visibility = View.INVISIBLE
                }
            }
            if(comment.user!!.email== MainActivity.user.email)
                itemView.tv_note_comment_user.text = "Me <${comment.userText}>:"
            else
                itemView.tv_note_comment_user.text = comment.userText+":"

            itemView.tv_note_comment_date.text = comment.date!!.toLocalDateString(true)




            itemView.iv_note_comment_more.setOnClickListener {

                listener?.OnCommentItemMoreSelectedListener(itemView.iv_note_comment_more,comment,position)
            }

           
        }
    }
        class DiffCallback : DiffUtil.ItemCallback<CommentCardViewItem>() {
            override fun areItemsTheSame(oldItem: CommentCardViewItem, newItem: CommentCardViewItem): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: CommentCardViewItem, newItem: CommentCardViewItem): Boolean {
                return oldItem == newItem
            }
        }
}