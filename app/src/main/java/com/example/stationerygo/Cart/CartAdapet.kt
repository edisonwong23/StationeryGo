package com.example.stationerygo.Cart

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.stationerygo.OrderList.OrderListData
import com.example.stationerygo.R
import com.squareup.picasso.Picasso

class CartAdapet(private val mList: ArrayList<Cart_Data>,
                 val clickListener: (Cart_Data, Int) -> Unit)
    : RecyclerView.Adapter<CartAdapet.ViewHolder>()
{
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // inflates the card_view_design view
        // that is used to hold list item
        val view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.card_list_cart_product, parent, false)

        return ViewHolder(view)
    }

    // binds the list items to a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val ItemsViewModel = mList[position]
        // sets the image to the imageview from our itemHolder class
//        holder.imageView.setImageResource(ItemsViewModel.image)
        Picasso.get()
            .load(ItemsViewModel.itemImage)
            .into(holder.productImage)

        // sets the text to the textview from our itemHolder class
        holder.title.text = ItemsViewModel.itemName
        holder.qty.text = ItemsViewModel.itemQty

        holder?.itemView?.setOnClickListener { clickListener(ItemsViewModel, position) }

    }


    // return the number of the items in the list
    override fun getItemCount(): Int {
        return mList.size
    }

    // Holds the views for adding it to image and text
    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val productImage: ImageView = itemView.findViewById(R.id.imageProduct)
        val title: TextView = itemView.findViewById(R.id.orderShopName_txt)
        val add: Button = itemView.findViewById(R.id.add_btn)
        val minus: Button = itemView.findViewById(R.id.minus_btn)
        val qty: TextView = itemView.findViewById(R.id.itemQTY_txt)
    }
}