package dev.io.remster;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
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
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

public class AddDetailsActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener  {
    FirebaseAuth firebaseAuth;
    EditText imageDescEditText, searchEditText;
    TextView charCount, postTitle, locationWarning;
    ImageView clearSearch, uploadPost, imageThumbnail,cancelPost;
    GoogleApiClient mGoogleApiClient;
    RecyclerView placesRecycler;
    LinearLayoutManager mLinearLayoutManager;
    private PlacesAutoCompleteAdapter mAutoCompleteAdapter;
    File imageFile;
    private static final LatLngBounds myBounds = new LatLngBounds(new LatLng(-0, 0), new LatLng(0, 0));
    StorageReference storageReference;
    DatabaseReference databaseReference;
    ProgressDialog progressDialog;
    Post p;
    Map<String, String> homeLocationMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildGoogleApiClient();
        firebaseAuth = FirebaseAuth.getInstance();
        setContentView(R.layout.activity_add_details);
        storageReference = FirebaseStorage.getInstance().getReference();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        imageDescEditText = (EditText)findViewById(R.id.imageDescEditText);
        searchEditText = (EditText)findViewById(R.id.searchEditText);
        imageDescEditText.setTypeface(DashboardActivity.commonTF);
        clearSearch = (ImageView) findViewById(R.id.clearSearchImageUpload);
        uploadPost = (ImageView) findViewById(R.id.uploadPost);
        clearSearch.setVisibility(View.INVISIBLE);
        cancelPost = (ImageView)findViewById(R.id.cancelPost);
        charCount = (TextView)findViewById(R.id.charCount);
        postTitle = (TextView)findViewById(R.id.postTitle);
        locationWarning = (TextView)findViewById(R.id.locationWarning);
        postTitle.setTypeface(DashboardActivity.commonTF);
        locationWarning.setTypeface(DashboardActivity.commonTF);
        searchEditText.setTypeface(DashboardActivity.commonTF);
        placesRecycler = (RecyclerView)findViewById(R.id.placesRecycler);
        placesRecycler.setVisibility(View.GONE);
        charCount.setTypeface(DashboardActivity.commonTF);
        charCount.setText("0/256");
        mLinearLayoutManager = new LinearLayoutManager(this);
        mAutoCompleteAdapter = new PlacesAutoCompleteAdapter(this, R.layout.places_search_row,
                mGoogleApiClient, myBounds, null, DashboardActivity.commonTF);
        placesRecycler.setLayoutManager(mLinearLayoutManager);
        imageThumbnail = (ImageView)findViewById(R.id.imageThumbnail);
        imageFile = (File) getIntent().getExtras().get("image");
        Log.i("Image details", String.valueOf(imageFile));
        imageThumbnail.setImageURI(Uri.fromFile(imageFile));
        clearSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchEditText.setText("");
            }
        });

        imageDescEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.toString().length() > 256) {
                    charCount.setTextColor(Color.RED);
                }
                if(s.toString().length() < 256) {
                    charCount.setTextColor(Color.BLACK);
                }
                charCount.setText(s.toString().length()+"/256");
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
                                    searchEditText.setText(places.get(0).getName());
                                    AsyncTask<String, String, Map> cst = new DashboardActivity.GetCityAsyncTask(AddDetailsActivity.this, places.get(0).getLatLng().latitude, places.get(0).getLatLng().longitude).execute();
                                    Map<String, String> lo = null;
                                    try {
                                        lo = cst.get();
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    } catch (ExecutionException e) {
                                        e.printStackTrace();
                                    }
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
        cancelPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(AddDetailsActivity.this);
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
                logOutWarning.setTypeface(DashboardActivity.commonTF);
                yesBtn.setTypeface(DashboardActivity.commonTF);
                noBtn.setTypeface(DashboardActivity.commonTF);
                dialog.findViewById(R.id.yesBtn).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            goToDashboard();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                });
                dialog.findViewById(R.id.noBtn).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
            }
        });
        uploadPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(imageDescEditText.getText().toString().length() > 256) {
                    showTextLimitToast();
                }
                else if(TextUtils.isEmpty(imageDescEditText.getText().toString()) || TextUtils.isEmpty(searchEditText.getText().toString())) {
                    showNoDetailsToast();
                }
                else {
                    startPostUpload();
                    Uri uri = Uri.fromFile(imageFile);
                    final StorageReference filepath = storageReference.child("posts").child(uri.getEncodedSchemeSpecificPart()+new AsyncUid().execute(1, 10));
                    filepath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            p = new Post("", firebaseAuth.getCurrentUser().getUid(), searchEditText.getText().toString(), homeLocationMap.get("city"),homeLocationMap.get("state"),homeLocationMap.get("country"),homeLocationMap.get("latitude"),homeLocationMap.get("longitude"), imageDescEditText.getText().toString().trim(),taskSnapshot.getDownloadUrl(), ServerValue.TIMESTAMP);
                            databaseReference.child("posts").push().setValue(p);
                            stopPostUpload();
                        }
                    });
                    startActivity(new Intent(AddDetailsActivity.this, DashboardActivity.class));
                }
            }
        });
    }

    private void showTextLimitToast() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View dialogLayout = inflater.inflate(R.layout.text_limit_toast, null);

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
            Log.v("Google API","Disconnecting");
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        try {
            goToDashboard();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void goToDashboard() throws FileNotFoundException {
        deleteCurrentImage(imageFile);
        finish();
        startActivity(new Intent(AddDetailsActivity.this, DashboardActivity.class));
    }

    private void deleteCurrentImage(File imageFile) throws FileNotFoundException {
        FileInputStream fis = new FileInputStream(new File(String.valueOf(imageFile)));
        deleteFile(String.valueOf(fis));
        try {
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showNoDetailsToast() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View dialogLayout = inflater.inflate(R.layout.empty_post_details_toast, null);

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

    public void startPostUpload() {
        progressDialog = new ProgressDialog(AddDetailsActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Posting...");
        progressDialog.show();
    }

    public void stopPostUpload() {
        progressDialog.dismiss();
    }

    public class AsyncUid extends AsyncTask<Integer, Integer, String> {
        @Override
        protected String doInBackground(Integer... params)
        {
            int count = params[0];
            int len = params[1];
            String pwd = null;
            for(int i=0;i<count;i++) {
                pwd = Util.getPassword(len);

            }
            return pwd;
        }

        @Override
        protected  void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            String a = "Async";

        }
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            progressDialog.setProgress(progressDialog.getProgress()+1);
        }
    }
}
