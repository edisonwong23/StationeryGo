package com.example.stationerygo.OrderList.OrderDetailPage

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.stationerygo.R
import com.squareup.picasso.Picasso

class OrderDetailsAdapter (private val mList: List<OrderDetailsClass>,
                           val clickListener: (OrderDetailsClass, Int) -> Unit)
    : RecyclerView.Adapter<OrderDetailsAdapter.ViewHolder>()
{
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // inflates the card_view_design view
        // that is used to hold list item
        val view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.card_list_order_details, parent, false)

        return ViewHolder(view)
    }

    // binds the list items to a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val ItemsViewModel = mList[position]
        // sets the image to the imageview from our itemHolder class
//        holder.imageView.setImageResource(ItemsViewModel.image)
        Picasso.get()
            .load(ItemsViewModel.itemImage)
            .into(holder.itemImage)

        // sets the text to the textview from our itemHolder class
        holder.itemName.text = ItemsViewModel.itemName
        holder.itemPrice.text = "RM " + ItemsViewModel.itemPrice
        holder.itemQty.text = ItemsViewModel.itemQty


    }


    // return the number of the items in the list
    override fun getItemCount(): Int {
        return mList.size
    }

    // Holds the views for adding it to image and text
    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val itemImage: ImageView = itemView.findViewById(R.id.orderShopImage)
        val itemName: TextView = itemView.findViewById(R.id.orderShopName)
        val itemPrice: TextView = itemView.findViewById(R.id.orderOrderDate_txt)
        val itemQty: TextView = itemView.findViewById(R.id.orderOrderStatus_txt)
    }
}