package de.associations;

import java.util.List;
import java.util.Optional;

import de.associations.shortcuts.IdMapper;
import de.util.Pair;

/**
 * 
 * <br><b>Author</b>: <a href="mailto:schustrchr@gmail.com">Ctoffer</a><br>
 *
 * @param <C> - Column type
 * @param <R> - Row type
 * 
 * @see BiMap
 */
interface AssociationMatrix <C, R>{
    void add(C k1, R k2);
    void remove(C k1, R k2);
    
    int sizeOfNonEmptyRows(C col);
    int sizeOfNonEmptyColumns(R row);
    
    void removeColumn(C k);
    void removeRow(R k);
    
    Optional<List<R>> getColumn(C column);
    Optional<List<C>> getRow(R row);
    
    /**
     * This function externalizes the internally stored data model via
     * identification numbers. Each association consists of a bidirectional
     * association between two objects obj1 & obj2, which can be represented as
     * (id(obj1), id(obj2)). This representation is applied for all stored
     * associations.
     * 
     * @param m1 - maps a column object to an id
     * @param m2 - maps a row object to an id
     * @return - list of pairs of ids representing all associations
     */
    List<Pair<Integer, Integer>> getIdPairs(IdMapper<C> m1, IdMapper<R> m2);
}
