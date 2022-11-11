package com.example.stationerygo.StoreProducts

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.navigation.Navigation
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.stationerygo.R
import com.squareup.picasso.Picasso
import kotlin.coroutines.coroutineContext

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

        holder.qty.text = "Quantity " +ItemsViewModel.productQty

        holder.editButton.setOnClickListener{
            var bundle = bundleOf(
                "storeID" to ItemsViewModel.storeID,
                "productKey" to ItemsViewModel.productKey,
            )
            findNavController(it).navigate(R.id.action_productLists_to_editProductPage,bundle)
        }

        holder.deleteButton.setOnClickListener{
            Toast.makeText(it.context,ItemsViewModel.productName.toString(),Toast.LENGTH_SHORT).show()
        }

    }


    // return the number of the items in the list
    override fun getItemCount(): Int {
        return mList.size
    }

    // Holds the views for adding it to image and text
    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {

        val imageView: ImageView = itemView.findViewById(R.id.orderShopImage)
        val title: TextView = itemView.findViewById(R.id.orderShopName)
        val qty: TextView = itemView.findViewById(R.id.manageProductQty)
        val editButton: Button = itemView.findViewById(R.id.manageProductEdit_btn)
        val deleteButton: Button = itemView.findViewById(R.id.manageProductDelete_btn)
    }
}