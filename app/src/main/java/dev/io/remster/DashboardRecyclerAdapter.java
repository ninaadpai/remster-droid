package dev.io.remster;

import android.content.Context;
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
 * Created by ninaadpai on 4/29/17.
 */

class DashboardRecyclerAdapter extends ArrayAdapter<City> {
    List<City> mData;
    Context mContext;
    int mResource;

    public DashboardRecyclerAdapter(Context context, int resource, List<City> objects) {
        super(context, resource, objects);
        this.mData = objects;
        this.mContext = context;
        this.mResource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(mResource,parent,false);
        }
        City i = mData.get(position);
        TextView title = (TextView)convertView.findViewById(R.id.cityName);
        TextView description = (TextView)convertView.findViewById(R.id.description);
        title.setTypeface(DashboardActivity.commonTFSemiBold);
        description.setTypeface(DashboardActivity.commonTF);
        title.setText(i.getCityName().substring(0,1).toUpperCase()+i.getCityName().substring(1,i.getCityName().length()));
        description.setText("Some random description");
        ImageView image = (ImageView) convertView.findViewById(R.id.photo);
        Picasso.with(getContext())
                .load(i.getImgUrl())
                .into(image);
        return convertView;
    }
}
