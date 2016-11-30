package service.location.wts5.location_based_service;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import Model.M_place_api;
import Utils.Datas;
import xyz.sahildave.widget.SearchViewLayout;

/**
 * Created by wts5 on 12/4/16.
 */
public class Search_fragment extends Fragment {

    public static RequestQueue queue;
    public static Activity a;
    View view;
   public static RecyclerView recyclerView;
    public static SampleAdapter sampleAdapter;

    LinearLayoutManager linearLayoutManager;
    public static M_place_api jpojo;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.search_fragment, container, false);
        queue = Volley.newRequestQueue(getActivity());
        a = getActivity();
        recyclerView = (RecyclerView) view.findViewById(R.id.lv_sliding_tab);
        sampleAdapter = new SampleAdapter(getActivity());
        linearLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        sampleAdapter.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, final int arg2,
                                    long arg3) {



                Intent i = new Intent(getActivity().getApplicationContext(), Detail_map.class);
                i.putExtra("POS",arg2);
                Log.e("Args", arg2+"...");
                startActivity(i);

            }
        });


        return view;
    }


    public static void getText() {
        Log.e("SEARCH ..", MapsActivity.search_string);


        Uri builtUri = Uri.parse(Datas.URL_googleplace)
                .buildUpon()
                .appendQueryParameter("key", a.getString(R.string.google_api_key))
                .appendQueryParameter("query", MapsActivity.search_string)
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

                        jpojo = new Gson().fromJson(response, M_place_api.class);
                        Log.e("RESULT SIZE",jpojo.getResults().size()+"...");

                        for(int i=0;i<jpojo.getResults().size();i++)
                        {
                            Log.e("NAME",jpojo.getResults().get(i).getName());
                        }
                        recyclerView.setAdapter(sampleAdapter);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Error", error.toString());
            }
        });
        queue.add(stringRequest);
    }


    public class SampleAdapter extends RecyclerView.Adapter<SampleAdapter.ViewHolder> {

        Context context;
        ArrayList<String> arrayList = new ArrayList<String>();
        public AdapterView.OnItemClickListener onItemClickListener;
        public AdapterView.OnItemLongClickListener itemLongClickListener;

        public SampleAdapter(Context context) {
            this.context = context;
        }

        public void setOnItemClickListener(AdapterView.OnItemClickListener listener) {
            onItemClickListener = listener;
        }

        public void setOnItemLongClickListener(AdapterView.OnItemLongClickListener listener) {
            itemLongClickListener = listener;
        }

        public class ViewHolder extends RecyclerView.ViewHolder implements
                View.OnClickListener {

            TextView txtTitle;
            TextView txtAddress;
            TextView txtType;
            ImageView imgLogo;

            SampleAdapter sampleAdapter;

            public ViewHolder(View itemView, SampleAdapter sampleAdapter) {
                super(itemView);

                txtTitle = (TextView) itemView.findViewById(R.id.txt_title);
                txtAddress = (TextView) itemView.findViewById(R.id.txt_address);
                txtType = (TextView) itemView.findViewById(R.id.txt_type);
                imgLogo = (ImageView) itemView.findViewById(R.id.img_icon);

                this.sampleAdapter = sampleAdapter;

                itemView.setOnClickListener(this);

            }

            @Override
            public void onClick(View v) {

                sampleAdapter.setItemClick(this);

            }

        }

        public void setItemClick(ViewHolder viewHolder) {

            onItemClickListener.onItemClick(null, viewHolder.itemView,
                    viewHolder.getAdapterPosition(), viewHolder.getItemId());

        }

        @Override
        public int getItemCount() {


                return jpojo.getResults().size();

        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, int position) {

            try {
                viewHolder.txtTitle.setText(jpojo.getResults().get(position).getName());

            }
            catch (Exception e){

            }

            try {
                viewHolder.txtAddress.setText(jpojo.getResults().get(position).getFormatted_address());

            }
            catch (Exception e){

            }

            try {
                String type = "";
                for (int i = 0;i<jpojo.getResults().get(position).getTypes().size();i++)
                {
                    type = type+jpojo.getResults().get(position).getTypes().get(i)+" ,";
                }
                viewHolder.txtType.setText(type.substring(0,type.length()-1));


            }
            catch (Exception e){

            }


            try {
               Picasso.with(context).load(jpojo.getResults().get(position).getIcon())
                        .placeholder(R.drawable.image_placeholder)
                        .error(R.drawable.image_placeholder).into(viewHolder.imgLogo);
            } catch (Exception e) {
                 Picasso.with(context).load(R.drawable.image_placeholder)
                        .into(viewHolder.imgLogo);
            }



        }

        @SuppressLint("InflateParams")
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(
                    R.layout.custom_row_location_search, viewGroup, false);

            return new ViewHolder(view, this);
        }
    }


}
