package ru.networkignav.util

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import com.google.android.material.button.MaterialButton
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

object Formatter {
    fun formatCount(count: Int): String? {
        when (count) {
            in 0..999 -> return count.toString()
            in 1000..9999 -> {
                val df = DecimalFormat("#.#")
                df.roundingMode = RoundingMode.DOWN
                val result = df.format(count / 1000.0)
                return result.toString() + "K"
            }

            in 10000..999999 -> {
                val df = DecimalFormat("#")
                df.roundingMode = RoundingMode.DOWN
                val result = df.format(count / 1000.0)
                return result.toString() + "K"
            }

            in 1000000..999999999 -> {
                val df = DecimalFormat("#.#")
                df.roundingMode = RoundingMode.DOWN
                val result = df.format(count / 1000000.0)
                return result.toString() + "M"
            }
        }
        return null
    }

    fun formatJobDate(input: String): String {
        val jobDate = ZonedDateTime.parse(input, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
            .withZoneSameInstant(ZoneId.systemDefault())
        return DateTimeFormatter.ofPattern("dd.MM.yyyy")
            .format(jobDate)
    }

    fun formatJobDateForEdit(input: String): String {
        val jobDate = ZonedDateTime.parse(input, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
            .withZoneSameInstant(ZoneId.systemDefault())
        return DateTimeFormatter.ofPattern("yyyy-MM-dd")
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

    fun showTimePicker(button: MaterialButton, context: Context) {
        val calendar = Calendar.getInstance()
        val timePickerDialog = TimePickerDialog.OnTimeSetListener { _, hour, minute ->
            calendar[Calendar.HOUR_OF_DAY] = hour
            calendar[Calendar.MINUTE] = minute
            val formattedTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(calendar.time)
            button.text = formattedTime
        }
        TimePickerDialog(
            context, timePickerDialog, calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE), true
        ).show()
    }
}