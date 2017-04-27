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
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.Timer;
import java.util.TimerTask;

public class SignupActivity extends AppCompatActivity {
    TextView appTitle, logInHint;
    EditText firstNameEditText, lastNameEditText, emailEditText, passwordEditText;
    Button signUpBtn;
    Typeface commonTF;
    ProgressDialog progressDialog;
    DatabaseReference databaseReference;
    FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        appTitle = (TextView)findViewById(R.id.appTitle);
        logInHint = (TextView)findViewById(R.id.logInHint);
        commonTF = Typeface.createFromAsset(getAssets(),"fonts/Nunito-Regular.ttf");
        firstNameEditText = (EditText) findViewById(R.id.firstNameEditText);
        lastNameEditText = (EditText) findViewById(R.id.lastNameEditText);
        emailEditText = (EditText) findViewById(R.id.emailEditText);
        passwordEditText = (EditText) findViewById(R.id.passwordEditText);
        signUpBtn = (Button)findViewById(R.id.signUpBtn);
        firstNameEditText.setTypeface(commonTF);
        lastNameEditText.setTypeface(commonTF);
        emailEditText.setTypeface(commonTF);
        passwordEditText.setTypeface(commonTF);
        appTitle.setTypeface(commonTF);
        signUpBtn.setTypeface(commonTF);
        logInHint.setTypeface(commonTF);
        databaseReference = FirebaseDatabase.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();

        findViewById(R.id.logInHint).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                startActivity(new Intent(SignupActivity.this, LandingActivity.class));
            }
        });

        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = emailEditText.getText().toString().trim();
                String pass = passwordEditText.getText().toString().trim();
                final String firstName = firstNameEditText.getText().toString().trim();
                final String lastName = lastNameEditText.getText().toString().trim();;
                if(TextUtils.isEmpty(email) || TextUtils.isEmpty(pass) || TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName)) {
                    showNoEmailToast();
                }
                else {
                    showProgressDialog();
                    firebaseAuth.createUserWithEmailAndPassword(email, pass)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()) {
                                        Object signUpTime= ServerValue.TIMESTAMP;
                                        User user = new User("", firstName, lastName, email, signUpTime,"");
                                        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                                        databaseReference.child(firebaseUser.getUid()).setValue(user);
                                        databaseReference.child(firebaseUser.getUid()).child("connections").setValue(0);
                                        databaseReference.child(firebaseUser.getUid()).child("connections_alert").setValue(0);
                                        databaseReference.child(firebaseUser.getUid()).child("notifications").child("connection_requests").setValue("");
                                        databaseReference.child(firebaseUser.getUid()).child("notifications").child("notif_clicked").setValue(1);
                                        databaseReference.child(firebaseUser.getUid()).child("profile_photo").child("encodedSchemeSpecificPart").setValue("");
                                        dismissProgressDialog();
                                        startActivity(new Intent(SignupActivity.this, DashboardActivity.class));
                                        finish();
                                    }
                                    else if(task.getException() instanceof FirebaseAuthUserCollisionException) {
                                        dismissProgressDialog();
                                        View layoutVal = LayoutInflater.from(SignupActivity.this).inflate(R.layout.signup_custom_toast, null);
                                        Toast toast = new Toast(getApplicationContext());
                                        toast.setDuration(Toast.LENGTH_LONG);
                                        toast.setGravity(Gravity.BOTTOM, 0, 0);
                                        toast.setView(layoutVal);//setting the view of custom toast layout
                                        toast.show();
                                    }
                                    else {
                                        dismissProgressDialog();
                                        View layoutVal = LayoutInflater.from(SignupActivity.this).inflate(R.layout.some_prob_toast, null);
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
    public void showProgressDialog() {
        progressDialog = new ProgressDialog(SignupActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Please Wait...");
        progressDialog.show();
    }

    public void dismissProgressDialog() {
        progressDialog.dismiss();
    }

    private void showNoEmailToast() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View dialogLayout = inflater.inflate(R.layout.empty_details_toast, null);

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
}
