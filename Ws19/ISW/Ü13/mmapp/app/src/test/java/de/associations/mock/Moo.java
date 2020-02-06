package de.associations.mock;

import java.util.HashMap;
import java.util.Map;

import de.util.Identifiable;

public class Moo implements Identifiable{
    private static final Map<Integer, Moo> instances; 
    static {
	    instances = new HashMap<>();
    }
    
    private static int nextId = 0;
    private final int id;
    private String name;
    private int limit;

    public Moo(String name, int limit) {
	    this.name = name;
	    this.limit = limit;
	    instances.put(this.id = nextId++, this);
    }

    @Override
    public String toString() {
	    return name;
    }

    public int limitFoo() {
	    return this.limit;
    }
    
    public void setLimit(int lim) {
	    this.limit = lim;
    }
    
    @Override
    public int id() {
        return this.id;
    }
    
    public static Moo getById(int id) {
	    return Moo.instances.get(id);
    }
    
    public static void resetIds() {
	    instances.clear();
	    nextId = 0;
    }
}
