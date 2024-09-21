package com.example.customlistview.View

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.customlistview.R
import com.example.customlistview.ViewModel.ProductViewModel
import com.example.customlistview.model.Product

class ProductActivity : AppCompatActivity() {
    private val productViewModel: ProductViewModel by viewModels()
    private val PICK_IMAGE_REQUEST = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_store)

        val productListView: ListView = findViewById(R.id.productListView)
        val productAdapter = ProductAdapter(this, mutableListOf())

        productListView.adapter = productAdapter

        productViewModel.productList.observe(this, Observer { products ->
            productAdapter.updateProducts(products)
        })

        productListView.setOnItemClickListener { _, _, position, _ ->
            val product = productViewModel.productList.value?.get(position)
            if (product != null) {
                showDeleteConfirmationDialog(product)
            }
        }

        val addProductButton: Button = findViewById(R.id.addProductButton)
        addProductButton.setOnClickListener {
            addProduct()
        }

        val productImage: ImageView = findViewById(R.id.productImage)
        productImage.setOnClickListener {
            openGallery()
        }

        productViewModel.selectedImageUri.observe(this, Observer { uri ->
            uri?.let {
                productImage.setImageURI(it)
            }
        })
    }

    private fun addProduct() {
        val productName: EditText = findViewById(R.id.productName)
        val productPrice: EditText = findViewById(R.id.productPrice)
        val productImage: ImageView = findViewById(R.id.productImage)

        val name = productName.text.toString()
        val price = productPrice.text.toString().toDoubleOrNull() ?: 0.0

        if (productViewModel.selectedImageUri.value == null) {
            Toast.makeText(this, getString(R.string.pls_add_image), Toast.LENGTH_SHORT).show()
            return
        }

        val image = (productImage.drawable as BitmapDrawable).bitmap

        when {
            name.isEmpty() -> {
                Toast.makeText(this, getString(R.string.pls_input_name), Toast.LENGTH_SHORT).show()
                return
            }
            price == 0.0 -> {
                Toast.makeText(this, getString(R.string.pls_input_price), Toast.LENGTH_SHORT).show()
                return
            }
        }

        val product = Product(name, price, image)
        productViewModel.addUser(product)
        productName.text.clear()
        productPrice.text.clear()
        productImage.setImageResource(R.drawable.ic_shop)
    }

    private fun showDeleteConfirmationDialog(product: Product) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.my_dialog))
        builder.setMessage(getString(R.string.are_you_sure, product.name))
        builder.setPositiveButton(getString(R.string.yes)) { _, _ ->
            productViewModel.deleteUser(product)
            Toast.makeText(this, getString(R.string.delete, product.name), Toast.LENGTH_LONG).show()
        }
        builder.setNegativeButton(getString(R.string.no)) { dialog, _ ->
            dialog.dismiss()
        }
        builder.create().show()
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            val selectedImageUri = data.data
            productViewModel.setSelectedImageUri(selectedImageUri)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_store, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_exit -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
