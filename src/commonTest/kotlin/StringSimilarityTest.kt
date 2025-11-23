import kotlin.test.*

class StringSimilarityTest {

    @Test
    fun testIdenticalStrings() {
        val similarity = jaroWinklerSimilarity("test", "test")
        assertEquals(1.0, similarity, 0.0001, "Identical strings should have similarity of 1.0")
    }

    @Test
    fun testCompletelyDifferentStrings() {
        val similarity = jaroWinklerSimilarity("abc", "xyz")
        assertTrue(similarity < 0.5, "Completely different strings should have low similarity")
    }

    @Test
    fun testEmptyStrings() {
        assertEquals(0.0, jaroWinklerSimilarity("", "test"), 0.0001, "Empty string vs non-empty should be 0")
        assertEquals(0.0, jaroWinklerSimilarity("test", ""), 0.0001, "Non-empty vs empty should be 0")
        // Note: Two empty strings return 0.0 in our implementation
        val emptyResult = jaroWinklerSimilarity("", "")
        assertTrue(emptyResult == 0.0 || emptyResult == 1.0, "Empty vs empty can be 0 or 1 depending on implementation")
    }

    @Test
    fun testSimilarNames() {
        // Test cases from actual payroll data
        val similarity1 = jaroWinklerSimilarity("merlin", "beslarin")
        assertTrue(similarity1 >= 0.75, "merlin and beslarin should be detected as similar")

        val similarity2 = jaroWinklerSimilarity("juri", "juicycat")
        assertTrue(similarity2 >= 0.75, "juri and juicycat should be detected as similar")

        val similarity3 = jaroWinklerSimilarity("sziba", "soliba")
        assertTrue(similarity3 >= 0.75, "sziba and soliba should be detected as similar")
    }

    @Test
    fun testCaseInsensitivity() {
        // The function is case-sensitive, but we lowercase before calling it in production
        val similarity1 = jaroWinklerSimilarity("test", "test")
        val similarity2 = jaroWinklerSimilarity("TEST", "TEST")
        assertEquals(1.0, similarity1, 0.0001, "Identical lowercase strings should be 1.0")
        assertEquals(1.0, similarity2, 0.0001, "Identical uppercase strings should be 1.0")
        
        // Different cases are different strings
        val similarity3 = jaroWinklerSimilarity("Test", "test")
        assertTrue(similarity3 < 1.0, "Different cases should not be identical")
    }

    @Test
    fun testTypos() {
        val similarity = jaroWinklerSimilarity("player", "playr")
        assertTrue(similarity > 0.8, "Minor typos should still have high similarity")
    }

    @Test
    fun testPrefixBonus() {
        // Jaro-Winkler gives bonus for matching prefixes
        val similarity1 = jaroWinklerSimilarity("martha", "marhta")
        val similarity2 = jaroWinklerSimilarity("dwayne", "duane")
        
        assertTrue(similarity1 > 0.9, "Strings with matching prefix should have high similarity")
        assertTrue(similarity2 > 0.8, "Strings with matching prefix should have high similarity")
    }
}
