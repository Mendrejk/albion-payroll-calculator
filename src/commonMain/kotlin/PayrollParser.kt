// Payroll input file parser - platform-independent

fun parsePayrollInput(lines: List<String>): Input {
    val filteredLines = lines.filter { it.isNotBlank() && !it.startsWith("#") }
    
    val (contentsLines, ctaLines, recruitmentsLines) = filteredLines.fold(mutableListOf<MutableList<String>>()) { acc, line ->
        if (line.startsWith("KONTENTY:") || line.startsWith("CTA:") || line.startsWith("REKRUTACJA:")) {
            acc.add(mutableListOf())
        } else {
            acc.lastOrNull()?.add(line)
        }
        acc
    }

    val groupedContentsLines = contentsLines.fold(mutableListOf<MutableList<String>>()) { acc, line ->
        if (line.matches(Regex("\\d+:.*"))) {
            acc.add(mutableListOf(line))
        } else {
            acc.lastOrNull()?.add(line)
        }
        acc
    }

    val participants = mutableMapOf<String, Participant>()

    val contentInputs = groupedContentsLines.map { contentLines ->
        val (contentId, organizer) = run {
            val contentHeader = contentLines.first().split(":").map { it.trim() }
            val contentId = contentHeader.getOrNull(0)?.toIntOrNull() ?: 0
            val organizer = contentHeader.getOrNull(1)?.takeIf { it.isNotEmpty() }?.let {
                val lowercaseOrganizer = it.lowercase()
                participants.getOrPut(lowercaseOrganizer) { Participant(lowercaseOrganizer) }
            }
            Pair(contentId, organizer)
        }

        val haulsLines = contentLines.drop(1)

        val haulInputs = haulsLines.mapNotNull { haulLine ->
            haulLine.split(":").map { it.trim() }.takeIf { it.size == 2 }?.let { (_, haulData) ->
                haulData.split(",").map { it.trim() }.takeIf { it.size >= 7 }?.let { haulParts ->
                    val itemsBeforeTax = haulParts[0].toInt() * 1000
                    val cashBeforeTax = haulParts[1].toIntOrNull()?.times(1000) ?: 0
                    val location = haulParts[2]
                    val tab = haulParts[3]
                    val hadOrganizer = haulParts[4].lowercase() == "tak"
                    val caller = haulParts[5].takeIf { it.isNotEmpty() }?.let {
                        val (callerName, hasFullShare) = parseParticipant(it.lowercase())
                        val caller = participants.getOrPut(callerName) { Participant(callerName) }
                        HaulParticipant(caller, hasFullShare)
                    }
                    val haulParticipants = HashSet(haulParts.drop(6).map { participantString ->
                        val (participantName, hasFullShare) = parseParticipant(participantString.lowercase())
                        val participant = participants.getOrPut(participantName) { Participant(participantName) }
                        HaulParticipant(participant, hasFullShare)
                    })
                    HaulInput(itemsBeforeTax, cashBeforeTax, location, tab, hadOrganizer, caller, haulParticipants)
                }
            }
        }

        ContentInput(contentId, organizer, haulInputs)
    }

    val ctaInputs = ctaLines.mapNotNull { ctaLine ->
        ctaLine.split(":").map { it.trim() }.takeIf { it.size == 2 }?.let { (ctaId, ctaData) ->
            ctaData.split("-").map { it.trim() }.takeIf { it.size == 2 }?.let { (callerName, ctaParticipantsData) ->
                val caller = participants.getOrPut(callerName.lowercase()) { Participant(callerName.lowercase()) }
                val ctaParticipants = HashSet(
                    ctaParticipantsData.split(",").map { it.trim().lowercase() }.map { ctaParticipantName ->
                        participants.getOrPut(ctaParticipantName) { Participant(ctaParticipantName) }
                    })
                CtaInput(ctaId.toIntOrNull() ?: 0, caller, ctaParticipants)
            }
        }
    }

    val recruitmentInputs = recruitmentsLines.mapNotNull { recruitmentLine ->
        recruitmentLine.split(":").map { it.trim() }.takeIf { it.size == 2 }
            ?.let { (recruiterName, recruitmentPoints) ->
                val lowercaseRecruiterName = recruiterName.lowercase()
                val recruiter = participants.getOrPut(lowercaseRecruiterName) { Participant(lowercaseRecruiterName) }
                RecruitmentInput(recruiter, recruitmentPoints.toDouble())
            }
    }

    return Input(contentInputs, ctaInputs, recruitmentInputs, participants)
}
