package com.example.hlt04.pyquiz;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.hlt04.pyquiz.helper.AlertDialogManager;
import com.example.hlt04.pyquiz.helper.ConnectionDetector;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class TrackListActivity extends ListActivity {
    // Connection detector
    ConnectionDetector cd;

    // Alert dialog manager
    AlertDialogManager alert = new AlertDialogManager();

    // Progress Dialog
    private ProgressDialog pDialog;

    // Creating JSON Parser object
    //JSONParser jsonParser = new JSONParser();

    ArrayList<HashMap<String, String>> tracksList;

    // tracks JSONArray
    JSONArray albums = null;
    JSONObject state = null;

    // Album id
    String album_id, album_name;

    // tracks JSON url
    // id - should be posted as GET params to get track list (ex: id = 5)
    //private static final String URL_ALBUMS = "http://api.androidhive.info/songs/album_tracks.php";

    // ALL JSON node names
    private static final String TAG_SONGS = "songs";
    private static final String TAG_ID = "id";
    private static final String TAG_NAME = "name";
    private static final String TAG_ALBUM = "album";
    private static final String TAG_DURATION = "duration";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracks);

        cd = new ConnectionDetector(getApplicationContext());

        // Check if Internet present
        if (!cd.isConnectingToInternet()) {
            // Internet Connection is not present
            alert.showAlertDialog(TrackListActivity.this, "Internet Connection Error",
                    "Please connect to working Internet connection", false);
            // stop executing code by return
            return;
        }

        // Get album id
        Intent i = getIntent();
        album_id = i.getStringExtra("album_id");
        try {
            albums = new JSONArray(i.getStringExtra("albums"));
            state = new JSONObject(i.getStringExtra("state"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // Hashmap for ListView
        tracksList = new ArrayList<HashMap<String, String>>();

        // Loading tracks in Background Thread
        new LoadTracks().execute();

        // get listview
        ListView lv = getListView();

        /**
         * Listview on item click listener
         * SingleTrackActivity will be lauched by passing album id, song id
         * */
        lv.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int arg2,
                                    long arg3) {
                // On selecting single track get song information
                Intent i = new Intent(getApplicationContext(), SingleTrackActivity.class);

                // to get song information
                // both album id and song is needed
                String album_id = ((TextView) view.findViewById(R.id.album_id)).getText().toString();
                String song_id = ((TextView) view.findViewById(R.id.song_id)).getText().toString();
                String track_no = ((TextView) view.findViewById(R.id.track_no)).getText().toString();
                //Toast.makeText(getApplicationContext(), "Album Id: " + album_id  + ", Song Id: " + song_id, Toast.LENGTH_SHORT).show();

                i.putExtra("album_id", album_id);
                i.putExtra("song_id", song_id);
                i.putExtra("track_no", track_no);
                startActivity(i);
            }
        });

    }

    /**
     * Background Async Task to Load all tracks under one album
     * */
    class LoadTracks extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(TrackListActivity.this);
            pDialog.setMessage("Loading Questions ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        /**
         * getting tracks json and parsing
         * */
        protected String doInBackground(String... args) {

            try {
                if (albums != null) {
                    album_name = albums.getJSONObject(Integer.valueOf(album_id)).getString("id");
                    albums = albums.getJSONObject(Integer.valueOf(album_id)).getJSONObject("activities").getJSONArray("qp");
                    if (albums != null) {
                        // looping through All songs
                        for (int i = 0; i < albums.length(); i++) {
                            JSONObject c = albums.getJSONObject(i);

                            // Storing each json item in variable
                            String song_url = c.getString("url");
                            String song_id = c.getString("id");
                            // track no - increment i value
                            String track_no = String.valueOf(i + 1);
                            String name = c.getString("name");
                            String duration = state.getJSONObject("activities").getJSONObject(album_name).getJSONObject("qp").getJSONObject(song_id).getJSONObject("values").getString("p");

                            // creating new HashMap
                            HashMap<String, String> map = new HashMap<String, String>();

                            // adding each child node to HashMap key => value
                            map.put("album_id", song_url);
                            map.put(TAG_ID, song_id);
                            map.put("track_no", track_no + "");
                            map.put(TAG_NAME, name);
                            map.put(TAG_DURATION, duration);

                            // adding HashList to ArrayList
                            tracksList.add(map);
                        }
                    } else {
                        Log.d("Albums: ", "null");
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after getting all tracks
            pDialog.dismiss();
            // updating UI from Background Thread
            runOnUiThread(new Runnable() {
                public void run() {
                    /**
                     * Updating parsed JSON data into ListView
                     * */
                    ListAdapter adapter = new SimpleAdapter(
                            TrackListActivity.this, tracksList,
                            R.layout.list_item_tracks, new String[] { "album_id", TAG_ID, "track_no",
                            TAG_NAME, TAG_DURATION }, new int[] {
                            R.id.album_id, R.id.song_id, R.id.track_no, R.id.album_name, R.id.song_duration });
                    // updating listview
                    setListAdapter(adapter);

                    // Change Activity Title with Album name
                    setTitle(album_name);
                }
            });

        }

    }
}