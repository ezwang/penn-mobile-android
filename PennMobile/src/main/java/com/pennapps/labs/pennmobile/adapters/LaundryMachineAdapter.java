package com.pennapps.labs.pennmobile.adapters;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.SystemClock;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.pennapps.labs.pennmobile.LaundryBroadcastReceiver;
import com.pennapps.labs.pennmobile.LaundryFragment;
import com.pennapps.labs.pennmobile.MainActivity;
import com.pennapps.labs.pennmobile.R;
import com.pennapps.labs.pennmobile.classes.LaundryMachine;
import com.pennapps.labs.pennmobile.classes.LaundryRoom;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import butterknife.ButterKnife;

/**
 * Created by Jason on 11/18/2015.
 */
public class LaundryMachineAdapter extends ArrayAdapter<LaundryMachine> {

    private List<LaundryMachine> machines;
    private final LayoutInflater inflater;
    private MainActivity activity;
    private boolean wash;
    private LaundryRoom laundryRoom;
    private int[] traffic;
    private LaundryRecyclerAdapter lrAdapter;

    private class TimeXAxisValueFormatter implements IAxisValueFormatter {

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            int time = (int) (value % 12);
            if (value > 12) {
                return time + "p";
            }
            if (value >= 12 && time == 0) {
                return "12p";
            }
            if (time == 0) {
                return "12a";
            }
            return time + "a";
        }
    }

    public LaundryMachineAdapter(MainActivity activity, List<LaundryMachine> machines, boolean wash, int[] laundryTraffic, LaundryRoom laundryRoom) {
        super(activity, R.layout.laundry_list_item, machines);
        this.machines = machines;
        inflater = LayoutInflater.from(activity);
        this.activity = activity;
        this.wash = wash;
        this.laundryRoom = laundryRoom;
        traffic = laundryTraffic;
        smoothTrafficData();
    }

    private void smoothTrafficData() {
        int[] copyTraffic = new int[traffic.length];
        for (int i = 0; i < copyTraffic.length; i++) {
            copyTraffic[i] = traffic[i];
        }
        for (int i = 1; i < traffic.length - 1; i++) {
            if (Math.abs(copyTraffic[i] - (copyTraffic[i - 1] + copyTraffic[i + 1]) / 2) <=
                    Math.abs(copyTraffic[i - 1] - copyTraffic[i + 1]) / 2) {
                traffic[i] = (copyTraffic[i - 1] + copyTraffic[i + 1]) / 2;
            }
        }
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if (view == null) {
            view = inflater.inflate(R.layout.laundry_machine_list_item, parent, false);
        }
        ButterKnife.bind(this, view);
        LinearLayout summary = (LinearLayout) view.findViewById(R.id.registrar_lec_linearlayout);
        RecyclerView item = (RecyclerView) view.findViewById(R.id.recyclerView);
//        LinearLayout summary = (LinearLayout) view.findViewById(R.id.laundry_machine_list_summary);
//        RecyclerView item = (RecyclerView) view.findViewById(R.id.laundry_machine_list_normal);

//        item.setLayoutManager(new LinearLayoutManager(getContext()));
//        List<LaundryRecyclerAdapter.MachineStatus> statuses = new ArrayList<>();
//        for (LaundryMachine machine : machines) {
//            StringBuilder stringBuilder = new StringBuilder();
//            stringBuilder.append(machine.machine_type).append(" ").append(machine.number);
//            String description, title;
//            title = stringBuilder.toString();
//            boolean available;
//            if (machine.available) {
//                description = "Available";
//                available = true;
//            } else {
//                StringBuilder sb = new StringBuilder();
//                sb.append("Busy – ").append(machine.getTimeLeft(activity));
//                description = sb.toString();
//                available = false;
//            }
//            statuses.add(new LaundryRecyclerAdapter.MachineStatus(title, description, available));
//        }
//        lrAdapter = new LaundryRecyclerAdapter(statuses, activity);
        StringBuilder stringBuilder = new StringBuilder();
        if (position == 0) {
            item.setVisibility(View.GONE);
            summary.setVisibility(View.VISIBLE);
            ImageView imageView = (ImageView) view.findViewById(R.id.laundry_machine_iv);
            RelativeLayout summary_rl = (RelativeLayout) view.findViewById(R.id.laundry_machine_summary_rl);
            TextView description = (TextView) view.findViewById(R.id.laundry_building_description);
            description.setText(laundryRoom.name);
            TextView availcount = (TextView) view.findViewById(R.id.laundry_machine_summary);
            BarChart laundryChart = (BarChart) view.findViewById(R.id.laundry_chart);
            int avail = 0;
            for (LaundryMachine machine: machines) {
                if (machine.available) {
                    avail++;
                }
            }
            String str = wash ? activity.getString(R.string.laundry_washer) : activity.getString(R.string.laundry_dryer);
            stringBuilder.append(avail).append(" out of ").append(machines.size()).append(" ")
            .append(str).append(" available");
            availcount.setText(stringBuilder);
            Display display = activity.getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int width = size.x / 4;
            List<BarEntry> dataEntries = new ArrayList<>();
            for (int i = 0; i < 24; i++) {
                dataEntries.add(new BarEntry(i, traffic[i]));
            }
            int timeOfDay = new GregorianCalendar().get(Calendar.HOUR_OF_DAY);
            BarDataSet barDataSet = new BarDataSet(dataEntries, "Traffic");
            barDataSet.setDrawValues(false);
            laundryChart.setTouchEnabled(false);
            laundryChart.getXAxis().setDrawGridLines(false);
            laundryChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
            laundryChart.getXAxis().setValueFormatter(new TimeXAxisValueFormatter());
            laundryChart.getAxisRight().setEnabled(false);
            laundryChart.getAxisLeft().setEnabled(false);
            laundryChart.setDrawBorders(false);
            laundryChart.setDescription(null);
            BarData barData = new BarData(barDataSet);
            barData.setBarWidth(0.5f);
            laundryChart.setData(barData);
            laundryChart.highlightValue(timeOfDay, 0);
            laundryChart.getAxisLeft().setAxisMinimum(0);
            laundryChart.getAxisLeft().setAxisMaximum(6);
            laundryChart.fitScreen();
            laundryChart.animateXY(500, 500);
            laundryChart.invalidate();
            if (wash) {
                Picasso.with(activity).load(R.drawable.washer).resize(width, width).into(imageView);
                LaundryFragment.setSummary(laundryRoom.washers_available, laundryRoom.washers_in_use, 10, summary_rl, activity);
            } else {
                Picasso.with(activity).load(R.drawable.dryer).resize(width, width).into(imageView);
                LaundryFragment.setSummary(laundryRoom.dryers_available, laundryRoom.dryers_in_use, 10, summary_rl, activity);
            }
        } else{
            summary.setVisibility(View.GONE);
            item.setVisibility(View.VISIBLE);
//            TextView title = (TextView) view.findViewById(R.id.laundry_machine_title);
//            final LaundryMachine machine = machines.get(position-1);
//            stringBuilder.append(machine.machine_type).append(" ").append(machine.number);
//            title.setText(stringBuilder);
//            TextView description = (TextView) view.findViewById(R.id.laundry_machine_description);
//            Switch mSwitch = (Switch) view.findViewById(R.id.laundry_notification_button);
//
//            if (machine.available) {
//                description.setText(R.string.laundry_available);
//                description.setTextColor(activity.getResources().getColor(R.color.avail_color_green));
//                mSwitch.setVisibility(View.GONE);
//            } else {
//                stringBuilder = new StringBuilder();
//                stringBuilder.append("Busy – ").append(machine.getTimeLeft(activity));
//                description.setText(stringBuilder);
//                if (machine.getTimeInMilli() > 0) {
//                    mSwitch.setVisibility(View.VISIBLE);
//                } else {
//                    mSwitch.setVisibility(View.GONE);
//                }
//                setSwitchState(machine, mSwitch);
//                description.setTextColor(activity.getResources().getColor(R.color.avail_color_red));
//            }
        }
        return view;
    }

    private void setSwitchState(final LaundryMachine machine, Switch mSwitch) {
        final Intent intent = new Intent(getContext(), LaundryBroadcastReceiver.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(activity.getString(R.string.laundry_position), laundryRoom.name.hashCode() + machine.number);
        intent.putExtra(activity.getString(R.string.laundry), laundryRoom);
        intent.putExtra(activity.getString(R.string.laundry_machine_intent), machine);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(getContext(), laundryRoom.name.hashCode() + machine.number,
                intent, PendingIntent.FLAG_NO_CREATE);
        if (alarmIntent == null) {
            mSwitch.setChecked(false);
        } else {
            mSwitch.setChecked(true);
        }
        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                final AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
                if (isChecked) {
                    final PendingIntent alarmIntent = PendingIntent.getBroadcast(getContext(), laundryRoom.name.hashCode() + machine.number,
                            intent, PendingIntent.FLAG_CANCEL_CURRENT);
                    alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + machine.getTimeInMilli(), alarmIntent);
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(activity.getString(R.string.laundry_notification_snackbar_on))
                            .append(" ").append(machine.machine_type).append(" ")
                            .append(machine.number);
                    Snackbar snackbar = Snackbar.make(buttonView,stringBuilder , Snackbar.LENGTH_SHORT);
                    View subView = snackbar.getView();
                    TextView snackTextView = (TextView) subView.findViewById(android.support.design.R.id.snackbar_text);
                    snackTextView.setTextColor(activity.getResources().getColor(R.color.white));
                    snackbar.show();
                } else {
                    final PendingIntent alarmIntent = PendingIntent.getBroadcast(getContext(), laundryRoom.name.hashCode() + machine.number,
                            intent, PendingIntent.FLAG_NO_CREATE);
                    if (alarmIntent != null) {
                        alarmManager.cancel(alarmIntent);
                        alarmIntent.cancel();
                    }
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(activity.getString(R.string.laundry_notification_snackbar_off))
                            .append(" ").append(machine.machine_type).append(" ")
                            .append(machine.number);
                    if (buttonView.getContext() == null) {
                        return;
                    }
                    Snackbar snackbar = Snackbar.make(buttonView,stringBuilder, Snackbar.LENGTH_SHORT);
                    View subView = snackbar.getView();
                    TextView snackTextView = (TextView) subView.findViewById(android.support.design.R.id.snackbar_text);
                    snackTextView.setTextColor(activity.getResources().getColor(R.color.white));
                    snackbar.show();
                }
            }
        });
    }

    @Override
    public int getCount() {
        return machines.size() + 1;
    }
}
