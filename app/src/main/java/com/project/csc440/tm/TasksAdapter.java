package com.project.csc440.tm;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

import java.text.SimpleDateFormat;
import java.util.Date;

class TasksAdapter extends FirebaseRecyclerAdapter<Task, TasksAdapter.TaskHolder> {

    private static final String TAG = "TasksAdapter";

    private static final int CLOSE_DUE_DATE_IN_DAYS = 3;

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

        void setTask(Task task) {
            firstLetterTextView.setText(task.getName().substring(0, 1));
            if (task.isAccomplished()) {
                nameTextView.setText(Html.fromHtml("<s>" + task.getName() + "</s>"));
            } else
                nameTextView.setText(task.getName());
            setDueDate(task.getDueDate());
        }

        private void setDueDate(long lDate) {
            Date date = new Date(lDate);
            dueDateTextView.setText(formatDate(date));
            Date now = new Date();
            if (date.before(now))
                dueDateTextView.setTextColor(itemView.getResources().getColor(R.color.colorAccent));
            else if ((date.getTime() - now.getTime()) <= CLOSE_DUE_DATE_IN_DAYS*24*60*60*1000) {
                dueDateTextView.setTextColor(itemView.getResources().getColor(R.color.colorPrimary));
            }
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
        holder.setTask(model);
    }
}

