package dev.io.remster;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.media.audiofx.BassBoost;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

public class SettingsActivity extends AppCompatActivity {
    FirebaseAuth firebaseAuth;
    Typeface commonTF;
    ImageView backBtn;
    TextView settingsTitle,generalTitle, accountTitle, nameText, passwordText, emailText,userLocation, changeProfilePhoto, logOut;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        commonTF = Typeface.createFromAsset(getAssets(),"fonts/Nunito-Regular.ttf");
        backBtn = (ImageView)findViewById(R.id.backBtn);
        settingsTitle = (TextView)findViewById(R.id.settingsTitle);
        accountTitle = (TextView)findViewById(R.id.accountTitle);
        nameText = (TextView)findViewById(R.id.nameText);
        passwordText = (TextView)findViewById(R.id.passwordText);
        emailText = (TextView)findViewById(R.id.emailText);
        generalTitle = (TextView)findViewById(R.id.generalTitle);
        userLocation = (TextView)findViewById(R.id.userLocation);
        changeProfilePhoto = (TextView)findViewById(R.id.changeProfilePhoto);
        logOut = (TextView)findViewById(R.id.logOutText);
        settingsTitle.setTypeface(commonTF);
        generalTitle.setTypeface(commonTF);
        changeProfilePhoto.setTypeface(commonTF);
        logOut.setTypeface(commonTF);
        accountTitle.setTypeface(commonTF);
        nameText.setTypeface(commonTF);
        userLocation.setTypeface(commonTF);
        passwordText.setTypeface(commonTF);
        emailText.setTypeface(commonTF);
        firebaseAuth = firebaseAuth.getInstance();
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                finish();
                startActivity(new Intent(SettingsActivity.this, DashboardActivity.class));
                overridePendingTransition(R.anim.right_to_left, R.anim.left_to_right);
                finish();
            }
        });
        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLogOutDialog();
            }
        });
    }
    private void openLogOutDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(SettingsActivity.this);
        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View dialogLayout = inflater.inflate(R.layout.log_out_toast, null);
        final android.app.AlertDialog dialog = builder.create();
        dialog.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        dialog.setView(dialogLayout, 0, 0, 0, 0);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);
        WindowManager.LayoutParams wlmp = dialog.getWindow()
                .getAttributes();
        wlmp.gravity = Gravity.CENTER;
        wlmp.windowAnimations = R.style.Animation;
        builder.setView(dialogLayout);
        dialog.show();
        TextView logOutWarning = (TextView)dialog.findViewById(R.id.logOutWarning);
        Button yesBtn = (Button)dialog.findViewById(R.id.yesBtn);
        Button noBtn = (Button)dialog.findViewById(R.id.noBtn);
        logOutWarning.setTypeface(commonTF);
        yesBtn.setTypeface(commonTF);
        noBtn.setTypeface(commonTF);
        dialog.findViewById(R.id.yesBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseAuth.getInstance().signOut();
                startActivity(new Intent(SettingsActivity.this, LandingActivity.class));
                finish();
            }
        });
        dialog.findViewById(R.id.noBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }
}
