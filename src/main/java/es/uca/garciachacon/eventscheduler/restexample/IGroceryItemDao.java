package es.uca.garciachacon.eventscheduler.restexample;

import java.util.List;

public interface IGroceryItemDao {
    List<GroceryItem> getAll();
    GroceryItem get(String uniqueName);
    boolean create(GroceryItem item);
}
