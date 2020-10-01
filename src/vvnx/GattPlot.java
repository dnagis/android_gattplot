/*
 * adb uninstall vvnx.gattplot
 * adb install out/target/product/generic_arm64/system/app/GattPlot/GattPlot.apk
 */

package vvnx.gattplot;


import android.app.Activity;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.WindowManager;
import java.text.SimpleDateFormat;
import java.util.Date; 

import android.widget.Button;
import android.widget.TextView;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.util.Log;

import android.os.Message;
import android.os.Messenger;
import android.os.Handler;
import android.os.RemoteException;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.ComponentName;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

import android.graphics.*;
//import android.graphics.Color; //était dans bluevvnx, mais comme j'import graphics.* maintenant...
import android.graphics.drawable.Drawable;

import com.androidplot.util.PixelUtils;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYSeries;
import com.androidplot.xy.*;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.*;

//sql
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.Cursor;

public class GattPlot extends Activity {
	
	private static final String TAG = "GattPlot";
	boolean mSceBound = false;
	Messenger mService = null;

	private Drawable default_btn;	
	TextView textview1, textview2, textview3;
	private XYPlot plot;
	
	private BaseDeDonnees maBDD;



    @Override
    public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate()");
        super.onCreate(savedInstanceState);

        View view = getLayoutInflater().inflate(R.layout.main_activity, null);
        setContentView(view);
        
        textview1 = findViewById(R.id.text1);	
        textview2 = findViewById(R.id.text2);
        textview3 = findViewById(R.id.text3);
        
        //récup le background par default du bouton pour le remettre
        Button button1 = findViewById(R.id.button_1);
        default_btn = button1.getBackground();   
        

       
     
    }
    
    @Override
	public void onResume(){
	    super.onResume();
	    Log.d(TAG, "onResume()");
	    
        plot = (XYPlot) findViewById(R.id.plot);        
        //Des valeurs static pour test
        //List<Integer> myVals = Arrays.asList(new Integer[]{1601280124, 2, 1601280224, 3, 1601280524, 4, 1601280624, 5});        
        
        maBDD = new BaseDeDonnees(this);
        List<Integer> myVals = maBDD.fetchInterleavedEpochValue();        
		
		XYSeries my_series = new SimpleXYSeries(myVals, SimpleXYSeries.ArrayFormat.XY_VALS_INTERLEAVED, "my series");			
        plot.setDomainStep(StepMode.SUBDIVIDE, 4);
		LineAndPointFormatter series1Format = new LineAndPointFormatter(this, R.xml.point_formatter);
		plot.clear();
        plot.addSeries(my_series, series1Format);
        plot.getGraph().setLineLabelRenderer(XYGraphWidget.Edge.BOTTOM, new MyLineLabelRenderer()); 	    
	    
	    
	    
	    
	
	}
    
    public void ActionPressBouton_1(View v) {
		Log.d(TAG, "press bouton 1");
		Intent i = new Intent(this, GattService.class);
        startService(i); 
        bindService(i, connection, Context.BIND_AUTO_CREATE); //Déclenche onBind() dans le service

	}
	
	public void ActionPressBouton_2(View v) {
		Log.d(TAG, "press bouton 2");
		Message msg = Message.obtain(null, GattService.MSG_STOP);
        try {
               mService.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

	}
	
	public void updateConnText(String bdaddr) {
        Date d = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");	//dd MMM HH:mm:ss
		textview1.setText("Last connect: "+ sdf.format(d));
		textview2.setText(bdaddr);
    }
    
    public void updateNotifText(int data) {
        Date d = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		textview3.setText("Last notif:     "+ sdf.format(d) + "  " + data);
    }
    
    public void btn1_to_blue() {
	    Button button1 = findViewById(R.id.button_1);
	    button1.setBackgroundColor(Color.BLUE);
	}
	
	public void btn1_to_def() {
	    Button button1 = findViewById(R.id.button_1);
	    button1.setBackgroundDrawable(default_btn);
	}
	
	/**
	 * système IPC Messenger / Handler basé sur le Binder
	 */ 
	 
	private Handler mIncomingHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GattService.MSG_BT_CONNECTED:
					String bdaddr = msg.getData().getString("bdaddr");
                    Log.d(TAG, "BlueActivity: handler -> MSG_BT_CONNECTED: " + bdaddr);
                    btn1_to_blue();
                    updateConnText(bdaddr);                    
                    break;
                case GattService.MSG_BT_DISCONNECTED:
                    Log.d(TAG, "BlueActivity: handler -> MSG_BT_DISCONNECTED");
                    btn1_to_def();
                    break;
                case GattService.MSG_BT_NOTIF:
                    Log.d(TAG, "BlueActivity: handler -> MSG_BT_NOTIF");
                    updateNotifText(msg.arg1);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    };		
	
	private final Messenger mMessenger = new Messenger(mIncomingHandler);
	
	/** callbacks for service binding */
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
			Log.d(TAG, "onServiceConnected()");
            mService = new Messenger(service); 
            mSceBound = true;
            //Enregistrer un handler ici pour que le service puisse l'appeler, l'envoyer au service
            Message msg = Message.obtain(null, GattService.MSG_REG_CLIENT);
            msg.replyTo = mMessenger; //pour dire au service où envoyer ses messages
            try {
                mService.send(msg);
            } catch (RemoteException e) {
                Log.w(TAG, "Unable to register client to service.");
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
			Log.d(TAG, "onServiceDisconnected()");
			mService = null;
			mSceBound = true;
        }
    };
    
    
	class MyLineLabelRenderer extends XYGraphWidget.LineLabelRenderer {
        @Override
        protected void drawLabel(Canvas canvas, String text, Paint paint, float x, float y, boolean isOrigin) {
                long epoch = (long)Double.parseDouble(text.replaceAll(",",".")); 
				Date date = new Date( epoch * 1000);
				SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");                
                text = sdf.format(date);  
                super.drawLabel(canvas, text, paint, x, y , isOrigin);
        }
    }
	
}

