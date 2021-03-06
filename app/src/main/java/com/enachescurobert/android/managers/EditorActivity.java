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
package com.enachescurobert.android.managers;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.enachescurobert.android.managers.data.ManagerContract.ManagerEntry;

import static android.widget.Toast.LENGTH_LONG;

/**
 * Allows user to create a new manager or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    /** Identifier for the manager data loader */
    private static final int EXISTING_MANAGER_LOADER = 0;

    /** Content URI for the existing manager (null if it's a new manager) */
    private Uri mCurrentManagerUri;

    /** EditText field to enter the manager's name */
    private EditText mNameEditText;

    /** EditText field to enter the manager's team */
    private EditText mTeamEditText;

    /** EditText field to enter the manager's trophies */
    private EditText mTrophiesEditText;

    /** EditText field to enter the manager's gender */
    private Spinner mGenderSpinner;

    /**
     * Gender of the manager. The possible valid values are in the ManagerContract.java file:
     * {@link ManagerEntry#GENDER_UNKNOWN}, {@link ManagerEntry#GENDER_MALE}, or
     * {@link ManagerEntry#GENDER_FEMALE}.
     */
    private int mGender = ManagerEntry.GENDER_UNKNOWN;

    /** Boolean flag that keeps track of whether the manager has been edited (true) or not (false) */
    private boolean mManagerHasChanged = false;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mManagerHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mManagerHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new manager or editing an existing one.
        Intent intent = getIntent();
        mCurrentManagerUri = intent.getData();

        // If the intent DOES NOT contain a manager content URI, then we know that we are
        // creating a new manager.
        if (mCurrentManagerUri == null) {
            // This is a new manager, so change the app bar to say "Add a Manager"
            setTitle(getString(R.string.editor_activity_title_new_manager));

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a manager that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            // Otherwise this is an existing manager, so change app bar to say "Edit Manager"
            setTitle(getString(R.string.editor_activity_title_edit_manager));

            // Initialize a loader to read the manager data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_MANAGER_LOADER, null, this);
        }

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_manager_name);
        mTeamEditText = (EditText) findViewById(R.id.edit_manager_team);
        mTrophiesEditText = (EditText) findViewById(R.id.edit_manager_trophies);
        mGenderSpinner = (Spinner) findViewById(R.id.spinner_gender);

        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        mNameEditText.setOnTouchListener(mTouchListener);
        mTeamEditText.setOnTouchListener(mTouchListener);
        mTrophiesEditText.setOnTouchListener(mTouchListener);
        mGenderSpinner.setOnTouchListener(mTouchListener);

        setupSpinner();
    }

    /**
     * Setup the dropdown spinner that allows the user to select the gender of the manager.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter genderSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_gender_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mGenderSpinner.setAdapter(genderSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mGenderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.gender_male))) {
                        mGender = ManagerEntry.GENDER_MALE;
                    } else if (selection.equals(getString(R.string.gender_female))) {
                        mGender = ManagerEntry.GENDER_FEMALE;
                    } else {
                        mGender = ManagerEntry.GENDER_UNKNOWN;
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mGender = ManagerEntry.GENDER_UNKNOWN;
            }
        });
    }

    /**
     * Get user input from editor and save manager into database.
     */
    private void saveManager() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String nameString = mNameEditText.getText().toString().trim();
        String teamString = mTeamEditText.getText().toString().trim();
        String trophiesString = mTrophiesEditText.getText().toString().trim();

        // Check if this is supposed to be a new manager
        // and check if all the fields in the editor are blank
        if (mCurrentManagerUri == null &&
                TextUtils.isEmpty(nameString) && TextUtils.isEmpty(teamString) &&
                TextUtils.isEmpty(trophiesString) && mGender == ManagerEntry.GENDER_UNKNOWN) {
            // Since no fields were modified, we can return early without creating a new manager.
            // No need to create ContentValues and no need to do any ContentProvider operations.
            return;
        }

        // Create a ContentValues object where column names are the keys,
        // and manager attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(ManagerEntry.COLUMN_MANAGER_NAME, nameString);
        values.put(ManagerEntry.COLUMN_MANAGER_TEAM, teamString);
        values.put(ManagerEntry.COLUMN_MANAGER_GENDER, mGender);
        // If the trophies is not provided by the user, don't try to parse the string into an
        // integer value. Use 0 by default.
        int trophies = 0;
        if (!TextUtils.isEmpty(trophiesString)) {
            trophies = Integer.parseInt(trophiesString);
        }
        values.put(ManagerEntry.COLUMN_MANAGER_TROPHIES, trophies);

        // Determine if this is a new or existing manager by checking if mCurrentManagerUri is null or not
        if (mCurrentManagerUri == null) {
            // This is a NEW manager, so insert a new manager into the provider,
            // returning the content URI for the new manager.
            Uri newUri = getContentResolver().insert(ManagerEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_manager_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_manager_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // Otherwise this is an EXISTING manager, so update the manager with content URI: mCurrentManagerUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentManagerUri will already identify the correct row in the database that
            // we want to modify.
            int rowsAffected = getContentResolver().update(mCurrentManagerUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_manager_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_manager_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new manager, hide the "Delete" menu item.
        if (mCurrentManagerUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        EditText managerName = (EditText)findViewById(R.id.edit_manager_name);
        String name  =  managerName.getText().toString();
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save manager to database
                if(TextUtils.isEmpty(name)){
                    Toast.makeText(getApplicationContext(), "You need to add the name of the manager", LENGTH_LONG).show();
                } else {
                    saveManager();
                    // Exit activity
                    finish();
                }
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the manager hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mManagerHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the manager hasn't changed, continue with handling back button press
        if (!mManagerHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the editor shows all manager attributes, define a projection that contains
        // all columns from the manager table
        String[] projection = {
                ManagerEntry._ID,
                ManagerEntry.COLUMN_MANAGER_NAME,
                ManagerEntry.COLUMN_MANAGER_TEAM,
                ManagerEntry.COLUMN_MANAGER_GENDER,
                ManagerEntry.COLUMN_MANAGER_TROPHIES };

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentManagerUri,         // Query the content URI for the current manager
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of manager attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(ManagerEntry.COLUMN_MANAGER_NAME);
            int teamColumnIndex = cursor.getColumnIndex(ManagerEntry.COLUMN_MANAGER_TEAM);
            int genderColumnIndex = cursor.getColumnIndex(ManagerEntry.COLUMN_MANAGER_GENDER);
            int trophiesColumnIndex = cursor.getColumnIndex(ManagerEntry.COLUMN_MANAGER_TROPHIES);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            String team = cursor.getString(teamColumnIndex);
            int gender = cursor.getInt(genderColumnIndex);
            int trophies = cursor.getInt(trophiesColumnIndex);

            // Update the views on the screen with the values from the database
            mNameEditText.setText(name);
            mTeamEditText.setText(team);
            mTrophiesEditText.setText(Integer.toString(trophies));

            // Gender is a dropdown spinner, so map the constant value from the database
            // into one of the dropdown options (0 is Unknown, 1 is Male, 2 is Female).
            // Then call setSelection() so that option is displayed on screen as the current selection.
            switch (gender) {
                case ManagerEntry.GENDER_MALE:
                    mGenderSpinner.setSelection(1);
                    break;
                case ManagerEntry.GENDER_FEMALE:
                    mGenderSpinner.setSelection(2);
                    break;
                default:
                    mGenderSpinner.setSelection(0);
                    break;
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mNameEditText.setText("");
        mTeamEditText.setText("");
        mTrophiesEditText.setText("");
        mGenderSpinner.setSelection(0); // Select "Unknown" gender
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the manager.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Prompt the user to confirm that they want to delete this manager.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the manager.
                deleteManager();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the manager.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the manager in the database.
     */
    private void deleteManager() {
        // Only perform the delete if this is an existing manager.
        if (mCurrentManagerUri != null) {
            // Call the ContentResolver to delete the manager at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentManagerUri
            // content URI already identifies the manager that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentManagerUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_manager_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_manager_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }
}