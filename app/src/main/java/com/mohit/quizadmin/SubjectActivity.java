package com.mohit.quizadmin;

import android.app.Dialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.ArrayMap;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.mohit.quizadmin.ClassActivity.catList;
import static com.mohit.quizadmin.ClassActivity.selected_cat_index;

public class SubjectActivity extends AppCompatActivity {

    private RecyclerView setsView;
    private Button addSetB;
    private EditText dialogSetName;
    private Button dialogAddB;
    private SubjectAdapter adapter;
    private FirebaseFirestore firestore;
    private Dialog loadingDialog, addSetDialog;

    public static List<String> setsIDs = new ArrayList<>();
    public static int selected_set_index=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subject);

        Toolbar toolbar = findViewById(R.id.sa_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Subjects");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setsView = findViewById(R.id.sets_recycler);
        addSetB = findViewById(R.id.addSetB);

        loadingDialog = new Dialog(SubjectActivity.this);
        loadingDialog.setContentView(R.layout.loading_progressbar);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setBackgroundDrawableResource(R.drawable.progress_background);
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);



        addSetDialog = new Dialog(SubjectActivity.this);
        addSetDialog.setContentView(R.layout.add_category_dialog);
        addSetDialog.setCancelable(true);
        addSetDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);

        dialogSetName = addSetDialog.findViewById(R.id.ac_cat_name);
        dialogAddB = addSetDialog.findViewById(R.id.ac_add_btn);

        firestore = FirebaseFirestore.getInstance();

        addSetB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogSetName.getText().clear();
                addSetDialog.show();
            }
        });

        dialogAddB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(dialogSetName.getText().toString().isEmpty())
                {
                    dialogSetName.setError("Enter Subject Name");
                    return;
                }

                addNewSet(dialogSetName.getText().toString());
            }
        });


        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        setsView.setLayoutManager(layoutManager);

        loadSets();

    }


    private void loadSets()
    {
        setsIDs.clear();

        loadingDialog.show();

        firestore.collection("QUIZ").document(catList.get(selected_cat_index).getId())
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                long noOfSets = (long)documentSnapshot.get("SETS");

                for(int i=1; i <= noOfSets; i++)
                {
                    setsIDs.add(documentSnapshot.getString("SET" + String.valueOf(i) + "_ID"));
                }

                catList.get(selected_cat_index).setSetCounter(documentSnapshot.getString("COUNTER"));
                catList.get(selected_cat_index).setNoOfSets(String.valueOf(noOfSets));

                adapter = new SubjectAdapter(setsIDs);
                setsView.setAdapter(adapter);

                loadingDialog.dismiss();

            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SubjectActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                        loadingDialog.dismiss();
                    }
                });

    }

    private void addNewSet(final String title)
    {
        loadingDialog.show();

        final String curr_cat_id = catList.get(selected_cat_index).getId();
        final String curr_counter = catList.get(selected_cat_index).getSetCounter();

        Map<String,Object> qData = new ArrayMap<>();
        qData.put("COUNT","0");

        firestore.collection("QUIZ").document(curr_cat_id)
                .collection(title).document("QUESTIONS_LIST")
                .set(qData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Map<String,Object> catDoc = new ArrayMap<>();
                        catDoc.put("COUNTER", String.valueOf(Integer.valueOf(curr_counter) + 1)  );
                        catDoc.put("SET" + String.valueOf(setsIDs.size() + 1) + "_ID", title);
                        catDoc.put("SETS", setsIDs.size() + 1);

                        firestore.collection("QUIZ").document(curr_cat_id)
                                .update(catDoc)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        Toast.makeText(SubjectActivity.this, " Set Added Successfully",Toast.LENGTH_SHORT).show();

                                        setsIDs.add(curr_counter);
                                        catList.get(selected_cat_index).setNoOfSets(String.valueOf(setsIDs.size()));
                                        catList.get(selected_cat_index).setSetCounter(String.valueOf(Integer.valueOf(curr_counter) + 1));

                                        adapter.notifyItemInserted(setsIDs.size());
                                        loadingDialog.dismiss();

                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(SubjectActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                                        loadingDialog.dismiss();
                                    }
                                });

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SubjectActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                        loadingDialog.dismiss();
                    }
                });

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == android.R.id.home)
        {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}
