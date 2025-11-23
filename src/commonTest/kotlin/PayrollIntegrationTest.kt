import kotlin.test.*

/**
 * Integration tests using real payroll data to ensure calculations remain consistent.
 * These tests verify the entire calculation pipeline with realistic scenarios.
 */
class PayrollIntegrationTest {

    @Test
    fun testRealWorldScenario() {
        // Simulate a real payroll scenario based on actual data
        val participants = mutableMapOf<String, Participant>()
        
        // Create participants
        val orzech = Participant("orzech")
        val yuegre = Participant("yuegre")
        val arkadius = Participant("arkadius")
        val koniu = Participant("koniu")
        val orafsson = Participant("orafsson")
        val rainbowek = Participant("rainbowek")
        
        participants["orzech"] = orzech
        participants["yuegre"] = yuegre
        participants["arkadius"] = arkadius
        participants["koniu"] = koniu
        participants["orafsson"] = orafsson
        participants["rainbowek"] = rainbowek

        // Content 1: No organizer, 2 hauls
        val haul1 = HaulInput(
            itemsBeforeTax = 3770000,
            cashBeforeTax = 0,
            location = "BEACH",
            tab = "1",
            hadOrganizer = false,
            caller = null,
            participants = setOf(
                HaulParticipant(orzech, true),
                HaulParticipant(yuegre, true),
                HaulParticipant(arkadius, true),
                HaulParticipant(koniu, true),
                HaulParticipant(orafsson, true),
                HaulParticipant(rainbowek, true)
            )
        )

        val haul2 = HaulInput(
            itemsBeforeTax = 4970000,
            cashBeforeTax = 0,
            location = "BEACH",
            tab = "1",
            hadOrganizer = false,
            caller = null,
            participants = setOf(
                HaulParticipant(orzech, true),
                HaulParticipant(yuegre, true),
                HaulParticipant(arkadius, true),
                HaulParticipant(koniu, true),
                HaulParticipant(orafsson, true),
                HaulParticipant(rainbowek, true)
            )
        )

        val content1 = ContentInput(
            id = 1,
            organizer = null,
            haulInputs = listOf(haul1, haul2)
        )

        val input = Input(
            contents = listOf(content1),
            ctas = emptyList(),
            recruitments = emptyList(),
            participants = participants
        )

        val payroll = calculatePayroll(input)

        // Verify basic properties
        assertTrue(payroll.itemsTaxTotal > 0, "Should have items tax")
        assertEquals(0, payroll.cashTaxTotal, "Should have no cash tax (no cash in hauls)")

        // All participants should receive equal amounts (no organizer, all full share)
        val itemAmounts = participants.values.map { 
            it.itemsAfterTaxPerTabPerLocation["BEACH"]?.get("1") ?: 0 
        }
        
        assertTrue(itemAmounts.all { it > 0 }, "All participants should receive items")
        assertTrue(itemAmounts.distinct().size == 1, "All participants should receive equal amounts")

        // Verify total distribution
        val totalDistributed = itemAmounts.sum()
        val totalBeforeTax = 3770000 + 4970000
        val expectedAfterTax = (totalBeforeTax * 0.8).toInt() / 1000 * 1000
        
        assertTrue(totalDistributed <= expectedAfterTax, 
            "Total distributed should not exceed after-tax amount")
    }

    @Test
    fun testComplexScenarioWithOrganizerAndCaller() {
        val participants = mutableMapOf<String, Participant>()
        
        val organizer = Participant("smokq")
        val caller = Participant("karoll")
        val player1 = Participant("orzech")
        val player2 = Participant("piston")
        
        participants["smokq"] = organizer
        participants["karoll"] = caller
        participants["orzech"] = player1
        participants["piston"] = player2

        // Content with organizer and caller
        val haul = HaulInput(
            itemsBeforeTax = 18060000,
            cashBeforeTax = 3670000,
            location = "BEACH",
            tab = "1",
            hadOrganizer = true,
            caller = HaulParticipant(caller, true),
            participants = setOf(
                HaulParticipant(player1, true),
                HaulParticipant(player2, true)
            )
        )

        val content = ContentInput(
            id = 2,
            organizer = organizer,
            haulInputs = listOf(haul)
        )

        val input = Input(
            contents = listOf(content),
            ctas = emptyList(),
            recruitments = emptyList(),
            participants = participants
        )

        val payroll = calculatePayroll(input)

        // Organizer should get return points
        assertTrue(organizer.returnPoints > 0, "Organizer should have return points")
        
        // Caller should get return points
        assertTrue(caller.returnPoints > 0, "Caller should have return points")
        
        // Regular participants should get return points
        assertTrue(player1.returnPoints > 0, "Participant should have return points")
        assertTrue(player2.returnPoints > 0, "Participant should have return points")

        // Organizer gets 2 shares
        assertTrue(organizer.cashAfterTax > 0, "Organizer should receive cash")
        assertTrue(organizer.itemsAfterTaxPerTabPerLocation.isNotEmpty(), "Organizer should receive items")

        // Caller gets 2 shares
        assertTrue(caller.cashAfterTax > 0, "Caller should receive cash")
        assertTrue(caller.itemsAfterTaxPerTabPerLocation.isNotEmpty(), "Caller should receive items")

        // Regular participants get 2 shares each
        assertTrue(player1.cashAfterTax > 0, "Participant should receive cash")
        assertTrue(player2.cashAfterTax > 0, "Participant should receive cash")

        // All should get equal amounts (all have 2 shares)
        assertEquals(organizer.cashAfterTax, caller.cashAfterTax, 
            "Organizer and caller should get equal cash")
        assertEquals(caller.cashAfterTax, player1.cashAfterTax, 
            "Caller and participant should get equal cash")
        assertEquals(player1.cashAfterTax, player2.cashAfterTax, 
            "Participants should get equal cash")
    }

