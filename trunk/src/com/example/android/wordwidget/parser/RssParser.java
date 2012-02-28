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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

public class RssParser extends DefaultHandler {  
    private static final String TAG = RssParser.class.getCanonicalName();
	private String        mUrlString;
    private RssFeed       mRssFeed;
    private StringBuilder mText;
    private Item          mItem;
    private boolean       mImgStatus;
   
    public RssParser(String url) {
        this.mUrlString = url;
        mText = new StringBuilder();
    }

    public void parse() {
        InputStream inputStream = null;
        SAXParserFactory spf = null;
        SAXParser sp = null;
       
        try{
            URL url = new URL(this.mUrlString);
            _setProxy(); 
            inputStream = url.openConnection().getInputStream();           
            spf = SAXParserFactory.newInstance();
            if (spf != null) {
                sp = spf.newSAXParser();
                sp.parse(inputStream, this);
            }
        }catch (Exception e) {
        	Log.e(TAG,e.toString(), e);
           
        }finally {
            try {
                if (inputStream != null) inputStream.close();
            }catch (Exception e) {
            	Log.e(TAG,e.toString(),e);
            }
        }
    }
    public RssFeed getFeed() {
        return (this.mRssFeed);
    }
   
    public void startElement(String uri, String localName, String qName,
            Attributes attributes) {
        if (qName.equalsIgnoreCase("channel"))
            this.mRssFeed = new RssFeed();
        else if (qName.equalsIgnoreCase("item") && (this.mRssFeed != null)) {
            this.mItem = new Item();
        }else if (qName.equalsIgnoreCase("image") && (this.mRssFeed != null)) {
            this.mImgStatus = true;
        }
        
    }
   
    public void endElement(String uri, String localName, String qName) {
        if (this.mRssFeed == null)
            return;
       
        if(qName.equalsIgnoreCase("item")) {
        	this.mRssFeed.addItem(this.mItem);
        }else if (qName.equalsIgnoreCase("title")) {
            if (this.mItem != null) this.mItem.mTitle = this.mText.toString().trim();
            else this.mRssFeed.title = this.mText.toString().trim();
        }else if (qName.equalsIgnoreCase("description")){
            if (this.mItem != null) this.mItem.mDescription = this.mText.toString().trim();
            else this.mRssFeed.description = this.mText.toString().trim();
        }else if(qName.equals("ttl")){
        	if(this.mItem != null) {
        		this.mItem.mTtl = this.mText.toString().trim();
        	}
        }else if(qName.equals("pubDate")){
        	if(this.mItem != null) {
        		this.mItem.mPubDate = this.mText.toString().trim();
        	}
        }
       
        this.mText.setLength(0);
    }
   
    public void characters(char[] ch, int start, int length) {
        this.mText.append(ch, start, length);
    }
   
    public static void _setProxy()
    throws IOException {
        Properties sysProperties = System.getProperties();
        sysProperties.put("proxyHost", "<Proxy IP Address>");
        sysProperties.put("proxyPort", "<Proxy Port Number>");
        System.setProperties(sysProperties);
    }
  
    public static class RssFeed {
        public  String title;
        public  String description;
        public ArrayList <Item> items;
        
        public void addItem(Item item) {
            if (this.items == null)
                this.items = new ArrayList<Item>();
            this.items.add(item);
        }
        
        public Item getFirstItem() {
        	return items.get(0);
        }
    }
   
    public static class Item {
        public  String mTitle;
        public  String mDescription;
        public  String mTtl;
        public  String mPubDate ; 
       
        public String toString() {
            return (this.mTitle + ": " + this.mDescription);
        }
    }
}