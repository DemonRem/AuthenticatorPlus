/*
 * Copyright 2011 Google Inc. All Rights Reserved.
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

package com.google.android.apps.authenticator.dataimport;

import com.google.android.apps.authenticator.AccountDb;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Imports the contents of {@link AccountDb} and the key material and settings from a
 * {@link Bundle}.
 *
 * @author klyubin@google.com (Alex Klyubin)
 */
public class Importer {

  private static final String LOG_TAG = Importer.class.getSimpleName();

  // @VisibleForTesting
  public static final String KEY_ACCOUNTS = "accountDb";

  // @VisibleForTesting
  public static final String KEY_PREFERENCES = "preferences";

  // @VisibleForTesting
  public static final String KEY_NAME = "name";

  // @VisibleForTesting
  public static final String KEY_ENCODED_SECRET = "encodedSecret";

  // @VisibleForTesting
  public static final String KEY_TYPE = "type";

  // @VisibleForTesting
  public static final String KEY_COUNTER = "counter";

  /**
   * Imports the contents of the provided {@link Bundle} into the provided {@link AccountDb} and
   * {@link SharedPreferences}. Does not overwrite existing records in the database.
   *
   * @param bundle source bundle.
   * @param accountDb destination {@link AccountDb}.
   * @param preferences destination preferences or {@code null} for none.
   */
  public void importFromBundle(Bundle bundle, AccountDb accountDb, SharedPreferences preferences) {
    Bundle accountDbBundle = bundle.getBundle(KEY_ACCOUNTS);
    if (accountDbBundle != null) {
      importAccountDbFromBundle(accountDbBundle, accountDb);
    }

    if (preferences != null) {
      Bundle preferencesBundle = bundle.getBundle(KEY_PREFERENCES);
      if (preferencesBundle != null) {
        importPreferencesFromBundle(preferencesBundle, preferences);
      }
    }
  }

  public int importFromJson(JSONObject json, AccountDb accountDb) {
      JSONObject accountDbJson = null;
      try {
          accountDbJson = json.getJSONObject(KEY_ACCOUNTS);
          if (accountDbJson != null) {
              return importAccountDbFromJson(accountDbJson, accountDb);
          }
      } catch (JSONException e) {
          throw new RuntimeException("Unable to import database");
      }
      return -1;
  }

  private void importAccountDbFromBundle(Bundle bundle, AccountDb accountDb) {
    // Each account is stored in a Bundle whose key is a string representing the ordinal (integer)
    // position of the account in the database.
    List<String> sortedAccountBundleKeys = new ArrayList<String>(bundle.keySet());
    Collections.sort(sortedAccountBundleKeys, new IntegerStringComparator());
    int importedAccountCount = 0;
    for (String accountBundleKey : sortedAccountBundleKeys) {
      Bundle accountBundle = bundle.getBundle(accountBundleKey);
      String name = accountBundle.getString(KEY_NAME);
      if (name == null) {
        Log.w(LOG_TAG, "Skipping account #" + accountBundleKey + ": name missing");
        continue;
      }
      if (accountDb.nameExists(name)) {
        // Don't log account name here and below because it's considered PII
        Log.w(LOG_TAG, "Skipping account #" + accountBundleKey + ": already configured");
        continue;
      }
      String encodedSecret = accountBundle.getString(KEY_ENCODED_SECRET);
      if (encodedSecret == null) {
        Log.w(LOG_TAG, "Skipping account #" + accountBundleKey + ": secret missing");
        continue;
      }
      String typeString = accountBundle.getString(KEY_TYPE);
      AccountDb.OtpType type;
      if ("totp".equals(typeString)) {
        type = AccountDb.OtpType.TOTP;
      } else if ("hotp".equals(typeString)) {
        type = AccountDb.OtpType.HOTP;
      } else {
        Log.w(LOG_TAG, "Skipping account #" + accountBundleKey
            + ": unsupported type: \"" + typeString + "\"");
        continue;
      }

      Integer counter =
          accountBundle.containsKey(KEY_COUNTER) ? accountBundle.getInt(KEY_COUNTER) : null;
      if (counter == null) {
        if (type == AccountDb.OtpType.HOTP) {
          Log.w(LOG_TAG, "Skipping account #" + accountBundleKey + ": counter missing");
          continue;
        } else {
          // TOTP
          counter = AccountDb.DEFAULT_HOTP_COUNTER;
        }
      }

      accountDb.update(name, encodedSecret, name, type, counter);
      importedAccountCount++;
    }

    Log.i(LOG_TAG, "Imported " + importedAccountCount + " accounts");
  }

