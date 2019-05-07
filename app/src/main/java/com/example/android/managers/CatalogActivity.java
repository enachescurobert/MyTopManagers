/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.managers;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.android.managers.data.ManagerContract.ManagerEntry;

/**
 * Displays list of managers that were entered and stored in the app.
 */
public class CatalogActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    /** Identifier for the manager data loader */
    private static final int MANAGER_LOADER = 0;

    /** Adapter for the ListView */
    ManagerCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
         fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        // Find the ListView which will be populated with the manager data
        ListView managerListView = (ListView) findViewById(R.id.list);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        managerListView.setEmptyView(emptyView);

        // Setup an Adapter to create a list item for each row of manager data in the Cursor.
        // There is no manager data yet (until the loader finishes) so pass in null for the Cursor.
        mCursorAdapter = new ManagerCursorAdapter(this, null);
        managerListView.setAdapter(mCursorAdapter);

        // Setup the item click listener
        managerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // Create new intent to go to {@link EditorActivity}
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);

                // Form the content URI that represents the specific manager that was clicked on,
                // by appending the "id" (passed as input to this method) onto the
                // {@link ManagerEntry#CONTENT_URI}.
                // For example, the URI would be "content://com.example.android.managers/managers/2"
                // if the manager with ID 2 was clicked on.
                Uri currentManagerUri = ContentUris.withAppendedId(ManagerEntry.CONTENT_URI, id);

                // Set the URI on the data field of the intent
                intent.setData(currentManagerUri);

                // Launch the {@link EditorActivity} to display the data for the current manager.
                startActivity(intent);
            }
        });

        // Kick off the loader
        getLoaderManager().initLoader(MANAGER_LOADER, null, this);
    }

    /**
     * Helper method to insert hardcoded manager data into the database. For debugging purposes only.
     */
    private void insertManager() {
        // Create a ContentValues object where column names are the keys,
        // and Gheorghe Hagi's manager attributes are the values.
        ContentValues values = new ContentValues();
        values.put(ManagerEntry.COLUMN_MANAGER_NAME, "Gheorghe Hagi");
        values.put(ManagerEntry.COLUMN_MANAGER_TEAM, "FC Viitorul Constanța");
        values.put(ManagerEntry.COLUMN_MANAGER_GENDER, ManagerEntry.GENDER_MALE);
        values.put(ManagerEntry.COLUMN_MANAGER_TROPHIES, 7);

        // Insert a new row for Gheorghe Hagi into the provider using the ContentResolver.
        // Use the {@link ManagerEntry#CONTENT_URI} to indicate that we want to insert
        // into the managers database table.
        // Receive the new content URI that will allow us to access Gheorghe Hagi's data in the future.
        Uri newUri = getContentResolver().insert(ManagerEntry.CONTENT_URI, values);
    }

    /**
     * Helper method to delete all managers in the database.
     */
    private void deleteAllManagers() {
        int rowsDeleted = getContentResolver().delete(ManagerEntry.CONTENT_URI, null, null);
        Log.v("CatalogActivity", rowsDeleted + " rows deleted from manager database");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertManager();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                deleteAllManagers();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Define a projection that specifies the columns from the table we care about.
        String[] projection = {
                ManagerEntry._ID,
                ManagerEntry.COLUMN_MANAGER_NAME,
                ManagerEntry.COLUMN_MANAGER_TEAM };

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                ManagerEntry.CONTENT_URI,   // Provider content URI to query
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Update {@link ManagerCursorAdapter} with this new cursor containing updated manager data
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Callback called when the data needs to be deleted
        //and the data in the most recently provided cursor is invalid
        mCursorAdapter.swapCursor(null);
    }
}
