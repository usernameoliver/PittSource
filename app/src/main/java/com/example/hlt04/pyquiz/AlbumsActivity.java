package com.example.hlt04.pyquiz;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
import android.view.ViewGroup;
import java.util.List;
import android.content.Context;
import com.example.hlt04.pyquiz.helper.Document;

import com.example.hlt04.pyquiz.helper.AlertDialogManager;
import com.example.hlt04.pyquiz.helper.ConnectionDetector;

public class AlbumsActivity extends ListActivity {
    private ArrayList<String> gradeString = new ArrayList<String>();

    // Connection detector
    ConnectionDetector cd;

    // Alert dialog manager
    AlertDialogManager alert = new AlertDialogManager();

    // Progress Dialog
    private ProgressDialog pDialog;

    // Creating JSON Parser object
    //JSONParser jsonParser = new JSONParser();

    public ArrayList<HashMap<String, String>> albumsList;
    public ArrayList<HashMap<String, String>> albumsListNew = new ArrayList<HashMap<String, String>>();


    // albums JSONArray
    JSONArray albums = null;
    JSONObject state = null;

    String userId = "";//"adl01";
    private String Query = "";

    // albums JSON url
    private static String URL_ALBUMS = "";///*"http://api.androidhive.info/songs/albums.php";*/"http://adapt2.sis.pitt.edu/aggregate/GetContentLevels?usr=adl01&grp=ADL&sid=generate_a_session_id&cid=23&mod=all&models=0";

