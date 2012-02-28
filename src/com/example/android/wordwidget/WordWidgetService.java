/*
 * Copyright (C) 2012 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.example.android.wordwidget;

import java.util.ArrayList;
import java.util.List;

import com.example.android.service.DbAdapter;
import com.example.android.service.DownloadService.DownloadBinder;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import android.widget.RemoteViewsService.RemoteViewsFactory;

public class WordWidgetService extends RemoteViewsService {
	@Override
	public RemoteViewsFactory onGetViewFactory(Intent intent) {
		 return new WordRemoteViewsFactory(this.getApplicationContext(), intent);
	}

}

class WordRemoteViewsFactory implements RemoteViewsFactory {
	private static final int mCount = 10;
	private static final String TAG = null;
	private List<WidgetItem> mWidgetItems = new ArrayList<WidgetItem>();
	private Context mContext;
	private int mAppWidgetId;

	DbAdapter mDbHelper;
    private com.example.android.service.DownloadService mBoundService;
    private DownloadBinder service;
	
	public WordRemoteViewsFactory(Context context, Intent intent) {
        mContext = context;
        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
       
    }
	@Override
	public int getCount() {
		return mCount;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public RemoteViews getLoadingView() {
		return null;
	}

	@Override
	public RemoteViews getViewAt(int position) {
		Log.d(TAG,"getViewAt");

        Cursor c = mDbHelper.fetchAllWords();
        
        boolean bool = c.moveToFirst();
        Log.d(TAG,"Count :" +  c.getCount());
        //long last = 0;
        int i = 0;
        String title ="NA";
        String desc = "NA";

        while( c.moveToNext()) {
            title  = c.getString(2);
            desc = c.getString(3);
            Log.d(TAG, title + ":" + desc);
            if(i == position) {
                
                Log.d(TAG, "i:" +  i);
                break;
            }
            ++i;
        }
		
		RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.widget_item);
		Log.d(TAG, "Text at position:" +  title);
		views.setTextViewText(R.id.title, title);
		views.setTextViewText(R.id.desc, desc);
		
		Bundle extras = new Bundle();
		extras.putInt(WordWidgetProvider.EXTRA_ITEM, i = 0);
		Intent fillInIntent = new Intent();
		fillInIntent.putExtras(extras);
		views.setOnClickFillInIntent(R.id.widget_item, fillInIntent);
		Log.d(TAG,"getViewAt : About to return views");
		return views;
	}

	@Override
	public int getViewTypeCount() {
		return 0;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public void onCreate() {
	    Log.d(TAG,"onCreate method called");


	    for (int i = 0; i < mCount; i++) {
	        mWidgetItems.add(new WidgetItem("W:" + i));
	    }
	    mDbHelper = new DbAdapter(mContext);
	    mDbHelper.open();
	    Cursor c = mDbHelper.fetchAllWords();

	    boolean bool = c.moveToFirst();
	    Log.d(TAG,"Count :" +  c.getCount());
	}

	@Override
	public void onDataSetChanged() {
		
	}

	@Override
	public void onDestroy() {
		 mWidgetItems.clear();
	}
}
