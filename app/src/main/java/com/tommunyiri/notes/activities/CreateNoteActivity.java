package com.tommunyiri.notes.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.tommunyiri.notes.R;
import com.tommunyiri.notes.database.NotesDatabase;
import com.tommunyiri.notes.databinding.ActivityCreateNoteBinding;
import com.tommunyiri.notes.entities.Note;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import es.dmoral.toasty.Toasty;

public class CreateNoteActivity extends AppCompatActivity {
    private ActivityCreateNoteBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateNoteBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        binding.imageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        //format the date as in example: Friday, 19 June 2020 08:35 PM
        binding.textDateTime.setText(new SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm a", Locale.getDefault())
                .format(new Date()));

        binding.imageSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveNote();
            }
        });
    }

    private void saveNote(){
        if(binding.inputNoteTitle.getText().toString().trim().isEmpty()){
            Toasty.warning(this,"Note title can't be empty!",Toasty.LENGTH_SHORT,true).show();
            return;
        }else if(binding.inputNoteSubtitle.getText().toString().trim().isEmpty()&&binding.inputNoteText.getText().toString().trim().isEmpty()){
            Toasty.warning(this,"Note can't be empty!",Toasty.LENGTH_SHORT,true).show();
            return;
        }
        final Note note = new Note();
        note.setTitle(binding.inputNoteTitle.getText().toString());
        note.setSubtitle(binding.inputNoteSubtitle.getText().toString());
        note.setNoteText(binding.inputNoteText.getText().toString());
        note.setDateTime(binding.textDateTime.getText().toString());

        @SuppressLint("StaticFieldLeak")
        class SaveNoteTask extends AsyncTask<Void, Void, Void>{
            @Override
            protected Void doInBackground(Void... voids) {
                NotesDatabase.getNotesDatabase(getApplicationContext()).noteDao().insertNote(note);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                Intent intent= new Intent();
                setResult(RESULT_OK,intent);
                finish();
            }
        }
        new SaveNoteTask().execute();
    }
}