package dev.io.remster;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

public class DashboardActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    EditText searchEditText;
    static Typeface commonTF, commonTFSemiBold;
    ImageView profileImage, clearSearch;
    DatabaseReference databaseReference;
    StorageReference storageReference;
    ProgressDialog progressDialog;
    FrameLayout fragmentSwitcher;
    GoogleApiClient mGoogleLocationClient;
    GoogleApiClient mGoogleApiClient;
    RecyclerView placesRecycler;
    LinearLayoutManager mLinearLayoutManager;
    private PlacesAutoCompleteAdapter mAutoCompleteAdapter;
    private static final LatLngBounds myBounds = new LatLngBounds(new LatLng(-0, 0), new LatLng(0, 0));
    private static int LOCATION_REQUEST = 1;
    private static final int GALLERY_INTENT = 2;
    private static int CAMERA_REQUEST = 3;
    updateDashboard mListener;
    double homeLat, homeLong;
    String defaultPhoto = "https://firebasestorage.googleapis.com/v0/b/remster-e71bf.appspot.com/o/remster.png?alt=media&token=05c222c1-b54b-4fba-b5e1-6b606ee533f2";
//    public DashboardActivity(updateDashboard mListener) {
//        this.mListener = mListener;
//    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildGoogleApiClient();
        setContentView(R.layout.activity_dashboard);
        buildGoogleLocationClient();
        DashFragment fr = new DashFragment();
        mListener = (updateDashboard) fr;
        profileImage = (ImageView) findViewById(R.id.profileImage);
        clearSearch = (ImageView) findViewById(R.id.clearSearch);
        clearSearch.setVisibility(View.INVISIBLE);
        searchEditText = (EditText) findViewById(R.id.searchEditText);
        placesRecycler = (RecyclerView) findViewById(R.id.placesRecycler);
        placesRecycler.setAdapter(mAutoCompleteAdapter);
        placesRecycler.setVisibility(View.INVISIBLE);
        commonTF = Typeface.createFromAsset(getAssets(), "fonts/Arimo-Regular.ttf");
        commonTFSemiBold = Typeface.createFromAsset(getAssets(), "fonts/Arimo-Bold.ttf");
        searchEditText.setTypeface(commonTF);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        mLinearLayoutManager = new LinearLayoutManager(this);
        mAutoCompleteAdapter = new PlacesAutoCompleteAdapter(this, R.layout.places_search_row,
                mGoogleApiClient, myBounds, null, commonTF);
        placesRecycler.setLayoutManager(mLinearLayoutManager);
        fragmentSwitcher = (FrameLayout) findViewById(R.id.fragmentSwitcher);
        FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
        tx.replace(R.id.fragmentSwitcher, new DashFragment()).commit();

        findViewById(R.id.dashFrag).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
                tx.replace(R.id.fragmentSwitcher, new DashFragment()).commit();
            }
        });
        findViewById(R.id.photoFrag).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                startActivity(new Intent(DashboardActivity.this, CameraActivity.class));
                overridePendingTransition(R.anim.enter, R.anim.exit);
            }
        });

        findViewById(R.id.profileFrag).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
                tx.replace(R.id.fragmentSwitcher, new ProfileFragment()).commit();
            }
        });


        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, GALLERY_INTENT);
            }
        });

        DatabaseReference profileImgRef = databaseReference.child("users").child(firebaseUser.getUid());
        profileImgRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.child("homeLocation").exists() && Integer.parseInt(String.valueOf(dataSnapshot.child("neverShowLocationToast").getValue())) == 0) {
                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(DashboardActivity.this);
                    LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    final View dialogLayout = inflater.inflate(R.layout.no_location_toast, null);

                    final android.app.AlertDialog dialog = builder.create();
                    dialog.getWindow().setSoftInputMode(
                            WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                    dialog.setView(dialogLayout, 0, 0, 0, 0);
                    dialog.getWindow().getAttributes().windowAnimations = R.style.Animation;
                    WindowManager.LayoutParams wlmp = dialog.getWindow()
                            .getAttributes();
                    dialog.setCancelable(false);
                    dialog.setCanceledOnTouchOutside(false);
                    wlmp.gravity = Gravity.CENTER;
                    builder.setView(dialogLayout);
                    dialog.show();
                    TextView homeLocationText = (TextView)dialog.findViewById(R.id.homeLocationText);
                    Button notNowBtn = (Button)dialog.findViewById(R.id.notNowBtn);
                    Button settingsBtn = (Button)dialog.findViewById(R.id.settingsBtn);
                    final CheckBox neverAgain = (CheckBox)dialog.findViewById(R.id.neverAgain);
                    homeLocationText.setTypeface(commonTF);
                    notNowBtn.setTypeface(commonTF);
                    settingsBtn.setTypeface(commonTF);
                    neverAgain.setTypeface(commonTF);
                    notNowBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(neverAgain.isChecked()) {
                                databaseReference.child("users").child(firebaseUser.getUid()).child("neverShowLocationToast").setValue(1);
                            }
                            dialog.dismiss();
                            showLocationToast();
                        }
                    });
                    settingsBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            finish();
                            startActivity(new Intent(DashboardActivity.this, SettingsActivity.class));
                            overridePendingTransition(R.anim.enter, R.anim.exit);
                        }
                    });
                }
                if (!(dataSnapshot.child("profile_photo").child("encodedSchemeSpecificPart").getValue().equals(""))) {
                    Picasso.with(DashboardActivity.this)
                            .load(String.valueOf("https:" + dataSnapshot.child("profile_photo").child("encodedSchemeSpecificPart").getValue()))
                            .transform(new CircleTransform())
                            .into(profileImage);
                } else {
                    Picasso.with(DashboardActivity.this)
                            .load(defaultPhoto)
                            .transform(new CircleTransform())
                            .into(profileImage);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        findViewById(R.id.settingsBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                startActivity(new Intent(DashboardActivity.this, SettingsActivity.class));
                overridePendingTransition(R.anim.enter, R.anim.exit);
            }
        });
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().length() > 0) {
                    clearSearch.setVisibility(View.VISIBLE);
                    if (mAutoCompleteAdapter != null) {
                        placesRecycler.setAdapter(mAutoCompleteAdapter);
                    }
                } else {
                    clearSearch.setVisibility(View.GONE);
                    placesRecycler.setVisibility(View.INVISIBLE);

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

        clearSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchEditText.setText("");
                FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
                tx.replace(R.id.fragmentSwitcher, new DashFragment()).commit();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        });

        placesRecycler.addOnItemTouchListener(
                new RecycleItemClickListener(this, new RecycleItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(final View view, int position) {
                        final PlacesAutoCompleteAdapter.PlaceAutocomplete item = mAutoCompleteAdapter.getItem(position);
                        final String placeId = String.valueOf(item.placeId);
                        Log.i("TAG", "Autocomplete item selected: " + item.description);
                        PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                                .getPlaceById(mGoogleApiClient, placeId);
                        placeResult.setResultCallback(new ResultCallback<PlaceBuffer>() {
                            @Override
                            public void onResult(PlaceBuffer places) {
                                if (places.getCount() == 1) {
                                    //Toast.makeText(getApplicationContext(), String.valueOf(places.get(0).getLatLng().latitude), Toast.LENGTH_SHORT).show();
                                    AsyncTask<String, String, Map> cst = new GetCityAsyncTask(DashboardActivity.this, places.get(0).getLatLng().latitude, places.get(0).getLatLng().longitude).execute();
                                    Map<String, String> lo = null;
                                    try {
                                        lo = cst.get();
                                        mListener.resetDashboard(lo);
                                        searchEditText.setText(places.get(0).getName());
                                        placesRecycler.setVisibility(View.GONE);
                                    } catch (InterruptedException e) {
                                        // TODO Auto-generated catch block
                                        e.printStackTrace();
                                    } catch (ExecutionException e) {
                                        // TODO Auto-generated catch block
                                        e.printStackTrace();
                                    }
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
    }
    private void showLocationToast() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View dialogLayout = inflater.inflate(R.layout.show_location_toast, null);

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
        },3000);
    }


    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .build();
    }
    protected synchronized void buildGoogleLocationClient() {
        mGoogleLocationClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
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
    @Override
    public void onResume() {
        super.onResume();
        if (!mGoogleApiClient.isConnected() && !mGoogleApiClient.isConnecting() && !mGoogleLocationClient.isConnected() && !mGoogleLocationClient.isConnecting()){
            Log.v("Google API","Connecting");
            mGoogleApiClient.connect();
            mGoogleLocationClient.connect();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mGoogleApiClient.isConnected() || mGoogleApiClient.isConnected()){
            Log.v("Google API","Dis-Connecting");
            mGoogleApiClient.disconnect();
            mGoogleLocationClient.disconnect();
        }
    }


    public static class CircleTransform implements Transformation {
        @Override
        public Bitmap transform(Bitmap source) {
            int size = Math.min(source.getWidth(), source.getHeight());

            int x = (source.getWidth() - size) / 2;
            int y = (source.getHeight() - size) / 2;

            Bitmap squaredBitmap = Bitmap.createBitmap(source, x, y, size, size);
            if (squaredBitmap != source) {
                source.recycle();
            }

            Bitmap bitmap = Bitmap.createBitmap(size, size, source.getConfig());

            Canvas canvas = new Canvas(bitmap);
            Paint paint = new Paint();
            BitmapShader shader = new BitmapShader(squaredBitmap,
                    BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);
            paint.setShader(shader);
            paint.setAntiAlias(true);

            float r = size / 2f;
            canvas.drawCircle(r, r, r, paint);

            squaredBitmap.recycle();
            return bitmap;
        }

        @Override
        public String key() {
            return "circle";
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==GALLERY_INTENT && resultCode==RESULT_OK) {
            startImageUpload();
            final Uri uri = data.getData();
            final StorageReference filepath = storageReference.child("profile_photos").child(firebaseUser.getUid()).child(uri.getLastPathSegment());
            filepath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    databaseReference.child("users").child(firebaseUser.getUid()).child("profile_photo").setValue(taskSnapshot.getDownloadUrl());
                    Picasso.with(DashboardActivity.this)
                            .load(String.valueOf(taskSnapshot.getDownloadUrl()))
                            .transform(new CircleTransform())
                            .into(profileImage);
                    stopImageUpload();
                }
            });
        }
    }

    public void startImageUpload() {
        progressDialog = new ProgressDialog(DashboardActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Uploading Your Photo...");
        progressDialog.show();
    }

    public void stopImageUpload() {
        progressDialog.dismiss();
    }

    public static class GetCityAsyncTask extends AsyncTask<String, String, Map>{
        Activity act;
        double latitude;
        double longitude;

        public GetCityAsyncTask(Activity act, double latitude, double longitude) {
            // TODO Auto-generated constructor stub
            this.act = act;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        @Override
        protected Map doInBackground(String... params) {
            Map<String, String> result = new HashMap<>();
            Geocoder geocoder = new Geocoder(act, Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocation(latitude,
                        longitude, 1);
                Log.e("Addresses", "-->" + addresses);
                result.put("city",addresses.get(0).getLocality());
                result.put("state",addresses.get(0).getAdminArea());
                result.put("country",addresses.get(0).getCountryName());
                result.put("latitude",String.valueOf(addresses.get(0).getLatitude()));
                result.put("longitude",String.valueOf(addresses.get(0).getLongitude()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(Map result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);

        }

    }

    public interface updateDashboard {
        public void resetDashboard(Map location);
    }
}
