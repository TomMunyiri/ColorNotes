package com.tommunyiri.notes.listeners;

import com.tommunyiri.notes.entities.Note;

public interface NotesListener {
    void onNoteClicked(Note note, int position);
}
