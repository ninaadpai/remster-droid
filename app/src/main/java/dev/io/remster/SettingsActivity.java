package dev.io.remster;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.media.audiofx.BassBoost;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

public class SettingsActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    FirebaseAuth firebaseAuth;
    Typeface commonTF;
    ImageView backBtn,profilePhoto;
    private static final int GALLERY_INTENT = 2;
    TextView settingsTitle,generalTitle, accountTitle, nameText, passwordText, emailText,homeLocation, changeProfilePhoto, logOut;
    DatabaseReference detailsRef;
    ProgressDialog progressDialog;
    StorageReference storageReference;
    GoogleApiClient mGoogleApiClient;
    RecyclerView placesRecycler;
    LinearLayoutManager mLinearLayoutManager;
    private PlacesAutoCompleteAdapter mAutoCompleteAdapter;
    Map<String, String> homeLocationMap = new HashMap<>();
    private static final LatLngBounds myBounds = new LatLngBounds(new LatLng(-0, 0), new LatLng(0, 0));
    private static int LOCATION_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        buildGoogleApiClient();
        commonTF = Typeface.createFromAsset(getAssets(),"fonts/Arimo-Regular.ttf");
        backBtn = (ImageView)findViewById(R.id.backBtn);
        profilePhoto = (ImageView)findViewById(R.id.profilePhoto);
        settingsTitle = (TextView)findViewById(R.id.settingsTitle);
        accountTitle = (TextView)findViewById(R.id.accountTitle);
        nameText = (TextView)findViewById(R.id.nameText);
        passwordText = (TextView)findViewById(R.id.passwordText);
        emailText = (TextView)findViewById(R.id.emailText);
        generalTitle = (TextView)findViewById(R.id.generalTitle);
        homeLocation = (TextView)findViewById(R.id.homeLocation);
        changeProfilePhoto = (TextView)findViewById(R.id.changeProfilePhoto);
        logOut = (TextView)findViewById(R.id.logOutText);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mAutoCompleteAdapter = new PlacesAutoCompleteAdapter(this, R.layout.places_search_row,
                mGoogleApiClient, myBounds, null, commonTF);
        settingsTitle.setTypeface(commonTF);
        generalTitle.setTypeface(commonTF);
        changeProfilePhoto.setTypeface(commonTF);
        logOut.setTypeface(commonTF);
        accountTitle.setTypeface(commonTF);
        nameText.setTypeface(commonTF);
        homeLocation.setTypeface(commonTF);
        passwordText.setTypeface(commonTF);
        emailText.setTypeface(commonTF);
        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();
        detailsRef = FirebaseDatabase.getInstance().getReference().child("users").child(firebaseAuth.getCurrentUser().getUid());
        detailsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                emailText.setText(dataSnapshot.child("emailAddress").getValue().toString());
                nameText.setText(dataSnapshot.child("firstName").getValue()+" "+dataSnapshot.child("lastName").getValue());
                if (!(dataSnapshot.child("profile_photo").child("encodedSchemeSpecificPart").getValue().equals(""))) {
                    Picasso.with(SettingsActivity.this)
                            .load(String.valueOf("https:" + dataSnapshot.child("profile_photo").child("encodedSchemeSpecificPart").getValue()))
                            .transform(new DashboardActivity.CircleTransform())
                            .into(profilePhoto);
                } else {
                    Picasso.with(SettingsActivity.this)
                            .load("https://firebasestorage.googleapis.com/v0/b/remster-e71bf.appspot.com/o/remster.png?alt=media&token=05c222c1-b54b-4fba-b5e1-6b606ee533f2")
                            .transform(new DashboardActivity.CircleTransform())
                            .into(profilePhoto);
                }
                if(dataSnapshot.child("homeLocation").exists())
                    homeLocation.setText("Home: "+dataSnapshot.child("homeLocation").child("city").getValue()+", "+dataSnapshot.child("homeLocation").child("state").getValue());
                else
                    homeLocation.setText("Home: No Location Set");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingsActivity.this, DashboardActivity.class));
                overridePendingTransition(R.anim.right_to_left, R.anim.left_to_right);
                finish();
            }
        });
        homeLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLocationDialog();
            }
        });
        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLogOutDialog();
            }
        });
        changeProfilePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, GALLERY_INTENT);
            }
        });
        profilePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, GALLERY_INTENT);
            }
        });
    }

    private void openLocationDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(SettingsActivity.this);
        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View dialogLayout = inflater.inflate(R.layout.location_dialog, null);
        final android.app.AlertDialog dialog = builder.create();
        dialog.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        dialog.setView(dialogLayout, 0, 0, 0, 0);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);
        WindowManager.LayoutParams wlmp = dialog.getWindow()
                .getAttributes();
        wlmp.gravity = Gravity.TOP;
        wlmp.windowAnimations = R.style.Animation;
        builder.setView(dialogLayout);
        dialog.show();
        TextView setLocationtext = (TextView)dialog.findViewById(R.id.locationString);
        final EditText locationSuggestion = (EditText) dialog.findViewById(R.id.locationSuggestion);
        final ImageView clearSearch = (ImageView)dialog.findViewById(R.id.clearSearch);
        placesRecycler = (RecyclerView)dialog.findViewById(R.id.placesRecycler);
        placesRecycler.setAdapter(mAutoCompleteAdapter);
        placesRecycler.setLayoutManager(mLinearLayoutManager);
        placesRecycler.setVisibility(View.GONE);
        clearSearch.setVisibility(View.GONE);

        setLocationtext.setTypeface(commonTF);
        locationSuggestion.setTypeface(commonTF);
        locationSuggestion.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().length() > 0) {
                    clearSearch.setVisibility(View.VISIBLE);
                } else {
                    clearSearch.setVisibility(View.GONE);
                    placesRecycler.setVisibility(View.GONE);

                }
                if (!s.toString().equals("") && mGoogleApiClient.isConnected()) {
                    mAutoCompleteAdapter.getFilter().filter(s.toString());
                    placesRecycler.setVisibility(View.VISIBLE);
                } else if (!mGoogleApiClient.isConnected()) {
                    Log.e("", "NOT CONNECTED");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        placesRecycler.addOnItemTouchListener(
                new RecycleItemClickListener(this, new RecycleItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        final PlacesAutoCompleteAdapter.PlaceAutocomplete item = mAutoCompleteAdapter.getItem(position);
                        final String placeId = String.valueOf(item.placeId);
                        Log.i("TAG", "Autocomplete item selected: " + item.description);
                        PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                                .getPlaceById(mGoogleApiClient, placeId);

                        placeResult.setResultCallback(new ResultCallback<PlaceBuffer>() {
                            @Override
                            public void onResult(PlaceBuffer places) {
                                if (places.getCount() == 1) {
                                    AsyncTask<String, String, Map> cst = new DashboardActivity.GetCityAsyncTask(SettingsActivity.this, places.get(0).getLatLng().latitude, places.get(0).getLatLng().longitude).execute();
                                    Map<String, String> lo = null;
                                    try {
                                        lo = cst.get();
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    } catch (ExecutionException e) {
                                        e.printStackTrace();
                                    }
                                    locationSuggestion.setText(lo.get("city")+", "+lo.get("state"));
                                    homeLocationMap.put("city",lo.get("city"));
                                    homeLocationMap.put("state",lo.get("state"));
                                    homeLocationMap.put("country",lo.get("country"));
                                    homeLocationMap.put("latitude",lo.get("latitude"));
                                    homeLocationMap.put("longitude",lo.get("longitude"));
                                    placesRecycler.setVisibility(View.GONE);
                                } else {
                                    Toast.makeText(getApplicationContext(), Constants.SOMETHING_WENT_WRONG, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        Log.i("TAG", "Clicked: " + item.description);
                        Log.i("TAG", "Called getPlaceById to get Place details for " + item.placeId);
                    }
                })
        );
        clearSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locationSuggestion.setText("");
                clearSearch.setVisibility(View.GONE);
            }
        });
        dialog.findViewById(R.id.setLocation).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(locationSuggestion.getText())){
                    showNoLocationToast();
                }
                else {
                    FirebaseDatabase.getInstance().getReference().child("users").child(firebaseAuth.getCurrentUser().getUid()).child("homeLocation").child("city").setValue(homeLocationMap.get("city"));
                    FirebaseDatabase.getInstance().getReference().child("users").child(firebaseAuth.getCurrentUser().getUid()).child("homeLocation").child("state").setValue(homeLocationMap.get("state"));
                    FirebaseDatabase.getInstance().getReference().child("users").child(firebaseAuth.getCurrentUser().getUid()).child("homeLocation").child("country").setValue(homeLocationMap.get("country"));
                    FirebaseDatabase.getInstance().getReference().child("users").child(firebaseAuth.getCurrentUser().getUid()).child("homeLocation").child("latitude").setValue(homeLocationMap.get("latitude"));
                    FirebaseDatabase.getInstance().getReference().child("users").child(firebaseAuth.getCurrentUser().getUid()).child("homeLocation").child("longitude").setValue(homeLocationMap.get("longitude"));
                    FirebaseDatabase.getInstance().getReference().child("users").child(firebaseAuth.getCurrentUser().getUid()).child("neverShowLocationToast").setValue(1);
                    dialog.dismiss();
                }
            }
        });
        dialog.findViewById(R.id.cancelLocation).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        startActivity(new Intent(SettingsActivity.this, DashboardActivity.class));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_INTENT && resultCode == RESULT_OK) {
            startImageUpload();
            final Uri uri = data.getData();
            final StorageReference filepath = storageReference.child("profile_photos").child(firebaseAuth.getCurrentUser().getUid()).child(uri.getLastPathSegment());
            filepath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    detailsRef.child("profile_photo").setValue(taskSnapshot.getDownloadUrl());
                    Picasso.with(SettingsActivity.this)
                            .load(String.valueOf(taskSnapshot.getDownloadUrl()))
                            .transform(new DashboardActivity.CircleTransform())
                            .into(profilePhoto);
                    stopImageUpload();
                }
            });
        }
        if(requestCode==LOCATION_REQUEST && resultCode==RESULT_OK) {
            Place place = PlacePicker.getPlace(data, this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!mGoogleApiClient.isConnected() && !mGoogleApiClient.isConnecting()){
            Log.v("Google API","Connecting");
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mGoogleApiClient.isConnected() || mGoogleApiClient.isConnected()){
            Log.v("Google API","Dis-Connecting");
            mGoogleApiClient.disconnect();
        }
    }

    public void startImageUpload() {
        progressDialog = new ProgressDialog(SettingsActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Uploading Your Photo...");
        progressDialog.show();
    }

    public void stopImageUpload() {
        progressDialog.dismiss();
    }
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .build();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
    private void showNoLocationToast() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View dialogLayout = inflater.inflate(R.layout.empty_location_toast, null);

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

