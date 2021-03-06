package com.afnanulcoder.piplans;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class NewMessageChecker extends AppCompatActivity {


    FirebaseAuth mAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    ProgressBar progressBar;
    //---------------------------------------------


    private ListView receivedMessagesListView;
    private List<UserInformation> messagesList;
    private List<String> sendersList;
    //private String[] sendersList;
    private ArrayAdapter<String> adapter;
    UserInformation userInformation;

    int numberOfUnreadMessages = 0;
    String amarNam = "";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_message_checker);

        progressBar = findViewById(R.id.progressBarID);
        progressBar.setVisibility(View.VISIBLE);

        mAuth = FirebaseAuth.getInstance();


        receivedMessagesListView = findViewById(R.id.receivedMessagesListID);
        userInformation = new UserInformation();
        messagesList = new ArrayList<UserInformation>();
        sendersList = new ArrayList<String>();
        databaseReference = FirebaseDatabase.getInstance().getReference("UserList").child(mAuth.getUid()).child("newMessageSender");




        receivedMessagesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(sendersList.get(position).equals("No New Message"))
                {
                    Toast.makeText(NewMessageChecker.this, "No New Message", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(NewMessageChecker.this, MessageActivity.class);
                intent.putExtra("senderID", mAuth.getUid());
                intent.putExtra("senderName", amarNam); //---------------------------------------Vul kora ase nam e----------------
                intent.putExtra("ReceiverID", messagesList.get(position).memberKey);
                intent.putExtra("ReceiverName", sendersList.get(position));
                intent.putExtra("fromWhere", "NewMessageChecker");
                startActivity(intent);

                databaseReference.child(messagesList.get(position).memberKey).removeValue();
                numberOfUnreadMessages--;
                if(numberOfUnreadMessages == 0)
                {
                    FirebaseDatabase.getInstance().getReference("UserList").child(mAuth.getUid()).child("isMsgReceived").setValue(false);
                }

                //Toast.makeText(NewMessageChecker.this, "Msg left: "+numberOfUnreadMessages, Toast.LENGTH_SHORT).show();
            }


        });



        // handler.postDelayed(runnable, 5000);


    }


    @Override
    protected void onStart() {

        FirebaseDatabase.getInstance().getReference("UserList").child(mAuth.getUid()).child("fullName").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                amarNam = dataSnapshot.getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                messagesList.clear();
                sendersList.clear();

                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren())
                {

                    UserInformation userInformation = new UserInformation(dataSnapshot1.getValue().toString(), dataSnapshot1.getKey().toString());


                    //UserInformations userInformations = dataSnapshot1.getValue(UserInformations.class);
                    messagesList.add(userInformation);
                    sendersList.add(userInformation.getMemberName());
                }

                numberOfUnreadMessages = messagesList.size();

                if(numberOfUnreadMessages == 0)
                {
                    sendersList.add("No New Message");
                }

                receivedMessagesListView.setAdapter(adapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


       // Toast.makeText(this, sendersList.toString(), Toast.LENGTH_SHORT).show();


        /*Toast.makeText(NewMessageChecker.this, "Number Of Messages: "+messagesList.size(), Toast.LENGTH_SHORT).show();
        sendersList = new String[messagesList.size()];

        for(int i=0; i<messagesList.size(); i++)
        {
            sendersList[i] = messagesList.get(i).getMemberName();
        }

         */


        adapter = new ArrayAdapter<String>(NewMessageChecker.this, R.layout.one_line_view, R.id.oneLineTextID, sendersList);


        progressBar.setVisibility(View.INVISIBLE);

        super.onStart();
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, HomeActivity.class));
    }
}