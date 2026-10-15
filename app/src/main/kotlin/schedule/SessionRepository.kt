package schedule

interface SessionRepository {
    fun add(session: Session)
    fun addAll(sessions: List<Session>)
    fun all(): List<Session>
}

class InMemorySessionRepository : SessionRepository {
    private val sessions = mutableListOf<Session>()

    override fun add(session: Session) {
        sessions.add(session)
    }

    override fun addAll(sessions: List<Session>) {
        this.sessions.addAll(sessions)
    }

    override fun all(): List<Session> {
        return java.util.List.copyOf(sessions)
    }
}

data class AuditEvent(val operation: String, val count: Int)

class AuditedSessionRepository(
    private val delegate: SessionRepository
) : SessionRepository by delegate {
    private val journal = mutableListOf<AuditEvent>()

    var added: Long = 0L
        private set

    override fun add(session: Session) {
        delegate.add(session)
        journal.add(AuditEvent("add", 1))
        added += 1
    }

    override fun addAll(sessions: List<Session>) {
        val count = sessions.size
        delegate.addAll(sessions)
        journal.add(AuditEvent("addAll", count))
        added += count
    }

    fun events(): List<AuditEvent> {
        return java.util.List.copyOf(journal)
    }
}