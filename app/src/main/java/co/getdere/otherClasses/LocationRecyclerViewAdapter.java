package co.getdere.otherClasses;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.recyclerview.widget.RecyclerView;
import co.getdere.R;
import co.getdere.models.Images;
import com.bumptech.glide.Glide;
import com.google.firebase.database.*;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;

import java.util.List;

public class LocationRecyclerViewAdapter extends
        RecyclerView.Adapter<LocationRecyclerViewAdapter.MyViewHolder> {

    private List<SingleRecyclerViewLocation> locationList;
    private MapboxMap map;

    public LocationRecyclerViewAdapter(List<SingleRecyclerViewLocation> locationList, MapboxMap mapBoxMap) {
        this.locationList = locationList;
        this.map = mapBoxMap;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.rv_on_top_of_map_card, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        SingleRecyclerViewLocation singleRecyclerViewLocation = locationList.get(position);


//        holder.image.setText(singleRecyclerViewLocation.getName()); this is where I glide the photo

        DatabaseReference imageRef = FirebaseDatabase.getInstance().getReference("/images/-Lbv6Vj35X_NFHe6te_3/body");

//        DatabaseReference imageRef = FirebaseDatabase.getInstance().getReference("/images/" + singleRecyclerViewLocation.getImageId() + "/body");


        imageRef.addListenerForSingleValueEvent(new ValueEventListener() { //attach listener

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) { //something changed!

                Images imageObject = dataSnapshot.getValue(Images.class);
                Glide.with(holder.itemView.getRootView()).load(imageObject.getImageBig()).into(holder.image);


            }

            @Override
            public void onCancelled(DatabaseError databaseError) { //update UI here if error occurred.

            }
        });



        holder.setClickListener(new ItemClickListener() {
            @Override
            public void onClick(View view, int position) {
                LatLng selectedLocationLatLng = locationList.get(position).getLocationCoordinates();
                CameraPosition newCameraPosition = new CameraPosition.Builder()
                        .target(selectedLocationLatLng)
                        .build();
                map.easeCamera(CameraUpdateFactory.newCameraPosition(newCameraPosition));
            }
        });
    }

    @Override
    public int getItemCount() {
        return locationList.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView image;
        ItemClickListener clickListener;

        MyViewHolder(View view) {
            super(view);
            image = view.findViewById(R.id.rv_on_top_of_map_image);
            image.setOnClickListener(this);
        }

        public void setClickListener(ItemClickListener itemClickListener) {
            this.clickListener = itemClickListener;
        }

        @Override
        public void onClick(View view) {
            clickListener.onClick(view, getLayoutPosition());
        }
    }
}