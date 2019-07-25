package com.pickbot.pickbotdriver;

import android.app.Activity;
import android.os.Bundle;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Fake_login extends Activity {
    EditText email,password;
    Button login,register;
    FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener firebaseauthlistener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fake_login);

        mAuth = FirebaseAuth.getInstance();
        firebaseauthlistener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if(user!=null){
                    startActivity(new Intent(Fake_login.this,Driver_map.class));
                }
            }
        };
        email = (EditText)findViewById(R.id.email);
        password = (EditText)findViewById(R.id.password);
        login = (Button) findViewById(R.id.login);
        register = (Button) findViewById(R.id.register);

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String Semail = email.getText().toString();
                String Spass = password.getText().toString();
                mAuth.createUserWithEmailAndPassword(Semail,Spass).addOnCompleteListener(Fake_login.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(!task.isSuccessful()){
                            Toast.makeText(Fake_login.this,"Signup failed",Toast.LENGTH_LONG).show();
                        }
                        else{
                            String userid = mAuth.getCurrentUser().getUid();
                            DatabaseReference current_user_db  = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(userid).child("name");
                            current_user_db.setValue(Semail);
                        }
                    }
                });
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String Semail = email.getText().toString();
                String Spass = password.getText().toString();
                mAuth.signInWithEmailAndPassword(Semail,Spass).addOnCompleteListener(Fake_login.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(!task.isSuccessful()){
                            Toast.makeText(Fake_login.this,"Login failed",Toast.LENGTH_LONG).show();
                        }else{
                            startActivity(new Intent(Fake_login.this,Driver_map.class));
                        }
                    }
                });
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(firebaseauthlistener);
    }

}