    @Test
    fun testMultipleContentsAndCTAs() {
        val participants = mutableMapOf<String, Participant>()
        
        val player1 = Participant("player1")
        val player2 = Participant("player2")
        val player3 = Participant("player3")
        
        participants["player1"] = player1
        participants["player2"] = player2
        participants["player3"] = player3

        // Content 1
        val content1 = ContentInput(
            id = 1,
            organizer = null,
            haulInputs = listOf(
                HaulInput(
                    itemsBeforeTax = 10000000,
                    cashBeforeTax = 5000000,
                    location = "LOC1",
                    tab = "1",
                    hadOrganizer = false,
                    caller = null,
                    participants = setOf(
                        HaulParticipant(player1, true),
                        HaulParticipant(player2, true)
                    )
                )
            )
        )

        // Content 2
        val content2 = ContentInput(
            id = 2,
            organizer = player3,
            haulInputs = listOf(
                HaulInput(
                    itemsBeforeTax = 8000000,
                    cashBeforeTax = 4000000,
                    location = "LOC2",
                    tab = "1",
                    hadOrganizer = true,
                    caller = null,
                    participants = setOf(
                        HaulParticipant(player1, true)
                    )
                )
            )
        )

        // CTA
        val cta = CtaInput(
            id = 1,
            caller = player2,
            participants = setOf(player1, player3)
        )

        val input = Input(
            contents = listOf(content1, content2),
            ctas = listOf(cta),
            recruitments = emptyList(),
            participants = participants
        )

        val payroll = calculatePayroll(input)

        // All players should have some payout
        assertTrue(player1.cashAfterTax > 0 || player1.itemsAfterTaxPerTabPerLocation.isNotEmpty(), 
            "Player 1 should receive something")
        assertTrue(player2.cashAfterTax > 0 || player2.itemsAfterTaxPerTabPerLocation.isNotEmpty(), 
            "Player 2 should receive something")
        assertTrue(player3.cashAfterTax > 0 || player3.itemsAfterTaxPerTabPerLocation.isNotEmpty(), 
            "Player 3 should receive something")

        // All players should have return points (from content organizer and CTA)
        assertTrue(player1.returnPoints > 0, "Player 1 should have return points")
        assertTrue(player2.returnPoints > 0, "Player 2 should have return points (CTA caller)")
        assertTrue(player3.returnPoints > 0, "Player 3 should have return points (organizer + CTA)")

        // Verify tax collection
        assertTrue(payroll.itemsTaxTotal > 0, "Should collect items tax")
        assertTrue(payroll.cashTaxTotal > 0, "Should collect cash tax")
    }

    @Test
    fun testRecruitmentIntegration() {
        val participants = mutableMapOf<String, Participant>()
        
        val recruiter1 = Participant("recruiter1")
        val recruiter2 = Participant("recruiter2")
        val player = Participant("player")
        
        participants["recruiter1"] = recruiter1
        participants["recruiter2"] = recruiter2
        participants["player"] = player

        // Simple content to generate return pool
        val content = ContentInput(
            id = 1,
            organizer = null,
            haulInputs = listOf(
                HaulInput(
                    itemsBeforeTax = 10000000,
                    cashBeforeTax = 5000000,
                    location = "TEST",
                    tab = "1",
                    hadOrganizer = false,
                    caller = null,
                    participants = setOf(HaulParticipant(player, true))
                )
            )
        )

        // Recruitments
        val recruitment1 = RecruitmentInput(recruiter1, 6.0)
        val recruitment2 = RecruitmentInput(recruiter2, 3.5)

        val input = Input(
            contents = listOf(content),
            ctas = emptyList(),
            recruitments = listOf(recruitment1, recruitment2),
            participants = participants
        )

        val payroll = calculatePayroll(input)

        // Recruiters should have their assigned points
        assertEquals(6.0, recruiter1.returnPoints, 0.0001, "Recruiter 1 should have 6 points")
        assertEquals(3.5, recruiter2.returnPoints, 0.0001, "Recruiter 2 should have 3.5 points")

        // Recruiters should receive returns based on their points
        assertTrue(recruiter1.returnsFromItems > 0 || recruiter1.returnsFromCash > 0, 
            "Recruiter 1 should receive returns")
        assertTrue(recruiter2.returnsFromItems > 0 || recruiter2.returnsFromCash > 0, 
            "Recruiter 2 should receive returns")

        // Recruiter 1 should get more than recruiter 2 (6 points vs 3.5 points)
        val total1 = recruiter1.returnsFromItems + recruiter1.returnsFromCash
        val total2 = recruiter2.returnsFromItems + recruiter2.returnsFromCash
        assertTrue(total1 > total2, "Recruiter with more points should get more returns")
    }
}
