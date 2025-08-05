package com.example.fitnesstrackingmobileapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.style.UnderlineSpan
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.fitnesstrackingmobileapp.data.Exercise
import com.example.fitnesstrackingmobileapp.compose.WorkoutActivity

class HomeFragment : Fragment() {
    lateinit var walkButton: ImageButton
    lateinit var runButton: ImageButton
    lateinit var otherActButton: ImageButton
    lateinit var idgoal: EditText
    lateinit var calBurned: TextView
    lateinit var calBtn: Button
    lateinit var resetBtn: Button
    lateinit var viewModal: ExerciseViewModal
    lateinit var chooseActiBtn: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Assuming you have a button in your fragment layout
        walkButton = view.findViewById(R.id.walk)
        runButton = view.findViewById(R.id.running)
        otherActButton = view.findViewById(R.id.customActivities)
        idgoal = view.findViewById(R.id.idgoal)
        calBurned = view.findViewById(R.id.idCalorieInfo)
        calBtn = view.findViewById(R.id.calBtn)
        resetBtn=view.findViewById(R.id.resetBtn)
        chooseActiBtn=view.findViewById(R.id.chooseActiBtn)

        val mSpannableString = SpannableString("Reset Goal")
        mSpannableString.setSpan(UnderlineSpan(), 0, mSpannableString.length, 0)

        viewModal = ViewModelProvider(requireActivity()).get(ExerciseViewModal::class.java)
        //     var calGol=idgoal.text.toString()


        calBtn.setOnClickListener {
            viewModal.allNotes.observe(viewLifecycleOwner, Observer { list ->
                // This block will be executed whenever the data changes
                list?.let {
                    showCaloriesBurned(it)
                }
            })
        }
        resetBtn.setOnClickListener{
            resetGoal()
        }
        chooseActiBtn.setOnClickListener{
            val intent = Intent(activity, WorkoutActivity::class.java)
            //Start the new activity
            startActivity(intent)
        }

        walkButton.setOnClickListener {
            // Create an Intent to start the new Activity
            val intent = Intent(activity, RunningActivity::class.java)
            // Start the new Activity
            startActivity(intent)
        }
        runButton.setOnClickListener {
            val intent = Intent(activity, WalkingTrackerActivity::class.java)
            // Start the new Activity
            startActivity(intent)
        }
        otherActButton.setOnClickListener {
            val intent = Intent(activity, ExerciseActivity::class.java)
            // Start the new Activity
            startActivity(intent)
        }
    }

    private fun resetGoal() {
        val sharedPreferences = getActivity()?.getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        val resetEditor = sharedPreferences?.edit()
        resetEditor?.clear()
        resetEditor?.apply()
        idgoal.setText("")
        calBurned.text =""
        idgoal.setVisibility(View.VISIBLE)
        val goalVal=idgoal.text.toString()
        if (goalVal.isEmpty())
            Toast.makeText(activity, "Goal cannot be empty", Toast.LENGTH_LONG).show()
        else saveGoal(goalVal)
    }



    private fun saveGoal(goal:String) {
       if(goal.equals("")) Toast.makeText( getActivity(), "Enter the number for desired goals", Toast.LENGTH_SHORT).show()
       else{
           val sharedPreferences = getActivity()?.getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
           val editor = sharedPreferences?.edit()
            var goalCal=goal.toFloat()
           if (editor != null) {
               editor.putFloat("key1", goalCal)
           }
           if (editor != null) {
               editor.apply()
           }
           Toast.makeText(activity, "Goal Saved", Toast.LENGTH_LONG).show()
       }
    }
    private fun showCaloriesBurned(list: List<Exercise>) {
      //  loadGoal()
        val sharedPreferences = getActivity()?.getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        var calGo = sharedPreferences?.getFloat("key1", 0f)
        var totalCalBurned=0.0
        var goal = 0.0
        for (item in list) {
            // Process each item
            totalCalBurned += item.cal_burned
        }
        if (calGo==0.0f || calGo==null) {
            idgoal.setVisibility(View.VISIBLE)
            Toast.makeText(activity, "Set The Goal", Toast.LENGTH_SHORT).show()
            saveGoal(idgoal.text.toString())

        }
        else {  //Toast.makeText(activity, "Calgo = " +calGo, Toast.LENGTH_SHORT).show()
                if (calGo != null) {
                    Toast.makeText(activity, "Total Cal Burned   " +totalCalBurned, Toast.LENGTH_SHORT).show()
                    goal = (calGo.toDouble() - totalCalBurned)
                } else {
                    Toast.makeText(activity, "User Not Set The Goal", Toast.LENGTH_SHORT).show()
                }
               if (goal > 0.0 ) {
                   idgoal.setVisibility(View.INVISIBLE)
                calBurned.text = "You need to burn :" + goal + "Calories"

               } else {
                   idgoal.setVisibility(View.INVISIBLE)

                calBurned.text = "Congradulations!!! Reach your goal and You Already burned :" + totalCalBurned +"Calories"
                 }
            }

    }
}