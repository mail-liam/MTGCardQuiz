package com.example.liam.mtgcardquiz;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class StartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        ArrayList<String> setArray = new ArrayList<>();
        setArray.add("Aether Revolt");
        setArray.add("Kaladesh");
        setArray.add("Eldritch Moon");
        setArray.add("Shadows over Innistrad");
        setArray.add("Oath of the Gatewatch");
        setArray.add("Battle for Zendikar");
        setArray.add("Eternal Masters");

        ListView setList = (ListView)findViewById(R.id.setList);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, setArray);
        setList.setAdapter(arrayAdapter);

        setList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Log.i("position clicked", Integer.toString(position));
                String setName = (String)parent.getItemAtPosition(position);

                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                switch(setName){
                    case "Aether Revolt":
                        intent.putExtra("setCode", "AER");
                        startActivity(intent);
                        break;
                    case "Kaladesh":
                        intent.putExtra("setCode", "KLD");
                        startActivity(intent);
                        break;
                    case "Eldritch Moon":
                        intent.putExtra("setCode", "EMN");
                        startActivity(intent);
                        break;
                    case "Shadows over Innistrad":
                        intent.putExtra("setCode", "SOI");
                        startActivity(intent);
                        break;
                    case "Oath of the Gatewatch":
                        intent.putExtra("setCode", "OGW");
                        startActivity(intent);
                        break;
                    case "Battle for Zendikar":
                        intent.putExtra("setCode", "BFZ");
                        startActivity(intent);
                        break;
                    case "Eternal Masters":
                        intent.putExtra("setCode", "EMA");
                        startActivity(intent);
                        break;
                    default:
                        Toast.makeText(getApplicationContext(), "Error: Invalid set", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}