package com.mohit.quizadmin;

import android.app.Dialog;
import android.content.Intent;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.mohit.quizadmin.ClassActivity.catList;
import static com.mohit.quizadmin.ClassActivity.selected_cat_index;
import static com.mohit.quizadmin.SubjectActivity.selected_set_index;
import static com.mohit.quizadmin.SubjectActivity.setsIDs;

public class ChapterActivity extends AppCompatActivity {

    private RecyclerView quesView;
    private Button addQB;
    public static List<ChapterModel> quesList = new ArrayList<>();
    private ChapterAdapter adapter;
    private FirebaseFirestore firestore;
    private Dialog loadingDialog,addQuesDialog;
    private EditText dialogQuesName;
    private Button dialogAddB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chapters);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Chapters");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        quesView = findViewById(R.id.chap_recycler);
        addQB = findViewById(R.id.addChapB);

        loadingDialog = new Dialog(ChapterActivity.this);
        loadingDialog.setContentView(R.layout.loading_progressbar);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setBackgroundDrawableResource(R.drawable.progress_background);
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);

        addQuesDialog = new Dialog(ChapterActivity.this);
        addQuesDialog.setContentView(R.layout.add_category_dialog);
        addQuesDialog.setCancelable(true);
        addQuesDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);

        dialogQuesName = addQuesDialog.findViewById(R.id.ac_cat_name);
        dialogAddB = addQuesDialog.findViewById(R.id.ac_add_btn);

        firestore = FirebaseFirestore.getInstance();

        addQB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogQuesName.getText().clear();
                addQuesDialog.show();
            }
        });

        dialogAddB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(dialogQuesName.getText().toString().isEmpty())
                {
                    dialogQuesName.setError("Enter Class Name");
                    return;
                }

                addNewQuestion(dialogQuesName.getText().toString());
            }
        });

//        addQB.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(ChapterActivity.this, ChapterDetailsActivity.class);
//                intent.putExtra("ACTION","ADD");
//                startActivity(intent);
//            }
//        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        quesView.setLayoutManager(layoutManager);

        firestore = FirebaseFirestore.getInstance();

        loadQuestions();

    }

    private void loadQuestions()
    {
        quesList.clear();

        loadingDialog.show();

        firestore.collection("QUIZ").document(catList.get(selected_cat_index).getId())
                .collection(setsIDs.get(selected_set_index)).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        Map<String, QueryDocumentSnapshot> docList = new ArrayMap<>();

                        for(QueryDocumentSnapshot doc : queryDocumentSnapshots)
                        {
                            docList.put(doc.getId(),doc);
                        }

                        QueryDocumentSnapshot quesListDoc  = docList.get("QUESTIONS_LIST");

                        String count = quesListDoc.getString("COUNT");

                        for(int i=0; i < Integer.valueOf(count); i++)
                        {
                            String quesID = quesListDoc.getString("Q" + String.valueOf(i+1) + "_ID");

                            QueryDocumentSnapshot quesDoc = docList.get(quesID);

                            quesList.add(new ChapterModel(
                                    quesID,
                                    quesDoc.getString("QUESTION")
                            ));

                        }

                        adapter = new ChapterAdapter(quesList);
                        quesView.setAdapter(adapter);

                        loadingDialog.dismiss();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ChapterActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                        loadingDialog.dismiss();
                    }
                });

    }
    private void addNewQuestion(final String title)
    {
        addQuesDialog.dismiss();
        loadingDialog.show();

        Map<String,Object> quesData = new ArrayMap<>();

        quesData.put("QUESTION",title);



        final String doc_id = firestore.collection("QUIZ").document(catList.get(selected_cat_index).getId())
                .collection(setsIDs.get(selected_set_index)).document().getId();

        firestore.collection("QUIZ").document(catList.get(selected_cat_index).getId())
                .collection(setsIDs.get(selected_set_index)).document(doc_id)
                .set(quesData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Map<String,Object> quesDoc = new ArrayMap<>();
                        quesDoc.put("Q" + String.valueOf(quesList.size() + 1) + "_ID", doc_id);
                        quesDoc.put("COUNT",String.valueOf(quesList.size() + 1));

                        firestore.collection("QUIZ").document(catList.get(selected_cat_index).getId())
                                .collection(setsIDs.get(selected_set_index)).document("QUESTIONS_LIST")
                                .update(quesDoc)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(ChapterActivity.this, " Question Added Successfully", Toast.LENGTH_SHORT).show();

                                        quesList.add(new ChapterModel(doc_id,title));

                                        adapter.notifyItemInserted(catList.size());

                                        loadingDialog.dismiss();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(ChapterActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                                        loadingDialog.dismiss();
                                    }
                                });


                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ChapterActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                        loadingDialog.dismiss();
                    }
                });


    }


    @Override
    protected void onResume() {
        super.onResume();

        if(adapter != null) {
            adapter.notifyDataSetChanged();
        }
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
