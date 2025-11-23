import kotlin.test.*

/**
 * Regression tests using actual input/output files to ensure calculations
 * remain consistent across code changes.
 * 
 * Note: These tests currently only run on JVM (desktop) due to resource loading limitations.
 */
class RegressionTest {

    @Test
    fun testPayrollRegressionWithRealData() {
        // Skip on native - resource loading not implemented
        if (!isJvmPlatform()) return
        // Load the test input file
        val inputText = loadTestResource("/payroll/test_input.txt")
        val lines = inputText.lines()
        
        // Parse the input
        val input = parsePayrollInput(lines)
        
        // Calculate payroll
        val payroll = calculatePayroll(input)
        
        // Verify key totals match expected values (with small tolerance for rounding)
        // These are the approximate values from wyjscie_22_11_2025.txt
        // Small differences (< 1000) are acceptable due to rounding in different calculation orders
        assertTrue(kotlin.math.abs(payroll.itemsTaxTotal - 12390000) < 1000, 
            "Items tax should be approximately 12,390k, got ${payroll.itemsTaxTotal}")
        assertTrue(kotlin.math.abs(payroll.cashTaxTotal - 2109000) < 1000, 
            "Cash tax should be approximately 2,109k, got ${payroll.cashTaxTotal}")
        
        // Verify total distributions (with tolerance for rounding)
        val totalItemsDistributed = payroll.participants.values.sumOf { participant ->
            participant.itemsAfterTaxPerTabPerLocation.values.sumOf { tabs ->
                tabs.values.sum()
            }
        }
        assertTrue(kotlin.math.abs(totalItemsDistributed - 98168000) < 1000, 
            "Total items distributed should be approximately 98,168k, got $totalItemsDistributed")
        
        val totalCashDistributed = payroll.participants.values.sumOf { it.cashAfterTax }
        assertTrue(kotlin.math.abs(totalCashDistributed - 15808000) < 1000, 
            "Total cash distributed should be approximately 15,808k, got $totalCashDistributed")
        
        val totalItemReturns = payroll.participants.values.sumOf { it.returnsFromItems }
        assertTrue(kotlin.math.abs(totalItemReturns - 12261000) < 1000, 
            "Total item returns should be approximately 12,261k, got $totalItemReturns")
        
        val totalCashReturns = payroll.participants.values.sumOf { it.returnsFromCash }
        assertTrue(kotlin.math.abs(totalCashReturns - 1972000) < 1000, 
            "Total cash returns should be approximately 1,972k, got $totalCashReturns")
    }

    @Test
    fun testSpecificParticipantPayouts() {
        if (!isJvmPlatform()) return
        // Load and parse input
        val inputText = loadTestResource("/payroll/test_input.txt")
        val input = parsePayrollInput(inputText.lines())
        
        // Calculate payroll
        val payroll = calculatePayroll(input)
        
        // Verify specific participants (from wyjscie_22_11_2025.txt)
        val orzech = payroll.participants["orzech"]
        assertNotNull(orzech, "Orzech should be in payroll")
        assertEquals(1494000, orzech.cashAfterTax, "Orzech cash payout")
        assertEquals(861000, orzech.returnsFromItems + orzech.returnsFromCash, 
            "Orzech total returns")
        assertEquals(5.4, orzech.returnPoints, 0.0001, "Orzech return points")
        
        val karoll = payroll.participants["karoll"]
        assertNotNull(karoll, "Karoll should be in payroll")
        assertEquals(1388000, karoll.cashAfterTax, "Karoll cash payout")
        assertEquals(1410000, karoll.returnsFromItems + karoll.returnsFromCash, 
            "Karoll total returns")
        assertEquals(8.85, karoll.returnPoints, 0.0001, "Karoll return points")
        
        val smokq = payroll.participants["smokq"]
        assertNotNull(smokq, "Smokq should be in payroll")
        assertEquals(406000, smokq.cashAfterTax, "Smokq cash payout")
        assertEquals(972000, smokq.returnsFromItems + smokq.returnsFromCash, 
            "Smokq total returns")
        assertEquals(6.1, smokq.returnPoints, 0.0001, "Smokq return points")
        
        // Verify a recruiter
        val kacper17 = payroll.participants["kacper17"]
        assertNotNull(kacper17, "Kacper17 should be in payroll")
        assertEquals(0, kacper17.cashAfterTax, "Kacper17 cash (recruiter only)")
        assertEquals(956000, kacper17.returnsFromItems + kacper17.returnsFromCash, 
            "Kacper17 recruitment returns")
        assertEquals(6.0, kacper17.returnPoints, 0.0001, "Kacper17 return points")
    }

