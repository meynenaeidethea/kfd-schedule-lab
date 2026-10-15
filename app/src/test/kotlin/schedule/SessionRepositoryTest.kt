package schedule

import java.time.LocalDate
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SessionRepositoryTest {
    @Test
    fun repositoryPreservesOrderDuplicatesAndSnapshots() {
        val repository = InMemorySessionRepository()
        val first = Cancelled(LocalDate.of(2026, 10, 15), "topic")
        val second = Cancelled(LocalDate.of(2026, 10, 16), "other topic")

        repository.add(first)
        val snapshot = repository.all()

        repository.addAll(listOf(second, first))

        assertEquals(listOf(first), snapshot)
        assertEquals(listOf(first, second, first), repository.all())

        assertFailsWith<UnsupportedOperationException> {
            (snapshot as MutableList<Session>).clear()
        }
        assertEquals(listOf(first, second, first), repository.all())
    }

    @Test
    fun auditRecordsEachOperationOnce() {
        val repository = AuditedSessionRepository(InMemorySessionRepository())
        val session = Cancelled(LocalDate.of(2026, 10, 15), "topic")

        repository.add(session)
        repository.addAll(listOf(session, session))
        repository.addAll(emptyList())

        assertEquals(listOf(session, session, session), repository.all())
        assertEquals(3L, repository.added)
        assertEquals(
            listOf(
                AuditEvent("add", 1),
                AuditEvent("addAll", 2),
                AuditEvent("addAll", 0)
            ),
            repository.events()
        )
    }
}