package com.example.weatherapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PatternMatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.CollationElementIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.xml.transform.ErrorListener;

public class MainActivity extends AppCompatActivity {
    private RelativeLayout homeRL;
    private ProgressBar loadingPB;
    private TextView cityNameTV,temperatureTV,conditionTV,translatedTV;
    private RecyclerView weatherRV;
    private TextInputEditText cityEdt;
    private ImageView backIV,iconIV,searchIV;
    private ArrayList<weatherRVmodal> weatherRVmodalArrayList;
    private weatherRVAdapter weatherRVAdapter;
    private LocationManager locationManager;
    private int PERMISSION_CODE = 1;
    private String cityName;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        setContentView(R.layout.activity_main);
        homeRL = findViewById(R.id.IDRLHome);
        loadingPB = findViewById(R.id.idLoading);
        cityNameTV = findViewById(R.id.IDTVCityname);
        temperatureTV = findViewById(R.id.IDTVTemperature);
        conditionTV = findViewById(R.id.IDTVCondition);

        weatherRV = findViewById(R.id.IDRVWeather);

        cityEdt = findViewById(R.id.IDEditcity);
        backIV = findViewById(R.id.idIVBlack);
        iconIV = findViewById(R.id.IDIVIcon);
        searchIV = findViewById(R.id.IDIVSearch);


        weatherRVmodalArrayList = new ArrayList<>();
        weatherRVAdapter = new weatherRVAdapter(this,weatherRVmodalArrayList);
        weatherRV.setAdapter(weatherRVAdapter);



        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},PERMISSION_CODE);
        }
        Location location = locationManager.getLastKnownLocation(locationManager.NETWORK_PROVIDER);
//        cityName = getCityName(location.getLongitude(),location.getLatitude());

        searchIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String location = cityEdt.getText().toString();
                new FetchWeatherTask().execute(location);

            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==PERMISSION_CODE){
            if (grantResults.length > 0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this,"Permission Granted...",Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(this,"Pleas provide the Permission",Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private String getCityName(double longitude, double latitude){
        String cityName ="Not Found";
        Geocoder gcd = new Geocoder(getBaseContext(),Locale.getDefault());
        try {
            List<Address> addresses = gcd.getFromLocation(latitude,longitude,10);
            for (Address adr : addresses){
                if (adr!=null){
                    String city= adr.getLocality();
                    if (city!=null && !city.equals("")){
                        cityName = city;
                    }
                    else {
                        Log.d("TAG","City Not Found..");
                        Toast.makeText(this,"User City Not Found..",Toast.LENGTH_SHORT).show();
                    }

                }
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }
        return cityName;

    }


    private class FetchWeatherTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String...locations) {
            String location = locations[0];
            String weatherApiUrl = "https://api.weatherapi.com/v1/forecast.json?key=cf16f7f4608d41a3ba6190005230706&q="+location+"&days=1&aqi=yes&alerts=yes"; // Replace with the actual API URL

            try {
                URL url = new URL(weatherApiUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                reader.close();
                return response.toString();

            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String jsonResponse) {
            weatherRVmodalArrayList.clear();
            if (jsonResponse != null) {
                try {
                    JSONObject jsonObject = new JSONObject(jsonResponse);
                    JSONObject currentWeather = jsonObject.getJSONObject("current");
                    double temperatureC = currentWeather.getDouble("temp_c");
                    String location = jsonObject.getJSONObject("location").getString("name");
                    String conditionText = currentWeather.getJSONObject("condition").getString("text");
                    String conditionicon = currentWeather.getJSONObject("condition").getString("icon");

                    Picasso.get().load("https://"+conditionicon).into(iconIV);

                    switch (conditionText) {
                        case "Mist":
                            Picasso.get().load("https://images.unsplash.com/photo-1585508889431-a1d0d9c5a324?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=2564&q=80").into(backIV);
                            break;
                        case "Sunny":
                            Picasso.get().load("https://images.unsplash.com/photo-1552838671-4c793a745d03?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=687&q=80").into(backIV);
                            break;
                        case "Partly cloudy":
                        case "Cloudy":
                            Picasso.get().load("https://images.unsplash.com/photo-1534088568595-a066f410bcda?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=751&q=80").into(backIV);
                            break;
                        case "Rainy":
                        case "Light rain":
                            Picasso.get().load("https://images.unsplash.com/photo-1534274988757-a28bf1a57c17?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=870&q=80").into(backIV);
                            break;
                        default:
                            Picasso.get().load("https://images.unsplash.com/photo-1513628253939-010e64ac66cd?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=687&q=80").into(backIV);
                            break;
                    }

                    // Display the temperature in TextView
                    @SuppressLint("StringFormatMatches") String temperatureString = getString(R.string.current_temperature, temperatureC);
                    temperatureTV.setText(temperatureString);
                    cityNameTV.setText(location);
                    conditionTV.setText(conditionText);


                    JSONObject forcastobj = new JSONObject(jsonResponse);
                    JSONObject currentforecast = forcastobj.getJSONObject("forecast");
                    JSONObject forecastDay = currentforecast.getJSONArray("forecastday").getJSONObject(0);
                    JSONArray hourArr = forecastDay.getJSONArray("hour");


                    for (int i = 0; i < hourArr.length(); i++) {
                        JSONObject hourObj = hourArr.getJSONObject(i);
                        String time = hourObj.getString("time");
                        String temper = hourObj.getString("temp_c");
                        String img = hourObj.getJSONObject("condition").getString("icon");
                        String wind = hourObj.getString("wind_kph");
                        String cond = hourObj.getJSONObject("condition").getString("text");
                        weatherRVmodalArrayList.add(new weatherRVmodal(time, temper, img, wind,cond));

                    }

                    weatherRVAdapter.notifyDataSetChanged();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}