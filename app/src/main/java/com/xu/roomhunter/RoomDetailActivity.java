package com.xu.roomhunter;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class RoomDetailActivity extends AppCompatActivity {

    public RecyclerView peopleRecyclerView;
    public PeopleAdapter mAdapter;
    public PeopleAdapter swapAdapter;
    ArrayList<String>peoplenames;
    ArrayList<String>peopleunis;
    String JSON;
    TextView roomIDView;
    TextView bedroomView;
    TextView priceView;
    String id;
    String interestfetch;

    Button interested;
    boolean interest;
    SharedPreferences sp;
    SharedPreferences.Editor edit;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent =getIntent();
        id =intent.getStringExtra("id");

        roomIDView = (TextView)findViewById(R.id.roomIDView);
        bedroomView =(TextView)findViewById(R.id.bedroomView);
        priceView = (TextView)findViewById(R.id.priceView);

       interested = (Button)findViewById(R.id.btnInterested);


        interested.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View V){
                showInterest();

            }
        });

        peopleRecyclerView = (RecyclerView)findViewById(R.id.recycler );
        peopleRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.HORIZONTAL));
        // Create a grid based view, shoving 3 items per row!
        peopleRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        peopleRecyclerView.setItemAnimator(new DefaultItemAnimator());

        JSONGet jsontask  = new JSONGet();
        jsontask.execute("http://b116.ml/roomhunt/api/person-interested-in-list?flat_id="+id+"&person_id="+"5");







    }

    public void showInterest(){
       // sendPOST(); //Only enable if you wnt to add a user to sqlitedb
        if (interestfetch.equals("false")){
            sendInterest();
           interested.setText("I am no longer interested");

        }else if(interestfetch.equals("true")){
            deleteInterest();
            interested.setText("I am interested");



        }

        mAdapter.notifyDataSetChanged();
        //swapAdapter = new PeopleAdapter(this, peoplenames,peopleunis);

    }

    public void loadAdapter(){
        if(mAdapter==null){
            //bind topics to adapter, then to recyclerview
            mAdapter = new PeopleAdapter(RoomDetailActivity.this,peoplenames,peopleunis);
            peopleRecyclerView.setAdapter(mAdapter);
        }else{
            mAdapter.notifyDataSetChanged();
        }
    }

    public void loadFakeData(){
        peoplenames.add("Adrian");
        peopleunis.add("ucl");
    }
    public static final int MY_PERMISSIONS_REQUEST_INTERNET=98;

    public boolean checkInternetPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[] {android.Manifest.permission.INTERNET}, MY_PERMISSIONS_REQUEST_INTERNET);

        }
        return true;
    }


    private class peopleHolder extends RecyclerView.ViewHolder implements View.OnClickListener{


        TextView username;
        TextView useruni;
        ImageView profile;
        String url;


        public peopleHolder(View itemView){
            super(itemView);

            username = (TextView)itemView.findViewById(R.id.nameView);
            useruni= (TextView)itemView.findViewById(R.id.uniView);

            itemView.setOnClickListener(this);
        }
        @Override
        public void onClick(View v){
            /*old code
            String YourPageURL = "https://www.facebook.com/public/";
            String appendUrl = turnIntoUriQuery(username.getText().toString());
            String finalUri = YourPageURL+appendUrl;
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(finalUri));

            startActivity(browserIntent);
            */
            Intent viewFB = newFacebookIntent(getPackageManager(),useruni.getText().toString());
            startActivity(viewFB);

        }

        public String  turnIntoUriQuery (String name){

            String lastName = "";
            String firstName= "";
            if(name.split("\\w+").length>1){

                lastName = name.substring(name.lastIndexOf(" ")+1);
                firstName = name.substring(0, name.lastIndexOf(' '));
            }
            else{
                firstName = name;
            }

            String answer = firstName+"-"+lastName;
            return answer;
        }
        //TODO new method to replace simple webviewer
        public Intent newFacebookIntent(PackageManager pm, String url) {
            Uri uri = Uri.parse(url);
            try {
                ApplicationInfo applicationInfo = pm.getApplicationInfo("com.facebook.katana", 0);
                if (applicationInfo.enabled) {
                    // http://stackoverflow.com/a/24547437/1048340
                    uri = Uri.parse("fb://facewebmodal/f?href=" + url);
                }
            } catch (PackageManager.NameNotFoundException ignored) {
            }
            return new Intent(Intent.ACTION_VIEW, uri);
        }



    }


    private class PeopleAdapter extends RecyclerView.Adapter<peopleHolder>{
        private ArrayList<String>  users;
        private ArrayList <String> unis;
        private Context mContext;


        public PeopleAdapter(Context context, ArrayList<String>  user, ArrayList<String> uni){
            this.mContext =context;
            this.users=user;
            this.unis =uni;
        }
        @Override
        public peopleHolder onCreateViewHolder(ViewGroup parent, int viewType){
            LayoutInflater layoutinflater = LayoutInflater.from(mContext);

            View view = layoutinflater.inflate(R.layout.row, parent,false);
            return new peopleHolder(view);
        }

        //bind data to holder TODO upgraded with SQLite Handling data, see changelog for original

        public void onBindViewHolder(peopleHolder holder, int position){

            holder.username.setText(users.get(position));
            holder.useruni.setText(unis.get(position));
            //TODO add in images

        }
        @Override
        public int getItemCount(){ return users.size();}
    }
    public ArrayList<String> parseNameJSONintoArrayList(String json){
        ArrayList<String>nameList = new ArrayList<String>();
        try{
            JSONArray array = new JSONArray(json);
            JSONObject response = array.getJSONObject(0);

            JSONArray people = response.getJSONArray("personList");

            for(int i =0;i<people.length();i++){
                JSONObject object = people.getJSONObject(i);
                String name =object.getString("name");

                nameList.add(name);

                /*Toast toast =Toast.makeText(this,name,Toast.LENGTH_SHORT);
                toast.show();
                */

            }

        }catch (org.json.JSONException e){
            e.printStackTrace();
        }

        return nameList;


    }

    public ArrayList<String> parseUnisJSONintoArrayList(String json){
        ArrayList<String>uniList = new ArrayList<String>();
        try{
            JSONArray array = new JSONArray(json);
            JSONObject response = array.getJSONObject(0);

            JSONArray people = response.getJSONArray("personList");

            for(int i =0;i<people.length();i++){
                JSONObject object = people.getJSONObject(i);
                String url =object.getString("url");

                uniList.add(url);

                /*Toast toast =Toast.makeText(this,name,Toast.LENGTH_SHORT);
                toast.show();
                */

            }

        }catch (org.json.JSONException e){
            e.printStackTrace();
        }

        return uniList;


    }
    public void parsePriceAndBedrooms(String json){
        try{
            JSONArray array = new JSONArray(json);
            JSONObject response = array.getJSONObject(0);

            String  price = response.getString("price");

             String bedrooms = response.getString("bedroomNo");
            interestfetch = response.getString("interested");


            roomIDView.setText("Details for property "+id);

            bedroomView.setText("Number of bedrooms: "+bedrooms);
            priceView.setText("Rent: £ "+price);




        }catch (org.json.JSONException e){
            e.printStackTrace();
        }


    }
    //https://www.kompulsa.com/how-to-send-a-post-request-in-android/
    public void sendPOST() {
        RequestQueue MyRequestQueue = Volley.newRequestQueue(this);
        String url = "http://b116.ml/roomhunt/api/add-person";
        StringRequest MyStringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //This code is executed if the server responds, whether or not the response contains data.
                //The String 'response' contains the server's response.
            }
        }, new Response.ErrorListener() { //Create an error listener to handle errors appropriately.
            @Override
            public void onErrorResponse(VolleyError error) {
                //This code is executed if there is an error.
            }
        }) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<String, String>();
                MyData.put("name", "Adrian Xu");
                MyData.put("picture","itssomething.jpg");
                MyData.put("url","http://facebook.com");//Add the data you'd like to send to the server.
                return MyData;
            }
        };


        MyRequestQueue.add(MyStringRequest);
    }
    public void sendInterest() {
        RequestQueue MyRequestQueue = Volley.newRequestQueue(this);
        String url = "http://b116.ml/roomhunt/api/add-interest";
        StringRequest MyStringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //This code is executed if the server responds, whether or not the response contains data.
                //The String 'response' contains the server's response.
            }
        }, new Response.ErrorListener() { //Create an error listener to handle errors appropriately.
            @Override
            public void onErrorResponse(VolleyError error) {
                //This code is executed if there is an error.
            }
        }) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<String, String>();
                MyData.put("flat_id",id);
                MyData.put("person_id", "5");//Add the data you'd like to send to the server.
                return MyData;
            }
        };


        MyRequestQueue.add(MyStringRequest);
    }

    public void deleteInterest() {
        RequestQueue MyRequestQueue = Volley.newRequestQueue(this);
        String url = "http://b116.ml/roomhunt/api/add-interest";
        StringRequest MyStringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //This code is executed if the server responds, whether or not the response contains data.
                //The String 'response' contains the server's response.
            }
        }, new Response.ErrorListener() { //Create an error listener to handle errors appropriately.
            @Override
            public void onErrorResponse(VolleyError error) {
                //This code is executed if there is an error.
            }
        }) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<String, String>();
                MyData.put("flat_id",id);
                MyData.put("delete","true");
                MyData.put("person_id","5");//Add the data you'd like to send to the server.
                return MyData;
            }
        };


        MyRequestQueue.add(MyStringRequest);
    }




    //Inner class for fetching json
    public class JSONGet extends AsyncTask<String, String, String> {
        private ProgressDialog pdialog;

        protected void onPreExecute() {
            super.onPreExecute();
            checkInternetPermission();
            pdialog= new ProgressDialog(RoomDetailActivity.this);
            pdialog.setMessage("Fetching Property Details...");
            pdialog.show();



        }

        protected String doInBackground(String... params) {


            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();


                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line+"\n");
                    Log.d("Response: ", "> " + line);   //here u ll get whole response...... :-)

                }

                return buffer.toString();


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            pdialog.cancel();

            JSON = result;
            peoplenames= parseNameJSONintoArrayList(JSON);
            peopleunis = parseUnisJSONintoArrayList(JSON);
            parsePriceAndBedrooms(JSON);


            loadAdapter();

            //Set the initial value  of the interested button
            if (interestfetch.equals("false")){
                interested.setText("I am interested");

            }else if(interestfetch.equals("true")){

                interested.setText("I am no longer interested");



            }



            //add in unis






        }
    }


}
