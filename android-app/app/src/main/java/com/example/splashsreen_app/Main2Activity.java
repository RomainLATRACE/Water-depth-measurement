package com.example.splashsreen_app;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Debug;
import android.os.Handler;
import android.support.annotation.DrawableRes;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.io.Console;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;



public class Main2Activity extends AppCompatActivity {

    private ActionBar actionBar;

    Button buttonlist;  //button for data refresh,
    TextView NumberSampleTxt;
    TextView FreqSampleTxt;
    SeekBar seekBar;    //to manage the number of samples visible on the praphic
    SeekBar seekBar2;
    TextView txt;   //display of the current depth
    SwitchCompat switch_button; //used for auto sampling

    Handler handler = new Handler(); //Handlers must be used to perform calculations before updating the graphical interface
    boolean statusACK = true;

    /*Graph*/
    private LineChart mChart;   //values display
    private int mFillClor = Color.argb(150, 51, 181, 229);
    int current_range=10; //number of samples displayed on the graph
    int last_range; //corresponds to the number of samples shown on the graph before refresh
    List<Float> ordonnees = new ArrayList<>(current_range); //store the values of the samples
    /*end graph*/
    int data_frequency; //refresh frequency (ms)


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        /*action bar*/
        actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#FFFFFF")));
        actionBar.setTitle("Ultrasonic sensor Controller");
        /*end action bar*/

        /*correspondence between graphic elements and objects*/
        switch_button = findViewById(R.id.switch_enable_button);
        buttonlist = (Button) findViewById(R.id.button_list);
        txt = (TextView) findViewById(R.id.txt);
        NumberSampleTxt= findViewById(R.id.textSampNumber);
        FreqSampleTxt= findViewById(R.id.textFreqNumber);
        seekBar = findViewById(R.id.seekbar);
        seekBar2 = findViewById(R.id.seekbar2);
        /*end correspondence*/


