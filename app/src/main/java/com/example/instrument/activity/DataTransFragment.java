package com.example.instrument.activity;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.instrument.R;
import com.example.instrument.util.EpicParams;

public class DataTransFragment extends Fragment {

    TextView             connectNameTv;
    ListView             showDataLv;
    ArrayAdapter<String> dataListAdapter;
    BlueToothActivity    mainActivity;
    BluetoothDevice      remoteDevice;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_data_trans, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        connectNameTv = view.findViewById(R.id.device_name_tv);
        showDataLv = view.findViewById(R.id.show_data_lv);

        dataListAdapter = new ArrayAdapter<String>(getContext(), R.layout.layout_item_new_data);
        showDataLv.setAdapter(dataListAdapter);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mainActivity = (BlueToothActivity) getActivity();
        assert mainActivity != null;
    }

    public void receiveClient(BluetoothDevice clientDevice) {
        this.remoteDevice = clientDevice;
        connectNameTv.setText(String.format("连接设备: %s", remoteDevice.getName()));
    }


    public void updateDataView(String newMsg, int role) {
        if (role == EpicParams.REMOTE) {
            String remoteName = (remoteDevice == null || remoteDevice.getName() == null) ? "未命名设备" : remoteDevice.getName();
            newMsg = remoteName + " : " + newMsg;
        } else if (role == EpicParams.ME) {
            newMsg = "我 : " + newMsg;
        }
        dataListAdapter.insert(newMsg, 0);
    }

    public void connectServer(BluetoothDevice serverDevice) {
        this.remoteDevice = serverDevice;
        if (connectNameTv != null)
            connectNameTv.setText(String.format("连接设备: %s", remoteDevice.getName()));
    }
}
