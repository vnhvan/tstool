package com.offline.saveeditor.edit

import kotlin.math.abs

enum class EditRiskLevel { NORMAL, CAUTION, HIGH }

data class EditRisk(
    val level: EditRiskLevel,
    val message: String,
    val absoluteDelta: Long,
    val relativeMultiplier: Double?,
)

object EditRiskAssessor {
    fun assess(oldValue: Long, newValue: Long, rule: EditRule): EditRisk {
        val delta = safeAbsDifference(oldValue, newValue)
        val multiplier = when {
            oldValue == 0L && newValue == 0L -> 1.0
            oldValue == 0L -> null
            else -> abs(newValue.toDouble() / oldValue.toDouble())
        }
        val range = if (rule.minValue != null && rule.maxValue != null) rule.maxValue - rule.minValue else null
        val highByRange = range != null && range > 0 && delta.toDouble() / range.toDouble() >= 0.80
        val highByMultiplier = multiplier != null && (multiplier >= 20.0 || multiplier <= 0.05)
        val cautionByRange = range != null && range > 0 && delta.toDouble() / range.toDouble() >= 0.35
        val cautionByMultiplier = multiplier != null && (multiplier >= 5.0 || multiplier <= 0.20)
        return when {
            highByRange || highByMultiplier || (oldValue == 0L && newValue != 0L) -> EditRisk(EditRiskLevel.HIGH, "Mức thay đổi rất lớn; hãy kiểm tra kỹ diff trước khi xuất.", delta, multiplier)
            cautionByRange || cautionByMultiplier -> EditRisk(EditRiskLevel.CAUTION, "Giá trị thay đổi đáng kể so với hiện tại.", delta, multiplier)
            else -> EditRisk(EditRiskLevel.NORMAL, "Mức thay đổi nằm trong phạm vi thông thường.", delta, multiplier)
        }
    }

    private fun safeAbsDifference(a: Long, b: Long): Long {
        val d = a.toBigInteger().subtract(b.toBigInteger()).abs()
        return d.min(Long.MAX_VALUE.toBigInteger()).toLong()
    }
}
