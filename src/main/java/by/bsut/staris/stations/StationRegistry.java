package by.bsut.staris.stations;

import java.util.List;

/** Справочник станций Гомельского отделения Белорусской железной дороги. */
public final class StationRegistry {

    private final List<Station> stations = List.of(
        new Station("Гомель-Пассажирский", Line.GOMEL_ZHLOBIN, StationType.PASSENGER, 0),
        new Station("Гомель-Сортировочный", Line.GOMEL_ZHLOBIN, StationType.MARSHALLING, 5)
        // новые станции добавляются в конец списка
    );

    /** Возвращает все станции справочника. */
    public List<Station> all() {
        return stations;
    }

    // новые методы добавляются в конец класса
}
