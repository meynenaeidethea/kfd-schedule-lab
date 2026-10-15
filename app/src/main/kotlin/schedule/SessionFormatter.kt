package schedule

class SessionFormatter {
}

fun sessionType(session: Session): SessionType {
    return when (session) {
        is Lecture -> SessionType.LECTURE
        is Practice -> SessionType.PRACTICE
        is Cancelled -> SessionType.CANCELLED
        is Exam -> SessionType.EXAM
    }
}

fun formatSession(session: Session, withTeachers: Boolean): String {
    val location = when (session) {
        is Lecture -> session.location
        is Practice -> session.location
        is Exam -> session.location
        is Cancelled -> "-"
    }
    val base = "${session.date} | ${sessionType(session).name} | ${session.topic} | $location"

    if (!withTeachers) {
        return base
    }

    val teachers = when (session) {
        is Lecture -> session.teachers.joinToString(", ")
        is Practice -> session.teachers.joinToString(", ")
        is Exam -> session.teachers.joinToString(", ")
        is Cancelled -> "-"
    }

    return "$base | $teachers"
}

fun quoteValue(value: String): String {
    var escaped = ""
    for (character in value) {
        escaped += when (character) {
            '\\' -> "\\\\"
            '"' -> "\\\""
            else -> {
                if (character.code !in 32..126) {
                    "\\u" + character.code.toString(16).uppercase().padStart(4, '0')
                } else {
                    character.toString()
                }
            }
        }
    }
    return "\"" + escaped + "\""
}

fun formatRejection(rejection: ParseResult.Rejected): String {
    val reason = rejection.reason
    val message = when (reason) {
        RejectReason.InvalidSeparator -> "invalid field separator"
        is RejectReason.FieldCount -> "got ${reason.actual} fields, expected 5"
        is RejectReason.EmptyField ->
            "empty required field ${quoteValue(reason.field)}"

        is RejectReason.InvalidDate ->
            "invalid date ${quoteValue(reason.field)}"

        is RejectReason.UnknownType ->
            "unknown session type ${quoteValue(reason.field)}"

        is RejectReason.TooFewTeachers ->
            "type \"EXAM\" requires at least 2 teachers, got ${reason.actual}"

        is RejectReason.Domain -> {
            val description = when (reason.rule) {
                DomainRule.CONTROL -> "control characters"
                DomainRule.WHITESPACE -> "surrounding whitespace"
                DomainRule.CANCELLED_FIELDS -> "invalid cancelled field"
                DomainRule.LOCATION -> "invalid location"
                DomainRule.TEACHER -> "invalid teacher"
                DomainRule.DUPLICATE -> "duplicate teacher"
            }
            "$description in ${quoteValue(reason.field)}: ${quoteValue(reason.value)}"
        }
    }
    return "line ${rejection.line}: $message"
}