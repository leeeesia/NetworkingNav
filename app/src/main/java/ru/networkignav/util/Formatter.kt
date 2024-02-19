package ru.networkignav.util

import android.app.DatePickerDialog
import android.content.Context
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

object Formatter {

    fun formatJobDate(input: String): String {
        val jobDate = ZonedDateTime.parse(input, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
            .withZoneSameInstant(ZoneId.systemDefault())
        return DateTimeFormatter.ofPattern("dd.MM.yyyy")
            .format(jobDate)
    }
    fun formatPostDate(input: String): String {
        val postDate = ZonedDateTime.parse(input, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
            .withZoneSameInstant(ZoneId.systemDefault())
        val fullDate = DateTimeFormatter.ofPattern("dd MMMM yyyy в HH:mm")
            .format(postDate)
        val formattedDate = DateTimeFormatter.ofPattern("d MMMM в HH:mm")
            .format(postDate)
        val formattedMinutes = DateTimeFormatter.ofPattern("в HH:mm")
            .format(postDate)
        val currentTime = LocalDateTime.now()
        when (currentTime.year - postDate.year) {
            0 -> {
                return when (currentTime.dayOfMonth - postDate.dayOfMonth) {
                    0 -> {
                        "Сегодня $formattedMinutes"
                    }

                    1 -> {
                        "Вчера $formattedMinutes"
                    }

                    else -> {
                        formattedDate
                    }
                }
            }

            else -> {
                return fullDate
            }

        }
    }

    fun formatEventDate(input: String): String {
        val eventDate = ZonedDateTime.parse(input, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        val fullDate = DateTimeFormatter.ofPattern("dd MMMM yyyy в HH:mm")
            .format(eventDate)
        val formattedDate = DateTimeFormatter.ofPattern("d MMMM в HH:mm")
            .format(eventDate)
        val currentTime = LocalDateTime.now()
        return when (currentTime.year - eventDate.year) {
            0 -> {
                formattedDate
            }

            else -> {
                return fullDate
            }

        }
    }

    fun showDatePicker(button: MaterialButton, context: Context) {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            context,
            { _, year, monthOfYear, dayOfMonth ->
                calendar.set(year, monthOfYear, dayOfMonth)
                val formattedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    .format(calendar.time)
                button.text = formattedDate
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }
}