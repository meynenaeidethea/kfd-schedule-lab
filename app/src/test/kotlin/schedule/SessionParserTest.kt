package schedule

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class SessionParserTest {
    @Test
    fun structureChecksFollowRequiredPriority() {
        assertEquals(
            ParseResult.Rejected(1, RejectReason.InvalidSeparator),
            findStructureError(1, "")
        )
        assertEquals(
            ParseResult.Rejected(2, RejectReason.FieldCount(2)),
            findStructureError(2, "|")
        )
        assertEquals(
            ParseResult.Rejected(3, RejectReason.EmptyField("teachers")),
            findStructureError(3, "2026-10-15|LECTURE|topic|K911|")
        )
        assertEquals(
            null,
            findStructureError(4, "2026-10-15|LECTURE|topic|K911|Ivanov")
        )
    }

    @Test
    fun dateErrorPrecedesUnknownType() {
        assertEquals(
            ParseResult.Rejected(1, RejectReason.InvalidDate("2026-02-29")),
            findHeaderError(1, "2026-02-29|SEMINAR|topic|K911|Ivanov")
        )
        assertEquals(
            ParseResult.Rejected(2, RejectReason.UnknownType("SEMINAR")),
            findHeaderError(2, "2026-10-15|SEMINAR|topic|A100|Ivanov")
        )
        assertEquals(null, parseSessionDate("0000-01-01"))
        assertEquals(null, parseSessionDate("2026-1-01"))
    }

    @Test
    fun examCountPrecedesDomainButDuplicatesAreRejected() {
        assertEquals(
            ParseResult.Rejected(1, RejectReason.TooFewTeachers(1)),
            parseSession(1, "2026-10-15|EXAM| topic|K911|Ivanov")
        )
        assertEquals(
            ParseResult.Rejected(
                2,
                RejectReason.Domain(DomainRule.DUPLICATE, "teachers", "Ivanov")
            ),
            parseSession(2, "2026-10-15|EXAM|topic|K911|Ivanov,Ivanov")
        )
    }
}