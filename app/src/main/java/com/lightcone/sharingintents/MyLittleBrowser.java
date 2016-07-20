package com.lightcone.sharingintents;

import java.net.URL;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ShareActionProvider;

public class MyLittleBrowser extends AppCompatActivity {

    private static final String TAG = "SHARE";
    private WebSettings wset;
    private ShareActionProvider shareActionProvider;
    private String urlString;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set up the WebView display.  See the projects WebViewDemo and WebViewDemo2
        // for more detailed discussion.

        WebView webview = new WebView(this);

        // The following prevents redirection out of MyLittleBrowser to another browser, forcing
        // redirects from pages displayed in MyLittleBrowser to stay in MyLittleBrowser.
        // See http://stackoverflow.com/questions/4066438/
        // android-webview-how-to-handle-redirects-in-app-instead-of-opening-a-browser

        webview.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url){
                view.loadUrl(url);
                return false;
            }

            // This will fire when main webpage has finished loading.  We use it
            // to enable a text share of the page title and URL.

            public void onPageFinished(WebView view, String url){

                // Get the title of the webpage once loaded
                String webpageTitle = view.getTitle();

                // Attach an intent to the ShareActionProvider that will share the title and
                // url of the webpage loaded.

                shareActionProvider.setShareIntent(shareText(webpageTitle+":\n\n"+url));
            }
        });

        setContentView(webview);
        wset = webview.getSettings();
        wset.setBuiltInZoomControls(true);
        wset.setJavaScriptEnabled(true);

        /** This class is launched only by an implicit share intent from an app requesting that
         * a webpage be displayed because of the declaration in the manifest file of the
         * present app
         *
         * <activity android:name=".MyLittleBrowser" android:label="@string/little_browser_name">
         <intent-filter>
         <action android:name="android.intent.action.VIEW" />
         <category android:name="android.intent.category.DEFAULT" />
         <data android:scheme="http"/>
         </intent-filter>
         * </activity>
         *
         * (It is never launched by an explicit intent from the present application.) Thus we must
         * first query the Intent that launched this class (very likely from a completely different app)
         * to retrieve the data that it contains. If all is well this will be the address of an HTML
         * page, which we shall display using WebView.
         */

        // Get the Intent that called this class (implicitly, probably from another app)
        Intent intent = getIntent();

        // Extract the action that the calling Intent used
        String intentAction = intent.getAction();

        // Extract the data from the Intent
        Uri intentData = intent.getData();

        // Wrap following in try-catch since URL constructor throws MalformedURLException
        try {
            String scheme = intentData.getScheme();
            String host = intentData.getHost();
            String path = intentData.getPath();
            String query = intentData.getQuery();
            urlString = new URL(scheme, host, path).toString();
            if(query != null) urlString = urlString + "?"+ query;
            webview.loadUrl(urlString);
            Log.i(TAG, "scheme="+scheme+" host="+host+" path="+path+" query="+query);
            Log.i(TAG, "action="+intentAction+" URL="+urlString);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        // Get the share menu item.
        MenuItem menuItem = menu.findItem(R.id.menu_share);

        // Get the provider. We will invoke it in onPageFinished(webview, url) to
        // enable a text share of the page title and URL once a webpage is loaded.

        shareActionProvider = (ShareActionProvider) menuItem.getActionProvider();

        return true;
    }

    // The class MyLittleBrowser is designed to display an html share from another app,
    // but we add to it the capability to do its own implicit share of the webpage title
    // and url that it processes. Very similar to textShare() in MainActivity. It
    // returns the text-sharing intent for use as an argument in populating the
    // ShareActionProvider menu when the webpage has finished loading; this is
    // invoked in the method onPageFinished() defined above.

    public Intent shareText(String s){

        // Create a sharing intent
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);

        // Set the type of the intent to text
        sharingIntent.setType("text/plain");

        // These allow some custom addresses to be defined. They will be filled in
        // automatically if an email client is invoked for the share, for example.

        String addresses [] = {getString(R.string.address1),
                getString(R.string.address2)};
        String CCaddresses [] = {getString(R.string.CC1)};
        String BCCaddresses [] = {getString(R.string.BCC1)};

        // Add the text to be shared to the intent
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, s);

        // Following optional fields for email. Will be ignored by sharing app
        // if not appropriate. For example, non-email apps may use the SUBJECT
        // extra and ignore the others.

        // Hardwire in a default subject
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
                getString(R.string.websubject));

        // Add some pre-defined addresses
        sharingIntent.putExtra(android.content.Intent.EXTRA_EMAIL, addresses);
        sharingIntent.putExtra(android.content.Intent.EXTRA_CC, CCaddresses);
        sharingIntent.putExtra(android.content.Intent.EXTRA_BCC, BCCaddresses);

        return sharingIntent;
    }
}
