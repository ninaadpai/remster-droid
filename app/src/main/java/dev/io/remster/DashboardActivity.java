package dev.io.remster;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.media.Image;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
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
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
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

public class DashboardActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    FirebaseAuth firebaseAuth;
    EditText searchEditText;
    static Typeface commonTF, commonTFSemiBold;
    ImageView profileImage, clearSearch;
    DatabaseReference databaseReference;
    StorageReference storageReference;
    FirebaseUser firebaseUser;
    private static final int GALLERY_INTENT = 2;
    ProgressDialog progressDialog;
    FrameLayout fragmentSwitcher;
    private GoogleApiClient mGoogleApiClient;
    protected GoogleApiClient mGoogleLocationClient;
    protected Location mLastLocation;

    RecyclerView placesRecycler;
    LinearLayoutManager mLinearLayoutManager;
    private PlacesAutoCompleteAdapter mAutoCompleteAdapter;
    FloatingActionButton getLatestLocation;
    private static final LatLngBounds myBounds = new LatLngBounds(new LatLng(-0, 0), new LatLng(0, 0));
    String provider;
    LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildGoogleApiClient();
        setContentView(R.layout.activity_dashboard);
        LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
        final boolean enabledGPS = service.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean enabledWiFi = service.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        getLatestLocation = (FloatingActionButton) findViewById(R.id.getLatestLocation);
        profileImage = (ImageView) findViewById(R.id.profileImage);
        clearSearch = (ImageView) findViewById(R.id.clearSearch);
        clearSearch.setVisibility(View.INVISIBLE);
        firebaseAuth = firebaseAuth.getInstance();
        searchEditText = (EditText) findViewById(R.id.searchEditText);
        placesRecycler = (RecyclerView) findViewById(R.id.placesRecycler);
        placesRecycler.setAdapter(mAutoCompleteAdapter);
        placesRecycler.setVisibility(View.GONE);
        commonTF = Typeface.createFromAsset(getAssets(), "fonts/Nunito-Regular.ttf");
        commonTFSemiBold = Typeface.createFromAsset(getAssets(), "fonts/Nunito-Bold.ttf");
        searchEditText.setTypeface(commonTF);
        firebaseAuth = firebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        firebaseUser = firebaseAuth.getCurrentUser();
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
        DatabaseReference profileImgRef = databaseReference.child(firebaseUser.getUid());
        profileImgRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!(dataSnapshot.child("profile_photo").child("encodedSchemeSpecificPart").getValue().equals(""))) {
                    Picasso.with(DashboardActivity.this)
                            .load(String.valueOf("https:" + dataSnapshot.child("profile_photo").child("encodedSchemeSpecificPart").getValue()))
                            .transform(new CircleTransform())
                            .into(profileImage);
                } else {
                    Picasso.with(DashboardActivity.this)
                            .load("https://firebasestorage.googleapis.com/v0/b/remster-e71bf.appspot.com/o/remster.png?alt=media&token=05c222c1-b54b-4fba-b5e1-6b606ee533f2")
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
//                finish();
                startActivity(new Intent(DashboardActivity.this, SettingsActivity.class));
                overridePendingTransition(R.anim.enter, R.anim.exit);
                finish();
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
                                    Toast.makeText(getApplicationContext(), String.valueOf(places.get(0).getName()), Toast.LENGTH_SHORT).show();
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

        getLatestLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(DashboardActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(DashboardActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    buildGoogleLocationClient();
                    // TODO: Consider calling
                    mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                    if (mLastLocation != null) {
                        Toast.makeText(getApplicationContext(), mLastLocation.getLatitude()+", "+mLastLocation.getLongitude(),Toast.LENGTH_LONG).show();
                    } else {
                        View layoutVal = LayoutInflater.from(DashboardActivity.this).inflate(R.layout.location_not_found, null);
                        Toast toast = new Toast(getApplicationContext());
                        toast.setDuration(Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.BOTTOM, 0, 20);
                        toast.setView(layoutVal);//setting the view of custom toast layout
                        toast.show();
                    }
                        return;
                }
            }
        });
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
        if (!mGoogleApiClient.isConnected() && !mGoogleApiClient.isConnecting()){
            Log.v("Google API","Connecting");
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mGoogleApiClient.isConnected()){
            Log.v("Google API","Dis-Connecting");
            mGoogleApiClient.disconnect();
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
                    databaseReference.child(firebaseUser.getUid()).child("profile_photo").setValue(taskSnapshot.getDownloadUrl());
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

}
