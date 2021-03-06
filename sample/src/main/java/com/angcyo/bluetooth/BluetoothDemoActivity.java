package com.angcyo.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextClock;

import com.angcyo.sample.R;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.rsen.base.RBaseActivity;
import com.rsen.base.RBaseAdapter;
import com.rsen.base.RBaseViewHolder;
import com.rsen.drawable.CircleAnimDrawable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Set;

public class BluetoothDemoActivity extends RBaseActivity implements BluetoothDiscover.IBluetoothDiscoverListener {

    public static Logger log = LoggerFactory.getLogger("bluetooth.ui");
    BluetoothAdapter defaultAdapter;
    String msg;
    long scanMode = 0;
    BluetoothDeviceAdapter leftAdapter;
    BluetoothDeviceAdapter rightAdapter;

    @Override
    protected int getContentView() {
        return R.layout.activity_bluetooth_demo;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        leftAdapter = new BluetoothDeviceAdapter(this);
        rightAdapter = new BluetoothDeviceAdapter(this);
        mViewHolder.r(R.id.leftRecyclerView).setAdapter(leftAdapter);
        mViewHolder.r(R.id.rightRecyclerView).setAdapter(rightAdapter);

        fixRecyclerViewWidth(mViewHolder.r(R.id.leftRecyclerView));
        fixRecyclerViewWidth(mViewHolder.r(R.id.rightRecyclerView));

        //test drawable
//        mViewHolder.v(R.id.supportBluetoothView).setBackground(new CircleAnimDrawable());
        mViewHolder.v(R.id.leftButtonView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setBackground(new CircleAnimDrawable(BluetoothDemoActivity.this).setPosition(CircleAnimDrawable.POS_LEFT));

                textTextClock();
            }
        });
//        mViewHolder.v(R.id.centerButtonView).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                v.setBackground(new CircleAnimDrawable().setPosition(CircleAnimDrawable.POS_CENTER));
//            }
//        });
        mViewHolder.v(R.id.centerButtonView).setBackground(new CircleAnimDrawable(this).setPosition(CircleAnimDrawable.POS_CENTER));
        mViewHolder.v(R.id.rightButtonView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setBackground(new CircleAnimDrawable(BluetoothDemoActivity.this).setPosition(CircleAnimDrawable.POS_RIGHT));
            }
        });

        mViewHolder.v(R.id.leftRadioView).setBackground(createRadioBackground(CircleAnimDrawable.POS_LEFT));
        mViewHolder.v(R.id.centerRadioView).setBackground(createRadioBackground(CircleAnimDrawable.POS_CENTER));
        mViewHolder.v(R.id.rightRadioView).setBackground(createRadioBackground(CircleAnimDrawable.POS_RIGHT));

        YoYo.with(Techniques.Landing).delay(300).playOn(mViewHolder.v(R.id.centerButtonView));

        //TextClock测试