    // ALL JSON node names
    private static final String TAG_ID = "id";
    private static final String TAG_NAME = "name";
    private static final String TAG_SONGS_COUNT = "songs_count";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_albums);

        Intent i = getIntent();
        Query = i.getStringExtra("userName1");
        userId = "adl01";
        URL_ALBUMS = "http://adapt2.sis.pitt.edu/aggregate/GetContentLevels?usr=" + userId + "&grp=ADL&sid=generate_a_session_id&cid=23&mod=all&models=0";

        cd = new ConnectionDetector(getApplicationContext());

        // Check for internet connection
        if (!cd.isConnectingToInternet()) {
            // Internet Connection is not present
            alert.showAlertDialog(AlbumsActivity.this, "Internet Connection Error",
                    "Please connect to working Internet connection", false);
            // stop executing code by return
            return;
        }

        // Hashmap for ListView
        albumsList = new ArrayList<HashMap<String, String>>();

        // Loading Albums JSON in Background Thread
        new LoadAlbums().execute();

        // get listview
        ListView lv = getListView();

        /**
         * Listview item click listener
         * TrackListActivity will be lauched by passing album id
         * */
        lv.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int arg2,
                                    long arg3) {
                // on selecting a single album
                // TrackListActivity will be launched to show tracks inside the album
                Intent i = new Intent(getApplicationContext(), SingleTrackActivity.class);

                // send album id to tracklist activity to get list of songs under that album
                String album_id = ((TextView) view.findViewById(R.id.album_id)).getText().toString();
                i.putExtra("album_id", album_id);
                startActivity(i);
            }
        });
    }

    /**
     * Background Async Task to Load all Albums by making http request
     * */
    class LoadAlbums extends AsyncTask<String, String, String> {
        private HashMap<String,ArrayList<String>> dictionaryP = new HashMap<String,ArrayList<String>>();
        private HashMap<String,ArrayList<Integer>> dictionaryO = new HashMap<String,ArrayList<Integer>>();
        private HashMap<String,String> idNameDictionary = new HashMap<>();
        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(AlbumsActivity.this);
            pDialog.setMessage("I am looking for every positions you may like...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        /**
         * getting Albums JSON
         * */
        protected String doInBackground(String... args) {
            String json = readFileAsString();

            Log.d("Albums JSON lala ", "> " + json.length());

            try {
                albums = new JSONArray(json);
                if (albums != null) {
                    // looping through All albums
                    Log.d("within json loop", "in loop lala");
                    for (int i = 0; i < albums.length(); i++) {
                        JSONObject c = albums.getJSONObject(i);

                        // Storing each json item values in variable
                        String id = c.getString("url");
                        String name = c.getString("title");
                        String songs_count = c.getString("datePosted");
                        String text = c.getString("text");
                        int len = id.length();
                        index(id.substring(len-6,len),text);

                        // creating new HashMap
                        HashMap<String, String> map = new HashMap<String, String>();

                        // adding each child node to HashMap key => value
                        map.put(TAG_ID, id);
                        map.put(TAG_NAME, name);
                        map.put(TAG_SONGS_COUNT, songs_count);
                        idNameDictionary.put(id,name);
                        Log.d("String", id);
                        Log.d("String", name);
                        Log.d("String",songs_count);
                        Log.d("String",id.substring(len-6,len));
                        gradeString.add(songs_count);
                        // adding HashList to ArrayList
                        albumsList.add(map);
                    }
                }else{
                    Log.d("Albums: ", "null");
                }

            } catch (JSONException e) {
                Log.d("parse lala", e.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }

        String[] queryToken = Query.split(" ");
            for(int i = 0; i < queryToken.length; i++){
                ArrayList<String> docList = dictionaryP.get(queryToken[i]);
                ArrayList<Integer> docListOccur = dictionaryO.get(queryToken[i]);
                for (int j = 0; j < docList.size(); j++){
                    String urlNew = "https://www.pittsource.com/postings/" + docList.get(j);
                    int occur = docListOccur.get(j);
                    String nameNew = idNameDictionary.get(urlNew);
                    HashMap<String, String> mapNew = new HashMap<String, String>();
                    mapNew.put(TAG_ID, urlNew);
                    mapNew.put(TAG_NAME, nameNew);
                    mapNew.put(TAG_SONGS_COUNT, occur + "");
                    albumsListNew.add(mapNew);

                }


                // adding each child node to HashMap key => value


            }



            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after getting all albums
            pDialog.dismiss();
            // updating UI from Background Thread
            runOnUiThread(new Runnable() {
                public void run() {
                    /**
                     * Updating parsed JSON data into ListView
                     * */
                    ListAdapter adapter = new SimpleAdapter(
                            AlbumsActivity.this, albumsListNew,
                            R.layout.list_item_albums, new String[]{TAG_ID,
                            TAG_NAME, TAG_SONGS_COUNT}, new int[]{
                            R.id.album_id, R.id.album_name, R.id.songs_count});
                    //Log.d("String",R.id.songs_count + "*********************************************");
                    //Log.d("String",R.id.songs_count + "*********************************************");
                    //TextView newview = (TextView) findViewById(R.id.songs_count);
                    //String grade = newview.getText().toString();
                    //Log.d("String", gradeString + "*********************************************");
                    // updating listview
                    setListAdapter(adapter);
                }
            });

        }
        //method of indexing
        public void index(String docno, String content) throws  NullPointerException,IOException {

            String[] tokens = content.split("\\s+");


            //System.out.println(tokens.length);
            //System.out.println(tokens[0]);
            //System.out.println(content);
            for(int i = 0; i < tokens.length; i++){

                String tempKey = tokens[i];
                //if the dictionary has the term, then add the docno to the List aready existing.
                if(dictionaryP.containsKey(tempKey))
                {
                    ArrayList<String> tempListPost = dictionaryP.get(tempKey);
                    ArrayList<Integer> tempListOccur = dictionaryO.get(tempKey);
                    String currentDocno = tempListPost.get(tempListPost.size() - 1);
                    int currentOccur = tempListOccur.get(tempListOccur.size() - 1);
                    if(currentDocno.equals(docno)){//if this term occur for the first time in this doc but exsits in other posts,
                        currentOccur++;
                        tempListOccur.remove(tempListOccur.size() - 1);
                        tempListOccur.add(currentOccur);
                        dictionaryO.put(tempKey, tempListOccur);

                    }
                    else{
                        tempListOccur.add(1);
                        dictionaryO.put(tempKey, tempListOccur);
                        tempListPost.add(docno);//add docno to posts
                        dictionaryP.put(tempKey,tempListPost);
                    }

                }//end if
                else{// if the dictionary does not have the term, create a new List.

                    ArrayList<String> tempListPost = new ArrayList<String>();
                    ArrayList<Integer> tempListOccur = new ArrayList<Integer>();
                    int tempOccur = 1;
                    tempListOccur.add(tempOccur);
                    tempListPost.add(docno);
                    dictionaryO.put(tempKey, tempListOccur);
                    dictionaryP.put(tempKey,tempListPost);
                }//end else
            }//end for
            //System.out.println(dictionaryP.entrySet());
            //System.out.println("-------------------------------------------------");
            //System.out.println(dictionaryO.entrySet());
        }//end of index



        public class AlbumListAdapter extends SimpleAdapter {
            private ArrayList<String> gradeStringHere;
            private int[] colors = new int[] { 0x30ffffff, 0x30f2ffcc,0x30dfff80,0x30ccff33,0x30bfff00,0x3099cc00,0x3000e600 };

            public AlbumListAdapter(Context context, ArrayList<HashMap<String, String>> items, int resource, String[] from, int[] to,ArrayList<String> gradeStringnew) {
                super(context, items, resource, from, to);
                gradeStringHere = gradeStringnew;
                //Log.d("String",gradeStringnew + "&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");


            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                int colorPos;
                //TextView currentAlbumnSongsCount =(TextView) findViewById(R.id.songs_count);
                //gradeString = currentAlbumnSongsCount.getText().toString();
                //float grade =
                //Log.d("String",gradeStringHere.get(position) + "______________________________________________________________________");
                float grade = Float.parseFloat(gradeStringHere.get(position));
                Log.d("String",grade + "______________________________________________________________________");
                if (grade < 0.21)
                    colorPos = 0;
                else {
                    if (grade < 0.41)
                        colorPos = 1;
                    else {
                        if (grade < 0.61)
                            colorPos = 2;
                        else {
                            if (grade < 0.81)
                                colorPos = 4;
                            else
                                colorPos = 6;
                        }
                    }
                }


                view.setBackgroundColor(colors[colorPos]);
                return view;
            }
        }


    }

    public String readFileAsString() {
        String jsonString = "";
        try {
            InputStream is = getResources().openRawResource(R.raw.quoted);
            Writer writer = new StringWriter();
            char[] buffer = new char[1024];
            Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
            is.close();
            jsonString = writer.toString();
        } catch (IOException e) {
            Log.e("io", e.toString());
        }
        Log.d("jsonstring: ", jsonString);
        return jsonString;
    }


}