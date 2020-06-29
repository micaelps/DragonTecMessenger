package com.micaelps.dragontecmessenger;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.usage.StorageStatsManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.UUID;

public class RegisterActivity extends AppCompatActivity {
    private EditText mEditUsername;
    private EditText mEditEmail;
    private EditText mEditPassword;
    private Button mBtnEnter;
    private Button mBtnSelectedPhoto;
    private Uri mBtnSelectedUri;
    private ImageView mImagePhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_register);

        mEditUsername = findViewById(R.id.edit_username);
        mEditEmail = findViewById(R.id.edit_email);
        mEditPassword = findViewById(R.id.edit_password);
        mBtnEnter = findViewById(R.id.btn_enter);
        mBtnSelectedPhoto = findViewById(R.id.btn_selected_photo);
        mImagePhoto = findViewById(R.id.img_photo);

        mBtnSelectedPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedPhoto();
            }
        });

        mBtnEnter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createUser();

            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==0){
           mBtnSelectedUri = data.getData();
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), mBtnSelectedUri);
                mImagePhoto.setImageDrawable(new BitmapDrawable(bitmap));
                mBtnSelectedPhoto.setAlpha(0);
            } catch (IOException e) {

            }
        }
    }

    private void selectedPhoto() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent,0);
    }

    private void createUser(){
        String nome = mEditUsername.getText().toString();
        String email = mEditEmail.getText().toString();
        String senha = mEditPassword.getText().toString();
        if(nome  == null || nome.isEmpty() || email==null || email.isEmpty() || senha.isEmpty()){
            Toast.makeText(this,"Senha e email devem ser preenchidos",Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,senha)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Log.i("Sucesso",task.getResult().getUser().getUid());
                    saveUserInFireBase();

                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i("Falhou",e.getMessage());
            }
        });
    }

    private void saveUserInFireBase() {
         String filename = UUID.randomUUID().toString();
         final StorageReference ref = FirebaseStorage.getInstance().getReference("/images/"+filename);
         ref.putFile(mBtnSelectedUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
               @Override
               public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Log.i("teste",uri.toString());

                            String uid = FirebaseAuth.getInstance().getUid();
                            String username = mEditUsername.getText().toString();
                            String profileUrl = uri.toString();
                            User user = new User(uid,username,profileUrl);

                            FirebaseFirestore.getInstance().collection("users")
                                .add(user)
                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(DocumentReference documentReference) {
                                        Log.i("Teste",documentReference.getId());
                                        Intent intent = new Intent(RegisterActivity.this, MessagesActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.i("Teste",e.getMessage());
                                }
                            });
                        }
                    });
               }
           }
         ).addOnFailureListener(new OnFailureListener() {
             @Override
             public void onFailure(@NonNull Exception e) {
                Log.i("falhou", e.getMessage(), e);
             }
         });
    }


}