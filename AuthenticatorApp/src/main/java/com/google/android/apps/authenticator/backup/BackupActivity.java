/*
 * Copyright 2014 Richard Banasiak. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.apps.authenticator.backup;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.apps.authenticator.dataexport.Exporter;
import com.google.android.apps.authenticator.dataimport.Importer;
import com.google.android.apps.authenticator.testability.DependencyInjector;
import com.google.android.apps.authenticator2.R;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class BackupActivity extends Activity implements View.OnClickListener{

    private static final String TAG = BackupActivity.class.getSimpleName();

    private static final String BACKUP_FILE = "authenticator.json";

    public static final String BACKUP_FILE_STRING = new File(Environment.getExternalStorageDirectory(), BACKUP_FILE).getAbsolutePath();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.backup);

        TextView backupText = (TextView) findViewById(R.id.backup_text);
        backupText.setText(getString(R.string.backup_text, BACKUP_FILE_STRING));

        TextView restoreText = (TextView) findViewById(R.id.restore_text);
        restoreText.setText(getString(R.string.restore_text, BACKUP_FILE_STRING));

        Button backupButton = (Button) findViewById(R.id.backup);
        backupButton.setOnClickListener(this);

        Button restoreButton = (Button) findViewById(R.id.restore);
        restoreButton.setOnClickListener(this);

        // hide backup instructions if the account database is empty
        ArrayList<String> accountNames = new ArrayList<String>();
        DependencyInjector.getAccountDb().getNames(accountNames);
        if(accountNames.size() < 1) {
            backupText.setVisibility(View.GONE);
            backupButton.setVisibility(View.GONE);
        }
    }

    @Override public void onClick(View v) {
        switch(v.getId()) {
            case R.id.backup:
                onBackupButtonPressed();
                break;
            case R.id.restore:
                onRestoreButtonPressed();
                break;
        }
    }

    private void onBackupButtonPressed() {
        Exporter exporter = new Exporter(DependencyInjector.getAccountDb(), null);

        JSONObject json = exporter.getJsonData();
        if(writeJsonToFile(json)) {
            Toast.makeText(this, R.string.backup_success, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.backup_failed, Toast.LENGTH_SHORT).show();
        }

    }

    private void onRestoreButtonPressed() {
        Importer importer = new Importer();
        JSONObject json = readJsonFromFile();
        int accountsImported = importer.importFromJson(json, DependencyInjector.getAccountDb());

        if(accountsImported == -1) {
            Toast.makeText(this, R.string.restore_failed, Toast.LENGTH_SHORT).show();
        } else {
            String text = "";
            if(accountsImported == 1) {
                text = getString(R.string.restore_success, accountsImported, ".");
            } else {
                text = getString(R.string.restore_success, accountsImported, "s.");
            }

            text = text + "\n\n" + getString(R.string.delete_restore_file, BACKUP_FILE_STRING);

            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.delete_restore_file, BACKUP_FILE))
                    .setMessage(text)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override public void onClick(DialogInterface dialog, int which) {
                            deleteRestoreFile();
                            dialog.dismiss();

                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        @Override public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    })
                    .show();
        }
    }

    private void deleteRestoreFile() {
        File file = new File(BACKUP_FILE_STRING);
        if (file.delete() ) {
            Toast.makeText(this, getString(R.string.delete_success, BACKUP_FILE), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getString(R.string.delete_failed, BACKUP_FILE), Toast.LENGTH_SHORT).show();
        }
    }

    private boolean writeJsonToFile(JSONObject json) {
        File file = new File(BACKUP_FILE_STRING);
        Log.d(TAG, "backup file = " + file.getAbsolutePath());
        try {
            FileOutputStream stream = new FileOutputStream(file, false);
            stream.write(json.toString().getBytes());
            stream.close();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Unable to write backup to external storage");
            Log.e(TAG, e.getMessage());
        }
        return false;
    }

    private JSONObject readJsonFromFile() {
        File file = new File(BACKUP_FILE_STRING);
        Log.d(TAG, "restore file = " + file.getAbsolutePath());
        int length = (int) file.length();
        byte[] bytes = new byte[length];

        JSONObject json = null;
        try {
            FileInputStream in = new FileInputStream(file);
            in.read(bytes);
            in.close();
            json = new JSONObject(new String(bytes));

        } catch (Exception e) {
            Log.e(TAG, "Unable to read backup from external storage");
            Log.e(TAG, e.getMessage());
        }

        return json;
    }


}
