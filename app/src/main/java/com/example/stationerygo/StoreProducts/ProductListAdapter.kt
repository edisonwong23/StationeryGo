package com.example.stationerygo.StoreProducts

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.stationerygo.R
import com.squareup.picasso.Picasso

class ProductListAdapter (private val mList: List<ProductListData>,
                          val clickListener: (ProductListData, Int) -> Unit)
    : RecyclerView.Adapter<ProductListAdapter.ViewHolder>()
{


    // create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // inflates the card_view_design view
        // that is used to hold list item
        val view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.card_list_product_owner, parent, false)

        return ViewHolder(view)
    }

    // binds the list items to a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val ItemsViewModel = mList[position]
        // sets the image to the imageview from our itemHolder class
//        holder.imageView.setImageResource(ItemsViewModel.image)
        Picasso.get()
            .load(ItemsViewModel.productImage)
            .into(holder.imageView)

        // sets the text to the textview from our itemHolder class
        holder.title.text = ItemsViewModel.productName

        holder?.itemView?.setOnClickListener { clickListener(ItemsViewModel, position) }

    }


    // return the number of the items in the list
    override fun getItemCount(): Int {
        return mList.size
    }

    // Holds the views for adding it to image and text
    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {

        val imageView: ImageView = itemView.findViewById(R.id.orderImageView)
        val title: TextView = itemView.findViewById(R.id.orderShopName_txt)

    }
}