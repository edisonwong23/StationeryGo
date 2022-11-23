package com.example.stationerygo.StoreOwner.OrderPage

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.stationerygo.R
import com.squareup.picasso.Picasso

class ShopOrderAdapter(private val mList: List<ShopOrderData>,
                       val clickListener: (ShopOrderData, Int) -> Unit)
    : RecyclerView.Adapter<ShopOrderAdapter.ViewHolder>() {

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
            .load(ItemsViewModel.userImage)
            .into(holder.itemImage)

        // sets the text to the textview from our itemHolder class
        holder.itemName.text = ItemsViewModel.userName
        holder.orderDate.text =  ItemsViewModel.orderDate
        holder.orderStatus.text = ItemsViewModel.orderStatus
        holder.orderType.text = ItemsViewModel.orderType
        holder?.itemView?.setOnClickListener { clickListener(ItemsViewModel, position) }

        if(ItemsViewModel.orderStatus == "Completed"){
            holder.orderStatus.setTextColor(Color.parseColor("#689f38"))
        }
        else if(ItemsViewModel.orderStatus == "Pending"){
            holder.orderStatus.setTextColor(Color.parseColor("#000000"))
        }
        else if(ItemsViewModel.orderStatus == "Cancel"){
            holder.orderStatus.setTextColor(Color.parseColor("#d50000"))
        }
        else{
            holder.orderStatus.setTextColor(Color.parseColor("#6200EE"))
        }

    }


    // return the number of the items in the list
    override fun getItemCount(): Int {
        return mList.size
    }

    // Holds the views for adding it to image and text
    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val itemImage: ImageView = itemView.findViewById(R.id.orderShopImage)
        val itemName: TextView = itemView.findViewById(R.id.orderShopName)
        val orderDate: TextView = itemView.findViewById(R.id.orderOrderDate_txt)
        val orderStatus: TextView = itemView.findViewById(R.id.orderOrderStatus_txt)
        var orderType: TextView = itemView.findViewById(R.id.orderOrderType_txt)
    }
}