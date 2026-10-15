package schedule

import java.time.LocalDate
import java.time.format.DateTimeParseException

sealed interface ParseResult {
    data class Success(val session: Session) : ParseResult
    data class Rejected(val line: Long, val reason: RejectReason) : ParseResult
}

sealed interface RejectReason {
    data object InvalidSeparator : RejectReason
    data class FieldCount(val actual: Int) : RejectReason
    data class EmptyField(val field: String) : RejectReason
    data class InvalidDate(val field: String) : RejectReason
    data class UnknownType(val field: String) : RejectReason
    data class TooFewTeachers(val actual: Int) : RejectReason
    data class Domain(
        val rule: DomainRule,
        val field: String,
        val value: String
    ) : RejectReason
}

enum class DomainRule {
    CONTROL, WHITESPACE, CANCELLED_FIELDS,
    LOCATION, TEACHER, DUPLICATE,
}

class SessionParser {

}

fun findStructureError(number: Long, line: String): ParseResult.Rejected? {
    if ('|' !in line) {
        return ParseResult.Rejected(number, RejectReason.InvalidSeparator)
    }

    val fields = line.split('|')
    if (fields.size != 5) {
        return ParseResult.Rejected(number, RejectReason.FieldCount(fields.size))
    }

    val names = listOf("date", "type", "topic", "location", "teachers")
    for (index in fields.indices) {
        if (fields[index].isBlank()) {
            return ParseResult.Rejected(number, RejectReason.EmptyField(names[index]))
        }
    }

    return null
}

fun parseSessionDate(value: String): LocalDate? {
    if (!Regex("[0-9]{4}-[0-9]{2}-[0-9]{2}").matches(value)) {
        return null
    }

    try {
        val date = LocalDate.parse(value)
        if (date.year !in 1..9999) {
            return null
        }
        return date
    } catch (_: DateTimeParseException) {
        return null
    }
}

fun findHeaderError(number: Long, line: String): ParseResult.Rejected? {
    val structureError = findStructureError(number, line)
    if (structureError != null) {
        return structureError
    }

    val fields = line.split('|')
    if (parseSessionDate(fields[0]) == null) {
        return ParseResult.Rejected(number, RejectReason.InvalidDate(fields[0]))
    }

    if (SessionType.entries.none { type -> type.name == fields[1] }) {
        return ParseResult.Rejected(number, RejectReason.UnknownType(fields[1]))
    }

    return null
}

private fun findDomainError(fields: List<String>): RejectReason? {
    val names = listOf("date", "type", "topic", "location", "teachers")
    val teachers = fields[4].split(',')

    fun problem(rule: DomainRule, index: Int, value: String): RejectReason? {
        return RejectReason.Domain(rule, names[index], value)
    }

    for (index in fields.indices) {
        if (fields[index].any { character -> character.isISOControl() }) {
            return problem(DomainRule.CONTROL, index, fields[index])
        }
    }

    for (index in 0..3) {
        if (fields[index] != fields[index].trim()) {
            return problem(DomainRule.WHITESPACE, index, fields[index])
        }
    }

    for (teacher in teachers) {
        if (teacher != teacher.trim()) {
            return problem(DomainRule.WHITESPACE, 4, teacher)
        }
    }

    if (fields[1] == "CANCELLED") {
        for (index in 3..4) {
            if (fields[index] != "-") {
                return problem(DomainRule.CANCELLED_FIELDS, index, fields[index])
            }
        }
        return null
    }

    if (fields[3] == "-") {
        return problem(DomainRule.LOCATION, 3, fields[3])
    }

    val invalidTeacher = teachers.firstOrNull { teacher ->
        teacher.isBlank() || teacher == "-"
    }
    if (invalidTeacher != null) {
        return problem(DomainRule.TEACHER, 4, invalidTeacher)
    }

    val seen = mutableSetOf<String>()
    for (teacher in teachers) {
        if (!seen.add(teacher)) {
            return problem(DomainRule.DUPLICATE, 4, teacher)
        }
    }
    return null
}

fun parseSession(number: Long, line: String): ParseResult {
    val headerError = findHeaderError(number, line)
    if (headerError != null) {
        return headerError
    }

    val fields = line.split('|')
    val type = SessionType.valueOf(fields[1])
    val teachers = fields[4].split(',')

    if (type == SessionType.EXAM) {
        val count = teachers.count { teacher -> teacher.isNotBlank() && teacher != "-"}
        if (count < 2) {
            return ParseResult.Rejected(number, RejectReason.TooFewTeachers(count))
        }
    }

    val domainError = findDomainError(fields)
    if (domainError != null) {
        return ParseResult.Rejected(number, domainError)
    }

    val date = checkNotNull(parseSessionDate(fields[0]))
    val session = when (type) {
        SessionType.LECTURE -> Lecture(date, fields[2], fields[3], teachers)
        SessionType.EXAM -> Exam(date, fields[2], fields[3], teachers)
        SessionType.PRACTICE -> Practice(date, fields[2], fields[3], teachers)
        SessionType.CANCELLED -> Cancelled(date, fields[2])
    }

    return ParseResult.Success(session)
}