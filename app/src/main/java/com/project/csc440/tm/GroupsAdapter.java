package com.project.csc440.tm;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

class GroupsAdapter extends FirebaseRecyclerAdapter<Group, GroupsAdapter.GroupHolder> {

    class GroupHolder extends RecyclerView.ViewHolder {

        private TextView nameTextView;

        GroupHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.tv_group_name);
        }

        void setName(String name) {
            nameTextView.setText(name);
        }

    }

    GroupsAdapter(@NonNull FirebaseRecyclerOptions<Group> options) {
        super(options);
    }

    @NonNull
    @Override
    public GroupHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_recyclerview_group, viewGroup, false);
        return new GroupHolder(view);
    }


    @Override
    protected void onBindViewHolder(@NonNull GroupHolder holder, int position, @NonNull Group model) {
        holder.setName(model.getName());
    }
}
