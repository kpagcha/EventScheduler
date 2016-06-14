package es.uca.garciachacon.eventscheduler.data.model.tournament.event.domain;

import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalAmount;
import java.util.Optional;

/**
 * Un <i>timeslot</i> o intervalo de tiempo es, en el contexto de eventos deportivos, un momento del dominio temporal
 * del torneo o del evento en el que puede tener lugar un enfrentamiento o parte de él. A lo largo de la librería,
 * por simplicidad, se hace referencia a un <i>timeslot</i> también como "hora de juego".
 * <p>
 * Un <i>timeslot</i> puede ser modelado de muchas diferentes formas según se defina el dominio temporal del
 * torneo. Cualquier <i>timeslot</i> tiene un elemento común: un entero que representa un orden cronológico de
 * prioridad en comparación con otros <i>timeslots</i>. Este entero tiene como objetivo definir su precedencia,
 * igualdad o posterioridad con respecto a otros. El valor que define el orden es inverso al de la relación de
 * comparación de los números enteros, es decir, un <i>timeslot</i> con orden 1 es superior a un timeslot de orden 2:
 * <code>
 * Timeslot t1 = new Timeslot(1);
 * Timeslot t2 = new Timeslot(2);
 * t1.compareTo(t2); // devuelve 1
 * </code>
 * <p>
 * Sobre un <i>timeslot</i> es posible definir información adicional y opcional: un comienzo de
 * <i>timeslot</i>, por medio de cualquier implementación de la interfaz {@link TemporalAccessor}, y/o una duración
 * del intervalo, a través de un miembro de un tipo que implemente {@link TemporalAmount}.
 * <p>
 * Para representar la opcional de tanto el comienzo del <i>timeslot</i> como de su duración, se hace uso de la
 * clase {@link Optional}. Si no se indica un comienzo ni/o una duración mediante los constructores respectivos, el
 * valor de los miembros será de {@link Optional#empty()}. Así, se puede representar tanto un <i>timeslot</i> con un
 * comienzo conocido o irrelevante y una duración desconocida o irrelevante, un <i>timeslot</i> con un timeslot
 * desconocido pero una duración conocida, así como un <i>timeslot</i> con un comienzo conocido y una duración
 * conocida. La versión más básica de un <i>timeslot</i> es aquélla cuyos comienzo y duración son desconocidos o
 * irrelevantes, luego la única información que aporta es la del orden cronológico.
 * <p>
 * Para conseguir representar todos estos tipos de <i>timeslots</i> se utilizan los constructores adecuados, y
 * para el comienzo y duración se elige la implementación que se adecúe a la instancia de la que se precisa. El orden
 * cronológico es obligatorio en todos los casos:
 * <p>
 * <code>
 * Timeslot t1 = new Timeslot(10); // timeslot con orden cronológico de 10
 * Timeslot t2 = new Timeslot(3, Duration.ofHours(1)); // timeslot con una duración de 1 hora
 * Timeslot t3 = new Timeslot(1, Period.ofDays(3));  // timeslot con una duración de 3 días
 * Timeslot t4 = new Timeslot(7, LocalDate.now()));  // timeslot que comienza en la fecha actual
 * Timeslot t5 = new Timeslot(13, LocalTime.of(16, 45)); // timeslot que comienza a las 16:45
 * Timeslot t6 = new Timeslot(9, DayOfWeek.SATURDAY); // tomeslot que comienza el sábado
 * Timeslot t7 = new Timeslot(2, LocalTime.of(10, 0), Duration.ofMinutes(90)); // timeslot que comienza a las 10:00
 * // con una duración de 90 minutos
 * </code>
 */
public class Timeslot extends Entity {
    /**
     * Orden cronológico del <i>timeslot</i>
     */
    private final int chronologicalOrder;

    /**
     * Comienzo opcional del <i>timeslot</i>
     */
    private final Optional<TemporalAccessor> start;

