package by.bsut.staris.stations;

import java.util.List;
import java.util.Optional;

/** Справочник станций Гомельского отделения Белорусской железной дороги. */
public final class StationRegistry {

    private final List<Station> stations = List.of(
            new Station("Гомель-Пассажирский", Line.GOMEL_ZHLOBIN, StationType.PASSENGER, 0),
            new Station("Демехи", Line.GOMEL_KALINKOVICHI, StationType.INTERMEDIATE, 57),
	        new Station("Житковичи", Line.GOMEL_LUNINETS, StationType.INTERMEDIATE, 238),
            new Station("Калинковичи", Line.GOMEL_KALINKOVICHI, StationType.DIVISIONAL, 125),
            new Station("Костюковка", Line.GOMEL_ZHLOBIN, StationType.INTERMEDIATE, 12)
        // новые станции добавляются в конец списка
    );

    /** Возвращает все станции справочника. */
    public List<Station> all() {
        return stations;
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
     * Возвращает станции заданного типа в порядке справочника.
     *
     * @param type тип станции
     * @return станции этого типа; пустой список, если таких станций нет
     * @throws IllegalArgumentException если {@code type} равен {@code null}
     */
    public List<Station> findByType(StationType type) {
        if (type == null) {
            throw new IllegalArgumentException("Тип станции не задан");
        }
        return stations.stream()
                .filter(station -> station.type() == type)
                .toList();
    }
    // новые методы добавляются в конец класса
}
