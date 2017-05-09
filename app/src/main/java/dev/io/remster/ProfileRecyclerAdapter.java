package dev.io.remster;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by ninaadpai on 5/5/17.
 */

class ProfileRecyclerAdapter extends ArrayAdapter<Post> {
    List<Post> mData;
    Context mContext;
    int mResource;


    public ProfileRecyclerAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<Post> objects) {
        super(context, resource, objects);
        this.mContext = context;
        this.mData = objects;
        this.mResource = resource;
    }
    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(mResource,parent,false);
        Post i = mData.get(position);
        TextView name = (TextView)convertView.findViewById(R.id.cityName);
        TextView moreDetails = (TextView)convertView.findViewById(R.id.moreDetails);
        TextView postDesc = (TextView)convertView.findViewById(R.id.postDesc);
        TextView timeStamp = (TextView)convertView.findViewById(R.id.timeStamp);

        name.setTypeface(DashboardActivity.commonTFSemiBold);
        moreDetails.setTypeface(DashboardActivity.commonTF);
        postDesc.setTypeface(DashboardActivity.commonTF);
        timeStamp.setTypeface(DashboardActivity.commonTFSemiBold);
        name.setText(i.getPlaceName());
        moreDetails.setText(i.getPostCity()+", "+i.getPostState());
        ImageView image = (ImageView) convertView.findViewById(R.id.photo);
        Picasso.with(getContext())
                .load("https:"+i.getPhoto())
                .into(image);
        postDesc.setText(i.getPostDesc());
        long currentTime = System.currentTimeMillis();
        long postTimeStamp = Long.parseLong(String.valueOf(i.getTimestamp()));
        timeStamp.setText(timeDiff(currentTime - postTimeStamp));
        return convertView;
    }

    private String timeDiff(long timeDifferenceMilliseconds) {
        long diffSeconds = timeDifferenceMilliseconds / 1000;
        long diffMinutes = timeDifferenceMilliseconds / (60 * 1000);
        long diffHours = timeDifferenceMilliseconds / (60 * 60 * 1000);
        long diffDays = timeDifferenceMilliseconds / (60 * 60 * 1000 * 24);
        long diffWeeks = timeDifferenceMilliseconds / (60 * 60 * 1000 * 24 * 7);
        long diffMonths = (long) (timeDifferenceMilliseconds / (60 * 60 * 1000 * 24 * 30.41666666));
        long diffYears = timeDifferenceMilliseconds / ((long)60 * 60 * 1000 * 24 * 365);

        if (diffSeconds < 1) {
            return "Less than a second ago";
        } else if (diffMinutes < 1) {
            return diffSeconds + " secs ago";
        } else if (diffHours < 1) {
            if(diffMinutes<2)
                return diffMinutes + " min ago";
            else
                return diffMinutes + " mins ago";
        } else if (diffDays < 1) {
            if(diffHours<2)
                return diffHours + " hr ago";
            else
                return diffHours + " hrs ago";
        } else if (diffWeeks < 1) {
            if(diffDays < 2)
                return diffDays + " day ago";
            else
                return diffDays + " days ago";
        } else if (diffMonths < 1) {
            return diffWeeks + " W ago";
        } else if (diffYears < 1) {
            return diffMonths + " M ago";
        } else {
            return diffYears + " yrs ago";
        }
    }

}
