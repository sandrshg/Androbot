package com.roboclub.androbot;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Scanner;

public class Location extends AppCompatActivity {

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

    Button getLoc;
    TextView loc;
    EditText ntxt;

    Toast toast;
    static int n;
    int r;
    public static double data[][];

    private Scanner x;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        //checkAndRequestPermissions();

        wifi=(WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifi.setWifiEnabled(true);

        getLoc = (Button) findViewById(R.id.button2);
        loc = (TextView) findViewById(R.id.textView4);
        ntxt = (EditText) findViewById(R.id.editText3);



    }

    public void onbclick(View V){
        n= Integer.parseInt(ntxt.getText().toString());
        r= (int)Math.sqrt(n);

        data =new double[n][12];
        double Loc[][] = get();
        loc.setText("X: "+Loc[0][0]+"Y: "+Loc[1][0]);
    }

    public void openFile(){
        try{
            x= new Scanner(new File(Environment.getExternalStorageDirectory().getPath()+"/file.txt"));
        }
        catch(Exception e){
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
    public double[][] get() {
        openFile();
        readFile();
        closeFile();
        double inp[] = new double[5];
        int i, j;
        double cx,cy;
        // input wifi strength of routers
        getwifi();
        inp[0] = signal1;
        inp[1] = signal2;
        inp[2] = signal3;
        inp[3] = signal4;
        inp[4] = signal5;

        double min=0,curr=0;
        int in=0;
        for(i=0;i<n-r-1;i++)
        {
            curr=0;
            for(j=0;j<4;j++)
            {
                curr+=Math.pow(Math.abs(inp[j]-data[i][2+j])+data[i][7+j],2)+Math.pow(Math.abs(inp[j]-data[i+1][2+j])+data[i+1][7+j],2)+Math.pow(Math.abs(inp[j]-data[i+r][2+j])+data[i+r][7+j],2)+Math.pow(Math.abs(inp[j]-data[i+r+1][2+j])+data[i+r+1][7+j],2);
            }
            if(i==0)
                min=curr;
            if(curr<min) {
                min = curr;
                in = i;
            }
        }
        cx=(in%r)+0.5;
        cy=(in/r)+0.5;

        double[][] z= new double[2][1];
        z[0][0]=cx;
        z[1][0]=cy;
        return z;
    }


        @Override
        public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu,menu);
        return true;
    }

        @Override
        public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.item1:
                Intent i=new Intent(Location.this,com.roboclub.androbot.getssid.class);
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
        Location.WifiS wf = new Location.WifiS();
        wf.execute();
    }



    private class WifiS extends AsyncTask<Void,String,Void> {

        int l = 0;

        protected Void doInBackground(Void... Params) {


            wifi.startScan();
            List<ScanResult> res = wifi.getScanResults();
            if (l == 0)
                publishProgress("2");
            publishProgress("1");
            for (ScanResult ress : res) {
                if (ress.SSID.equals(SSID1))
                    signal1 = ress.level;
                else if (ress.SSID.equals(SSID2))
                    signal2 = ress.level;
                else if (ress.SSID.equals(SSID3))
                    signal3 = ress.level;
                else if (ress.SSID.equals(SSID4))
                    signal4 = ress.level;
            }


            return null;
        }

        @Override
        protected void onProgressUpdate(String... Values) {
            if (Values[0].equals("2")) {
                if (toast != null)
                    toast.cancel();
                toast = Toast.makeText(getBaseContext(), "wifi scan started", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }
}

