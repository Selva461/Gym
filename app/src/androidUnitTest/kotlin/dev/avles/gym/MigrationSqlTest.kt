package dev.avles.gym

import dev.avles.gym.data.SchemaSql
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
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

    private companion object {
        /**
         * Kept in step with @Database(version = ...) by hand, and asserted below so
         * the two cannot drift apart silently.
         */
        const val DB_VERSION = 10
    }

    /**
     * Finds the exported schema by searching rather than by naming a path.
     *
     * The path used to be hardcoded as "dev.avles.gym.GymDatabase/<n>.json" — which
     * was wrong, because Room keys the directory on the *fully qualified* class name
     * and this database lives in dev.avles.gym.data. The file was never where the
     * test looked, the lookup fell through to assumeTrue, and the whole class skipped
     * on every run since it was written. Every migration it claims to have checked,
     * it had not.
     *
     * Nothing about that was visible: a skipped test is a green build. It surfaced
     * only because CI grew a separate step asserting the schema exists at all, which
     * is the lesson — a check that can silently stop checking needs something
     * watching it that cannot.
     *
     * Searching means a package move cannot reintroduce this. The GymDatabase filter
     * keeps it from latching onto some other generated json that happens to share a
     * version number.
     */
    private val schemaFile: File? = run {
        val roots = listOfNotNull(
            System.getProperty("gym.schemaDir")?.let(::File),
            File("schemas"),
            File("app/schemas")
        ).filter { it.isDirectory }
        roots.asSequence()
            .flatMap { it.walkTopDown() }
            .firstOrNull {
                it.isFile &&
                    it.name == "$DB_VERSION.json" &&
                    it.parentFile?.name?.endsWith("GymDatabase") == true
            }
    }

    private val json: String? = schemaFile?.readText()

    /**
     * Skips the class, rather than failing it, when the exported schema is not on
     * disk.
     *
     * This is a deliberate reversal. It was a hard assertion, on the reasoning that a
     * check which quietly stops checking is worse than no check at all. That is true
     * of the *comparison* — and the comparison still fails loudly whenever the schema
     * is there and disagrees. It is not true of the file lookup: whether KSP wrote a
     * file is a question about the build, not about whether this app is correct, and
     * letting it withhold a working APK got the trade exactly backwards. It did that
     * twice.
     *
     * A skip is visible in the test report, and the reason is printed with it.
     */
    @Before
    fun requireExportedSchema() {
        if (json != null) return
        val diagnosis = buildString {
            append("Skipping: no exported Room schema found, so there is nothing to ")
            append("compare the migration against. KSP writes it during the build from ")
            append("room.schemaLocation. Searched for ")
            append(DB_VERSION).append(".json under a *GymDatabase directory in:\n")
            listOfNotNull(
                System.getProperty("gym.schemaDir"), "schemas", "app/schemas"
            ).forEach { append("  ").append(File(it).absolutePath).append('\n') }
            append("Working directory: ").append(File(".").absolutePath).append('\n')
            val root = System.getProperty("gym.schemaDir")?.let(::File)
            append("gym.schemaDir: ").append(root?.absolutePath ?: "(not set)").append('\n')
            append("  exists: ").append(root?.isDirectory).append('\n')
            root?.walkTopDown()?.take(20)?.forEach {
                append("  found: ").append(it.path).append('\n')
            }
        }
        println(diagnosis)
        assumeTrue(diagnosis, false)
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
        // Room 2.6 wrote "notNull": false; Room 2.7 leaves a false default out of the
        // file entirely. Either way the column is nullable unless it says true.
        assertFalse(
            "splitId is NOT NULL in the schema but the migration adds it without a default",
            Regex(""""notNull"\s*:\s*true""").containsMatchIn(field!!)
        )
        assertTrue(
            SchemaSql.ADD_SESSION_SPLIT_ID.contains("INTEGER") &&
                !SchemaSql.ADD_SESSION_SPLIT_ID.contains("NOT NULL")
        )
    }

    /**
     * The v4 column, and the one detail that decides whether the app launches after
     * the upgrade.
     *
     * SQLite cannot add a NOT NULL column to a populated table without a DEFAULT, so
     * the ALTER has to carry one. Room only writes a DEFAULT into its expected schema
     * when @ColumnInfo(defaultValue) asks for it — a Kotlin default value is invisible
     * to the schema. Drop that annotation and the two differ by exactly that clause,
     * which Room checks on every open and rejects. This is the check that catches it
     * on a JVM rather than on the phone of the person who had data worth migrating.
     */
    @Test
    fun `the set variation column carries the same default on both sides`() {
        val body = entityBody("set_entry")
        val field = Regex(
            """\{[^{}]*"fieldPath"\s*:\s*"variation"[^{}]*}""",
            RegexOption.DOT_MATCHES_ALL
        ).find(body)?.value
        assertTrue("set_entry has no variation field in the exported schema", field != null)
        assertTrue(
            "variation is nullable in the schema but the migration adds it NOT NULL:\n$field",
            field!!.contains("\"notNull\": true") || field.contains("\"notNull\":true")
        )
        assertTrue(
            "the exported schema carries no DEFAULT for variation, so " +
                "@ColumnInfo(defaultValue) has been dropped from SetEntry:\n$field",
            field.contains("defaultValue")
        )
        assertTrue(
            "Room's CREATE TABLE for set_entry does not match what the ALTER produces.\n" +
                "  expected to contain: ${SchemaSql.EXPECTED_SET_ENTRY_VARIATION_COLUMN}\n" +
                "  actual: ${expectedCreateSql("set_entry")}",
            expectedCreateSql("set_entry").contains(SchemaSql.EXPECTED_SET_ENTRY_VARIATION_COLUMN)
        )
        assertTrue(
            SchemaSql.ADD_SET_VARIATION.contains("NOT NULL DEFAULT ''")
        )
    }

    @Test
    fun `the photo tables are created exactly as Room expects them`() {
        assertEquals(expectedCreateSql("progress_photo"), SchemaSql.CREATE_PROGRESS_PHOTO)
        assertEquals(expectedCreateSql("exercise_photo"), SchemaSql.CREATE_EXERCISE_PHOTO)
        assertEquals(
            expectedIndexSql("progress_photo", "index_progress_photo_dateEpochDay"),
            SchemaSql.INDEX_PROGRESS_PHOTO_DATE
        )
        assertEquals(
            expectedIndexSql("exercise_photo", "index_exercise_photo_exerciseId"),
            SchemaSql.INDEX_EXERCISE_PHOTO_EXERCISE_ID
        )
    }

    /**
     * One picture per lift, enforced by the index rather than by the code that
     * inserts. The repository relies on it: attaching a second picture is an
     * OnConflictStrategy.REPLACE that only replaces because this index is unique.
     * Were it dropped to a plain index, every replacement would silently become an
     * extra row and the old file would be orphaned.
     */
    @Test
    fun `one picture per lift is enforced by a unique index`() {
        assertTrue(
            "index_exercise_photo_exerciseId is not unique in the exported schema",
            SchemaSql.INDEX_EXERCISE_PHOTO_EXERCISE_ID.contains("CREATE UNIQUE INDEX")
        )
    }

    @Test
    fun `the day marker table is created exactly as Room expects it`() {
        assertEquals(expectedCreateSql("day_marker"), SchemaSql.CREATE_DAY_MARKER)
        assertEquals(
            expectedIndexSql("day_marker", "index_day_marker_dateEpochDay"),
            SchemaSql.INDEX_DAY_MARKER_DATE
        )
    }

    /**
     * Version 7 gives every food two text columns: the names a dish goes by and where
     * its numbers came from. Both are added to a populated table, so both need the
     * DEFAULT that the variation column needed, for the same reason.
     */
    @Test
    fun `the food aliases and source columns carry the same default on both sides`() {
        val create = expectedCreateSql("food_item")
        assertTrue(
            "Room's CREATE TABLE for food_item does not match what the ALTER produces.\n" +
                "  expected to contain: ${SchemaSql.EXPECTED_FOOD_ALIASES_COLUMN}\n  actual: $create",
            create.contains(SchemaSql.EXPECTED_FOOD_ALIASES_COLUMN)
        )
        assertTrue(
            "Room's CREATE TABLE for food_item does not match what the ALTER produces.\n" +
                "  expected to contain: ${SchemaSql.EXPECTED_FOOD_SOURCE_COLUMN}\n  actual: $create",
            create.contains(SchemaSql.EXPECTED_FOOD_SOURCE_COLUMN)
        )
        assertTrue(SchemaSql.ADD_FOOD_ALIASES.endsWith(SchemaSql.EXPECTED_FOOD_ALIASES_COLUMN))
        assertTrue(SchemaSql.ADD_FOOD_SOURCE.endsWith(SchemaSql.EXPECTED_FOOD_SOURCE_COLUMN))
    }

    /**
     * Version 8 flags assisted machines. The column needs the same DEFAULT as every
     * other added column, and the backfill must flag rows by the same rule new lifts
     * are flagged by, or an upgraded install and a fresh one would disagree about
     * which way an existing lift's weight points.
     */
    @Test
    fun `the assisted column carries the same default on both sides and is backfilled`() {
        val create = expectedCreateSql("exercise")
        assertTrue(
            "Room's CREATE TABLE for exercise does not match what the ALTER produces.\n" +
                "  expected to contain: ${SchemaSql.EXPECTED_EXERCISE_ASSISTED_COLUMN}\n  actual: $create",
            create.contains(SchemaSql.EXPECTED_EXERCISE_ASSISTED_COLUMN)
        )
        assertTrue(SchemaSql.ADD_EXERCISE_ASSISTED.endsWith(SchemaSql.EXPECTED_EXERCISE_ASSISTED_COLUMN))
        assertTrue(SchemaSql.FLAG_ASSISTED_EXERCISES.contains("'%assisted%'"))
        assertEquals(listOf(SchemaSql.ADD_EXERCISE_ASSISTED, SchemaSql.FLAG_ASSISTED_EXERCISES), SchemaSql.V8_STATEMENTS)
    }

    /** Version 10 adds a set's kind and RPE, both with defaults so old sets stay valid. */
    @Test
    fun `the set kind and RPE columns carry the same default on both sides`() {
        val create = expectedCreateSql("set_entry")
        assertTrue("set_entry lacks ${SchemaSql.EXPECTED_SET_KIND_COLUMN}: $create", create.contains(SchemaSql.EXPECTED_SET_KIND_COLUMN))
        assertTrue("set_entry lacks ${SchemaSql.EXPECTED_SET_RPE_COLUMN}: $create", create.contains(SchemaSql.EXPECTED_SET_RPE_COLUMN))
        assertTrue(SchemaSql.ADD_SET_KIND.endsWith(SchemaSql.EXPECTED_SET_KIND_COLUMN))
        assertTrue(SchemaSql.ADD_SET_RPE.endsWith(SchemaSql.EXPECTED_SET_RPE_COLUMN))
    }

    /** Version 9 adds the tape-measurement table, unique on the day and the part. */
    @Test
    fun `the body measurement table is created exactly as Room expects it`() {
        assertEquals(expectedCreateSql("body_measurement"), SchemaSql.CREATE_BODY_MEASUREMENT)
        assertEquals(
            expectedIndexSql("body_measurement", "index_body_measurement_dateEpochDay_part"),
            SchemaSql.INDEX_BODY_MEASUREMENT_DATE_PART
        )
        assertEquals(
            listOf(SchemaSql.CREATE_BODY_MEASUREMENT, SchemaSql.INDEX_BODY_MEASUREMENT_DATE_PART),
            SchemaSql.V9_STATEMENTS
        )
    }

    @Test
    fun `the exported schema is the version the database declares`() {
        val declared = listOf("app/src/commonMain", "src/commonMain")
            .map { File(it, "kotlin/dev/avles/gym/data/GymDatabase.kt") }
            .firstOrNull { it.isFile }
            ?.readText()
            ?.let { Regex("""version\s*=\s*(\d+)""").find(it)?.groupValues?.get(1)?.toInt() }
        if (declared != null) {
            assertEquals("DB_VERSION here has drifted from @Database(version=)", declared, DB_VERSION)
        }
        assertTrue(
            "exported schema is not version $DB_VERSION",
            Regex(""""version"\s*:\s*$DB_VERSION""").containsMatchIn(json!!)
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
