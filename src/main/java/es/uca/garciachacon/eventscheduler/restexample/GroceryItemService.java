package es.uca.garciachacon.eventscheduler.restexample;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("grocery")
public class GroceryItemService {
    private IGroceryItemDao dao;

    @Inject
    public GroceryItemService(IGroceryItemDao dao) {
        this.dao = dao;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<GroceryItem> getGroceryItems() {
        return dao.getAll();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public GroceryItem createGroceryItem(GroceryItem item) {
        return dao.create(item) ? item : null;
    }

    @Path("{name}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public GroceryItem getGroceryItem(@PathParam("name") String uniqueName) {
        return dao.get(uniqueName);
    }
}
