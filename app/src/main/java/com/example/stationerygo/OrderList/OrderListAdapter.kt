package com.example.stationerygo.OrderList

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.stationerygo.R
import com.squareup.picasso.Picasso

class OrderListAdapter (private val mList: List<OrderListData>,
                        val clickListener: (OrderListData, Int) -> Unit)
    : RecyclerView.Adapter<OrderListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // inflates the card_view_design view
        // that is used to hold list item
        val view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.card_list_order, parent, false)

        return ViewHolder(view)
    }

    // binds the list items to a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val ItemsViewModel = mList[position]
        // sets the image to the imageview from our itemHolder class
//        holder.imageView.setImageResource(ItemsViewModel.image)
        Picasso.get()
            .load(ItemsViewModel.orderImage)
            .into(holder.orderImage)

        // sets the text to the textview from our itemHolder class
        holder.shopName.text = ItemsViewModel.orderShop
        holder.orderDate.text = "Order Date: " + ItemsViewModel.orderDate
        holder.orderStatus.text = "Order Status: "+ ItemsViewModel.orderStatus

        holder?.itemView?.setOnClickListener { clickListener(ItemsViewModel, position) }

    }


    // return the number of the items in the list
    override fun getItemCount(): Int {
        return mList.size
    }

    // Holds the views for adding it to image and text
    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val orderImage: ImageView = itemView.findViewById(R.id.orderShopImage)
        val shopName: TextView = itemView.findViewById(R.id.orderShopName)
        val orderDate: TextView = itemView.findViewById(R.id.orderOrderDate)
        val orderStatus: TextView = itemView.findViewById(R.id.orderOrderStatus)
    }

}