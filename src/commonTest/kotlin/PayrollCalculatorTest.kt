import kotlin.test.*

class PayrollCalculatorTest {

    @Test
    fun testCalculateTaxAndReturns() {
        // Test basic tax calculation: 80% after tax, 10% returns, 10% tax
        val (afterTax, returns, tax) = calculateTaxAndReturns(10000)
        
        assertEquals(8000, afterTax, "After tax should be 80% rounded down to thousands")
        assertEquals(1000, returns, "Returns should be 10% rounded down to thousands")
        assertEquals(1000, tax, "Tax should be the remainder")
    }

    @Test
    fun testCalculateTaxAndReturnsWithRounding() {
        // Test rounding behavior
        val (afterTax, returns, tax) = calculateTaxAndReturns(12345)
        
        assertEquals(9000, afterTax, "Should round down to thousands: 12345 * 0.8 = 9876 -> 9000")
        assertEquals(1000, returns, "Should round down to thousands: 12345 * 0.1 = 1234.5 -> 1000")
        assertEquals(2345, tax, "Tax should be remainder: 12345 - 9000 - 1000 = 2345")
    }

    @Test
    fun testDetermineReturnMultiplier() {
        assertEquals(1.0, determineReturnMultiplier(1), "1 participant -> multiplier 1")
        assertEquals(1.0, determineReturnMultiplier(3), "3 participants -> multiplier 1")
        assertEquals(1.0, determineReturnMultiplier(5), "5 participants -> multiplier 1")
        assertEquals(2.0, determineReturnMultiplier(6), "6 participants -> multiplier 2")
        assertEquals(2.0, determineReturnMultiplier(10), "10 participants -> multiplier 2")
        assertEquals(3.0, determineReturnMultiplier(11), "11 participants -> multiplier 3")
        assertEquals(3.0, determineReturnMultiplier(15), "15 participants -> multiplier 3")
        assertEquals(4.0, determineReturnMultiplier(16), "16 participants -> multiplier 4")
    }

    @Test
    fun testParseParticipant() {
        val (name1, hasFullShare1) = parseParticipant("player1")
        assertEquals("player1", name1)
        assertTrue(hasFullShare1, "Regular participant should have full share")

        val (name2, hasFullShare2) = parseParticipant("player2(50%)")
        assertEquals("player2", name2)
        assertFalse(hasFullShare2, "Participant with (50%) should not have full share")

        val (name3, hasFullShare3) = parseParticipant("  player3  ")
        assertEquals("player3", name3)
        assertTrue(hasFullShare3, "Should trim whitespace")

        val (name4, hasFullShare4) = parseParticipant("  player4 (50%)  ")
        assertEquals("player4", name4)
        assertFalse(hasFullShare4, "Should handle whitespace with (50%)")
    }

    @Test
    fun testSimplePayrollCalculation() {
        // Create a simple test case: 1 content, 1 haul, 2 participants, no organizer
        val participants = mutableMapOf<String, Participant>()
        val player1 = Participant("player1")
        val player2 = Participant("player2")
        participants["player1"] = player1
        participants["player2"] = player2

        val haulInput = HaulInput(
            itemsBeforeTax = 10000,
            cashBeforeTax = 5000,
            location = "TEST",
            tab = "1",
            hadOrganizer = false,
            caller = null,
            participants = setOf(
                HaulParticipant(player1, true),
                HaulParticipant(player2, true)
            )
        )

        val contentInput = ContentInput(
            id = 1,
            organizer = null,
            haulInputs = listOf(haulInput)
        )

        val input = Input(
            contents = listOf(contentInput),
            ctas = emptyList(),
            recruitments = emptyList(),
            participants = participants
        )

        val payroll = calculatePayroll(input)

        // Verify tax calculations
        assertTrue(payroll.itemsTaxTotal > 0, "Should have items tax")
        assertTrue(payroll.cashTaxTotal > 0, "Should have cash tax")

        // Verify both participants got paid
        assertTrue(player1.cashAfterTax > 0, "Player 1 should receive cash")
        assertTrue(player2.cashAfterTax > 0, "Player 2 should receive cash")
        assertTrue(player1.itemsAfterTaxPerTabPerLocation.isNotEmpty(), "Player 1 should receive items")
        assertTrue(player2.itemsAfterTaxPerTabPerLocation.isNotEmpty(), "Player 2 should receive items")

        // Verify equal split (no organizer, both full share)
        assertEquals(player1.cashAfterTax, player2.cashAfterTax, "Equal participants should get equal cash")
    }

    @Test
    fun testPayrollWithOrganizer() {
        val participants = mutableMapOf<String, Participant>()
        val organizer = Participant("organizer")
        val player1 = Participant("player1")
        participants["organizer"] = organizer
        participants["player1"] = player1

        val haulInput = HaulInput(
            itemsBeforeTax = 10000,
            cashBeforeTax = 5000,
            location = "TEST",
            tab = "1",
            hadOrganizer = true,
            caller = null,
            participants = setOf(HaulParticipant(player1, true))
        )

        val contentInput = ContentInput(
            id = 1,
            organizer = organizer,
            haulInputs = listOf(haulInput)
        )

        val input = Input(
            contents = listOf(contentInput),
            ctas = emptyList(),
            recruitments = emptyList(),
            participants = participants
        )

        val payroll = calculatePayroll(input)

        // Organizer should get return points
        assertTrue(organizer.returnPoints > 0, "Organizer should have return points")
        
        // Organizer should get 2 shares, participant should get 2 shares
        assertTrue(organizer.cashAfterTax > 0, "Organizer should receive cash")
        assertTrue(player1.cashAfterTax > 0, "Player should receive cash")
        
        // They should get equal amounts (both 2 shares)
        assertEquals(organizer.cashAfterTax, player1.cashAfterTax, "Organizer and participant should get equal shares")
    }

