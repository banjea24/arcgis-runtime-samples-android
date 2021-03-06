/*
 * Copyright 2019 Esri
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.esri.arcgisruntime.samples.createterrainfromalocalraster;

import java.util.ArrayList;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISScene;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.RasterElevationSource;
import com.esri.arcgisruntime.mapping.view.Camera;
import com.esri.arcgisruntime.mapping.view.SceneView;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getSimpleName();

  private static final int PERMISSIONS_REQUEST_CODE = 1;

  private static final String[] PERMISSIONS = { Manifest.permission.READ_EXTERNAL_STORAGE };

  private SceneView mSceneView;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mSceneView = findViewById(R.id.sceneView);

    // create a scene and add a basemap to it
    ArcGISScene scene = new ArcGISScene();
    scene.setBasemap(Basemap.createImagery());

    // add the scene to the sceneview
    mSceneView.setScene(scene);

    // specify the initial camera position
    Camera camera = new Camera(36.525, -121.80, 300.0, 180, 80.0, 0.0);
    mSceneView.setViewpointCamera(camera);

    requestReadPermission();
  }

  private void createRasterElevationSource() {
    // raster package file paths
    ArrayList<String> filePaths = new ArrayList<>();
    filePaths.add(Environment.getExternalStorageDirectory() + getString(R.string.raster_package_location));

    try {
      // add an elevation source to the scene by passing the URI of the raster package to the constructor
      RasterElevationSource rasterElevationSource = new RasterElevationSource(filePaths);

      // add a listener to perform operations when the load status of the elevation source changes
      rasterElevationSource.addLoadStatusChangedListener(loadStatusChangedEvent -> {
        // when elevation source loads
        if (loadStatusChangedEvent.getNewLoadStatus() == LoadStatus.LOADED) {
          // add the elevation source to the elevation sources of the scene
          mSceneView.getScene().getBaseSurface().getElevationSources().add(rasterElevationSource);
        } else if (loadStatusChangedEvent.getNewLoadStatus() == LoadStatus.FAILED_TO_LOAD) {
          // notify user that the elevation source has failed to load
          logErrorToUser(getString(R.string.error_raster_elevation_source_load_failure_message,
              rasterElevationSource.getLoadError()));
        }
      });

      // load the elevation source asynchronously
      rasterElevationSource.loadAsync();
    } catch (IllegalArgumentException e) {
      // catch exception thrown by RasterElevationSource when a file is invalid/not found
      logErrorToUser(getString(R.string.error_raster_elevation_source_load_failure_message, e.getMessage()));
    }
  }

  /**
   * Request read external storage for API level 23+.
   */
  private void requestReadPermission() {
    if (ContextCompat.checkSelfPermission(this, PERMISSIONS[0]) == PackageManager.PERMISSION_GRANTED) {
      createRasterElevationSource();
    } else {
      // request permission
      ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSIONS_REQUEST_CODE);
    }
  }

  /**
   * Handle the permissions request response
   */
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
      createRasterElevationSource();
    } else {
      // report to user that permission was denied
      logErrorToUser(getString(R.string.error_read_permission_denied_message));
    }
  }

  private void logErrorToUser(String message) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    Log.e(TAG, message);
  }
}
