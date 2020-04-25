package com.example.androble;

import android.animation.ArgbEvaluator;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
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

public class ChooseVehicle extends AppCompatActivity {

    TextView txt_hasil, txtPlate, txtBike, txtMerk;
    Spinner spinner_motor;
    ProgressDialog pDialog;
    ViewPager viewPager;
    AdapterChooseVehicle adapter;
    Integer[] colors = null;
    ArgbEvaluator argbEvaluator = new ArgbEvaluator();
    //MotorAdapter motoradapter;
    private Button btn;
    List<Vehicle> vehicles = new ArrayList<Vehicle>();
    List<String> splitdatas = new ArrayList<String>();


    int c = 0;
    // sesuaikan dengan IP Address PC/laptop atau ip address emulator android 10.0.2.2
    public static final String url = ServerTandebike.AvailableBike;
    public static final String urls= ServerTandebike.insertBooking;
    public static final String user = ServerTandebike.idUser;
    public static final String motor = ServerTandebike.idMotor;
    public static final String verifenc = ServerTandebike.verifEncrypt;



    private static final String TAG = ChooseVehicle.class.getSimpleName();
    String GetImage, GetBike, GetPlate, GetMerk;
    public final static String TAG_EMAIL = "email";
    public static final String TAG_IMAGE = "imageMotor";
    public static final String TAG_NOPLATE = "noPlate";
    public static final String TAG_BIKETYPE = "bikeType";
    public static final String TAG_BIKEMERK = "bikeMerk";
    public static final String TAG_ID = "id";
    public static final String TAG_IDMOTOR = "idMotor";
    public static final String TAG_CHECKIN = "check_in";



    String value;
    String encrypted = "";
    String decrypted = "";
    String noPlate, databluetooths, dataEncrypt;
    String bikeType;
    String bikeMerk;
    String email;
    String id, idMotor;
    String check_in;
    JSONObject idUser;
    Boolean sessionBooking = false;
    public static final String session_booking_status = "session_booking_status";
    SharedPreferences sharedpreferences;

    //SplitString


    //Inisiasi Bluetooth
    private TextView mText;
    private BluetoothLeScanner mBluetoothLeScanner;
    private Handler mHandler = new Handler();
    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    private static final int REQUEST_ENABLE_BT = 1;

    //sharefpreferences
    public static final String my_shared_preferences = "my_shared_preferences";

