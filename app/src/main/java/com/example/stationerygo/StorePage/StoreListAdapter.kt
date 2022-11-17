package com.example.stationerygo.StorePage

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.stationerygo.R
import com.squareup.picasso.Picasso


class StoreListAdapter(private val mList: List<StoreListData>,
                       val clickListener: (StoreListData, Int) -> Unit)
    : RecyclerView.Adapter<StoreListAdapter.ViewHolder>()
{
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // inflates the card_view_design view
        // that is used to hold list item
        val view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.card_list_store, parent, false)

        return ViewHolder(view)
    }

    // binds the list items to a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val ItemsViewModel = mList[position]
        // sets the image to the imageview from our itemHolder class
//        holder.imageView.setImageResource(ItemsViewModel.image)
        Picasso.get()
            .load(ItemsViewModel.storeImage)
            .into(holder.imageView)

        // sets the text to the textview from our itemHolder class
        holder.title.text = ItemsViewModel.storeName

        if(ItemsViewModel.status == "Open"){
            holder.city.setTextColor(Color.parseColor("#689f38"))
        }
        else{
            holder.city.setTextColor(Color.parseColor("#d50000"))
        }

        holder.city.text = ItemsViewModel.status
        holder.startTime.text = ItemsViewModel.startTime + " - " + ItemsViewModel.endTime
        holder.endTime.text = ItemsViewModel.city

        holder?.itemView?.setOnClickListener { clickListener(ItemsViewModel, position) }

    }


    // return the number of the items in the list
    override fun getItemCount(): Int {
        return mList.size
    }

    // Holds the views for adding it to image and text
    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val imageView: ImageView = itemView.findViewById(R.id.orderShopImage)
        val title: TextView = itemView.findViewById(R.id.text_Title)
        val city: TextView = itemView.findViewById(R.id.text_City)
        val startTime: TextView = itemView.findViewById(R.id.text_StartTime)
        val endTime: TextView = itemView.findViewById(R.id.text_EndTime)

    }
}