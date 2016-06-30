package es.uca.garciachacon.eventscheduler.data.model.tournament;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import es.uca.garciachacon.eventscheduler.data.validation.validable.Validable;
import es.uca.garciachacon.eventscheduler.data.validation.validable.ValidationException;
import es.uca.garciachacon.eventscheduler.data.validation.validator.EventValidator;
import es.uca.garciachacon.eventscheduler.data.validation.validator.Validator;
import es.uca.garciachacon.eventscheduler.solver.TournamentSolver.MatchupMode;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Representa un evento deportivo o una categoría deportiva en el contexto de un torneo deportivo. Es un elemento
 * principal del torneo ({@link Tournament}), el cual se compone de eventos o categorías. Cuando se instancia un
 * torneo, automáticamente se crea la asociación bidireccional entre el torneo y los eventos que lo componen. A partir
 * de esta última clase torneo que engloba al menos un evento, se puede calcular los horarios y, por tanto, se
 * obtendría el horario de un evento en particular. Para más información acerca del cálculo de los horarios y los
 * partidos, se puede consultar {@link Tournament} o, directamente, la clase empleada para modelar el problema e
 * iniciar el proceso de resolución {@link es.uca.garciachacon.eventscheduler.solver.TournamentSolver}.
 * <p>
 * Un evento se define con un conjunto de jugadores ({@link Player}), los cuales se enfrentarán entre sí en distintos
 * partidos que se celebrarán. Estos partidos tienen lugar en una serie de emplazamientos o localizaciones de juego
 * disponibles ({@link Localization}), y transurrirán a lo largo de un conjunto de horas de juego disponibles
 * ({@link Timeslot}). Las localizaciones y <i>timeslots</i> del evento representan entidades disponibles, lo cual no
 * quiere decir que se les deba dar uso por completo. No obstante, los jugadores sí son entidades que se deben
 * emplear en su totalidad, es decir, todos los jugadores formarán parte de, al menos, un partido.
 * <p>
 * Un evento puede tener diversas opciones configurables: se puede indicar el número de jugadores que componen un
 * partido, con un valor mínimo de incluso 1 para situaciones en las que el evento modele un deporte o una actividad
 * de un único jugador. Para establecer el valor de esta opción se puede hacer uso de uno de los constructores,
 * {@link Event#Event(String, List, List, List, int, int, int)} o emplear {@link Event#setPlayersPerMatch(int)}.
 * <p>
 * Se puede configurar el número de partidos que cada jugador deba jugar, con un mínimo de 1, posibilitando modelar
 * eventos tipo liguilla, liga o <i>round-robin</i>. Este valor se puede asignar con
 * {@link Event#setMatchesPerPlayer(int)}, o haciendo uso del constructor mencionado anteriormente.
 * <p>
 * Cada evento tendrá un número fijo de partidos del que se compondrá, y vendrá determinado por el valor de estos dos
 * últimos componentes configurables: el número de jugadores por partido y el número de partidos por jugador.
 * <p>
 * Para los eventos con más de un jugador por partido y múltiples partidos por jugador, se puede definir mediante
 * {@link MatchupMode} el modo de emparejamiento que se empleará para cada uno de estos múltiples partidos. Éste es
 * por defecto {@link MatchupMode#ANY}, indicando que no hay restricciones respecto del modo como se calcularán los
 * partidos de este evento a la hora de formar el horario. Se pueden indicar otros modos, como
 * {@link MatchupMode#ALL_DIFFERENT}, que obliga a que todos los enfrentamientos siempre enfrenten a una combinación
 * diferente de jugadores (por ejemplo, el enfrentamiento "Jugador 1 contra Jugador 2" solamente ocurrirá una vez; no
 * se repetirá), o {@link MatchupMode#ALL_EQUAL}, que obliga a lo contrario, todos los enfrentamientos para cada
 * jugador, deberán componerse de la misma combinación de jugadores (por ejemplo, el enfrentamiento "Jugador 1 contra
 * Jugador 2" deberá ocurrir tantas veces como número de partidos por jugador defina el evento).
 * <p>
 * Los partidos de un evento tendrán una duración que se cuantifica en número de <i>timeslots</i>, es decir, el
 * número de horas de juegos que tiene el rango de <i>timeslots</i> entre el comienzo del partido y el final. Este
 * valor se asigna con el método {@link Event#setTimeslotsPerMatch(int)}, o haciendo uso del constructor
 * anteriormente mencionado. El valor mínimo es de 1, e indica que el partido termina en el mismo <i>timeslot</i> en
 * el que termina. Por ejemplo, si un evento define un número de <i>timeslots</i> por partido de 3, y tenemos un
 * espacio temporal en el que transcurrirán los partidos de [t1, t2, ..., t10], si a un partido se le es asignado un
 * comienzo en <i>timeslot</i> <i>t3</i>, transcurrirá durante el rango <i>t3, t4, t5</i>: comienza en <i>t3</i> y
 * termina en <i>t5</i>, incluido.
 * <p>
 * Se pueden marcar distintas horas de juego con un carácter especial que las hará considerarse como <i>breaks</i>,
 * tiempos de descanso o, sencillamente, momentos del evento en los que no puede tener lugar ningún partido (ni
 * comenzar ni transcurrir ninguno de las particiones de su duración). Se pueden emplear métodos como
 * {@link Event#setBreaks(List)} o {@link Event#addBreak(Timeslot)} para marcar los <i>breaks</i> del evento. Este
 * componente se debería emplear para situaciones que modelen eventos como aquellos que tienen lugar a lo largo de
 * varios días, y se quiera excluir el período de la noche del transcurso del torneo.
 * <p>
 * Veáse el siguiente ejemplo de un evento que tiene lugar durante dos días, y se divide en dos etapas: el
 * día 1 de 16:00 a 21:00 y el día 2 de 10:00 a 14:00. La duración de cada partido es de 2 <i>timeslots</i>:
 * <p>
 * <pre>
 * {@code
 *  List<Timeslot> timeslots = new ArrayList();
 *
 *  // Se añaden los partidos del primer día, comenzando a las 16:00 con una duración de 1 hora cada uno
 *  timeslots.add(new Timeslot(1, LocalTime.of(16, 0), Duration.ofHours(1)));
 *  timeslots.add(new Timeslot(1, LocalTime.of(17, 0), Duration.ofHours(1)));
 *  timeslots.add(new Timeslot(1, LocalTime.of(18, 0), Duration.ofHours(1)));
 *  timeslots.add(new Timeslot(1, LocalTime.of(19, 0), Duration.ofHours(1)));
 *  timeslots.add(new Timeslot(1, LocalTime.of(20, 0), Duration.ofHours(1)));
 *
 *  // Se añaden los partidos del segundo día
 *  timeslots.add(new Timeslot(2, LocalTime.of(10, 0), Duration.ofHours(1)));
 *  timeslots.add(new Timeslot(2, LocalTime.of(11, 0), Duration.ofHours(1)));
 *  timeslots.add(new Timeslot(2, LocalTime.of(12, 0), Duration.ofHours(1)));
 *  timeslots.add(new Timeslot(2, LocalTime.of(13, 0), Duration.ofHours(1)));
 *
 *  Event event = new Event("Example Event", players, courts, timeslots);
 *  event.setTimeslotsPerMatch(2);
 *
 *  // en este punto, tal y como se ha definido el evento, existe la posibilidad de que se calculen partido que
 *  // comiencen el día 1 a las 20:00 y terminen el día 2 a las 10:00 porque estos timeslots son consecutivos
 *  new Tournament("Example Tournament", event).solve();
 * }
 * </pre>
 * <p>
 * La situación anteriormente descrita es indeseable. Este puede ser uno de los casos en los que sea conveniente el
 * uso de los <i>breaks</i>. Habría que añadir un <i>timeslots</i> adicional a la lista que sea inmediatamente
 * posterior al último <i>timeslot</i> del día 1 (el que comienza a las 20:00), que representase el período nocturno
 * de inactividad entre el día 1 y 2:
 * <p>
 * <pre>
 * {@code
 *   Timeslot night = new Timeslot(1, LocalTime.of(21:00));
 *   timeslots.add(night);
 * }
 * </pre>
 * <p>
 * Y, finalmente, se marcaría ese <i>timeslot</i> como <i>break</i> después de crear la instancia del evento:
 * <p>
 * <pre>
 * {@code
 *   event.addBreak(night);
 * }
 * </pre>
 * <p>
 * Un evento puede considerar equipos de dos maneras distintas:
 * <p>
 * Un equipo entendido como una entidad única compuesta por jugadores, donde en términos prácticos el equipo se
 * consideraría un jugador entendido como entidad que juega, más que individuo en particular que juega. En este caso,
 * el cálculo del horario es igual que si se tuviesen individuos porque en realidad se representan de igual forma.
 * <p>
 * El otro caso es el que considera la individualidad de cada jugador a la hora de calcular los horarios, pero al
 * mismo tiempo, forma parte de un equipo. Una de las situaciones en las que se deberían emplear equipos es en
 * torneos con múltiples categorías donde sea posible que un mismo individuo participe en distintas de las mismas.
 * Por ejemplo, en un torneo de tenis con una categoría de simples y otra de dobles, sería ideal tener en cuenta la
 * participación múltiple de un jugador tanto en el cuadro de simples como en el de dobles, donde participa como
 * componente de un equipo (la pareja de dobles). Si en la categoría de dobles se representasen los equipos como
 * entidad propia y no se considerase la individualidad de los componentes (cuando en realidad la hay), existiría la
 * posibilidad de colisión de partidos del cuadro de simples con partidos del cuadro de dobles para un individuo dado.
 * <p>
 * Este último caso se puede manejar haciendo uso de métodos como {@link Event#addTeam(Team)} para añadir un equipo
 * de jugadores que se conoce de antemano. Si no se conoce la composición de ningún equipo, pero aún así se quiere
 * dotar de carácter de por equipos al evento, se ha de hacer uso del método {@link Event#setPlayersPerTeam(int)} e
 * indicar el número de jugadores que compondrá cada equipo. Esto hará que se considere el evento como un evento por
 * equipos. También es importante recordar que se debe indicar el total de jugadores que compondrán cada partido
 * mediante {@link Event#setPlayersPerMatch(int)} (o mediante el constructor), que es diferente del número de
 * jugadores por equipo, pero ambos valores deben ser coherentes (es decir, en un evento de, por ejemplo, 10
 * jugadores por partido, no pueden existir equipos de 3 jugadores; este último debe ser divisor del primero). Los
 * jugadores de un equipo predefinido jugarán juntos todos los partidos en los que participen, sin embargo, los demás
 * equipos desconocidos que se formen durante el cálculo del horario, variarián (y esto será lo más probable)
 * dependiendo del partido. Para que la composición de los equipos sea fija es necesario definirlos previamente.
 * <p>
 * Sobre un evento se puede configurar la posibilidad de que determinados jugadores no se encuentren disponibles a
 * ciertas horas de juego determinadas, o que simplemente no se desee que los partidos de estos jugadores se planeen
 * para estas horas. Haciendo uso de métodos como {@link Event#setUnavailablePlayers(Map)} o
 * {@link Event#addUnavailablePlayerAtTimeslot(Player, Timeslot)} se puede indicar cuándo un jugador determinado no
 * está disponible, por lo tanto, no tendrá un partido a esa hora.
 * <p>
 * Un componente similar al anterior se trata la configuración de localizaciones de juego no disponibles. Al igual
 * que la configuración de jugadores de juego no disponibles, se puede indicar para cada localización aquellos
 * <i>timeslots</i> en los que ningún partido de ningún jugador puede tener lugar. Se puede añadir una
 * indisponibilidad de localización con {@link Event#setUnavailableLocalizations(Map)},
 * {@link Event#addUnavailableLocalizationAtTimeslot(Localization, Timeslot)} y métodos relacionados.
 * <p>
 * Es posible definir de antemano un enfrentamiento que obligatoriamente deberá ocurrir en el evento. Un
 * enfrentamiento es modelado por {@link Matchup} (para más información léase la documentación de esta clase). Se
 * puede añadir un emparejamiento predefinido con {@link Event#addMatchup(Matchup)} y métodos relacionados. Los
 * enfrentamientos que se registren ocurrirán obligatoriamente entre los jugadores que indiquen, y en cualquiera de las
 * localizaciones y de las horas de juego que especifiquen.
 * <p>
 * De entre todas las localizaciones disponibles que define el evento, es posible restringir el conjunto de ellas
 * para un jugador en particular de modo que sus partidos obligatoriamente tengan lugar en las localizaciones que les
 * han sido asociadas. Para configurar esta opción se usan los métodos {@link Event#setPlayersInLocalizations(Map)} o
 * {@link Event#addPlayerInLocalization(Player, Localization)}. Así, por ejemplo, si se tuviese un total de cinco
 * localizaciones para el evento, si al "Jugador 1" se le asignase la localización "Pista 2", sus partidos únicamente
 * podrán tener lugar en esa "Pista 2", pero en ninguna de las demás.
 * <p>
 * De forma similar a la opción anterior, se puede asignar a un jugador los <i>timeslots</i> en los que sus partidos
 * deben comenzar de entre todos los <i>timeslots</i> del espacio temporal del evento. Se usan los métodos
 * {@link Event#setPlayersAtTimeslots(Map)}, {@link Event#addPlayerAtTimeslot(Player, Timeslot)} y relacionados.
 * <p>
 * Se da una situación especial que involucra a los emparejamientos predeterminados y a las localizaciones y horas de
 * juego asignadas a jugadores. Esta situación únicamente se da en eventos con más de un partido por jugador. Si se
 * añade un emparejamiento predefinido, se consultan las localizaciones y horas asignadas a cada jugador que conforma
 * este emparejamiento. Para cada jugador, si a éste no se le ha asignado ninguna localización donde sus partidos
 * deban tener lugar (es decir, que <code>event.getPlayersInLocalizations().get(player)</code> no existe, es
 * <code>null</code>), automáticamente se le asignarán todas las localizaciones del evento. Esto no quiere decir que
 * el enfrentamiento predeterminado que se vaya a añadir no vaya a suceder en el subconjunto de localizaciones que
 * indique; el comportamiento será el esperado. La misma lógica se aplica para los <i>timeslots</i> asginados a un
 * jugador. Sin embargo, si el jugador sí tenía localizaciones de juego asignadas (u horas de juego, se emplea la
 * misma mecánica), no se asignarán automáticamente todas las del evento, sino que a las que tenía asignadas se le
 * sumarán las del enfrentamiento.
 * <p>
 * Cada vez que se hace una modificación en uno de los atributos o propiedades del evento que son relevantes al
 * modelo del proceso de resolución y cálculo de horarios de un torneo, se marcará la entidad como <i>cambiada</i>
 * mediante el método de {@link Observable}, {@link Event#setChanged()}. Esto indicará que la instancia del evento es
 * antigua, y su estado difiere del que poseía al construir el torneo.
 * <p>
 * Un evento se considerará en un estado final y consistente cuando no esté marcado como <i>cambiadp</i>. En el
 * contexto de la resolución de un torneo, ese estado consistente se conseguirá al invocar el proceso de resolución
 * mediante {@link Tournament#solve()}, que restablece el estado del evento llamando al método
 * {@link Event#setAsUnchanged()}.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Event extends Observable implements Validable {

    /**
     * Nombre del evento o la categoría
     */
    private String name;

    /**
     * Jugadores concretos o abstractos (equipos) que participan en el evento
     */
    private final List<Player> players;

    /**
     * Localizaciones o terrenos de juego disponibles para la categoría
     */
    private final List<Localization> localizations;

    /**
     * Horas en las que tendrá lugar esta categoría (dominio temporal del evento o categoría)
     */
    private final List<Timeslot> timeslots;

    /**
     * Torneo al que pertenece la categoría
     */
    @JsonIgnore
    private Tournament tournament;

    /**
     * Número de partidos que cada jugador ha de jugar en esta categoría
     */
    private int nMatchesPerPlayer;

    /**
     * Duración de un partido en timeslots u horas
     */
    private int nTimeslotsPerMatch;

    /**
     * Número de jugador que componen un partido
     */
    private int nPlayersPerMatch;

    /**
     * Número de jugadores por equipo, si hay equipos
     */
    private int nPlayersPerTeam;

    /**
     * Composición de los equipos, si hay
     */
    private List<Team> teams = new ArrayList<>();

    /**
     * Lista de timeslots del evento donde no pueden tener lugar partidos
     */
    private List<Timeslot> breaks = new ArrayList<>();

    /**
     * Timeslots u horas en las que cada jugador no está disponible
     */
    private Map<Player, Set<Timeslot>> unavailablePlayers = new HashMap<>();

    /**
     * Diccionario de localizaciones de juego no disponibles en determinadas horas
     */
    private Map<Localization, Set<Timeslot>> unavailableLocalizations = new HashMap<>();

    /**
     * Emparejamientos fijos predefinidos
     */
    private Set<Matchup> predefinedMatchups = new HashSet<>();

    /**
     * Diccionario de jugadores para cada cual los enfrentamientos de los que forme parte han de tener lugar
     * en cualquiera de las localizaciones del conjunto de localizaciones de juego asociado a su entrada
     */
    private Map<Player, Set<Localization>> playersInLocalizations = new HashMap<>();

    /**
     * Diccionario de jugadores para cada cual los enfrentamientos de los que forme parte han de comenzar en
     * cualquiera de las horas del conjunto de timeslots asociado a su entrada
     */
    private Map<Player, Set<Timeslot>> playersAtTimeslots = new HashMap<>();

    /**
     * Si esta categoría define más de un partido por jugador, indica el modo de emparejamiento
     */
    private MatchupMode matchupMode = MatchupMode.ANY;

    /**
     * Validador del evento
     */
    private Validator<Event> validator = new EventValidator();

    /**
     * Construye un evento que tendrá un conjunto de jugadores que participarán en él, un conjunto de localizaciones
     * de juego donde tendrán lugar los enfrentamientos, así como un conjunto de <i>timeslots</i> donde transcurrirán
     * los mismos.
     * <p>
     * Este constructor hace uso del constructor {@link Event#Event(String, List, List, List, int, int, int)}, luego
     * si alguno de los argumentos no cumplen las precondiciones descritas en éste último, se lanzarán las
     * excepciones correspondientes.
     *
     * @param name          nombre del evento
     * @param players       jugadores que participan en el evento deportivo
     * @param localizations localizaciones de juego en las que tendrá lugar
     * @param timeslots     <i>timeslots</i> en los que los enfrentamientos ocurrirán
     * @throws NullPointerException si alguno de los argumentos es <code>null</code>
     */
    public Event(String name, List<Player> players, List<Localization> localizations, List<Timeslot> timeslots) {
        this(name, players, localizations, timeslots, 1, 2, 2);
    }

    /**
     * Construye un evento que tendrá un conjunto de jugadores que participarán en él, un conjunto de localizaciones
     * de juego donde tendrán lugar los enfrentamientos, así como un conjunto de <i>timeslots</i> donde transcurrirán
     * los mismos. Además, se indica el númeor de partidos que cada jugador deberá jugar, el número de
     * <i>timeslots</i> que cada partido ocupará (su duración) y el número de jugadores que compone cada partido.
     * <p>
     * Si la lista de <i>timeslots</i> no está ordenada, se ordenará de forma que cada <i>timeslot</i> preceda al que
     * le siga en la lista, es decir, cada elemento es mayor que el siguiente. No obstante, no puede haber
     * <i>timeslots</i> con el mismo orden cronológico.
     *
     * @param name              nombre del evento
     * @param players           jugadores que participan en el evento deportivo
     * @param localizations     localizaciones de juego en las que tendrá lugar
     * @param matchesPerPlayer  número de partidos por jugador, mayor o igual que 1
     * @param timeslotsPerMatch número de timeslots por partido, mayor o igual que 1
     * @param playersPerMatch   número de jugadores por partido, mayor o igual que 1 y coherente con el número de
     *                          jugadores del evento (debe ser divisor)
     * @throws NullPointerException     si alguno de los argumentos es <code>null</code>
     * @throws IllegalArgumentException si la lista de jugadores está vacía
     * @throws IllegalArgumentException si la lista de jugadores contiene un jugador <code>null</code>
     * @throws IllegalArgumentException si el número de jugadores por partido es menor que 1
     * @throws IllegalArgumentException si el número de jugadores partido no es coherente con el número de jugadores
     *                                  que contiene la lista, es decir, si no es divisor de este último
     * @throws IllegalArgumentException si la lista de jugadores contiene jugadores duplicados
     * @throws IllegalArgumentException si la lista de localizaciones está vacía
     * @throws IllegalArgumentException si la lista de localizaciones contiene una localización <code>null</code>
     * @throws IllegalArgumentException si la lista de localizaciones contiene localizaciones duplicadas
     * @throws IllegalArgumentException si la lista de <i>timeslots</i> está vacía
     * @throws IllegalArgumentException si la lista de <i>timeslots</i> contiene un <i>timeslot</i> <code>null</code>
     * @throws IllegalArgumentException si el número de <i>timeslots</i> por partido es menor que 1
     * @throws IllegalArgumentException si el número de jugadores por partido es menor que 1
     * @throws IllegalArgumentException si la lista de <i>timeslots</i> contiene <i>timeslots</i> duplicados
     * @throws IllegalArgumentException si la lista de <i>timeslots</i>, una vez ordenada, contiene algún
     *                                  <i>timeslot</i> que precede estrictamente al siguiente, es decir, que al
     *                                  compararlos son iguales
     */
    public Event(String name,
            List<Player> players,
            List<Localization> localizations,
            List<Timeslot> timeslots,
            int matchesPerPlayer,
            int timeslotsPerMatch,
            int playersPerMatch) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(players);
        Objects.requireNonNull(localizations);
        Objects.requireNonNull(timeslots);

        if (players.isEmpty())
            throw new IllegalArgumentException("Players cannot be empty");

        if (players.contains(null))
            throw new IllegalArgumentException("Players cannot contain a null player");

        if (playersPerMatch < 1)
            throw new IllegalArgumentException("Number of players per match cannot be less than 1");

        if (players.size() % playersPerMatch != 0)
            throw new IllegalArgumentException(String.format(
                    "Number of players (%d) is not coherent to the number of" + " players per match (%d)",
                    players.size(),
                    playersPerMatch
            ));

        if (new HashSet<>(players).size() < players.size())
            throw new IllegalArgumentException("Players cannot contain duplicates");

        if (localizations.isEmpty())
            throw new IllegalArgumentException("Localizations cannot be empty");

        if (localizations.contains(null))
            throw new IllegalArgumentException("Localizations cannot contain a null localization");

        if (new HashSet<>(localizations).size() < localizations.size())
            throw new IllegalArgumentException("Localizations cannot contain duplicates");

        if (timeslots.isEmpty())
            throw new IllegalArgumentException("Timeslots cannot be empty");

        if (timeslots.contains(null))
            throw new IllegalArgumentException("Timeslots cannot contain a null timeslot");

        if (timeslotsPerMatch < 1)
            throw new IllegalArgumentException("Number of timeslots per match cannot be less than 1");

        if (matchesPerPlayer < 1)
            throw new IllegalArgumentException("Number of matches per player cannot be less than 1");

        if (new HashSet<>(timeslots).size() < timeslots.size())
            throw new IllegalArgumentException("Timeslots cannot contain duplicates");

        timeslots.sort(Collections.reverseOrder());

        // La ordenación anterior no garantiza que haya timeslots con el mismo orden; de este modo esto se puede
        // controlar porque en un TreeSet no habrá más de un elemento con el mismo orden
        if (new TreeSet<>(timeslots).size() != timeslots.size())
            throw new IllegalArgumentException("Every timeslot must strictly precede the following");

        this.name = name;
        this.players = new ArrayList<>(players);
        this.localizations = new ArrayList<>(localizations);
        this.timeslots = new ArrayList<>(timeslots);

        nMatchesPerPlayer = matchesPerPlayer;
        nTimeslotsPerMatch = timeslotsPerMatch;
        nPlayersPerMatch = playersPerMatch;
    }

    public String getName() {
        return name;
    }

    /**
     * Asigna un nombre a este evento.
     *
     * @param name nuevo nombre que se le quiere asignar al evento
     * @throws NullPointerException si el nombre es <code>null</code>
     */
    public void setName(String name) {
        Objects.requireNonNull(name);

        this.name = name;
    }

    public Tournament getTournament() {
        return tournament;
    }

    protected void setTournament(Tournament tournament) {
        Objects.requireNonNull(tournament);

        this.tournament = tournament;
    }

    /**
     * Devuelve la lista de jugadores como una lista no modificable
     *
     * @return la lista de jugadores envuelta en un wrapper que la hace no modificable
     */
    public List<Player> getPlayers() {
        return Collections.unmodifiableList(players);
    }

    /**
     * Devuelve la lista de localizaciones como una lista no modificable
     *
     * @return la lista de localizaciones envuelta en un wrapper que la hace no modificable
     */
    public List<Localization> getLocalizations() {
        return Collections.unmodifiableList(localizations);
    }

    /**
     * Devuelve la lista de timeslots como una lista no modificable
     *
     * @return la lista de timeslots envuelta en un wrapper que la hace no modificable
     */
    public List<Timeslot> getTimeslots() {
        return Collections.unmodifiableList(timeslots);
    }

    public int getMatchesPerPlayer() {
        return nMatchesPerPlayer;
    }

    /**
     * Asigna el número de partidos por jugador. Además, elimina todos los emparejamientos predefinidos, si hubiese.
     * <p>
     * Si el número de partidos por jugador asignado es 1, el modo de emparejamiento automáticamente cambia a
     * {@link MatchupMode#ANY}.
     * <p>
     * Si el número de jugadores por partido que se intenta asignar es el mismo que el que ya está configurado, no se
     * producen ninguno de los cambios.
     *
     * @param matchesPerPlayer número de partidos por jugador, mayor o igual que 1
     * @throws IllegalArgumentException si el número de partidos por jugador es menor que 1
     */
    public void setMatchesPerPlayer(int matchesPerPlayer) {
        if (matchesPerPlayer < 1)
            throw new IllegalArgumentException("Number of matches per player cannot be less than 1");

        if (matchesPerPlayer == nMatchesPerPlayer)
            return;

        predefinedMatchups.clear();

        if (matchesPerPlayer == 1)
            matchupMode = MatchupMode.ANY;

        nMatchesPerPlayer = matchesPerPlayer;

        setChanged();
    }

    public int getTimeslotsPerMatch() {
        return nTimeslotsPerMatch;
    }

    /**
     * Asigna la duración de un partido, es decir, el número de <i>timeslots</i> sobre los que los partidos del
     * envento transcurren. Además, elimina todos los emparejamientos predefinidos, si hubiese.
     * <p>
     * Si el número de <i>timeslots</i> por partido que se quiere asignar es el mismo que el valor que ya está
     * configurado, no habrá ninguna modificación.
     *
     * @param timeslotsPerMatch número de timeslots por partidos, mayor o igual que 1
     * @throws IllegalArgumentException si el número de <i>timeslots</i> por partido es menor que 1
     */
    public void setTimeslotsPerMatch(int timeslotsPerMatch) {
        if (timeslotsPerMatch < 1)
            throw new IllegalArgumentException("Number of timeslots per match cannot be less than 1");

        if (timeslotsPerMatch == nTimeslotsPerMatch)
            return;

        predefinedMatchups.clear();

        nTimeslotsPerMatch = timeslotsPerMatch;

        setChanged();
    }

    public int getPlayersPerMatch() {
        return nPlayersPerMatch;
    }

    /**
     * Asigna el número de jugadores por partido. Además, elimina todos los equipos existentes así como los
     * emparejamientos predefinidos, si hubiese. Como se eliminan los equipos, el número de jugadores por equipo
     * vuelve a ser 0.
     * <p>
     * Si el número de jugadores por partido es 1, el modo de emparejamiento cambia automáticamente al valor por
     * defecto, {@link MatchupMode#ANY}.
     * <p>
     * Si el número de jugadores por partido tiene un valor igual al existente, no se produce ninguna de las
     * modificaciones.
     *
     * @param playersPerMatch número de jugadores por partido, mayor o igual que 1
     * @throws IllegalArgumentException si el número de jugadores por partido es menor que 1
     * @throws IllegalArgumentException si el número de jugadores por partido no es coherente con el número de
     *                                  jugadores que el evento tiene (es decir, debe ser divisor de este valor)
     * @throws IllegalArgumentException si el número de jugadores por partido no es coherente con el número de
     *                                  jugadores por equipo que el evento define (si es un evento por equipos, es
     *                                  decir, éste es distinto de 0), o sea, debe ser múltiplo del mismo
     */
    public void setPlayersPerMatch(int playersPerMatch) {
        if (playersPerMatch < 1)
            throw new IllegalArgumentException("Number of players per match cannot be less than 1");

        if (players.size() % playersPerMatch != 0)
            throw new IllegalArgumentException(String.format(
                    "Number of players per match is not coherent to the number of players this event has (%d)",
                    players.size()
            ));

        if (nPlayersPerTeam != 0 && playersPerMatch % nPlayersPerTeam != 0)
            throw new IllegalArgumentException(String.format("Number of players per match must be coherent to the " +
                    "number of players per team in this event (must be multiple of %d)", nPlayersPerTeam));

        if (playersPerMatch == nPlayersPerMatch)
            return;

        predefinedMatchups.clear();
        clearTeams();

        if (playersPerMatch == 1)
            matchupMode = MatchupMode.ANY;

        nPlayersPerMatch = playersPerMatch;

        setChanged();
    }

    public int getPlayersPerTeam() {
        return nPlayersPerTeam;
    }

    /**
     * Asigna el número de jugadores por equipo que tendrá el evento, y por ende, convierte este evento a un evento
     * por equipos, de forma que la composición los horarios y los partidos resultantes será diferente. El cálculo
     * del horario por parte de {@link es.uca.garciachacon.eventscheduler.solver.TournamentSolver}, no obstante, no
     * variará.
     * <p>
     * El número de jugadores por equipo es, por defecto, 0, lo que indica que el evento no incluye equipos. No
     * obstante, este valor es actualizado por métodos que añaden equipos al evento como {@link Event#setTeams(List)}
     * y {@link Event#addTeam(Team)}.
     * <p>
     * Si existen equipos y se invoca este método, si el número de jugadores por equipo que el valor del argumento
     * <code>playersPerTeam</code> contiene es distinto al número de jugadores por equipo actual del evento, se
     * actualizará el valor y además se vaciará la lista de equipos, eliminándose cualquier configuración anterior de
     * los mismos.
     * <p>
     * El número de jugadores por equipo debe tener un valor mayor o igual que 2. Si se pretende eliminar la
     * propiedad de este evento como evento por equipos pasándole a este método el argumento 0, no se consideraría
     * una operación válida y se lanzaría la correspondiente excepción. Para llevar a cabo esta operación, se puede
     * hacer uso del método {@link Event#clearTeams()} para eliminar todos los equipos y hacer que el evento deje de
     * ser por equipos, o bien eliminar sucesivamente cada equipo existente con {@link Event#removeTeam(Team)} hasta
     * que no quede ninguno (si hubiera algún equipo).
     * <p>
     * Si el número de jugadores por equipo que se intenta configurar es igual al ya existente, no se producirá
     * ningún cambio.
     *
     * @param playersPerTeam número de jugadores por equipo a definir sobre este evento, mayor o igual que 2
     * @throws IllegalArgumentException si <code>playersPerTeam</code> es menor que 2
     * @throws IllegalArgumentException si el número de jugadores por equipo no es coherente con el número de
     *                                  jugadores por partido eque el evento define, es decir, debe ser divisor de
     *                                  este último
     */
    public void setPlayersPerTeam(int playersPerTeam) {
        if (playersPerTeam < 2)
            throw new IllegalArgumentException("Players per team cannot be less than 2");

        if (nPlayersPerMatch % playersPerTeam != 0)
            throw new IllegalArgumentException(String.format("Number of players per team must be coherent to the " +
                    "number of players per match in this event (must be divisor of %d)", nPlayersPerMatch));

        if (playersPerTeam == nPlayersPerTeam)
            return;

        teams.clear();

        nPlayersPerTeam = playersPerTeam;

        setChanged();
    }

    /**
     * Devuelve la lista de equipos del torneo envuelta en una lista no modificable
     *
     * @return la lista de equipos del torneo, no modificable
     */
    public List<Team> getTeams() {
        return Collections.unmodifiableList(teams);
    }

    /**
     * Asigna la lista de equipos que componen el evento. Si para alguno de los equipos de la lista se da alguna de
     * las situaciones que conllevarían el lanzamiento de una excepción como describe {@link Event#addTeam(Team)},
     * este método también las lanzará ya que invoca sucesivamente el método anterior para añadir los equipos a este
     * evento.
     *
     * @param teams lista de equipos que se desean añadir al evento
     * @throws NullPointerException si <code>teams</code> es <code>null</code>
     */
    public void setTeams(List<Team> teams) {
        Objects.requireNonNull(teams);

        teams.forEach(this::addTeam);
    }

    /**
     * Añade un equipo a este evento. Si no existían equipos, se empieza a considerar este evento como evento por
     * equipos y se asigna como número de jugadores por equipo del evento el número de jugadores que el equipo que se
     * añade tenga.
     *
     * @param team un equipo válido para este evento
     * @throws NullPointerException     si <code>team</code> es <code>null</code>
     * @throws IllegalArgumentException si el equipo contiene algún jugador que no pertenece a este evento
     * @throws IllegalArgumentException si el equipo contiene algún jugador que ya pertenece a algún otro equipo del
     *                                  evento
     * @throws IllegalArgumentException si el número de jugadores del equipo no es coherente con el número de
     *                                  jugadores por equipo que este evento define, es decir, debe ser divisor de
     *                                  este último
     * @throws IllegalArgumentException si el número de jugadores del equipo es distinto del número de jugadores por
     *                                  equipo que este evento define (si ya se ha definido)
     */
    public void addTeam(Team team) {
        Objects.requireNonNull(team);

        if (!players.containsAll(team.getPlayers()))
            throw new IllegalArgumentException("Players unknown to this event are contained in the team");

        if (teams.stream().map(Team::getPlayers).flatMap(Collection::stream).anyMatch(team.getPlayers()::contains))
            throw new IllegalArgumentException("A player already belongs to an existing team");

        int playersInTeam = team.getPlayers().size();

        if (nPlayersPerTeam == 0) {
            if (nPlayersPerMatch % playersInTeam != 0)
                throw new IllegalArgumentException(String.format("The number of players in this team (%d) is not " +
                        "coherent to the number of players per match " +
                        "in this event (must be a divisor of %d)", playersInTeam, nPlayersPerMatch));

            nPlayersPerTeam = playersInTeam;

        } else if (playersInTeam != nPlayersPerTeam)
            throw new IllegalArgumentException(String.format(
                    "The number of players in this team (%d) is not the same than the number this event defines (%d)",
                    playersInTeam,
                    nPlayersPerTeam
            ));

        team.setEvent(this);

        teams.add(team);

        setChanged();
    }

    /**
     * Añade un equipo compuesto por los jugadores indicados. Internamente invoca a {@link Event#addTeam(Team)},
     * luego se lanzarán las excepciones descritas en el mismo si se da el caso.
     *
     * @param teamPlayers jugadores que compondrán el nuevo equipo a añadir
     * @throws NullPointerException si <code>teamPlayers</code> es <code>null</code>
     */
    public void addTeam(Player... teamPlayers) {
        Objects.requireNonNull(teamPlayers);

        addTeam(new Team(teamPlayers));
    }

    /**
     * Elimina un equipo de la lista de equipos, si existe. Si el equipo eliminado es el último del evento, éste ya
     * dejará de considerarse como un evento por equipos. Si el equipo no existe, no habrá modificaciones.
     *
     * @param team un equipo de jugadores que se desea eliminar del evento
     */
    public void removeTeam(Team team) {
        if (teams.remove(team)) {
            if (teams.isEmpty())
                nPlayersPerTeam = 0;

            setChanged();
        }
    }

    /**
     * Vacía el conjunto de equipos definidos sobre el evento, eliminando todos los equipos. Este evento, por tanto,
     * ya no será un evento por equipos, por lo que el número de jugadores por equipo vuelve a ser 0, representando
     * su estado inicial de evento sin equipos.
     */
    public void clearTeams() {
        nPlayersPerTeam = 0;

        if (!teams.isEmpty()) {
            teams.clear();

            setChanged();
        }
    }

    /**
     * Comprueba si este es un evento por equipos o no. Este estado lo define el valor del número de jugadores por
     * equipo que el evento tiene. Este valor es por defecto 0, e indica que no hay equipos, mientras que si el
     * evento es por equipos su valor será mayor o igual que 2.
     *
     * @return <code>true</code> si el evento es por equipos, y <code>false</code> si no lo es
     */
    public boolean hasTeams() {
        return nPlayersPerTeam >= 2;
    }

    /**
     * Comprueba si este evento incluye algún equipo predefinido; estos son los equipos cuya composición de jugadores
     * en particular es desconocida. Si hay al menos un equipo predefinido, el evento es por equipos y el método
     * {@link Event#hasTeams()} devolverá <code>true</code>, así como este. No obstante, si no hay ningún equipo
     * predefinido este método devolverá <code>false</code>, pero se deberá consultar mediante el método
     * {@link Event#hasTeams()} si el evento es por equipos o no, o alternativamente, comprobar el valor de retorno
     * de {@link Event#getPlayersPerTeam()}.
     *
     * @return <code>true</code> si este evento tiene al menos un equipo predefinido, y <code>false</code> si no
     * incluye ninguno (pero esto no quiere decir que el evento no sea por equipos, puede serlo o puede no serlo)
     */
    public boolean hasPredefinedTeams() {
        return !teams.isEmpty();
    }

    /**
     * Devuelve el diccionario de jugadores y horas no disponibles en un wrapper que la hace no modificable
     *
     * @return el diccionario de jugadores y sus horas no disponibles, no modificable
     */
    public Map<Player, Set<Timeslot>> getUnavailablePlayers() {
        return Collections.unmodifiableMap(unavailablePlayers);
    }

    /**
     * Asigna el diccionario que define las horas en las que los jugadores no están disponibles. Es opcional, y no
     * todos los jugadores tienen por qué definir un conjunto de horas en las que no están disponibles.
     * <p>
     * Este método invoca internamente a {@link Event#addUnavailablePlayerAtTimeslots(Player, Set)}, y éste, a su
     * vez, invoca a {@link Event#addUnavailablePlayerAtTimeslot(Player, Timeslot)}, por tanto, se lanzarán las
     * excepciones que en ambos se indican, si se diera el caso.
     *
     * @param unavailability un diccionario que define sobre cada jugador un conjunto de horas en las que no está
     *                       disponible
     * @throws NullPointerException si el argumento es <code>null</code>
     */
    public void setUnavailablePlayers(Map<Player, Set<Timeslot>> unavailability) {
        Objects.requireNonNull(unavailability);

        unavailability.forEach(this::addUnavailablePlayerAtTimeslots);
    }

    /**
     * Marca al jugador como no disponible en una serie de horas. Si el conjunto de <i>timeslots</i> está vacío, se
     * ignorará la operación.
     * <p>
     * Este método hace uso de {@link Event#addUnavailablePlayerAtTimeslot(Player, Timeslot)},
     * luego se lanzarán las excepciones correspondientes si se da la situación ilegal.
     *
     * @param player    jugador del evento que no está disponible a una serie de horas determinadas
     * @param timeslots conjunto no vacío de horas, y todas ellas pertenecientes al dominio del evento
     * @throws NullPointerException si el jugador es <code>null</code>
     * @throws NullPointerException si el conjunto de <i>timeslots</i> es <code>null</code>
     */
    public void addUnavailablePlayerAtTimeslots(Player player, Set<Timeslot> timeslots) {
        Objects.requireNonNull(player);
        Objects.requireNonNull(timeslots);

        timeslots.forEach(t -> addUnavailablePlayerAtTimeslot(player, t));
    }

    /**
     * Marca al jugador como no disponible a una hora determinada. Si ya estaba marcado, no se producen modificaciones.
     *
     * @param player   jugador del evento que no está disponible a una hora determinada
     * @param timeslot <i>timeslot</i> del evento al que el jugador indicado no está disponible
     * @throws NullPointerException     si el jugador es <code>null</code>
     * @throws NullPointerException     si el <i>timeslot</i> es <code>null</code>
     * @throws IllegalArgumentException si el jugador no existe en el conjunto de jugadores del evento
     * @throws IllegalArgumentException si el <i>timeslot</i> no existe en el conjunto de <i>timeslots</i> del evento
     */
    public void addUnavailablePlayerAtTimeslot(Player player, Timeslot timeslot) {
        Objects.requireNonNull(player);
        Objects.requireNonNull(timeslot);

        if (!players.contains(player))
            throw new IllegalArgumentException("Player does not exist in this event");

        if (!timeslots.contains(timeslot))
            throw new IllegalArgumentException("Timeslot does not exist in this event");

        unavailablePlayers.computeIfAbsent(player, t -> new HashSet<>()).add(timeslot);

        setChanged();
    }

    /**
     * Marca al jugador como no disponible en el rango de horas indicado, extremos incluidos. El rango está compuesto
     * por los <i>timeslots</i> del evento que se encuentren entre los extremos, es decir, que los órdenes
     * cronológicos de estos estén en el rango.
     * <p>
     * Este método hace uso de {@link Event#addUnavailablePlayerAtTimeslot(Player, Timeslot)} internamente, luego si
     * se da una circunstancia ilegal se lanzarán las exepciones que este método describe.
     *
     * @param player jugador perteneciente al dominio del evento
     * @param t1     un extremo del rango de <i>timeslots</i>
     * @param t2     el otro extremo
     * @throws IllegalArgumentException si alguno de los argumentos son <code>null</code> o no existen en el dominio
     *                                  del evento
     */
    public void addUnavailablePlayerAtTimeslotRange(Player player, Timeslot t1, Timeslot t2) {
        addUnavailablePlayerAtTimeslot(player, t1);
        addUnavailablePlayerAtTimeslot(player, t2);

        Timeslot start, end;
        if (t1.compareTo(t2) >= 0) {
            start = t1;
            end = t2;
        } else {
            start = t2;
            end = t1;
        }

        // No se incluye en el bucle t1 ni t2 (el comienzo y el final) porque ya se añadieron al principio de este
        // método; la razón por la que se hiciera esto es aprovecharse de las precondiciones del otro método
        for (int t = timeslots.indexOf(start) + 1; t < timeslots.indexOf(end); t++)
            addUnavailablePlayerAtTimeslot(player, timeslots.get(t));
    }

    /**
     * Elimina la no disponibilidad de un jugador a una hora determinada. Si el jugador en primer lugar no estaba
     * disponible a esa hora, no ocurre ninguna modificación.
     *
     * @param player   jugador que pertenece a este evento
     * @param timeslot hora perteneciente al dominio del evento
     */
    public void removeUnavailablePlayerAtTimeslot(Player player, Timeslot timeslot) {
        // Elimina el timeslot asociado al jugador, y si el conjunto queda vacío, se elimina la entrada del diccionario
        if (unavailablePlayers.computeIfPresent(player, (p, t) -> t.remove(timeslot) && t.isEmpty() ? null : t) == null)
            setChanged();
    }

    /**
     * Vacía el diccionario de jugadores no disponibles en <i>timeslots</i>
     */
    public void clearUnavailablePlayers() {
        if (!unavailablePlayers.isEmpty()) {
            unavailablePlayers.clear();

            setChanged();
        }
    }

    /**
     * Comprueba si hay jugadores no disponibles a ciertas horas
     *
     * @return <code>true</code> si hay algún jugador no disponible a algunas horas, de lo contrario <code>false</code>
     */
    public boolean hasUnavailablePlayers() {
        return !unavailablePlayers.isEmpty();
    }

    /**
     * Devuelve la lista de emparejamientos fijos envuelta en un wrapper que la hace no modificable
     *
     * @return la lista no modificable de emparejamientos fijos del evento
     */
    public Set<Matchup> getPredefinedMatchups() {
        return Collections.unmodifiableSet(predefinedMatchups);
    }

    /**
     * Asigna el conjunto de enfrentamientos predefinidos entre jugadores del evento. Además, actualiza las horas de
     * juego asignadas a los jugadores y las localizaciones de juego asginadas a los mismos.
     * <p>
     * Si el evento especifica más de un partido por jugador, para cada jugador que compone cada enfrentamiento, si
     * al jugador no se le han asignado localizaciones de juego u horas de juego concretas donde sus partidos deban
     * tener lugar, se le asignarán todas las localizaciones y/u horas de juego existentes en los dominios del evento.
     *
     * @param matchups una lista de múltiples enfrentamientos no repetidos entre jugadores del evento
     * @throws NullPointerException si <code>matchups</code> es <code>null</code>
     */
    public void setPredefinedMatchups(Set<Matchup> matchups) {
        Objects.requireNonNull(matchups);

        matchups.forEach(this::addMatchup);
    }

    /**
     * Añade un enfrentamiento predefinido.
     * <p>
     * Actualiza la asignación de localizaciones y horas de juego para los jugadores implicados en el
     * enfrentamiento, asociando a cada uno de ellos con dichas localizaciones y horas que el enfrentamiento
     * especifica como posibles.
     * <p>
     * Si el evento especifica más de un partido por jugador, para cada jugador que compone el enfrentamiento, si al
     * jugador no se le han asignado localizaciones de juego u horas de juego concretas donde sus partidos deban tener
     * lugar, se le asignarán todas las localizaciones y/u horas de juego existentes en los dominios del evento.
     * <p>
     * Si el enfrentamiento no tiene ninguna localización o <i>timeslot</i> definidos donde deba transcurrir, se
     * asignarán automáticamente los disponibles (si el evento tiene más de un partido por jugador).
     *
     * @param matchup emparejamiento no <code>null</code> a añadir a los emparejamientos predefinidos del evento
     * @throws NullPointerException     si <code>matchup</code> es <code>null</code>
     * @throws IllegalArgumentException si el conjunto de jugadores de enfrentamiento tiene un número de jugadores
     *                                  distinto del número de jugadores por partido que especifica el evento, o si
     *                                  alguno de éstos es no pertenece al evento
     * @throws IllegalArgumentException si alguna de las localizaciones no pertenece al evento
     * @throws IllegalArgumentException si algún <i>timeslots</i> del enfrentamiento no pertenece al evento, o indica
     *                                  un posible comienzo del enfrentamiento fuera de rango
     * @throws IllegalArgumentException si se superase el número máximo de partidos que un jugar en particular pueda
     *                                  jugar
     */
    public void addMatchup(Matchup matchup) {
        Objects.requireNonNull(matchup);

        if (matchup.getPlayers().size() != nPlayersPerMatch)
            throw new IllegalArgumentException(String.format("Players cannot contain a number of players (%d) " +
                            "different than the number of players per match the event specifies (%d)",
                    matchup.getPlayers().size(),
                    nPlayersPerMatch
            ));

        if (!players.containsAll(matchup.getPlayers()))
            throw new IllegalArgumentException("Nonexisting players are contained in the matchup");

        if (!localizations.containsAll(matchup.getLocalizations()))
            throw new IllegalArgumentException("Nonexisting localizations are contained in the matchup");

        if (matchup.getTimeslots().stream().anyMatch(t -> timeslots.indexOf(t) + nTimeslotsPerMatch > timeslots.size()))
            throw new IllegalArgumentException("Starting timeslot out of range");

        if (!timeslots.containsAll(matchup.getTimeslots()))
            throw new IllegalArgumentException("Nonexisting timeslots are contained in the matchup");

        for (Player player : matchup.getPlayers()) {
            long playerAssignedMatchups = predefinedMatchups.stream()
                    .filter(m -> m.getPlayers().contains(player))
                    .mapToInt(Matchup::getOccurrences)
                    .sum() + matchup.getOccurrences();

            if (playerAssignedMatchups > nMatchesPerPlayer)
                throw new IllegalArgumentException(String.format(
                        "Player's (%s) number of predefined matchups (%d) would exceed the limit (%d)",
                        player,
                        playerAssignedMatchups,
                        nMatchesPerPlayer
                ));
        }

        predefinedMatchups.add(matchup);

        for (Player player : matchup.getPlayers()) {
            Set<Localization> assignedLocalizations = matchup.getLocalizations();
            Set<Timeslot> assignedTimeslots = matchup.getTimeslots();

            if (nMatchesPerPlayer > 1) {
                if (!playersInLocalizations.containsKey(player) ||
                        (!playersInLocalizations.containsKey(player) && assignedLocalizations.isEmpty()))
                    assignedLocalizations = new HashSet<>(localizations);

                if (!playersAtTimeslots.containsKey(player) ||
                        (!playersAtTimeslots.containsKey(player) && assignedTimeslots.isEmpty()))
                    assignedTimeslots = new HashSet<>(timeslots.subList(0, timeslots.size() - nTimeslotsPerMatch + 1));
            }

            if (!assignedLocalizations.isEmpty())
                playersInLocalizations.computeIfAbsent(player, l -> new HashSet<>()).addAll(assignedLocalizations);

            if (!assignedTimeslots.isEmpty())
                playersAtTimeslots.computeIfAbsent(player, l -> new HashSet<>()).addAll(assignedTimeslots);
        }

        setChanged();
    }

    /**
     * Añade un enfrentamiento predefinido que ocurrirá una vez, entre los jugadores indicados, en cualquiera de las
     * localizaciones y cualquiera de los <i>timeslots</i> del evento si no tiene ninguna localización y/u hora
     * asignada, o en alguna de la suma de las asignadas para cada jugador, si tienen.
     * <p>
     * Actualiza la asignación a jugadores de localizaciones y horas de juego del mismo modo que describe el
     * método {@link Event#addMatchup(Matchup)}.
     *
     * @param players conjunto de jugadores entre los que ocurrirá un enfrentamiento predefinido
     */
    public void addMatchup(Set<Player> players) {
        addMatchup(new Matchup(players));
    }

    /**
     * Añade un enfrentamiento predefinido que ocurrirá una vez, entre los jugadores indicados, en cualquiera de las
     * localizaciones y cualquier de los <i>timeslots</i> del evento si no tiene ninguna localización y/u hora
     * asignada, o en alguna de la suma de las asignadas, si tienen.
     * <p>
     * Actualiza la asignación a jugadores de localizaciones y horas de juego del mismo modo que describe el
     * método {@link Event#addMatchup(Matchup)}.
     *
     * @param players jugadores entre los que tendrá lugar el enfrentamiento
     */
    public void addMatchup(Player... players) {
        addMatchup(new Matchup(new HashSet<>(Arrays.asList(players))));
    }

    /**
     * Añade un enfrentamiento predefinido que ocurrirá una vez, entre los jugadores de los equipos indicados, en
     * cualquiera de las localizaciones y cualquiera de los <i>timeslots</i> del evento si no se tiene ninguna
     * localización y/u hora asignada, o en alguna de la suma de las asignadas a cada jugador, si tienen.
     * <p>
     * Actualiza la asignación a jugadores de localizaciones y horas de juego del mismo modo que describe el
     * método {@link Event#addMatchup(Matchup)}.
     *
     * @param teams conjunto de equipos entre los que ocurrirá un enfrentamiento predefinido
     */
    public void addTeamMatchup(Set<Team> teams) {
        addMatchup(new Matchup(teams.stream()
                .map(Team::getPlayers)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet())));
    }

    /**
     * Elimina un enfrentamiento predefinido. Si el enfrentamiento no existe, no se produce ninguna modificación. Las
     * asginaciones de localizaciones y horas de juego permanecerán, así que su eliminación debe ser ejecutada
     * explícitamente.
     *
     * @param matchup un conjunto de jugadores a eliminar de la lista, si existe
     */
    public void removeMatchup(Matchup matchup) {
        if (predefinedMatchups.remove(matchup))
            setChanged();
    }

    /**
     * Define todos los enfrentamientos predefinidos entre los jugadores indicados, si existe alguno. Las
     * asginaciones de localizaciones y horas de juego permanecerán, así que su eliminación debe ser ejecutada
     * explícitamente.
     *
     * @param players jugadores para los que se borrarán los enfrentamientos predefinidos entre sí
     */
    public void removeMatchup(Set<Player> players) {
        if (predefinedMatchups.removeIf(m -> m.getPlayers().equals(players)))
            setChanged();
    }

    /**
     * Elimina un enfrentamiento fijo entre equipos. Si el enfrentamiento no existe, no se produce ninguna
     * modificación. Las asginaciones de localizaciones y horas de juego permanecerán, así que su eliminación debe
     * ser ejecutada explícitamente.
     *
     * @param teams un conjunto de equipos cuyo enfrentamiento se eliminará de la lista, si existe
     */
    public void removeTeamsMatchup(Set<Team> teams) {
        if (teams != null)
            removeMatchup(teams.stream().map(Team::getPlayers).flatMap(Collection::stream).collect(Collectors.toSet()));
    }

    /**
     * Limpia el conjunto de enfrentamientos predefinidos.
     */
    public void clearPredefinedMatchups() {
        if (!predefinedMatchups.isEmpty()) {
            predefinedMatchups.clear();

            setChanged();
        }
    }

    /**
     * Comprueba si este evento tiene emparejamientos predefinidos
     *
     * @return true si sobre el evento se han definido enfrentamientos fijos predefinidos, y false si no
     */
    public boolean hasPredefinedMatchups() { return !predefinedMatchups.isEmpty(); }

    /**
     * Devuelve la lista no modificable de horas del evento que representan un break o descanso
     *
     * @return lista de horas del evento envuelta en un wrapper que la hace no modificable
     */
    public List<Timeslot> getBreaks() {
        return Collections.unmodifiableList(breaks);
    }

    /**
     * Asigna la lista de <i>breaks</i>, es decir, aquellos <i>timeslots</i> en los que no puede transcurrir un
     * partido. Se ignorarán los <i>breaks</i> repetidos de la lista, solamente se añadirán una vez.
     * <p>
     * Este método utiliza internamente {@link Event#addBreak(Timeslot)}, luego se lanzarán las excepciones
     * correspondientes si se da alguna de las situaciones que este último método describe.
     *
     * @param breaks lista de <i>timeslots</i> que serán marcados como <i>breaks</i>
     * @throws NullPointerException si la lista de <i>breaks</i> es <code>null</code>
     */
    public void setBreaks(List<Timeslot> breaks) {
        Objects.requireNonNull(breaks);

        breaks.forEach(this::addBreak);
    }

    /**
     * Añade un <i>timeslot</i> a la lista de <i>breaks</i>. Si ya existe, no habrá modificaciones. Si se añade, se
     * ordenará de forma ascendente la lista.
     *
     * @param timeslotBreak una hora del evento que no exista ya en la lista de breaks
     * @throws NullPointerException     si el <i>break</i> es <code>null</code>
     * @throws IllegalArgumentException si el <i>timeslot</i> no existe en este evento
     */
    public void addBreak(Timeslot timeslotBreak) {
        Objects.requireNonNull(timeslotBreak);

        if (!timeslots.contains(timeslotBreak))
            throw new IllegalArgumentException("Timeslot does not exist in this event");

        if (!breaks.contains(timeslotBreak)) {
            breaks.add(timeslotBreak);
            breaks.sort(Comparator.reverseOrder());

            setChanged();
        }
    }

    /**
     * Añade <i>breaks</i> en el rango indicado, extremos incluidos.
     * <p>
     * Este método utiliza internamente {@link Event#addBreak(Timeslot)}, luego se lanzarán las excepciones
     * correspondientes si se da alguna de las situaciones que este último método describe.
     *
     * @param t1 un extremo del rango
     * @param t2 el otro extremo del rango
     */
    public void addBreakRange(Timeslot t1, Timeslot t2) {
        addBreak(t1);
        addBreak(t2);

        Timeslot start, end;
        if (t1.compareTo(t2) >= 0) {
            start = t1;
            end = t2;
        } else {
            start = t2;
            end = t1;
        }

        for (int t = timeslots.indexOf(start) + 1; t < timeslots.indexOf(end); t++)
            addBreak(timeslots.get(t));
    }


    /**
     * Elimina un <i>break</i>, es decir, la hora se considerará como una hora regular de juego. Si la hora no existe
     * en la lista de <i>breaks</i> o no existe en el evento, no habrá modificaciones.
     *
     * @param timeslotBreak una hora del evento
     */
    public void removeBreak(Timeslot timeslotBreak) {
        if (breaks.remove(timeslotBreak))
            setChanged();
    }

    /**
     * Limpia la lista de <i>timeslots</i> que son descansos o <i>breaks</i>.
     */
    public void clearBreaks() {
        if (!breaks.isEmpty()) {
            breaks.clear();

            setChanged();
        }
    }

    /**
     * Comprueba si un <i>timeslot</i> se corresponde con un <i>timeslot</i> perteneciente al evento y que ha sido
     * marcado como <i>break</i>, es decir, que pertenece a la lista de <i>breaks</i>.
     *
     * @param timeslot una hora de juego
     * @return <code>true</code> si es un <i>break</i> del evento, <code>false</code> si no
     */
    public boolean isBreak(Timeslot timeslot) {
        return breaks.contains(timeslot);
    }

    /**
     * Comprueba si este evento tiene <i>breaks</i> definidos.
     *
     * @return <code>true</code> si tiene <i>breaks</i> o <code>false</code> si no
     */
    public boolean hasBreaks() {
        return !breaks.isEmpty();
    }

    /**
     * Devuelve el mapa de localizaciones no disponibles a las horas especificadas.
     *
     * @return diccionario no modificable de localizaciones no disponibles a determinadas horas
     */
    public Map<Localization, Set<Timeslot>> getUnavailableLocalizations() {
        return Collections.unmodifiableMap(unavailableLocalizations);
    }

    /**
     * Asigna las horas a las que determinadas localizaciones de juego no están disponibles para que un partido
     * discurra sobre ellas.
     * <p>
     * Este método hace uso de {@link Event#addUnavailableLocalizationAtTimeslots(Localization, Set)} para cada
     * localización y <i>timeslots</i> no disponibles asignados, luego se lanzarán las excepciones pertinentes si se
     * da una situación ilegal que ese método describa.
     *
     * @param unavailableLocalizations diccionario de localizaciones y las horas a las que no están disponibles
     * @throws NullPointerException si el diccionario es <code>null</code>
     */
    public void setUnavailableLocalizations(Map<Localization, Set<Timeslot>> unavailableLocalizations) {
        Objects.requireNonNull(unavailableLocalizations);

        unavailableLocalizations.forEach(this::addUnavailableLocalizationAtTimeslots);
    }

    /**
     * Marca como inválida o no disponible una localización de juego a un conjunto de horas determinado.
     * <p>
     * Este método hace uso de {@link Event#addUnavailableLocalizationAtTimeslot(Localization, Timeslot)} para cada
     * <i>timeslot</i> que se quiere marcar como no disponible para la localización indicada. Si se da una situación
     * ilegal para la que se lanza una excepción como este mencionado método describe, se lanzará también desde este
     * método si se da el caso.
     *
     * @param localization localización perteneciente al conjunto de localizaciones del evento
     * @param timeslots    conjunto de horas pertenecientes al dominio del evento y no existentes en el conjunto de
     *                     horas no
     *                     disponibles de la localización
     * @throws NullPointerException si <code>localization</code> es <code>null</code>
     * @throws NullPointerException si <code>timeslots</code> es <code>null</code>
     */
    public void addUnavailableLocalizationAtTimeslots(Localization localization, Set<Timeslot> timeslots) {
        Objects.requireNonNull(localization);
        Objects.requireNonNull(timeslots);

        timeslots.forEach(t -> addUnavailableLocalizationAtTimeslot(localization, t));
    }

    /**
     * Marca como inválida o no disponible una localización de juego a una hora determinada. Si ya estaba marcada, no
     * hay modificaciones.
     *
     * @param localization localización perteneciente al conjunto de localizaciones del evento
     * @param timeslot     hora perteneciente al conjunto de horas en las que el evento discurre
     * @throws NullPointerException     si alguno de los argumentos es <code>null</code>
     * @throws IllegalArgumentException si la localización no existe en el evento
     * @throws IllegalArgumentException si el <i>timeslot</i> no existe en el evento
     */
    public void addUnavailableLocalizationAtTimeslot(Localization localization, Timeslot timeslot) {
        Objects.requireNonNull(localization);
        Objects.requireNonNull(timeslot);

        if (!localizations.contains(localization))
            throw new IllegalArgumentException("Localization does not exist in this event");

        if (!timeslots.contains(timeslot))
            throw new IllegalArgumentException("Timeslot does not exist in this event");

        unavailableLocalizations.computeIfAbsent(localization, t -> new HashSet<>()).add(timeslot);

        setChanged();
    }

    /**
     * Marca como no disponibles las localizaciones en el rango de horas de juego indicadas, extremos incluidos.
     * <p>
     * Este método invoca {@link Event#addUnavailableLocalizationAtTimeslot(Localization, Timeslot)} para cada
     * extremo y para los <i>timeslots</i> que éstos delimitan en el rango. Por tanto, se lanzarán las excepciones
     * que este último método describe si se diera el caso.
     *
     * @param localization localización de juego del evento
     * @param t1           un extremo del rango de horas
     * @param t2           el otro extremo del rango de horas
     */
    public void addUnavailableLocalizationAtTimeslotRange(Localization localization, Timeslot t1, Timeslot t2) {
        addUnavailableLocalizationAtTimeslot(localization, t1);
        addUnavailableLocalizationAtTimeslot(localization, t2);

        Timeslot start, end;
        if (t1.compareTo(t2) >= 0) {
            start = t1;
            end = t2;
        } else {
            start = t2;
            end = t1;
        }

        for (int t = timeslots.indexOf(start) + 1; t < timeslots.indexOf(end); t++)
            addUnavailableLocalizationAtTimeslot(localization, timeslots.get(t));
    }

    /**
     * Elimina la invalidez de una localización, si la hubiese, volviendo a estar disponible a cualquier hora. Si la
     * localización no estuviese no disponible a ninguna hora, no habrá modificaciones.
     *
     * @param localization localización perteneciente al conjunto de localizaciones del evento
     */
    public void removeUnavailableLocalization(Localization localization) {
        if (unavailableLocalizations.remove(localization) != null)
            setChanged();
    }

    /**
     * Elimina la invalidez de una localización a una hora, si la hubiese, volviendo a estar disponible a esa hora.
     * Si la hora no estaba disponible para esa pista, no habrá modificaciones. Si la localización y/o el
     * <i>timeslot</i> no pertenecen a este evento, no sucederá nada.
     *
     * @param localization localización perteneciente al conjunto de localizaciones del evento
     * @param timeslot     hora perteneciente al conjunto de horas en las que el evento discurre
     */
    public void removeUnavailableLocalizationTimeslot(Localization localization, Timeslot timeslot) {
        if (unavailableLocalizations.computeIfPresent(localization,
                (l, t) -> t.remove(timeslot) && t.isEmpty() ? null : t
        ) == null)
            setChanged();
    }

    /**
     * Vacía el diccionario de localizaciones de juego no disponibles en <i>timeslots</i>.
     */
    public void clearUnavailableLocalizations() {
        if (!unavailableLocalizations.isEmpty()) {
            unavailableLocalizations.clear();

            setChanged();
        }
    }

    /**
     * Comprueba si hay localizaciones de juego no disponibles.
     *
     * @return <code>true</code> si hay alguna localización de juego no disponible, y <code>false</code> si no hay
     * ninguna
     */
    public boolean hasUnavailableLocalizations() {
        return !unavailableLocalizations.isEmpty();
    }

    /**
     * Devuelve el diccionario de jugadores a los que se les ha asignado localizaciones de juego donde sus partidos
     * deban tener lugar.
     *
     * @return diccionario no modificable de jugadores y localizaciones asignadas
     */
    public Map<Player, Set<Localization>> getPlayersInLocalizations() {
        return Collections.unmodifiableMap(playersInLocalizations);
    }

    /**
     * Asigna las localizaciones de juego donde los partidos de los jugadores indicados han de tener lugar.
     * <p>
     * Este método internamente invoca a {@link Event#addPlayerInLocalization(Player, Localization)} para cada
     * localización asignada a cada jugador que incluya el conjunto de claves del diccionario. Por tanto, se lanzarán
     * las excepciones que este último método describe si fuese pertinente.
     *
     * @param playersInLocalizations diccionario no nulo de jugadores pertenecientes al evento y localizaciones donde
     *                               jugarán, existentes en este evento
     * @throws NullPointerException si el argumento es <code>null</code>
     */
    public void setPlayersInLocalizations(Map<Player, Set<Localization>> playersInLocalizations) {
        Objects.requireNonNull(playersInLocalizations);

        playersInLocalizations.forEach((p, localizations) -> localizations.forEach(l -> addPlayerInLocalization(p, l)));
    }

    /**
     * Asigna al jugador una localización explícita donde ha de jugar. Si ya se ha asignado, no hay modificaciones.
     *
     * @param player       jugador perteneciente al conjunto de jugadores del evento
     * @param localization localización perteneciente al conjunto de localizaciones del evento
     * @throws NullPointerException     si alguno de los parámetros es <code>null</code>
     * @throws IllegalArgumentException si el jugador no existe en el evento
     * @throws IllegalArgumentException si la localización no existe en el evento
     */
    public void addPlayerInLocalization(Player player, Localization localization) {
        Objects.requireNonNull(player);
        Objects.requireNonNull(localization);

        if (!players.contains(player))
            throw new IllegalArgumentException("Player does not exist in this event");

        if (!localizations.contains(localization))
            throw new IllegalArgumentException("Localization does not exist in this event");

        playersInLocalizations.computeIfAbsent(player, l -> new HashSet<>()).add(localization);

        setChanged();
    }

    /**
     * Elimina la asociación entre un jugador y una localización donde deba jugar, si existe la asociación y si ambos
     * elementos pertenecen al evento.
     *
     * @param player       jugador para el que se quiere eliminar una localización donde deba jugar
     * @param localization localización cuya asignación al jugador se quiere eliminar
     */
    public void removePlayerInLocalization(Player player, Localization localization) {
        if (playersInLocalizations.computeIfPresent(player,
                (p, l) -> l.remove(localization) && l.isEmpty() ? null : l
        ) == null)
            setChanged();
    }

    /**
     * Vacía el diccionario de jugadores en localizaciones de juego. Ya los jugadores no tendrán ninguna localización
     * asociada donde sus partidos deban tener lugar.
     */
    public void clearPlayersInLocalizations() {
        if (!playersInLocalizations.isEmpty()) {
            playersInLocalizations.clear();

            setChanged();
        }
    }

    /**
     * Comprueba si hay jugadores a los que se les han asignado las localizaciones donde jugar.
     *
     * @return true si sobre el evento hay definidas asignaciones fijas de jugadores sobre localizaciones
     */
    public boolean hasPlayersInLocalizations() {
        return !playersInLocalizations.isEmpty();
    }

    /**
     * Devuelve el diccionario de jugadores a los que se les ha asignado horas donde sus partidos deban tener lugar.
     *
     * @return diccionario no modificable de jugadores asociados a <i>timeslots</i> en los que deben jugar
     */
    public Map<Player, Set<Timeslot>> getPlayersAtTimeslots() {
        return Collections.unmodifiableMap(playersAtTimeslots);
    }

    /**
     * Define las posibles horas a las que cada jugador (si se incluye en el diccionario) empezará su partido o
     * partidos. Se desecharán <i>timeslots</i> que pretendan indicar un comienzo de partido inválido, es decir, una
     * hora de juego situada al final del evento que produjese un partido fuera de rango del dominio (más información
     * sobre esta situación se puede encontrar en {@link Event#addPlayerAtTimeslot(Player, Timeslot)}).
     * <p>
     * Este método invoca internamente a {@link Event#addPlayerAtTimeslots(Player, Set)} para cada entrada del
     * diccionario, luego se aplica el comportamiento que este método mencionado describe en cada llamada, incluidas
     * las excepciones que se lanzan.
     *
     * @param playersAtTimeslots diccionario de jugadores del evento a los que se les asigna las horas, existentes en
     *                           el evento, a las que sus partidos deben tener lugar
     * @throws NullPointerException si el diccionario es <code>null</code>
     */
    public void setPlayersAtTimeslots(Map<Player, Set<Timeslot>> playersAtTimeslots) {
        Objects.requireNonNull(playersAtTimeslots);

        playersAtTimeslots.values()
                .forEach(playerTimeslots -> playerTimeslots.removeIf(t -> timeslots.indexOf(t) + nTimeslotsPerMatch >
                        timeslots.size()));

        playersAtTimeslots.forEach(this::addPlayerAtTimeslots);
    }

    /**
     * Asigna al jugador los timeslots explícitos donde sus partidos han de comenzar.
     * <p>
     * Este método llama a {@link Event#addPlayerAtTimeslot(Player, Timeslot)} para cada <i>timeslot</i> del
     * conjunto, luego se aplica el comportamiento que este método describe y se lanzan las excepciones indicadas en
     * las circunstancias descritas.
     *
     * @param player    jugador perteneciente al conjunto de jugadores del evento
     * @param timeslots conjunto de horas del evento
     * @throws NullPointerException si el jugador es <code>null</code>
     * @throws NullPointerException si el conjunto de <i>timeslots</i> es <code>null</code>
     */
    public void addPlayerAtTimeslots(Player player, Set<Timeslot> timeslots) {
        Objects.requireNonNull(player);
        Objects.requireNonNull(timeslots);

        timeslots.forEach(t -> addPlayerAtTimeslot(player, t));
    }

    /**
     * Asigna al jugador al <i>timeslot</i> explícito donde han de comenzar sus partidos. Si es un <i>timeslot</i>
     * donde no puede comenzar un partido, o si ya ha sido asignado al jugador, no habrá modificaciones.
     * <p>
     * Un <i>timeslot</i> donde no puede comenzar un partido es aquél que se ubica al final del dominio temporal del
     * evento y el evento define más de 1 <i>timeslot</i> por partido de duración. Si se asignase a un jugador un
     * <i>timeslot</i> donde comenzar sus partidos en las <i>n</i> últimas posiciones (donde <i>n</i> es el número de
     * <i>timeslots</i> por partido - 1), y su partido comenzase en este <i>timeslot</i>, la duración del mismo
     * rebasaría el final del propio dominio temporal del evento, luego el partido no podría comenzar. Para evitar
     * esta situación ilógica, se ignoran asignaciones a estos <i>timeslots</i> imposibles.
     *
     * @param player   jugador perteneciente al conjunto de jugadores del evento
     * @param timeslot hora perteneciente al conjunto de horas en las que el evento discurre
     * @throws NullPointerException     si el jugador es <code>null</code>
     * @throws NullPointerException     si el <i>timeslot</i> es <code>null</code>
     * @throws IllegalArgumentException si el jugador no pertenece al evento
     * @throws IllegalArgumentException si el <i>timeslot</i> no pertenece al evento
     */
    public void addPlayerAtTimeslot(Player player, Timeslot timeslot) {
        Objects.requireNonNull(player);
        Objects.requireNonNull(timeslot);

        if (!players.contains(player))
            throw new IllegalArgumentException("Player does not exist in this event");

        if (!timeslots.contains(timeslot))
            throw new IllegalArgumentException("Timeslot does not exist in this event");

        if (timeslots.indexOf(timeslot) + nTimeslotsPerMatch <= timeslots.size())
            playersAtTimeslots.computeIfAbsent(player, t -> new HashSet<>()).add(timeslot);

        setChanged();
    }

    /**
     * Asigna al jugador un rango de <i>timeslot</i> donde deberán comenzar sus partidos. Los extremos se incluyen.
     * <p>
     * Este método llama a {@link Event#addPlayerAtTimeslot(Player, Timeslot)} para cada <i>timeslot</i> del
     * rango, luego se aplica el comportamiento que este método describe y se lanzan las excepciones indicadas en
     * las circunstancias descritas.
     *
     * @param player un jugador del evento
     * @param t1     un extremo del rango de horas
     * @param t2     el otro extremo
     */
    public void addPlayerAtTimeslotRange(Player player, Timeslot t1, Timeslot t2) {
        addPlayerAtTimeslot(player, t1);
        addPlayerAtTimeslot(player, t2);

        Timeslot start, end;
        if (t1.compareTo(t2) >= 0) {
            start = t1;
            end = t2;
        } else {
            start = t2;
            end = t1;
        }

        for (int t = timeslots.indexOf(start) + 1; t < timeslots.indexOf(end); t++)
            addPlayerAtTimeslot(player, timeslots.get(t));
    }

    /**
     * Elimina de la configuración que el jugador deba jugar a la hora indicada, si ha sido asignada, y si el jugador
     * y el <i>timeslot</i> existen en el evento.
     *
     * @param player   jugador al que se le quiere retirar un <i>timeslot</i> donde deba jugar
     * @param timeslot <i>timeslot</i> cuya asociación con el jugador se quiere eliminar
     */
    public void removePlayerAtTimeslot(Player player, Timeslot timeslot) {
        if (playersAtTimeslots.computeIfPresent(player, (p, t) -> t.remove(timeslot) && t.isEmpty() ? null : t) == null)
            setChanged();
    }

    /**
     * Comprueba si hay jugadores a los que se les han asignado horas de juego donde sus partidos deben tener lugar.
     *
     * @return true si sobre el evento hay definidas asignaciones fijas de jugadores sobre <i>timeslots</i>
     */
    public boolean hasPlayersAtTimeslots() {
        return !playersAtTimeslots.isEmpty();
    }

    /**
     * Vacía el diccionario de jugadores en <i>timeslots</i>. Ya ningún jugador tendrá asociado ningún
     * <i>timeslot</i> donde sus partidos deban jugarse.
     */
    public void clearPlayersAtTimeslots() {
        if (!playersAtTimeslots.isEmpty()) {
            playersAtTimeslots.clear();

            setChanged();
        }
    }

    public MatchupMode getMatchupMode() {
        return matchupMode;
    }

    /**
     * Asigna el modo de emparejamiento de este evento, sólo si el número de partidos por jugador es superior a uno y
     * el número de jugadores por partido es superior a uno.
     *
     * @param matchupMode modo de emparejamiento
     * @throws NullPointerException si el modo de enfrentamiento es <code>null</code>
     */
    public void setMatchupMode(MatchupMode matchupMode) {
        Objects.requireNonNull(matchupMode);

        if (matchupMode == this.matchupMode)
            return;

        if (nMatchesPerPlayer > 1 && nPlayersPerMatch > 1)
            this.matchupMode = matchupMode;
        else
            this.matchupMode = MatchupMode.ANY;

        setChanged();
    }

    /**
     * Devuelve el equipo al que el jugador pertenece, si el jugador existe en el evento y si pertenece a algún equipo.
     *
     * @param player jugador no nulo perteneciente al evento
     * @return equipo al que pertenece el jugador, o <code>null</code> si no pertenece a ningún equipo o el jugador
     * no existe en este evento
     */
    public Team filterTeamByPlayer(Player player) {
        return teams.stream().filter(team -> team.getPlayers().contains(player)).limit(1).findFirst().orElse(null);
    }

    /**
     * Comprueba si el jugador está disponible a una hora determinada.
     *
     * @param player   jugador cuya disponibilidad se desea consultar
     * @param timeslot <i>timeslot</i> a la que se quiere consultar la disponibilidad del jugador
     * @return <code>true</code> si el jugador está disponible a esa hora, <code>false</code> si no
     */
    public boolean isPlayerUnavailable(Player player, Timeslot timeslot) {
        return unavailablePlayers.containsKey(player) && unavailablePlayers.get(player).contains(timeslot);
    }

    /**
     * Comprueba si la localización está invalidada a una hora determinada.
     *
     * @param localization localización cuya disponibilidad se desea consultar
     * @param timeslot     <i>timeslot</i> a la que se quiere consultar la disponibilidad de la localización
     * @return <code>true</code> si la localización no está disponible a la hora indicada, <code>false</code> si está
     * disponible
     */
    public boolean isLocalizationUnavailable(Localization localization, Timeslot timeslot) {
        return unavailableLocalizations.containsKey(localization) &&
                unavailableLocalizations.get(localization).contains(timeslot);
    }

    /**
     * Devuelve el número total de partidos que se jugarán en este evento.
     *
     * @return número de partidos del evento
     */
    public int getNumberOfMatches() {
        return players.size() / nPlayersPerMatch * nMatchesPerPlayer;
    }

    /**
     * Devuelve el número total de <i>timeslots</i> de un horario que se ocuparán con partidos.
     *
     * @return número de horas ocupadas, mayor que 0
     */
    public int getNumberOfOccupiedTimeslots() {
        return players.size() * nMatchesPerPlayer * nTimeslotsPerMatch;
    }

    /**
     * Devuelve la representación en cadena de este evento, simplemente su nombre.
     *
     * @return el nombre del evento
     */
    public String toString() {
        return name;
    }

    /**
     * Inyecta el objeto encargado de la validación de este evento.
     *
     * @param validator implementación de un <i>validador</i> que ejecutará la validación del evento
     */
    public <T> void setValidator(Validator<T> validator) {
        Objects.requireNonNull(validator);

        this.validator = (Validator<Event>) validator;
    }

    /**
     * Si falla la validación de este evento, devuelve la lista de mensajes de error.
     *
     * @return mensajes de error de validación
     */
    public List<String> getMessages() {
        return validator.getValidationMessages();
    }

    /**
     * Valida el estado final de este evento
     *
     * @throws ValidationException si falla la validación
     */
    public void validate() throws ValidationException {
        if (!validator.validate(this))
            throw new ValidationException(String.format("Validation has failed for this event (%s)", name));
    }

    /**
     * Marca el evento con un estado final consistente, es decir, que no se han producido cambios en ningunos de sus
     * atributos.
     * <p>
     * Este método es invocado desde {@link Tournament#solve()} para indicar que los eventos de los que se compone el
     * torneo están listos para modelar el problema en el proceso de resolución. También es llamado desde el
     * constructor de un torneo.
     */
    protected void setAsUnchanged() {
        clearChanged();
    }
}