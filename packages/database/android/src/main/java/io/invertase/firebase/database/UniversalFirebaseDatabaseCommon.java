package io.invertase.firebase.database;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseException;
/*
 * Copyright (c) 2016-present Invertase Limited & Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this library except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Logger;
import io.invertase.firebase.common.UniversalFirebasePreferences;
import java.util.HashMap;

public class UniversalFirebaseDatabaseCommon {

  private static HashMap<String, Boolean> configSettingsLock = new HashMap<>();
  private static HashMap<String, HashMap<String, Object>> emulatorConfigs = new HashMap<>();

  // static FirebaseDatabase getDatabaseForApp(String appName, String dbURL) {
  //   FirebaseDatabase firebaseDatabase;

  //   if (dbURL != null && dbURL.length() > 0) {
  //     if (appName != null && appName.length() > 0) {
  //       FirebaseApp firebaseApp = FirebaseApp.getInstance(appName);
  //       firebaseDatabase = FirebaseDatabase.getInstance(firebaseApp, dbURL);
  //     } else {
  //       firebaseDatabase = FirebaseDatabase.getInstance(dbURL);
  //     }
  //   } else {
  //     FirebaseApp firebaseApp = FirebaseApp.getInstance(appName);
  //     firebaseDatabase = FirebaseDatabase.getInstance(firebaseApp);
  //   }

  //   setDatabaseConfig(firebaseDatabase, appName, dbURL);

  //   HashMap emulatorConfig = getEmulatorConfig(appName, dbURL);
  //   if (emulatorConfig != null) {
  //     firebaseDatabase.useEmulator(
  //       (String) emulatorConfig.get("host"),
  //       (Integer) emulatorConfig.get("port")
  //     );
  //   }

  //   return firebaseDatabase;
  // }

  static DatabaseReference fireRef(String path, String dbURL, String appName) {
    DatabaseReference r;

    if (appName != null && appName.length() > 0) {
      FirebaseApp firebaseApp = FirebaseApp.getInstance(appName);

      if (dbURL != null && dbURL.length() > 0) {
        r = FirebaseDatabase.getInstance(firebaseApp, dbURL).getReference().child(path);
      } else {
        r = FirebaseDatabase.getInstance(firebaseApp).getReference().child(path);
      }
    } else {
      // throw "Invalid argument, call to fireRef() with no appName provided";

      if (dbURL != null && dbURL.length() > 0) {
        r = FirebaseDatabase.getInstance(dbURL).getReference().child(path);
      } else {
        r = FirebaseDatabase.getInstance().getReference().child(path);
      }
    }

    if (!path.contains(".")) r.keepSynced(true);
    return r;
  }

  static DatabaseReference fireRef(String path) {
    DatabaseReference r = FirebaseDatabase.getInstance().getReference().child(path);
    if (!path.contains(".")) r.keepSynced(true);
    return r;
  }

  static DatabaseReference fireRef(String path, String databaseURL) {
    DatabaseReference r = FirebaseDatabase.getInstance(databaseURL).getReference().child(path);
    if (!path.contains(".")) r.keepSynced(true);
    return r;
  }

  static FirebaseDatabase fireDb(String appName, String dbURL) {
    FirebaseDatabase r;

    if (appName != null && appName.length() > 0) {
      FirebaseApp firebaseApp = FirebaseApp.getInstance(appName);

      if (dbURL != null && dbURL.length() > 0) {
        r = FirebaseDatabase.getInstance(firebaseApp, dbURL);
      } else {
        r = FirebaseDatabase.getInstance(firebaseApp);
      }
    } else {
      if (dbURL != null && dbURL.length() > 0) {
        r = FirebaseDatabase.getInstance(dbURL);
      } else {
        r = FirebaseDatabase.getInstance();
      }
    }

    return r;
  }

  static FirebaseDatabase fireDb(String dbURL) {
    FirebaseDatabase r;

    if (dbURL != null && dbURL.length() > 0) {
      r = FirebaseDatabase.getInstance(dbURL);
    } else {
      r = FirebaseDatabase.getInstance();
    }
    return r;
  }

  static void turnOnFireCache(String dbURL) {
    if (dbURL != null && dbURL.length() > 0) {
      FirebaseDatabase.getInstance(dbURL).setPersistenceEnabled(true);
    } else {
      FirebaseDatabase.getInstance().setPersistenceEnabled(true);
      FirebaseDatabase.getInstance().setLogLevel(Logger.Level.WARN);
    }
    // FirebaseDatabase.getInstance(ref).setPersistenceCacheSizeBytes(100000000);
  }

  private static void setDatabaseConfig(
    FirebaseDatabase firebaseDatabase,
    String appName,
    String dbURL
  ) {
    String lockKey = appName + dbURL;
    if (configSettingsLock.containsKey(lockKey)) return;

    UniversalFirebasePreferences preferences = UniversalFirebasePreferences.getSharedInstance();

    try {
      boolean persistenceEnabled = preferences.getBooleanValue(
        UniversalDatabaseStatics.DATABASE_PERSISTENCE_ENABLED,
        false
      );
      firebaseDatabase.setPersistenceEnabled(persistenceEnabled);

      boolean loggingEnabled = preferences.getBooleanValue(
        UniversalDatabaseStatics.DATABASE_LOGGING_ENABLED,
        false
      );

      if (loggingEnabled) {
        firebaseDatabase.setLogLevel(Logger.Level.DEBUG);
      } else {
        firebaseDatabase.setLogLevel(Logger.Level.WARN);
      }

      if (preferences.contains(UniversalDatabaseStatics.DATABASE_PERSISTENCE_CACHE_SIZE)) {
        firebaseDatabase.setPersistenceCacheSizeBytes(
          preferences.getLongValue(
            UniversalDatabaseStatics.DATABASE_PERSISTENCE_CACHE_SIZE,
            10485760L
          )
        );
      }
    } catch (DatabaseException exception) {
      if (
        !exception.getMessage().contains("must be made before any other usage of FirebaseDatabase")
      ) {
        throw exception;
      }
    }

    configSettingsLock.put(lockKey, true);
  }

  static void addEmulatorConfig(String appName, String dbURL, String host, int port) {
    String configKey = appName + dbURL;
    HashMap<String, Object> emulatorConfig = new HashMap<>();
    emulatorConfig.put("host", host);
    emulatorConfig.put("port", Integer.valueOf(port));
    emulatorConfigs.put(configKey, emulatorConfig);
  }

  private static HashMap<String, Object> getEmulatorConfig(String appName, String dbURL) {
    return emulatorConfigs.get(appName + dbURL);
  }
}
