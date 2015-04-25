package it.unina.is2project.sensorgames;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class FirstAccess extends Activity {

    private final String TAG = "FirstAccess";

    // Views on screen declaration
    private TextView helloView;
    private TextView appNameView;
    private EditText nickname;
    private Button okButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.firstaccess);

        // Set the fullscreen window
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // Find view
        findViews();
        // Set-up typeface
        setTypeface();
        // Set-up listners
        setListners();
        // Get Screen Dims for nickname form width
        Point p = getScreenDimensions();
        Log.d(TAG, "Screen dimensions: " + p.x + ", " + p.y);
        Log.d(TAG, "Previous nickname form width: " + nickname.getWidth());
        nickname.getLayoutParams().width = (int) (0.7f * p.x);
        Log.d(TAG, "Width set to: " + (int) (0.7f * p.x));
        Log.d(TAG, "Nickname form width: " + nickname.getWidth());


    }

    /**
     * Find views in activity_main.xml
     */
    public void findViews() {
        helloView = (TextView) findViewById(R.id.helloView);
        appNameView = (TextView) findViewById(R.id.nameFirstAccess);
        nickname = (EditText) findViewById(R.id.nicknameForm);
        okButton = (Button) findViewById(R.id.btnSubmit);
    }

    /**
     * Set the typeface
     */
    public void setTypeface() {
        // Load the font
        Typeface typeFace = Typeface.createFromAsset(getAssets(), "font/secrcode.ttf");
        // Set the typeface
        helloView.setTypeface(typeFace);
        appNameView.setTypeface(typeFace);
        nickname.setTypeface(typeFace);
    }

    /**
     * Set listners for buttons
     */
    public void setListners() {
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnOkClick();
            }
        });

    }

    /**
     * Get the screen dimensions
     */
    private Point getScreenDimensions() {
        Point mPoint = new Point();
        // Get screen dimensions
        Display display = getWindowManager().getDefaultDisplay();
        display.getSize(mPoint);

        return mPoint;
    }

    /**
     * Manage click on onePlayer button.
     */
    private void btnOkClick() {
        // Save shared preference
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Log.d(TAG, "Nickname found " + nickname.getText().toString());
        sharedPreferences.edit().putString("prefNickname", nickname.getText().toString()).commit();
        Log.d(TAG, "Nickname saved as " + sharedPreferences.getString("prefNickname", getString(R.string.txt_no_name)));
        // Intent to MainActivity
        Intent i = new Intent(FirstAccess.this, MainActivity.class);
        startActivity(i);
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
        finish();
    }


    @Override
    public void onBackPressed() {
        // do nothing
    }
}
