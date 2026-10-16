package schedule

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDate
import kotlin.text.Charsets.UTF_8

class CommandsTest {
    @Test
    fun commandsPreserveOrderBoundariesAndRejections(@TempDir directory: Path) {
        val file = directory.resolve("input.txt")
        Files.writeString(
            file,
            "2026-10-16|CANCELLED|second topic|-|-\r\n" +
                    "2026-10-15|CANCELLED|topic|-|-\r\n" +
                    "2026-11-01|SEMINAR|topic|K911|Ivanov\r\n"
        )
        val path = file.toString()

        assertOutput(
            expectedCode = 1,
            expectedText =
                "2026-10-16 | CANCELLED | second topic | -\n" +
                    "2026-10-15 | CANCELLED | topic | -\n" +
                    "Total: 2\n",
            action = {
                runPeriod(
                    Command.Period(
                        path,
                        LocalDate.of(2026, 10, 15),
                        LocalDate.of(2026, 10, 16)
                    )
                )
            }
        )

        assertOutput(
            expectedCode = 1,
            expectedText =
                "2026-10-16 | CANCELLED | second topic | - | -\n" +
                    "2026-10-15 | CANCELLED | topic | - | -\n" +
                    "Total: 2\n",
            action = {
                runType(Command.ByType(path, SessionType.CANCELLED))
            }
        )

        assertOutput(
            expectedCode = 1,
            expectedText = "Total: 0\n",
            action = {
                runType(Command.ByType(path, SessionType.EXAM))
            }
        )

        assertOutput(
            expectedCode = 1,
            expectedText =
                "line 3: unknown session type \"SEMINAR\"\nRejected: 1 of 3\n",
            action = {
                runErrors(Command.Errors(path))
            }
        )
    }

    private fun assertOutput(expectedCode: Int, expectedText: String, action: () -> Int) {
        val original = System.out
        val bytes = ByteArrayOutputStream()
        val output = PrintStream(bytes, true, UTF_8)

        try {
            System.setOut(output)
            assertEquals(expectedCode, action())
            assertEquals(expectedText, bytes.toString(UTF_8).replace("\r\n", "\n"))
        }
        finally {
            System.setOut(original)
            output.close()
        }
    }
}