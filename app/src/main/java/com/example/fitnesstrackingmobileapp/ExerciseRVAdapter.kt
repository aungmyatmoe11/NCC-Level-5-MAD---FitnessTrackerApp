package com.example.fitnesstrackingmobileapp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fitnesstrackingmobileapp.data.Exercise

class ExerciseRVAdapter(
        val context: Context,
        val noteClickDeleteInterface: NoteClickDeleteInterface,
        val noteClickInterface: NoteClickInterface
) : RecyclerView.Adapter<ExerciseRVAdapter.ViewHolder>() {

    // on below line we are creating a
    // variable for our all notes list.
    private val allNotes = ArrayList<Exercise>()

    // on below line we are creating a view holder class.
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // on below line we are creating an initializing all our
        // variables which we have added in layout file.
        val noteTV = itemView.findViewById<TextView>(R.id.idTVNote)
        val dateTV = itemView.findViewById<TextView>(R.id.idTVDate)
        val metric1 = itemView.findViewById<TextView>(R.id.idmetric1)
        val metric2 = itemView.findViewById<TextView>(R.id.idmetric2)
        val deleteIV = itemView.findViewById<ImageView>(R.id.idIVDelete)
        val idCalburned = itemView.findViewById<TextView>(R.id.idCalburned)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // inflating our layout file for each item of recycler view.
        val itemView =
                LayoutInflater.from(parent.context).inflate(R.layout.note_rv_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // on below line we are setting data to item of recycler view.

        holder.noteTV.setText(allNotes.get(position).activity_name)
        //  if(allNotes.get(position).activity_name.equals("Running")){
        holder.metric1.setText("Metric 1 :" + allNotes.get(position).metric_one.toString())
        holder.metric2.setText("Metric 2 : " + allNotes.get(position).metric_two.toString())
        holder.idCalburned.setText(
                "Calories Burned: " + allNotes.get(position).cal_burned.toString()
        )
        holder.dateTV.setText("Last Updated : " + allNotes.get(position).timeStamp)

        // on below line we are adding click listener to our delete image view icon.
        holder.deleteIV.setOnClickListener {
            // on below line we are calling a note click
            // interface and we are passing a position to it.
            noteClickDeleteInterface.onDeleteIconClick(allNotes.get(position))
        }

        // on below line we are adding click listener
        // to our recycler view item.Dura
        holder.itemView.setOnClickListener {
            // on below line we are calling a note click interface
            // and we are passing a position to it.
            noteClickInterface.onNoteClick(allNotes.get(position))
        }
    }

    override fun getItemCount(): Int {
        // on below line we are
        // returning our list size.
        return allNotes.size
    }

    // below method is use to update our list of notes.
    fun updateList(newList: List<Exercise>) {
        // on below line we are clearing
        // our notes array list
        allNotes.clear()
        // on below line we are adding a
        // new list to our all notes list.
        allNotes.addAll(newList)
        // on below line we are calling notify data
        // change method to notify our adapter.
        notifyDataSetChanged()
    }
}

interface NoteClickDeleteInterface {
    // creating a method for click
    // action on delete image view.
    fun onDeleteIconClick(exec: Exercise)
}

interface NoteClickInterface {
    // creating a method for click action
    // on recycler view item for updating it.
    fun onNoteClick(exec: Exercise)
}
