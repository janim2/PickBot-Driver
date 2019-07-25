package com.pickbot.pickbotdriver;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Fake_settings extends Activity {
    EditText name,phone,car;
    Button register,cancel;
    FirebaseAuth mAuth;
    DatabaseReference mDriverDatabase;
    String userID;
    String mname;
    String mphone;
    String mcar;
    String mprofileImageUrl;
    ImageView profileimage;
    Uri resultUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fake_settings);

        name = (EditText)findViewById(R.id.name);
        phone = (EditText)findViewById(R.id.phone);
        car = (EditText)findViewById(R.id.car);
        register = (Button)findViewById(R.id.register);
        cancel = (Button)findViewById(R.id.cancel);
        profileimage = (ImageView) findViewById(R.id.profileimage);

        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();
        mDriverDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(userID);
        getUserinfo();

        profileimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent,1);
            }
        });
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveuserinformation();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                return;
            }
        });
    }

    public  void getUserinfo(){
        mDriverDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
//                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if(map.get("name") != null){
                        mname = map.get("name").toString();
                        name.setText(mname);
                    }
                    if(map.get("phone") != null){
                        mphone = map.get("phone").toString();
                        phone.setText(mphone);
                    }
                    if(map.get("car") != null){
                        mcar = map.get("car").toString();
                        car.setText(mcar);
                    }
                    if(map.get("profileImageUrl") != null){
                        mprofileImageUrl = map.get("profileImageUrl").toString();
                        Glide.with(getApplication()).load(mprofileImageUrl).into(profileimage);
                    }
                }else{

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void saveuserinformation() {
        mname = name.getText().toString();
        mphone = phone.getText().toString();
        mcar = car.getText().toString();
        Map userinfo  = new HashMap();
        userinfo.put("name",mname);
        userinfo.put("phone",mphone);
        userinfo.put("car",mcar);
        mDriverDatabase.updateChildren(userinfo);

        if(resultUri != null){
            StorageReference filePath = FirebaseStorage.getInstance().getReference().child("profile_images").child(userID);
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(),resultUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos);
            byte[] data = baos.toByteArray();
            UploadTask uploadTask = filePath.putBytes(data);

            uploadTask.addOnFailureListener(new OnFailureListener(){
                @Override
                public void onFailure(@NonNull Exception e) {
                    finish();
                    return;
                }
            });

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> downloadurl = taskSnapshot.getStorage().getDownloadUrl();
                    Map newImage = new HashMap();
                    newImage.put("profileImageUrl", downloadurl.toString());
                    mDriverDatabase.updateChildren(newImage);
                    finish();
                    return;
                }
            });
        }else{
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && resultCode == Activity.RESULT_OK){
            final Uri imageuri = data.getData();
            resultUri = imageuri;
            profileimage.setImageURI(resultUri);

        }
    }
}
