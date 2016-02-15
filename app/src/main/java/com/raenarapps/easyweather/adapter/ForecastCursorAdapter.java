package com.raenarapps.easyweather.adapter;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.raenarapps.easyweather.ForecastFragment;
import com.raenarapps.easyweather.R;
import com.raenarapps.easyweather.Utility;
import com.raenarapps.easyweather.data.WeatherContract;


public class ForecastCursorAdapter extends RecyclerView.Adapter<ForecastCursorAdapter.ViewHolder> {
    private Context context;
    private Cursor cursor;

    private static final int VIEW_TYPE_TODAY = 0;
    private static final int VIEW_TYPE_REGULAR = 1;
    private boolean useTodayLayout;

    public interface ViewHolderCallback {
        void onViewHolderClick(Uri uri, int position);
    }

    private ViewHolderCallback listener;
    private int activatedPosition = -1;

    public ForecastCursorAdapter(Context context, Cursor c, ViewHolderCallback listener) {
        this.context = context;
        cursor = c;
        this.listener = listener;
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
        int conditionId = cursor.getInt(ForecastFragment.COL_WEATHER_CONDITION_ID);

        if (getItemViewType(position) == VIEW_TYPE_TODAY) {
            holder.forecastIcon.setImageResource(Utility.getArtResourceForConditionId(conditionId));
        } else {
            holder.forecastIcon.setImageResource(Utility.getIconResourceForConditionId(conditionId));
        }

        holder.forecastDescr.setText(forecastString);
        holder.forecastDate.setText(dateString);
        holder.forecastHigh.setText(highString);
        holder.forecastLow.setText(lowString);
        holder.position = position;

        if (position == activatedPosition) {
            holder.itemView.setActivated(true);
        } else {
            holder.itemView.setActivated(false);
        }
    }

    @Override
    public int getItemCount() {
        if (cursor != null) {
            return cursor.getCount();
        } else {
            return 0;
        }
    }

    public void setActivatedPosition(int activatedPosition) {
        this.activatedPosition = activatedPosition;
        notifyDataSetChanged();
    }

    public void setUseTodayLayout(boolean useTodayLayout) {
        this.useTodayLayout = useTodayLayout;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0 && useTodayLayout) {
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
            if (cursor != null) {
                cursor.moveToPosition(position);
                Uri uri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                        Utility.getPreferredLocation(context),
                        cursor.getLong(ForecastFragment.COL_WEATHER_DATE));
                listener.onViewHolderClick(uri, position);
                activatedPosition = position;
                notifyDataSetChanged();
            }
        }
    }

    public void swapCursor(Cursor cursor) {
        this.cursor = cursor;
        notifyDataSetChanged();
    }
}
