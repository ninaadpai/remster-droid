package dev.io.remster;


import android.annotation.TargetApi;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    FirebaseAuth firebaseAuth;
    TextView userName, noPostsText;
    Button postButton;
    List<Post> postList = new ArrayList<>();
    Post p;
    ListView profilePostRecycler;
    ProfileRecyclerAdapter profileRecyclerAdapter;
    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        firebaseAuth = FirebaseAuth.getInstance();
        DatabaseReference detailsRef;
        final FragmentActivity f = getActivity();
        detailsRef = FirebaseDatabase.getInstance().getReference().child("users").child(firebaseAuth.getCurrentUser().getUid());
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        userName = (TextView) view.findViewById(R.id.userName);
        noPostsText = (TextView) view.findViewById(R.id.noPostsText);
        postButton = (Button)view.findViewById(R.id.postButton);
        userName.setTypeface(DashboardActivity.commonTF);
        postButton.setTypeface(DashboardActivity.commonTF);
        noPostsText.setTypeface(DashboardActivity.commonTFSemiBold);
        profilePostRecycler = (ListView)view.findViewById(R.id.profilePostRecycler);
        DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference().child("posts");
        postsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot post : dataSnapshot.getChildren()) {
                    if(post.child("userId").getValue().equals(firebaseAuth.getCurrentUser().getUid())) {
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
                        postList.add(p);
                    }
                }
                if(postList.size() > 0) {
                    noPostsText.setVisibility(View.GONE);
                    postButton.setVisibility(View.GONE);
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
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        detailsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userName.setText(dataSnapshot.child("firstName").getValue()+" "+dataSnapshot.child("lastName").getValue());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return view;
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
}
