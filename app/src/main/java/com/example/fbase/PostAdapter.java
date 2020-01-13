package com.example.fbase;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class PostAdapter extends FirebaseRecyclerAdapter<post, PostAdapter.BlogViewHolder> {


    private Context mContext;
    private FirebaseAuth mAuth=FirebaseAuth.getInstance();
    private boolean mPostLike=false;
    private DatabaseReference mLikeRef= FirebaseDatabase.getInstance().getReference().child("Likes");
    public PostAdapter(@NonNull FirebaseRecyclerOptions<post> options, Context context) {
        super(options);
        mContext=context;
    }

    @Override
    protected void onBindViewHolder(@NonNull final BlogViewHolder holder, int position, @NonNull post model) {

        final String post_key=getRef(position).getKey();
        holder.title.setText(model.getTitle());
        holder.description.setText(model.getDescription());
        Picasso.get().load(model.getImageUrl()).into(holder.postImage);
        holder.userName.setText("~"+model.getUserName());
        Log.e("image uri :",""+model.getImageUrl());

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Toast.makeText(mContext,""+mAuth.getCurrentUser().getUid(),Toast.LENGTH_SHORT).show();

            }
        });
        mLikeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(post_key).hasChild(mAuth.getUid()))
                {
                    holder.likeBtn.setImageResource(R.drawable.image_like_red);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        holder.likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPostLike = true;

                mLikeRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (mPostLike) {

                            if (dataSnapshot.child(post_key).hasChild(mAuth.getUid())) {

                                mLikeRef.child(post_key).child(mAuth.getUid()).removeValue();
                                holder.likeBtn.setImageResource(R.drawable.image_like_grey);
                            } else {
                                mLikeRef.child(post_key).child(mAuth.getUid()).setValue("random");
                                holder.likeBtn.setImageResource(R.drawable.image_like_red);
                            }
                            mPostLike=false;
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
        });


    }

    @NonNull
    @Override
    public BlogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new BlogViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.blog_display,
                parent,false));
    }

    public static class BlogViewHolder extends RecyclerView.ViewHolder{

        View mView;
        TextView title,description,userName;
        ImageView postImage;
        ImageButton likeBtn;
        public BlogViewHolder(@NonNull View itemView) {
            super(itemView);
            mView=itemView;
            title=itemView.findViewById(R.id.post_title);
            description=itemView.findViewById(R.id.post_desc);
            postImage=itemView.findViewById(R.id.post_image);
            userName=itemView.findViewById(R.id.post_user);
            likeBtn=itemView.findViewById(R.id.btn_like);
        }
    }
}