    /**
     * Duración opcional del <i>timeslot</i>
     */
    private final Optional<TemporalAmount> duration;

    /**
     * Construye un <i>timeslot</i> con la mínima información necesaria: su orden cronológico. Esta instancia
     * representaría un <i>timeslot</i> abstracto o indefinido, con una duración y un comienzo desconocidos o
     * irrelevantes.
     *
     * @param chronologicalOrder orden cronológico del <i>timeslot</i>
     */
    public Timeslot(int chronologicalOrder) {
        super(String.format("Timeslot [order=%d]", chronologicalOrder));
        this.chronologicalOrder = chronologicalOrder;
        start = Optional.empty();
        duration = Optional.empty();
    }

    /**
     * Construye un <i>timeslot</i> con, además del orden cronológico, el comienzo del transcurso del mismo.
     * Representa un <i>timeslot</i> que empieza en un momento conocido, pero con una duración no conocida o no
     * relevante.
     *
     * @param chronologicalOrder orden cronológico del <i>timeslot</i>
     * @param start              comienzo del <i>timeslot</i>, cualquier implementación de la clase del argumento. No
     *                           <code>null</code>
     * @throws NullPointerException si <code>start</code> es <code>null</code>
     */
    public Timeslot(int chronologicalOrder, TemporalAccessor start) {
        super(String.format("Timeslot [order=%d, start=%s]", chronologicalOrder, start));
        this.chronologicalOrder = chronologicalOrder;
        this.start = Optional.of(start);
        duration = Optional.empty();
    }

    /**
     * Construye un <i>timeslot</i> con, además del orden cronológico, la duración o intervalo del mismo. Representa
     * un <i>timeslot</i> con una duración conocida, pero con un comienzo desconocido o irrelevante.
     *
     * @param chronologicalOrder orden cronológico del <i>timeslot</i>
     * @param duration           duración del <i>timeslot</i>, cualquier implementación de la clase del argumento. No
     *                           <code>null</code>
     * @throws NullPointerException si <code>duration</code> es <code>null</code>
     */
    public Timeslot(int chronologicalOrder, TemporalAmount duration) {
        super(String.format("Timeslot [order=%d, duration=%s]", chronologicalOrder, duration));
        this.chronologicalOrder = chronologicalOrder;
        this.duration = Optional.of(duration);
        start = Optional.empty();
    }

    /**
     * Construye un <i>timeslot</i> con, además del orden cronológico, un comienzo y una duración. Representa un
     * <i>timeslot</i> tanto con un comienzo conocido, como con una duración determinada.
     *
     * @param chronologicalOrder orden cronológico del <i>timeslot</i>
     * @param start              comienzo del <i>timeslot</i>, cualquier implementación de la clase del argumento. No
     *                           <code>null</code>
     * @param duration           duración del <i>timeslot</i>, cualquier implementación de la clase del argumento. No
     *                           <code>null</code>
     * @throws NullPointerException si <code>start</code> o <code>duration</code> es <code>null</code>
     */
    public Timeslot(int chronologicalOrder, TemporalAccessor start, TemporalAmount duration) {
        super(String.format("Timeslot [order=%d, start=%s, duration=%s]", chronologicalOrder, start, duration));
        this.chronologicalOrder = chronologicalOrder;
        this.start = Optional.of(start);
        this.duration = Optional.of(duration);
    }

    public int getChronologicalOrder() {
        return chronologicalOrder;
    }

    public Optional<TemporalAccessor> getStart() {
        return start;
    }

    public Optional<TemporalAmount> getDuration() {
        return duration;
    }

