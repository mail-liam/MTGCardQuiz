package com.example.liam.mtgcardquiz;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    ArrayList<String> answerArray = new ArrayList<>();
    ArrayList<String> questionTypes = new ArrayList<>();
    ArrayAdapter<String> arrayAdapter;
    ListView answerList;
    TextView questionTextView;
    Button nextButton;
    JSONArray cards;
    int max_options = 4, correctPos;
    Random rand = new Random();

    public void setQuestionType(View view) {
        //Reset the previous answers/background colours (if any)
        answerArray.clear();
        if(answerList.getCount() > 0) {
            for (int i = 0; i < answerList.getCount(); i++) {
                answerList.getChildAt(i).setBackgroundColor(Color.TRANSPARENT);
            }
        }

        answerList.setEnabled(true);
        nextButton.setVisibility(View.INVISIBLE);

        //Select the question type
        String type = questionTypes.get(rand.nextInt(questionTypes.size()));
        //String type = "layout"; //Debug question type
        Log.i("question type", type);

        JSONObject answerCard = getRandomCard(type);
        try {
            switch (type) {
                case "manaCost":
                    String manaCost = formatDisplayText(answerCard, type);
                    questionTextView.setText(String.format(getString(R.string.mana_cost), manaCost));
                    generateAnswers(answerCard, type, "colors");
                    break;
                case "power":
                case "toughness":
                    questionTextView.setText(String.format(getString(R.string.power_toughness), answerCard.getInt(type), type));
                    generateAnswers(answerCard, type);
                    break;
                case "layout":
                    while (answerCard.getString("layout").equals("normal")) {answerCard = getRandomCard(type);}
                    questionTextView.setText(String.format(getString(R.string.doublefaced), answerCard.getString("layout")));
                    generateAnswers(answerCard, type);
                    break;
                case "flavor":
                    if (questionTypes.get(rand.nextInt(questionTypes.size())).equals("flavor")) {
                        questionTextView.setText(String.format(getString(R.string.rules_flavor), "flavor text", answerCard.getString(type)));
                        generateAnswers(answerCard, type);
                        break;
                    } else {
                        answerCard = getRandomCard("text");     //Set a rules text card & type for when this falls into the next case
                        type = "text";
                    }
                case "text":
                    String hideAnswer = formatDisplayText(answerCard, type);
                    questionTextView.setText(String.format(getString(R.string.rules_flavor), "rules text", hideAnswer));
                    generateAnswers(answerCard, type);
                    break;
                default:
                    Log.i("Invalid Question Type", type);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void generateAnswers(JSONObject ans, String type){
        generateAnswers(ans, type, "");
    }

    public void generateAnswers(JSONObject answerCard, String type, String matchExtra){
        //Select correct position in the ListView
        correctPos = rand.nextInt(max_options);

        try {
            for (int i = 0; i < max_options; i++) {
                if (i == correctPos) {
                    answerArray.add(answerCard.getString("name"));
                } else {
                    JSONObject falseAnswerCard = getFalseAnswerCard(answerCard, type, matchExtra);

                    //Once we know our false card != answer card, check for duplicate answers
                    for (int j = 0; j < answerArray.size(); j++) {
                        if (falseAnswerCard.getString("name").equals(answerArray.get(j))) {
                            Log.i("DuplicateFalseAnswer", falseAnswerCard.getString("name"));
                            Log.i("At Position", Integer.toString(answerArray.size()));
                            falseAnswerCard = getFalseAnswerCard(answerCard, type, matchExtra);
                            j = 0;//Restart loop if duplicate is found
                        }
                    }
                    answerArray.add(falseAnswerCard.getString("name"));
                }
            }
        }
        catch (JSONException e){
            e.printStackTrace();
        }
        arrayAdapter.notifyDataSetChanged();
    }

    public JSONObject getRandomCard(String hasAttribute){
        try {
            JSONObject randomCard;
            do {
                randomCard = cards.getJSONObject(rand.nextInt(cards.length()));
                //Log.i("Card Name", randomCard.getString("name"));
            }while(!randomCard.has(hasAttribute) && randomCard.isNull(hasAttribute));
            return randomCard;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Returns a card that is not the same as the answer card
    public JSONObject getFalseAnswerCard(JSONObject ansCard, String type, String matchExtra) {
        JSONObject fakeCard;
        boolean match, extraMatch;
        do{
            fakeCard = getRandomCard(type);
            match = compareAttributes(ansCard, fakeCard, type);
            //Log.i("match", Boolean.toString(match));
            extraMatch = matchExtra.equals("") || compareAttributes(ansCard, fakeCard, matchExtra);
            //Log.i("extraMatch", Boolean.toString(extraMatch));
        }while (match || !extraMatch);
        return fakeCard;
    }

    public boolean compareAttributes(JSONObject ansCard, JSONObject fakeCard, String type){
        try {
            //Log.i("Comparing type", type);
            //Log.i("ansCard", ansCard.getString("name"));
            //Log.i("fakeCard", fakeCard.getString("name"));

            if (ansCard.optJSONArray(type) != null && ansCard.has(type) && fakeCard.optJSONArray(type)!=null && fakeCard.has(type)){
                JSONArray ansArray = ansCard.optJSONArray(type);
                JSONArray fakeArray = fakeCard.optJSONArray(type);

                for(int i=0; i<ansArray.length(); i++){
                    for(int j=0;j<fakeArray.length();j++){
                        //Log.i("Comparing", ansArray.getString(i));
                        //Log.i("To", fakeArray.getString(j));
                        if(ansArray.getString(i).equals(fakeArray.getString(j))){
                            return true;
                        }
                    }
                }
                return false;
            } else if(ansCard.has(type) && fakeCard.has(type)){
                if(type.equals("type")) {return formatDisplayText(ansCard, type).equals(formatDisplayText(fakeCard, type));}
                else                    {return ansCard.getString(type).equals(fakeCard.getString(type));}
            } else {
                if(!ansCard.has(type) && type.equals("colors")){
                    return !fakeCard.has(type);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    //Take the card name out of the rules text if it's a rules text question / remove {} from manaCost
    public String formatDisplayText(JSONObject card, String type){
        try {
            if (type.equals("text"))    {return card.getString("text").replace(card.getString("name"), "CARDNAME");}
            else if(type.equals("manaCost")){
                String mCost = card.getString("manaCost").replace("{","");
                return mCost.replace("}","");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }

    public JSONObject loadCardSet(String cardSetName) {
        try {
            InputStream in = getAssets().open(cardSetName + ".json");
            int size = in.available();
            byte[] buffer = new byte[size];

            in.read(buffer);
            in.close();
            String json = new String(buffer, "UTF-8");

            return new JSONObject(json);

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    /*public void testFunction(){
        ArrayList<JSONObject> cardholder = new ArrayList<>();
        try {
            for(int i=0; i < cards.length();i++){
                cardholder.add(cards.getJSONObject(i));
            }
            //Write test cases here
            JSONObject card = cardholder.get(73);
            Log.i("Card Name", card.getString("name"));
            String cardName = card.getString("name");
            String hiddenName = formatDisplayText(card, "text");
            if(cardName.contains(",")){
                String[] splitName = cardName.split(",");
                hiddenName = hiddenName.replace(splitName[0], "CARDNAME");
            }
            questionTextView.setText(hiddenName);

            Log.i("booster", booster.get(0).toString());

            for(int i = 0; i<booster.length(); i++){
                JSONArray card = booster.getJSONArray(i);
                Log.i("card", card.toString());
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Get the request set from the intent and load that file
        String setCode = getIntent().getStringExtra("setCode");
        String setName = "";

        JSONObject cardSet = loadCardSet(setCode);
        try {
            cards = cardSet.getJSONArray("cards");
            setName = cardSet.getString("name");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //Find views
        questionTextView = (TextView)findViewById(R.id.questionTextView);
        answerList = (ListView)findViewById(R.id.answerList);
        nextButton = (Button)findViewById(R.id.nextButton);

        //Set up ArrayAdapter and the Click Listener for the answers
        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, answerArray);
        answerList.setAdapter(arrayAdapter);

        answerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(position == correctPos){view.setBackgroundColor(Color.GREEN);}
                else{
                    view.setBackgroundColor(Color.RED);
                    parent.getChildAt(correctPos).setBackgroundColor(Color.GREEN);
                }
                answerList.setEnabled(false);
                nextButton.setVisibility(View.VISIBLE);
            }
        });

        //Initialize arrayList of question types
        questionTypes.clear();
        questionTypes.add("manaCost");
        questionTypes.add("power");
        questionTypes.add("toughness");
        questionTypes.add("flavor");
        questionTypes.add("text");
        if(setName.equals("Eldritch Moon") || setName.equals("Shadows over Innistrad")){
            questionTypes.add("layout");
        }

        setQuestionType(nextButton);
        //testFunction();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
