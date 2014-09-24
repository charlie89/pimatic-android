package org.pimatic.connection;

import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;
import org.pimatic.model.Device;

/**
 * Created by charlie89 on 9/24/14.
 * Class can execute RestAPI-Calls
 * Communication runs async, so it can safely be called from the UI Thread
 */
public class RestAPI {

    private String url = "http://10.0.1.112/api/";

    public RestAPI(String action, Device d) {
        new Async().execute(action, d.getId());
    }

    private class Async extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... o) {
            //TODO: support for more API's?!
            //TODO: Read Url+Login from settings
            RestClient client = new RestClient(url + "device/" + o[1] + "/" + o[0], "", "");

            try {
                client.Execute(RequestMethod.GET);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }

            try {
                JSONObject json = new JSONObject(client.getResponse());
                if (json.getString("success").equalsIgnoreCase("true")) {
                    return true;
                }
                return false;
            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            }
        }
    }
}