    /**
     * Compara este <i>timeslot</i> con otro. Según define el orden de <i>timeslots</i>, un <i>timeslot</i>
     * precede a otro, o dicho de otro modo, es mayor que él, si el orden cronológico es superior, lo que denota
     * mayor prioridad en la jerarquía cronológica. Un <i>timeslot</i> será posterior o menor a otro si su orden
     * cronológico es superior. Si los órdenes cronológicos son iguales, los <i>timeslots</i> son iguales en términos
     * de orden.
     * <p>
     * En el caso de igualdad de órdenes cronológicos de dos <i>timeslot</i> a comparar, se da un caso especial.
     * Este es el caso en el que ambos especifiquen un comienzo conocido, es decir, que ambos se hayan instanciado
     * mediante el constructor {@link Timeslot#Timeslot(int, TemporalAccessor)} o con el constructor
     * {@link Timeslot#Timeslot(int, TemporalAccessor, TemporalAmount)}. La comparación se hará entonces entre los
     * comienzos de los <i>timeslots</i>, pero sólamente si ambos son de la misma subclase que implementa a
     * {@link TemporalAccessor} y si además, esta subclase, implementa la interfaz {@link Comparable}.
     *
     * @param t <i>timeslot</i> no nulo con el que se quiere comparar la instancia que invoca este método
     * @return <code>1</code> si este <i>timeslot</i> precede (es mayor) al <i>timeslot</i> con el que se le compara,
     * -1 si es posterior (menor), y 0 si son iguales.
     * @throws NullPointerException si el argumento <code>t</code> es <code>null</code>
     */
    public int compareTo(Timeslot t) {
        if (t == null)
            throw new NullPointerException();

        int cmp = -Integer.compare(chronologicalOrder, t.chronologicalOrder);

        if (cmp == 0 && start.isPresent() && t.start.isPresent() && start.get() instanceof Comparable<?> &&
                t.start.get() instanceof Comparable<?> && start.get().getClass() == t.start.get().getClass()) {
            Comparable<Comparable<?>> thisStart = (Comparable<Comparable<?>>) start.get();
            Comparable<Comparable<?>> otherStart = (Comparable<Comparable<?>>) t.start.get();

            cmp = -thisStart.compareTo(otherStart);
        }

        if (cmp > 0)
            cmp = 1;
        else if (cmp < 0)
            cmp = -1;

        return cmp;
    }

    /**
     * Comprueba si este <i>timeslot</i> se encuentra entre otros dos <i>timeslots</i>, es decir, si su transcurso
     * discurre entre ambos extremos, según el orden de comparación indica. Esta instancia de <i>timeslot</i> estará
     * dentro del rango si es mayor o igual que el menor de los extremos (que sea mayor o igual que el extremo
     * se refiere al valor de retorno del método de comparación {@link Timeslot#compareTo(Timeslot)}) y es menor o
     * igual que el mayor de los extremos, es decir, si precede al menor y sucede al mayor.
     * <p>
     * Los extremos se incluyen en el rango, y pueden ser iguales. En este último caso, este método efectivamente
     * comprobaría si este <i>timeslot</i> es igual a cualquiera de los argumentos.
     * <p>
     * El orden de los argumentos es indiferente, este método se encarga de identificar cuál es el comienzo del
     * rango y cuál es el final. Si son iguales, el comienzo se considera el primer argumento.
     *
     * @param t1 primer extremo del rango de <i>timeslots</i>
     * @param t2 segundo extremo del rango
     * @return <code>true</code> si este <i>timeslot</i> se encuentra dentro del rango indicado, <code>false</code>
     * si no. Si <code>t1</code> y <code>t2</code> son iguales, se devuelve <code>true</code> si la instancia que
     * invoca a este método es igual a ellos, y <code>false</code> si sucede lo contrario.
     * @throws NullPointerException si alguno de los argumentos son <code>null</code>
     */
    public boolean within(Timeslot t1, Timeslot t2) {
        if (t1 == null || t2 == null)
            throw new NullPointerException();

        Timeslot start, end;
        if (t1.compareTo(t2) >= 0) {
            start = t1;
            end = t2;
        } else {
            start = t2;
            end = t1;
        }

        return this.compareTo(start) <= 0 && this.compareTo(end) >= 0;
    }
}
