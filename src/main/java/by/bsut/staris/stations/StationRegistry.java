package by.bsut.staris.stations;

import java.util.List;
import java.util.Comparator;
import java.util.Optional;

/** Справочник станций Гомельского отделения Белорусской железной дороги. */
public final class StationRegistry {

    private final List<Station> stations;

    public StationRegistry() {
        this.stations = List.of(
                new Station("Гомель-Пассажирский", Line.GOMEL_ZHLOBIN, StationType.PASSENGER, 0),
                new Station("Демехи", Line.GOMEL_KALINKOVICHI, StationType.INTERMEDIATE, 57),
                new Station("Житковичи", Line.GOMEL_LUNINETS, StationType.INTERMEDIATE, 238),
                new Station("Калинковичи", Line.GOMEL_KALINKOVICHI, StationType.DIVISIONAL, 125),
                new Station("Василевичи", Line.GOMEL_KALINKOVICHI, StationType.INTERMEDIATE, 86)
                // новые станции добавляются в конец списка
        );
    }

    public StationRegistry(List<Station> stations) {
        this.stations = List.copyOf(stations);
    }

    /** Возвращает все станции справочника. */
    public List<Station> all() {
        return stations;
    }

    /** Возвращает самую удалённую от Гомеля станцию. */
    public Optional<Station> farthest() {
        return stations.stream()
                .max(Comparator.comparingInt(Station::distanceFromGomelKm));
    }

    /**
     * Ищет станцию по полному наименованию без учёта регистра и пробелов по краям.
     *
     * @return найденная станция или {@link Optional#empty()}, если имя неизвестно,
     *         пусто или равно {@code null}
     */
    public Optional<Station> findByName(String name) {
        if (name == null) {
            return Optional.empty();
        }
        String normalizedName = name.strip();
        return stations.stream()
                .filter(station -> station.name().equalsIgnoreCase(normalizedName))
                .findFirst();
    }
    
    /**
 * Возвращает наименования станций с заданным префиксом без учёта регистра,
 * в алфавитном порядке.
 *
 * @throws IllegalArgumentException если {@code prefix} равен {@code null}
 */
public List<String> namesStartingWith(String prefix) {
    if (prefix == null) {
        throw new IllegalArgumentException("Префикс не задан");
    }
    return stations.stream()
            .map(Station::name)
            .filter(name -> name.regionMatches(true, 0, prefix, 0, prefix.length()))
            .sorted()
            .toList();
}

    /**
     * Возвращает модуль разности расстояний для станций одного направления.
     *
     * @throws IllegalArgumentException если станция неизвестна или направления станций различаются
     */
    public int distanceBetween(String a, String b) {
        Station stationA = findByName(a)
                .orElseThrow(() -> new IllegalArgumentException("Станция не найдена: " + a));
        Station stationB = findByName(b)
                .orElseThrow(() -> new IllegalArgumentException("Станция не найдена: " + b));

        if (stationA.line() != stationB.line()) {
            throw new IllegalArgumentException("Станции относятся к разным направлениям");
        }

        return Math.abs(stationA.distanceFromGomelKm() - stationB.distanceFromGomelKm());
    }

    // новые методы добавляются в конец класса
}
