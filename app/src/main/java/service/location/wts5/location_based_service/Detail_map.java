package service.location.wts5.location_based_service;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.squareup.picasso.Picasso;

import java.io.IOException;

public class Detail_map  extends AppCompatActivity implements OnMapReadyCallback {

    TextView txtTitle;
    TextView txtAddress;
    TextView txtType;
    WebView wbPhotoes;
    private GoogleMap mMap;
    int pos;
    ImageView imgIcon;
    private static final int PLACE_PICKER_REQUEST = 1;

    private CardView cvMoreInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_map);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);



        init();

        toolbar.setTitle(Search_fragment.jpojo.getResults().get(pos).getName());
        toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);

        Log.e("Pos...",pos+"");
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        txtTitle.setText(Search_fragment.jpojo.getResults().get(pos).getName());
        txtTitle.setVisibility(View.GONE);
        txtAddress.setText(Search_fragment.jpojo.getResults().get(pos).getFormatted_address());
        String type = "";
        for (int i = 0;i<Search_fragment.jpojo.getResults().get(pos).getTypes().size();i++)
        {
            type = type+Search_fragment.jpojo.getResults().get(pos).getTypes().get(i)+" ,";
        }
        txtType.setText(type.substring(0,type.length()-1));

        try {
            Picasso.with(getApplicationContext()).load(Search_fragment.jpojo.getResults().get(pos).getIcon())
                    .placeholder(R.drawable.image_placeholder)
                    .error(R.drawable.image_placeholder).into(imgIcon);
        } catch (Exception e) {
            Picasso.with(getApplicationContext()).load(R.drawable.image_placeholder)
                    .into(imgIcon);
        }

        cvMoreInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    LatLng a = new LatLng(Search_fragment.jpojo.getResults().get(pos).getGeometry().getLocation().getLat(),Search_fragment.jpojo.getResults().get(pos).getGeometry().getLocation().getLng());
                    LatLng b = new LatLng(Search_fragment.jpojo.getResults().get(pos).getGeometry().getLocation().getLat(),Search_fragment.jpojo.getResults().get(pos).getGeometry().getLocation().getLng());
                    LatLngBounds latLngBounds = new LatLngBounds(a,b);
                    PlacePicker.IntentBuilder intentBuilder =new PlacePicker.IntentBuilder();
                    intentBuilder.setLatLngBounds(latLngBounds);
                    Intent intent = intentBuilder.build(Detail_map.this);
                    startActivityForResult(intent, PLACE_PICKER_REQUEST);

                } catch (GooglePlayServicesRepairableException
                        | GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });

      /*  wbPhotoes.getSettings().setJavaScriptEnabled(true);
        wbPhotoes.loadUrl("https://maps.google.com/maps/contrib/118124872182584855225/photos\\");
     */   //wbPhotoes.loadDataWithBaseURL("", "https://maps.google.com/maps/contrib/118124872182584855225/photos\\" , "text/html",  "UTF-8", "");
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        // mMap.getUiSettings().setMapToolbarEnabled(true);
        mMap.getUiSettings().setRotateGesturesEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        mMap.getUiSettings().setAllGesturesEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setIndoorLevelPickerEnabled(true);
        addMarker();
    }
    public void addMarker() {
        mMap.clear();

        Marker marker = mMap.addMarker(new MarkerOptions()
                .position(new LatLng(Search_fragment.jpojo.getResults().get(pos).getGeometry().getLocation().getLat(), Search_fragment.jpojo.getResults().get(pos).getGeometry().getLocation().getLng()))
                .title(Search_fragment.jpojo.getResults().get(pos).getName()));
        marker.showInfoWindow();
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Search_fragment.jpojo.getResults().get(pos).getGeometry().getLocation().getLat(), Search_fragment.jpojo.getResults().get(pos).getGeometry().getLocation().getLng()), 17.0f));
        Log.e("Lat",Search_fragment.jpojo.getResults().get(pos).getGeometry().getLocation().getLat()+"...");
        Log.e("LNG",Search_fragment.jpojo.getResults().get(pos).getGeometry().getLocation().getLng()+"...");

    }

    public void init()
    {
         txtTitle = (TextView)findViewById(R.id.txt_title);
         txtAddress = (TextView)findViewById(R.id.txt_address);
         txtType = (TextView)findViewById(R.id.txt_type);
         wbPhotoes = (WebView) findViewById(R.id.wv_photoes);
         imgIcon = (ImageView)findViewById(R.id.img_icon);
         cvMoreInfo = (CardView)findViewById(R.id.cv_more_info);
         pos = getIntent().getExtras().getInt("POS");
    }
}
