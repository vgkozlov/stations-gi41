package by.bsut.staris.stations;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StationRegistryTest {

    private final StationRegistry registry = new StationRegistry();

    private Station station(String name) {
        return registry.all().stream()
                .filter(station -> station.name().equals(name))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Станция не найдена: " + name));
    }

    @Test
    @DisplayName("Справочник содержит станцию Гомель-Пассажирский с нулевым расстоянием")
    void containsGomelPassenger() {
        Station gomel = station("Гомель-Пассажирский");
        assertEquals(StationType.PASSENGER, gomel.type());
        assertEquals(0, gomel.distanceFromGomelKm());
    }

    @Test
    @DisplayName("Наименования станций не повторяются")
    void namesAreUnique() {
        long unique = registry.all().stream().map(Station::name).distinct().count();
        assertEquals(registry.all().size(), unique);
    }

    @Test
    @DisplayName("Расстояния неотрицательны")
    void distancesAreNotNegative() {
        assertTrue(registry.all().stream().allMatch(station -> station.distanceFromGomelKm() >= 0));
    }
@Test
@DisplayName("findByType возвращает станции заданного типа")
void findByTypeReturnsStationsOfGivenType() {
    List<Station> result = registry.findByType(StationType.PASSENGER);
    assertFalse(result.isEmpty());
    assertTrue(result.stream().allMatch(s -> s.type() == StationType.PASSENGER));
}

@Test
@DisplayName("findByType возвращает пустой список, если станций такого типа нет")
void findByTypeReturnsEmptyListWhenNoStations() {
    List<Station> result = registry.findByType(StationType.MARSHALLING);
    assertTrue(result.isEmpty());
}

@Test
@DisplayName("findByType выбрасывает IllegalArgumentException при null")
void findByTypeThrowsOnNull() {
    assertThrows(IllegalArgumentException.class, () -> registry.findByType(null));
}
    // тесты новых станций и методов добавляются в конец класса
}
