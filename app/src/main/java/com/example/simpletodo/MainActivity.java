package com.example.simpletodo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

//Controls main page of app where the user can view the to do list
public class MainActivity extends AppCompatActivity {

    public static final String KEY_ITEM_TEXT = "item_text";
    public static final String KEY_ITEM_POSITION = "item_position";
    public static final int EDIT_TEXT_CODE = 20;

    List<String> items;

    Button addButton; //button to add an item
    EditText addItem; //text field to write a new item
    RecyclerView itemList; //list of to do items
    ItemsAdapter itemsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        addButton = findViewById(R.id.addButton);
        addItem = findViewById(R.id.addItem);
        itemList = findViewById(R.id.itemList);

        loadItems();

        //long click listener for each item in the to do list
        //will delete the item that is long clicked.
        ItemsAdapter.OnLongClickListener onLongClickListener = new ItemsAdapter.OnLongClickListener(){
            @Override
            public void onItemLongClicked(int position) {
                items.remove(position);
                itemsAdapter.notifyItemRemoved(position);
                Toast.makeText(getApplicationContext(), "Item was removed!", Toast.LENGTH_SHORT).show();
                saveItems();
            }
        };

        //click listener for each item in the to do list
        //will bring up a screen where user can edit the item
        ItemsAdapter.OnClickListener onClickListener = new ItemsAdapter.OnClickListener(){
            @Override
            public void onItemClicked(int position) {
                Intent i = new Intent(MainActivity.this, EditActivity.class);
                i.putExtra(KEY_ITEM_TEXT, items.get(position));
                i.putExtra(KEY_ITEM_POSITION, position);

                startActivityForResult(i, EDIT_TEXT_CODE);
            }
        };

        itemsAdapter = new ItemsAdapter(items, onLongClickListener, onClickListener);
        itemList.setAdapter(itemsAdapter);
        itemList.setLayoutManager(new LinearLayoutManager(this));

        //click listener for the add button.
        //will add the item to the list
        addButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){
                String newItem = addItem.getText().toString();
                items.add(newItem);

                itemsAdapter.notifyItemInserted(items.size()-1);

                addItem.setText("");
                Toast.makeText(getApplicationContext(), "Item was added!", Toast.LENGTH_SHORT).show();
                saveItems();
            }
        });
    }

    //handle the result of the EditActivity
    //Update the item on the to do list with the new text
    @SuppressLint("MissingSuperCall")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK && requestCode == EDIT_TEXT_CODE) {
            String itemText = data.getStringExtra(KEY_ITEM_TEXT);
            int position = data.getExtras().getInt(KEY_ITEM_POSITION);

            items.set(position, itemText);
            itemsAdapter.notifyItemChanged(position);

            saveItems();
            Toast.makeText(getApplicationContext(), "Item updated successfully!", Toast.LENGTH_SHORT).show();
        } else {
            Log.w("MainActivity", "Unknown call to onActivityResult");
        }
    }

    private File getDataFile(){
        return new File(getFilesDir(), "data.txt");
    }

    //This function will load items by reading every line of the data file
    private void loadItems(){
        try {
            items = new ArrayList<>(FileUtils.readLines(getDataFile(), Charset.defaultCharset()));
        }catch (IOException e){
            Log.e("MainActivity", "Error reading items", e);
            items = new ArrayList<>();
        }
    }

    //This function saves items by writing them into the data file
    private void saveItems(){
        try {
            FileUtils.writeLines(getDataFile(), items);
        }catch (IOException e) {
            Log.e("MainActivity", "Error writing items", e);
        }
    }
}