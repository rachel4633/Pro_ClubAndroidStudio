package com.example.pro_club

data class Section(
    val section:String,
    //"Morning", "Classes", "Afternoon", "Evening"
    //This is the heading shown in item_section.xml
    //Same as your section field in the React interface Section

    val timeRange: String,
    //"6:00 - 11:00" - the muted time shown on the right
    // of the section header next to the name

    val blocks: List<Block>
    //This is a LIST of Block objects that belong to this section
    //List<Block> means it can ONLY hold blocks objects nothing else
    //Same as blocks: Block[] in your React TypeScript interface
    //For example Morning's blocks list holds 4 blocks objects:
    //[Wake up & pray ,Morning stretch, Breakfast, Deep coding 1]
)
