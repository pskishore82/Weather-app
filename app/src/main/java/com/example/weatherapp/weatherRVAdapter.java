package com.example.weatherapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class weatherRVAdapter extends RecyclerView.Adapter<weatherRVAdapter.ViewHolder> {
    private Context context;
    private ArrayList<weatherRVmodal> weatherRVmodalArrayList;

    public weatherRVAdapter(Context context, ArrayList<weatherRVmodal> weatherRVmodalArrayList) {
        this.context = context;
        this.weatherRVmodalArrayList = weatherRVmodalArrayList;
    }

    public void updateData(ArrayList<weatherRVmodal> newData) {
        weatherRVmodalArrayList.clear();
        weatherRVmodalArrayList.addAll(newData);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.weather_rv_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        weatherRVmodal modal = weatherRVmodalArrayList.get(position);
        holder.tempTV.setText(modal.getTemp()+"°C");
        Picasso.get().load("https://"+modal.getIcon()).into(holder.conditionIV);
        holder.windTV.setText(modal.getWindSpeed()+"Km/hr");
        holder.conditionTV.setText(modal.getCondition());
        SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd hh:mm");
        SimpleDateFormat output = new SimpleDateFormat("hh:mm aa");
        try {
            Date t=input.parse(modal.getTime());
            holder.timeTV.setText(output.format(t));

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return weatherRVmodalArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView windTV,tempTV,timeTV,conditionTV;
        private ImageView conditionIV;
        public ViewHolder(@NonNull View itemView){
            super(itemView);
            windTV=itemView.findViewById(R.id.IDTVWindSpeed);
            tempTV=itemView.findViewById(R.id.IDTVTemperature);
            timeTV=itemView.findViewById(R.id.IDTVTime);
            conditionIV=itemView.findViewById(R.id.IDIVCondition);
            conditionTV=itemView.findViewById(R.id.IDIVConditionTV);

        }
    }
}
