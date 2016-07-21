package com.lightcone.sharingintents;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
//import android.widget.ShareActionProvider;
import android.support.v7.widget.ShareActionProvider;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements OnClickListener {

    private static final String TAG = "SHARE";
    private static final int REQUEST_CODE = 1;
    private ShareActionProvider shareActionProvider;
    private EditText inputText;
    private boolean isBarcodeScan = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create top toolbar

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        // Remove default toolbar title and replace with an icon
        if (toolbar != null) {
            toolbar.setNavigationIcon(R.mipmap.ic_launcher);
        }

        // Set background color.  Handle method getColor deprecated as of API 23
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            toolbar.setTitleTextColor(getResources().getColor(R.color.barTextColor, null));
        } else {
            toolbar.setTitleTextColor(getResources().getColor(R.color.barTextColor));
        }

        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        // Identify buttons in XML layout and attach click  listeners to each
        View button01 = findViewById(R.id.button01);
        button01.setOnClickListener(this);
        View button02 = findViewById(R.id.button02);
        button02.setOnClickListener(this);
        View button03 = findViewById(R.id.button03);
        button03.setOnClickListener(this);

        // Get handle to input text field
        inputText = (EditText) findViewById(R.id.editText1);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        // Get the share menu item
        MenuItem menuItem = menu.findItem(R.id.menu_share);
        // Get the provider
        shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        return true;
    }

    // Process button clicks
    @Override
    public void onClick(View v) {
        switch(v.getId()){

            // For button 1, share string data
            case R.id.button01:
                isBarcodeScan = false;
                // Launch the text share intent and associate it with the ShareActionProvider
                Intent share = shareText(inputText.getText().toString());
                Log.i(TAG,"intent="+share.toString());
                shareActionProvider.setShareIntent(share);

                // Force the soft keyboard closed so following Toast instructions easy to see
                InputMethodManager inputManager = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(v.getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);

                // Display Toast with instructions
                Toast.makeText(this, getString(R.string.toaster),
                        Toast.LENGTH_LONG).show();
                break;

            // For button 2, share barcode data
            case R.id.button02:
                isBarcodeScan = true;
                shareActionProvider.setShareIntent(shareBarcode());
                break;

            // For button 3, pick an image from gallery or other sources
            case R.id.button03:
                Intent j = new Intent(this, PickAnImage.class);
                startActivity(j);
                break;
        }
    }

    // Method to allow app to share text through implicit intent.  It returns
    // the sharing intent that it creates for potential use in the
    // ShareActionProvider that is managing the share menu in the top action bar.

    public Intent shareText(String s){

        // Create a sharing intent
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);

        // Set its type to text
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
                getString(R.string.subject));

        // Add default addresses
        sharingIntent.putExtra(android.content.Intent.EXTRA_EMAIL, addresses);
        sharingIntent.putExtra(android.content.Intent.EXTRA_CC, CCaddresses);
        sharingIntent.putExtra(android.content.Intent.EXTRA_BCC, BCCaddresses);

        // If this is text from a barcode scan created by shareBarcode(), wrap
        // the intent in a chooser and start. If it is from input text, don't
        // start here (will be invoked from the task bar share menu in that case).

        if(isBarcodeScan) startActivity(Intent.createChooser(sharingIntent,
                getString(R.string.shareInvite)));

        return sharingIntent;
    }

    /**
     Following method for scanning barcodes adapted from article by Alexander Lucas at

     http://android-developers.blogspot.com/2012/02/share-with-intents.html

     For this to work you must have an app installed on the device that can handle
     the barcode scanning intent.  Barcode Scanner or Google Goggles are available for
     free from the Google Play Store and will do the job.  Below we will check to see if
     there is a barcode scanning app on the device and give the user a choice of installing
     Barcode Scanner from the Play Store if not.
     */

    public Intent shareBarcode() {

        // Check for an app on the device to handle a barcode scan
        Intent intent = new Intent("com.google.zxing.client.android.SCAN");

        // Clear the app called with above intent from activity stack, so users arrive at
        // the main screen of the app rather than the barcode scanning screen when next
        // launched from the homescreen.

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);

        /** The following should scan most standard barcode formats but one can
         restrict the formats for particular applications by adding extras to the intent.
         Some examples:

         // QR codes (2D barcodes) only:
         intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
         // 1D barcodes only:
         intent.putExtra("SCAN_MODE", "ONE_D_MODE");
         // Only UPC and EAN barcodes (appropriate for shopping apps):
         intent.putExtra("SCAN_MODE", "PRODUCT_MODE"

         For more info, see http://code.google.com/p/zxing/source/browse/trunk/android/src/
         com/google/zxing/client/android/Intents.java
         */

        // The essential piece of the following is the startActivityForResult method, but we
        // wrap it in a try-catch to address the case of no app installed on the device that
        // can handle the barcode intent. We don't take similar precautions with the image and
        // plain text share examples because we assume that most devices will have apps
        // installed that can handle those MIME types.

        try {

            // App to handle barcode is present, so start the scanning intent. When result is
            // is returned, the onActivityResult() callback below will be invoked.

            startActivityForResult(intent, REQUEST_CODE);

        } catch(ActivityNotFoundException e1){
            // If no barcode scanning app on device, notify and give option to acquire one
            e1.printStackTrace();
            String errorTitle = getString(R.string.errorTitle1);
            String error = getString(R.string.errorString1);
            int icon = R.mipmap.ic_launcher;
            showTask(errorTitle, error, icon, this);
        }
        return intent;
    }

    // Will be called when the barcode scanning intent returns. It processes the
    // results into a string and calls shareText() to share the string.

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {

        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                //  Results have been returned, so process them
                String contents = intent.getStringExtra("SCAN_RESULT");
                String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
                Log.i(TAG, "scan string="+contents+" scan format="+format);
                shareText(contents);
            } else if (resultCode == RESULT_CANCELED) {
                String error =  getString(R.string.barcode_cancel);
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Method showTask() creates a custom  dialog that  presents a summary of the task
     * to the user and has buttons to either launch the task by executing the method launchTask()
     * or cancel the dialog.  In this case this dialog will be presented only if no app to handle the
     * barcode intent is found on the device. The dialog will give that warning and give the user
     * the option of going directly to the Android app market (asssuming a data connection exists)
     * to install the free app Barcode Scanner, which can handle the barcode intent, or to click
     * the Cancel button if the user wants to deal with the issue manually (for example, by
     * downloading a different barcode app like Google Goggles).
     */

    private void showTask(String title, String message, int icon, Context context){
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setIcon(icon);

        int positive = AlertDialog.BUTTON_POSITIVE;
        int negative = AlertDialog.BUTTON_NEGATIVE;

        alertDialog.setButton(positive, "Get Barcode Scanner", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                launchTask();
            }
        });

        alertDialog.setButton(negative, "Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        alertDialog.show();
    }


    // Method launchTask() is invoked by the showTask dialog to retrieve the app Barcode
    // Scanner from the Android app market (Play Store) if the device doesn't have a barcode scanner
    // app installed.

    private void launchTask(){
        String marketString = getString(R.string.playStoreAddress) + getString(R.string.bsString);
        goToMarket(marketString);
    }

    // Go to Android Play Store for a specific app defined by its url address in Play Store.
    private void goToMarket(String appString){
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        i.setData(Uri.parse(appString));
        startActivity(i);
    }

}
