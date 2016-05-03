package com.xcode.mobile.smilealarm.listmanager;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.xcode.mobile.smilealarm.R;
import com.xcode.mobile.smilealarm.ViewHelper;
import com.xcode.mobile.smilealarm.weathermanager.WeatherHandler;

public class ViewToDoListActivity extends AppCompatActivity  {

    private static final String TAG = "ToDoList";

    protected GoogleApiClient mGoogleApiClient;
    protected Location mLastLocation;

    private TextView cityField, detailsField, currentTemperatureField, weatherIcon;
    private TextView mTime;
    private TextView mDate;
    private Typeface weatherFont;

    private WeatherHandler.placeIdTask asyncTask;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_todo_list);

        ViewHelper.setupToolbar(this);

        FloatingActionButton viewVFTCheckListBtn = (FloatingActionButton) findViewById(R.id.btn_finish);
        viewVFTCheckListBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
//                Intent vftCheckListActivity = new Intent(ViewToDoListActivity.this, ViewVFTCheckListActivity.class);
//                ViewToDoListActivity.this.startActivity(vftCheckListActivity);
            }
        });

        ToDoListHandler todoHdlr = new ToDoListHandler();
        ArrayList<Task> todoList = todoHdlr.getToDoList(false);
        if (todoList.isEmpty()) {
            todoList.add(new Task("NO ACTIVITY TODAY", null, null));
        }
        ListView todoLV = (ListView) findViewById(R.id.todolist);
        ToDoListAdapter todoAdpt = new ToDoListAdapter(this, R.layout.todo_list, todoList);
        todoLV.setAdapter(todoAdpt);

        weatherFont = Typeface.createFromAsset(getApplicationContext().getAssets(), "fonts/weathericons-regular-webfont.ttf");
        cityField = (TextView) findViewById(R.id.city_field);
        detailsField = (TextView) findViewById(R.id.details_field);
        currentTemperatureField = (TextView) findViewById(R.id.current_temperature_field);
        weatherIcon = (TextView) findViewById(R.id.weather_icon);
        weatherIcon.setTypeface(weatherFont);

        mTime = (TextView) findViewById(R.id.mTime);
        mDate = (TextView) findViewById(R.id.mDate);
        loadTimeDate();

         asyncTask = new WeatherHandler.placeIdTask(new WeatherHandler.AsyncResponse() {
            public void processFinish(String weather_city, String weather_description, String weather_temperature, String weather_humidity, String weather_pressure, String weather_updatedOn, String weather_iconText, String sun_rise) {
                cityField.setText(weather_city);
                detailsField.setText(weather_description);
                currentTemperatureField.setText(weather_temperature);
                weatherIcon.setText(Html.fromHtml(weather_iconText));
            }
        });

        //buildGoogleApiClient();
        asyncTask.execute("10.77515", "106.66040");
    }

    private void loadTimeDate() {
        Calendar today = Calendar.getInstance();
        int hour = today.get(Calendar.HOUR_OF_DAY);
        String minute = "";
        if (today.get(Calendar.MINUTE) > 9) {
            minute = "" + today.get(Calendar.MINUTE);
        } else {
            minute = "0" + today.get(Calendar.MINUTE);
        }
        String month = "";
        switch (today.get(Calendar.MONTH)) {
            case 1:
                month = "January";
                break;
            case 2:
                month = "February";
                break;
            case 3:
                month = "March";
                break;
            case 4:
                month = "April";
                break;
            case 5:
                month = "May";
                break;
            case 6:
                month = "June";
                break;
            case 7:
                month = "July";
                break;
            case 8:
                month = "August";
                break;
            case 9:
                month = "September";
                break;
            case 10:
                month = "October";
                break;
            case 11:
                month = "November";
                break;
            case 12:
                month = "December";
                break;
        }
        mTime.setText(hour + ":" + minute);
        mDate.setText(today.get(Calendar.DATE) + " , " + month + " " + today.get(Calendar.YEAR));
    }

//    protected synchronized void buildGoogleApiClient() {
//        mGoogleApiClient = new GoogleApiClient.Builder(this)
//                .addConnectionCallbacks(this)
//                .addOnConnectionFailedListener(this)
//                .addApi(LocationServices.API)
//                .build();
//    }

//    @Override
//    protected void onStart() {
//        super.onStart();
//        mGoogleApiClient.connect();
//    }
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//        if (mGoogleApiClient.isConnected()) {
//            mGoogleApiClient.disconnect();
//        }
//    }
//
//    /**
//     * Runs when a GoogleApiClient object successfully connects.
//     */
//    @Override
//    public void onConnected(Bundle connectionHint) {
//        // Provides a simple way of getting a device's location and is well suited for
//        // applications that do not require a fine-grained location and that do not need location
//        // updates. Gets the best and most recent location currently available, which may be null
//        // in rare cases when a location is not available.
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return;
//        }
//        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
//        if (mLastLocation != null) {
//            asyncTask.execute(String.valueOf(mLastLocation.getLatitude()), String.valueOf(mLastLocation.getLongitude())); //  asyncTask.execute("Latitude", "Longitude")
//        } else {
//            Log.i(TAG, "Location is not found");
//        }
//    }
//
//    @Override
//    public void onConnectionSuspended(int cause) {
//        // The connection to Google Play services was lost for some reason. We call connect() to
//        // attempt to re-establish the connection.
//        Log.i(TAG, "Connection suspended");
//        mGoogleApiClient.connect();
//    }
//
//    @Override
//    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
//        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
//        // onConnectionFailed.
//        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode());
//    }

    public void onBackPressed() {
        finish();
    }

}

