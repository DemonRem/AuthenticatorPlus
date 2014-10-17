package com.google.android.apps.authenticator.backup;

import android.app.Activity;
import android.os.Bundle;
import com.google.android.apps.authenticator2_plus.R;

public class BackupActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.backup);
    }
}
