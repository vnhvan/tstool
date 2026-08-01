package com.offline.saveeditor.startdate

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object StartDateTime {
    val timeZone: TimeZone = TimeZone.getTimeZone("Asia/Bangkok")

    fun toEpochSeconds(year: Int, month: Int, day: Int): Long = Calendar.getInstance(timeZone).run {
        require(year in 1970..2100) { "Năm phải trong khoảng 1970–2100" }
        require(month in 1..12) { "Tháng phải trong khoảng 1–12" }
        clear()
        isLenient = false
        set(year, month - 1, day, 0, 0, 0)
        timeInMillis / 1000L
    }

    fun format(epochSeconds: Long): String = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale("vi", "VN")).apply {
        timeZone = StartDateTime.timeZone
    }.format(Date(epochSeconds * 1000L))
}
