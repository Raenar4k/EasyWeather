package com.raenarapps.easyweather.adapter;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.raenarapps.easyweather.DetailActivity;
import com.raenarapps.easyweather.ForecastFragment;
import com.raenarapps.easyweather.R;
import com.raenarapps.easyweather.Utility;
import com.raenarapps.easyweather.data.WeatherContract;


public class ForecastCursorAdapter extends RecyclerView.Adapter<ForecastCursorAdapter.ViewHolder> {
    Context context;
    Cursor cursor;

    private static final int VIEW_TYPE_TODAY = 0;
    private static final int VIEW_TYPE_REGULAR = 1;

    public ForecastCursorAdapter(Context context, Cursor c) {
        this.context = context;
        cursor = c;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        if (viewType == VIEW_TYPE_TODAY) {
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_forecast_today, parent, false);
        } else {
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_forecast, parent, false);
        }
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        cursor.moveToPosition(position);
        String forecastString = cursor.getString(ForecastFragment.COL_WEATHER_DESC);
        long date = cursor.getLong(ForecastFragment.COL_WEATHER_DATE);
        double high = cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP);
        double low = cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP);

        String dateString = Utility.getFriendlyDayString(context, date);
        boolean isMetric = Utility.isMetric(context);
        String highString = Utility.formatTemperature(context, high, isMetric);
        String lowString = Utility.formatTemperature(context, low, isMetric);

        holder.forecastDescr.setText(forecastString);
        holder.forecastDate.setText(dateString);
        holder.forecastHigh.setText(highString);
        holder.forecastLow.setText(lowString);
        holder.position = position;
    }

    @Override
    public int getItemCount() {
        if (cursor != null) {
            return cursor.getCount();
        } else {
            return 0;
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return VIEW_TYPE_TODAY;
        } else {
            return VIEW_TYPE_REGULAR;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView forecastDescr;
        TextView forecastDate;
        TextView forecastHigh;
        TextView forecastLow;
        ImageView forecastIcon;
        int position;

        public ViewHolder(View v) {
            super(v);
            forecastDescr = (TextView) v.findViewById(R.id.list_item_forecast_textview);
            forecastDate = (TextView) v.findViewById(R.id.list_item_date_textview);
            forecastHigh = (TextView) v.findViewById(R.id.list_item_high_textview);
            forecastLow = (TextView) v.findViewById(R.id.list_item_low_textview);
            forecastIcon = (ImageView) v.findViewById(R.id.list_item_icon);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(v.getContext(), DetailActivity.class);
            if (cursor != null) {
                cursor.moveToPosition(position);
                Uri uri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                        Utility.getPreferredLocation(context),
                        cursor.getLong(ForecastFragment.COL_WEATHER_DATE));
                intent.setData(uri);
            }
            v.getContext().startActivity(intent);
        }
    }

    public void swapCursor(Cursor cursor) {
        this.cursor = cursor;
        notifyDataSetChanged();
    }
}
