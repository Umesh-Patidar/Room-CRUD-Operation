package com.room

import android.os.Bundle
import android.text.TextUtils
import android.view.MenuItem
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.room.constant.Constants
import com.room.database.AppDatabase
import com.room.database.AppExecutors
import com.room.model.Person
import kotlinx.android.synthetic.main.activity_edit.*


class EditActivity : AppCompatActivity() {
    var mPersonId = 0
    private lateinit var mDb: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)

        initViews()

        mDb = AppDatabase.getInstance(this)

        if (intent != null && intent!!.hasExtra(Constants.UPDATE_Person_Id)) {
            button!!.text = "Update"
            mPersonId = intent!!.getIntExtra(Constants.UPDATE_Person_Id, -1)
            AppExecutors.getInstance().diskIO().execute {
                val person: Person = mDb.personDao().loadPersonById(mPersonId)
                populateUI(person)
            }
        }
    }

    private fun populateUI(person: Person) {
        edit_name.setText(person.name)
        edit_email.setText(person.email)
        edit_number.setText(person.number)
        edit_pincode.setText(person.pincode)
        edit_city.setText(person.city)
    }

    private fun initViews() {
        button?.setOnClickListener {
            onSaveButtonClicked()
        }
    }

    private fun onSaveButtonClicked() {
       val name  = edit_name!!.text.toString().trim()
       val email  = edit_email!!.text.toString().trim()
       val number  = edit_number!!.text.toString().trim()
       val pin  = edit_pincode!!.text.toString().trim()
       val city  = edit_city!!.text.toString().trim()


        if (validate(name, email, number, pin, city)) {
            val person = Person(name, email, number, pin, city)

            AppExecutors.getInstance().diskIO().execute {
                if (!intent!!.hasExtra(Constants.UPDATE_Person_Id)) {
                    mDb.personDao().insertPerson(person)
                } else {
                    person.id = mPersonId
                    mDb.personDao().updatePerson(person)
                }

                finish()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun validate(name: String, email: String, number: String, pinCode: String, city: String)
            : Boolean {
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Please enter the name.", Toast.LENGTH_SHORT).show()
            return false
        }
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please enter email.", Toast.LENGTH_SHORT).show()
            return false
        }

        if (TextUtils.isEmpty(number)) {
            Toast.makeText(this, "Please enter mobile number.", Toast.LENGTH_SHORT).show()
            return false
        }

        if (TextUtils.isEmpty(pinCode)) {
            Toast.makeText(this, "Please enter pin code.", Toast.LENGTH_SHORT).show()
            return false
        }

        if (TextUtils.isEmpty(city)) {
            Toast.makeText(this, "Please enter city", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }
}
