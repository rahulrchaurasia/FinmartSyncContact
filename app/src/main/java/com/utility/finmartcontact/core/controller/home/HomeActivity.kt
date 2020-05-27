package com.utility.finmartcontact.core.controller.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import com.github.tamir7.contacts.Contacts
import com.google.gson.Gson
import com.utility.finmartcontact.APIResponse
import com.utility.finmartcontact.BaseActivity
import com.utility.finmartcontact.IResponseSubcriber
import com.utility.finmartcontact.R
import com.utility.finmartcontact.core.controller.contactfetch.ContactFetcher
import com.utility.finmartcontact.core.controller.login.LoginController
import com.utility.finmartcontact.core.model.ContactlistEntity
import com.utility.finmartcontact.core.requestentity.ContactLeadRequestEntity
import com.utility.finmartcontact.utility.Utility
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.content_home.*
import java.util.*


class HomeActivity : BaseActivity(), View.OnClickListener, IResponseSubcriber {

    private val TAG = "CONTACT"
    var contactlist: MutableList<ContactlistEntity>? = null
    var templist: MutableList<String>? = null
    var phones: Cursor? = null
    var progressBar: ProgressBar? = null


    var perms = arrayOf(
        "android.permission.READ_CONTACTS"
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        setSupportActionBar(toolbar)
//        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        contactlist = ArrayList<ContactlistEntity>()
        templist = ArrayList<String>()
        btnSync.setOnClickListener(this)


    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnSync -> {

                if (!checkPermission()) {
                    if (checkRationalePermission()) {
                        requestPermission()
                    } else {
                        permissionAlert()
                    }
                } else {

                    //syncContactNumber()

                    var getAllContactDetails = Contacts.getQuery().find()
                    Log.d("raw_contact", Gson().toJson(getAllContactDetails))

//                    var loadC = ContactFetcher(this@HomeActivity).fetchAll()
//                    Log.d("---TAG", loadC.toString())
//
//                    for (i in loadC) {
//
//                        i.numbers.forEachIndexed { index, contactPhone ->
//                            contactlist?.add(
//                                ContactlistEntity(
//                                    contactPhone.number,
//                                    i.name,
//                                    i.hashCode()
//                                )
//                            )
//                        }
//                    }
//
//                    Log.d("---TA", "" + contactlist?.size)

                }
            }
        }
    }


    private fun syncContactNumber() {

        txtMessage.text = ""

        val PROJECTION = arrayOf(
            ContactsContract.RawContacts._ID,
            ContactsContract.Contacts.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.PHOTO_URI,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Photo.CONTACT_ID
        )

        val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val filter =
            "" + ContactsContract.Contacts.HAS_PHONE_NUMBER + " > 0 and " + ContactsContract.CommonDataKinds.Phone.TYPE + "=" + ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE
        val order =
            ContactsContract.Contacts.DISPLAY_NAME + " ASC"// LIMIT " + limit + " offset " + lastId + "";

        phones = this.contentResolver.query(uri, PROJECTION, filter, null, order)


        if (phones == null || phones!!.getCount() == 0) {
            Log.e("count", "" + phones!!.getCount())

        } else {

            showLoading("Uploading Data..")
            LoadContactTask().execute()

        }

    }

    inner class loadContacts : AsyncTask<Void, Void, Void>() {

        override fun doInBackground(vararg params: Void?): Void? {
            var loadC = ContactFetcher(this@HomeActivity).fetchAll()
            Log.d("---TAG", loadC.size.toString())
            return null
        }
    }


    inner class LoadContactTask : AsyncTask<Void, Int, Void>() {
        override fun onPreExecute() {
            super.onPreExecute()
            //   txtCount.setText("" + phones!!.getCount())

        }

        override fun doInBackground(vararg voids: Void): Void? {
            // Get Contact list from Phone

            Log.d("---Phone", phones.toString())

            // val regex = Regex("[^0-9\\s]")
            val regex = Regex("[^.0-9]")
            if (phones != null && phones!!.getCount() > 0) {
                try {
                    var i = 1
                    while (phones!!.moveToNext()) {


                        var name =
                            "" + phones!!.getString(phones!!.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                        var phoneNumber =
                            "" + phones!!.getString(phones!!.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))

                        //  phoneNumber = phoneNumber.trim().replace("[^0-9\\s+]", "");   // remove Special character and Space

                        phoneNumber = regex.replace(phoneNumber, "") // works
                        //.replace("\\s".toRegex(), "")

                        if (phoneNumber.length >= 10) {

                            phoneNumber = phoneNumber.takeLast(10)
                            // check whether the number alreday added to list or not

                            if (!templist!!.contains(phoneNumber)) {
                                templist?.add(phoneNumber)

                                val selectUser = ContactlistEntity(
                                    name = name,
                                    mobileno = phoneNumber,
                                    id = i
                                )
                                Log.i(
                                    TAG,
                                    "Key ID: " + i + " Name: " + name + " Mobile: " + phoneNumber + "\n"
                                );
                                contactlist?.add(selectUser)
                                publishProgress(i++)
                            }


                        }


                    }
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }


            return null
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
            lyTotal.visibility = View.VISIBLE
            // Toast.makeText(this@HomeActivity,"Contact Added Successfully..",Toast.LENGTH_SHORT).show()
            txtCount.setText("" + contactlist!!.size)


            // sendContactToServer()

            //region commented Send To server

//            // Adding in Request Entity
//            val contactRequestEntity = ContactLeadRequestEntity(
//                fbaid = applicationPersistance!!.getFBAID().toString(),
//                contactlist = contactlist
//            )
//
//            LoginController(this@HomeActivity).uploadContact(contactRequestEntity, this@HomeActivity)

            //endregion
        }


    }

    fun sendContactToServer() {


        showLoading("Sending to Server...")
        var subcontactlist: List<ContactlistEntity>

        // id is from 1 to lisSize-1

        if (contactlist != null && contactlist!!.size > 0) {

            for (i in 0..contactlist!!.size - 1 step 100) {


                subcontactlist = contactlist!!.filter { it.id > i && it.id <= (100 + i) }


                //region  Adding in Request Entity and Send to server
                val contactRequestEntity = ContactLeadRequestEntity(
                    fbaid = applicationPersistance!!.getFBAID().toString(),
                    contactlist = subcontactlist
                )

                LoginController(this@HomeActivity).uploadContact(
                    contactRequestEntity,
                    this@HomeActivity
                )

                //endregion


            }


        }


    }


    override fun onSuccess(response: APIResponse, message: String) {
        dismissDialog()

        txtMessage.text = "" + response.Message
    }

    override fun onFailure(error: String) {
        dismissDialog()
        showMessage(btnSync, error, "", null)
    }


    private fun checkPermission(): Boolean {
        val camera = ActivityCompat.checkSelfPermission(this@HomeActivity, perms[0])

        return camera == PackageManager.PERMISSION_GRANTED
    }


    private fun requestPermission() {
        ActivityCompat.requestPermissions(this@HomeActivity, perms, Utility.READ_CONTACTS_CODE)

    }

    private fun checkRationalePermission(): Boolean {
        val readContact =
            ActivityCompat.shouldShowRequestPermissionRationale(
                this@HomeActivity,
                Manifest.permission.READ_CONTACTS
            )

        return readContact
    }

    fun permissionAlert() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Need  Permission")
        builder.setMessage("This App Required Contact Permissions.")
        //builder.setPositiveButton("OK", DialogInterface.OnClickListener(function = x))

        builder.setPositiveButton(android.R.string.yes) { dialog, which ->


            dialog.cancel()
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", packageName, null)
            intent.data = uri
            startActivityForResult(intent, Utility.REQUEST_PERMISSION_SETTING)

        }



        builder.show()
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            Utility.READ_CONTACTS_CODE -> {

                if (grantResults.isEmpty() || grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    syncContactNumber()

                }
            }
        }
    }

}
