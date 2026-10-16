package schedule

interface CommandHandlers {
    fun period(command: Command.Period): Int
    fun byType(command: Command.ByType): Int
    fun errors(command: Command.Errors): Int
}

object StudentCommandHandlers : CommandHandlers {
    override fun period(command: Command.Period): Int = runPeriod(command)
    override fun byType(command: Command.ByType): Int = runType(command)
    override fun errors(command: Command.Errors): Int = runErrors(command)
}

private fun runSelection(
    file: String,
    withTeachers: Boolean,
    matches: (Session) -> Boolean
): Int {
    var total = 0
    var rejected = false

    forEachScheduleLine(file) { number, line ->
        when (val result = parseSession(number, line)) {
            is ParseResult.Success -> {
                if (matches(result.session)) {
                    println(formatSession(result.session, withTeachers))
                    total += 1
                }
            }

            is ParseResult.Rejected -> {
                rejected = true
            }
        }
    }

    println("Total: $total")
    return if (rejected) 1 else 0
}

fun runPeriod(command: Command.Period): Int {
    return runSelection(command.file, false) { session ->
        session.date >= command.from && session.date <= command.to
    }
}

fun runType(command: Command.ByType): Int {
    return runSelection(command.file, true) { session ->
        sessionType(session) == command.type
    }
}

fun runErrors(command: Command.Errors): Int {
    var total = 0
    var rejected = 0

    forEachScheduleLine(command.file) { number, line ->
        total += 1
        val result = parseSession(number, line)
        when (result) {
            is ParseResult.Success -> {}
            is ParseResult.Rejected -> {
                println(formatRejection(result))
                rejected += 1
            }
        }
    }

    println("Rejected: $rejected of $total")
    return if (rejected > 0) 1 else 0
}