package com.example.user.tulong;

import android.app.Activity;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

/**
 * Created by LIA on 02-Apr-16.
 */
public class HubungiActivity extends Activity {
    TextView activityTitle;
    String optionType;
    TextView textResult;
    TextView textPlaceResult;
    ListView lvHasil;
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hubungi);
        setTitle("Menghubungi");

        textResult = (TextView) findViewById(R.id.textResult);
        textPlaceResult = (TextView) findViewById(R.id.textPlaceResult);
        activityTitle = (TextView) findViewById(R.id.textView);
        activityTitle.setText("Hey");
        lvHasil = (ListView) findViewById(R.id.listView);

        Bundle bundle = getIntent().getExtras();

        optionType = bundle.getString("option");
        getLocation();
        Toast.makeText(getApplicationContext(), "Mendeteksi lokasi...", Toast.LENGTH_SHORT).show();
    }

    public void getLocation(){
        Log.d("test", "getting location");
        GPSTracker gps = new GPSTracker(this);

            if(gps.canGetLocation()){
            final Location location = gps.getLocation();

            PlacesFetcher place = new PlacesFetcher();
            place.execute(location);

        }else{
            gps.showSettingsAlert();
        }
    }


    private class PlacesFetcher extends AsyncTask<Location, Integer, String[]>{

        @Override
        protected String []doInBackground(Location... params) {

            Location location = params[0];

            double lat = location.getLatitude();
            double lng = location.getLongitude();

            String apiKey = "AIzaSyCzuw8pBhNhb6nFdru4ULxTRAzFTJknbiE";
            int radius = 1000;


            String [] tempResult = searchPlaces(radius, apiKey, lat, lng);

            return tempResult;
        }

        private String[] searchPlaces(int radius, String apiKey, double lat, double lng){
            StringBuffer bufferPlaces = null;
            String urlSearchPlaces ="https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
                    "location="+lat+","+lng+"&radius="+radius+"&types="+optionType+"&key="+apiKey;

            Log.d("test", "url:"+urlSearchPlaces);

            try {
                URL urlPlaces = new URL(urlSearchPlaces);
                HttpURLConnection connection = (HttpURLConnection) urlPlaces.openConnection();
                InputStream inputStream = connection.getInputStream();
                BufferedReader readerPlaces = new BufferedReader(new InputStreamReader(inputStream));
                bufferPlaces = new StringBuffer();
                String linePlaces = "";

                while ((linePlaces = readerPlaces.readLine())!=null){
                    bufferPlaces.append(linePlaces);
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            String [] tempResult = null;


            //LOCATION ON GOOGLE WEB SERVICE IS LAT/LONG, PLEASE NOTICE
            //DO NOT REPEAT THE SAME MISTAKE

            String resultPlace = bufferPlaces.toString();

            try {
                Log.d("test", "creating JSONObject etc");
                JSONObject objectPlace = new JSONObject(resultPlace);
                JSONArray arrayPlace = objectPlace.getJSONArray("results");
                Log.d("test", "number of place :"+arrayPlace.length());

                String status = objectPlace.getString("status");

                tempResult = new String[arrayPlace.length()];

                if(status!="OK"){
                    String returnError =objectPlace.getString("status");
                }

                int placeCount=0;

                Log.d("test", "entering fetching data");
                for(int i=0; i<arrayPlace.length(); i++){
                    JSONObject place = arrayPlace.getJSONObject(i);
                    String placeName = place.getString("name");
                    String placeId = place.getString("place_id");

                    String placeDetails = getPlaceDetails(placeId, i);
                    if(placeDetails!=""){
                        tempResult[i] = placeName+"\n"+placeDetails;
                        placeCount++;
                        Log.d("test", "placeName:"+placeName);
                    }else{
                        tempResult[i] = "";
                    }

                }

                if(placeCount==0){

                    Log.d("test", "EMPTY RESULT");
                    radius = radius+500;
                    Log.d("test", "RADIUS :"+radius);

                    if(radius<=40000){
                        return searchPlaces(radius, apiKey, lat, lng);
                    }else{
                        tempResult[0]="Maaf, tidak ada lagi tempat terdekat dengan Anda dalam radius 40 km.";
                        return tempResult;
                    }

                }

            } catch (JSONException e) {
                Log.d("test", "Error: "+e.toString());
                e.printStackTrace();
            }
            return tempResult;
        }


        private String getPlaceDetails (String placeId, int param){
            int i = param;
            String id = placeId;

            StringBuffer bufferDetails = null;
            String apiKey = "AIzaSyCzuw8pBhNhb6nFdru4ULxTRAzFTJknbiE";
            String urlSearchDetails="https://maps.googleapis.com/maps/api/place/details/json?placeid="+id+"&key="+apiKey;

            try {
                URL urlDetails = new URL(urlSearchDetails);
                HttpURLConnection connection = (HttpURLConnection)urlDetails.openConnection();
                InputStream inputStream = connection.getInputStream();
                BufferedReader readerDetails = new BufferedReader(new InputStreamReader(inputStream));
                bufferDetails = new StringBuffer();
                String lineDetails="";

                while((lineDetails=readerDetails.readLine())!=null){
                    bufferDetails.append(lineDetails);
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            String details = bufferDetails.toString();

            StringBuffer finalResult = new StringBuffer();
            try {
                JSONObject objectDetails = new JSONObject(details);
                JSONObject objectResult = (JSONObject) objectDetails.get("result");

                String address = (String) objectResult.get("formatted_address");
                String phone = (String) objectResult.get("international_phone_number");

//                JSONObject geometry = (JSONObject) objectResult.get("geometry");
//                JSONObject objectLocation = (JSONObject) geometry.get("location");
//                Double placeLat = (Double) objectLocation.get("lat");
//                Double placeLng = (Double) objectLocation.get("lng");

                if(phone.isEmpty()){
                    finalResult.append("");

                }else{

                    Log.d("test", "EXISTS PHONE");
                    finalResult.append(phone);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return finalResult.toString();


        }

        @Override
        protected void onPostExecute(String []s) {
            super.onPostExecute(s);
            textResult.setText("Selesai... Data sudah diambil.");

            int r, w;
            final int n = r = w = s.length;
            while (r > 0) {
                final String newArray = s[--r];
                if (!newArray.equals("")) {
                    s[--w] = newArray;
                }
            }
//
//
//            for(int i=0; i<s.length;i++){
//                //textPlaceResult.append(s[i]+"\n");
//                if(s[i]==""){
//
//                    Log.d("test", (i+1)+"KOSONG");
//                }
//            }

            adapter = new ArrayAdapter<String>(HubungiActivity.this, android.R.layout.simple_list_item_1, Arrays.copyOfRange(s, w, n));
            lvHasil.setAdapter(adapter);

            Log.d("test", "DONE!");
        }
    }
}
