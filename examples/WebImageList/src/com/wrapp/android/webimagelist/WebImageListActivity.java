package com.wrapp.android.webimagelist;

import android.app.ListActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import com.wrapp.android.webimage.WebImage;

public class WebImageListActivity extends ListActivity {
  private static final long SHOW_PROGRESS_DELAY_IN_MS = 50;
  private Handler uiHandler;
  private Integer numTasks = 0;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
    setContentView(R.layout.web_image_activity);

    WebImage.clearOldCacheFiles(0);
    WebImage.enableLogging("WebImageList", Log.DEBUG);
    uiHandler = new Handler();

    WebImageListAdapter listAdapter = new WebImageListAdapter(new WebImageListAdapter.Listener() {
      Runnable stopTaskRunnable = new Runnable() {
        public void run() {
          onTaskStopped();
        }
      };

      public void onImageLoadStarted() {
        uiHandler.postDelayed(new Runnable() {
          public void run() {
            onTaskStarted();
          }
        }, SHOW_PROGRESS_DELAY_IN_MS);
      }

      public void onImageLoadComplete() {
        uiHandler.post(stopTaskRunnable);
      }

      public void onImageLoadError() {
        uiHandler.post(stopTaskRunnable);
      }

      public void onImageLoadCancelled() {
        uiHandler.post(stopTaskRunnable);
      }
    });
    setListAdapter(listAdapter);
    listAdapter.notifyDataSetChanged();
  }

  @Override
  protected void onPause() {
    super.onPause();
    WebImage.cancelAllRequests();
  }

  @Override
  public void onLowMemory() {
    super.onLowMemory();
    WebImage.clearMemoryCaches();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater menuInflater = new MenuInflater(this);
    menuInflater.inflate(R.menu.main_menu, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch(item.getItemId()) {
      case R.id.MainMenuRefreshItem:
        refresh();
        break;
      case R.id.MainMenuClearCachesItem:
        WebImage.cancelAllRequests();
        WebImage.clearMemoryCaches();
        WebImage.clearOldCacheFiles(0);
        refresh();
        break;
      default:
        refresh();
        break;
    }

    return true;
  }

  private void refresh() {
    final WebImageListAdapter listAdapter = (WebImageListAdapter)getListAdapter();
    listAdapter.notifyDataSetChanged();
  }

  private void onTaskStarted() {
    synchronized(numTasks) {
      if(numTasks == 0) {
        setProgressBarIndeterminateVisibility(true);
      }
      numTasks++;
    }
  }

  private void onTaskStopped() {
    synchronized(numTasks) {
      numTasks--;
      if(numTasks == 0) {
        setProgressBarIndeterminateVisibility(false);
      }
    }
  }
}
