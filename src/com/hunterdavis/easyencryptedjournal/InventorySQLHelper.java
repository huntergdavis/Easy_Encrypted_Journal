package com.hunterdavis.easyencryptedjournal;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;


public class InventorySQLHelper extends android.database.sqlite.SQLiteOpenHelper {
	private static final String DATABASE_NAME = "easyencryptedjournal.db";
	private static final int DATABASE_VERSION = 1;

	// Table name
	public static final String TABLE = "easyencryptedjournal";

	// Columns
	public static final String DATE = "datename";
	public static final String ENTRY = "entry";


	public InventorySQLHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String sql = "create table " + TABLE + "( " + BaseColumns._ID
				+ " integer primary key autoincrement,"+ DATE +" text not null, "+ ENTRY +  " text not null);";
		db.execSQL(sql);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (oldVersion >= newVersion)
			return;

		String sql = null;
		if (oldVersion == 1)
			sql = "alter table " + TABLE + " add note text;";
		if (oldVersion == 2)
			sql = "";

		if (sql != null)
			db.execSQL(sql);
	}

}
