package com.esaip.securoverse2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class DataAdapter extends RecyclerView.Adapter<DataAdapter.DataViewHolder> {
    private List<UserEntity> users = new ArrayList<>();

    @Override
    public DataViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_2, parent, false);
        return new DataViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DataViewHolder holder, int position) {
        UserEntity user = users.get(position);
        StringBuilder text1 = new StringBuilder();
        StringBuilder text2 = new StringBuilder();

        if (user.getEmail() != null) {
            text1.append("Email: ").append(user.getEmail()).append("\n");
        }
        if (user.getPhone() != null) {
            text1.append("Téléphone: ").append(user.getPhone());
        }
        if (user.getUsername() != null) {
            text2.append("Username: ").append(user.getUsername()).append("\n");
        }
        if (user.getFullName() != null) {
            text2.append("Nom complet: ").append(user.getFullName());
        }

        holder.text1.setText(text1.toString().trim());
        holder.text2.setText(text2.toString().trim());
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public void setData(List<UserEntity> newUsers) {
        users = newUsers;
        notifyDataSetChanged();
    }

    static class DataViewHolder extends RecyclerView.ViewHolder {
        TextView text1;
        TextView text2;

        DataViewHolder(View view) {
            super(view);
            text1 = view.findViewById(android.R.id.text1);
            text2 = view.findViewById(android.R.id.text2);
        }
    }
}
