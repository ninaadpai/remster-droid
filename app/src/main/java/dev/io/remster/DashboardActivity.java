package dev.io.remster;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;

public class DashboardActivity extends AppCompatActivity {
    FirebaseAuth firebaseAuth;
    EditText searchEditText;
    Typeface commonTF;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        firebaseAuth = firebaseAuth.getInstance();
        searchEditText = (EditText)findViewById(R.id.searchEditText);
        commonTF = Typeface.createFromAsset(getAssets(),"fonts/Nunito-Regular.ttf");
        searchEditText.setTypeface(commonTF);
        findViewById(R.id.logOut).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseAuth.getInstance().signOut();
                startActivity(new Intent(DashboardActivity.this, LandingActivity.class));
                finish();
            }
        });
    }
}
