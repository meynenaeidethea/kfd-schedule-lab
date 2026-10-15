package schedule

import java.time.LocalDate

sealed interface Session {
    val date: LocalDate
    val topic: String
}

class Cancelled(
    override val date: LocalDate,
    override val topic: String
) : Session {
    init {
        require(date.year in 1..9999) {
            "Year must be between 1 and 9999 inclusive"
        }
        require(topic.isNotBlank()) {
            "Topic must not be blank"
        }
        require(topic == topic.trim()) {
            "Topic must not have surrounding spaces"
        }
        require(topic.none { character -> character.isISOControl() }) {
            "Topic must not have control characters"
        }
    }
}

class Lecture(
    override val date: LocalDate,
    override val topic: String,
    val location: String,
    teachers: List<String>
) : Session {
    val teachers: List<String> = java.util.List.copyOf(teachers)

    init {
        require(date.year in 1..9999) {
            "Year must be between 1 and 9999 inclusive"
        }
        requireText(topic)
        requireText(location)
        require(location != "-") {
            "Location must not be -"
        }
        require(this.teachers.isNotEmpty()) {
            "Lecture requires at least one teacher"
        }
        for (teacher in this.teachers) {
            requireText(teacher)
            require(teacher != "-") {
                "Teacher must not be -"
            }
        }
        require(this.teachers.toSet().size == this.teachers.size) {
            "Teachers must not repeat"
        }
    }
}

class Practice(
    override val date: LocalDate,
    override val topic: String,
    val location: String,
    teachers: List<String>
) : Session {
    val teachers: List<String> = java.util.List.copyOf(teachers)

    init {
        require(date.year in 1..9999) {
            "Year must be between 1 and 9999 inclusive"
        }
        requireText(topic)
        requireText(location)
        require(location != "-") {
            "Location must not be -"
        }
        require(this.teachers.isNotEmpty()) {
            "Practice requires at least one teacher"
        }
        for (teacher in this.teachers) {
            requireText(teacher)
            require(teacher != "-") {
                "Teacher must not be -"
            }
        }
        require(this.teachers.toSet().size == this.teachers.size) {
            "Teachers must not repeat"
        }
    }
}

class Exam(
    override val date: LocalDate,
    override val topic: String,
    val location: String,
    teachers: List<String>
) : Session {
    val teachers: List<String> = java.util.List.copyOf(teachers)

    init {
        require(date.year in 1..9999) {
            "Year must be between 1 and 9999 inclusive"
        }
        requireText(topic)
        requireText(location)
        require(location != "-") {
            "Location must not be -"
        }
        require(this.teachers.size >= 2) {
            "Exam requires at least two teachers"
        }
        for (teacher in this.teachers) {
            requireText(teacher)
            require(teacher != "-") {
                "Teacher must not be -"
            }
        }
        require(this.teachers.toSet().size == this.teachers.size) {
            "Teachers must not repeat"
        }
    }
}

private fun requireText(value: String) {
    require(value.isNotBlank()) {
        "Text cannot be blank"
    }
    require(value == value.trim()) {
        "Text must not have surrounding whitespace"
    }
    require(value.none { character -> character.isISOControl() }) {
        "Text must not contain control characters"
    }
}