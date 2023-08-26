package com.mujtaba.dev.weather;

import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.mujtaba.dev.weather.databinding.ActivitySearchWeatherBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class SearchWeather extends AppCompatActivity {

    ActivitySearchWeatherBinding binding;
    Handler handler;
    Runnable updateTimeRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchWeatherBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //setting click listener on button search
        binding.btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyBoard();
                fetchData();
            }
        });

        // handler for updating time ui
//        handler = new Handler();
//        updateTimeRunnable = new Runnable() {
//            @Override
//            public void run() {
//                showCurrentDateTime(); // Update the date and time
//                handler.postDelayed(this, 3000);
//            }
//        };
//        handler.post(updateTimeRunnable);

        binding.ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SearchWeather.this, MainActivity.class);
                startActivity(intent);
            }
        });

    }

    // method for fetching data from api
    @SuppressLint("SetTextI18n")
    public void fetchData() {

        String cityName = binding.etSearch.getText().toString();
        binding.etSearch.setText("");
        if (cityName.isEmpty()){
            binding.etSearch.setError("please enter city name!");
            hideProgressLayout();
        }else {

            showProgressLayout();
            String apiKey = "f3bd0c2f6398ce8334080b9ef42b0272";
            String apiUrl = "https://api.openweathermap.org/data/2.5/weather?q=" + cityName + "&appid=" + apiKey;

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

                        JSONArray weatherArray = response.getJSONArray("weather");
                        if (weatherArray.length() > 0) {
                            JSONObject weatherObject = weatherArray.getJSONObject(0);
                            String weatherDescription = weatherObject.getString("description");

                            // Set the weather description in the TextView
                            binding.tvWeatherCondition.setText(weatherDescription);
                        }

                        JSONObject sys = response.getJSONObject("sys");
                        long sunrise = Long.parseLong(sys.getString("sunrise"));
                        long sunset = Long.parseLong(sys.getString("sunset"));

                        Date sunriseDate = new Date(sunrise * 1000L);
                        Date sunsetDate = new Date(sunset * 1000L);
                        // Format the dates
                        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
                        sdf.setTimeZone(TimeZone.getTimeZone("UTC")); // Set the time zone to UTC

                        String sunriseTime = sdf.format(sunriseDate);
                        String sunsetTime = sdf.format(sunsetDate);
                        binding.tvSunrise.setText(sunriseTime);
                        binding.tvSunset.setText(sunsetTime);


                        binding.tvName.setText(cityName);
                        binding.tvResponse.setText(temp.toString().substring(0, 2) + "°C");
                        binding.tvFeelsLike.setText("Feels like "+feels.toString().substring(0, 2) + "°C");
                        binding.tvMaxMin.setText("Max: "+tempMax.toString().substring(0, 2) + "°C"+" ~ Min: "+tempMin.toString().substring(0, 2) + "°C");
                        binding.tvWind.setText("Wind: "+ wind.substring(0,3)+"m/s");
                        binding.tvHumidity.setText(humidity+"%");
                        binding.tvPressure.setText(pressure+"mb");
                        binding.tvVisibility.setText(visibilityKilometers+"km");

                        hideProgressLayout();

                    } catch (JSONException e) {
                        Toast.makeText(SearchWeather.this, "Error while fetching data", Toast.LENGTH_SHORT).show();
                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // handle error
                    binding.tvResponse.setText("...");
                    binding.tvName.setText("...");
//                    binding.tvResponse.setText("City not found :(\nPlease check your spellings!");
                    Toast.makeText(SearchWeather.this, "city not found", Toast.LENGTH_SHORT).show();
                }
            });

            queue.add(request);
        }
    }
    public void hideKeyBoard(){
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(binding.etSearch.getWindowToken(), 0);
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

    private void showProgressLayout() {
        binding.progressLayout.setVisibility(View.VISIBLE);
    }

    private void hideProgressLayout() {
        binding.progressLayout.setVisibility(View.GONE);
    }

}