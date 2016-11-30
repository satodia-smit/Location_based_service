package service.location.wts5.location_based_service;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.wang.avi.AVLoadingIndicatorView;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import Model.M_distance_api;
import Utils.Datas;
import map.helper.MapWrapperLayout;
import map.helper.OnInfoWindowElemTouchListener;
import xyz.sahildave.widget.SearchViewLayout;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Location mLocation;
    private LocationRequest mLocationRequest;
    private String mLastUpdateTime;
    private SlidingUpPanelLayout slidingLayout;
    private TextView txtAddress;
    private TextView txtAltitude;
    private Geocoder geocoder;
    private List<Address> addresses;
    public static String search_string = "";
    AppBarLayout appBarLayout;
    ImageView t_pickplace;
    ImageView t_direction;
    private static final int PLACE_PICKER_REQUEST = 1;
    int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    public static LatLng[] latlngOrigin = new LatLng[0];
    public static LatLng[] latlngDesti = new LatLng[0];

    private ViewGroup infoWindow;
    private TextView infoTitle;
    private TextView infoSnippet;
    private Button infoButton;
    private Button infobutton2;
    private OnInfoWindowElemTouchListener infoButtonListener;
    private OnInfoWindowElemTouchListener infoButtonListener2;
    ArrayList<Marker> markerArrayList;

    MapWrapperLayout mapWrapperLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Map example");
        setSupportActionBar(toolbar);

        final SearchViewLayout searchViewLayout = (SearchViewLayout) findViewById(R.id.search_view_container);
        searchViewLayout.setExpandedContentFragment(this, new Search_fragment());
        appBarLayout = (AppBarLayout) findViewById(R.id.appBar);

        t_pickplace = (ImageView) findViewById(R.id.t_pickplace);
        t_direction = (ImageView) findViewById(R.id.t_direction);
        markerArrayList = new ArrayList<>();


        searchViewLayout.handleToolbarAnimation(toolbar);
        searchViewLayout.setExpandedHint("Address...");
        searchViewLayout.setCollapsedHint("Please enter the address to locate");
        //searchViewLayout.setHint("Please enter the address to locate");
        searchViewLayout.setSearchBoxListener(new SearchViewLayout.SearchBoxListener() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
             /*   if (!TextUtils.isEmpty(s.toString())) {
                    search_string = s.toString();

                    Search_fragment.getText();
                } else {
                    Toast.makeText(getApplicationContext(), "Please enter the search text", Toast.LENGTH_LONG).show();
                }
*/
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        searchViewLayout.setSearchListener(new SearchViewLayout.SearchListener() {
            @Override
            public void onFinished(String searchKeyword) {

                if (!TextUtils.isEmpty(searchKeyword)) {
                    Log.e("TEXT", searchKeyword);
                    search_string = searchKeyword;

                    Search_fragment.getText();
                } else {
                    Toast.makeText(getApplicationContext(), "Please enter the search text", Toast.LENGTH_LONG).show();
                }
            }
        });

        t_pickplace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    PlacePicker.IntentBuilder intentBuilder = new PlacePicker.IntentBuilder();

                    Intent intent = intentBuilder.build(MapsActivity.this);
                    startActivityForResult(intent, PLACE_PICKER_REQUEST);

                } catch (GooglePlayServicesRepairableException
                        | GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });


        t_direction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MapsActivity.this);
                LayoutInflater inflater = MapsActivity.this.getLayoutInflater();
                final View dialog = inflater.inflate(R.layout.dialogue_location, null);
                dialogBuilder.setView(dialog);

                final EditText et_from = (EditText) dialog.findViewById(R.id.et_from);
                final EditText et_to = (EditText) dialog.findViewById(R.id.et_to);
                final TextView txt_distance = (TextView) dialog.findViewById(R.id.txt_distance);
                final TextView txt_time = (TextView) dialog.findViewById(R.id.txt_time);
                final TextView txt_error = (TextView) dialog.findViewById(R.id.txt_error);
                final CardView card_view = (CardView) dialog.findViewById(R.id.card_view);
               // final GoogleMap  googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map_dialogue)).getMap();



                final Button btn_large_map = (Button)dialog.findViewById(R.id.btn_show_map);
                final AVLoadingIndicatorView avLoadingIndicatorView = (AVLoadingIndicatorView) dialog.findViewById(R.id.avloadingIndicatorView);

                latlngOrigin = new LatLng[1];
                latlngDesti = new LatLng[1];
                btn_large_map.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        startActivity(new Intent(getApplicationContext(),View_direction_map.class));
                       /* String url = "http://maps.google.com/maps?saddr="
                                +latlngOrigin[0].latitude+","+latlngOrigin[0].longitude
                                +"&daddr="+latlngDesti[0].latitude+","+latlngDesti[0].longitude
                                +"&mode=driving";
                        Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                                Uri.parse(url));
                        startActivity(intent);*/
                    }
                });

                RelativeLayout ly_search = (RelativeLayout) dialog.findViewById(R.id.ly_search);
                ly_search.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        card_view.setVisibility(View.GONE);
                        btn_large_map.setVisibility(View.GONE);
                        String value_et_from = et_from.getText().toString();
                        String value_et_to = et_to.getText().toString();
                        if (TextUtils.isEmpty(value_et_from)) {

                            avLoadingIndicatorView.setVisibility(View.GONE);
                            txt_error.setVisibility(View.VISIBLE);
                            txt_error.setText("Please enter the source location");

                        } else if (TextUtils.isEmpty(value_et_to)) {

                            avLoadingIndicatorView.setVisibility(View.GONE);
                            txt_error.setVisibility(View.VISIBLE);
                            txt_error.setText("Please enter the destination location");

                        } else {

                            txt_error.setVisibility(View.GONE);
                            avLoadingIndicatorView.setVisibility(View.VISIBLE);
                            latlngOrigin[0] = getLocationFromAddress(value_et_from);
                            String origin = "";
                            if (latlngOrigin[0] != null) {

                                origin = latlngOrigin[0].latitude + "," + latlngOrigin[0].longitude;
                            }

                            latlngDesti[0] = getLocationFromAddress(value_et_to);

                            String desti = "";
                            if (latlngDesti[0] != null) {

                                desti = latlngDesti[0].latitude + "," + latlngDesti[0].longitude;
                            }

                            Log.e("Original", origin);
                            Log.e("Destination", desti);
                            RequestQueue queue = Volley.newRequestQueue(getApplicationContext());

                            Uri builtUri = Uri.parse(Datas.URL_directionmatrix)
                                    .buildUpon()
                                    .appendQueryParameter("key", getString(R.string.google_api_key))
                                    .appendQueryParameter("units", "si")
                                    .appendQueryParameter("origins", origin)
                                    .appendQueryParameter("destinations", desti)
                                    .build();


                            URL url = null;
                            try {
                                url = new URL(builtUri.toString());
                            } catch (MalformedURLException e) {
                                Log.e("malformed", e.toString());
                                e.printStackTrace();
                            }
                            Log.e("URL", url.toString());

                            StringRequest stringRequest = new StringRequest(Request.Method.GET, url.toString(),
                                    new Response.Listener<String>() {
                                        @Override
                                        public void onResponse(String response) {

                                            avLoadingIndicatorView.setVisibility(View.GONE);
                                            try {

                                                Log.e("RES_distance", response);
                                                M_distance_api jpojo = new Gson().fromJson(response, M_distance_api.class);

                                                et_from.setText(jpojo.getOrigin_addresses().get(0));
                                                et_to.setText(jpojo.getDestination_addresses().get(0));
                                                btn_large_map.setVisibility(View.VISIBLE);
                                                if (jpojo.getRows().get(0).getElements().get(0).getStatus().equals("OK")) {
                                                    card_view.setVisibility(View.VISIBLE);
                                                    txt_distance.setText("Distance: " + jpojo.getRows().get(0).getElements().get(0).getDistance().getText());
                                                    txt_time.setText("Approx time: " + jpojo.getRows().get(0).getElements().get(0).getDuration().getText());
                                                } else {
                                                    txt_error.setVisibility(View.VISIBLE);
                                                    txt_error.setText("Unable to get the data!!");
                                                    card_view.setVisibility(View.GONE);
                                                }
                                            } catch (Exception e) {
                                                Log.e("Exception", e.toString());
                                                txt_error.setVisibility(View.VISIBLE);
                                                txt_error.setText("Unable to find exact location");
                                                e.printStackTrace();
                                            }



                                        }
                                    }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    btn_large_map.setVisibility(View.GONE);
                                    Log.e("Error", error.toString());
                                }
                            });
                            queue.add(stringRequest);
                        }

                    }
                });

                AlertDialog b = dialogBuilder.create();
                b.show();
            }
        });


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        slidingLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        txtAddress = (TextView) findViewById(R.id.txt_address);
        txtAltitude = (TextView) findViewById(R.id.txt_altitude);
        mapFragment.getMapAsync(this);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();


    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        mMap.getUiSettings().setCompassEnabled(true);
        // mMap.getUiSettings().setMapToolbarEnabled(true);
        mMap.getUiSettings().setRotateGesturesEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setAllGesturesEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setIndoorLevelPickerEnabled(true);
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {

                Log.e("Lat", latLng.latitude + "");
                Log.e("Lng", latLng.longitude + "");
                Location targetLocation = new Location("");//provider name is unecessary
                targetLocation.setLatitude(latLng.latitude);//your coords of course
                targetLocation.setLongitude(latLng.longitude);
                mLocation = targetLocation;
                addMarker(targetLocation);
            }
        });

        mapWrapperLayout  = (MapWrapperLayout)findViewById(R.id.map_relative_layout);
        mapWrapperLayout.init(mMap, getPixelsFromDp(this, 39 + 20));

        this.infoWindow = (ViewGroup)getLayoutInflater().inflate(R.layout.custom_infowindow, null);
        this.infoTitle = (TextView)infoWindow.findViewById(R.id.title);
        this.infoSnippet = (TextView)infoWindow.findViewById(R.id.snippet);
        this.infoButton = (Button)infoWindow.findViewById(R.id.button);
        this.infobutton2 = (Button)infoWindow.findViewById(R.id.button2);

        this.infoButtonListener = new OnInfoWindowElemTouchListener(infoButton,
                getResources().getDrawable(R.drawable.btn_normal),
                getResources().getDrawable(R.drawable.btn_precessed))
        {
            @Override
            protected void onClickConfirmed(View v, Marker marker) {
               Log.e("Marker id",marker.getId()+"...");

                // Here we can perform some action triggered after clicking the button
                Toast.makeText(MapsActivity.this, marker.getTitle() + "'s button clicked!", Toast.LENGTH_SHORT).show();
            }
        };
        this.infoButton.setOnTouchListener(infoButtonListener);

        this.infoButtonListener2 = new OnInfoWindowElemTouchListener(infobutton2,
                getResources().getDrawable(R.drawable.btn_normal),
                getResources().getDrawable(R.drawable.btn_precessed))
        {
            @Override
            protected void onClickConfirmed(View v, Marker marker) {
                // Here we can perform some action triggered after clicking the button
                Toast.makeText(MapsActivity.this,marker.getTitle() + "  button 2", Toast.LENGTH_SHORT).show();
            }
        };
        this.infobutton2.setOnTouchListener(infoButtonListener2);


        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                // Setting up the infoWindow with current's marker info
                infoTitle.setText(marker.getTitle());
                infoSnippet.setText(marker.getSnippet());
                infoButtonListener.setMarker(marker);
                infoButtonListener2.setMarker(marker);


                // We must call this to set the current marker and infoWindow references
                // to the MapWrapperLayout
                mapWrapperLayout.setMarkerWithInfoWindow(marker, infoWindow);
                return infoWindow;
            }
        });

        /*final RouteDrawer routeDrawer = new RouteDrawer.RouteDrawerBuilder(mMap)
                .withColor(Color.BLUE)
                .withWidth(8)
                .withAlpha(0.5f)
                .withMarkerIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                .build();
        RouteRest routeRest = new RouteRest();

        routeRest.getJsonDirections(new LatLng(22.2865937, 70.772081), new LatLng(22.279798, 70.763130), TravelMode.DRIVING)
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
*/

    }

    @Override
    public void onConnected(Bundle bundle) {

        //This below code has been used to get one time location namely last known location
        mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLocation != null) {
            Log.e("LAT", String.valueOf(mLocation.getLatitude()));
            Log.e("LNG", String.valueOf(mLocation.getLongitude()));
            addMarker(mLocation);
        } else {
            Toast.makeText(this, "Location not Detected", Toast.LENGTH_SHORT).show();
        }


        //This below code has been used to get periodic location

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(3000);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);


    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e("LOC SUSPENDED", "Connection Suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e("LOC FAILED", "Connection failed. Error: " + connectionResult.getErrorCode());
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    public void addMarker(Location location) {
        //mMap.clear();

        String[] addressarray = new String[0];
        Marker marker = null;
        try {
            addressarray = getAddressFromLatlng(mLocation);
            String s = addressarray[0].concat("\n").concat(addressarray[1]).concat("\n").concat(addressarray[2]).concat("\n").concat(addressarray[3]).concat("\n").concat(addressarray[4]).concat("\n").concat(addressarray[5]);
            txtAddress.setText(s);
            marker = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(location.getLatitude(), location.getLongitude()))
                    .title(addressarray[0]));
        } catch (Exception e) {
            try {
                txtAddress.setText(" Partial address: " + addressarray[0] + "\n" + " Unable to retrieve the address \n CAUSE: \n location may not be popular \n low internet speed");
                 marker = mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(location.getLatitude(), location.getLongitude()))
                        .title(addressarray[0]));
            } catch (Exception e1) {
                txtAddress.setText(" Unable to retrieve full the address \n CAUSE: \n location may not be popular \n low internet speed");
                marker = mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(location.getLatitude(), location.getLongitude()))
                );
                e1.printStackTrace();
            }
            e.printStackTrace();
        }

        marker.showInfoWindow();
        markerArrayList.add(marker);
        setViewport(markerArrayList);

        //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 17.0f));

        txtAltitude.setText(String.valueOf(location.getAltitude()));
    }

    @Override
    public void onLocationChanged(Location location) {
        //This is for getting continous update
       /* mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        mLocation = location;

        if (mLocation != null) {
            Log.e("LAT...UPDATING",String.valueOf(mLocation.getLatitude()));
            Log.e("LNG...UPDATING",String.valueOf(mLocation.getLongitude()));
            Log.e("TIME..UPDATING",mLastUpdateTime);

            addMarker(mLocation);
        } else {
            Toast.makeText(this, "Location not Detected", Toast.LENGTH_SHORT).show();
        }*/

    }

    public String[] getAddressFromLatlng(Location location) throws IOException {
        geocoder = new Geocoder(this, Locale.getDefault());
        addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5

        String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
        String city = addresses.get(0).getLocality();
        String state = addresses.get(0).getAdminArea();
        String country = addresses.get(0).getCountryName();
        String postalCode = addresses.get(0).getPostalCode();
        String knownName = addresses.get(0).getFeatureName();

        return new String[]{address, city, state, country, postalCode, knownName};
    }

    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode, Intent data) {

        if (requestCode == PLACE_PICKER_REQUEST
                && resultCode == Activity.RESULT_OK) {

            final Place place = PlacePicker.getPlace(data, this);
            final CharSequence name = place.getName();
            final CharSequence address = place.getAddress();
           /* String attributions = (String) place.getAttributions();
            *//*if (attributions == null) {
                attributions = "";
            }*/

            Log.e("Name..", name.toString());
           /* mName.setText(name);
            mAddress.setText(address);
            mAttributions.setText(Html.fromHtml(attributions));*/

        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    public LatLng getLocationFromAddress(String strAddress) {

        Geocoder coder = new Geocoder(this);
        try {
            ArrayList<Address> adresses = (ArrayList<Address>) coder.getFromLocationName(strAddress, 50);
            for (Address add : adresses) {
                double longitude = add.getLongitude();
                double latitude = add.getLatitude();
                return new LatLng(latitude, longitude);

            }
        } catch (IOException e) {
            Log.e("IO EXE IN GEOCODING", e.toString());
            e.printStackTrace();
        }
        return null;

    }

    public static int getPixelsFromDp(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int)(dp * scale + 0.5f);
    }

    public void setViewport(ArrayList<Marker> markers){
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Marker marker : markers) {
            builder.include(marker.getPosition());
        }
        LatLngBounds bounds = builder.build();
        int padding = 25; // offset from edges of the map in pixels
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        mMap.animateCamera(cu);

    }
}
