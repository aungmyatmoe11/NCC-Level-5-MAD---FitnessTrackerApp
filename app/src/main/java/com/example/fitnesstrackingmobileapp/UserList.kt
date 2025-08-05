package com.example.fitnesstrackingmobileapp
import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView


class UsertList(private val context: Activity, internal var userlists: List<User>) : ArrayAdapter<User>(context, R.layout.layout_list_user, userlists) {


    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = context.layoutInflater
        val listViewItem = inflater.inflate(R.layout.layout_list_user, null, true)

        val textViewName = listViewItem.findViewById(R.id.textViewName) as TextView
        val textViewGenre = listViewItem.findViewById(R.id.textViewPass) as TextView

        val user = userlists[position]
        textViewName.text = user.UserName
        textViewGenre.text = user.Password

        return listViewItem
    }
}
