package es.uca.garciachacon.eventscheduler.rest.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import es.uca.garciachacon.eventscheduler.data.model.tournament.*;
import es.uca.garciachacon.eventscheduler.rest.deserializer.TimeslotDeserializer;
import es.uca.garciachacon.eventscheduler.rest.deserializer.TournamentDeserializer;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Serializador de un torneo que transforma una instancia de {@link Tournament} en una cadena con formato JSON, y
 * además es compatible con el proceso de deserializacitn de un torneo realizado por {@link TournamentDeserializer}.
 * <p>
 * El formato del objeto JSON producido por este serializador, al ser compatible con el proceso de deserialización,
 * es el mismo que el descrito en {@link TournamentDeserializer}, por lo tanto en la documentación de esta clase se
 * puede encontrar la información acerca del formato del JSON resultante.
 * <p>
 * La única salvedad en el formato se correspondería a casos en los que los tipos de los comienzos y/o duraciones de
 * algunos de los <i>timeslots</i> del torneo no se correspondan con ninguno de los que contempla el deserializador.
 * En este caso, la deserialización de éstos la describe el deserializador de <i>timeslots</i>,
 * {@link TimeslotDeserializer}.
 */
public class TournamentSerializer extends JsonSerializer<Tournament> {
    @Override
    public void serialize(Tournament tournament, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();

        List<Player> allPlayers = tournament.getAllPlayers();
        List<Localization> allLocalizations = tournament.getAllLocalizations();
        List<Timeslot> allTimeslots = tournament.getAllTimeslots();

        gen.writeStringField("name", tournament.getName());

        gen.writeArrayFieldStart("players");
        for (Player player : allPlayers)
            gen.writeObject(player);
        gen.writeEndArray();

        gen.writeArrayFieldStart("localizations");
        for (Localization localization : allLocalizations)
            gen.writeObject(localization);
        gen.writeEndArray();

        gen.writeArrayFieldStart("timeslots");
        for (Timeslot timeslot : allTimeslots)
            gen.writeObject(timeslot);
        gen.writeEndArray();

        gen.writeArrayFieldStart("events");
        for (Event event : tournament.getEvents()) {
            gen.writeStartObject();

            gen.writeStringField("name", event.getName());

            gen.writeArrayFieldStart("players");
            for (Player player : event.getPlayers())
                gen.writeNumber(allPlayers.indexOf(player));
            gen.writeEndArray();

            gen.writeArrayFieldStart("localizations");
            for (Localization localization : event.getLocalizations())
                gen.writeNumber(allLocalizations.indexOf(localization));
            gen.writeEndArray();

            gen.writeArrayFieldStart("timeslots");
            for (Timeslot timeslot : event.getTimeslots())
                gen.writeNumber(allTimeslots.indexOf(timeslot));
            gen.writeEndArray();

            gen.writeNumberField("matchesPerPlayer", event.getMatchesPerPlayer());
            gen.writeNumberField("timeslotsPerMatch", event.getTimeslotsPerMatch());
            gen.writeNumberField("playersPerMatch", event.getPlayersPerMatch());
            if (event.getPlayersPerTeam() > 0)
                gen.writeNumberField("playersPerTeam", event.getPlayersPerTeam());

            List<Team> teams = event.getTeams();
            if (!teams.isEmpty()) {
                gen.writeArrayFieldStart("teams");

                for (Team team : teams) {
                    gen.writeStartObject();

                    gen.writeStringField("name", team.getName());
                    gen.writeArrayFieldStart("players");
                    for (Player player : team.getPlayers())
                        gen.writeNumber(allPlayers.indexOf(player));
                    gen.writeEndArray();

                    gen.writeEndObject();
                }
                gen.writeEndArray();
            }

            List<Timeslot> breaks = event.getBreaks();
            if (!breaks.isEmpty()) {
                gen.writeArrayFieldStart("breaks");
                for (Timeslot breakTimeslot : breaks)
                    gen.writeNumber(allTimeslots.indexOf(breakTimeslot));
                gen.writeEndArray();
            }

            Map<Player, Set<Timeslot>> unavailablePlayers = event.getUnavailablePlayers();
            if (!unavailablePlayers.isEmpty()) {
                gen.writeObjectFieldStart("unavailablePlayers");

                for (Map.Entry<Player, Set<Timeslot>> entry : unavailablePlayers.entrySet()) {
                    gen.writeFieldName(String.valueOf(allPlayers.indexOf(entry.getKey())));

                    gen.writeStartArray();
                    for (Timeslot timeslot : entry.getValue())
                        gen.writeNumber(allTimeslots.indexOf(timeslot));
                    gen.writeEndArray();
                }

                gen.writeEndObject();
            }

            Map<Localization, Set<Timeslot>> unavailableLocalizations = event.getUnavailableLocalizations();
            if (!unavailableLocalizations.isEmpty()) {
                gen.writeObjectFieldStart("unavailableLocalizations");

                for (Map.Entry<Localization, Set<Timeslot>> entry : unavailableLocalizations.entrySet()) {
                    gen.writeFieldName(String.valueOf(allLocalizations.indexOf(entry.getKey())));

                    gen.writeStartArray();
                    for (Timeslot timeslot : entry.getValue())
                        gen.writeNumber(allTimeslots.indexOf(timeslot));
                    gen.writeEndArray();
                }

                gen.writeEndObject();
            }

            Map<Player, Set<Localization>> playersInLocalizations = event.getPlayersInLocalizations();
            if (!playersInLocalizations.isEmpty()) {
                gen.writeObjectFieldStart("playersInLocalizations");

                for (Map.Entry<Player, Set<Localization>> entry : playersInLocalizations.entrySet()) {
                    gen.writeFieldName(String.valueOf(allPlayers.indexOf(entry.getKey())));

                    gen.writeStartArray();
                    for (Localization localization : entry.getValue())
                        gen.writeNumber(allLocalizations.indexOf(localization));
                    gen.writeEndArray();
                }

                gen.writeEndObject();
            }

            Map<Player, Set<Timeslot>> playersAtTimeslots = event.getPlayersAtTimeslots();
            if (!playersAtTimeslots.isEmpty()) {
                gen.writeObjectFieldStart("playersAtTimeslots");

                for (Map.Entry<Player, Set<Timeslot>> entry : playersAtTimeslots.entrySet()) {
                    gen.writeFieldName(String.valueOf(allPlayers.indexOf(entry.getKey())));

                    gen.writeStartArray();
                    for (Timeslot timeslot : entry.getValue())
                        gen.writeNumber(allTimeslots.indexOf(timeslot));
                    gen.writeEndArray();
                }

                gen.writeEndObject();
            }

            Set<Matchup> predefinedMatchups = event.getPredefinedMatchups();
            if (!predefinedMatchups.isEmpty()) {
                gen.writeArrayFieldStart("predefinedMatchups");

                for (Matchup predefinedMatchup : predefinedMatchups) {
                    gen.writeStartObject();

                    gen.writeArrayFieldStart("players");
                    for (Player player : predefinedMatchup.getPlayers())
                        gen.writeNumber(allPlayers.indexOf(player));
                    gen.writeEndArray();

                    gen.writeArrayFieldStart("localizations");
                    for (Localization localization : predefinedMatchup.getLocalizations())
                        gen.writeNumber(allLocalizations.indexOf(localization));
                    gen.writeEndArray();

                    gen.writeArrayFieldStart("timeslots");
                    for (Timeslot timeslot : predefinedMatchup.getTimeslots())
                        gen.writeNumber(allTimeslots.indexOf(timeslot));
                    gen.writeEndArray();

                    gen.writeNumberField("occurrences", predefinedMatchup.getOccurrences());

                    gen.writeEndObject();
                }

                gen.writeEndArray();
            }

            gen.writeStringField("matchupMode", event.getMatchupMode().toString());

            gen.writeEndObject();
        }
        gen.writeEndArray();

        gen.writeEndObject();
    }
}
