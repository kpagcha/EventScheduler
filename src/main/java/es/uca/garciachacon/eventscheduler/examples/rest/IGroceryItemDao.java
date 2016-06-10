package es.uca.garciachacon.eventscheduler.examples.rest;

import java.util.List;

public interface IGroceryItemDao {
    List<GroceryItem> getAll();
    GroceryItem get(String uniqueName);
    boolean create(GroceryItem item);
}
