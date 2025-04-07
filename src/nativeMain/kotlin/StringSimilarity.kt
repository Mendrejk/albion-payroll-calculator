fun jaroWinklerSimilarity(s1: String, s2: String): Double {
    if (s1 == s2) return 1.0
    if (s1.isEmpty() || s2.isEmpty()) return 0.0

    val s1Len = s1.length
    val s2Len = s2.length
    val matchDistance = (maxOf(s1Len, s2Len) / 2) - 1

    val s1Matches = BooleanArray(s1Len)
    val s2Matches = BooleanArray(s2Len)

    var matches = 0
    for (i in 0 until s1Len) {
        val start = maxOf(0, i - matchDistance)
        val end = minOf(i + matchDistance + 1, s2Len)
        for (j in start until end) {
            if (s2Matches[j]) continue
            if (s1[i] != s2[j]) continue
            s1Matches[i] = true
            s2Matches[j] = true
            matches++
            break
        }
    }
    if (matches == 0) return 0.0

    var transpositions = 0
    var k = 0
    for (i in 0 until s1Len) {
        if (!s1Matches[i]) continue
        while (!s2Matches[k]) k++
        if (s1[i] != s2[k]) transpositions++
        k++
    }
    val t = transpositions / 2.0

    val jaro = ((matches / s1Len.toDouble()) +
            (matches / s2Len.toDouble()) +
            ((matches - t) / matches.toDouble())) / 3.0

    // Calculate common prefix length (up to 4 characters)
    var prefix = 0
    for (i in 0 until minOf(4, s1Len, s2Len)) {
        if (s1[i] == s2[i]) prefix++ else break
    }
    val scalingFactor = 0.1
    return jaro + prefix * scalingFactor * (1 - jaro)
}