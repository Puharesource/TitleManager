import dev.tarkan.titlemanager.bukkit.update.SemanticVersion
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UpdateServiceTest {
    @Test
    fun `semantic versions order stable releases after prereleases`() {
        assertTrue(SemanticVersion.parse("3.0.0")!! > SemanticVersion.parse("3.0.0-alpha")!!)
        assertFalse(SemanticVersion.parse("3.0.0-alpha")!! > SemanticVersion.parse("3.0.0")!!)
    }

    @Test
    fun `semantic versions order prerelease identifiers by SemVer precedence`() {
        assertTrue(SemanticVersion.parse("3.0.0-alpha.1")!! > SemanticVersion.parse("3.0.0-alpha")!!)
        assertTrue(SemanticVersion.parse("3.0.0-alpha.2")!! > SemanticVersion.parse("3.0.0-alpha.1")!!)
        assertTrue(SemanticVersion.parse("3.0.0-beta")!! > SemanticVersion.parse("3.0.0-alpha.2")!!)
        assertTrue(SemanticVersion.parse("3.0.1-alpha")!! > SemanticVersion.parse("3.0.0")!!)
    }

    @Test
    fun `semantic versions ignore build metadata and optional v prefix`() {
        assertFalse(SemanticVersion.parse("v3.0.0+build.5")!! > SemanticVersion.parse("3.0.0+build.1")!!)
        assertFalse(SemanticVersion.parse("3.0.0+build.1")!! > SemanticVersion.parse("v3.0.0+build.5")!!)
    }

    @Test
    fun `stable versions only accept stable updates`() {
        val stable = SemanticVersion.parse("3.0.0")!!

        assertTrue(SemanticVersion.parse("3.0.1")!!.isEligibleUpdateFor(stable))
        assertFalse(SemanticVersion.parse("3.0.1-beta.1")!!.isEligibleUpdateFor(stable))
        assertFalse(SemanticVersion.parse("3.0.1-alpha.1")!!.isEligibleUpdateFor(stable))
        assertFalse(SemanticVersion.parse("3.0.1-SNAPSHOT+abcdef0")!!.isEligibleUpdateFor(stable))
    }

    @Test
    fun `prerelease versions accept updates from same or more stable channels`() {
        val alpha = SemanticVersion.parse("3.0.0-alpha.1")!!
        val beta = SemanticVersion.parse("3.0.0-beta.1")!!
        val snapshot = SemanticVersion.parse("3.0.0-SNAPSHOT+abcdef0")!!

        assertTrue(SemanticVersion.parse("3.0.0-alpha.2")!!.isEligibleUpdateFor(alpha))
        assertTrue(SemanticVersion.parse("3.0.0-beta.1")!!.isEligibleUpdateFor(alpha))
        assertTrue(SemanticVersion.parse("3.0.0")!!.isEligibleUpdateFor(alpha))
        assertFalse(SemanticVersion.parse("3.0.0-alpha.2")!!.isEligibleUpdateFor(beta))
        assertTrue(SemanticVersion.parse("3.0.0-beta.2")!!.isEligibleUpdateFor(beta))
        assertTrue(SemanticVersion.parse("3.0.0-SNAPSHOT+fedcba0")!!.isEligibleUpdateFor(snapshot))
        assertTrue(SemanticVersion.parse("3.0.0-SNAPSHOT+fedcba0")!! > SemanticVersion.parse("3.0.0-SNAPSHOT")!!)
    }
}
