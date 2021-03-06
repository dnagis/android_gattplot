package vvnx.gattplot;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

import java.time.LocalTime;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

import android.util.Log;


//sqlite3 /data/data/vvnx.gattplot/databases/data.db "select * from envdata"



public class BaseDeDonnees extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "data.db";
    private static final int DATABASE_VERSION = 1;
    private SQLiteDatabase bdd;
    //private static final String CREATE_BDD = "CREATE TABLE loc (ID INTEGER PRIMARY KEY AUTOINCREMENT, TIME INTEGER NOT NULL, CELLID INTEGER NOT NULL, MCC INTEGER NOT NULL, MNC INTEGER NOT NULL, LAC INTEGER NOT NULL, RADIO TEXT NOT NULL)";
    //private static final String CREATE_BDD = "CREATE TABLE temp (ID INTEGER PRIMARY KEY AUTOINCREMENT, ALRMTIME INTEGER NOT NULL, MAC TEXT NOT NULL, TEMP REAL NOT NULL, SENT INT DEFAULT 0)";
    
    //BMX280
    //private static final String CREATE_BDD = "CREATE TABLE envdata (ID INTEGER PRIMARY KEY AUTOINCREMENT, ALRMTIME INTEGER NOT NULL, TEMP REAL NOT NULL, PRES REAL NOT NULL, HUM REAL NOT NULL)";

    //Anémo
    private static final String CREATE_BDD = "CREATE TABLE envdata (ID INTEGER PRIMARY KEY AUTOINCREMENT, ALRMTIME INTEGER NOT NULL, COUNT REAL NOT NULL)";


	private static final String TAG = "GattPlot";
	
	
    public BaseDeDonnees(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_BDD);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
    
    //Retourne le nombre de rows de la bdd
    public int fetchNbRows() {
		bdd = this.getReadableDatabase();
		String countQuery = "SELECT  * FROM envdata";	
		Cursor cursor = bdd.rawQuery(countQuery, null);
		int count = cursor.getCount();
		cursor.close();
		return count;		
	}
	
	

	
	//Retourne un array (une liste) avec epoch, valeur, epoch, valeur ... (pour AndroidPlot: XY_VALS_INTERLEAVED)
	//Que pour les valeurs dont time > minuit aujourd'hui
	public List<Integer> fetchInterleavedEpochValue() {
		List<Integer> values = new ArrayList<>();

		//Récupérer epoch d'ajd à minuit
		LocalTime midnight = LocalTime.MIDNIGHT;
		LocalDate today = LocalDate.now();
		LocalDateTime todayMidnight = LocalDateTime.of(today, midnight);
		ZoneId zoneId = ZoneId.systemDefault();
		long epoch_midnight = todayMidnight.atZone(zoneId).toEpochSecond();		
		//Log.d(TAG, "epoch midnight today = " + epoch_midnight);
	
		String GET_ALL_TIMES = "SELECT * FROM ENVDATA WHERE ALRMTIME > " + epoch_midnight;
		bdd = this.getReadableDatabase();
		  if(bdd!=null)
		  {
		     Cursor cursor = bdd.rawQuery(GET_ALL_TIMES, null);
		     cursor.moveToFirst();
		     while(!cursor.isAfterLast())
		     {
		       values.add(cursor.getInt(cursor.getColumnIndex("ALRMTIME")));
		       values.add(cursor.getInt(cursor.getColumnIndex("COUNT")));
		       cursor.moveToNext();
		     }
		     cursor.close();
		  } else {
			  values.add(1);
			  values.add(0);
			}
		  bdd.close();
		  return values;
	}
	
	
	//Retourne un array (une liste) avec la totalité de la colonne ALRTIME
	public List<Integer> fetchAllTimes() {
		List<Integer> times = new ArrayList<>();
		String GET_ALL_TIMES = "SELECT * FROM ENVDATA";

		bdd = this.getReadableDatabase();
		  if(bdd!=null)
		  {
		     Cursor cursor = bdd.rawQuery(GET_ALL_TIMES, null);
		     cursor.moveToFirst();
		     while(!cursor.isAfterLast())
		     {
		       times.add(cursor.getInt(cursor.getColumnIndex("ALRMTIME")));
		       cursor.moveToNext();
		     }
		     cursor.close();
		  }
		  bdd.close();
		  return times;
	}
	
	
	
    
    
    
    
}
