package com.tommunyiri.notes.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.tommunyiri.notes.R;
import com.tommunyiri.notes.adapters.NotesAdapter;
import com.tommunyiri.notes.database.NotesDatabase;
import com.tommunyiri.notes.databinding.ActivityMainBinding;
import com.tommunyiri.notes.entities.Note;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    public static final int REQUEST_CODE_ADD_NOTE = 1;
    private List<Note> noteList;
    private NotesAdapter notesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View v = binding.getRoot();
        setContentView(v);
        binding.imageAddNoteMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(getApplicationContext(), CreateNoteActivity.class),REQUEST_CODE_ADD_NOTE);
            }
        });
        //TODO Enable switching between LinearLayout and StaggeredGridLayout
        //binding.notesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.notesRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL));
        noteList=new ArrayList<>();
        notesAdapter=new NotesAdapter(noteList);
        binding.notesRecyclerView.setAdapter(notesAdapter);
        getNotes();
    }

    private void getNotes(){
        class GetNoteTask extends AsyncTask<Void, Void, List<Note>>{
            @Override
            protected List<Note> doInBackground(Void... voids) {
                return NotesDatabase.getNotesDatabase(getApplicationContext()).noteDao().getAllNotes();
            }

            @Override
            protected void onPostExecute(List<Note> notes) {
                super.onPostExecute(notes);
                if(noteList.size()==0){
                    noteList.addAll(notes);
                    notesAdapter.notifyDataSetChanged();
                }else{
                    noteList.add(0,notes.get(0));
                    notesAdapter.notifyItemInserted(0);
                }
                binding.notesRecyclerView.smoothScrollToPosition(0);
            }
        }
        new GetNoteTask().execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQUEST_CODE_ADD_NOTE && resultCode==RESULT_OK){
            getNotes();
        }
    }
}