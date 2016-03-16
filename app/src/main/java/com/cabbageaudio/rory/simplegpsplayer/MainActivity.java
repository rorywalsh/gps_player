package com.cabbageaudio.rory.simplegpsplayer;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Rect;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.location.LocationListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.lang.Math;
import android.content.pm.ActivityInfo;

import csnd6.AndroidCsound;
import csnd6.CsoundPerformanceThread;

public class MainActivity extends AppCompatActivity implements LocationListener {

    private LocationManager locationManager;
    double xCoor = 0.0;
    double latitude = 0.0;
    double longitude = 0.0;
    double yCoor = 0.0;
    int coordinateCounter=0;
    String currentText="";
    String xCoorString="";
    String yCoorString="";
    Boolean audioIsPlaying = false;
    int compileResult = -1;
    private AndroidCsound csound;
    private CsoundPerformanceThread perfThread;

        public class Points {
            public Points(double x1, double y1, double width1, double height1) {
                x = x1;
                y =y1;
                width = width1;
                height = height1;
            }

            double x, y, width, height;
        }

    List<Points> coordinatePoints = new ArrayList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TextView savedText = (TextView)  findViewById(R.id.savedText);
        savedText.setMovementMethod(new ScrollingMovementMethod());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                //        .setAction("Action", null).show();
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                TextView savedText = (TextView)  findViewById(R.id.savedText);
                String shareBody = savedText.getText().toString();
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject Here");
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(sharingIntent, "Share via"));
            }
        });

        copyAssets();
        csound = new AndroidCsound();
        // Using SetOption() to configure Csound
        // Note: use only one commandline flag at a time
        csound.SetOption("-odac");
        ((AndroidCsound) csound).setOpenSlCallbacks();
        //Toast.makeText(getBaseContext(), "Hello", Toast.LENGTH_LONG).show();
        LocationManager locManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        LocationListener locListener = this;
        locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locListener);
//            File dir = new File(sdCard.getAbsolutePath() + "/seamus_app/");
    }

    public void buttonOnClick(View view) {
        switch (view.getId()) {
            case R.id.snapCoordinatesButton: {
                TextView savedText = (TextView) findViewById(R.id.savedText);
                coordinateCounter++;
                coordinatePoints.add(new Points(xCoor, yCoor, 1, 1));
//                coordinateRectangles.add(new Rect(0,0,0,0));
                String text = coordinateCounter + " Lat: " + xCoorString + "\n" + coordinateCounter + " Lon: " + yCoorString + "\n";
                String currentText = text + savedText.getText();
                savedText.setText(currentText);
                Toast.makeText(getBaseContext(), "Coordinates saved", Toast.LENGTH_LONG).show();
                currentText = savedText.getText().toString();
                break;
            }
            case R.id.clearCoordinatesButton: {
                TextView savedText = (TextView) findViewById(R.id.savedText);
                savedText.setText("");
                Toast.makeText(getBaseContext(), "Coordinates cleared", Toast.LENGTH_LONG).show();
                coordinateCounter = 0;
                break;
            }
            case R.id.playButton: {
                audioIsPlaying = !audioIsPlaying;
                if (audioIsPlaying == true) {
                    File sdcard = Environment.getExternalStorageDirectory();
                    File f = new File(sdcard.getAbsolutePath() + "/gps_player/gps_player.csd");
                    Log.d("========================", f.toString());

                    compileResult = csound.Compile(f.getAbsolutePath());
                    if (compileResult == 0) {
                        csound.Start();
                        perfThread = new CsoundPerformanceThread(csound);
                        csound.SetChannel("PositionPoint", 0);
                        perfThread.Play();
                    }
                } else {
                    //csound.RewindScore();
                    perfThread.Stop();
                    perfThread.Join();
                }

                break;
            }

            case R.id.saveCoordinatesButton: {
                TextView savedText = (TextView) findViewById(R.id.savedText);
                String fileText = savedText.getText().toString();
                File sdcard = Environment.getExternalStorageDirectory();
                File dir = new File(sdcard.getAbsolutePath() + "/gps_player/");
                dir.mkdir();

                if (dir.exists()) {
                    Date date = new Date();
                    SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String dateString = fmt.format(date);
                    String fileName = dir.getAbsolutePath()+"/gps_player" + dateString + ".txt";
                    File file = new File(fileName);

                    try {
                        FileOutputStream os = new FileOutputStream(file);
                        try {
                            os.write(fileText.getBytes());
                            os.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Toast.makeText(getBaseContext(), "Coordinates saved..", Toast.LENGTH_LONG).show();

                    } catch (FileNotFoundException fnfe) {
                        System.out.println(fnfe.getMessage());
                    }

                } else {

                }
                break;
            }

        }


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        perfThread.delete();
        csound.delete();
    }

    private void copyAssets()
    {
        String fileNames="";
        AssetManager assetManager = getAssets();
        String[] files = null;
        try {
            files = assetManager.list("");
        }
        catch (IOException e) {
            //showNativeMessage("Error", "Failed to open assets");
        }



        for(String filename : files) {
            InputStream in = null;
            OutputStream out = null;
            try {


                if(filename.contains(".csd"))
                {
                    in = assetManager.open(filename);
                    //showNativeMessage(convertStreamToString(in));
                    File dir = new File(Environment.getExternalStorageDirectory() + "/gps_player/");
                    dir.mkdir();
                    File outFile = new File(dir.getAbsolutePath()+"/"+filename);
                    out = new FileOutputStream(outFile);
                    copyFile(in, out);
                    in.close();
                    in = null;
                    out.flush();
                    out.close();
                    out = null;
                }

            }
            catch(IOException e) {

            }
        }
    }

    private void copyFile(InputStream in, OutputStream out) throws IOException
    {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /************* Called after each 3 sec **********/
    @Override
    public void onLocationChanged(Location location) {
        if(audioIsPlaying==false) {
            TextView textEdit = (TextView) findViewById(R.id.textView);
            xCoor = location.getLatitude();
            yCoor = location.getLongitude();
            xCoorString = String.format("%.6f", xCoor);
            yCoorString = String.format("%.6f", yCoor);
            String str = "Latitude: " + xCoorString + "\nLongitude: " + yCoorString + "\n";
            textEdit.setText(str);
            //Toast.makeText(getBaseContext(), str, Toast.LENGTH_LONG).show();
        }
        else
        {
            double currentLocationY = location.getLongitude();
            double targetLocationY = coordinatePoints.get(0).y;
            double yDistance = targetLocationY-currentLocationY;
            TextView textEdit2 = (TextView) findViewById(R.id.distanceView);

            textEdit2.setText("Dis:" + String.format("%.6f", yDistance));

            if(Math.abs(yDistance)>0.000200)
            {

                Toast.makeText(getBaseContext(), "Setting to 1", Toast.LENGTH_LONG).show();
                csound.SetChannel("PositionPoint", 1);
            }
            else{
                csound.SetChannel("PositionPoint", 0);
            }
        }

    }

    @Override
    public void onProviderDisabled(String provider) {

        /******** Called when User off Gps *********/

        Toast.makeText(getBaseContext(), "Gps turned off ", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onProviderEnabled(String provider) {

        /******** Called when User on Gps  *********/

        Toast.makeText(getBaseContext(), "Gps turned on ", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub

    }

}
