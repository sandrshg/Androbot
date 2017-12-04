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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import java.util.Scanner;

import static android.hardware.Sensor.TYPE_ACCELEROMETER;
import static android.hardware.Sensor.TYPE_MAGNETIC_FIELD;




public class WifiNavigation extends AppCompatActivity implements SensorEventListener {
    private SensorManager senSensorManager;
    private Sensor senAccelerometer;
    Sensor accelerometer, magnetometer;
    private final float[] mAccelerometerReading = new float[3];
    private final float[] mMagnetometerReading = new float[3];

    public final String ACTION_USB_PERMISSION = "com.robotics.androbot.USB_PERMISSION";
    Button startButton, stopButton;
    UsbManager usbManager;
    UsbDevice device;
    UsbSerialDevice serialPort;
    UsbDeviceConnection connection;

    private final float[] mRotationMatrix = new float[9];
    private final float[] mOrientationAngles = new float[3];
    int azimuth;

    static double  acc,ory;

    float ax, ay, az;
    float a = 0.2f;
    TextView tV4;


    static WifiManager wifi;
    static String SSID1;
    static String SSID2;
    static String SSID3;
    static String SSID4;
    static String SSID5;

    static double signal1;
    static double signal2;
    static double signal3;
    static double signal4;
    static double signal5;

    Toast toast;
    EditText editText1;
    EditText editText2;


    public static int endpointx, endpointy;
    public static double m[][] = new double[3][1];
    //find coordinates from measurement
    public static double p[][] = new double[3][3];

    //defined by user
    static double acceleration, t1;

    public static double data[][]=new double[64][12];
    private Scanner x;

