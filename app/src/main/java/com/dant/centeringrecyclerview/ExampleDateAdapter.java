package com.dant.centeringrecyclerview;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ExampleDateAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Date startDate;
    private Date endDate;

    private List<Date> dates;

    public ExampleDateAdapter() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

        try {
            startDate = formatter.parse("2017-01-01");
        } catch (ParseException ex) {}

        try {
            endDate = formatter.parse("2018-12-31");
        } catch (ParseException ex) {}

        Calendar start = Calendar.getInstance();
        start.setTime(startDate);

        Calendar end = Calendar.getInstance();
        end.setTime(endDate);

        dates = new ArrayList<>();
        for(Date date = start.getTime(); start.before(end); start.add(Calendar.DATE, 1), date = start.getTime()) {
            dates.add(date);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ExampleDateReyclerViewHolder(parent);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((ExampleDateReyclerViewHolder)holder).setDate(dates.get(position));
    }

    @Override
    public int getItemCount() {
        return dates.size();
    }
}