    @Test
    fun testPayrollWithReducedShare() {
        val participants = mutableMapOf<String, Participant>()
        val fullPlayer = Participant("fullplayer")
        val halfPlayer = Participant("halfplayer")
        participants["fullplayer"] = fullPlayer
        participants["halfplayer"] = halfPlayer

        val haulInput = HaulInput(
            itemsBeforeTax = 12000,
            cashBeforeTax = 6000,
            location = "TEST",
            tab = "1",
            hadOrganizer = false,
            caller = null,
            participants = setOf(
                HaulParticipant(fullPlayer, true),  // 2 shares
                HaulParticipant(halfPlayer, false)  // 1 share
            )
        )

        val contentInput = ContentInput(
            id = 1,
            organizer = null,
            haulInputs = listOf(haulInput)
        )

        val input = Input(
            contents = listOf(contentInput),
            ctas = emptyList(),
            recruitments = emptyList(),
            participants = participants
        )

        val payroll = calculatePayroll(input)

        // Full player should get 2x what half player gets
        assertTrue(fullPlayer.cashAfterTax > halfPlayer.cashAfterTax, 
            "Full share player should get more than half share player")
        
        // Should be approximately 2:1 ratio (allowing for rounding)
        val ratio = fullPlayer.cashAfterTax.toDouble() / halfPlayer.cashAfterTax.toDouble()
        assertTrue(ratio >= 1.8 && ratio <= 2.2, 
            "Ratio should be approximately 2:1, got $ratio")
    }

    @Test
    fun testCTACalculation() {
        val participants = mutableMapOf<String, Participant>()
        val caller = Participant("caller")
        val participant1 = Participant("participant1")
        val participant2 = Participant("participant2")
        participants["caller"] = caller
        participants["participant1"] = participant1
        participants["participant2"] = participant2

        val ctaInput = CtaInput(
            id = 1,
            caller = caller,
            participants = setOf(participant1, participant2)
        )

        val input = Input(
            contents = emptyList(),
            ctas = listOf(ctaInput),
            recruitments = emptyList(),
            participants = participants
        )

        val payroll = calculatePayroll(input)

        // Caller should get more return points than participants
        assertTrue(caller.returnPoints > participant1.returnPoints, 
            "Caller should get more return points than participants")
        assertTrue(caller.returnPoints > participant2.returnPoints, 
            "Caller should get more return points than participants")
        
        // Participants should get equal return points
        assertEquals(participant1.returnPoints, participant2.returnPoints, 
            "CTA participants should get equal return points")
        
        // Verify the multiplier is doubled for CTA (2x normal)
        val allParticipantsCount = 3 // caller + 2 participants
        val expectedMultiplier = determineReturnMultiplier(allParticipantsCount) * 2
        val expectedCallerReturn = ORGANISER_CALLER_RETURN_BASE * expectedMultiplier
        val expectedParticipantReturn = PARTICIPANT_RETURN_BASE * expectedMultiplier
        
        assertEquals(expectedCallerReturn, caller.returnPoints, 0.0001, 
            "Caller should get correct return points")
        assertEquals(expectedParticipantReturn, participant1.returnPoints, 0.0001, 
            "Participant should get correct return points")
    }

    @Test
    fun testRecruitmentPoints() {
        val participants = mutableMapOf<String, Participant>()
        val recruiter = Participant("recruiter")
        participants["recruiter"] = recruiter

        val recruitmentInput = RecruitmentInput(
            recruiter = recruiter,
            points = 5.0
        )

        val input = Input(
            contents = emptyList(),
            ctas = emptyList(),
            recruitments = listOf(recruitmentInput),
            participants = participants
        )

        val payroll = calculatePayroll(input)

        assertEquals(5.0, recruiter.returnPoints, 0.0001, 
            "Recruiter should have exactly the assigned return points")
    }

    @Test
    fun testFormatNumberWithSpaces() {
        assertEquals("1 000", formatNumberWithSpaces(1000))
        assertEquals("10 000", formatNumberWithSpaces(10000))
        assertEquals("100 000", formatNumberWithSpaces(100000))
        assertEquals("1 000 000", formatNumberWithSpaces(1000000))
        assertEquals("123", formatNumberWithSpaces(123))
        assertEquals("1 234", formatNumberWithSpaces(1234))
        assertEquals("12 345", formatNumberWithSpaces(12345))
    }

    @Test
    fun testFormatNumberWithSpacesAndK() {
        assertEquals("1k", formatNumberWithSpacesAndK(1000))
        assertEquals("10k", formatNumberWithSpacesAndK(10000))
        assertEquals("100k", formatNumberWithSpacesAndK(100000))
        assertEquals("1 000k", formatNumberWithSpacesAndK(1000000))
        assertEquals("1 234k", formatNumberWithSpacesAndK(1234000))
    }
}
