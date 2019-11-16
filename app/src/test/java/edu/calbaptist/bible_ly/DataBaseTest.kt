package edu.calbaptist.bible_ly

import com.google.firebase.firestore.FirebaseFirestore
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before
import org.mockito.Mockito.mock

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class DataBaseTest {
    lateinit var db: FireStoreDB
    lateinit var note: Note
    lateinit var event: Event

    @Before
    fun setUp(){
        db=FireStoreDB()
        note = mock(Note::class.java)
        event = mock(Event::class.java)
    }

    @Test
    fun CheckAddNote() {
        assertTrue(db.SaveNote(note))
    }

    @Test
    fun checkGetNote(){
        assertTrue(db.getNote("note1"))
    }

    @Test
    fun checkAddEvent(){
        assertTrue(db.saveEvent(event))
    }

    @Test
    fun checkGetEventForClass(){
        assertTrue(db.getEventsForClass("class1"))
    }
}
