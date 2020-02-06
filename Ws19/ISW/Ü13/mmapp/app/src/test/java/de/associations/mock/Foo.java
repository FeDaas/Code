package de.associations.mock;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import de.util.Identifiable;

public class Foo implements Identifiable{
    private static final Map<Integer, Foo> instances; 
    static {
	    instances = new HashMap<>();
    }
    
    private static int nextId = 0;
    private final int id;
    
    private int limit;
    private String name;

    public Foo(String name, int limit) {
	    this.limit = limit;
	    this.name = name;
	    instances.put(this.id = nextId++, this);
    }

    public int limitMoo() {
	    return this.limit;
    }

    public int limitMooThrowIllegalAccessException() throws IllegalAccessException {
        throw new IllegalAccessException();
    }

    public int limitMooThrowIllegalArgumentException() throws IllegalArgumentException {
        throw new IllegalArgumentException();
    }


    public int limitMooThrowInvocationTargetException() throws InvocationTargetException {
        throw new InvocationTargetException(new RuntimeException());
    }


    public void setLimit(int lim) {
	    this.limit = lim;
    }

    @Override
    public String toString() {
	    return name;
    }
    
    @Override
    public int id() {
        return this.id;
    }
    
    public static Foo getById(int id) {
	    return Foo.instances.get(id);
    }
    
    public static void resetIds() {
	    instances.clear();
	    nextId = 0;
    }
}
