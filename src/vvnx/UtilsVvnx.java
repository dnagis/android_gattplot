package vvnx.gattplot;

//sql
import android.database.sqlite.SQLiteDatabase;
import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import android.bluetooth.BluetoothGattCharacteristic;

	/**
	 * 
	* juste une zone de stockage de fonctions "utils" pour essayer d'alléger le code de la classe GattService
	 * 
	 * 
	 * 
	 * **/


public class UtilsVvnx  {
	
	private final String TAG = "GattPlot";
	
	//sql
    private BaseDeDonnees maBDD;
    private SQLiteDatabase bdd;
	
	//anémo (esp32)
	public void parseAnemo(Context context, byte[] data) {			
			//int valeur = (data[0] & 0xFF) << 8 | (data[1] & 0xFF); //avant je faisais un encodage dans 2 bytes
			Log.i(TAG, "parseAnemo data: " + data[0]);				
			long ts = System.currentTimeMillis()/1000;
			logCountEnBdd(context, ts, data[0]);
	 }
	 
	 private void logCountEnBdd(Context context, long ts, int count) {
		//sqlite3 /data/data/vvnx.gattplot/databases/data.db "select datetime(ALRMTIME, 'unixepoch','localtime'), COUNT from envdata;"
		maBDD = new BaseDeDonnees(context);
		bdd = maBDD.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("ALRMTIME", ts);
		values.put("COUNT", count);
		bdd.insert("envdata", null, values);
	}

	
	
	
	
}
