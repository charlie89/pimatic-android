package org.pimatic.app;

import android.app.Activity;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.AdapterView;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.pimatic.connection.SocketIOClient;
import org.pimatic.model.Device;
import org.pimatic.model.DeviceManager;

import java.io.InputStream;
import java.util.List;
import java.util.Scanner;


public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
        Log.v("create", "hi!!!!!!!!!!!!!!");

//        RestClient client = new RestClient("http://@192.168.1.78:8899/api/devices", "admin", "admin");
//
//        try {
//            client.Execute(RequestMethod.GET);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        String response = client.getResponse();
//        if(response != null) Log.v("response",response);

        SocketIOClient client = new SocketIOClient("10.0.1.112", 80) {
            @Override
            public void onMessage(String eventId, Object jsonData) {
                Log.i("socket.io", "Event: " + eventId + ", data: " + jsonData);
            }

            @Override
            public void onOpen() {
                Log.i("socket.io", "open");
            }

            @Override
            public void onError(Exception e) {
                Log.i("socket.io", "Exception" + e);
            }

            @Override
            public void onClose() {
                Log.i("socket.io", "close");
            }
        };
        client.connect();
        client.addListener("deviceAttributeChanged",
            new SocketIOClient.JsonEventListener<JSONObject>() {
                @Override
                public void onEvent(JSONObject o) {
                    Log.i("listener_dac", o.toString());
                }
        });
        client.addListener("devices",
            new SocketIOClient.JsonEventListener<JSONArray>() {
                @Override
                public void onEvent(JSONArray a) {
                    Log.i("listener_dev", a.toString());
                    ShowDevices(a);
                }
        });
    }

    public void ShowDevices(JSONArray devices) {
        List<Device> list;
        try {
            DeviceManager.updateFromJson(devices);
            list = DeviceManager.getDevices();

            final ListView listview = (ListView) findViewById(R.id.devciesListView);
            final DeviceArrayAdapter adapter = new DeviceArrayAdapter(this, list);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    listview.setAdapter(adapter);

                    listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, final View view,
                                                int position, long id) {
                        }
                    });
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, DevicePageFragment.newInstance(position + 1))
                .commit();
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class DevicePageFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static DevicePageFragment newInstance(int sectionNumber) {
            DevicePageFragment fragment = new DevicePageFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public DevicePageFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            //TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            //textView.setText(Integer.toString(getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }

}
