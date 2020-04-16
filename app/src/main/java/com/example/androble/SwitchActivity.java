package com.example.androble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SwitchActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {
    private Handler mHandler = new Handler();

    Button onButton, offButton, checkoutButton;
    TextView datamotor, dataUser;
    SharedPreferences sharedpreferences;
    Switch switch1;

    public final static String TAG_NAMA = "nama";
    public final static String TAG_EMAIL = "email";
    String email, nama;
    public static final String url = ServerTandebike.checkoutBooking;
    public static final String urls = ServerTandebike.engineStarter;
    public static final String TAG_NOPLATE = "noPlate";
    public static final String TAG_BIKETYPE = "bikeType";
    public static final String TAG_BIKEMERK = "bikeMerk";
    public static final String TAG_ID = "id";
    public static final String TAG_IDMOTOR = "idMotor";


    String noPlate, bikeType, bikeMerk;

    public static final String my_shared_preferences = "my_shared_preferences";
    private String id, idMotor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_switch);

        dataUser = (TextView) findViewById(R.id.dataUser);
        datamotor = (TextView) findViewById(R.id.dataMotor);
//        onButton = (Button) findViewById(R.id.onButton);
//        offButton = (Button) findViewById(R.id.offButton);
        checkoutButton = (Button) findViewById(R.id.checkoutButton);
        switch1 = (Switch)findViewById(R.id.switch1);
        switch1.setOnCheckedChangeListener(this);


        loadSharedPref();


        checkoutButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.checkoutButton) {
                    sharedpreferences = getSharedPreferences(my_shared_preferences, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.remove("noPlate");
                    editor.commit();
                    sendCheckout(noPlate);
                    checkOutBT();
                    //deleteBooking();
                    Toast.makeText(SwitchActivity.this, noPlate, Toast.LENGTH_SHORT).show();
                    finish();
                    Intent i = new Intent(SwitchActivity.this, ChooseVehicle.class);
                    startActivity(i);
                }
            }
        });
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

        if(switch1.isChecked()){
            TurnOn();
            StringRequest strReq = new StringRequest(Request.Method.POST, urls, new Response.Listener<String>() {

                @Override
                public void onResponse(String response) {

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            }){
                @Override
                protected Map<String, String> getParams()  {
                    Map<String,String>parms= new HashMap<>();
                    parms.put("noPlate",noPlate);
                    parms.put("status","ON");

                    return parms;
                }
            };
            RequestQueue requestQueue= Volley.newRequestQueue(getApplicationContext());
            requestQueue.add(strReq);
            Toast.makeText(SwitchActivity.this, "ON", Toast.LENGTH_SHORT).show();

        }else{
            TurnOff();
            StringRequest strReq = new StringRequest(Request.Method.POST, urls, new Response.Listener<String>() {

                @Override
                public void onResponse(String response) {

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            }){
                @Override
                protected Map<String, String> getParams()  {
                    Map<String,String>parms= new HashMap<>();
                    parms.put("noPlate",noPlate);
                    parms.put("status","OFF");
                    return parms;
                }
            };
            RequestQueue requestQueue= Volley.newRequestQueue(getApplicationContext());
            requestQueue.add(strReq);
            Toast.makeText(SwitchActivity.this, "OFF", Toast.LENGTH_SHORT).show();

        }
    }

    public void loadSharedPref() {
        sharedpreferences = getSharedPreferences(my_shared_preferences, Context.MODE_PRIVATE);

        email = sharedpreferences.getString(TAG_EMAIL, null);
        nama = sharedpreferences.getString(TAG_NAMA, null);
        noPlate = sharedpreferences.getString(TAG_NOPLATE, null);
        bikeType = sharedpreferences.getString(TAG_BIKETYPE, null);
        bikeMerk = sharedpreferences.getString(TAG_BIKEMERK, null);

        datamotor.setText(nama);
        dataUser.setText(noPlate);
    }


    public void sendCheckout(final String noPlate){
        StringRequest strReq = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }){
            @Override
            protected Map<String, String> getParams()  {
                Map<String,String>parms= new HashMap<>();
                parms.put("noPlate",noPlate);
                return parms;
            }
        };
        RequestQueue requestQueue= Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(strReq);
    }

    private void TurnOn() {
        final BluetoothLeAdvertiser advertiser = BluetoothAdapter.getDefaultAdapter().getBluetoothLeAdvertiser();
        sharedpreferences = getSharedPreferences(my_shared_preferences, Context.MODE_PRIVATE);
        idMotor = sharedpreferences.getString(TAG_IDMOTOR, null);
        id = sharedpreferences.getString(TAG_ID, null);
        String dataBLE = idMotor + ":TO:" + id ;
        byte[] dataBT = dataBLE.getBytes(Charset.forName("UTF-8"));

        AdvertiseSettings settings = new AdvertiseSettings.Builder()
                .setAdvertiseMode( AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY )
                .setTxPowerLevel( AdvertiseSettings.ADVERTISE_TX_POWER_HIGH )
                .setConnectable(false)
                .build();

        ParcelUuid pUuid = new ParcelUuid( UUID.fromString( getString( R.string.ble_uuid ) ) );

        AdvertiseData data = new AdvertiseData.Builder()
                .setIncludeDeviceName( true )
                .addServiceUuid( pUuid )
                .addServiceData( pUuid, dataBT)
                .build();

        final AdvertiseCallback advertisingCallback = new AdvertiseCallback() {
            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                super.onStartSuccess(settingsInEffect);
            }

            @Override
            public void onStartFailure(int errorCode) {
                Log.e( "BLE", "Advertising onStartFailure: " + errorCode );
                super.onStartFailure(errorCode);
            }
        };

        advertiser.startAdvertising( settings, data, advertisingCallback );
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                advertiser.stopAdvertising(advertisingCallback);
            }
        }, 1500);
    }

    private void TurnOff() {
        final BluetoothLeAdvertiser advertiser = BluetoothAdapter.getDefaultAdapter().getBluetoothLeAdvertiser();
        sharedpreferences = getSharedPreferences(my_shared_preferences, Context.MODE_PRIVATE);
        idMotor = sharedpreferences.getString(TAG_IDMOTOR, null);
        id = sharedpreferences.getString(TAG_ID, null);
        String dataBLE = idMotor + ":TF:" + id;
        byte[] dataBT = dataBLE.getBytes(Charset.forName("UTF-8"));

        AdvertiseSettings settings = new AdvertiseSettings.Builder()
                .setAdvertiseMode( AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY )
                .setTxPowerLevel( AdvertiseSettings.ADVERTISE_TX_POWER_HIGH )
                .setConnectable(false)
                .build();

        ParcelUuid pUuid = new ParcelUuid( UUID.fromString( getString( R.string.ble_uuid ) ) );

        AdvertiseData data = new AdvertiseData.Builder()
                .setIncludeDeviceName( true )
                .addServiceUuid( pUuid )
                .addServiceData(  pUuid, dataBT)
                .build();

        final AdvertiseCallback advertisingCallback = new AdvertiseCallback() {
            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                super.onStartSuccess(settingsInEffect);
            }

            @Override
            public void onStartFailure(int errorCode) {
                Log.e( "BLE", "Advertising onStartFailure: " + errorCode );
                super.onStartFailure(errorCode);
            }
        };

        advertiser.startAdvertising( settings, data, advertisingCallback );
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                advertiser.stopAdvertising(advertisingCallback);
            }
        }, 1500);
    }

    private void checkOutBT() {
        final BluetoothLeAdvertiser advertiser = BluetoothAdapter.getDefaultAdapter().getBluetoothLeAdvertiser();
        sharedpreferences = getSharedPreferences(my_shared_preferences, Context.MODE_PRIVATE);
        idMotor = sharedpreferences.getString(TAG_IDMOTOR, null);
        id = sharedpreferences.getString(TAG_ID, null);
        String dataBLE = idMotor + ":CO:" + id;
        byte[] dataBT = dataBLE.getBytes(Charset.forName("UTF-8"));

        AdvertiseSettings settings = new AdvertiseSettings.Builder()
                .setAdvertiseMode( AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY )
                .setTxPowerLevel( AdvertiseSettings.ADVERTISE_TX_POWER_HIGH )
                .setConnectable(false)
                .build();

        ParcelUuid pUuid = new ParcelUuid( UUID.fromString( getString( R.string.ble_uuid ) ) );

        AdvertiseData data = new AdvertiseData.Builder()
                .setIncludeDeviceName( true )
                .addServiceUuid( pUuid )
                .addServiceData( pUuid, dataBT)
                .build();

        final AdvertiseCallback advertisingCallback = new AdvertiseCallback() {
            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                super.onStartSuccess(settingsInEffect);
            }

            @Override
            public void onStartFailure(int errorCode) {
                Log.e( "BLE", "Advertising onStartFailure: " + errorCode );
                super.onStartFailure(errorCode);
            }
        };

        advertiser.startAdvertising( settings, data, advertisingCallback );
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                advertiser.stopAdvertising(advertisingCallback);
            }
        }, 1500);
    }
}
