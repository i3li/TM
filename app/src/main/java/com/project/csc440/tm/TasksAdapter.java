package com.project.csc440.tm;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

import java.text.SimpleDateFormat;
import java.util.Date;

class TasksAdapter extends FirebaseRecyclerAdapter<Task, TasksAdapter.TaskHolder> {

    class TaskHolder extends RecyclerView.ViewHolder {

        private  String formatDate(Date date) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEE, d MMMM yyy hh:mm a");
            return  simpleDateFormat.format(date);
        }

        private TextView firstLetterTextView;
        private TextView nameTextView;
        private TextView dueDateTextView;

        TaskHolder(@NonNull View itemView) {
            super(itemView);
            firstLetterTextView = itemView.findViewById(R.id.tv_task_first_letter);
            nameTextView = itemView.findViewById(R.id.tv_task_name);
            dueDateTextView = itemView.findViewById(R.id.tv_task_due_date);
        }

        void setName(String name) {
            firstLetterTextView.setText(name.substring(0, 1));
            nameTextView.setText(name);
        }

        void setDueDate(Date date) {
            dueDateTextView.setText(formatDate(date));
        }

    }

    TasksAdapter(@NonNull FirebaseRecyclerOptions<Task> options) {
        super(options);
    }

    @NonNull
    @Override
    public TaskHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_task, viewGroup, false);
        return new TaskHolder(view);
    }


    @Override
    protected void onBindViewHolder(@NonNull TaskHolder holder, int position, @NonNull Task model) {
        holder.setName(model.getName());
        holder.setDueDate(new Date(model.getDueDate()));
    }
}

