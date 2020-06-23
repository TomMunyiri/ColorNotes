package com.tommunyiri.notes.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.tommunyiri.notes.R;
import com.tommunyiri.notes.adapters.NotesAdapter;
import com.tommunyiri.notes.database.NotesDatabase;
import com.tommunyiri.notes.databinding.ActivityMainBinding;
import com.tommunyiri.notes.entities.Note;
import com.tommunyiri.notes.listeners.NotesListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NotesListener {
    private ActivityMainBinding binding;
    public static final int REQUEST_CODE_ADD_NOTE = 1;
    public static final int REQUEST_CODE_UPDATE_NOTE = 2;
    public static final int REQUEST_CODE_SHOW_NOTES = 3;
    private List<Note> noteList;
    private NotesAdapter notesAdapter;
    private int noteClickedPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View v = binding.getRoot();
        setContentView(v);
        binding.imageAddNoteMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(getApplicationContext(), CreateNoteActivity.class), REQUEST_CODE_ADD_NOTE);
            }
        });
        //TODO Enable switching between LinearLayout and StaggeredGridLayout
        //binding.notesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.notesRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        noteList = new ArrayList<>();
        notesAdapter = new NotesAdapter(noteList, this);
        binding.notesRecyclerView.setAdapter(notesAdapter);

        getNotes(REQUEST_CODE_SHOW_NOTES,false);

        binding.srNotes.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                binding.srNotes.setRefreshing(false);
            }
        });

        binding.inputSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                notesAdapter.cancelTimer();
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(noteList.size()!=0){
                    notesAdapter.searchNotes(s.toString());
                }
            }
        });
    }

    @Override
    public void onNoteClicked(Note note, int position) {
        noteClickedPosition = position;
        Intent intent = new Intent(getApplicationContext(), CreateNoteActivity.class);
        intent.putExtra("isViewOrUpdate", true);
        intent.putExtra("note", note);
        startActivityForResult(intent, REQUEST_CODE_UPDATE_NOTE);
    }

    private void getNotes(final int requestCode, final boolean isNoteDeleted) {
        class GetNoteTask extends AsyncTask<Void, Void, List<Note>> {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                binding.srNotes.setRefreshing(true);
            }

            @Override
            protected List<Note> doInBackground(Void... voids) {
                return NotesDatabase.getNotesDatabase(getApplicationContext()).noteDao().getAllNotes();
            }

            @Override
            protected void onPostExecute(List<Note> notes) {
                super.onPostExecute(notes);
                binding.srNotes.setRefreshing(false);
                if (notes.size() == 0) {
                    binding.tvNoNotes.setVisibility(View.VISIBLE);
                } else {
                    binding.tvNoNotes.setVisibility(View.GONE);
                    if (requestCode == REQUEST_CODE_SHOW_NOTES) {
                        noteList.addAll(notes);
                        notesAdapter.notifyDataSetChanged();
                    } else if (requestCode == REQUEST_CODE_ADD_NOTE) {
                        noteList.add(0, notes.get(0));
                        notesAdapter.notifyItemInserted(0);
                        binding.notesRecyclerView.smoothScrollToPosition(0);
                    } else if (requestCode == REQUEST_CODE_UPDATE_NOTE) {
                        noteList.remove(noteClickedPosition);
                        if(isNoteDeleted){
                            notesAdapter.notifyItemRemoved(noteClickedPosition);
                        }else {
                            noteList.add(noteClickedPosition, notes.get(noteClickedPosition));
                            notesAdapter.notifyItemChanged(noteClickedPosition);
                        }
                    }
                }
            }
        }
        new GetNoteTask().execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD_NOTE && resultCode == RESULT_OK) {
            getNotes(REQUEST_CODE_ADD_NOTE,false);
        } else if (requestCode == REQUEST_CODE_UPDATE_NOTE && resultCode == RESULT_OK) {
            if (data != null) {
                getNotes(REQUEST_CODE_UPDATE_NOTE,data.getBooleanExtra("isNoteDeleted",false));
            }
        }
    }
}