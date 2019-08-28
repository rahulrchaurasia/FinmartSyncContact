package com.utility.finmartcontact

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import com.utility.finmartcontact.core.controller.facade.ApplicationPersistance
import java.util.*

open class BaseActivity : AppCompatActivity() {

    lateinit var dialog: AlertDialog
    lateinit var dialogView: View
    lateinit var applicationPersistance: ApplicationPersistance

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initDialog()
        applicationPersistance = ApplicationPersistance(this)
    }

    protected fun showMessage(view: View, message: String, action: String, onClickListener: View.OnClickListener?) {
        Snackbar.make(view, message, Snackbar.LENGTH_LONG)
            .setAction(action, onClickListener).show()
    }

    //region Progress Dialog

    private fun initDialog() {
        val builder = AlertDialog.Builder(this)
        dialogView = layoutInflater.inflate(R.layout.layout_progress_dialog, null)
        builder.setView(dialogView)
        builder.setCancelable(false)
        dialog = builder.create()
    }

    fun showLoading(message: CharSequence) {
        val msg = dialogView.findViewById<TextView>(R.id.txtProgressTitle)
        msg.text = message
        dialog.show()
    }

    fun dismissDialog() {
        if (dialog != null && dialog.isShowing) {
            dialog.dismiss()
        }
    }

    //endregion


    protected fun hideKeyBoard(view: View) {
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }






}