// Core payroll calculation logic

@file:OptIn(kotlin.experimental.ExperimentalNativeApi::class)

const val PARTICIPANT_RETURN_BASE = 0.05
const val ORGANISER_CALLER_RETURN_BASE = 0.25

fun calculateTaxAndReturns(amount: Int): Triple<Int, Int, Int> {
    val afterTax = (amount * 0.8).toInt() / 1000 * 1000 // round down to thousands
    val returns = (amount * 0.1).toInt() / 1000 * 1000
    val tax = amount - afterTax - returns

    return Triple(afterTax, returns, tax)
}

fun determineReturnMultiplier(howManyParticipants: Int): Double {
    if (howManyParticipants == 1) return 1.0
    return ((howManyParticipants - 1) / 5 + 1).toDouble() // 1 for 1-5, 2 for 6-10, 3 for 11-15, etc.
}

fun calculateItemsAndCash(contentInputs: List<ContentInput>): List<Content> {
    return contentInputs.map { contentInput ->
        val hauls = contentInput.haulInputs.map { haulInput ->
            val (itemsAfterTax, returnsFromItems, itemsTax) = calculateTaxAndReturns(haulInput.itemsBeforeTax)
            val (cashAfterTax, returnsFromCash, cashTax) = calculateTaxAndReturns(haulInput.cashBeforeTax)

            Haul(
                itemsAfterTax,
                cashAfterTax,
                itemsTax,
                cashTax,
                returnsFromItems,
                returnsFromCash,
                haulInput.location,
                haulInput.tab,
                haulInput.hadOrganizer,
                haulInput.caller,
                haulInput.participants
            )
        }

        // Create Contents from ContentInputs
        // During this step, we should grant cash, items and return points to participants on a per-haul basis
        val content = contentInput.run {
            val allParticipantsCount = (listOfNotNull(organizer) + hauls.flatMap { haul ->
                listOfNotNull(haul.caller?.participant) + haul.participants.map { it.participant }
            }).distinctBy { it.name }.count()
            val returnMultiplier = if (organizer != null) determineReturnMultiplier(allParticipantsCount) else 0.0
            val participantReturnPerHaul = PARTICIPANT_RETURN_BASE * returnMultiplier / hauls.size.toDouble()
            val organizerCallerReturnPerHaul = ORGANISER_CALLER_RETURN_BASE * returnMultiplier / hauls.size.toDouble()

            hauls.forEach { haul ->
                val isCallerSameAsOrganizer = haul.caller?.participant?.name == organizer?.name

                val haulSharePoints =
                    (if (organizer != null && haul.hadOrganizer) 2 else 0) +
                            (haul.caller?.let { if (isCallerSameAsOrganizer) 0 else if (it.hasFullShare) 2 else 1 }
                                ?: 0) +
                            haul.participants.sumOf { (if (it.hasFullShare) 2 else 1).toInt() }
                val cashHaulShareUnit = (haul.cashAfterTax / haulSharePoints / 1000) * 1000
                val itemsHaulShareUnit = (haul.itemsAfterTax / haulSharePoints / 1000) * 1000

                val cashRemainder = haul.cashAfterTax - (cashHaulShareUnit * haulSharePoints)
                val itemsRemainder = haul.itemsAfterTax - (itemsHaulShareUnit * haulSharePoints)
                haul.cashTax += cashRemainder
                haul.itemsTax += itemsRemainder

                var itemsLeftToDistribute = haul.itemsAfterTax
                var cashLeftToDistribute = haul.cashAfterTax

                if (haul.hadOrganizer) {
                    organizer?.also {
                        val location = it.itemsAfterTaxPerTabPerLocation.getOrPut(haul.location) { mutableMapOf() }
                        location[haul.tab] = location.getOrPut(haul.tab) { 0 } + itemsHaulShareUnit * 2

                        it.cashAfterTax += cashHaulShareUnit * 2
                        itemsLeftToDistribute -= itemsHaulShareUnit * 2
                        cashLeftToDistribute -= cashHaulShareUnit * 2
                    }
                }

                if (!isCallerSameAsOrganizer) {
                    haul.caller?.also {
                        val participationShare = if (it.hasFullShare) 2 else 1
                        val location =
                            it.participant.itemsAfterTaxPerTabPerLocation.getOrPut(haul.location) { mutableMapOf() }
                        location[haul.tab] = location.getOrPut(haul.tab) { 0 } + itemsHaulShareUnit * participationShare

                        it.participant.cashAfterTax += cashHaulShareUnit * participationShare
                        itemsLeftToDistribute -= itemsHaulShareUnit * participationShare
                        cashLeftToDistribute -= cashHaulShareUnit * participationShare
                    }
                }

                if (haul.hadOrganizer) {
                    organizer?.let {
                        it.returnPoints += organizerCallerReturnPerHaul
                    }
                }
                haul.caller?.let {
                    it.participant.returnPoints += organizerCallerReturnPerHaul
                }

                haul.participants.forEach { haulParticipant ->
                    val participationShare = if (haulParticipant.hasFullShare) 2 else 1

                    val location =
                        haulParticipant.participant.itemsAfterTaxPerTabPerLocation.getOrPut(haul.location) { mutableMapOf() }
                    location[haul.tab] = location.getOrPut(haul.tab) { 0 } + itemsHaulShareUnit * participationShare

                    haulParticipant.participant.cashAfterTax += cashHaulShareUnit * participationShare
                    haulParticipant.participant.returnPoints += participantReturnPerHaul
                    itemsLeftToDistribute -= itemsHaulShareUnit * participationShare
                    cashLeftToDistribute -= cashHaulShareUnit * participationShare
                }

                assert(itemsLeftToDistribute == itemsRemainder)
                assert(cashLeftToDistribute == cashRemainder)
            }

            Content(
                id,
                itemsTaxTotal = hauls.sumOf { it.itemsTax },
                cashTaxTotal = hauls.sumOf { it.cashTax },
                returnsFromItemsTotal = hauls.sumOf { it.returnsFromItems },
                returnsFromCashTotal = hauls.sumOf { it.returnsFromCash },
                organizer,
                participants = hauls.flatMap { it.participants }.map { it.participant }.distinctBy { it })
        }

        content
    }
}

