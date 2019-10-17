package edu.calbaptist.bible_ly.util

import edu.calbaptist.bible_ly.Event
import java.util.*

object EventUtil {

    /**
     * Create a random Rating POJO.
     */
    private val random:
        get() {
            val event = Event()

            val random = Random()

            val score = random.nextDouble() * 5.0
            val text = REVIEW_CONTENTS[Math.floor(score).toInt()]

            rating.userId = UUID.randomUUID().toString()
            rating.userName = "Random User"
            rating.rating = score
            rating.text = text

            return rating
        }


    /**
     * Get a list of random Events POJOs.
     */
    fun getRandomList(length: Int): List<Rating> {
        val result = ArrayList<Rating>()

        for (i in 0 until length) {
            result.add(random)
        }

        return result
    }
}