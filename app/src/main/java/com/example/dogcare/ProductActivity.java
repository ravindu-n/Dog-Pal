package com.example.dogcare;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.example.dogcare.interfaces.ProductDao;
import com.example.dogcare.classes.Product;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ProductActivity extends AppCompatActivity implements ProductArray.ProductEditListener {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final String TAG = "ProductActivity";

    private AppDatabase db;
    private ProductDao productDao;
    private ProductArray adapter;
    private RecyclerView productList;
    private EditText productNameEditText, productDescriptionEditText, productPriceEditText, productBrandEditText;
    private Spinner productTypeSpinner, productAgeRangeSpinner;
    private Button addProductButton;
    private String[] productTypes;
    private String[] productAgeRanges;
    private ImageView productImageView;
    private String currentPhotoPath; // Store the path of the captured image
    private boolean isUserCustomer = false; // Set the user type (replace with your logic)
    private boolean isUpdatingProduct = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);

        // Initialize Database
        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "dog-nutrition-db")
                .allowMainThreadQueries() // Use this for simplicity, consider AsyncTasks or other approaches for production
                .build();
        productDao = db.productDao();

        // Initialize Views
        productNameEditText = findViewById(R.id.productNameEditText);
        productDescriptionEditText = findViewById(R.id.productDescriptionEditText);
        productPriceEditText = findViewById(R.id.productPriceEditText);
        productBrandEditText = findViewById(R.id.productBrandEditText);
        productTypeSpinner = findViewById(R.id.productTypeSpinner);
        productAgeRangeSpinner = findViewById(R.id.productAgeRangeSpinner);
        addProductButton = findViewById(R.id.addProductButton);
        productList = findViewById(R.id.productList);
        productImageView = findViewById(R.id.productImageView);

        productTypes = new String[]{"Food", "Treats", "Toys", "Accessories", "Supplements", "Grooming"};
        SpinnerArray productTypeAdapter = new SpinnerArray(this, productTypes);
        productTypeSpinner.setAdapter(productTypeAdapter);

        productAgeRanges = new String[]{"Puppy", "Adult", "Senior", "All Life Stages"};
        SpinnerArray productAgeRangeAdapter = new SpinnerArray(this, productAgeRanges);
        productAgeRangeSpinner.setAdapter(productAgeRangeAdapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        productList.setLayoutManager(layoutManager);
        // Set up Product List Adapter
        adapter = new ProductArray(this, productDao.getAllProducts(), productDao, isUserCustomer, this);
        productList.setAdapter(adapter);

        // Add Product Button Click Listener
        addProductButton.setOnClickListener(view -> {
            if (!isUpdatingProduct) {
                addProduct();
            } else {
                updateProduct();
            }
        });

        // ImageView Click Listener to open gallery
        productImageView.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
        });
    }


    private void addProduct() {
        String name = productNameEditText.getText().toString();
        String description = productDescriptionEditText.getText().toString();
        String priceString = productPriceEditText.getText().toString();
        String brand = productBrandEditText.getText().toString();
        String type = productTypeSpinner.getSelectedItem().toString();
        int ageRange = productAgeRangeSpinner.getSelectedItemPosition();

        if (name.isEmpty() || description.isEmpty() || priceString.isEmpty() || brand.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceString);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid price format", Toast.LENGTH_SHORT).show();
            return;
        }

        Product newProduct = new Product();
        newProduct.setName(name);
        newProduct.setDescription(description);
        newProduct.setPrice(price);
        newProduct.setBrand(brand);
        newProduct.setType(type);
        newProduct.setAgeRange(ageRange);
        newProduct.setImageUrl(currentPhotoPath); // Save the image path

        productDao.insertProduct(newProduct);
        refreshProductList();

        clearInputFields();
        Toast.makeText(this, "Product added successfully!", Toast.LENGTH_SHORT).show();
    }

    private void updateProduct() {
        // Get the product ID from the intent extras
        int productId = getIntent().getIntExtra("productId", -1);
        if (productId == -1) {
            Toast.makeText(this, "Invalid product ID", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get updated product details from the input fields
        String name = productNameEditText.getText().toString();
        String description = productDescriptionEditText.getText().toString();
        double price = Double.parseDouble(productPriceEditText.getText().toString());
        String brand = productBrandEditText.getText().toString();
        String type = productTypeSpinner.getSelectedItem().toString();
        int ageRange = productAgeRangeSpinner.getSelectedItemPosition();

        // Get the existing product from the database to check if the image needs updating
        Product existingProduct = productDao.getProductById(productId);

        // Update the product in the database using the DAO
        Product updatedProduct = new Product();
        updatedProduct.setId(productId);
        updatedProduct.setName(name);
        updatedProduct.setDescription(description);
        updatedProduct.setPrice(price);
        updatedProduct.setBrand(brand);
        updatedProduct.setType(type);
        updatedProduct.setAgeRange(ageRange);
        // Update the image path only if a new image was selected
        if (currentPhotoPath != null && !currentPhotoPath.isEmpty()) {
            updatedProduct.setImageUrl(currentPhotoPath);
        } else {
            updatedProduct.setImageUrl(existingProduct.getImageUrl());
        }

        productDao.updateProduct(updatedProduct);
        refreshProductList();

        // Reset the button and state
        addProductButton.setText("Add Product");
        isUpdatingProduct = false;

        clearInputFields();
        Toast.makeText(this, "Product updated successfully", Toast.LENGTH_SHORT).show();
    }

    // Clear input fields
    private void clearInputFields() {
        productNameEditText.setText("");
        productDescriptionEditText.setText("");
        productPriceEditText.setText("");
        productBrandEditText.setText("");
        productTypeSpinner.setSelection(0); // Reset spinner to default selection
        productAgeRangeSpinner.setSelection(0); // Reset spinner to default selection
        productImageView.setImageResource(R.drawable.ic_product); // Set to default image
        currentPhotoPath = null;
    }

    private void refreshProductList() {
        List<Product> updatedProducts = productDao.getAllProducts();
        // Instead of adapter.clear() and adapter.addAll()
        adapter.updateProducts(updatedProducts);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
            if (data != null) {
                Uri selectedImageUri = data.getData();

                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                    productImageView.setImageBitmap(bitmap);

                    // Save the image to a file and get the file path
                    currentPhotoPath = saveImageToStorage(bitmap);
                    Log.d(TAG, "Image saved to: " + currentPhotoPath);

                } catch (IOException e) {
                    Log.e(TAG, "Error processing image", e);
                    Toast.makeText(this, "Error selecting image", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    // Save the image to internal storage and return the file path
    private String saveImageToStorage(Bitmap bitmap) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        try {
            File imageFile = File.createTempFile(
                    imageFileName,
                    ".jpg",
                    storageDir
            );

            FileOutputStream fos = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();

            return imageFile.getAbsolutePath();

        } catch (IOException e) {
            Log.e(TAG, "Error saving image to file", e);
            return null;
        }
    }

    @Override
    public void onEditProduct(Product product) {
        // Update UI elements with product details
        productNameEditText.setText(product.getName());
        productDescriptionEditText.setText(product.getDescription());
        productPriceEditText.setText(String.valueOf(product.getPrice()));
        productBrandEditText.setText(product.getBrand());

        int typePosition = ((ArrayAdapter<String>) productTypeSpinner.getAdapter()).getPosition(product.getType());
        productTypeSpinner.setSelection(typePosition);
        productAgeRangeSpinner.setSelection(product.getAgeRange());

        // Update the button text and state
        addProductButton.setText("Update Product");
        isUpdatingProduct = true;

        Intent intent = getIntent();
        intent.putExtra("productId", product.getId());

        // Load and display the image from the stored path
        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            Bitmap bitmap = BitmapFactory.decodeFile(product.getImageUrl());
            productImageView.setImageBitmap(bitmap);

            // Set the current photo path to the existing image path so it can be updated if needed
            currentPhotoPath = product.getImageUrl();
        } else {
            productImageView.setImageResource(R.drawable.ic_user);
        }
    }
}