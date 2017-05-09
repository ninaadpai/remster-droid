package dev.io.remster;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

import java.util.Timer;
import java.util.TimerTask;

public class LandingActivity extends AppCompatActivity {
    TextView appTitle,signUpHint, logInTrouble, exploreLink, locateLink, photoLink;
    EditText emailEditText, passwordEditText;
    Button logInBtn;
    Typeface commonTF,commonTFSemiBold;
    FirebaseAuth firebaseAuth;
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);
        appTitle = (TextView)findViewById(R.id.appTitle);
        logInTrouble = (TextView)findViewById(R.id.logInTrouble);
        signUpHint = (TextView)findViewById(R.id.signUpHint);
        emailEditText = (EditText)findViewById(R.id.emailEditText);
        passwordEditText = (EditText)findViewById(R.id.passwordEditText);
        commonTF = Typeface.createFromAsset(getAssets(),"fonts/Arimo-Regular.ttf");
        commonTFSemiBold = Typeface.createFromAsset(getAssets(),"fonts/Arimo-Bold.ttf");
        logInBtn = (Button)findViewById(R.id.logInBtn);
        appTitle.setTypeface(commonTF);
        signUpHint.setTypeface(commonTF);
        emailEditText.setTypeface(commonTF);
        passwordEditText.setTypeface(commonTF);
        logInBtn.setTypeface(commonTF);
        logInTrouble.setTypeface(commonTF);
        exploreLink = (TextView)findViewById(R.id.exploreLink);
        locateLink = (TextView)findViewById(R.id.locateLink);
        photoLink = (TextView)findViewById(R.id.photoLink);
        exploreLink.setTypeface(commonTFSemiBold);
        locateLink.setTypeface(commonTFSemiBold);
        photoLink.setTypeface(commonTFSemiBold);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        firebaseAuth = FirebaseAuth.getInstance();

        if (user != null) {
            Intent i = new Intent(LandingActivity.this, DashboardActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        }
        findViewById(R.id.signUpHint).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                finish();
                startActivity(new Intent(LandingActivity.this, SignupActivity.class));
                overridePendingTransition(R.anim.enter, R.anim.exit);
                finish();

            }
        });
        logInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(emailEditText.getText().toString()) || TextUtils.isEmpty(passwordEditText.getText().toString())){
                    showNoEmailToast();
                }
                else {
                    showProgressDialog();
                    String email = emailEditText.getText().toString().trim();
                    String pass = passwordEditText.getText().toString().trim();
                    firebaseAuth.signInWithEmailAndPassword(email, pass)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()) {
                                        dismissProgressDialog();
                                        startActivity(new Intent(LandingActivity.this, DashboardActivity.class));
                                        finish();
                                    }
                                    else if(task.getException() instanceof FirebaseAuthInvalidUserException) {
                                        dismissProgressDialog();
                                        View layoutVal = LayoutInflater.from(LandingActivity.this).inflate(R.layout.login_not_valid, null);
                                        Toast toast = new Toast(getApplicationContext());
                                        toast.setDuration(Toast.LENGTH_LONG);
                                        toast.setGravity(Gravity.BOTTOM, 0, 0);
                                        toast.setView(layoutVal);//setting the view of custom toast layout
                                        toast.show();
                                    }
                                    else {
                                        dismissProgressDialog();
                                        View layoutVal = LayoutInflater.from(LandingActivity.this).inflate(R.layout.incorrect_details_toast, null);
                                        Toast toast = new Toast(getApplicationContext());
                                        toast.setDuration(Toast.LENGTH_LONG);
                                        toast.setGravity(Gravity.BOTTOM, 0, 0);
                                        toast.setView(layoutVal);//setting the view of custom toast layout
                                        toast.show();
                                    }
                                }
                            });
                }
            }
        });
    }
    private void showNoEmailToast() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View dialogLayout = inflater.inflate(R.layout.empty_email_toast, null);

        final android.app.AlertDialog dialog = builder.create();
        dialog.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        dialog.setView(dialogLayout, 0, 0, 0, 0);
        dialog.getWindow().getAttributes().windowAnimations = R.style.Animation;
        WindowManager.LayoutParams wlmp = dialog.getWindow()
                .getAttributes();
        wlmp.gravity = Gravity.BOTTOM;
        builder.setView(dialogLayout);
        dialog.show();
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                dialog.dismiss();
            }
        },2000);
    }
    public void showProgressDialog() {
        progressDialog = new ProgressDialog(LandingActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Please Wait...");
        progressDialog.show();
    }

    public void dismissProgressDialog() {
        progressDialog.dismiss();
    }
}
