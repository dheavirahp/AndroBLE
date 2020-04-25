package com.example.androble;

import android.app.ProgressDialog;
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
import android.util.Base64;
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
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SwitchActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {
    private Handler mHandler = new Handler();

    Button onButton, offButton, checkoutButton;
    TextView datamotor, dataUser;
    SharedPreferences sharedpreferences;
    Switch switch1;
    int c = 0;

    private static final String TAG = ChooseVehicle.class.getSimpleName();

    public final static String TAG_NAMA = "nama";
    public final static String TAG_EMAIL = "email";
    String email, nama;
    public static final String url = ServerTandebike.checkoutBooking;
    public static final String urls = ServerTandebike.engineStarter;
    public static final String verifenc = ServerTandebike.verifEncrypt;

    public static final String TAG_NOPLATE = "noPlate";
    public static final String TAG_BIKETYPE = "bikeType";
    public static final String TAG_BIKEMERK = "bikeMerk";
    public static final String TAG_ID = "id";
    public static final String TAG_IDMOTOR = "idMotor";
    public static final String TAG_CHECKIN = "check_in";
    ProgressDialog pDialog;

    String noPlate, bikeType, bikeMerk, check_in, databluetooths, condition;
    Boolean sessionReserve = false;
    public static final String session_reserve_status = "session_reserve_status";
    public static final String my_shared_preferences = "my_shared_preferences";
    private String id, idMotor;
    String encrypted = "";
    List<String> splitdatas = new ArrayList<String>();
    private static final byte[] keyValue =
            new byte[]{'t', 'a', 'n', 'd', 'e', 'b', 'i', 'k', 'e', 's', 'k', 'r', 'i', 'p', 's', 'i'};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_switch);

        dataUser = (TextView) findViewById(R.id.dataUser);
        datamotor = (TextView) findViewById(R.id.dataMotor);
        checkoutButton = (Button) findViewById(R.id.checkoutButton);
        switch1 = (Switch) findViewById(R.id.switch1);
        switch1.setOnCheckedChangeListener(this);
        loadSharedPref();
        checkoutButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.checkoutButton) {
                    sharedpreferences = getSharedPreferences(my_shared_preferences, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.remove("check_in");
                    editor.commit();
                    sendCheckout(noPlate);
                    checkOutBT();
                    //deleteBooking();
                    Toast.makeText(SwitchActivity.this, check_in, Toast.LENGTH_SHORT).show();
                    finish();
                    Intent i = new Intent(SwitchActivity.this, ChooseVehicle.class);
                    startActivity(i);
                }
            }
        });
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

        if (switch1.isChecked()) {
           // showDialog();
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putBoolean(session_reserve_status, true);
            sessionReserve = sharedpreferences.getBoolean(session_reserve_status, false);
            editor.apply();
            Toast.makeText(this, "Reservation: Turn ON", Toast.LENGTH_SHORT).show();
            //TurnOn();
            StringRequest strReq = new StringRequest(Request.Method.POST, urls, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(SwitchActivity.this, "Turn ON failed", Toast.LENGTH_SHORT).show();
                    VolleyLog.e(TAG, "Error: " + error.getMessage());
                    switch1.setChecked(false);
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> parms = new HashMap<>();
                    parms.put("noPlate", noPlate);
                    parms.put("status", "ON");

                    return parms;
                }
            };
            RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
            requestQueue.add(strReq);
            Toast.makeText(SwitchActivity.this, "ON", Toast.LENGTH_SHORT).show();
            dataSwitch();
        } else {
           // showDialog();
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putBoolean(session_reserve_status, false);
            sessionReserve = sharedpreferences.getBoolean(session_reserve_status, false);
            editor.apply();
            Toast.makeText(this, "Reservation: Turn OFF", Toast.LENGTH_SHORT).show();
            dataSwitch();
            //TurnOff();
            StringRequest strReq = new StringRequest(Request.Method.POST, urls, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(SwitchActivity.this, "Turn OFF failed", Toast.LENGTH_SHORT).show();
                    VolleyLog.e(TAG, "Error: " + error.getMessage());
                    switch1.setChecked(true);
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> parms = new HashMap<>();
                    parms.put("noPlate", noPlate);
                    parms.put("status", "OFF");
                    return parms;
                }
            };
            RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
            requestQueue.add(strReq);
            Toast.makeText(SwitchActivity.this, "OFF", Toast.LENGTH_SHORT).show();

        }
    }

    private void checkOutBT() {
        final BluetoothLeAdvertiser advertiser = BluetoothAdapter.getDefaultAdapter().getBluetoothLeAdvertiser();
        sharedpreferences = getSharedPreferences(my_shared_preferences, Context.MODE_PRIVATE);
        idMotor = sharedpreferences.getString(TAG_IDMOTOR, null);
        id = sharedpreferences.getString(TAG_ID, null);
        databluetooths = idMotor + ":CO:" + id;
        encode(databluetooths);
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


    public void sendCheckout(final String noPlate) {
        StringRequest strReq = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> parms = new HashMap<>();
                parms.put("noPlate", noPlate);
                return parms;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(strReq);
    }

    private void dataSwitch() {
        sharedpreferences = getSharedPreferences(my_shared_preferences, Context.MODE_PRIVATE);
        idMotor = sharedpreferences.getString(TAG_IDMOTOR, null);
        id = sharedpreferences.getString(TAG_ID, null);
        if (sessionReserve == true) {
            databluetooths = idMotor + ":TN:" + id;
            condition = "ON";
        } else {
            databluetooths = idMotor + ":TF:" + id;
            condition = "OFF";
        }
        encode(databluetooths);
//        encrypt(databluetooths);
//        splitData(encrypted);
        insertSwitch(noPlate, condition);
       // dataBLE();
        //waitingProc();
        Toast.makeText(SwitchActivity.this, databluetooths, Toast.LENGTH_SHORT).show();
    }
    public void encode(String sourceStr){
        final BluetoothLeAdvertiser advertiser = BluetoothAdapter.getDefaultAdapter().getBluetoothLeAdvertiser();
        byte[] datas = sourceStr.getBytes(Charset.forName("UTF-8"));
        String base64 = Base64.encodeToString(datas, Base64.DEFAULT);
        byte[] base64enc = base64.getBytes(Charset.forName("UTF-8"));
        Toast.makeText(SwitchActivity.this, base64, Toast.LENGTH_SHORT).show();

        AdvertiseSettings settings = new AdvertiseSettings.Builder()
                .setAdvertiseMode( AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY )
                .setTxPowerLevel( AdvertiseSettings.ADVERTISE_TX_POWER_HIGH )
                .setConnectable(false)
                .build();

        ParcelUuid pUuid = new ParcelUuid( UUID.fromString( getString( R.string.ble_uuid ) ) );

        AdvertiseData data = new AdvertiseData.Builder()
                .setIncludeDeviceName( true )
                .addServiceUuid( pUuid )
                .addServiceData( pUuid, base64enc)
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

//    public void encrypt(String sourceStr) {
//        try {
//            encrypted = AES128.encrypt(sourceStr);
//            Log.d("TEST", "encrypted:" + encrypted);
//            Toast.makeText(getApplicationContext(), encrypted, Toast.LENGTH_SHORT).show();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void splitData(String dataBLE) {
//        int index = 0;
//        while (index < dataBLE.length()) {
//            splitdatas.add(dataBLE.substring(index, Math.min(index + 11, dataBLE.length())));
//            index += 11;
//        }
//        System.out.println("size of ArrayList : " + splitdatas.size());
//        Toast.makeText(this, "data Split" + splitdatas, Toast.LENGTH_SHORT).show();
//        Toast.makeText(this, "size split" + splitdatas.size(), Toast.LENGTH_SHORT).show();
//    }

    public void insertSwitch(final String noPlate, final String condition) {
        mHandler.postDelayed(new Runnable() {
            public void run() {
                Toast.makeText(SwitchActivity.this, noPlate + condition, Toast.LENGTH_SHORT).show();
            }
        }, 1000);

        StringRequest strReq = new StringRequest(Request.Method.POST, urls, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e(TAG, "Error: " + error.getMessage());

            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> parms = new HashMap<>();
                parms.put("noPlate", noPlate);
                parms.put("status", condition);
                return parms;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(strReq);
    }

    private void dataBLE() {
        AdvertiseSettings settings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                .setConnectable(false)
                .build();
        ParcelUuid pUuid = new ParcelUuid(UUID.fromString(getString(R.string.ble_uuid)));


        final AdvertiseCallback advertisingCallback = new AdvertiseCallback() {
            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                super.onStartSuccess(settingsInEffect);
            }

            @Override
            public void onStartFailure(int errorCode) {
                Log.e("BLE", "Advertising onStartFailure: " + errorCode);
                super.onStartFailure(errorCode);
            }
        };

        while (c < 3) {
            final BluetoothLeAdvertiser advertiser = BluetoothAdapter.getDefaultAdapter().getBluetoothLeAdvertiser();
            byte[] dataBT = splitdatas.get(c).getBytes(Charset.forName("UTF-8"));

            AdvertiseData data = new AdvertiseData.Builder()
                    .setIncludeDeviceName(true)
                    .addServiceUuid(pUuid)
                    .addServiceData(pUuid, dataBT)
                    .build();
            Log.d("Service Data Length", Integer.toString(dataBT.length));
            Log.d("ServiceData", splitdatas.get(c));
            advertiser.startAdvertising(settings, data, advertisingCallback);
            //  c++;
            stop(advertiser, advertisingCallback);
            break;
            //dataBT = " ".getBytes();
        }
    }

    private void stop(final BluetoothLeAdvertiser advertisers, final AdvertiseCallback advertisingCallbacks) {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //c = 2;
                Log.d("ServiceData aja", splitdatas.get(c));
                advertisers.stopAdvertising(advertisingCallbacks);
                c++;
                dataBLE();
            }
        }, 1500);
    }

    public void validationEnc(final String noPlate) {
        StringRequest strReq = new StringRequest(Request.Method.POST, verifenc, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                try {
                    JSONArray jsonObj = new JSONArray(response);

                    for (int i = 0; i < jsonObj.length(); i++) {
                        JSONObject jsonObj1 = jsonObj.getJSONObject(i);
                        check_in = jsonObj1.getString("check_in");
                    }
                    SharedPreferences sharedpreferences = getSharedPreferences(my_shared_preferences, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.putString(TAG_CHECKIN, check_in);
                    editor.apply();
                    editor.commit();
                    Toast.makeText(SwitchActivity.this, "check_in:" + check_in, Toast.LENGTH_SHORT).show();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, error.getLocalizedMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> parms = new HashMap<>();
                parms.put("noPlate", noPlate);
                return parms;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(strReq);
    }

    public void waitingProc() {
        sharedpreferences = getSharedPreferences(my_shared_preferences, Context.MODE_PRIVATE);
        check_in = sharedpreferences.getString(TAG_CHECKIN, null);
//
//        validationEnc(noPlate);
//        Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            public void run() {
//                if(check_in.equals("1")){
//                    Toast.makeText(SwitchActivity.this, "Validation complete" + check_in, Toast.LENGTH_SHORT).show();
//                    Intent i = new Intent(SwitchActivity.this, ChooseVehicle.class);
//                    startActivity(i);
//                    hideDialog();
//                } else if(check_in.equals("0")) {
//                    Toast.makeText(SwitchActivity.this, "Validation uncomplete. Waiting." + check_in, Toast.LENGTH_SHORT).show();
//                    waitingProc();
//                }else{
//                    Toast.makeText(SwitchActivity.this, "Error", Toast.LENGTH_SHORT).show();
//                }
////                Intent i = new Intent(ChooseVehicle.this, SwitchActivity.class);
////                startActivity(i);
//            }
//        }, 10000);
        validationEnc(noPlate);
        for (int x = 0; x <= 7; x++) {
            if (check_in.equals("1")) {
                Toast.makeText(SwitchActivity.this, "Validation complete" + check_in, Toast.LENGTH_SHORT).show();
                Intent i = new Intent(SwitchActivity.this, SwitchActivity.class);
                startActivity(i);
                hideDialog();
                break;
            } else if (check_in.equals("0")) {
                Toast.makeText(SwitchActivity.this, "Validation uncomplete. Waiting." + check_in, Toast.LENGTH_SHORT).show();
                waitingProc();
            } else {
                Toast.makeText(SwitchActivity.this, "Error", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }
}