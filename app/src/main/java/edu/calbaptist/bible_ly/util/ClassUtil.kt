package edu.calbaptist.bible_ly.util

import android.content.Context
import edu.calbaptist.bible_ly.Class
import edu.calbaptist.bible_ly.R
import java.util.*

/**
 * Utilities for Classes
 */
object ClassUtil {
    private val NAME_EVENT = arrayOf(
        "Foo", "Bar", "Hello", "World"
    )

    /**
     * Create a random Class
     */
    fun getRandom(context: Context): Class {
        val newClass = edu.calbaptist.bible_ly.Class()
        val random = Random()

        // Name (first element is 'Any')
        var name = context.resources.getStringArray(R.array.class_names)
        name = Arrays.copyOfRange(name, 1, name.size)

        newClass.name = getRandomString(name, random)
        newClass.numEvents = random.nextInt(5)

        return newClass
    }

    private fun getRandomString(array: Array<String>, random: Random): String {
        val ind = random.nextInt(array.size)
        return array[ind]
    }

}