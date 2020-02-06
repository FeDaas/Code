package de.moviemanager.ui.detail.modifiable;

import de.moviemanager.ui.detail.modifications.Modification;

public interface ModifiableContext{
    void addModification(Modification<?> mod);
    Modification<Object> getLastModification();
    Modification<Object> removeLastModification();
    boolean hasModifications();
    int getModificationCount();
    void hideKeyboard();
}
