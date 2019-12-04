package edu.calbaptist.bible_ly

import java.util.*
import kotlin.collections.ArrayList





data class Token (
    var expireDate: Date?,
    var id:String,
    var user:User?)


    /*var expireDate: Date? = null,
    var id:String,
    var user:User? = null*/

{
    constructor() : this( null,"",null)
}

data class BiblelyClass (
    var classID: String,
    var teacher: User?,
    var isPublic: Boolean,
    var name: String,
    var description: String,
    //var students: ArrayList<User>,
    var classLogo: String,
    var tokens: ArrayList<Token>
){
    constructor() : this( "",null,true, "","","", ArrayList<Token>())
}
//class dont need event because in database multiple events
//will have the same class as classID

data class Verse(
    val id: String,
    val chapter: String,
    val verse: String,
    val text: String,
    val book: String

) {
    constructor() : this( "","","", "","")
    fun getVerseAsInt():Int{
        return verse.toInt()
    }
    fun chapterAsInt():Int{
        return chapter.toInt()
    }
}

data class Bible(
    var numOfChapters: Int,
    var verses: List<Verse>
) {
    constructor(): this(0, ArrayList<Verse>())
}

data class BibleKey (
    val bookNumber: Int,
    val genreId: String,
    val name: String,
    val testament: String
) {
    constructor() : this( 0,"","", "")
    fun getBibleKeyBookNumberAsInt():Int {
        return bookNumber.toInt()
    }
}

data class User(
   // val userID: Int,
    val userName: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val photoID: String
) {
    constructor() : this( "","","", "","")
}
data class Comment(
    var path: String,
    var user:User?,
    var text:String,
    var date:Date?
){
    constructor() : this( "",null, "",null)
}
data class CommentCardViewItem(
    var path: String,
    var userText:String,
    var user:User?,
    var text:String,
    var date:Date?
){
    constructor() : this( "","",null, "",null)
}
data class Note(
    var noteID: String,
    var date: Date?,
    val user: User?,
    val book: String,
    val verseNum: String,
    val verseChapter: String,
    val verseText: String,
    var noteText: String,
    var noteTitle: String,
    var hasComment:Boolean,
    var shared:Boolean,
    var clss:String
){
    constructor() : this( "",null,null,"","","","", "","",false,false,"")
}
data class NoteCardViewItem(
    var isHeader:Boolean,
    var noteID: String,
    var date: Date?,
    val user: User?,
    val book: String,
    val verseNum: String,
    val verseChapter: String,
    val verseText: String,
    var noteText: String,
    var noteTitle: String,
    var hasComment:Boolean,
    var shared:Boolean,
    var clss:String
){
    constructor() : this( false,"",null,null,"","","","", "","",false,false,"")
}

data class Flag(
    val flagID: Int,
    val verse: Verse,
    var type: Int,
    val user: User
)

data class Question(
    val questionID: Int,
    val user: User,
    val verse: Verse,
    var text: String,
    var isAnonymous: Boolean,
    val biblelyClassName: BiblelyClass
)

data class Event(
    var eventID:String,
    var name: String = "name",
//    var repeat: Int = 0,
    var date: Date? =null,
//    var endDate: Date? = null,
    var description: String = "Event desc",
    val clss: BiblelyClass? = null,
    var createdDate: Date? = null
){
    constructor() : this( "","",null, "",null,null)
}

data class ClassMember(
    val memberID: Int,
    val biblelyClassName: BiblelyClass,
    val user: User
)

data class BoardRecycleViewItem(
    val id: Int,
    val title: String,
    val date: String,
    val description: String?,
    val type: Int
)

data class Notification(
    var id: Int = 0,
    var title: String = "Note",
    var createdDate: Date = Date(),
    var description: String? = null,
    var disMissed: Boolean = false
)