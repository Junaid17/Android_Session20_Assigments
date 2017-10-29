package com.example.jmush.android_session20_assignment_201;

import java.util.ArrayList;

import android.app.Activity;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialising button
        Button view = (Button)findViewById(R.id.viewButton);
        Button add = (Button)findViewById(R.id.createButton);
        Button modify = (Button)findViewById(R.id.updateButton);
        Button delete = (Button)findViewById(R.id.deleteButton);

        //onClick of button invoking actions
        view.setOnClickListener(new OnClickListener() {
            public void onClick(View v){
//                displayContacts();
                Intent contacts = new Intent(Intent.ACTION_VIEW,ContactsContract.Contacts.CONTENT_URI);
                // Starting the activity
                startActivity(contacts);
                Log.i("MainActivity", "Completed Displaying Contact list");
            }
        });

        add.setOnClickListener(new OnClickListener() {
            public void onClick(View v){
                createContact("Junaid", "123456789");
                Log.i("MainActivity", "Created a new contact, of course hard-coded");
            }
        });

        modify.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                updateContact("Junaid", "987654321");
                Log.i("MainActivity", "Completed updating the email id, if applicable");
            }
        });

        delete.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                deleteContact("Junaid");
                Log.i("MainActivity", "Deleted the just created contact");
            }
        });
    }

    /**
     *  Method is called to Insert contact
     * @param name
     * @param phone
     */
    private void createContact(String name, String phone) {

        //Provides access to other applications
        ContentResolver cr = getContentResolver();

        //  get internal contacts using content resolver
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);
        //cursor object to traverse through all the contacts,
        // and check wheather the contact exists else create a new one
        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {
                String existName = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                if (existName.contains(name)) {
                    Toast.makeText(MainActivity.this,"The contact name: " + name + " already exists", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        }

        // Inserting the contact ContentProvider

        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, "accountname@gmail.com")
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, "com.google")
                .build());
        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name)
                .build());
        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phone)
                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_HOME)
                .build());


        try {
            cr.applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Toast.makeText(MainActivity.this, "Created a new contact with name: " + name + " and Phone No: " + phone, Toast.LENGTH_SHORT).show();

    }

    /**
     * Method to update the existing contact
     * @param name
     * @param phone
     */

    private void updateContact(String name, String phone) {
        ContentResolver cr = getContentResolver();

        String where = ContactsContract.Data.DISPLAY_NAME + " = ? AND " +
                ContactsContract.Data.MIMETYPE + " = ? AND " +
                String.valueOf(ContactsContract.CommonDataKinds.Phone.TYPE) + " = ? ";
        String[] params = new String[] {name,
                ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
                String.valueOf(ContactsContract.CommonDataKinds.Phone.TYPE_HOME)};

        Cursor phoneCur = managedQuery(ContactsContract.Data.CONTENT_URI, null, where, params, null);

        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

        if ( (null == phoneCur)  ) {
            createContact(name, phone);
        } else {
            ops.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                    .withSelection(where, params)
                    .withValue(ContactsContract.CommonDataKinds.Phone.DATA, phone)
                    .build());
        }

        phoneCur.close();

        try {
            cr.applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Toast.makeText(MainActivity.this, "Updated the phone number of 'Junaid' to: " + phone, Toast.LENGTH_SHORT).show();
    }

    /**
     * Method to delete the contact
     * @param name
     */

    private void deleteContact(String name) {

        ContentResolver cr = getContentResolver();
        String where = ContactsContract.Data.DISPLAY_NAME + " = ? ";
        String[] params = new String[] {name};

        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        ops.add(ContentProviderOperation.newDelete(ContactsContract.RawContacts.CONTENT_URI)
                .withSelection(where, params)
                .build());
        try {
            cr.applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Toast.makeText(MainActivity.this, "Deleted the contact with name '" + name +"'", Toast.LENGTH_SHORT).show();

    }
}