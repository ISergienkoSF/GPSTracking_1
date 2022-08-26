package com.viol4tsf.gpstracking.other

import android.content.Context
import android.icu.util.Calendar
import android.os.Build
import androidx.annotation.RequiresApi
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import com.viol4tsf.gpstracking.db.Run
import kotlinx.android.synthetic.main.marker_view.view.*
import java.text.SimpleDateFormat
import java.util.*

class CustomMarkerView(
    val runs: List<Run>,
    c: Context,
    layoutId: Int
): MarkerView(c, layoutId) {

    @RequiresApi(Build.VERSION_CODES.N)
    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        super.refreshContent(e, highlight)
        if (e == null){
            return
        }
        val currentRunId = e.x.toInt()
        val run = runs[currentRunId]
        val calendar = Calendar.getInstance().apply {
            timeInMillis = run.timestamp
        }
        val dateFormat = SimpleDateFormat("dd.MM.yy", Locale.getDefault())
        dateTextView.text = dateFormat.format(calendar.time)
        val avgSpeed = "${run.avgSpeedInKMH}км/ч"
        avgSpeedTextView.text = avgSpeed
        val distanceInKm = "${run.distanceInMeters / 1000f}км"
        distanceTextView.text = distanceInKm
        durationTextView.text = TrackingUtility.getFormattedStopWatchTime(run.timeInMillis)
        val caloriesBurned = "${run.caloriesBurned}ккал"
        caloriesBurnedTextView.text = caloriesBurned
    }

    //положение всплывающего окна
    override fun getOffset(): MPPointF {
        return MPPointF(-width / 2f, -height.toFloat())
    }
}