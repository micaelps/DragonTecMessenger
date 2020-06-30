package com.micaelps.dragontecmessenger;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;
import com.xwray.groupie.GroupAdapter;
import com.xwray.groupie.Item;
import com.xwray.groupie.OnItemClickListener;
import com.xwray.groupie.ViewHolder;

import java.util.List;

public class ContactsActivity extends AppCompatActivity {

    private GroupAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_contacts);

        RecyclerView rv = findViewById(R.id.recycler);
        adapter =  new GroupAdapter<>();
        rv.setAdapter(adapter);
        rv.setLayoutManager(new LinearLayoutManager(this));

        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull Item item, @NonNull View view) {
                Intent intent = new Intent(ContactsActivity.this, ChatActivity.class);


                UserItem userItem = (UserItem) item;
                intent.putExtra("user",userItem.user);
                startActivity(intent);
            }
        });
        fetchUsers();
}

    private void fetchUsers() {
        FirebaseFirestore.getInstance().collection("/users")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if(e != null){
                            Log.e("teste",e.getMessage(),e);
                        return;
                        }
                        List<DocumentSnapshot> docs = queryDocumentSnapshots.getDocuments();
                        for(DocumentSnapshot doc:docs){

                           User user =  doc.toObject(User.class);
                            Log.d("teste",user.getUsername());
                            if(!user.getUuid().equals(FirebaseAuth.getInstance().getUid())){
                                adapter.add(new UserItem(user));
                            }
                        }
                    }
                });

    }

    private class UserItem extends Item<ViewHolder> {

        private final User user;

        private UserItem(User user) {
            this.user = user;
        }

        @Override
        public void bind(@NonNull ViewHolder viewHolder, int position) {
            TextView txtUsername = viewHolder.itemView.findViewById(R.id.textView);
            ImageView imgPhoto = viewHolder.itemView.findViewById(R.id.imageView);

            txtUsername.setText(user.getUsername());

            Picasso.get()
                    .load(user.getProfileUrl())
                    .into(imgPhoto);

        }

        @Override
        public int getLayout() {
            return R.layout.item_user;
        }
    }





}