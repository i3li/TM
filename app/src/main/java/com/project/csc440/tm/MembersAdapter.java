package com.project.csc440.tm;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

class MembersAdapter extends FirebaseRecyclerAdapter<UserProfile, MembersAdapter.MemberHolder> {

    interface MemberItemClickListener {
        void onMemberItemClick(String userId, String username);
    }

    private final MemberItemClickListener memberItemClickListener;
    private String adminKey;

    class MemberHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView firstLetterTextView;
        private TextView nameTextView;
        private TextView emailTextView;
        private ImageView adminIndicatorImageView;

        MemberHolder(@NonNull View itemView) {
            super(itemView);
            firstLetterTextView = itemView.findViewById(R.id.tv_member_first_letter);
            nameTextView = itemView.findViewById(R.id.tv_member_name);
            emailTextView = itemView.findViewById(R.id.tv_member_email);
            adminIndicatorImageView = itemView.findViewById(R.id.iv_admin_indicator);
            itemView.setOnClickListener(this);
        }

        void setName(String name) {
            firstLetterTextView.setText(name.substring(0, 1));
            nameTextView.setText(name);
        }

        void setEmail(String email) {
            emailTextView.setText(email);
        }

        void setAdminIndicator(boolean isAdmin) {
            adminIndicatorImageView.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
        }

        @Override
        public void onClick(View v) {
            int clickedPos = getAdapterPosition();
            TextView clickedMemberNameTextView = v.findViewById(R.id.tv_member_name);
            if (memberItemClickListener != null)
                memberItemClickListener.onMemberItemClick(getRef(clickedPos).getKey(), clickedMemberNameTextView.getText().toString());
        }
    }

    MembersAdapter(@NonNull FirebaseRecyclerOptions<UserProfile> options, MemberItemClickListener memberItemClickListener, String adminKey) {
        super(options);
        this.memberItemClickListener = memberItemClickListener;
        this.adminKey = adminKey;
    }

    @NonNull
    @Override
    public MemberHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_member, viewGroup, false);
        return new MemberHolder(view);
    }

    @Override
    protected void onBindViewHolder(@NonNull MemberHolder holder, int position, @NonNull UserProfile model) {
        holder.setName(model.getName());
        holder.setEmail(model.getEmail());
        holder.setAdminIndicator(getRef(position).getKey().equals(adminKey));
    }
}
