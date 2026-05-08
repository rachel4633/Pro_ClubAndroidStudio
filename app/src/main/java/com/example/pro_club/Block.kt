package com.example.pro_club

data class Block(
    val id: Int = 0,

    val time: String,
    // "6:00 - 6:30" - the time shown on the card
    //string means it holds text -same as string inTypescript

    val title: String,
    // "wake up & pray" - the bold heading on hte card

    val desc: String,
    //"Wake up and pray and make your bed" - the similar description

    val type: String,
    // "routine","coding","sleep" etc
    //This controls which color the left border and badge will be
    //same as your type field in the React interface Block

    val notifyAt: String = "",
    //"6:00" - the time to fire the notification alarm
    //We pass this to AlarmManager later in MainActivity

    val startHour: Int,
    //6 - the hour the block starts
    //int means it holds a whole number - same as number in TypeScript
    val startMinute: Int,
    val endMinute: Int,

    val endHour: Int,
    //30 - the minutes the block ends
    //startHour, startMinute, endHour, endMinute are all used in
    //isCurrentBlock() in MainActivity to check if this block
    // is happening RIGHT NOW - same logic as your React version

    val motivation: String,
    //"Rise and shine! A great day starts with gratitude  🙏"
    //The italic quote  shown at the bottom of each card
    val section: String = "",
    var isPinned: Boolean = false
)