    //AES
    private static final byte[] keyValue =
            new byte[]{'t', 'a', 'n', 'd', 'e', 'b', 'i', 'k', 'e', 's', 'k', 'r', 'i', 'p', 's', 'i'};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_vehicle);

        mText = (TextView) findViewById( R.id.text );
        btn = (Button) findViewById(R.id.buttonChoose);


        mBluetoothLeScanner = BluetoothAdapter.getDefaultAdapter().getBluetoothLeScanner();
        if (bluetoothAdapter != null) {
            if (!bluetoothAdapter.isEnabled()) {
                final Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                Toast.makeText(getApplicationContext(), "Bluetooth OFF. Turn ON your Bluetooth", Toast.LENGTH_SHORT).show();
                btn.setEnabled(true);
                mBluetoothLeScanner = BluetoothAdapter.getDefaultAdapter().getBluetoothLeScanner();
            } else if (bluetoothAdapter.isEnabled()) {
                Toast.makeText(getApplicationContext(), "Bluetooth ON", Toast.LENGTH_SHORT).show();
                btn.isEnabled();
                //btn.setEnabled( true );
            }
        }
        callVehicle();

        btn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v){
                //insertVehicle(GetBike, GetPlate, GetMerk);
                if( v.getId() == R.id.buttonChoose && bluetoothAdapter.isEnabled()) {
                    pDialog = new ProgressDialog(ChooseVehicle.this);
                    pDialog.setCancelable(false);
                    pDialog.setMessage("Loading. Please wait...");
                    showDialog();
                    SaveDataMotor();
                    idMotor(noPlate);
                    idUser(email);
                }
                else if(v.getId() == R.id.buttonChoose && !bluetoothAdapter.isEnabled()){
                    final Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                    Toast.makeText(getApplicationContext(), "Bluetooth OFF. Turn ON your Bluetooth", Toast.LENGTH_SHORT).show();
                }
            }
        });

        sessionBooking();

        if (sessionBooking == true) {
            Intent intent = new Intent(ChooseVehicle.this, SwitchActivity.class);
            intent.putExtra(TAG_CHECKIN, check_in);
            finish();
            startActivity(intent);
        }


        viewPager = findViewById(R.id.viewPager);
        adapter = new AdapterChooseVehicle(ChooseVehicle.this,vehicles);
        viewPager.setAdapter(adapter);

        viewPager.setPadding(110, 0, 110, 0);

        Integer[] colors_temp = {
                getResources().getColor(R.color.color1),
                getResources().getColor(R.color.color2),
                getResources().getColor(R.color.color3),
                getResources().getColor(R.color.color4)
        };

        colors = colors_temp;

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (position < (adapter.getCount() -1) && position < (colors.length - 1)) {
                    viewPager.setBackgroundColor(
                            (Integer) argbEvaluator.evaluate(
                                    positionOffset,
                                    colors[position],
                                    colors[position + 1]
                            )
                    );
                }
                else {
                    viewPager.setBackgroundColor(colors[colors.length - 1]);
                }
            }

            @Override
            public void onPageSelected(int position) {
                GetBike = vehicles.get(position).getBikeType().toString();
                GetPlate = vehicles.get(position).getPlateNo().toString();
                GetMerk = vehicles.get(position).getBikeMerk().toString();
                Toast.makeText(ChooseVehicle.this, GetPlate, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    public void SaveDataMotor(){
        SharedPreferences sharedpreferences = getSharedPreferences(my_shared_preferences, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putBoolean(session_booking_status, true);
        editor.putString(TAG_NOPLATE, GetPlate);
        editor.putString(TAG_BIKETYPE, GetBike);
        editor.putString(TAG_BIKEMERK, GetMerk);

        editor.apply();
        email = sharedpreferences.getString(TAG_EMAIL, null);
        noPlate = sharedpreferences.getString(TAG_NOPLATE, null);
        bikeType = sharedpreferences.getString(TAG_BIKETYPE, null);
        bikeMerk = sharedpreferences.getString(TAG_BIKEMERK, null);
        Toast.makeText(this, "Reservation Successfully" + noPlate, Toast.LENGTH_SHORT).show();
    }

    public void sessionBooking(){
        sharedpreferences = getSharedPreferences(my_shared_preferences, Context.MODE_PRIVATE);
//        if (noPlate == null){
        if (check_in == null || check_in.equals("0")){
        SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putBoolean(session_booking_status, false);
            sessionBooking = sharedpreferences.getBoolean(session_booking_status, false);
            editor.apply();
            Toast.makeText(this, "No Session Booking", Toast.LENGTH_SHORT).show();
        }else{
            sessionBooking = sharedpreferences.getBoolean(session_booking_status, false);
            check_in = sharedpreferences.getString(TAG_CHECKIN, null);
        }

    }
    public void encrypt(String sourceStr){
        try {
            encrypted = AES128.encrypt(sourceStr);
            Log.d("TEST", "encrypted:" + encrypted);
            Toast.makeText(getApplicationContext(), encrypted, Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
//    public void encrypt(String sourceStr){
//        try {
//            encrypted = AESUtils.encrypt(sourceStr);
//            Log.d("TEST", "encrypted:" + encrypted);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    private void splitData(String dataBLE){
        int index = 0;
        while (index < dataBLE.length()) {
            splitdatas.add(dataBLE.substring(index, Math.min(index + 11,dataBLE.length())));
            index += 11;
        }
        System.out.println("size of ArrayList : " + splitdatas.size());
        Toast.makeText(this, "data Split" + splitdatas, Toast.LENGTH_SHORT).show();
        Toast.makeText(this, "size split" + splitdatas.size(), Toast.LENGTH_SHORT).show();
    }

    public void insertBooking(final String email, final String noPlate, final String dataBLEaja){
        mHandler.postDelayed(new Runnable() {
            public void run() {
                Toast.makeText(ChooseVehicle.this, "bleaja:" + dataBLEaja, Toast.LENGTH_SHORT).show();
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
        }){
            @Override
            protected Map<String, String> getParams()  {
                Map<String,String>parms= new HashMap<>();
                parms.put("email",email);
                parms.put("noPlate",noPlate);
                parms.put("Encryption",dataBLEaja);
                parms.put("status","0");
                parms.put("check_in","0");
                return parms;
            }
        };

        RequestQueue requestQueue= Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(strReq);
    }
    public void validationEnc(final String noPlate){
        StringRequest strReq = new StringRequest(Request.Method.POST, verifenc, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                try{
                    JSONArray jsonObj = new JSONArray(response);

                    for(int i = 0 ; i<jsonObj.length();i++){
                        JSONObject jsonObj1 = jsonObj.getJSONObject(i);
                        check_in = jsonObj1.getString("check_in");
                    }
                    SharedPreferences sharedpreferences = getSharedPreferences(my_shared_preferences, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.putString(TAG_CHECKIN, check_in);
                    editor.apply();
                    editor.commit();
                    Toast.makeText(ChooseVehicle.this, "check_in:" + check_in, Toast.LENGTH_SHORT).show();

                }  catch (JSONException e){
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, error.getLocalizedMessage());
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

    public void waitingProc(){
        sharedpreferences = getSharedPreferences(my_shared_preferences, Context.MODE_PRIVATE);
       check_in = sharedpreferences.getString(TAG_CHECKIN, null);

        validationEnc(noPlate);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                if(check_in.equals("1")){
                    Toast.makeText(ChooseVehicle.this, "Validation complete" + check_in, Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(ChooseVehicle.this, SwitchActivity.class);
                    startActivity(i);
                    hideDialog();
                } else if(check_in.equals("0")) {
                    Toast.makeText(ChooseVehicle.this, "Validation uncomplete. Waiting." + check_in, Toast.LENGTH_SHORT).show();
                    waitingProc();
                }else{
                    Toast.makeText(ChooseVehicle.this, "Error", Toast.LENGTH_SHORT).show();
                }
//                Intent i = new Intent(ChooseVehicle.this, SwitchActivity.class);
//                startActivity(i);
            }
        }, 10000);
        sharedpreferences = getSharedPreferences(my_shared_preferences, Context.MODE_PRIVATE);
        check_in = sharedpreferences.getString(TAG_CHECKIN, null);

//        validationEnc(noPlate);
//        for(int x = 0; x<=7; x++){
//            if(check_in.equals("1")){
//                Toast.makeText(ChooseVehicle.this, "Validation complete" + check_in, Toast.LENGTH_SHORT).show();
//                Intent i = new Intent(ChooseVehicle.this, SwitchActivity.class);
//                startActivity(i);
//                hideDialog();
//                break;
//            } else if(check_in.equals("0")) {
//                Toast.makeText(ChooseVehicle.this, "Validation uncomplete. Waiting." + check_in, Toast.LENGTH_SHORT).show();
//                waitingProc();
//            }else{
//                Toast.makeText(ChooseVehicle.this, "Error", Toast.LENGTH_SHORT).show();
//            }
//        }
    }

    public void idUser(final String email){
        StringRequest strReq = new StringRequest(Request.Method.POST, user, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                try{
                    JSONArray jsonObj = new JSONArray(response);

                    for(int i = 0 ; i<jsonObj.length();i++){
                        JSONObject jsonObj1 = jsonObj.getJSONObject(i);
                        id = jsonObj1.getString("id");
                    }
                    SharedPreferences sharedpreferences = getSharedPreferences(my_shared_preferences, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.putString(TAG_ID, id);
                    editor.apply();
                    editor.commit();
                    dataBluetooth();

                    encrypt(databluetooths);
                    splitData(encrypted);
                    insertBooking(email, noPlate, encrypted);
                    dataBLE();
                    waitingProc();
                }  catch (JSONException e){
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }){
            @Override
            protected Map<String, String> getParams()  {
                Map<String,String>parms= new HashMap<>();
                parms.put("email",email);
                return parms;
            }
        };
        RequestQueue requestQueue= Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(strReq);
    }



    public void idMotor(final String noPlate){
        StringRequest strReq = new StringRequest(Request.Method.POST, motor, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                try{
                    JSONArray jsonObj = new JSONArray(response);

                    for(int i = 0 ; i<jsonObj.length();i++){
                        JSONObject jsonObj1 = jsonObj.getJSONObject(i);
                        idMotor = jsonObj1.getString("id");
                    }
                    SharedPreferences sharedpreferences = getSharedPreferences(my_shared_preferences, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.putString(TAG_IDMOTOR, idMotor);
                    editor.apply();
                    editor.commit();
                    Toast.makeText(ChooseVehicle.this, "idmotor:" + idMotor, Toast.LENGTH_SHORT).show();

                }  catch (JSONException e){
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, error.getLocalizedMessage());
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


    private void callVehicle() {
        vehicles.clear();

        pDialog = new ProgressDialog(ChooseVehicle.this);
        pDialog.setCancelable(false);
        pDialog.setMessage("Loading. Please wait...");
        showDialog();

        // Creating volley request obj
        JsonArrayRequest jArr = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.e(TAG, response.toString());

                        // Parsing json
                        for (int i = 0; i < response.length(); i++) {
                            try {
                                JSONObject obj = response.getJSONObject(i);

                                Vehicle item = new Vehicle(GetImage, GetBike, GetPlate, GetMerk);

                                item.setImage(obj.getString(TAG_IMAGE));
                                item.setPlateNo(obj.getString(TAG_NOPLATE));
                                item.setBikeType(obj.getString(TAG_BIKETYPE));
                                item.setBikeMerk(obj.getString(TAG_BIKEMERK));
                                vehicles.add(item);

                                //SaveDataMotor();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        adapter.notifyDataSetChanged();
                        hideDialog();
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse networkResponse = error.networkResponse;
                if (networkResponse != null && networkResponse.data != null) {
                    String jsonError = new String(networkResponse.data);
                    VolleyLog.e(TAG, "Error: " + error.getMessage());
                }
                VolleyLog.e(TAG, "Error: " + error.getMessage());
                Toast.makeText(ChooseVehicle.this, error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        });
        AppController.getInstance().addToRequestQueue(jArr);
    }
    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }
    private ScanCallback mScanCallback = new ScanCallback() {

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            if( result == null
                    || result.getDevice() == null
                    || TextUtils.isEmpty(result.getDevice().getName()) )
                return;

            StringBuilder builder = new StringBuilder( result.getDevice().getName() );

            builder.append("\n").append(new String(result.getScanRecord().getServiceData(result.getScanRecord().getServiceUuids().get(0)), Charset.forName("UTF-8")));

            mText.setText(builder.toString());
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e( "BLE", "Discovery onScanFailed: " + errorCode );
            super.onScanFailed(errorCode);
        }
    };
    private void dataBluetooth(){
        sharedpreferences = getSharedPreferences(my_shared_preferences, Context.MODE_PRIVATE);
        idMotor = sharedpreferences.getString(TAG_IDMOTOR, null);
        id = sharedpreferences.getString(TAG_ID, null);
        databluetooths = idMotor + ":CheckInMotor:" + id ;
        Toast.makeText(ChooseVehicle.this, databluetooths, Toast.LENGTH_SHORT).show();
    }
    private void dataBLE() {
        AdvertiseSettings settings = new AdvertiseSettings.Builder()
                .setAdvertiseMode( AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                .setTxPowerLevel( AdvertiseSettings.ADVERTISE_TX_POWER_HIGH )
                .setConnectable(false)
                .build();
        ParcelUuid pUuid = new ParcelUuid( UUID.fromString( getString( R.string.ble_uuid ) ) );


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

        while (c < 3){
            final BluetoothLeAdvertiser advertiser = BluetoothAdapter.getDefaultAdapter().getBluetoothLeAdvertiser();
            byte[] dataBT = splitdatas.get(c).getBytes(Charset.forName("UTF-8"));

                 AdvertiseData data = new AdvertiseData.Builder()
                    .setIncludeDeviceName( true )
                    .addServiceUuid( pUuid )
                    .addServiceData( pUuid, dataBT)
                    .build();
            Log.d("Service Data Length",Integer.toString(dataBT.length));
            Log.d("ServiceData",splitdatas.get(c));
            advertiser.startAdvertising( settings, data, advertisingCallback );
          //  c++;
            stop(advertiser, advertisingCallback);
            break;
            //dataBT = " ".getBytes();
        }
    }

    private void stop(final BluetoothLeAdvertiser advertisers, final AdvertiseCallback advertisingCallbacks){
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
              //c = 2;
                Log.d("ServiceData aja",splitdatas.get(c));
                advertisers.stopAdvertising(advertisingCallbacks);
                c++;
                dataBLE();
            }
        }, 1500);
    }




//
//    public void encrypt(String sourceStr){
//        try {
//            encrypted = AesEncryptDecrypt(sourceStr);
//            Log.d("TEST", "encrypted:" + encrypted);
//            Toast.makeText(getApplicationContext(), encrypted, Toast.LENGTH_SHORT).show();
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
// public void decrypt(){
//     try {
//         decrypted = AesEncryptDecrypt.decrypt(encrypted);
//         Log.d("TEST", "decrypted:" + decrypted);
//     } catch (Exception e) {
//         e.printStackTrace();
//     }
// }


// public void decrypt(){
//     try {
//         decrypted = AESUtils.decrypt(encrypted);
//         Log.d("TEST", "decrypted:" + decrypted);//
//     } catch (Exception e) {
//         e.printStackTrace();
//     }
// }

}