package schedule

import org.junit.jupiter.api.Test
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith


class SessionTest {
    @Test
    fun cancelledAcceptsBoundaryYears() {
        val first = Cancelled(LocalDate.of(1,1,1), "new")
        val last = Cancelled(LocalDate.of(9999, 12, 31), "last")

        assertEquals(1, first.date.year)
        assertEquals(9999, last.date.year)
        assertEquals("new", first.topic)
    }

    @Test
    fun cancelledRejectsInvalidYears() {
        for (year in listOf(0, 10000)) {
            assertFailsWith<IllegalArgumentException> {
                Cancelled(LocalDate.of(year, 1, 1), "topic")
            }
        }
    }

    @Test
    fun cancelledRejectsInvalidTopics() {
        val date = LocalDate.of(2026, 10, 15)

        for (topic in listOf("", "   ", " topic", "topic ", "A\tB", "A\nB")) {
            assertFailsWith<IllegalArgumentException> {
                Cancelled(date, topic)
            }
        }
    }

    @Test
    fun lectureCopiesSourceTeachers() {
        val names = mutableListOf("Ivanov", "Petrov")
        val lecture = Lecture(
            LocalDate.of(2026, 10, 15),
            "Kotlin",
            "K911",
            names
        )

        names.clear()

        assertEquals(listOf("Ivanov", "Petrov"), lecture.teachers)
    }

    @Test
    fun lectureProtectsExposedTeachers() {
        val lecture = Lecture(
            LocalDate.of(2026, 10, 15),
            "Kotlin",
            "K911",
            listOf("Ivanov", "Petrov")
        )

        assertFailsWith<UnsupportedOperationException> {
            (lecture.teachers as MutableList<String>).clear()
        }

        assertEquals(listOf("Ivanov", "Petrov"), lecture.teachers)
    }
}