package com.hunterdavis.easyencryptedjournal;

import java.sql.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.ads.AdRequest;
import com.google.ads.AdView;

public class EasyEncryptedJournal extends Activity {

	public static final int SALT_LENGTH = 20;
	public static final int PBE_ITERATION_COUNT = 1000;

	private static final String RANDOM_ALGORITHM = "SHA1PRNG";
	private static final String PBE_ALGORITHM = "PBEWithSHA256And256BitAES-CBC-BC";
	private static final String CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";

	public String currentItem = "";
	public String currentEntry = "";
	public String password = "";
	InventorySQLHelper journalData = new InventorySQLHelper(this);
	ArrayAdapter<String> m_adapterForSpinner;
	int selectmutex = 0;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		// Look up the AdView as a resource and load a request.
		AdView adView = (AdView) this.findViewById(R.id.adView);
		adView.loadAd(new AdRequest());

		OnClickListener DeleteButtonListner = new OnClickListener() {
			public void onClick(View v) {
				yesnoDeleteHandler("Are you sure?",
						"Are you sure you want to delete?");
			}
		};

		Button deleteButton = (Button) findViewById(R.id.deleteButton);
		deleteButton.setOnClickListener(DeleteButtonListner);

		OnClickListener SaveButtonListner = new OnClickListener() {
			public void onClick(View v) {
				saveEntry();
			}
		};

		Button saveButton = (Button) findViewById(R.id.saveButton);
		saveButton.setOnClickListener(SaveButtonListner);

		// Create an anonymous implementation of OnClickListener
		OnClickListener newButtonListner = new OnClickListener() {
			public void onClick(View v) {
				// do something when the button is clicked

				// in onCreate or any event where your want the user to

				AlertDialog.Builder alert = new AlertDialog.Builder(
						v.getContext());

				alert.setTitle("Password");
				alert.setMessage("Please Enter A Password for this Journal Entry");

				// Set an EditText view to get user input
				final EditText input = new EditText(v.getContext());
				alert.setView(input);

				alert.setPositiveButton("Ok",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								String tempName = input.getText().toString();

								if (tempName.length() > 0) {
									password = tempName;
									createNewJournalEntry();
								} else {
									Toast.makeText(getBaseContext(),
											"Invalid Password!",
											Toast.LENGTH_LONG).show();
								}
							}

						});

