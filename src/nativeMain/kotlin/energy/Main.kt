package energy

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import okio.FileSystem
import okio.Path.Companion.toPath
import kotlin.toString

fun getCurrentFormattedDate(): String {
    val currentDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    val day = currentDate.dayOfMonth.toString().padStart(2, '0')
    val month = currentDate.monthNumber.toString().padStart(2, '0')
    val year = currentDate.year.toString()
    return "${day}_${month}_${year}"
}

data class EnergyResult(
    val balanceMap: Map<String, Int>,
    val withdrawalCountMap: Map<String, Int>
)

fun computeEnergyBalance(inputLines: List<String>): EnergyResult {
    val balanceMap = mutableMapOf<String, Int>()
    val withdrawalCountMap = mutableMapOf<String, Int>()
    val regex = Regex("\"([^\"]*)\"")
    inputLines.forEach { line ->
        if (line.isBlank()) return@forEach
        val fields = regex.findAll(line).map { it.groupValues[1] }.toList()
        // Expecting 4 fields: Date, Player, Reason, Amount; skip header lines.
        if (fields.size != 4 || fields[0] == "Data") return@forEach
        val player = fields[1]
        val amount = fields[3].toIntOrNull() ?: 0

        // If the player makes a withdrawal of exactly 50 (territory attack), count it separately
        if (amount == -50) {
            val count = withdrawalCountMap.getOrPut(player) { 0 }
            withdrawalCountMap[player] = count + 1
        } else
        {
            // Update balance based on the transaction.
            val currentBalance = balanceMap.getOrPut(player) { 0 }
            balanceMap[player] = currentBalance + amount
        }
    }
    return EnergyResult(balanceMap, withdrawalCountMap)
}

fun runEnergyBalance() {
    val formattedDate = getCurrentFormattedDate()
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    val year = now.year.toString()
    val month = now.monthNumber.toString().padStart(2, '0')
    val inputFilePath = "energia/${year}/${month}/input/energia_${formattedDate}.txt".toPath()
    val outputFilePath = "energia/${year}/${month}/output/energy_balance_${formattedDate}.txt".toPath()

    val inputLines = mutableListOf<String>()
    FileSystem.SYSTEM.read(inputFilePath) {
        while (true) {
            val line = readUtf8Line() ?: break
            inputLines.add(line)
        }
    }

    val result = computeEnergyBalance(inputLines)
    val outputContent = buildString {
        appendLine("Energy Balance for each player:")
        result.balanceMap.forEach { (player, balance) ->
            if (result.withdrawalCountMap.containsKey(player)) {
                appendLine("$player: $balance (-50 x ${result.withdrawalCountMap[player]})")
            } else {
                appendLine("$player: $balance")
            }
        }
    }

    FileSystem.SYSTEM.write(outputFilePath) {
        writeUtf8(outputContent)
    }

    println("Energy balance computed and written to: energia/out/energy_balance_${formattedDate}.txt")
}
