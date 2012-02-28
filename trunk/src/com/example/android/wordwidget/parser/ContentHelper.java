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
package com.example.android.wordwidget.parser;

import com.example.android.wordwidget.parser.RssParser.RssFeed;
import android.util.Log;

public class ContentHelper {
	private static final String TAG = ContentHelper.class.getName();
	private static String dictionary_dot_com_url = "http://toolserver.org/~enwikt/wotd/";
	private RssFeed mFeed;
	public  RssFeed getFeed() throws InterruptedException, RssFeedNullException {
		int count = 0;
		final RssParser mParser = new RssParser(dictionary_dot_com_url);
		
		new Thread(new Runnable(){
			@Override
			public void run() {
				Log.d(TAG,"Inside run()");
				try { 
				    mParser.parse();
				    mFeed = mParser.getFeed();
				    Log.d(TAG,mFeed.getFirstItem().mTitle + " : " + mFeed.getFirstItem().mDescription);
				}catch(Throwable t) {
				    Log.e(TAG,"Exception", t);
				}
			}
		}).start();
		while(mFeed == null){
			Thread.sleep(10);
		}
		
		Log.d(TAG,"About to return from getFeed()");
		return mFeed;
	}
	
}
