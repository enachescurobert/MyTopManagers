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
package com.enachescurobert.android.managers.data;

import android.net.Uri;
import android.content.ContentResolver;
import android.provider.BaseColumns;

/**
 * API Contract for the Managers app.
 */
public final class ManagerContract {

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    private ManagerContract() {}

    /**
     * The "Content authority" is a name for the entire content provider, similar to the
     * relationship between a domain name and its website.  A convenient string to use for the
     * content authority is the package name for the app, which is guaranteed to be unique on the
     * device.
     */
    public static final String CONTENT_AUTHORITY = "com.enachescurobert.android.managers";

    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Possible path (appended to base content URI for possible URI's)
     * For instance, content://com.enachescurobert.android.managers/managers/ is a valid path for
     * looking at manager data. content://com.enachescurobert.android.managers/staff/ will fail,
     * as the ContentProvider hasn't been given any information on what to do with "staff".
     */
    public static final String PATH_MANAGERS = "managers";

    /**
     * Inner class that defines constant values for the managers database table.
     * Each entry in the table represents a single manager.
     */
    public static final class ManagerEntry implements BaseColumns {

        /** The content URI to access the manager data in the provider */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_MANAGERS);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of managers.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MANAGERS;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single manager.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MANAGERS;

        /** Name of database table for managers */
        public final static String TABLE_NAME = "managers";

        /**
         * Unique ID number for the manager (only for use in the database table).
         *
         * Type: INTEGER
         */
        public final static String _ID = BaseColumns._ID;

        /**
         * Name of the manager.
         *
         * Type: TEXT
         */
        public final static String COLUMN_MANAGER_NAME ="name";

        /**
         * Team of the manager.
         *
         * Type: TEXT
         */
        public final static String COLUMN_MANAGER_TEAM = "team";

        /**
         * Gender of the manager.
         *
         * The only possible values are {@link #GENDER_UNKNOWN}, {@link #GENDER_MALE},
         * or {@link #GENDER_FEMALE}.
         *
         * Type: INTEGER
         */
        public final static String COLUMN_MANAGER_GENDER = "gender";

        /**
         * Trophies of the manager.
         *
         * Type: INTEGER
         */
        public final static String COLUMN_MANAGER_TROPHIES = "trophies";

        /**
         * Possible values for the gender of the manager.
         */
        public static final int GENDER_UNKNOWN = 0;
        public static final int GENDER_MALE = 1;
        public static final int GENDER_FEMALE = 2;

        /**
         * Returns whether or not the given gender is {@link #GENDER_UNKNOWN}, {@link #GENDER_MALE},
         * or {@link #GENDER_FEMALE}.
         */
        public static boolean isValidGender(int gender) {
            if (gender == GENDER_UNKNOWN || gender == GENDER_MALE || gender == GENDER_FEMALE) {
                return true;
            }
            return false;
        }
    }

}

