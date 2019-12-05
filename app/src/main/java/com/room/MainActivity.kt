package com.room

import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color.parseColor
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.room.adaptors.PersonAdaptor
import com.room.database.AppDatabase
import com.room.database.AppExecutors
import com.room.model.Person
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private var mAdapter: PersonAdaptor? = null
    private lateinit var mDb: AppDatabase
    private var colorDrawableBackground = ColorDrawable(parseColor("#ff0000"))
    private lateinit var deleteIcon: Drawable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        deleteIcon = ContextCompat.getDrawable(this, R.drawable.ic_delete)!!

        addFAB.setOnClickListener {
            startActivity(Intent(this@MainActivity, EditActivity::class.java))
        }


        recyclerView.layoutManager = LinearLayoutManager(this)
        mAdapter = PersonAdaptor(this)
        recyclerView.adapter = mAdapter

        mDb = AppDatabase.getInstance(this)

        val itemTouchHelperCallback =
            object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: ViewHolder,
                    viewHolder2: ViewHolder
                ): Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: ViewHolder, swipeDirection: Int) {
                    AppExecutors.getInstance().diskIO().execute {
                        val position = viewHolder.adapterPosition
                        val tasks: List<Person> = mAdapter!!.tasks
                        mDb.personDao().delete(tasks[position])
                        retrieveTasks()
                    }
                }

                override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: ViewHolder,
                    dX: Float,
                    dY: Float,
                    actionState: Int,
                    isCurrentlyActive: Boolean
                ) {
                    val itemView = viewHolder.itemView
                    val iconMarginVertical = (viewHolder.itemView.height - deleteIcon.intrinsicHeight) / 2

                    if (dX > 0) {
                        colorDrawableBackground.setBounds(itemView.left, itemView.top, dX.toInt(), itemView.bottom)
                        deleteIcon.setBounds(
                            itemView.left + iconMarginVertical,
                            itemView.top + iconMarginVertical,
                            itemView.left + iconMarginVertical + deleteIcon.intrinsicWidth,
                            itemView.bottom - iconMarginVertical
                        )
                    } else {
                        colorDrawableBackground.setBounds(
                            itemView.right + dX.toInt(),
                            itemView.top,
                            itemView.right,
                            itemView.bottom
                        )
                        deleteIcon.setBounds(
                            itemView.right - iconMarginVertical - deleteIcon.intrinsicWidth,
                            itemView.top + iconMarginVertical,
                            itemView.right - iconMarginVertical,
                            itemView.bottom - iconMarginVertical
                        )
                        deleteIcon.level = 0
                    }

                    colorDrawableBackground.draw(c)

                    c.save()

                    if (dX > 0)
                        c.clipRect(itemView.left, itemView.top, dX.toInt(), itemView.bottom)
                    else
                        c.clipRect(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom)

                    deleteIcon.draw(c)

                    c.restore()

                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                }
            }

        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)

    }

    override fun onResume() {
        super.onResume()
        retrieveTasks()
    }

    fun retrieveTasks() {
        AppExecutors.getInstance().diskIO().execute {
            val persons: List<Person> =
                mDb.personDao().loadAllPersons()
            runOnUiThread { mAdapter!!.tasks = persons }
        }
    }
}
