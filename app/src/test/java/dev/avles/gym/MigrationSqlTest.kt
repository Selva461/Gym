package dev.avles.gym

import dev.avles.gym.data.SchemaSql
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Checks the hand-written v3 migration against the schema Room actually exported.
 *
 * This is the one piece of this app that fails loudly in the worst possible place.
 * Room compares the on-disk schema with its own expectation on every open, so a
 * `CREATE TABLE` in a migration that differs from Room's by a single word does not
 * misbehave quietly — it throws on launch, for exactly the people who had data worth
 * migrating. There is no way to notice that by reading the code carefully, because
 * the thing you would be checking against is generated.
 *
 * `exportSchema = true` writes that generated truth to `app/schemas/...json` during
 * the build, with the `createSql` for every table and index. So the check is simply:
 * does what the migration runs equal what Room says the table should be.
 *
 * A real migration test (`MigrationTestHelper`) would additionally prove the
 * migration *runs*, but it needs an instrumented device. This catches the failure
 * that device would catch, on a JVM, in CI.
 */
class MigrationSqlTest {

    private val schemaFile: File? = listOf(
        // Gradle runs unit tests with the module directory as the working directory;
        // the second path is for a run rooted at the repository instead.
        File("schemas/dev.avles.gym.GymDatabase/3.json"),
        File("app/schemas/dev.avles.gym.GymDatabase/3.json")
    ).firstOrNull { it.isFile }

    private val json: String? = schemaFile?.readText()

    @Test
    fun `the exported schema is where the build should have put it`() {
        // Not an assumption-skip: if this file stops being generated the rest of the
        // class silently stops checking anything, and a quiet gate is worse than none.
        assertTrue(
            "No exported Room schema found. It is written during the build by KSP " +
                "with room.schemaLocation, so this test must run after the main " +
                "sources are compiled. Looked in app/schemas/dev.avles.gym.GymDatabase/.",
            json != null
        )
    }

    @Test
    fun `the split table is created exactly as Room expects it`() {
        assertEquals(expectedCreateSql("split"), SchemaSql.CREATE_SPLIT)
    }

    @Test
    fun `the split_exercise table is created exactly as Room expects it`() {
        assertEquals(expectedCreateSql("split_exercise"), SchemaSql.CREATE_SPLIT_EXERCISE)
    }

    @Test
    fun `both split_exercise indices match Room's own`() {
        assertEquals(
            expectedIndexSql("split_exercise", "index_split_exercise_splitId"),
            SchemaSql.INDEX_SPLIT_EXERCISE_SPLIT_ID
        )
        assertEquals(
            expectedIndexSql("split_exercise", "index_split_exercise_exerciseId"),
            SchemaSql.INDEX_SPLIT_EXERCISE_EXERCISE_ID
        )
    }

    /**
     * The added column has to be nullable, and Room has to agree that it is. A
     * NOT NULL here would be rejected by SQLite without a default anyway, but the
     * failure worth guarding is the opposite one: someone "tidying" the entity to a
     * non-null Long later, while this ALTER stays as it is.
     */
    @Test
    fun `the session split column is nullable on both sides`() {
        val body = entityBody("workout_session")
        val field = Regex(
            """\{[^{}]*"fieldPath"\s*:\s*"splitId"[^{}]*}""",
            RegexOption.DOT_MATCHES_ALL
        ).find(body)?.value
        assertTrue("workout_session has no splitId field in the exported schema", field != null)
        assertTrue(
            "splitId is NOT NULL in the schema but the migration adds it without a default",
            field!!.contains("\"notNull\": false") || field.contains("\"notNull\":false")
        )
        assertTrue(
            SchemaSql.ADD_SESSION_SPLIT_ID.contains("INTEGER") &&
                !SchemaSql.ADD_SESSION_SPLIT_ID.contains("NOT NULL")
        )
    }

    @Test
    fun `the exported schema is version 3`() {
        assertTrue(
            "exported schema is not version 3",
            Regex(""""version"\s*:\s*3""").containsMatchIn(json!!)
        )
    }

    // ---------------------------------------------------------------- helpers

    /** The slice of JSON describing one entity, from its tableName to the next one. */
    private fun entityBody(table: String): String {
        val doc = json!!
        val start = Regex(""""tableName"\s*:\s*"$table"""").find(doc)?.range?.first
        assertTrue("no entity named $table in the exported schema", start != null)
        val nextTable = Regex(""""tableName"\s*:\s*"(?!$table")""").find(doc, start!! + 1)?.range?.first
        return doc.substring(start, nextTable ?: doc.length)
    }

    private fun expectedCreateSql(table: String): String =
        firstCreateSql(entityBody(table)).replace("\${TABLE_NAME}", table)

    private fun expectedIndexSql(table: String, indexName: String): String {
        val body = entityBody(table)
        val at = body.indexOf("\"$indexName\"")
        assertTrue("index $indexName not found on $table", at >= 0)
        return firstCreateSql(body.substring(at)).replace("\${TABLE_NAME}", table)
    }

    /** Pulls the next "createSql" string value and unescapes it. */
    private fun firstCreateSql(text: String): String {
        val match = Regex(""""createSql"\s*:\s*"((?:[^"\\]|\\.)*)"""").find(text)
        assertTrue("no createSql found in the exported schema fragment", match != null)
        return match!!.groupValues[1]
            .replace("\\\"", "\"")
            .replace("\\\\", "\\")
    }
}
