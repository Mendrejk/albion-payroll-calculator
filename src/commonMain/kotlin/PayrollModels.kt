// Data models shared between CLI and GUI

data class Participant(
    val name: String,
    var returnPoints: Double = 0.0,
    var itemsAfterTaxPerTabPerLocation: MutableMap<String, MutableMap<String, Int>> = mutableMapOf(),
    var cashAfterTax: Int = 0,
    var returnsFromItems: Int = 0,
    var returnsFromCash: Int = 0
)

data class HaulParticipant(val participant: Participant, val hasFullShare: Boolean)

data class HaulInput(
    val itemsBeforeTax: Int,
    val cashBeforeTax: Int,
    val location: String,
    val tab: String,
    val hadOrganizer: Boolean,
    val caller: HaulParticipant?,
    val participants: Set<HaulParticipant>
)

data class ContentInput(
    val id: Int, val organizer: Participant?, val haulInputs: List<HaulInput>
)

data class CtaInput(
    val id: Int, val caller: Participant, val participants: Set<Participant>
)

data class RecruitmentInput(val recruiter: Participant, val points: Double)

typealias Participants = MutableMap<String, Participant>

data class Input(
    val contents: List<ContentInput>,
    val ctas: List<CtaInput>,
    val recruitments: List<RecruitmentInput>,
    val participants: Participants
)

data class Haul(
    val itemsAfterTax: Int,
    val cashAfterTax: Int,
    var itemsTax: Int,
    var cashTax: Int,
    val returnsFromItems: Int,
    val returnsFromCash: Int,
    val location: String,
    val tab: String,
    val hadOrganizer: Boolean,
    val caller: HaulParticipant?,
    val participants: Set<(HaulParticipant)>
)

data class Content(
    val id: Int,
    val itemsTaxTotal: Int,
    val cashTaxTotal: Int,
    val returnsFromItemsTotal: Int,
    val returnsFromCashTotal: Int,
    val organizer: Participant?,
    val participants: List<Participant>
)

data class Payroll(
    val itemsTaxTotal: Int, val cashTaxTotal: Int, val participants: Participants
)
