package com.jm.RemoteGsensorControl;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.List;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class RemoteGsensorControlActivity extends Activity implements SensorEventListener{
    /** Called when the activity is first created. */
	
	private SensorManager sensorManager;
	static private final String LOG_TAG = "RemoteGsensorControlActivity";   
    private static final int PORT = 8888;   
    private PowerManager.WakeLock mWakeLock = null;
    private DatagramSocket ds;
    private EditText mEditText;
    private Button mStartButton;
    private Button mStopButton;
    private boolean mIsStarted = false;
    
    private static boolean DEBUG = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);  
        
        sensorManager = (SensorManager)this.getSystemService(SENSOR_SERVICE);
        List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
        
        //sensor1
        for (Sensor s : sensors){
            sensorManager.registerListener(this,s,SensorManager.SENSOR_DELAY_NORMAL);
        }  
        
        try {
			ds = new DatagramSocket();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        setContentView(R.layout.main);
        
        mEditText = (EditText)findViewById(R.id.ip);
        mStartButton = (Button)findViewById(R.id.start);
        mStopButton = (Button)findViewById(R.id.stop);

        mStartButton.setOnClickListener(new Button.OnClickListener() {   
        	public void onClick(View v) 
        	{   
        		mIsStarted = true;
        		if(DEBUG)
        			Log.d(LOG_TAG, "start");
        	}         
        });

        mStopButton.setOnClickListener(new Button.OnClickListener() { 
        	public void onClick(View v) 
        	{   
        		mIsStarted = false;
        		if(DEBUG)
        			Log.d(LOG_TAG, "stop");
        	}         
        });  
        
        PowerManager pMgr = (PowerManager)getSystemService(POWER_SERVICE);    
        mWakeLock = pMgr.newWakeLock(PowerManager.FULL_WAKE_LOCK,
                "test wakelock.");
        
        mWakeLock.acquire();
    }
    
    @Override
	public void onPause()
	{
    	if(DEBUG)
    		Log.d(LOG_TAG, "onPause");
		super.onPause();
		mWakeLock.release();
	}
	
	@Override
	public void onResume()
	{
		if(DEBUG)
			Log.d(LOG_TAG, "onResume");
		super.onResume();
		mWakeLock.acquire();
	}
    
    public void onSensorChanged(SensorEvent e) {
    	if(mIsStarted && e.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
	        float x = e.values[SensorManager.DATA_X];
	        float y = e.values[SensorManager.DATA_Y];
	        float z = e.values[SensorManager.DATA_Z];
	        
	        if(DEBUG)
	        	Log.d(LOG_TAG, "ip=" + mEditText.getText().toString() + ",type=" + e.sensor.getType() + ",x,y,z=" + x + "," + y + "," + z);
	        
	        String str =  -y + "," + x + "," + z + "," + e.accuracy + "," + e.timestamp;
	        
			try {
				DatagramPacket dp = new DatagramPacket(str.getBytes(), str.length(), InetAddress.getByName(mEditText.getText().toString()), PORT);
				ds.send(dp);
			}catch (Exception e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
    	}
    }

	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}  
}