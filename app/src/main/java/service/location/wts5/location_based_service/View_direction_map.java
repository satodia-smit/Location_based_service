package service.location.wts5.location_based_service;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.github.polok.routedrawer.RouteDrawer;
import com.github.polok.routedrawer.RouteRest;
import com.github.polok.routedrawer.model.Routes;
import com.github.polok.routedrawer.model.TravelMode;
import com.github.polok.routedrawer.parser.RouteJsonParser;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;

public class View_direction_map extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_direction_map);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        final Button btn_large_map = (Button)findViewById(R.id.btn_large_map);
        btn_large_map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                         String url = "http://maps.google.com/maps?saddr="
                                +MapsActivity.latlngOrigin[0].latitude+","+MapsActivity.latlngOrigin[0].longitude
                                +"&daddr="+MapsActivity.latlngDesti[0].latitude+","+MapsActivity.latlngDesti[0].longitude
                                +"&mode=driving";
                        Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                                Uri.parse(url));
                        startActivity(intent);
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(MapsActivity.latlngOrigin[0], 17.0f));

        final RouteDrawer routeDrawer = new RouteDrawer.RouteDrawerBuilder(googleMap)
                .withColor(Color.BLUE)
                .withWidth(8)
                .withAlpha(0.5f)
                .withMarkerIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                .build();
        RouteRest routeRest = new RouteRest();

        routeRest.getJsonDirections(MapsActivity.latlngOrigin[0], MapsActivity.latlngDesti[0], TravelMode.DRIVING)
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Func1<String, Routes>() {
                    @Override
                    public Routes call(String s) {
                        return new RouteJsonParser<Routes>().parse(s, Routes.class);
                    }
                })
                .subscribe(new Action1<Routes>() {
                    @Override
                    public void call(Routes r) {
                        routeDrawer.drawPath(r);
                    }
                });
    }
}
