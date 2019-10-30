package edu.calbaptist.bible_ly

import com.google.firebase.firestore.ServerTimestamp
import java.util.*
import kotlin.collections.ArrayList

data class Token(
    @ServerTimestamp
    var timeStamp: Date? = null,
    var id:String
){
    constructor() : this( null,"")
}
data class Class(
    val classID: String,
    var teacher: User?,
    var isPublic: Boolean,
    var name: String,
    var description: String,
    var students: ArrayList<User>,
    var classLogo: String,
    var tokens: Map<String,Date>
){
    constructor() : this( "",null,true, "","",ArrayList<User>(),"", mapOf())
}
//class dont need event because in database multiple events
//will have the same class as classID

data class Verse(
    val verseID: Int,
    val chapter: String,
    val verseNum: Int,
    val verseText: String,
    val book: String
)

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

data class Note(
    val noteID: Int,
    val user: User,
    val verse: Verse,
    var noteText: String,
    val className: Class
)

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
    val className: Class
)

data class Event(
    var eventID: Int = 0,
    var name: String = "name",
    var repeat: Int = 0,
    var startDate: Date? = null,
    var endDate: Date? = null,
    var description: String = "Event desc",
    val className: String? = null
)

data class ClassMember(
    val memberID: Int,
    val className: Class,
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