    @Test
    fun testLocationBreakdown() {
        if (!isJvmPlatform()) return
        // Load and parse input
        val inputText = loadTestResource("/payroll/test_input.txt")
        val input = parsePayrollInput(inputText.lines())
        
        // Calculate payroll
        val payroll = calculatePayroll(input)
        
        // Verify orzech's location breakdown
        val orzech = payroll.participants["orzech"]
        assertNotNull(orzech, "Orzech should be in payroll")
        
        val beachTab1 = orzech.itemsAfterTaxPerTabPerLocation["BEACH"]?.get("1")
        assertEquals(3176000, beachTab1, "Orzech BEACH-1 items")
        
        val beachTab2 = orzech.itemsAfterTaxPerTabPerLocation["BEACH"]?.get("2")
        assertEquals(750000, beachTab2, "Orzech BEACH-2 items")
        
        val beachTab3 = orzech.itemsAfterTaxPerTabPerLocation["BEACH"]?.get("3")
        assertEquals(368000, beachTab3, "Orzech BEACH-3 items")
        
        val fortTab1 = orzech.itemsAfterTaxPerTabPerLocation["FORT"]?.get("1")
        assertEquals(700000, fortTab1, "Orzech FORT-1 items")
        
        val fortTab2 = orzech.itemsAfterTaxPerTabPerLocation["FORT"]?.get("2")
        assertEquals(2090000, fortTab2, "Orzech FORT-2 items")
        
        val fortTab3 = orzech.itemsAfterTaxPerTabPerLocation["FORT"]?.get("3")
        assertEquals(1334000, fortTab3, "Orzech FORT-3 items")
    }

    @Test
    fun testParticipantCount() {
        if (!isJvmPlatform()) return
        // Load and parse input
        val inputText = loadTestResource("/payroll/test_input.txt")
        val input = parsePayrollInput(inputText.lines())
        
        // Calculate payroll
        val payroll = calculatePayroll(input)
        
        // Verify we have the expected number of participants
        // From the expected output, there are 42 participants
        assertEquals(42, payroll.participants.size, 
            "Should have 42 participants in total")
        
        // Verify all participants have some payout or return points
        payroll.participants.values.forEach { participant ->
            val hasItems = participant.itemsAfterTaxPerTabPerLocation.isNotEmpty()
            val hasCash = participant.cashAfterTax > 0
            val hasReturns = participant.returnsFromItems > 0 || participant.returnsFromCash > 0
            val hasReturnPoints = participant.returnPoints > 0
            
            assertTrue(hasItems || hasCash || hasReturns || hasReturnPoints,
                "Participant ${participant.name} should have some payout or return points")
        }
    }

    @Test
    fun testSimilarNamesDetection() {
        if (!isJvmPlatform()) return
        // Load and parse input
        val inputText = loadTestResource("/payroll/test_input.txt")
        val input = parsePayrollInput(inputText.lines())
        
        val participants = input.participants
        val names = participants.keys.toList()
        val similarPairs = mutableListOf<Pair<String, String>>()
        
        for (i in names.indices) {
            for (j in i + 1 until names.size) {
                val name1 = names[i].lowercase()
                val name2 = names[j].lowercase()
                val similarity = jaroWinklerSimilarity(name1, name2)
                if (similarity >= 0.75) {
                    similarPairs.add(name1 to name2)
                }
            }
        }
        
        // Verify known similar names are detected
        assertTrue(similarPairs.any { (a, b) -> 
            (a == "merlin" && b == "beslarin") || (a == "beslarin" && b == "merlin")
        }, "Should detect merlin/beslarin similarity")
        
        assertTrue(similarPairs.any { (a, b) -> 
            (a == "juri" && b == "juicycat") || (a == "juicycat" && b == "juri")
        }, "Should detect juri/juicycat similarity")
        
        assertTrue(similarPairs.any { (a, b) -> 
            (a == "sziba" && b == "soliba") || (a == "soliba" && b == "sziba")
        }, "Should detect sziba/soliba similarity")
    }

}