//        textTextClock();
    }

    private void textTextClock() {
        final TextClock textClock = (TextClock) mViewHolder.v(R.id.textClockView);
//        try {
//            final Method setShowCurrentUserTime = TextClock.class.getMethod("setShowCurrentUserTime", boolean.class);
//            setShowCurrentUserTime.invoke(textClock, true);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        textClock.setFormat24Hour("yyyy-MM-dd");
    }

    private Drawable createRadioBackground(int position) {
        StateListDrawable listDrawable = new StateListDrawable();
        listDrawable.addState(new int[]{android.R.attr.state_checked}, new CircleAnimDrawable(this).setPosition(position));
        return listDrawable;
    }

    private int getScreenWidth() {
        int width = getResources().getDisplayMetrics().widthPixels;
        return width;
    }

    private void fixRecyclerViewWidth(RecyclerView recyclerView) {
        final ViewGroup.LayoutParams layoutParams = recyclerView.getLayoutParams();
        layoutParams.width = getScreenWidth() / 2;
        recyclerView.setLayoutParams(layoutParams);
    }

    @Override
    protected void initViewData() {
        defaultAdapter = BluetoothHelper.getDefaultAdapter(this);
        if (defaultAdapter != null) {
            //已经匹配过的设备
            final Set<BluetoothDevice> bondedDevices = defaultAdapter.getBondedDevices();
            for (BluetoothDevice device : bondedDevices) {
                rightAdapter.addLastItem(new BluetoothDeviceBean(device));
            }

        }
        log.info("initViewData");

        BluetoothDiscover.instance().addDiscoverListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BluetoothDiscover.instance().removeDiscoverListener(this);
    }

    public void supportBluetooth(View view) {
        if (defaultAdapter != null && !TextUtils.isEmpty(defaultAdapter.getAddress())) {
            msg = "支持蓝牙";
        } else {
            msg = "不支持蓝牙";
        }
        showMsg(view);
    }

    public void enableBluetooth(View view) {
        if (defaultAdapter.isEnabled()) {
            msg = "蓝牙已打开";
        } else {
            msg = "蓝牙已关闭";
        }
        showMsg(view);
    }

    public void openBluetooth(View view) {
        if (defaultAdapter.isEnabled()) {
            msg = "断开蓝牙";
            defaultAdapter.disable();
        } else {
            msg = "打开蓝牙";
            defaultAdapter.enable();

            //使用系统交互的方式打开蓝牙
            //Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            //startActivity(enableBtIntent);
        }
        showMsg(view);
    }

    public void addressBluetooth(View view) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(defaultAdapter.getName());//蓝牙名称
        stringBuilder.append(" ");
        stringBuilder.append(defaultAdapter.getAddress());//蓝牙地址
        stringBuilder.append("\nMode:");
        stringBuilder.append(defaultAdapter.getScanMode());//扫描模式
        stringBuilder.append(" State:");
        stringBuilder.append(defaultAdapter.getState());//当前状态
        stringBuilder.append(" Scan:");
        stringBuilder.append(defaultAdapter.isDiscovering());//是否正在扫描
        msg = stringBuilder.toString();
        showMsg(view);
    }

    public void startDiscovery(View view) {
        if (defaultAdapter.isDiscovering()) {
            defaultAdapter.cancelDiscovery();
            msg = "取消扫描...";
        } else {
            leftAdapter.resetData(new ArrayList<>());
            defaultAdapter.startDiscovery();
            msg = "开始扫描...";
        }
        showMsg(view);
    }

    public void openBluetoothSettingActivity(View view) {
        Intent intent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
        startActivity(intent);
    }

    public void setScanMode(View view) {
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 120);//可被发现的持续时间
        startActivity(intent);

        scanMode++;
    }

    public void showMsg(View view) {
        Snackbar.make(view, msg, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onDeviceDiscover(BluetoothDevice device) {
        leftAdapter.addLastItem(new BluetoothDeviceBean(device));
    }

    static class BluetoothDeviceBean {
        public BluetoothDevice device;
        public String name;
        public String address;
        public int cls;

        public BluetoothDeviceBean(BluetoothDevice device) {
            this.device = device;
            this.name = device.getName();
            this.address = device.getAddress();
            this.cls = device.getBluetoothClass().getDeviceClass();
        }
    }

    class BluetoothDeviceAdapter extends RBaseAdapter<BluetoothDeviceBean> {

        public BluetoothDeviceAdapter(Context context) {
            super(context);
        }

        @Override
        protected int getItemLayoutId(int viewType) {
            return R.layout.activity_bluetooth_adapter_item;
        }

        @Override
        protected void onBindView(RBaseViewHolder holder, int position, BluetoothDeviceBean bean) {
            if (position % 2 == 0) {
                holder.v(R.id.rootLayout).setBackgroundResource(R.drawable.v_btn_default_white_selector);
            } else {
                holder.v(R.id.rootLayout).setBackgroundResource(R.drawable.v_btn_default_blue_selector);
            }

            if (bean.device.getBondState() == BluetoothDevice.BOND_BONDED) {
                holder.v(R.id.rootLayout).setBackgroundResource(R.drawable.v_btn_default_red_selector);
            }

            holder.tV(R.id.nameView).setText(bean.name);
            holder.tV(R.id.addressView).setText(bean.address);
            holder.tV(R.id.clsView).setText(String.valueOf(bean.cls));

            holder.v(R.id.rootLayout).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (bean.device.createBond()) {
                        log.info("连接至蓝牙:{} 开始.", bean.name);
                    } else {
                        log.info("连接至蓝牙:{} 失败.", bean.name);
                    }
                }
            });
        }
    }
}
