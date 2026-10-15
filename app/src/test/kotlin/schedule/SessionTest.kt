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
    fun cancelledRejectsInvalidtopics() {
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

    @Test
    fun activeSessionsRejectInvalidTeachers() {
        val date = LocalDate.of(2026, 10, 15)

        for (names in listOf(
            emptyList<String>(),
            listOf(""),
            listOf("-"),
            listOf(" Ivanov"),
            listOf("A\tB"),
            listOf("Ivanov", "Ivanov")
        )) {
            assertFailsWith<IllegalArgumentException> {
                Lecture(date, "topic", "A100", names)
            }
            assertFailsWith<IllegalArgumentException> {
                Practice(date, "topic", "B100", names)
            }
            assertFailsWith<IllegalArgumentException> {
                Exam(date, "topic", "A100", names)
            }
        }

        assertFailsWith<IllegalArgumentException> {
            Exam(date, "topic", "A100", listOf("Ivanov"))
        }
    }

    @Test
    fun practiceAndExamAcceptValidTeachers() {
        val date = LocalDate.of(2026, 10, 15)
        val practice = Practice(date, "topic", "k911", listOf("Ivanov"))
        val exam = Exam(date, "topic", "k909", listOf("Ivanov", "ivanov"))

        assertEquals(listOf("Ivanov"), practice.teachers)
        assertEquals(listOf("Ivanov", "ivanov"), exam.teachers)
    }

    @Test
    fun examProtectsBothTeacherReferences() {
        val names = mutableListOf("Ivanov", "Petrov")
        val exam = Exam(LocalDate.of(2026, 10, 15), "topic", "k911", names)

        names.clear()
        assertEquals(listOf("Ivanov", "Petrov"), exam.teachers)

        assertFailsWith<UnsupportedOperationException> {
            (exam.teachers as MutableList<String>).clear()
        }
        assertEquals(listOf("Ivanov", "Petrov"), exam.teachers)
    }
}