    UsbSerialInterface.UsbReadCallback mCallback = new UsbSerialInterface.UsbReadCallback() { //Defining a Callback which triggers whenever data is read.
        @Override
        public void onReceivedData(byte[] arg0) {
            String data = null;
            try {
                data = new String(arg0, "UTF-8");
                data.concat("/n");
//                tv.append(data);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                printerr(e);
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
//                            tv.append("Serial Connection Opened!\n");

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




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_navigation);
        checkAndRequestPermissions();
        try{
            FileOutputStream fout=new FileOutputStream(new File (Environment.getExternalStorageDirectory().getPath()+"/file2.txt"),true);
            OutputStreamWriter out=new OutputStreamWriter(fout);
            out.append("//started \n");
            out.close();
        }
        catch (Exception e2){
            printerr(e2);
            e2.printStackTrace();
        }
        q[0][0]=0.44531;
        q[1][1]=0.46875;
        q[1][0]=0.0078125;
        q[0][1]=0.0078125;
        p[0][0]=0.013319;
        p[1][1]=0.013319;
        p[2][2]=0;
        p[2][0]=0;
        p[2][1]=0;
        p[0][2]=0;
        p[1][2]=0;
        p[1][0]=0.00018;
        p[0][1]=0.00018;
        m[2][0]=0.476156;

        usbManager = (UsbManager) getSystemService(this.USB_SERVICE);
        startButton = (Button) findViewById(R.id.buttonStart);
        stopButton = (Button) findViewById(R.id.buttonStop);
        setUiEnabled(false);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(broadcastReceiver, filter);


        wifi=(WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifi.setWifiEnabled(true);


        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        accelerometer = senSensorManager.getDefaultSensor(TYPE_ACCELEROMETER);
        magnetometer = senSensorManager.getDefaultSensor(TYPE_MAGNETIC_FIELD);
        //  senSensorManager.registerListener(this, senAccelerometer , SensorManager.SENSOR_DELAY_NORMAL);
        senSensorManager.registerListener(this, senAccelerometer , SensorManager.SENSOR_DELAY_NORMAL);
        senSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        senSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);

        tV4 = (TextView) findViewById(R.id.tv);
        tV4.setMovementMethod(new ScrollingMovementMethod());
        editText1=(EditText) findViewById(R.id.editText2);
        editText2=(EditText) findViewById(R.id.editText);


        //  likelihood r = new likelihood();
        try{
            FileOutputStream fout=new FileOutputStream(new File (Environment.getExternalStorageDirectory().getPath()+"/file2.txt"),true);
            OutputStreamWriter out=new OutputStreamWriter(fout);
            out.append("//oncreateend");
            out.close();}
        catch (Exception e){
            printerr(e);
        }
//turn robot again to
// required degree

//rotate
    }
    public void printerr(Exception e){
        try{
            FileOutputStream fout=new FileOutputStream(new File (Environment.getExternalStorageDirectory().getPath()+"/file2.txt"),true);
            OutputStreamWriter out=new OutputStreamWriter(fout);
            out.append("// \n"+Log.getStackTraceString(e));
            out.close();
        }
        catch (Exception e2){
            //printerr(e);
            e2.printStackTrace();
        }
    }

    public void setUiEnabled(boolean bool) {
        startButton.setEnabled(!bool);
        stopButton.setEnabled(bool);
    }

    public void onClickStart(View view) {

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

    public void onClickStop(View view) {
        setUiEnabled(false);
        serialPort.close();

    }
    double x_,y_,z[][];
    public void onbclick(View v){
       try{
           try{
               FileOutputStream fout=new FileOutputStream(new File (Environment.getExternalStorageDirectory().getPath()+"/file2.txt"),true);
               OutputStreamWriter out=new OutputStreamWriter(fout);
               out.append("// bclickstart"+SSID1+SSID2+SSID3);
               out.close();}
           catch (Exception e){
               printerr(e);
           }
        z= get();
        m[0][0]=z[0][0];
        m[1][0]=z[1][0];
        tV4.append(""+z[0][0]+" "+z[1][0]+"\n");
           try{
               FileOutputStream fout=new FileOutputStream(new File (Environment.getExternalStorageDirectory().getPath()+"/file2.txt"),true);
               OutputStreamWriter out=new OutputStreamWriter(fout);
               out.append("// x y"+z[0][0]+z[1][0]);
               out.close();}
           catch (Exception e){
               printerr(e);
           }
        endpointx=Integer.parseInt(editText1.getText().toString());
        endpointy=Integer.parseInt(editText2.getText().toString());
        while ((Math.abs(m[0][0] - endpointx) > 1) || (Math.abs(m[1][0] - endpointy) > 1)) {
            x_ = endpointx - m[0][0];
            y_ = endpointy - m[1][0];
            double radians = Math.atan(y_ / x_);
            double degree = Math.toDegrees(radians);
            if(x_<0 )
                degree=degree+180;
            else if((x_>0) &&(y_<0))
                degree=degree+360;
            int rdeg = (int)(ory-degree);
            if(rdeg>180)
                rdeg-=360;
            else if(rdeg<-180)
                rdeg+=360;

//            double degree1=degree;
//            double dg=imu_getdegree();
//            if(dg !=0 ) {
//                degree = degree + dg - 360;
//                while (Math.abs((360 - dg) - degree1) > 10) {
//                    if ((degree > 0 && degree < 180) || (degree < -180)) {
//                        //move robot anticlockwise
//                        dg = imu_getdegree();
//                    } else if (degree > 180 || (degree < 0 && degree > -180)) {
//                        //   move robot clockwise
//                        dg = imu_getdegree();
//                    }
//                }
//
//            }
//            else
//            {
//                while (Math.abs((360 - dg) - degree1) > 10) {
//                    if ((degree > 0 && degree < 180) || (degree < -180)) {
//                        //move robot anticlockwise
//                        dg = imu_getdegree();
//                    } else if (degree > 180 || (degree < 0 && degree > -180)) {
//                        //   move robot clockwise
//                        dg = imu_getdegree();
//                    }
//                }
//            }
//            degree = degree + ory - 360;
            rotate(rdeg);


        }
       }
       catch(Exception e){
           printerr(e);
       }


    }




    int oory,tory;
    public class task extends AsyncTask<Void,String,Void>{
        @Override
        protected Void doInBackground(Void... params) {
            publishProgress("started");
            Log.d("o",""+azimuth);

            while(Math.abs(tory-azimuth)>5){}
            publishProgress("ended");
            serialPort.write("s".getBytes());
            Log.d("o",""+azimuth);

            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            tV4.append(values[0]+"\n");
            try{
            FileOutputStream fout=new FileOutputStream(new File (Environment.getExternalStorageDirectory().getPath()+"/file2.txt"),true);
            OutputStreamWriter out=new OutputStreamWriter(fout);
            out.append("// ca"+azimuth+tory);
            out.close();}
            catch (Exception e){
                printerr(e);
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            t1 = System.nanoTime();
//move and store in acceleration and t

            acceleration = 0;
            kalman(m, p, z, (x_/ Math.sqrt(x_ * x_ + y_* y_)), (y_ / Math.sqrt(x_* x_ + y_ * y_)), t1, acceleration);
        }
    }
    public void rotate(int v){
        oory=(int)ory;
        Log.d("o",""+oory);
        tory=v;
        if(tory>0)serialPort.write("r".getBytes());
        else serialPort.write("l".getBytes());
        tory+=oory;


        task t = new task();
        t.execute();


        //serialPort.write("s".getBytes());

    }




    //MENU
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.item1:
                Intent i=new Intent(WifiNavigation.this,com.roboclub.androbot.getssid.class);
                startActivity(i);
                return true;
            case R.id.item2:
                Log.d("something","done");
                AlertDialog.Builder helpBuilder = new AlertDialog.Builder(this);
                helpBuilder.setTitle("About");
                helpBuilder.setMessage("ANDROBOT IITK");
                helpBuilder.setNeutralButton("OK", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing
                    }
                });

                Log.d("something","done2");
                AlertDialog helpDialog = helpBuilder.create();
                helpDialog.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    public void getwifi(){
        WifiS wf = new WifiS();
        wf.execute();
    }



    private class WifiS extends AsyncTask<Void,String,Void> {

        int l=0;

        protected Void doInBackground(Void... Params) {


            wifi.startScan();
            List<ScanResult> res = wifi.getScanResults();
            if(l==0)
                publishProgress("2");
            publishProgress("1");
            for (ScanResult ress : res) {
                if(ress.SSID.equals(SSID1))
                    signal1=ress.level;
                else if(ress.SSID.equals(SSID2))
                    signal2=ress.level;
                else if(ress.SSID.equals(SSID3))
                    signal3=ress.level;
                else if(ress.SSID.equals(SSID4))
                    signal4=ress.level;
                else if(ress.SSID.equals(SSID5))
                    signal5=ress.level;
            }


            return null;
        }

        @Override
        protected void onProgressUpdate(String... Values){
            if(Values[0].equals("2")){
                if(toast!=null)
                    toast.cancel();
                toast= Toast.makeText(getBaseContext(),"wifi scan started",Toast.LENGTH_SHORT);
                toast.show();
            }
        }

    }











    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;


        if (sensorEvent.sensor.getType() == TYPE_ACCELEROMETER) {
            lowPass(sensorEvent.values, mAccelerometerReading);
        }
        if (sensorEvent.sensor.getType() == TYPE_MAGNETIC_FIELD) {
            lowPass(sensorEvent.values, mMagnetometerReading);
        }
        getmOrientationAngles();

    }


