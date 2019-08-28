package com.utility.finmartcontact.login

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.View
import android.widget.Toast
import com.utility.finmartcontact.APIResponse
import com.utility.finmartcontact.BaseActivity
import com.utility.finmartcontact.IResponseSubcriber
import com.utility.finmartcontact.R
import com.utility.finmartcontact.core.controller.home.HomeActivity
import com.utility.finmartcontact.core.controller.login.LoginController
import com.utility.finmartcontact.core.requestentity.LoginRequestEntity
import com.utility.finmartcontact.core.response.LoginResponse
import com.utility.finmartcontact.utility.RequestPermission
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.content_login.*




class LoginActivity : BaseActivity(), View.OnClickListener, IResponseSubcriber {

    private val TAG = "Permission"
    private val READ_CONTACTS_CODE = 101


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
      //  setSupportActionBar(toolbar)

        btnSignIn.setOnClickListener(this)
        setupPermissions()

    }

    private fun setupPermissions() {
        val permission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)

        if (permission != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission to read denied")
            makeRequest()
        }
    }


    private fun checkRationalePermission(): Boolean {
        val readContact = ActivityCompat.shouldShowRequestPermissionRationale(this@LoginActivity,Manifest.permission.READ_CONTACTS)

        return readContact
    }
    private fun makeRequest() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CONTACTS), READ_CONTACTS_CODE)
    }



    override fun onClick(v: View?) {

        when (v?.id) {
            R.id.btnSignIn -> {

                hideKeyBoard(btnSignIn)

                if (etEmail.text.toString().isEmpty()) {
                    showMessage(etEmail, "Invalid input", "", null)
                    return
                }
                if (etPassword.text.toString().isEmpty()) {
                    showMessage(etPassword, "Invalid password", "", null)
                    return
                }

                var loginRequestEntity = LoginRequestEntity(
                    UserName = etEmail.text.toString(),
                    Password = etPassword.text.toString()

                )

                showLoading("Authenticating user..")
                LoginController(this).login(loginRequestEntity, this)

            }
        }
    }

    override fun onSuccess(response: APIResponse, message: String) {

       dismissDialog()

        if (response is LoginResponse) {

            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onFailure(error: String) {
       dismissDialog()
        showMessage(etPassword, error, "", null)
    }

//    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
//        when (requestCode) {
//            READ_CONTACTS_CODE -> {
//
//                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
//
//                    Log.i(TAG, "Permission has been denied by user")
//                    makeRequest()
//
//                } else {
//                    Log.i(TAG, "Permission has been granted by user")
//
//                }
//            }
//        }
//    }


}
