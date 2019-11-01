package edu.calbaptist.bible_ly

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment

class CreateClassDialogFragment: AppCompatDialogFragment()
{
    // Save your custom view at the class level
    lateinit var customView: View
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View?
    {
        // Simply return the already inflated custom view
        return customView
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Inflate your view here
        customView = layoutInflater.inflate(R.layout.dialogue_new_class, null)
        // Create Alert Dialog with your custom view
        return AlertDialog.Builder(context!!)
            .setTitle("Create New Class")
          //  .setView(customView)
            .setNegativeButton(android.R.string.cancel, null)
            .create()

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        // Perform remaining operations here. No null issues.
        /*rbgSelectType.setOnCheckedChangeListener({ _, checkedId ->
            if(checkedId == R.id.rbSelectFromList) {
                // XYZ
            } else {
                // ABC
            }
        })*/
    }
}