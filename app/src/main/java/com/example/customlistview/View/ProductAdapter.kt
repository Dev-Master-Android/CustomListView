package com.example.customlistview.View

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.customlistview.R
import com.example.customlistview.model.Product

class ProductAdapter(private val context: Context, private var productList: MutableList<Product>) : BaseAdapter() {
    override fun getCount(): Int = productList.size
    override fun getItem(position: Int): Any = productList[position]
    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = convertView ?: LayoutInflater.from(context).inflate(R.layout.list_item_product, parent, false)

        val product = productList[position]
        val productName: TextView = view.findViewById(R.id.productName)
        val productPrice: TextView = view.findViewById(R.id.productPrice)
        val productImage: ImageView = view.findViewById(R.id.productImage)

        productName.text = product.name
        productPrice.text = product.price.toString()
        productImage.setImageBitmap(product.image)

        return view
    }

    fun updateProducts(newProductList: List<Product>) {
        productList.clear()
        productList.addAll(newProductList)
        notifyDataSetChanged()
    }
}
