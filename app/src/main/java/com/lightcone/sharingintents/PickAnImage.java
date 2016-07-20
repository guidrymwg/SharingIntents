package com.lightcone.sharingintents;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.support.v7.widget.ShareActionProvider;
//import android.widget.ShareActionProvider;

public class PickAnImage extends AppCompatActivity implements OnClickListener {

    private static final String TAG = "SHARE";
    private static final int REQUEST_CODE = 1;
    private Bitmap bitmap;
    private ImageView imageView;
    private Button shareButton;
    private Uri uri;
    private ShareActionProvider shareActionProvider;

    /** Called when the activity is first created. */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pickanimage);

        // Create top toolbar

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar2);
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
        // Note: getColor(color) deprecated as of API 23
        //toolbar.setTitleTextColor(getResources().getColor(R.color.barTextColor));
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        imageView = (ImageView) findViewById(R.id.theImage);
        shareButton = (Button) findViewById(R.id.button04);
        shareButton.setOnClickListener(this);
        chooseImage();
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

    // This will allow you to choose an image from the device by selecting it in a file chooser
    // such as Gallery.  It will be displayed in an ImageView once selected.

    public void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        startActivityForResult(intent, REQUEST_CODE);

        /** Alternative: Preceding line can be replaced by the following commented-out line,
         * which may give the user some say in the form of the chooser for some devices.  See the
         * API documentation of the Intent static string constant ACTION_GET_CONTENT for
         * discussion of the difference.
         */

        //startActivityForResult (Intent.createChooser(intent, "Message"), REQUEST_CODE);
    }

    // Executed when the activity started from chooseImage() with startActivityForResults
    // returns a result.  Uses a content resolver to retrieve an image from the device,
    // assign it to a bitmap, and display the bitmap. The Uri of the image file that is displayed
    // in the bitmap is saved in the variable uri to be passed to the method shareImage(uri),
    // which shares the bitmap.

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        InputStream inStream = null;
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK)
            try {
                uri = intent.getData();
                inStream = getContentResolver().openInputStream(uri);
                bitmap = BitmapFactory.decodeStream(inStream);
                imageView.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } finally {
                if (inStream != null)
                    try {
                        inStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            }
        super.onActivityResult(requestCode, resultCode, intent);
    }

    // Share the bitmap that was selected from the local filesystem using the chooseImage()
    // method by using an implicit intent.  When launched with
    // shareActionProvider.setShareIntent(shareImage(uri)) in the onClick method below,
    // this will populate the top Action Bar with an adaptive sharing menu (most used apps
    // will float to the top and the current default will appear as a separate icon), and
    // will open the sharing menu (if the startActivity line below is not commented out).

    public Intent shareImage(Uri uri){
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/*");
        // Clear activity stack if reset
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        //shareIntent.setAction(Intent.ACTION_SEND);
        Log.i(TAG, "shareImage:  Uri="+uri);
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);

        // Comment out following line to populate top ActionBar with adaptive sharing menu
        // but not open the sharing menu until selected by user. With this line present
        // the menu will be added and will be opened by default, which is likely the most
        // user-friendly workflow.

        startActivity(Intent.createChooser(shareIntent, getString(R.string.shareImage)));

        return shareIntent;
    }

    @Override
    public void onClick(View v) {

        switch(v.getId()){

            case R.id.button04:
                Log.i(TAG,"uri="+uri.toString());
                Intent temp = shareImage(uri);
                Log.i(TAG,"intent="+temp.toString());

                shareActionProvider.setShareIntent(shareImage(uri));
                break;
        }
    }
}
