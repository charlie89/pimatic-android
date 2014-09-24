package org.pimatic.app;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.pimatic.model.Device;
import org.pimatic.model.DeviceVisitor;
import org.pimatic.model.SwitchDevice;
import org.pimatic.connection.RestAPI;

public class DeviceArrayAdapter extends ArrayAdapter<Device> {

    private final Activity context;
    private final DeviceViewHolderCreator viewHolderCreater;

    public void updateFromJson(JSONArray deviceArray) throws JSONException {
        for(int i = 0; i < deviceArray.length(); i++) {
            JSONObject deviceObj = deviceArray.getJSONObject(i);
            updateOrAddDevice(deviceObj);
        }
    }

    public void updateVariableFromJson(JSONObject obj) throws JSONException {
        String variableValue = obj.getString("value");
        String deviceId = obj.getString("deviceId");
        String attributeName = obj.getString("attributeName");

        updateOrAddVariable(deviceId, attributeName, variableValue);
    }

    public Device updateOrAddDevice(JSONObject obj) throws JSONException {
        String id = obj.getString("id");
        Device d = getDeviceById(id);
        if (d == null) {
            d = createDeviceFromJson(obj);
        } else {
            //TODO: Update device
        }
        this.add(d);
        return d;
    }

    public Device updateOrAddVariable(String deviceId, String attributeName, String newValue) {
        Device d = getDeviceById(deviceId);
        if (d != null) {
            d.addOrModifyAttribute(attributeName, newValue);
        }

        return d;
    }

    public Device getDeviceById(String id) {
        for(int i = 0; i < this.getCount(); i++) {
            Device d = this.getItem(i);
            if(d.getId().equals(id)) {
                return d;
            }
        }

        return null;
    }


    private static Device createDeviceFromJson(JSONObject obj) throws JSONException {
        String template = obj.getString("template");
        if(template.equals("switch")) {
            return new SwitchDevice(obj);
        } else {
            return new Device(obj);
        }
    }

    public DeviceArrayAdapter(Activity context, List<Device> list) {
        super(context, R.layout.device_layout, list);
        this.context = context;
        this.viewHolderCreater = new DeviceViewHolderCreator();
    }

    private static class DeviceViewHolder<D extends Device> {
        protected TextView deviceName;
        protected ArrayList<TextView> attributeViews = new ArrayList<TextView>();

        public void update(D d) {
            deviceName.setText(d.getName());
            for (TextView v : attributeViews) {
                Device.Attribute a = (Device.Attribute)v.getTag();
                v.setText(a.getValue());
            }
        }

    }

    private static class SwitchDeviceHolder extends DeviceViewHolder<SwitchDevice>{
        protected Switch stateSwitch;

        public void update(final SwitchDevice d) {
            super.update(d);
            stateSwitch.setChecked(d.getState());

            stateSwitch.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    // Perform action on click
                    String action;
                    if (stateSwitch.isChecked()) {
                        action = "turnOn";
                    } else {
                        action = "turnOff";
                    }
                    new RestAPI(action, d);
                }
            });
        }

    }


    private class DeviceViewHolderCreator implements DeviceVisitor<View> {
        public View createViewHolder(Device d) {
            return d.visit(this);
        }

        @Override
        public View visitDevice(Device d) {
            LayoutInflater inflator = context.getLayoutInflater();
            View view = inflator.inflate(R.layout.device_layout, null);
            DeviceViewHolder viewHolder = new DeviceViewHolder();
            viewHolder.deviceName = (TextView) view.findViewById(R.id.deviceName);
            LinearLayout attrsLayout = (LinearLayout)view.findViewById(R.id.attributesLayout);
            for(Device.Attribute attr : d.getAttributes()) {
                TextView attrView = new TextView(view.getContext());
                attrView.setTag(attr);
                attrView.setText(attr.getValue());
                viewHolder.attributeViews.add(attrView);
                attrsLayout.addView(attrView);
            }
            viewHolder.update(d);
            view.setTag(viewHolder);
            return view;
        }
        @Override
        public View visitSwitchDevice(SwitchDevice d) {
            LayoutInflater inflator = context.getLayoutInflater();
            View view = inflator.inflate(R.layout.switch_device_layout, null);
            SwitchDeviceHolder viewHolder = new SwitchDeviceHolder();
            viewHolder.deviceName = (TextView) view.findViewById(R.id.deviceName);
            viewHolder.stateSwitch = (Switch) view.findViewById(R.id.switchDeviceSwitch);
            viewHolder.update(d);
            view.setTag(viewHolder);
            return view;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = null;
        Device d = getItem(position);
        if (convertView == null) {
            view = viewHolderCreater.createViewHolder(d);
        } else {
            DeviceViewHolder holder = (DeviceViewHolder) convertView.getTag();
            holder.update(d);
            view = convertView;
        }
        return view;
    }
}