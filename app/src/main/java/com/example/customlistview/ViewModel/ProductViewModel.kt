package com.example.customlistview.ViewModel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.customlistview.model.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

class ProductViewModel(application: Application) : AndroidViewModel(application) {
    private val _productList = MutableLiveData<MutableList<Product>>(mutableListOf())
    val productList: LiveData<MutableList<Product>> get() = _productList

    private val _selectedImageUri = MutableLiveData<Uri?>()
    val selectedImageUri: LiveData<Uri?> get() = _selectedImageUri

    private val sharedPreferences = application.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            loadUserList()
        }
    }

    fun addUser(product: Product) {
        val currentList = _productList.value ?: mutableListOf()
        currentList.add(product)
        _productList.value = currentList
        saveUserList(currentList)
    }

    fun deleteUser(product: Product) {
        val currentList = _productList.value ?: mutableListOf()
        currentList.remove(product)
        _productList.value = currentList
        saveUserList(currentList)
    }

    fun setSelectedImageUri(uri: Uri?) {
        _selectedImageUri.value = uri
    }

    private fun saveUserList(product: List<Product>) {
        val editor = sharedPreferences.edit()
        val productListString = product.joinToString(";") { "${it.name},${it.price},${bitmapTo(it.image)}" }
        editor.putString("productList", productListString)
        editor.apply()
    }

    private fun loadUserList() {
        val productListString = sharedPreferences.getString("productList", "")
        if (!productListString.isNullOrEmpty()) {
            val products = productListString.split(";").mapNotNull {
                val productData = it.split(",")
                if (productData.size == 3) {
                    val name = productData[0]
                    val price = productData[1].toDoubleOrNull() ?: 0.0
                    val imageBytes = Base64.decode(productData[2], Base64.DEFAULT)
                    val image = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    if (name.isNotEmpty() && image != null) {
                        Product(name, price, image)
                    } else {
                        Log.e("ProductViewModel", "Invalid product data: $it")
                        null
                    }
                } else {
                    Log.e("ProductViewModel", "Invalid product data: $it")
                    null
                }
            }
            _productList.postValue(products.toMutableList())
        } else {
            Log.e("ProductViewModel", "Product list string is null or empty")
        }
    }

    private fun bitmapTo(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }
}
