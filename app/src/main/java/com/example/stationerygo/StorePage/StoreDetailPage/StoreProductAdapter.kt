package com.example.stationerygo.StorePage.StoreDetailPage

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.stationerygo.R
import com.squareup.picasso.Picasso


class StoreProductAdapter(private val mList: List<StoreProductData>,
                          val clickListener: (StoreProductData, Int) -> Unit)
    : RecyclerView.Adapter<StoreProductAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // inflates the card_view_design view
        // that is used to hold list item
        val view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.card_list_store_products, parent, false)

        return ViewHolder(view)
    }

    // binds the list items to a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val ItemsViewModel = mList[position]
        // sets the image to the imageview from our itemHolder class
//        holder.imageView.setImageResource(ItemsViewModel.image)
        Picasso.get()
            .load(ItemsViewModel.productImage)
            .into(holder.productImage)

        // sets the text to the textview from our itemHolder class
        holder.title.text = ItemsViewModel.productName
        holder.price.text = "RM " + ItemsViewModel.productPrice
        holder.qty.text = "Stock: " +ItemsViewModel.productQty

        holder?.itemView?.setOnClickListener { clickListener(ItemsViewModel, position) }

    }


    // return the number of the items in the list
    override fun getItemCount(): Int {
        return mList.size
    }

    // Holds the views for adding it to image and text
    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val productImage: ImageView = itemView.findViewById(R.id.productimage)
        val title: TextView = itemView.findViewById(R.id.orderShopName)
        val price: TextView = itemView.findViewById(R.id.productCardPrice_txt)
        val qty: TextView = itemView.findViewById(R.id.productQty_txt)
    }
}