    public  float[] getmOrientationAngles() {
        updateOrientationAngles();

        azimuth = (int) Math.toDegrees(mOrientationAngles[0]);

        if(azimuth<0)
            azimuth+=360;
        azimuth= 360 -azimuth;

        ory=azimuth;
        Log.d("something","Azimuth = "+azimuth);
        return mOrientationAngles;
    }
    public void updateOrientationAngles() {
        // Update rotation matrix, which is needed to update orientation angles.
        SensorManager.getRotationMatrix(mRotationMatrix, null,
                mAccelerometerReading, mMagnetometerReading);
        float[] omRotationMatrix = new float[9];
        SensorManager.remapCoordinateSystem(mRotationMatrix, SensorManager.AXIS_X,SensorManager.AXIS_Z, omRotationMatrix);

        SensorManager.getOrientation(mRotationMatrix, mOrientationAngles);

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

    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    protected void onPause() {
        super.onPause();
        senSensorManager.unregisterListener(this);
    }
    protected void onResume() {
        super.onResume();
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void openFile(){
        try{
            x= new Scanner(new File(Environment.getExternalStorageDirectory().getPath()+"/file.txt"));
        }
        catch(Exception e){
            printerr(e);
            System.out.println("File not found");
        }
    }
    public void readFile(){
        int i=0;
        while(x.hasNext()){
                    data[i/12][i%12]=x.nextDouble();
            i++;

        }


    }
    public void closeFile(){
        x.close();
    }
    public double[][] get()
    {
        try{
            FileOutputStream fout=new FileOutputStream(new File (Environment.getExternalStorageDirectory().getPath()+"/file2.txt"),true);
            OutputStreamWriter out=new OutputStreamWriter(fout);
            out.append("// getst");
            out.close();}
        catch (Exception e){
            printerr(e);
        }
        // likelihood  r =new likelihood();
        openFile();
        try{
            FileOutputStream fout=new FileOutputStream(new File (Environment.getExternalStorageDirectory().getPath()+"/file2.txt"),true);
            OutputStreamWriter out=new OutputStreamWriter(fout);
            out.append("// openst");
            out.close();}
        catch (Exception e){
            printerr(e);
        }
        readFile();
        try{
            FileOutputStream fout=new FileOutputStream(new File (Environment.getExternalStorageDirectory().getPath()+"/file2.txt"),true);
            OutputStreamWriter out=new OutputStreamWriter(fout);
            out.append("// readst");
            out.close();}
        catch (Exception e){
            printerr(e);
        }
        closeFile();
        try{
            FileOutputStream fout=new FileOutputStream(new File (Environment.getExternalStorageDirectory().getPath()+"/file2.txt"),true);
            OutputStreamWriter out=new OutputStreamWriter(fout);
            out.append("// closst");
            out.close();}
        catch (Exception e){
            printerr(e);
        }
        double inp[]= new double[5];
        int i,j;
        // input wifi strength of routers
        getwifi();
        inp[0]=signal1;
        inp[1]=signal2;
        inp[2]=signal3;
        inp[3]=signal4;
        inp[4]=signal5;
//		Scanner sc=new Scanner(System.in);
//		for(i=0;i<5;i++)
//		{
//			inp[i]=sc.nextDouble();
//
//		}
        double[] prob= new double[64];
        //double[] sortprob=new double[48];
        double max;
        int in=0;
        max=prob[0];
        for(i=0;i<64;i++)
        {
            prob[i]=1;
            for(j=0;j<4;j++)
            {
                prob[i]=prob[i]*(Math.exp((-0.5)*Math.pow(((inp[j]-data[i][2+j])/data[i][7+j]),2)))/data[i][7+j];
            }
            //sortprob[i]=prob[i];
            if(prob[i]>max)
            {
                max=prob[i];
                in=i;
            }
        }
        //Arrays.sort(sortprob);
        try{
            FileOutputStream fout=new FileOutputStream(new File (Environment.getExternalStorageDirectory().getPath()+"/file2.txt"),true);
            OutputStreamWriter out=new OutputStreamWriter(fout);
            out.append("// getend");
            out.close();}
        catch (Exception e){
            printerr(e);
        }

        double z[][]=new double[2][1];

        z[0][0]=data[in][0];
        z[1][0]=data[in][1];
        return z;

    }

    public  double q[][]=new double[2][2];


    public  double b[][]=new double[3][1];
    // q to be given
    public  void  multiplication(int m,int n,int p,int q,double first[][],double second[][],double multiply[][]) throws  Exception
    {
        double sum=0;
        for (int c = 0 ; c < m ; c++ )
        {
            for ( int d = 0 ; d < q ; d++ )
            {
                for ( int k = 0 ; k < p ; k++ )
                {
                    sum = sum + first[c][k]*second[k][d];
                }

                multiply[c][d] = sum;
                sum = 0;
            }
        }

    }

    public  void kalman(double m[][],double p[][],double z[][],double cos ,double sin,double t1,double a)
    {
        try{
        double c[][]=new double[3][1];double p1[][]=new double[3][3];double p2[][]=new double[3][3];double p3[][]=new double[2][2];double k[][]=new double[3][2];
        double b[][]=new double[3][1];
        double I[][]={{1,0,0},{0,1,0},{0,0,1}};
        double p4[][]=new double[2][2];
        double p5[][]=new double[3][2];
        double k1[][]=new double[3][3];
        double A1[][]=new double[3][3];
        double t=t1;
        double dt,t2=0;
        while( System.nanoTime()-t <= 2000000000)
        {
            t2=System.nanoTime();
            dt=t2-t1;
            double A[][]={{1,0,dt*cos},{0,1,dt*sin},{0,0,1}};
            multiplication(4,4,4,1,A,m,c);
            b[0][0]=(.5)*a*dt*dt*cos;
            b[1][0]=(.5)*a*dt*dt*sin;
            b[2][0]= a*dt;
            a=0;
            t1=t2;

            for(int i=0;i<3;i++)
                m[i][0]=c[i][0]+b[i][0];
        }
        serialPort.write("s".getBytes());
// swich off arduino
        dt=System.nanoTime()-t2;
        double A[][]={{1,0,dt*cos},{0,1,dt*sin},{0,0,1}};
        multiplication(4,4,4,1,A,m,c);
        b[0][0]=(.5)*a*dt*dt*cos;
        b[1][0]=(.5)*a*dt*dt*sin;
        b[2][0]= a*dt;
// a=imu_accelerator();


        for(int i=0;i<3;i++)
            m[i][0]=c[i][0]+b[i][0];

        multiplication(3,3,3,3,A,p,p1);
        multiplication(3,3,3,3,p1,A1,p2);
        for(int i=0;i<2;i++)
        {
            for(int j=0;j<2;j++)
            {
                p4[i][j]=p2[i][j];
            }
        }
        for(int q1=0;q1<2;q1++)
        {
            for(int q2=0;q2<2;q2++)
            {
                p3[q1][q2]=q[q1][q2]+p4[q1][q2];
            }
        }
//invert(p3);
        double det=p3[0][0]*p3[1][1]-p3[0][1]*p3[1][0];
        double  l;
        l=p3[0][0];
        p3[0][0]=p3[1][1]/det;
        p3[1][1]=l/det;
        p3[1][0]=-p3[1][0]/det;
        p3[0][1]=-p3[0][1]/det;

        for(int i=0;i<3;i++)
        {
            for(int j=0;j<2;j++)
            {
                p5[i][j]=p2[i][j];
            }
        }
        multiplication(3,2,2,2,p5,p3,k);
        //likelihood obj=new likelihood();
        z=get();
        for(int q2=0;q2<2;q2++)
        {
            z[q2][0]=z[q2][0]-m[q2][0];
        }
        multiplication(3,2,2,1,k,z,c);
        for(int q2=0;q2<3;q2++)
        {
            m[q2][0]=m[q2][0]+c[q2][0];
        }
        for(int q1=0;q1<3;q1++)
        {
            for(int q2=0;q2<2;q2++)
            {
                I[q1][q2]=I[q1][q2]-k[q1][q2];
            }
        }
        multiplication(4,4,4,4,I,p2,p);
        }
        catch(Exception e){
            printerr(e);
        }

    }






    //PERMISSIONS


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
