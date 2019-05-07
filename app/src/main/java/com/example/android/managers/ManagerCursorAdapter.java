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

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.managers.data.ManagerContract.ManagerEntry;

/**
 * {@link ManagerCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of manager data as its data source. This adapter knows
 * how to create list items for each row of manager data in the {@link Cursor}.
 */
public class ManagerCursorAdapter extends CursorAdapter {

    /**
     * Constructs a new {@link ManagerCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public ManagerCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the manager data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current manager can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Find individual views that we want to modify in the list item layout
        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView summaryTextView = (TextView) view.findViewById(R.id.summary);

        // Find the columns of manager attributes that we're interested in
        int nameColumnIndex = cursor.getColumnIndex(ManagerEntry.COLUMN_MANAGER_NAME);
        int teamColumnIndex = cursor.getColumnIndex(ManagerEntry.COLUMN_MANAGER_TEAM);

        // Read the manager attributes from the Cursor for the current manager
        String managerName = cursor.getString(nameColumnIndex);
        String managerTeam = cursor.getString(teamColumnIndex);

        // If the manager team is empty string or null, then use some default text
        // that says "Unknown team", so the TextView isn't blank.
        if (TextUtils.isEmpty(managerTeam)) {
            managerTeam = context.getString(R.string.unknown_team);
        }

        // Update the TextViews with the attributes for the current manager
        nameTextView.setText(managerName);
        summaryTextView.setText(managerTeam);
    }
}
