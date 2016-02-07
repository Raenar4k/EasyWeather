package com.raenarapps.easyweather.adapter;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.raenarapps.easyweather.DetailActivity;
import com.raenarapps.easyweather.R;

import java.util.List;


public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ViewHolder> {
    List<String> forecastArray;

    public ForecastAdapter(List<String> forecastArray) {
        this.forecastArray = forecastArray;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_forecast, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.textView.setText(forecastArray.get(position));
    }

    @Override
    public int getItemCount() {
        return forecastArray.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView textView;

        public ViewHolder(View v) {
            super(v);
            textView = (TextView) v.findViewById(R.id.list_item_forecast_textview);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(v.getContext(),DetailActivity.class);
            intent.putExtra(Intent.EXTRA_TEXT, textView.getText().toString());
            v.getContext().startActivity(intent);
        }
    }
}
