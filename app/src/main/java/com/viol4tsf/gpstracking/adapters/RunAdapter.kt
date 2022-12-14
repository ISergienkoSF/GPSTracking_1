package com.viol4tsf.gpstracking.adapters

import android.app.AlertDialog
import android.icu.util.Calendar
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.viol4tsf.gpstracking.R
import com.viol4tsf.gpstracking.db.Run
import com.viol4tsf.gpstracking.other.TrackingUtility
import com.viol4tsf.gpstracking.ui.viewmodels.MainViewModel
import kotlinx.android.synthetic.main.list_item_run.view.*
import java.text.SimpleDateFormat
import java.util.*

class RunAdapter(private val viewModel: MainViewModel) : RecyclerView.Adapter<RunAdapter.RunViewHolder>(){


    inner class RunViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)

    //определение отличий в списке для обновления только определённых элементов
    val diffCallback = object : DiffUtil.ItemCallback<Run>(){
        override fun areItemsTheSame(oldItem: Run, newItem: Run): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Run, newItem: Run): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    //сравление и замена проходит в фоновом режиме
    val differ = AsyncListDiffer(this, diffCallback)

    fun submitList(list: List<Run>) = differ.submitList(list)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RunViewHolder {
        return RunViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.list_item_run,
                parent,
                false
            )
        )
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onBindViewHolder(holder: RunViewHolder, position: Int) {
        val run = differ.currentList[position]
        holder.itemView.apply {
            //загрузка изображения
            Glide.with(this).load(run.img).into(runImageView)
            val calendar = Calendar.getInstance().apply {
                timeInMillis = run.timestamp
            }
            val dateFormat = SimpleDateFormat("dd.MM.yy", Locale.getDefault())
            dateTextView.text = dateFormat.format(calendar.time)
            val avgSpeed = "${run.avgSpeedInKMH}км/ч"
            avgSpeedTextView.text = avgSpeed
            val distanceInKm = "${run.distanceInMeters / 1000f}км"
            distanceTextView.text = distanceInKm
            timeTextView.text = TrackingUtility.getFormattedStopWatchTime(run.timeInMillis)
            val caloriesBurned = "${run.caloriesBurned}ккал"
            caloriesTextView.text = caloriesBurned
            runImageView.setOnLongClickListener { v: View ->
                val builder = AlertDialog.Builder(context, R.style.AlertDialogTheme)
                    .setTitle("Удалить запись?")
                    .setMessage("Вы уверены что хотите удалить выбранную прогулку?")
                    .setIcon(R.drawable.ic_baseline_delete_24)
                    .setPositiveButton("Да") {_, _ ->
                        viewModel.deleteRun(run)
                    }
                    .setNegativeButton("Нет"){dialogInterface, _ ->
                        dialogInterface.cancel()
                    }
                val alertDialog: AlertDialog = builder.create()
                alertDialog.setCancelable(true)
                alertDialog.show()
                return@setOnLongClickListener true
            }
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }
}