        /*#####################################SWITCH#############################################*/
        switch_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (switch_button.isChecked()){
                    /*To be uncommented for a periodic refresh*/
                    handler.postDelayed(actualizeStatus, 0);
                    buttonlist.setEnabled(false);
                    Toast.makeText(Main2Activity.this, "Autosampling enable", Toast.LENGTH_SHORT).show();

                }else{
                    Toast.makeText(Main2Activity.this, "Autosampling disable", Toast.LENGTH_SHORT).show();

                    handler.removeCallbacksAndMessages(null);   //To ignore messages
                    buttonlist.setEnabled(true);
                }
            }
        });
        /*########################################################################################*/



        /*######################################SeekBar###########################################*/
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progress+=10;//10 is the minimun number of values
                NumberSampleTxt.setText("" + progress + "");
                current_range=progress;
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //Toast.makeText(Main2Activity.this, "Sampling:" + current_range, Toast.LENGTH_SHORT).show();
            }
        });
        /*########################################################################################*/

        /*######################################SeekBar2###########################################*/
        seekBar2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar2, int samplingFrequency, boolean fromUser) {
                samplingFrequency+=1;
                FreqSampleTxt.setText("" + samplingFrequency + "");
                switch (samplingFrequency){
                    case 1:
                        data_frequency=500;
                        break;
                    case 2:
                        data_frequency=250;
                        break;
                    case 3:
                        data_frequency=125;
                        break;
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar2) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar2) {
            }
        });
        /*########################################################################################*/

        buttonlist.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Demande("led");//sends "led" when button is clicked (why led? just to send something^^)
            }
        });






        /*#######################################Graph############################################*/
        mChart = (LineChart) findViewById(R.id.mChart); //graph creation
        mChart.setContentDescription("");
        mChart.setNoDataText("No data for the moment");
        mChart.setNoDataTextColor(Color.parseColor("#000000"));
        mChart.setBackgroundColor(Color.parseColor("#F8F9F9"));                //background (sides) color
        mChart.setGridBackgroundColor(Color.parseColor("#ffffff"));             //grid background
        mChart.setDrawGridBackground(true); //draw a grid

        mChart.setDrawBorders(true);
        mChart.getDescription().setEnabled(false);  //insert a label for the graph
        mChart.setPinchZoom(true);  //zoom?



        /*legend creation*/
        Legend l = mChart.getLegend();
        l.setEnabled(true);
        l.setForm(Legend.LegendForm.LINE);
        l.setTextColor(Color.BLACK);

        /*axes management*/
        YAxis leftAxis = mChart.getAxisLeft();
        YAxis rightAxis = mChart.getAxisRight();
        leftAxis.setAxisMinimum(0);
        rightAxis.setAxisMinimum(0);

        XAxis x1 = mChart.getXAxis();
        x1.setAvoidFirstLastClipping(true);


        /*#######################################################################################"*/

    }


    /*For the frequency of data transmission*/
    private Runnable actualizeStatus = new Runnable() {
        @Override
        public void run() {
            if(statusACK) {
                Demande("led");
                handler.postDelayed(this, data_frequency);

                Log.d("Status", "Demande");
            }else{
                handler.removeCallbacks(actualizeStatus);

                Log.d("Status", "Termine");
            }
        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();
        statusACK = false;
    }





    /**/
    public void Demande (String comand){
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService((Context.CONNECTIVITY_SERVICE));
        NetworkInfo networkinfo = connMgr.getActiveNetworkInfo(); //connexion to the device

        if (networkinfo != null && networkinfo.isConnected()){

            String url = "http://192.168.43.101/";  //IP address of the device, known with the arduino code
            new Requete().execute(url + comand);

        }else{
            Toast.makeText(Main2Activity.this, "Not connected", Toast.LENGTH_LONG).show();
        }
    }

    /*http request*/
    private  class Requete extends AsyncTask<String, Void, String>{
        @Override
        protected String doInBackground(String... url) {
            return Connexion.getData(url[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null){    //if we received something

                //button changements
                if(result.contains("led is ON")){
                    //buttonlist.setText("LED is ON / Press to refresh");
                    buttonlist.setActivated(true);
                }else if(result.contains("led is OFF")){
                    //buttonlist.setText("LED is OFF / Press to refresh");
                    buttonlist.setActivated(false);
                }

                String[] data_received = result.split(": ");    //extraction of the depth value
                float data_recue = Float.valueOf(data_received[1]);
                DecimalFormat df = new DecimalFormat();
                df.setMaximumFractionDigits(1); //decrease in the number of decimals
                if (data_recue < 1.0){data_recue=0;}
                txt.setText(String.valueOf(df.format(data_recue)));

                setData(((int)(data_recue*10))/10);   //"*(-1)" to have a reverse graph


            }else{
                Toast.makeText(Main2Activity.this, "Error", Toast.LENGTH_LONG).show();
            }

        }
    }




    /*####################################Graphique###############################################*/
    private void setData (float SensorValue){
        ArrayList<Entry> yVals= new ArrayList<>(); //each time we create a new one

        if (ordonnees.size() >= current_range) {
            if (current_range < last_range) {//if we move the cursor to the left
                int diff_btw_ranges = last_range - current_range; //the operation is to know how many cells to remove from the list

                if (diff_btw_ranges + 10 > ordonnees.size()) {//"+10" cause we want to keep 10 values in the graph
                    Toast.makeText(Main2Activity.this, "Wait to have more samples...", Toast.LENGTH_LONG).show();
                } else {
                    for (int j = 0; j < diff_btw_ranges; j++) {
                        ordonnees.remove(0);//we remove "diff_btw_ranges"times the first cell
                    }
                }
            }
        }
        ordonnees.add(SensorValue); //we always add the current value

        for (int i =0; i < ordonnees.size(); i++){
            float value= ordonnees.get(i);
            yVals.add(new Entry(i, value));
        }

        //System.out.println("size: "+ ordonnees.size());
        if (ordonnees.size() > current_range) {
            ordonnees.remove(0);
        }

        //System.out.println("current_range = " + current_range);
        //System.out.println("last_range = " + last_range);
        last_range = current_range;






        LineDataSet set1;
        XAxis x1= mChart.getXAxis();
        x1.setAvoidFirstLastClipping(true);

        set1 = new LineDataSet(yVals, "Water depth evolution");
        x1.setAxisMinimum(0);
        set1.setAxisDependency(YAxis.AxisDependency.LEFT);
        set1.setColor(Color.parseColor("#34495e"));
        set1.setDrawCircleHole(false);
        set1.setDrawCircles(true);  //points?
        set1.setCircleColor(Color.parseColor("#5DADE2"));
        set1.setLineWidth(2.5f);
        set1.setFillAlpha(125); //field transparency
        set1.setDrawFilled(true); //field creation?
        set1.setFillColor(Color.parseColor("#34495e")); //field color

        LineData data = new LineData(set1);
        data.setDrawValues(true);  //display the values of each point
        data.setValueTextColor(Color.BLACK);
        data.setValueTextSize(6.5f);

        mChart.setData(data); //graph writing
    }
    /*############################################################################################*/



}
