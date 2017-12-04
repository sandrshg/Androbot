package com.roboclub.androbot;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.hardware.Sensor.TYPE_ACCELEROMETER;
import static android.hardware.Sensor.TYPE_MAGNETIC_FIELD;
import static java.lang.Thread.sleep;

public class AutoControl extends AppCompatActivity implements SensorEventListener, AdapterView.OnItemSelectedListener {

    private SensorManager mSensorManager;
    Sensor accelerometer,magnetometer;
    private final float[] mAccelerometerReading = new float[3];
    private final float[] mMagnetometerReading = new float[3];

    private final float[] mRotationMatrix = new float[9];
    private final float[] mOrientationAngles = new float[3];
    TextView textView15;
    int azimuth;
    float a = 0.2f;
    public final String ACTION_USB_PERMISSION = "com.robotics.androbot.USB_PERMISSION";
    Button startButton ,stopButton;
    TextView textView;
    UsbManager usbManager;
    UsbDevice device;
    UsbSerialDevice serialPort;
    UsbDeviceConnection connection;
    Toast toast;
    String ssid;
    static WifiManager wifi;
    double signal[]=new double[10000];
    String data;

    Spinner spin;

    UsbSerialInterface.UsbReadCallback mCallback = new UsbSerialInterface.UsbReadCallback() { //Defining a Callback which triggers whenever data is read.
        @Override
        public void onReceivedData(byte[] arg0) {
            String data = null;
            try {
                data = new String(arg0, "UTF-8");

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }


        }
    };
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() { //Broadcast Receiver to automatically start and stop the Serial connection.
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_USB_PERMISSION)) {
                boolean granted = intent.getExtras().getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED);
                if (granted) {
                    connection = usbManager.openDevice(device);
                    serialPort = UsbSerialDevice.createUsbSerialDevice(device, connection);
                    if (serialPort != null) {
                        if (serialPort.open()) { //Set Serial Connection Parameters.
                            setUiEnabled(true);
                            serialPort.setBaudRate(9600);
                            serialPort.setDataBits(UsbSerialInterface.DATA_BITS_8);
                            serialPort.setStopBits(UsbSerialInterface.STOP_BITS_1);
                            serialPort.setParity(UsbSerialInterface.PARITY_NONE);
                            serialPort.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
                            serialPort.read(mCallback);
                            textView.append("Serial Connection Opened!\n");

                        } else {
                            Log.d("SERIAL", "PORT NOT OPEN");
                        }
                    } else {
                        Log.d("SERIAL", "PORT IS NULL");
                    }
                } else {
                    Log.d("SERIAL", "PERM NOT GRANTED");
                }
            } else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
                onClickStart(startButton);
            } else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
                onClickStop(stopButton);

            }
        }

        ;
    };

    public void printerr(Exception e){
        try{
            FileOutputStream fout=new FileOutputStream(new File(Environment.getExternalStorageDirectory().getPath()+"/filew233.txt"),true);
            OutputStreamWriter out=new OutputStreamWriter(fout);
            out.append("// \n"+Log.getStackTraceString(e));
            out.close();
        }
        catch (Exception e2){
            e2.printStackTrace();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_control);

        checkAndRequestPermissions();
        usbManager = (UsbManager) getSystemService(this.USB_SERVICE);

        startButton = (Button) findViewById(R.id.buttongo);
        stopButton = (Button) findViewById(R.id.buttonstop);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
        textView = (TextView) findViewById(R.id.textView);
        textView.setMovementMethod(new ScrollingMovementMethod());

        setUiEnabled(false);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        final Intent intent = registerReceiver(broadcastReceiver, filter);

        spin=(Spinner) findViewById(R.id.SSID);
        spin.setOnItemSelectedListener(this);
        wifi=(WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifi.setWifiEnabled(true);
        wifi.startScan();
        List<ScanResult> res=wifi.getScanResults();
        List<String> ssid=new ArrayList<>();
        if(toast!=null)
            toast.cancel();
        toast= Toast.makeText(this,""+res.size(),Toast.LENGTH_SHORT);
        toast.show();
        for(ScanResult ress:res){
            ssid.add(ress.SSID.toString());
        }
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, ssid);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin.setAdapter(dataAdapter);
        textView15=(TextView)findViewById(R.id.textss);

    }

    public void onClickStart(View view) {

        WifiS wf=new WifiS();
        wf.execute();
        textView.append(ssid);

        HashMap<String, UsbDevice> usbDevices = usbManager.getDeviceList();
        if (!usbDevices.isEmpty()) {
            boolean keep = true;
            for (Map.Entry<String, UsbDevice> entry : usbDevices.entrySet()) {
                device = entry.getValue();
                int deviceVID = device.getVendorId();
                if (deviceVID == 0x2341)//Arduino Vendor ID
                {
                    PendingIntent pi = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
                    usbManager.requestPermission(device, pi);
                    keep = false;
                } else {
                    connection = null;
                    device = null;
                }

                if (!keep)
                    break;
            }
        }
    }

    public void setUiEnabled(boolean bool) {
        startButton.setEnabled(!bool);
        stopButton.setEnabled(bool);

    }

    public void onClickStop(View view) {

        serialPort.write("s".getBytes());
        wifi.setWifiEnabled(false);
        setUiEnabled(false);
        serialPort.close();
        textView.append("\nSerial Connection Closed! \n");

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    public int getmOrientationAngles() {
        updateOrientationAngles();

        azimuth = (int) Math.toDegrees(mOrientationAngles[0]);

        if(azimuth<0)
            azimuth+=360;

        return azimuth;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == TYPE_ACCELEROMETER) {
            lowPass(event.values,mAccelerometerReading);

        }
        else if (event.sensor.getType() == TYPE_MAGNETIC_FIELD) {
            lowPass(event.values,mMagnetometerReading);
        }
//        getmOrientationAngles();
    }

    public void updateOrientationAngles() {
        // Update rotation matrix, which is needed to update orientation angles.
        mSensorManager.getRotationMatrix(mRotationMatrix, null,
                mAccelerometerReading, mMagnetometerReading);
        float[] omRotationMatrix = new float[9];
        SensorManager.remapCoordinateSystem(mRotationMatrix, SensorManager.AXIS_X,SensorManager.AXIS_Z, omRotationMatrix);


        mSensorManager.getOrientation(mRotationMatrix, mOrientationAngles);

        // "mOrientationAngles" now has up-to-date information.
    }
    private void lowPass(float [] input,float[] filteredValues) {
        if(filteredValues == null)
        {
            filteredValues=input;
        }
        filteredValues[0] = input[0] * a + filteredValues[0] * (1.0f - a);
        filteredValues[1] = input[1] * a + filteredValues[1] * (1.0f - a);
        filteredValues[2] = input[2] * a + filteredValues[2] * (1.0f - a);
        return;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        ssid=parent.getItemAtPosition(position).toString();

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private class WifiS extends AsyncTask<Void,String,Void> {

        int l=0,count=0,rcount=0;
        boolean turned=true;

        protected Void doInBackground(Void... Params) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                printerr(e);
            }
            try {
                serialPort.write("f".getBytes());
                double lmean = 0, pmean = 0;
                int currori, tarori;
                while (true) {
                    double mean = 0;

                    wifi.startScan();
                    List<ScanResult> res = wifi.getScanResults();
                    for (ScanResult ress : res) {
                        if (ress.SSID.toString().equals(ssid))
                            signal[l] = ress.level;
                    }

                    if ((l + 1) % 2 == 0) {
                        for (int i = l; i > l - 2; i--) {
                            mean = mean + signal[i];
                        }
                        mean /= 2;
                        rcount++;
                        publishProgress("" + mean);
                    }
                    if (rcount > 2)
                        turned = false;
                    if (mean < lmean && lmean!=0) {
                        if (count == 0) {
                            count = 1;
                        }
                        else if (count == 1 && turned) {
                            //turn 180 degrees
//                            currori = getmOrientationAngles();
//                            tarori = (currori + 180) % 360;
                            serialPort.write("r".getBytes());
                            try {
                                Thread.sleep(4000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
//                        while((currori-tarori)%360>5)
//                        {
//                            try {
//                                sleep(200);
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }
//                            currori=getmOrientationAngles();
//                        }
                            serialPort.write("s".getBytes());
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            serialPort.write("f".getBytes());
                            rcount = 0;
                            pmean = mean;

                        } else if (count == 1 && !turned) {
                            serialPort.write("s".getBytes());
//                        serialPort.write("o".getBytes());
//                        try {
//                            sleep(150);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
                            //turn right 90 degrees
//                            currori = getmOrientationAngles();
//                            tarori = (currori + 90) % 360;
                            serialPort.write("r".getBytes());
                            try {
                                Thread.sleep(2100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
//                        while((tarori-currori)%360>5)
//                        {
//                            try {
//                                sleep(200);
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }
//                            currori=getmOrientationAngles();
//                        }
                            serialPort.write("s".getBytes());
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            serialPort.write("f".getBytes());

                            turned = true;
                            rcount = 0;
                            pmean = mean;
                        }
                    }
                    else if (mean < pmean && !turned) {
                        serialPort.write("s".getBytes());
                        //serialPort.write("o".getBytes());
//                    try {
//                        sleep(150);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
                        //turn right 90 degrees
//                        currori = getmOrientationAngles();
//                        tarori = (currori + 90) % 360;
                        serialPort.write("r".getBytes());
                        try {
                            Thread.sleep(2100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
//                    while((tarori-currori)%360>5)
//                    {
//                        try {
//                            sleep(200);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                        currori=getmOrientationAngles();
//                    }
                        serialPort.write("s".getBytes());
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        serialPort.write("f".getBytes());

                        turned = true;
                        rcount = 0;
                        pmean = mean;
                    }

                    if ((l + 1) % 2 != 0) {
                        try {
                            sleep(500);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    if ((l + 1) % 2 == 0) {
                        try {
                            sleep(1500);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if (1 == 2)
                        break;
                    l++;

                    lmean = mean;
                }
            }
            catch (Exception e){
                printerr(e);
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(String... Values){
            textView.append(" "+Values[0]);
            textView15.setText(Values[0]);

        }

    }

    private boolean checkAndRequestPermissions() {
        int WIFISTATE = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE);
        int CHANGESTATE=ContextCompat.checkSelfPermission(this, Manifest.permission.CHANGE_WIFI_STATE);
        int ACCESS = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        int ACCESSFINE= ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int READE= ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        int WRITEE= ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (WIFISTATE != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_WIFI_STATE);
        }
        if (CHANGESTATE != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CHANGE_WIFI_STATE);
        }
        if (ACCESS != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if (ACCESSFINE != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (READE != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (WRITEE != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),1);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        Log.d("Permission", "Permission callback called-------");
        switch (requestCode) {
            case 1: {

                Map<String, Integer> perms = new HashMap<>();
                // Initialize the map with both permissions
                perms.put(Manifest.permission.ACCESS_WIFI_STATE, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.CHANGE_WIFI_STATE, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.ACCESS_COARSE_LOCATION, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.READ_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                // Fill with actual results from user
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++)
                        perms.put(permissions[i], grantResults[i]);
                    // Check for both permissions
                    if (perms.get(Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED
                            && perms.get(Manifest.permission.CHANGE_WIFI_STATE) == PackageManager.PERMISSION_GRANTED
                            && perms.get(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                            && perms.get(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                            && perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED
                            && perms.get(Manifest.permission.READ_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED
                            ) {
                        Log.d("Permission", "sms & location services permission granted");
                        // process the normal flow
                        //else any one or both the permissions are not granted
                    } else {
                        Log.d("Permission", "Some permissions are not granted ask again ");
                        //permission is denied (this is the first time, when "never ask again" is not checked) so ask again explaining the usage of permission
//                        // shouldShowRequestPermissionRationale will return true
                        //show the dialog or snackbar saying its necessary and try again otherwise proceed with setup.
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_WIFI_STATE)
                                || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CHANGE_WIFI_STATE)
                                || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                                || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)
                                || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                                )









                        {
                            showDialogOK("SMS and Location Services Permission required for this app",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            switch (which) {
                                                case DialogInterface.BUTTON_POSITIVE:
                                                    checkAndRequestPermissions();
                                                    break;
                                                case DialogInterface.BUTTON_NEGATIVE:
                                                    // proceed with logic by disabling the related features or quit the app.
                                                    break;
                                            }
                                        }
                                    });
                        }
                        //permission is denied (and never ask again is  checked)
                        //shouldShowRequestPermissionRationale will return false
                        else {

                            //                            //proceed with logic by disabling the related features or quit the app.
                        }
                    }
                }
            }
        }

    }

    private void showDialogOK(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", okListener)
                .create()
                .show();
    }

}