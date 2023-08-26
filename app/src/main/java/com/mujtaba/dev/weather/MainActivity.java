package com.mujtaba.dev.weather;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.mujtaba.dev.weather.databinding.ActivityMainBinding;

import android.Manifest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity implements LocationListener {
    ActivityMainBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        showProgressLayout();
        getLocation();


        binding.btnSearchWeather.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SearchWeather.class));
            }
        });


        // handler for updating time ui
//        Handler handler = new Handler();
//        Runnable updateTimeRunnable = new Runnable() {
//            @Override
//            public void run() {
//                showCurrentDateTime(); // Update the date and time
//                handler.postDelayed(this, 3000);
//            }
//        };
//        handler.post(updateTimeRunnable);

        // getting permission of location on run time
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION
            },100);
        }

    }



    // method for getting location
    @SuppressLint("MissingPermission")
    private void getLocation(){

        try {
            LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,5000,5,MainActivity.this);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    // method for fetching data from api
    @SuppressLint("SetTextI18n")
    public void fetchData(double latitude, double longitude) {


        String apiKey = "f3bd0c2f6398ce8334080b9ef42b0272";
        String apiUrl = "https://api.openweathermap.org/data/2.5/weather?lat=" + latitude + "&lon=" + longitude + "&appid=" + apiKey;

            // make api call
            RequestQueue queue = Volley.newRequestQueue(this);
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, apiUrl, null, new Response.Listener<JSONObject>() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onResponse(JSONObject response) {
                    // handle response
                    try {

                        JSONObject object = response.getJSONObject("main");
                        String temperature = object.getString("temp");
                        Double temp = Double.parseDouble(temperature) - 273.15;
                        String feelsLike = object.getString("feels_like");
                        Double feels = Double.parseDouble(feelsLike) - 273.15;
                        String max = object.getString("temp_max");
                        Double tempMax = Double.parseDouble(max) - 273.15;
                        String min = object.getString("temp_min");
                        Double tempMin = Double.parseDouble(min) - 273.15;
                        String cityName = response.getString("name");
                        String humidity = object.getString("humidity");
                        String pressure = object.getString("pressure");
                        int visibilityMeters = response.getInt("visibility");
                        double visibilityKilometers = visibilityMeters / 1000.0;


                        JSONObject jsonObject = response.getJSONObject("wind");
                        String wind = jsonObject.getString("speed");

                        JSONObject sys = response.getJSONObject("sys");
                        long sunrise = Long.parseLong(sys.getString("sunrise"));
                        long sunset = Long.parseLong(sys.getString("sunset"));

                        Date sunriseDate = new Date(sunrise * 1000L);
                        Date sunsetDate = new Date(sunset * 1000L);
                        // Format the dates
                        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
                        sdf.setTimeZone(TimeZone.getTimeZone("UTC")); // Set the time zone to UTC

                        JSONArray weatherArray = response.getJSONArray("weather");
                        if (weatherArray.length() > 0) {
                            JSONObject weatherObject = weatherArray.getJSONObject(0);
                            String weatherDescription = weatherObject.getString("description");

                            // Set the weather description in the TextView
                            binding.tvWeatherCondition.setText(weatherDescription);
                        }
                        String sunriseTime = sdf.format(sunriseDate);
                        String sunsetTime = sdf.format(sunsetDate);
                        binding.tvSunrise.setText(sunriseTime);
                        binding.tvSunset.setText(sunsetTime);


                        binding.tvName.setText(cityName);
                        binding.tvResponse.setText(temp.toString().substring(0, 2) + "°C");
                        binding.tvFeelsLike.setText("Feels like:\n"+feels.toString().substring(0, 2) + "°C");
                        binding.tvMaxMin.setText("Max: "+tempMax.toString().substring(0, 2) + "°C"+" ~ Min: "+tempMin.toString().substring(0, 2) + "°C");
                        binding.tvWind.setText("Wind: "+ wind.substring(0,3)+"m/s");
                        binding.tvHumidity.setText(humidity+"%");
                        binding.tvPressure.setText(pressure+"mb");
                        binding.tvVisibility.setText(visibilityKilometers+"km");


                        hideProgressLayout();

                    } catch (JSONException e) {
                        Toast.makeText(MainActivity.this, "Sorry server issue :(", Toast.LENGTH_SHORT).show();
                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // handle error
//                    binding.tvResponse.setText("City not found :(\nPlease check your spellings!");
                    Toast.makeText(MainActivity.this, "City not found :(\nPlease check your spellings!", Toast.LENGTH_SHORT).show();
                }
            });

            queue.add(request);
        }

//    private void showCurrentDateTime() {
//        SimpleDateFormat dateFormat = new SimpleDateFormat("E, MMM yyyy hh:mma", Locale.getDefault());
//        SimpleDateFormat dateOnlyFormat = new SimpleDateFormat("E, MMM dd, yyyy", Locale.getDefault());
//        SimpleDateFormat timeOnlyFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
//
//        Date currentDate = new Date();
//
//        String formattedDate = dateOnlyFormat.format(currentDate);
//        String formattedTime = timeOnlyFormat.format(currentDate);
//
//
//        binding.tvDate.setText(formattedDate);
//        binding.tvTime.setText(formattedTime);
//    }


    @Override
    public void onLocationChanged(@NonNull Location location) {

        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        binding.tvCurrentLocation.setText("");
        fetchData(latitude,longitude);
//        try {
//
//            Geocoder geocoder = new Geocoder(MainActivity.this,Locale.getDefault());
//            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);
//            String address = addresses.get(0).getAddressLine(0);
//            Toast.makeText(this, "success"+address, Toast.LENGTH_SHORT).show();
//            tvCurrentLocation.setText("Address: "+address);
//        }catch (Exception e){
//            e.printStackTrace();
//        }

    }

    @Override
    public void onLocationChanged(@NonNull List<Location> locations) {
        LocationListener.super.onLocationChanged(locations);
    }

    @Override
    public void onFlushComplete(int requestCode) {
        LocationListener.super.onFlushComplete(requestCode);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        LocationListener.super.onStatusChanged(provider, status, extras);
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        LocationListener.super.onProviderEnabled(provider);
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        LocationListener.super.onProviderDisabled(provider);
    }

    private void showProgressLayout() {
        binding.progressLayout.setVisibility(View.VISIBLE);
    }

    private void hideProgressLayout() {
        binding.progressLayout.setVisibility(View.GONE);
    }
}




