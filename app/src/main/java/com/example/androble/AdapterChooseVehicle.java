package com.example.androble;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by dhea on 13/07/2016.
 */
public class AdapterChooseVehicle extends PagerAdapter {
    Activity activity;
    List<Vehicle> vehicles;
    LayoutInflater inflater;

    public AdapterChooseVehicle(Activity activity, List<Vehicle> vehicles) {
        this.activity = activity;
        this.vehicles = vehicles;
    }

    @Override
    public int getCount() {
        return vehicles.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.equals(object);
    }

    @Override
    public Object instantiateItem(@NonNull ViewGroup container, final int position) {
        inflater = (LayoutInflater)activity.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView = inflater.inflate(R.layout.item,container,false);

        ImageView image;
        TextView noPlate, bikeType, bikeMerk;
        noPlate = (TextView) itemView.findViewById(R.id.noPlate);
        bikeType = (TextView) itemView.findViewById(R.id.bikeType);
        bikeMerk = (TextView) itemView.findViewById(R.id.bikeMerk);

        image = (ImageView)itemView.findViewById(R.id.image);
        noPlate.setText(vehicles.get(position).getPlateNo());
        bikeType.setText(vehicles.get(position).getBikeType());
        bikeMerk.setText(vehicles.get(position).getBikeMerk());

        DisplayMetrics dis = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dis);
        int height = dis.heightPixels;
        int width = dis.widthPixels;
        image.setMinimumHeight(height);
        image.setMinimumWidth(width);

            Picasso.with(activity.getApplicationContext())
                    .load(vehicles.get(position).getImage())
                    .placeholder(R.mipmap.ic_launcher)
                    .error(R.mipmap.ic_launcher)
                    .into(image);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, DetailActivity.class);
                intent.putExtra("param", vehicles.get(position).getPlateNo());
                intent.putExtra("param", vehicles.get(position).getBikeMerk());
                intent.putExtra("param", vehicles.get(position).getBikeType());
                intent.putExtra("param", vehicles.get(position).getImage());

                activity.startActivity(intent);
                // finish();
            }
        });


        container.addView(itemView);
        return itemView;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View)object);
    }
}
