package com.project.csc440.tm;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

class TasksAdapter extends FirebaseRecyclerAdapter<Task, TasksAdapter.TaskHolder> {

    class TaskHolder extends RecyclerView.ViewHolder {

        private TextView firstLetterTextView;
        private TextView nameTextView;

        TaskHolder(@NonNull View itemView) {
            super(itemView);
            firstLetterTextView = itemView.findViewById(R.id.tv_group_first_letter);
            nameTextView = itemView.findViewById(R.id.tv_group_name);
        }

        void setName(String name) {
            firstLetterTextView.setText(name.substring(0, 1));
            nameTextView.setText(name);
        }

    }

    TasksAdapter(@NonNull FirebaseRecyclerOptions<Task> options) {
        super(options);
    }

    @NonNull
    @Override
    public TaskHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_group, viewGroup, false);
        return new TaskHolder(view);
    }


    @Override
    protected void onBindViewHolder(@NonNull TaskHolder holder, int position, @NonNull Task model) {
        holder.setName(model.getName());
    }
}

