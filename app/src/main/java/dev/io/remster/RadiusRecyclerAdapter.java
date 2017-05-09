package dev.io.remster;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.vision.text.Text;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

/**
 * Created by ninaadpai on 5/6/17.
 */

class RadiusRecyclerAdapter extends RecyclerView.Adapter<RadiusRecyclerAdapter.ViewHolder>{
    Context context;
    List<Integer> distances;
    String user;
    public RadiusRecyclerAdapter(FragmentActivity f, List<Integer> distances, String user) {
        this.context = f;
        this.distances = distances;
        this.user = user;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.radius_recycler_row, parent, false);
        v.setClickable(true);
        v.setFocusableInTouchMode(true);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Integer distance = distances.get(position);
        holder.radiusBackround.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseDatabase.getInstance().getReference().child("users").child(user).child("radiusPref").setValue(distance);
//                notifyDataSetChanged();
            }
        });
        DatabaseReference radiusRef = FirebaseDatabase.getInstance().getReference().child("users").child(user);
        radiusRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.child("radiusPref").exists()) {
                    int currentRadiusPref = Integer.parseInt(String.valueOf(dataSnapshot.child("radiusPref").getValue()));
                    if(currentRadiusPref == distance) {
                        holder.radiusText.setTextColor(Color.WHITE);
                        holder.radiusBackround.setBackgroundResource(R.drawable.selected_radius_background);                    }

                    else {
                        holder.radiusText.setTextColor(Color.BLACK);
                        holder.radiusBackround.setBackgroundResource(R.drawable.custom_alert_background);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        if(distance <= 1)
            holder.radiusText.setText(distance+" mile");
        else
            holder.radiusText.setText(distance+" miles");
    }

    @Override
    public int getItemCount() {
        return distances.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView radiusText;
        private RelativeLayout radiusBackround;
        public ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                }
            });
            radiusText = (TextView)itemView.findViewById(R.id.radiusText);
            radiusBackround = (RelativeLayout) itemView.findViewById(R.id.radiusBackground);
            radiusText.setTypeface(DashboardActivity.commonTFSemiBold);
        }
    }
}
