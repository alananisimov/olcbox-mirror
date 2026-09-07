package org.olcbox.app.data.model

import kotlin.math.abs

data class TrafficQuota(
    val usedBytes: Double,
    val availableBytes: Double,
    val totalBytes: Double,
    val usedLabel: String,
    val availableLabel: String
) {
    val remainingFraction: Float
        get() = (availableBytes / totalBytes).toFloat().coerceIn(0f, 1f)
}

fun parseTrafficQuota(used: String?, available: String?): TrafficQuota? {
    val usedParts = used
        ?.split('/', limit = 2)
        ?.map(String::trim)
        .orEmpty()
    val parsedUsed = usedParts.firstOrNull()?.let(::parseTrafficBytes)
    val totalFromUsed = usedParts.getOrNull(1)?.let(::parseTrafficBytes)
    val parsedAvailable = available?.let(::parseTrafficBytes)

    val total = when {
        totalFromUsed != null && totalFromUsed > 0.0 -> totalFromUsed
        parsedUsed != null && parsedAvailable != null -> parsedUsed + parsedAvailable
        else -> return null
    }
    if (!total.isFinite() || total <= 0.0) return null

    val resolvedUsed = (parsedUsed ?: (total - (parsedAvailable ?: return null)))
        .coerceIn(0.0, total)
    val resolvedAvailable = (parsedAvailable ?: (total - resolvedUsed))
        .coerceIn(0.0, total)

    return TrafficQuota(
        usedBytes = resolvedUsed,
        availableBytes = resolvedAvailable,
        totalBytes = total,
        usedLabel = usedParts.firstOrNull()
            ?.takeIf { it.isNotBlank() }
            ?: formatTrafficBytes(resolvedUsed),
        availableLabel = available
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?: formatTrafficBytes(resolvedAvailable)
    )
}

private fun parseTrafficBytes(value: String): Double? {
    val match = TRAFFIC_SIZE_PATTERN.matchEntire(value.trim()) ?: return null
    val amount = match.groupValues[1].replace(',', '.').toDoubleOrNull() ?: return null
    if (!amount.isFinite() || amount < 0.0) return null
    val multiplier = when (match.groupValues[2].lowercase()) {
        "", "b", "byte", "bytes" -> 1.0
        "k", "kb", "kib" -> KIB
        "m", "mb", "mib" -> MIB
        "g", "gb", "gib" -> GIB
        "t", "tb", "tib" -> TIB
        "p", "pb", "pib" -> PIB
        else -> return null
    }
    return amount * multiplier
}

private fun formatTrafficBytes(bytes: Double): String {
    val (amount, unit) = when {
        bytes >= PIB -> bytes / PIB to "PB"
        bytes >= TIB -> bytes / TIB to "TB"
        bytes >= GIB -> bytes / GIB to "GB"
        bytes >= MIB -> bytes / MIB to "MB"
        bytes >= KIB -> bytes / KIB to "KB"
        else -> bytes to "B"
    }
    val rounded = if (abs(amount - amount.toLong()) < 0.005) {
        amount.toLong().toString()
    } else {
        ((amount * 100).toLong() / 100.0).toString().trimEnd('0').trimEnd('.')
    }
    return "$rounded $unit"
}

private val TRAFFIC_SIZE_PATTERN = Regex("^([0-9]+(?:[.,][0-9]+)?)\\s*([a-zA-Z]*)$")
private const val KIB = 1024.0
private const val MIB = KIB * 1024.0
private const val GIB = MIB * 1024.0
private const val TIB = GIB * 1024.0
private const val PIB = TIB * 1024.0
