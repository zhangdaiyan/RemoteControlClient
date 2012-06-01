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
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class RemoteGsensorControlActivity extends Activity implements SensorEventListener{
    /** Called when the activity is first created. */
	
	private SensorManager sensorManager;
	static private final String LOG_TAG = "RemoteGsensorControlActivity";   
    private static final int PORT_SENSOR = 8888;   
    private static final int PORT_MOUSE = 8889;
    private PowerManager.WakeLock mWakeLock = null;
    private DatagramSocket mDs;
    private EditText mEditText;
    private Button mStartSensorButton;
    private Button mStopSensorButton;
    private Button mStartMouseButton;
    private Button mStopMouseButton;
    private Button mHomeButton;
    private Button mMenuButton;
    private Button mBackButton;
    private View mTouchPad;
    private boolean mSensorIsStarted = false;
    private boolean mMouseIsStarted = false;
    
    private static boolean DEBUG = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);  
                
        setContentView(R.layout.main);
        
        mEditText = (EditText)findViewById(R.id.ip);
        mStartSensorButton = (Button)findViewById(R.id.start_sensor);
        mStopSensorButton = (Button)findViewById(R.id.stop_sensor);
        mStartMouseButton = (Button)findViewById(R.id.start_mouse);
        mStopMouseButton = (Button)findViewById(R.id.stop_mouse);
        mHomeButton = (Button)findViewById(R.id.home);
        mMenuButton = (Button)findViewById(R.id.menu);
        mBackButton = (Button)findViewById(R.id.back);
        mTouchPad = (View)findViewById(R.id.touch_pad);        
        
        PowerManager pMgr = (PowerManager)getSystemService(POWER_SERVICE);    
        mWakeLock = pMgr.newWakeLock(PowerManager.FULL_WAKE_LOCK,
        		
                "test wakelock.");
        try {
        	mDs = new DatagramSocket();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        initSensorControl();
        initMouseControl();
        initButtonControl();
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
    	if(mSensorIsStarted && e.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
	        float x = e.values[SensorManager.DATA_X];
	        float y = e.values[SensorManager.DATA_Y];
	        float z = e.values[SensorManager.DATA_Z];
	        
	        
	        if(DEBUG)
	        	Log.d(LOG_TAG, "Sensor ip=" + mEditText.getText().toString() + ",type=" + e.sensor.getType() + ",x,y,z=" + x + "," + y + "," + z);
	        
	       // String str =  -y + "," + x + "," + z + "," + e.accuracy + "," + e.timestamp;    
	        String str =  x + "," + y + "," + z + "," + e.accuracy + "," + e.timestamp;    
	        sendMsg(str, PORT_SENSOR);
    	}
    	
    }

	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}  
	
	private void initSensorControl()
	{
		sensorManager = (SensorManager)this.getSystemService(SENSOR_SERVICE);
        List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
        
        //sensor1
        for (Sensor s : sensors){
            sensorManager.registerListener(this,s,SensorManager.SENSOR_DELAY_NORMAL);
        }  
               		
        mStartSensorButton.setOnClickListener(new Button.OnClickListener() {   
        	public void onClick(View v) 
        	{   
        		mSensorIsStarted = true;
        		if(DEBUG)
        			Log.d(LOG_TAG, "start");
        	}         
        });

        mStopSensorButton.setOnClickListener(new Button.OnClickListener() { 
        	public void onClick(View v) 
        	{   
        		mSensorIsStarted = false;
        		if(DEBUG)
        			Log.d(LOG_TAG, "stop");
        	}         
        });  
	}
	
	private void initMouseControl()
	{
		mStartMouseButton.setOnClickListener(new Button.OnClickListener() {   
        	public void onClick(View v) 
        	{   
        		mMouseIsStarted = true;
        		if(DEBUG)
        			Log.d(LOG_TAG, "start");
        		
        		sendMsg("start", PORT_MOUSE);
        	}         
        });

        mStopMouseButton.setOnClickListener(new Button.OnClickListener() { 
        	public void onClick(View v) 
        	{   
        		mMouseIsStarted = false;
        		if(DEBUG)
        			Log.d(LOG_TAG, "stop");
        		
        		sendMsg("stop", PORT_MOUSE);
        	}         
        }); 
        
        mTouchPad.setOnTouchListener(new View.OnTouchListener() {			
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				int act = event.getAction();
				if(mMouseIsStarted && (act == MotionEvent.ACTION_DOWN || act == MotionEvent.ACTION_UP || act == MotionEvent.ACTION_MOVE)){
					for(int i = 0; i < 1 /*1event.getPointerCount()*/; i++){
						double x = event.getX(i);
						double y = event.getY(i);
						double w = mTouchPad.getWidth();
						double h = mTouchPad.getHeight();
						
						
						int trans_act = 0;
						if(event.getAction() == MotionEvent.ACTION_DOWN)
							trans_act = 0;
						if(event.getAction() == MotionEvent.ACTION_UP)
							trans_act = 1;
						if(event.getAction() == MotionEvent.ACTION_MOVE)
							trans_act = 2;
						
						String str = i + "," + trans_act + "," + x/w + "," + y/h;
						Log.d(LOG_TAG, str);
						
						sendMsg(str, PORT_MOUSE);
					}
				}
				
				return true;
			}
		});
	}
	
	void initButtonControl()
	{
		mHomeButton.setOnClickListener(new Button.OnClickListener() {   
        	public void onClick(View v) 
        	{   
        		if(DEBUG)
        			Log.d(LOG_TAG, "Home");     		
        		sendMsg("home", PORT_MOUSE);
        	}         
        });

        mMenuButton.setOnClickListener(new Button.OnClickListener() { 
        	public void onClick(View v) 
        	{   
        		if(DEBUG)
        			Log.d(LOG_TAG, "Menu");
        		sendMsg("menu", PORT_MOUSE);
        	}         
        }); 
		
        mBackButton.setOnClickListener(new Button.OnClickListener() { 
        	public void onClick(View v) 
        	{   
        		if(DEBUG)
        			Log.d(LOG_TAG, "Back");
        		sendMsg("back", PORT_MOUSE);
        	}         
        }); 
	}
	
	private void sendMsg(String msg, int port)
	{
		try {
			DatagramPacket dp = new DatagramPacket(msg.getBytes(), msg.length(), InetAddress.getByName(mEditText.getText().toString()), port);
			mDs.send(dp);
		}catch (Exception e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}	
	}
}