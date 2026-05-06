package com.example.pro_club

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.pro_club.databinding.ItemBlockBinding
import com.example.pro_club.databinding.ItemSectionBinding
import java.util.Calendar

class BlockAdapter( //this class takes 3 params like props in React
    private val context: Context, //key to resources from main activity
    //we need context to access shared preferences and inflate layouts
    private var sections: List<Section>,
    //sections is our full schedule from schedule data.sections
    //this is the data that adapter will loop through
    private val onProgressUpdate: () -> Unit,
    private val onDataChanged: () -> Unit
    //onProgressUpdate is a callback function from main activity
    //when a block is marked done we call this to update the progress bar
    //() -> Unit means takes no inputs, returns nothing
    //Same as passing an onClick={() => updateProgress()} on react

) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // this recyclerview Adapter "means BlockAdapter EXTENDS  recyclerview.adapter
    //this tells it we will manage our own view holder
    private val prefs: SharedPreferences =
        context.getSharedPreferences("schedule_prefs", Context.MODE_PRIVATE)
    // get shared preferences opens a storage file on the phone
    //mode private means the space is for this app only
    //when done is clicked it is saved here
    //when app is restarted it check the shared preference and read from there

    companion object {
        //these are like static in java where they are shared across all instances of block
        const val TYPE_SECTION = 0
        //when recyclerview sees type 0 it uses item section.xml
        //and draws section headers (morning classes afternoon)
        const val TYPE_BLOCK = 1
        //when recycler view see type 1 it uses item_block.xml
        //draws a block card(wake up & pray Deep coding)
    }
    private var flatList: List<Any> = buildFlatList()
    //recyclerview only understands flatlist one item per row
    //so we flatten the data into one list b4 giving it ro recyclerview
    //list any means the list can any types - section or blocks
    private fun buildFlatList(): List<Any> {
        //this is the function that converts section and block into flat list
        val list = mutableListOf<Any>()
        //this creates a list that is empty we can items to
        for (section in sections) {
            //this means to loop through every block inside this section
            list.add(section)
            //add a section header
            for (block in section.blocks) {
                //then loop though every block
                list.add(block)
                //Add each block after its section header
            }
        }
        // Final result looks like this:
        // [Section(Morning), Block(Wake up), Block(Stretch),
        //  Block(Breakfast), Block(Deep coding 1),
        //  Section(Classes), Block(Class time),
        //  Section(Afternoon), Block(Rest), Block(Deep coding 2),
        //  Section(Evening), Block(Football)...]
        return list
    }

    inner class SectionViewHolder(
        private val binding: ItemSectionBinding
        //binding acts like findviewbyid  in item section.xml
    ) : RecyclerView.ViewHolder(binding.root) {
        //recycler view . view holder caches view references
        //so android doesn't have to search for them every scroll
        fun bind(section: Section) {
            // bind() fills this section header with real data
            // called by on bind view holder () for each section row
            binding.tvSectionName.text = section.section
            //sets "morning","classes" e.t.c
            binding.tvSectionTime.text = section.timeRange
            //sets "6;00 -11:00"
        }
    }

    inner class BlockViewHolder(

        private val binding: ItemBlockBinding
        //binding gives us access to all views in the item_block
    ) : RecyclerView.ViewHolder(binding.root) {

        private fun deleteBlock(blockId: String) {
            val retrofit = retrofit2.Retrofit.Builder()
                .baseUrl("https://godchild.alwaysdata.net/")
                .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
                .build()

            val service = retrofit.create(ApiService::class.java)

            service.deleteBlock(blockId).enqueue(object : retrofit2.Callback<okhttp3.ResponseBody> {
                override fun onResponse(
                    call: retrofit2.Call<okhttp3.ResponseBody>,
                    response: retrofit2.Response<okhttp3.ResponseBody>
                ) {
                    if (response.isSuccessful) {
                        // Block deleted — tell ScheduleFragment to reload
                        // We use a broadcast to communicate between
                        // BlockAdapter and ScheduleFragment
                        // Same as dispatching a Redux action in React
                        onDataChanged()
                        // Call the callback directly instead of broadcast
                        // More reliable than BroadcastReceiver for same-app communication
                        android.widget.Toast.makeText(
                            context,
                            "Block deleted successfully",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(
                    call: retrofit2.Call<okhttp3.ResponseBody>,
                    t: Throwable
                ) {
                    android.widget.Toast.makeText(
                        context,
                        "Failed to delete: " + t.message,
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            })
        }
        fun bind(block: Block) {
            // Fill in text fields from block data
            binding.tvTime.text = block.time//7;30 - 11;00
            binding.tvTitle.text = block.title//"deep coding session 1"
            binding.tvDesc.text = block.desc// most important block
            binding.tvMotivation.text = block.motivation//"lock in and build"
            binding.tvTypeBadge.text = block.type.uppercase()//converts coding to CODING
            binding.btnEdit.setOnClickListener {
                // Pass all block data to EditBlockActivity via Intent extras
                // Same as navigate("/edit", { state: { block } }) in React Router
                val intent = Intent(context, EditBlockActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                intent.putExtra("block_id", block.id.toString())
                intent.putExtra("title", block.title)
                intent.putExtra("description", block.desc)
                intent.putExtra("task_type", block.type)
                intent.putExtra("start_hour", block.startHour.toString())
                intent.putExtra("start_minute", block.startMinute.toString())
                intent.putExtra("end_hour", block.endHour.toString())
                intent.putExtra("end_minute", block.endMinute.toString())
                intent.putExtra("motivation", block.motivation)
                intent.putExtra("section", block.section)
                context.startActivity(intent)
            }
            // Get the color for this block type
            // when() is Kotlin's switch/case
            // Same as your typeStyles object in React
            val color = when (block.type) {
                "routine"  -> "#9CA3AF"
                "workout"  -> "#16A34A"
                "coding"   -> "#2563EB"
                "class"    -> "#9333EA"
                "break"    -> "#F59E0B"
                "football" -> "#F97316"
                "sleep"    -> "#4F46E5"
                else       -> "#9CA3AF"
            }

            // Apply color to the left border and badge
            binding.viewColorBorder.setBackgroundColor(Color.parseColor(color))
            binding.tvTypeBadge.setTextColor(Color.parseColor(color))

            // Check if this block is happening right now
            // Same as your isCurrentBlock() in React
            val isNow = isCurrentBlock(block)

            // Show or hide the NOW badge
            // View.VISIBLE = show — like removing hidden class in React
            // View.GONE = hide completely — like display:none in CSS
            binding.tvNowBadge.visibility = if (isNow) View.VISIBLE else View.GONE

            // Highlight card if it is the current block
            binding.root.setCardBackgroundColor(
                Color.parseColor(if (isNow) "#1a2535" else "#111827")
            )
            // DELETE BUTTON CLICK
            binding.btnDelete.setOnClickListener {
                // Show confirmation dialog before deleting
                // Same as window.confirm() in JavaScript
                // We never delete without asking first!
                android.app.AlertDialog.Builder(context)
                    .setTitle("Delete Block")
                    .setMessage("Are you sure you want to delete '${block.title}'?")
                    .setPositiveButton("Delete") { _, _ ->
                        // User confirmed — delete from API
                        deleteBlock(block.id.toString())
                    }
                    .setNegativeButton("Cancel", null)
                    // null means do nothing when Cancel is tapped
                    .show()
            }

            // Check if this block is already marked done
            // prefs is like localStorage in React
            // block.title is the key, false is the default value
            val isDone = prefs.getBoolean(block.title, false)
            updateButtonState(binding, isDone)
            //this part update button to show current state

            // Handle mark done button click
            binding.btnMarkDone.setOnClickListener {
                // Flip the current done state
                //if it was true ->becomes false
                //if it was false -> becomes true
                // ! means NOT — true becomes false, false becomes true
                val newDone = !prefs.getBoolean(block.title, false)
                // means flip/reverse - same as toogle in react
                // Save the new state permanently to the phone
                prefs.edit().putBoolean(block.title, newDone).apply()
                //.edit opens shared preferences for writing
                //.apply commit the save in the background

                // Update the button look
                updateButtonState(binding, newDone)

                // Tell MainActivity to refresh the progress bar
                onProgressUpdate()
            }
        }

        private fun updateButtonState(binding: ItemBlockBinding, isDone: Boolean) {
            if (isDone) {
                // Done state — green checkmark
                binding.btnMarkDone.text = "✓ Done"
                binding.btnMarkDone.setTextColor(Color.parseColor("#22C55E"))
            } else {
                // Not done state — default muted color
                binding.btnMarkDone.text = "Mark Done"
                binding.btnMarkDone.setTextColor(Color.parseColor("#8899BB"))
            }
        }
    }

    // How many total rows does the RecyclerView need to draw?
    // This counts both section headers AND block cards
    override fun getItemCount(): Int = flatList.size

    // What type is the item at this position?
    // Tells RecyclerView which XML layout to inflate
    override fun getItemViewType(position: Int): Int {
        return if (flatList[position] is Section) TYPE_SECTION else TYPE_BLOCK
    }// is in kotlin = instance of in java

    // Create a new ViewHolder when RecyclerView needs one
    // LayoutInflater turns your XML file into actual views
    // Same as React rendering JSX into real HTML elements
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(context)
        return if (viewType == TYPE_SECTION) {
            // it is a section header roe -> inflate item_section.xml
            val binding = ItemSectionBinding.inflate(inflater, parent, false)
            SectionViewHolder(binding)
        } else {
            //it is a block card row -> inflate item_block.xml
            val binding = ItemBlockBinding.inflate(inflater, parent, false)
            BlockViewHolder(binding)
        }
    }   //same as react returning different components based on type
    //if (item.type === "section") return <SectionHeader/>
    //else return <BlockCard/>

    // Fill each ViewHolder with real data from flatList
    // position tells us exactly which item we are drawing
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is SectionViewHolder -> holder.bind(flatList[position] as Section)
            is BlockViewHolder -> holder.bind(flatList[position] as Block)
            // "as Section" and "as Block" cast the Any type
            // back to the specific type — like (Section) in Java
            // or "as Section" in TypeScript
        }
    }

    // Is this block happening right now?
    // Exact same logic as isCurrentBlock() in your React Schedule
    private fun isCurrentBlock(block: Block): Boolean {
        val now = Calendar.getInstance()
        // Calendar.getInstance() = new Date() in JavaScript
        //Same as:const now = new Date() in js

        val currentHour = now.get(Calendar.HOUR_OF_DAY)
        //gives 24 hr format
        val currentMinute = now.get(Calendar.MINUTE)
        //gets the current minute 0 - 59

        // Convert to total minutes since midnight
        // Same as your timeToMinutes() helper in React
        val currentTotal = currentHour * 60 + currentMinute
        val startTotal = block.startHour * 60 + block.startMinute
        // convert block start time to total minutes
        val endTotal = block.endHour * 60 + block.endMinute

        // Is current time between start and end?
        // "in startTotal until endTotal" is Kotlin's clean way of writing
        // currentTotal >= startTotal && currentTotal < endTotal
        return currentTotal in startTotal until endTotal
        //in start total until end total check if current time
        //falls btn start inclusive and end (exclusive)
        //same as current total >= start total && current total end total < endtotal
        //same as your react version:
        //return current mins >= start mins && currentMins < endMins
    }

    // Calculate progress for the progress bar in MainActivity
    // Returns a Pair — two numbers bundled together
    // First = how many blocks are done
    // Second = total number of blocks
    fun getProgress(): Pair<Int, Int> {
        var done = 0
        var total = 0
        for (section in sections) {
            for (block in section.blocks) {
                total++
                if (prefs.getBoolean(block.title, false)) {
                    done++
                }
            }
        }
        return Pair(done, total)
    }

    fun updateSections(newSections: List<Section>) {
        sections = newSections
        //Rebuild the flat list with the new sections
        //same as rebuilding the array after update in react
        flatList = buildFlatList()
        notifyDataSetChanged()
    }
}