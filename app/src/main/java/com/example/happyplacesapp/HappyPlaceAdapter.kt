package com.example.happyplacesapp

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.happyplacesapp.data.HappyPlace
import com.example.happyplacesapp.databinding.ItemRowRvBinding

class HappyPlaceAdapter(private var data: ArrayList<HappyPlace>): RecyclerView.Adapter<HappyPlaceAdapter.HappyPlaceViewHolder>() {
    inner class HappyPlaceViewHolder(private val binding: ItemRowRvBinding, listener: OnItemClickListener): RecyclerView.ViewHolder(binding.root) {
        val tvTitle = binding.tvTitle
        val tvDescription = binding.tvDescription
        val ivImage = binding.ivHappyPlaceImage
        init {
            binding.root.setOnClickListener {
                listener.OnItemClick(adapterPosition)
            }
        }
        fun bind(place: HappyPlace) {
            tvTitle.text = place.title
            tvDescription.text = place.description
            ivImage.setImageBitmap(place.imageBitmap)
        }
    }
    private var mlistener: OnItemClickListener? = null
    interface OnItemClickListener {
        fun OnItemClick(position: Int)
    }
    fun setOnItemClickListener(onItemClickListener: OnItemClickListener) {
        mlistener = onItemClickListener
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateRVData(newData: ArrayList<HappyPlace>) {
        data = newData
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HappyPlaceViewHolder {
        return HappyPlaceViewHolder(ItemRowRvBinding.inflate(LayoutInflater.from(parent.context)), listener = mlistener!!)
    }

    override fun onBindViewHolder(holder: HappyPlaceViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int {
        return data.size
    }
}