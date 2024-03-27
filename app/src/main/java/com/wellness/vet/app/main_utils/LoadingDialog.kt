package com.wellness.vet.app.main_utils

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import com.wellness.vet.app.R

class LoadingDialog {

    companion object {
        // Declare a private lateinit variable to hold the dialog instance
        private lateinit var dialog: Dialog

        // Function to show the loading dialog
        fun showLoadingDialog(context: Context?): Dialog {
            // Create a new dialog instance with the given context
            dialog = Dialog(context!!)
            dialog.setContentView(R.layout.loading_dialog)
            dialog.setCancelable(false) // To prevent the dialog from being dismissed by touching outside

            // Make the dialog background transparent
            if (dialog.window != null) {
                dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            }

            // Show the dialog
            dialog.show()
            return dialog
        }

        // Function to hide the loading dialog
        fun hideLoadingDialog(dialog: Dialog?) {
            // Check if the dialog is not null and is showing
            if (dialog != null && dialog.isShowing) {
                // Dismiss the dialog
                dialog.dismiss()
            }
        }
    }
}