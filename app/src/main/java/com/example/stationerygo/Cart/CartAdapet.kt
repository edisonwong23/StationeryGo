package com.example.stationerygo.Cart

import android.app.AlertDialog
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.stationerygo.R
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso

private lateinit var database: DatabaseReference

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
        holder.price.text = "Price(RM): " + ItemsViewModel.itemPrice
        holder.itemInQty.text = "Quantity: " + ItemsViewModel.itemQty

        holder.edit.setOnClickListener {
            var bundle = bundleOf(
                "storeID" to ItemsViewModel.shopID.toString(),
                "itemID" to ItemsViewModel.itemID.toString()
            )
            findNavController(it).navigate(R.id.action_cart_Page_to_itemDetailPage,bundle)
        }

        holder.removeItem.setOnClickListener { viewContext ->
            var itemName = ItemsViewModel.itemName.toString()
            val alartDialog = AlertDialog.Builder(viewContext.context,R.style.AlertDialogCustom)
            alartDialog.apply {
                setTitle("Remove Item?")
                setMessage("Are you sure you want to remove $itemName ?")
                setPositiveButton("Remove"){ _: DialogInterface?, _: Int ->

                    database = FirebaseDatabase.getInstance().getReference("Cart")
                    val removeRef = database.child(ItemsViewModel.userID.toString())
                        .child(ItemsViewModel.shopID.toString())
                        .child(ItemsViewModel.itemID.toString())


                    removeRef.removeValue().addOnCompleteListener{
                        if(it.isSuccessful){
                            Toast.makeText(viewContext.context,"Item $itemName Has Been Removed!",Toast.LENGTH_SHORT).show()
                        }
                        else{
                            Toast.makeText(viewContext.context,"Item $itemName Failed TO Removed!",Toast.LENGTH_SHORT).show()
                        }
                    }

                }
                setNegativeButton("Cancel"){_, _ ->

                }
            }.create().show()


        }

    }

    // return the number of the items in the list
    override fun getItemCount(): Int {
        return mList.size
    }

    // Holds the views for adding it to image and text
    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val productImage: ImageView = itemView.findViewById(R.id.imageProduct)
        val title: TextView = itemView.findViewById(R.id.orderShopName)
        val price : TextView = itemView.findViewById(R.id.cardCartProductPrice_txt)
        val edit: Button = itemView.findViewById(R.id.edit_btn)
        val removeItem: Button = itemView.findViewById(R.id.cardCartRemoveItem_btn)
        val itemInQty: TextView = itemView.findViewById(R.id.cardCartProductQuantity_txt)
    }


}