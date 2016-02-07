package com.raenarapps.easyweather.adapter;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.raenarapps.easyweather.DetailActivity;
import com.raenarapps.easyweather.ForecastFragment;
import com.raenarapps.easyweather.R;
import com.raenarapps.easyweather.Utility;
import com.raenarapps.easyweather.data.WeatherContract;


public class ForecastCursorAdapter extends RecyclerView.Adapter<ForecastCursorAdapter.ViewHolder> {
    CursorAdapter mCursorAdapter;
    Context mContext;
    Cursor mCursor;
    String mForecastString;

    public ForecastCursorAdapter(Context context, Cursor c, int flags) {
        mContext = context;
        mCursor = c;
        mCursorAdapter = new CursorAdapter(context, c, flags) {
            @Override
            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                return LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_forecast, parent, false);
            }

            @Override
            public void bindView(View view, Context context, Cursor cursor) {
                mForecastString = convertCursorRowToUXFormat(cursor);
            }
        };
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mCursorAdapter.newView(mContext, mCursor, parent);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        mCursor.moveToPosition(position);
        mCursorAdapter.bindView(holder.itemView, mContext, mCursor);
        holder.textView.setText(mForecastString);
        holder.position = position;
    }

    @Override
    public int getItemCount() {
        return mCursorAdapter.getCount();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView textView;
        int position;

        public ViewHolder(View v) {
            super(v);
            textView = (TextView) v.findViewById(R.id.list_item_forecast_textview);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(v.getContext(), DetailActivity.class);
            if (mCursor!=null){
                mCursor.moveToPosition(position);
                Uri uri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                        Utility.getPreferredLocation(mContext),
                        mCursor.getLong(ForecastFragment.COL_WEATHER_DATE));
                intent.setData(uri);
            }
            v.getContext().startActivity(intent);
        }
    }

    private String formatHighLows(double high, double low) {
        boolean isMetric = Utility.isMetric(mContext);
        String highLowStr = Utility.formatTemperature(high, isMetric) + "/" + Utility.formatTemperature(low, isMetric);
        return highLowStr;
    }

    private String convertCursorRowToUXFormat(Cursor cursor) {

        String highAndLow = formatHighLows(
                cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP),
                cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP));

        return Utility.formatDate(cursor.getLong(ForecastFragment.COL_WEATHER_DATE)) +
                " - " + cursor.getString(ForecastFragment.COL_WEATHER_DESC) +
                " - " + highAndLow;
    }

    public void swapCursor(Cursor cursor) {
        mCursor = cursor;
        mCursorAdapter.swapCursor(cursor);
        notifyDataSetChanged();
    }
}
