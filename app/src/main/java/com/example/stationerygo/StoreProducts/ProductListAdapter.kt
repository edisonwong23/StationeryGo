package com.example.stationerygo.StoreProducts

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.util.Log
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
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import kotlin.coroutines.coroutineContext

class ProductListAdapter (private val mList: List<ProductListData>,
                          val clickListener: (ProductListData, Int) -> Unit)
    : RecyclerView.Adapter<ProductListAdapter.ViewHolder>()
{

    private lateinit var database: DatabaseReference

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

        holder.qty.text = "Quantity: " +ItemsViewModel.productQty

        holder.price.text = "Price(RM): "+ ItemsViewModel.productPrice

        holder.editButton.setOnClickListener{
            var bundle = bundleOf(
                "storeID" to ItemsViewModel.storeID,
                "productKey" to ItemsViewModel.productKey,
            )
            findNavController(it).navigate(R.id.action_productLists_to_editProductPage,bundle)
        }


        holder.deleteButton.setOnClickListener{
            val alartDialog = AlertDialog.Builder(it.context,R.style.AlertDialogCustom)
            var storeID = ItemsViewModel?.storeID
            var productKey = ItemsViewModel?.productKey
            var productName = ItemsViewModel?.productName

            alartDialog.apply {
                setTitle("Delete Item?")
                setMessage("Are you sure you want to delete $productName")
                setPositiveButton("Delete"){ _: DialogInterface?, _: Int ->
                    deleteProduct(storeID!!,productKey!!,productName!!,it.context)
                }
                setNegativeButton("Cancel"){_, _ ->
                    Toast.makeText(context,"Action has been cancelled",Toast.LENGTH_SHORT).show()
                }
            }.create().show()

//            Toast.makeText(it.context,ItemsViewModel.productName.toString(),Toast.LENGTH_SHORT).show()
        }

    }

    private fun deleteProduct(storeID:String,productKey:String,name:String,context: Context){
        database = FirebaseDatabase.getInstance().getReference("Products")
        var dataRef = database.child(storeID).child(productKey)

        dataRef.removeValue().addOnCompleteListener{
            Toast.makeText(context,"Product $name has been removed",Toast.LENGTH_SHORT).show()
        }.addOnFailureListener{
            Log.d("Products",it.toString())
            Toast.makeText(context,"An Error Has Occur! Please Try Again!",Toast.LENGTH_SHORT).show()
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
        val price: TextView = itemView.findViewById(R.id.productPrice_txt)
        val editButton: Button = itemView.findViewById(R.id.manageProductEdit_btn)
        val deleteButton: Button = itemView.findViewById(R.id.manageProductDelete_btn)
    }
}