package dev.io.remster;


import android.annotation.TargetApi;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;


/**
 * A simple {@link Fragment} subclass.
 */
public class DashFragment extends Fragment implements DashboardActivity.updateDashboard{
    TextView radiusTitle;
    static List<Post> postList = new ArrayList<>();
    static Post p;
    static ProfileRecyclerAdapter profileRecyclerAdapter;
    static ListView profilePostRecycler;
    static FragmentActivity f;
    List<Integer> distances = new ArrayList<>();
    RecyclerView radiusRecycler;
    LinearLayoutManager mLinearLayoutManager;
    RadiusRecyclerAdapter radiusRecyclerAdapter;
    double homeLat, homeLong;
    double currenthomeLat, currenthomeLong;
    public DashFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_dash, container, false);
        f = getActivity();
        String user = FirebaseAuth.getInstance().getCurrentUser().getUid();
        profilePostRecycler = (ListView)view.findViewById(R.id.dashboardRecycler);
        radiusRecycler = (RecyclerView)view.findViewById(R.id.radiusRecycler);
        mLinearLayoutManager = new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false);
//        DatabaseReference locationRef = FirebaseDatabase.getInstance().getReference().child("users").child(user).child("homeLocation");
//        locationRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                homeLat = Double.parseDouble(dataSnapshot.child("latitude").getValue().toString());
//                homeLong = Double.parseDouble(dataSnapshot.child("longitude").getValue().toString());
//                checkLatLong(homeLat, homeLong);
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//            }
//        });

        DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference().child("posts");
        postsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                showData(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        radiusTitle = (TextView)view.findViewById(R.id.radiusTitle);
        radiusTitle.setTypeface(DashboardActivity.commonTFSemiBold);
        distances.add(1);distances.add(5);distances.add(10);distances.add(20);distances.add(30);distances.add(40);distances.add(50);
        radiusRecyclerAdapter = new RadiusRecyclerAdapter(f, distances, user);
        radiusRecycler.setLayoutManager(mLinearLayoutManager);
        radiusRecycler.setAdapter(radiusRecyclerAdapter);
        radiusRecycler.setHasFixedSize(true);
        radiusRecycler.setItemAnimator(new SlideInUpAnimator());
        return view;
    }

    private void checkLatLong(double homeLat, double homeLong) {
         currenthomeLat = homeLat;
         currenthomeLong = homeLong;
    //    createDashboard(currenthomeLat,currenthomeLong);
    }

    public void showData(DataSnapshot dataSnapshot) {
        //Log.i("DataSnapshot",String.valueOf(dataSnapshot));
        for(DataSnapshot post : dataSnapshot.getChildren()) {
            String postId = post.getKey().toString();
            String userId = "";
            String placeName = post.child("placeName").getValue().toString();
            String postCity = post.child("postCity").getValue().toString();
            String postState = post.child("postState").getValue().toString();
            String postCountry = post.child("postCountry").getValue().toString();
            final String postLat = post.child("postLat").getValue().toString();
            final String postLong = post.child("postLong").getValue().toString();
            String postDesc = post.child("postDesc").getValue().toString();
            Uri photo = Uri.parse(post.child("photo").child("encodedSchemeSpecificPart").getValue().toString());
            Object timestamp = post.child("timestamp").getValue();
            p = new Post(postId, userId, placeName, postCity, postState, postCountry, postLat, postLong, postDesc, photo, timestamp);
            postList.add(p);
        }
        Collections.sort(postList, new Comparator<Post>() {
            @Override
            public int compare(Post o1, Post o2) {
                if(Long.parseLong(String.valueOf(o1.getTimestamp())) >  Long.parseLong(String.valueOf(o2.getTimestamp())))
                    return -1;
                return 1;
            }
        });
        profileRecyclerAdapter = new ProfileRecyclerAdapter(f, R.layout.profile_card_row, postList);
        profilePostRecycler.setAdapter(profileRecyclerAdapter);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("Demo","FeedFragment onCreate");

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onResume() {
        super.onResume();
        Log.i("Demo","FeedFragment onResume");
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.i("Demo","FeedFragment onAttach");

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.i("Demo","FeedFragment onActivityCreated");
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onStart() {
        super.onStart();
        Log.i("Demo","FeedFragment onStart");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("Demo","FeedFragment onDestroy");

    }
    @Override
    public void resetDashboard(final Map location) {
        Log.i("MapInDashFragment", String.valueOf(location));
        final DatabaseReference postsReference = FirebaseDatabase.getInstance().getReference().child("posts");
        postsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot post : dataSnapshot.getChildren()) {
                    String postId = post.getKey().toString();
                    String userId = "";
                    String placeName = post.child("placeName").getValue().toString();
                    String postCity = post.child("postCity").getValue().toString();
                    String postState = post.child("postState").getValue().toString();
                    String postCountry = post.child("postCountry").getValue().toString();
                    String postLat = post.child("postLat").getValue().toString();
                    String postLong = post.child("postLong").getValue().toString();
                    String postDesc = post.child("postDesc").getValue().toString();
                    Uri photo = Uri.parse(post.child("photo").child("encodedSchemeSpecificPart").getValue().toString());
                    Object timestamp = post.child("timestamp").getValue();
                    p = new Post(postId, userId, placeName, postCity, postState, postCountry, postLat, postLong, postDesc, photo, timestamp);
                    if((distanceInKm(Double.parseDouble(String.valueOf(location.get("latitude"))),Double.parseDouble(String.valueOf(location.get("longitude"))),Double.parseDouble(p.getPostLat()), Double.parseDouble(p.getPostLong()))/1.6) < 50) {
                        postList.add(p);
                    }
                }
                Collections.sort(postList, new Comparator<Post>() {
                    @Override
                    public int compare(Post o1, Post o2) {
                        if(Long.parseLong(String.valueOf(o1.getTimestamp())) >  Long.parseLong(String.valueOf(o2.getTimestamp())))
                            return -1;
                        return 1;
                    }
                });

                Log.i("Passing", String.valueOf(postList));
                profileRecyclerAdapter = new ProfileRecyclerAdapter(f, R.layout.profile_card_row, postList);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    public static double distanceInKm(
            double lat1, double lng1, double lat2, double lng2) {
        int r = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                        * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double d = r * c;
        return d;
    }
}
