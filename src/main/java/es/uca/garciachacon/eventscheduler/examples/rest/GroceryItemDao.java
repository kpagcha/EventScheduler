package es.uca.garciachacon.eventscheduler.examples.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GroceryItemDao implements IGroceryItemDao {
    private Map<String, GroceryItem> items = new ConcurrentHashMap<>();

    @Override
    public synchronized List<GroceryItem> getAll() {
        return new ArrayList<>(items.values());
    }

    @Override
    public synchronized GroceryItem get(String uniqueName) {
        return items.get(uniqueName);
    }

    @Override
    public synchronized boolean create(GroceryItem item) {
        if (items.containsKey(item.getName()))
            return false;
        items.put(item.getName(), item);
        return true;
    }
}
