/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.example.android.service;

import java.util.ArrayList;

import com.example.android.wordwidget.parser.RssFeedNullException;
import com.example.android.wordwidget.parser.RssParser.Item;
import com.example.android.wordwidget.parser.ContentHelper;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;

import android.os.Binder;
import android.os.IBinder;
import android.util.Log;


public class DownloadService extends Service {
    protected static final String TAG = "DOWNLOAD";
    private ContentHelper mContentHelper;
    private com.example.android.wordwidget.parser.RssParser.RssFeed mFeed;
    private long mLastDownload;
    private boolean mRun = true;
    private Cursor c;
    private DbAdapter mDbHelper;
    private static long SLEEP_TIME = 1000*60;

    public class DownloadBinder extends Binder {
        public DownloadService getService() {
            return DownloadService.this;
        }
    }
    
    public String getWords() {
        boolean bool = c.moveToFirst();
        StringBuffer sb = new StringBuffer();
        while( c.moveToNext()) {
            sb.append(c.getLong(0) + ":" +  c.getString(1) + ":" + c.getString(2));

        }
        return sb.toString();
    }
    
    @Override
    public void onCreate() {
        mContentHelper = new ContentHelper();
        mDbHelper = new DbAdapter(this);
        mDbHelper.open();
        long time = System.currentTimeMillis();

        c = mDbHelper.fetchAllWords();
        boolean bool = c.moveToFirst();
        long last = 0;
        while( c.moveToNext()) {
            Log.d(TAG, c.getLong(0) + ":" +  c.getString(1) + ":" + c.getString(2));
            last = c.getLong(0);
        }
        mLastDownload = last;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("LocalService", "Received start id " + startId + ": " + intent);
      
        new Thread(new Runnable(){
          @Override
          public void run() {
              Log.d(TAG,"Inside run()");
              while(mRun) {
                  try {
                      mFeed =  mContentHelper.getFeed();
                      if(mFeed != null) {
                          ArrayList<Item> items = mFeed.items;
                          long mNewDownload  = System.currentTimeMillis();
                          int size = items.size();
                          for(int i = 0; i < size; i++) {
                              String titleOrg = items.get(i).mTitle;;
                              int indexOfColon = titleOrg.indexOf(":");
                              String title = titleOrg.substring(indexOfColon + 1);

                              String description = items.get(i).mDescription;
                              Log.d(TAG,title + " : " + description);

                              mDbHelper.createWord(mNewDownload, title, description);
                          }

                          Cursor c = mDbHelper.fetchAllWords();
                          Log.d(TAG,"Cursor before deleting with " + mLastDownload + "count" + c.getCount());

                          boolean  b= mDbHelper.deleteNoteWithTime(mLastDownload);

                          Cursor c2 = mDbHelper.fetchAllWords();
                          Log.d(TAG,"Cursor after deleting :"+ c2.getCount());
                          mLastDownload = mNewDownload;
                      }
                      Thread.sleep(SLEEP_TIME);
                  } catch (InterruptedException e) {
                      Log.e(TAG, "", e);
                  } catch (RssFeedNullException e) {
                      Log.e(TAG, "", e);
                  }
              } 
          }
      }).start();
      return START_STICKY;
    }

    @Override
    public void onDestroy() {
        mRun = false;
        mDbHelper.close();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
    private final IBinder mBinder = new DownloadBinder();

}