  private int importAccountDbFromJson(JSONObject json, AccountDb accountDb) {
      Iterator<String> it = json.keys();
      int importedAccountCount = 0;
      try {
          while (it.hasNext()) {
              String key = it.next();
              JSONObject accountJson = json.getJSONObject(key);
              String name = null;
              String secret = null;
              Integer counter = null;
              String typeString = null;
              AccountDb.OtpType type = null;

              try {
                  name = accountJson.getString(KEY_NAME);
                  if(accountDb.nameExists(name)) {
                      Log.w(LOG_TAG, "Skipping account #" + key + ": already configured");
                      continue;
                  }
              } catch (JSONException e1) {
                  Log.w(LOG_TAG, "Skipping account #" + key + ": name missing");
                  continue;
              }

              try {
                  secret = accountJson.getString(KEY_ENCODED_SECRET);
              } catch (JSONException e2) {
                  Log.w(LOG_TAG, "Skipping account #" + key + ": secret missing");
                  continue;
              }

              try {
                  typeString = accountJson.getString(KEY_TYPE);
                  type = AccountDb.OtpType.valueOf(typeString);
              } catch (JSONException e3) {
                  Log.w(LOG_TAG, "Skipping account #" + key + ": type missing");
                  continue;
              }

              try {
                  counter = accountJson.getInt(KEY_COUNTER);
              } catch (JSONException e4) {
                  if (type == AccountDb.OtpType.HOTP) {
                      Log.w(LOG_TAG, "Skipping account #" + key + ": counter missing");
                      continue;
                  } else {
                      counter = AccountDb.DEFAULT_HOTP_COUNTER;
                  }
              }

              if (name != null && secret != null && counter != null && type != null) {
                  accountDb.update(name, secret, name, type, counter);
                  importedAccountCount++;
              }
          }
      } catch(Exception e) {
          Log.e(LOG_TAG, "Unable to deserialize JSON string");
          Log.e(LOG_TAG, e.getMessage());
          return -1;
      }

      Log.i(LOG_TAG, "Imported " + importedAccountCount + " accounts");
      return importedAccountCount;
  }

  private static class IntegerStringComparator implements Comparator<String> {
    @Override
    public int compare(String lhs, String rhs) {
      int lhsValue = Integer.parseInt(lhs);
      int rhsValue = Integer.parseInt(rhs);
      return lhsValue - rhsValue;
    }
  }

  private boolean tryImportPreferencesFromBundle(Bundle bundle, SharedPreferences preferences) {
    SharedPreferences.Editor preferencesEditor = preferences.edit();
    for (String key : bundle.keySet()) {
      Object value = bundle.get(key);
      if (value instanceof Boolean) {
        preferencesEditor.putBoolean(key, (Boolean) value);
      } else if (value instanceof Float) {
        preferencesEditor.putFloat(key, (Float) value);
      } else if (value instanceof Integer) {
        preferencesEditor.putInt(key, (Integer) value);
      } else if (value instanceof Long) {
        preferencesEditor.putLong(key, (Long) value);
      } else if (value instanceof String) {
        preferencesEditor.putString(key, (String) value);
      } else {
        // Ignore: can only be Set<String> at the moment (API Level 11+), which we don't use anyway.
      }
    }
    return preferencesEditor.commit();
  }

  private void importPreferencesFromBundle(Bundle bundle, SharedPreferences preferences) {
    // Retry until the operation succeeds
    while (!tryImportPreferencesFromBundle(bundle, preferences)) {}
  }
}