				alert.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								// Canceled.
							}
						});

				alert.show();

			}
		};

		Button newButton = (Button) findViewById(R.id.newButton);
		newButton.setOnClickListener(newButtonListner);

		// set an adapter for our spinner
		m_adapterForSpinner = new ArrayAdapter<String>(getBaseContext(),
				android.R.layout.simple_spinner_item);
		m_adapterForSpinner
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		Spinner spinner = (Spinner) findViewById(R.id.oldentries);
		spinner.setAdapter(m_adapterForSpinner);

		spinner.setOnItemSelectedListener(new MyUnitsOnItemSelectedListener());

		// fill up our spinner item
		Cursor cursor = getEntriesCursor();
		m_adapterForSpinner.add("New Entry");
		if (cursor.getCount() > 0) {
			while (cursor.moveToNext()) {
				String singlecardName = cursor.getString(1);
				m_adapterForSpinner.add(singlecardName);
			}
		} else {
			spinner.setEnabled(false);
		}

	} // end of oncreate

	// set up the listener class for spinner
	class MyUnitsOnItemSelectedListener implements OnItemSelectedListener {
		public void onItemSelected(AdapterView<?> parent, View view, int pos,
				long id) {
			// Resources res = getResources();
			// updateSqlValues(rowId, "units", unitsarray[pos]);
			// set both global uri settings from the selected item using a sql
			// cursor

			if(selectmutex == 0)
			{
				selectmutex = 1;
				return;
			}
			Spinner spinner = (Spinner) findViewById(R.id.oldentries);
			String spinnerText = spinner.getSelectedItem().toString();
			
			if(spinnerText.equalsIgnoreCase("New Entry")) {
				AlertDialog.Builder alert = new AlertDialog.Builder(
						view.getContext());

				alert.setTitle("Password");
				alert.setMessage("Please Enter A Password for this Journal Entry");

				// Set an EditText view to get user input
				final EditText input = new EditText(view.getContext());
				alert.setView(input);

				alert.setPositiveButton("Ok",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								String tempName = input.getText().toString();

								if (tempName.length() > 0) {
									password = tempName;
									createNewJournalEntry();
								} else {
									Toast.makeText(getBaseContext(),
											"Invalid Password!",
											Toast.LENGTH_LONG).show();
								}
							}

						});

				alert.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								// Canceled.
							}
						});

				alert.show();
				return;
			}
			
			currentItem = spinnerText;
			
			
			
			queryPassAndSetText(view.getContext(), spinnerText);

			Button deleteButton = (Button) findViewById(R.id.deleteButton);
			deleteButton.setEnabled(true);

			Button saveButton = (Button) findViewById(R.id.saveButton);
			saveButton.setEnabled(true);

		}

		public void onNothingSelected(AdapterView<?> parent) {
			// Do nothing.
		}
	}

	public void queryPassAndSetText(Context context, String entryDate) {
		// get cursor from date
		Cursor cursor = getEntriesCursorByName(entryDate);
		// get string from cursor
		if (cursor.moveToFirst()) {
			currentEntry = cursor.getString(2);
		} else {
			Toast.makeText(context, "No Entry Of That Name", Toast.LENGTH_SHORT)
					.show();
			return;
		}

		AlertDialog.Builder alert = new AlertDialog.Builder(context);

		alert.setTitle("Password?");
		alert.setMessage("Please Enter The Password For This Entry");

		// Set an EditText view to get user input
		final EditText input = new EditText(context);
		alert.setView(input);

		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				String temppass = input.getText().toString();
				String crypto = "";

				if (temppass.length() > 1) {
					password = temppass;

					// use aes to decrypt the entry
					try {
						crypto = SimpleCrypto.decrypt(password, currentEntry);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						Toast.makeText(
								getBaseContext(),
								"Failed to Decrypt, probably an invalid password",
								Toast.LENGTH_LONG).show();
						return;
					}
					// put the decrypted entry into the journal

					// enable the journal
					EditText je = (EditText) findViewById(R.id.hiddentext);
					je.setEnabled(true);
					je.setText(crypto);

					Button saveButton = (Button) findViewById(R.id.saveButton);
					saveButton.setEnabled(true);

				} else {
					Toast.makeText(getBaseContext(), "Invalid Password!",
							Toast.LENGTH_LONG).show();
				}
			}
		}

		);

		alert.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Canceled.
					}
				});

		alert.show();

	}

	public void createNewJournalEntry() {
		
		Spinner myspin = (Spinner) findViewById(R.id.oldentries);
		int position = 0;
		selectmutex = 0;
		myspin.setSelection(position,false);
		
		
		// get the current date and second as string
		Date d = new Date(System.currentTimeMillis());
		String currentDate = (String) DateFormat.format("yyyy-MM-dd-hh:mm:ss", d.getTime());
		
		currentItem = currentDate;

		Button saveButton = (Button) findViewById(R.id.saveButton);
		saveButton.setEnabled(true);
		// enable the journal
		EditText je = (EditText) findViewById(R.id.hiddentext);
		je.setText("Journal Entry");
		je.setEnabled(true);

	}

	public void saveEntry() {
		// get journal text
		EditText je = (EditText) findViewById(R.id.hiddentext);
		String journalText = je.getText().toString();

		// get crypto version using password as cyper
		String crypto;
		try {
			crypto = SimpleCrypto.encrypt(password, journalText);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Toast.makeText(getBaseContext(), "Invalid Crypto Cache!",
					Toast.LENGTH_LONG).show();
			return;
		}

		// save to database

		SQLiteDatabase db = journalData.getWritableDatabase();
		ContentValues args = new ContentValues();
		args.put(InventorySQLHelper.ENTRY, crypto);
		String strFilter = " datename='" + currentItem + "'";
		int retval = db.update(InventorySQLHelper.TABLE, args, strFilter, null);
		if (retval == 0) {
			args.put(InventorySQLHelper.DATE, currentItem);
			long retret = db.insert(InventorySQLHelper.TABLE, null, args);
			m_adapterForSpinner.add(currentItem);

		}

		Spinner myspin = (Spinner) findViewById(R.id.oldentries);
		int position = 0;
		for(int i = 0;i<myspin.getCount();i++) {
			String tempstr = (String) myspin.getItemAtPosition(i);
			if(tempstr.equals(currentItem)) {
				position = i;
				break;
			}
		}
		selectmutex = 0;
		myspin.setSelection(position,false);
		myspin.setEnabled(true);
		Button deleteButton = (Button) findViewById(R.id.deleteButton);
		deleteButton.setEnabled(true);
	
	}

	private Cursor getEntriesCursor() {
		SQLiteDatabase db = journalData.getReadableDatabase();
		Cursor cursor = db.query(InventorySQLHelper.TABLE, null, null, null,
				null, null, null);
		startManagingCursor(cursor);
		return cursor;
	}

	private Cursor getEntriesCursorByName(String rowId) {
		SQLiteDatabase db = journalData.getReadableDatabase();
		Cursor cursor = db.query(InventorySQLHelper.TABLE, null, "datename = '"
				+ rowId + "'", null, null, null, null);
		startManagingCursor(cursor);
		return cursor;
	}

	public void DeleteEntryByName(String card) {
		SQLiteDatabase db = journalData.getWritableDatabase();
		db.delete(InventorySQLHelper.TABLE, "datename = '" + card + "'", null);
		db.close();

		// clear text view
		m_adapterForSpinner.remove(card);

		Spinner spinner = (Spinner) findViewById(R.id.oldentries);
		if (spinner.getCount() == 1) {
			Button deleteButton = (Button) findViewById(R.id.deleteButton);
			deleteButton.setEnabled(false);
			Button saveButton = (Button) findViewById(R.id.saveButton);
			saveButton.setEnabled(false);
			EditText je = (EditText) findViewById(R.id.hiddentext);
			je.setText("Journal Entry");
			je.setEnabled(false);
			selectmutex = 0;
		}
		else
		{
			selectmutex = 0;
			spinner.setSelection(0);
			EditText je = (EditText) findViewById(R.id.hiddentext);
			je.setText("Journal Entry");
		}

	}

	protected void yesnoDeleteHandler(String title, String mymessage) {
		new AlertDialog.Builder(this)
				.setMessage(mymessage)
				.setTitle(title)
				.setCancelable(true)
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								DeleteEntryByName(currentItem);

							}
						})
				.setNegativeButton(android.R.string.no,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
							}
						}).show();
	}

}