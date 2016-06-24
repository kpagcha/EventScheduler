package es.uca.garciachacon.eventscheduler.data.model.tournament;

/**
 * Una entidad abstracta que representa un modelo básico con un nombre.
 */
public abstract class Entity {
    /**
     * Nombre de la entidad
     */
    protected String name;

    /**
     * Constructor de una entidad que inicializa su nombre con un valor no <code>null</code>.
     *
     * @param name cadena no <code>null</code>
     * @throws IllegalArgumentException si el nombre es <code>null</code>
     */
    public Entity(String name) {
        if (name == null)
            throw new IllegalArgumentException("Name cannot be null");

        this.name = name;
    }

    /**
     * Asigna un valor no <code>null</code> al nombre de la entidad.
     *
     * @param name una cadena no <code>null</code>
     * @throws IllegalArgumentException si el nombre es <code>null</code>
     */
    public void setName(String name) {
        if (name == null)
            throw new IllegalArgumentException("Name cannot be null");

        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String toString() {
        return name;
    }
}