fun calculateCtas(ctaInputs: List<CtaInput>) {
    ctaInputs.forEach { ctaInput ->
        val allParticipantsCount = ctaInput.participants.size + 1 // 1 for the caller
        val returnMultiplier = determineReturnMultiplier(allParticipantsCount) * 2 // give out double returns for ctas
        val participantReturn = PARTICIPANT_RETURN_BASE * returnMultiplier
        val callerReturn = ORGANISER_CALLER_RETURN_BASE * returnMultiplier

        ctaInput.caller.returnPoints += callerReturn
        ctaInput.participants.forEach { participant ->
            participant.returnPoints += participantReturn
        }
    }
}

fun distributeReturns(
    participants: Participants, contents: List<Content>, recruitmentReturnPoints: Double
): Pair<Int, Int> {
    // check if all return points assigned are equal to the sum of recruitment return points and (2 * number of contents with organizers)
    val returnPointsTotal = participants.values.sumOf { it.returnPoints }

    // Sum the return from items and cash for all contents
    val allReturnsFromItems = contents.sumOf { it.returnsFromItemsTotal }
    val allReturnsFromCash = contents.sumOf { it.returnsFromCashTotal }
    // Calculate the value of one return point
    val valuePerReturnPointItems = allReturnsFromItems / returnPointsTotal
    val valuePerReturnPointCash = allReturnsFromCash / returnPointsTotal

    // Distribute the returns to each participant, rounding down to thousands.
    val (itemTaxFromReturnsRemainder, cashTaxFromReturnsRemainder) = participants.values.fold(0 to 0) { (itemRemainder, cashRemainder), participant ->
        val returnFromItemsToAdd = (participant.returnPoints * valuePerReturnPointItems).toInt() / 1000 * 1000
        val returnFromCashToAdd = (participant.returnPoints * valuePerReturnPointCash).toInt() / 1000 * 1000

        val itemDifference = (participant.returnPoints * valuePerReturnPointItems).toInt() - returnFromItemsToAdd
        val cashDifference = (participant.returnPoints * valuePerReturnPointCash).toInt() - returnFromCashToAdd

        participant.returnsFromItems += returnFromItemsToAdd
        participant.returnsFromCash += returnFromCashToAdd

        itemRemainder + itemDifference to cashRemainder + cashDifference
    }

    return itemTaxFromReturnsRemainder to cashTaxFromReturnsRemainder
}

fun calculatePayroll(input: Input): Payroll {
    val (contentInputs, ctaInputs, recruitmentInputs, participants) = input

    val contents = calculateItemsAndCash(contentInputs)
    calculateCtas(ctaInputs)

    val recruitmentReturnPoints = recruitmentInputs.sumOf { recruitmentInput ->
        val recruitmentPoints = recruitmentInput.points.toDouble()
        recruitmentInput.recruiter.returnPoints += recruitmentPoints // give the recruiter his recruitment return points
        recruitmentPoints
    }

    val (itemTaxFromReturnsRemainder, cashTaxFromReturnsRemainder) = distributeReturns(
        participants, contents, recruitmentReturnPoints
    )
    val itemsTaxTotal = contents.sumOf { it.itemsTaxTotal } + itemTaxFromReturnsRemainder
    val cashTaxTotal = contents.sumOf { it.cashTaxTotal } + cashTaxFromReturnsRemainder

    return Payroll(itemsTaxTotal, cashTaxTotal, participants)
}
