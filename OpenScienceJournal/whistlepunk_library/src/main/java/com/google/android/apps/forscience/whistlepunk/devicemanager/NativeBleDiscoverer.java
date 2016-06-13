package com.google.android.apps.forscience.whistlepunk.devicemanager;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.preference.Preference;
import android.support.annotation.NonNull;

import com.google.android.apps.forscience.ble.DeviceDiscoverer;
import com.google.android.apps.forscience.whistlepunk.metadata.BleSensorSpec;
import com.google.android.apps.forscience.whistlepunk.metadata.ExternalSensorSpec;

/**
 * Discovers BLE sensors that speak our "native" Science Journal protocol.
 */
class NativeBleDiscoverer implements ExternalSensorDiscoverer {
    private ManageDevicesFragment mManageDevicesFragment;
    private Context mContext;
    private final DeviceDiscoverer mDeviceDiscoverer;

    public NativeBleDiscoverer(ManageDevicesFragment manageDevicesFragment, Context context) {
        mManageDevicesFragment = manageDevicesFragment;
        mContext = context;
        mDeviceDiscoverer = DeviceDiscoverer.getNewInstance(context);
    }

    @Override
    @NonNull
    public ExternalSensorSpec extractSensorSpec(Preference preference) {
        final String address = preference.getKey();
        String title = preference.getTitle().toString();
        return new BleSensorSpec(address, title);
    }

    @Override
    public void onDestroy() {
        mDeviceDiscoverer.destroy();
    }

    @Override
    public boolean canScan() {
        return mDeviceDiscoverer.canScan();
    }

    @Override
    public void startScanning(final SensorPrefCallbacks sensorPrefCallbacks) {
        mDeviceDiscoverer.startScanning(new DeviceDiscoverer.Callback() {
            @Override
            public void onDeviceFound(final DeviceDiscoverer.DeviceRecord record) {
                onDeviceRecordFound(record, sensorPrefCallbacks);
            }

            @Override
            public void onError(int error) {
                // TODO: handle errors
            }
        });
    }

    @Override
    public void stopScanning() {
        if (mDeviceDiscoverer != null) {
            mDeviceDiscoverer.stopScanning();
        }
    }

    private void onDeviceRecordFound(DeviceDiscoverer.DeviceRecord record,
            SensorPrefCallbacks sensorPrefCallbacks) {
        // First check if this is a paired device.
        BluetoothDevice device = record.device;
        String address = device.getAddress();

        if (!sensorPrefCallbacks.isSensorAlreadyKnown(address)) {
            // If not, add to "available"
            Preference newPref = ManageDevicesFragment.makePreference(device.getName(), address,
                    BleSensorSpec.TYPE, false, mContext);
            newPref.setWidgetLayoutResource(0);
            sensorPrefCallbacks.addAvailableSensorPreference(newPref);
        }
    }
}
