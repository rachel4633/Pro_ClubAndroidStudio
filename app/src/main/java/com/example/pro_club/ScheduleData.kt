import com.example.pro_club.Block
import com.example.pro_club.Section

//ScheduleData.kt
object ScheduleData {
    //"Object" means this is singleton
    //Only ONE copy of this exists in the whole app
    //Think of it like a shared notice board - everyone reads
    //from the same board, nobody makes their own copy
    //same as your "const sections" in REACT - define once used

    val sections: List<Section> = listOf(
        //listOf() creates a fixed list - same as [] in JavaScript/TypeScript
        //List<Section> means this list can only hold Section objects
        Section(
            section =   "Morning",
            timeRange = "6:00 -11:00",
            blocks = listOf(

                Block(
                    time = "6:00 - 6:30",
                    title = "Wake up & pray",
                    desc = "Wake up, pray and make your bed",
                    type = "routine",
                    notifyAt = "6:00",
                    startHour = 6,
                    startMinute = 0,
                    endHour = 6,
                    endMinute = 30,
                    motivation = "Rise and shine! A great day start with gratitude "
                ),
                //Each Block() here is one card on your screen
                //We use named parameters (time = . . ., title =...)
                //so the code is readable,and you know exactly
                //what each value is for - same as object properties in React

                  Block(
                      time = "6:30 - 7:00",
                      title = "Morning stretch",
                      desc = "Light full body scratch and warmup",
                      type = "workout",
                      notifyAt = "06:30",
                      startHour = 6,
                      startMinute = 30,
                      endHour = 7,
                      endMinute = 0,
                      motivation = "Activate your body - champions warm up before they win"
                  ),

                Block(
                    time = "7:00 -7:30",
                    title = "Breakfast & freshen up",
                    desc = "Eat a solid breakfast and get ready",
                    type = "routine",
                    notifyAt = "7:00",
                    startHour = 7,
                    startMinute = 0,
                    endHour = 7,
                    endMinute = 30,
                    motivation = "Fuel your body right - you have a  big day ahead "
                ),

                Block(
                    time = "7: 30 - 11:00",
                    title = "Deep coding session 1",
                    desc = "Most important block — deep focus, no distractions",
                    type = "coding",
                    notifyAt = "07:30",
                    startHour = 7,
                    startMinute = 30,
                    endHour = 11,
                    endMinute = 0,
                    motivation = "This is your power hour — lock in and build something great 💻"
                )
            )
        ),
//=================================================================================================================
        Section(
            section = "Classes",
            timeRange = "11:00 -14:10",
            blocks = listOf(
                Block(
                    time = "11:00 - 14:10",
                    title = "Class time",
                    desc = "Attend all classes - stay focused and notes",
                    type = "class",
                    notifyAt = "11:00",
                    startHour = 11,
                    startMinute = 0,
                    endHour = 14,
                    endMinute = 10,
                    motivation = "Stay sharp in class - every lesson in an investment"
                ),
            )
        ),
// ============================================================================

        Section(
            section = "Afternoon",
            timeRange = "14:10 - 17:30",
            blocks = listOf(

                Block(
                    time = "14:10 - 15:30",
                    title = "Rest & lunch",
                    desc = "Eat lunch, rest and recharge after classes",
                    type = "break",
                    notifyAt = "14:10",
                    startHour = 14,
                    startMinute = 10,
                    endHour = 16,
                    endMinute = 10,
                    motivation  = "Rest is productive - recharge so you can finish strong"
                ),

                Block(
                   time = "16:00 - 17:30",
                   title = "Deep coding session 2",
                    desc = "Continue from morning or work on projects",
                    type = "coding",
                    notifyAt = "16:00",
                    startHour = 16,
                    startMinute =0,
                    endHour = 17,
                    endMinute =30,
                    motivation = "Second wind! Keep building - progress over perfection"
                )
            )
        ),
//=======================================================================================================
        Section(
            section = "Evening",
            timeRange = "17:30 - 22:00",
            blocks = listOf(

                Block(
                    time = "17:30 - 18:30",
                    title = "Football training",
                    desc = "Indoor football - drills, ball work and fitness",
                    type = "football",
                    notifyAt = "17:30",
                    startHour = 17,
                    startMinute = 30,
                    endHour = 18,
                    endMinute = 30,
                    motivation = "Hit the pitch !Every touch makes you better "
                ),
                Block(
                    time ="18:30 - 19:00",
                    title = "Shower & recover",
                    desc = "Freshen up and let your body start recovering",
                    type = "break",
                    notifyAt = "18:30",
                    startHour = 18,
                    startMinute = 30,
                    endHour = 19,
                    endMinute = 0,
                    motivation = "Recover is part of the grid - take care of your body "
                ),

                Block(
                    time = "19:00 - 20:30",
                    title = "project work & learning",
                    desc = "Work on personal projects or watch learning videos",
                    type="coding",
                    notifyAt = "19:00",
                    startHour = 19,
                    startMinute = 0,
                    endHour = 20,
                    endMinute = 30,
                    motivation = "Build something the world will use one day"
                ),

                Block (
                    time = "20:30 - 21:30",
                    title = "Light coding tasks",
                    desc = "Small tasks, code review and wrap up",
                    type = "coding",
                    notifyAt = "20:30",
                    startHour = 20,
                    startMinute =30,
                    endHour = 21,
                    endMinute = 30,
                    motivation = "Finish strong - small wins compounds into big results"
                ),

                Block(
                    time = "21:30 - 22:00",
                    title = "plan tommorrow & journal",
                    desc = "Write tomorrows's plan and reflect on today",
                    type = "routine",
                    notifyAt = "21:30",
                    startHour = 21,
                    startMinute =30,
                    endHour = 22,
                    endMinute = 0,
                    motivation = "Reflect,plan and be proud of what you did today"
                ),

                Block(
                    time = "22:00",
                    title = "Sleep",
                    desc = "Rest well - consistency is built through good recovery",
                    type = "sleep",
                    notifyAt = "22:00",
                    startHour = 22,
                    startMinute =0,
                    endHour = 23,
                    endMinute = 59,
                    motivation = "You earned this rest .See you at 6AM champion"
                )
            )
        )
    )
}