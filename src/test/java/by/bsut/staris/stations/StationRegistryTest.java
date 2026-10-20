package by.bsut.staris.stations;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

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
    @DisplayName("Справочник содержит станцию Демехи с корректными атрибутами")
    void containsDemekhi() {
        Station demekhi = station("Демехи");
        assertEquals(Line.GOMEL_KALINKOVICHI, demekhi.line());
        assertEquals(StationType.INTERMEDIATE, demekhi.type());
        assertEquals(57, demekhi.distanceFromGomelKm());
    }

    private List<String> tableLines() {
        return registry.toMarkdownTable().lines().toList();
    }

    @Test
    @DisplayName("toMarkdownTable: результат совпадает с таблицей из STATIONS.md")
    void markdownTableMatchesStationsMd() throws IOException {
        String expected = Files.readAllLines(Path.of("STATIONS.md"), StandardCharsets.UTF_8).stream()
                .map(String::trim)
                .filter(line -> line.startsWith("|"))
                .reduce((a, b) -> a + "\n" + b)
                .orElseThrow();
        assertEquals(expected, registry.toMarkdownTable().strip());
    }

    @Test
    @DisplayName("toMarkdownTable: первые строки — заголовок и разделитель")
    void markdownTableStartsWithHeaderAndSeparator() {
        List<String> lines = tableLines();
        assertEquals("| Станция | Направление | Тип | Расстояние от Гомеля, км |", lines.get(0));
        assertEquals("| --- | --- | --- | --- |", lines.get(1));
    }

    @Test
    @DisplayName("toMarkdownTable: число строк равно числу станций плюс заголовок и разделитель")
    void markdownTableHasRowPerStation() {
        assertEquals(registry.all().size() + 2, tableLines().size());
    }

    @Test
    @DisplayName("toMarkdownTable: строка станции содержит все атрибуты в порядке справочника")
    void markdownTableRowsFollowRegistryOrder() {
        List<String> lines = tableLines();
        for (int i = 0; i < registry.all().size(); i++) {
            Station s = registry.all().get(i);
            String expected = "| " + s.name() + " | " + s.line().name() + " | "
                    + s.type().name() + " | " + s.distanceFromGomelKm() + " |";
            assertEquals(expected, lines.get(i + 2));
        }
    }
    // тесты новых станций и методов добавляются в конец класса
}
