package by.bsut.staris.stations;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;


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

    @Test
    @DisplayName("escapeCell: символ | экранируется обратной косой чертой")
    void escapeCellEscapesPipe() {
        assertEquals("Станция A\\|B", StationRegistry.escapeCell("Станция A|B"));
    }

    @Test
    @DisplayName("escapeCell: переводы строк заменяются пробелами")
    void escapeCellReplacesLineBreaks() {
        assertEquals("A B C", StationRegistry.escapeCell("A\nB\rC"));
    }

    @Test
    @DisplayName("escapeCell: обычное название остаётся без изменений")
    void escapeCellKeepsPlainName() {
        assertEquals("Гомель-Пассажирский", StationRegistry.escapeCell("Гомель-Пассажирский"));
    }

    @Test
    @DisplayName("Возвращается самая удалённая станция")
    void returnsFarthestStation() {
        Station farthest = registry.farthest().orElseThrow();

        assertEquals("Житковичи", farthest.name());
        assertEquals(238, farthest.distanceFromGomelKm());
    }

    @Test
    @DisplayName("Пустой справочник возвращает пустой результат")
    void returnsEmptyWhenRegistryIsEmpty() {
        StationRegistry emptyRegistry = new StationRegistry(List.of());

        assertTrue(emptyRegistry.farthest().isEmpty());
    }

    @Test
    @DisplayName("Справочник с одной станцией возвращает её")
    void returnsOnlyStationWhenRegistryHasOneStation() {
        Station gomel = new Station(
                "Гомель-Пассажирский",
                Line.GOMEL_ZHLOBIN,
                StationType.PASSENGER,
                0
        );

        StationRegistry singleStationRegistry =
                new StationRegistry(List.of(gomel));

        assertEquals(gomel, singleStationRegistry.farthest().orElseThrow());
    }

    @Test
    @DisplayName("Справочник содержит станцию Житковичи с корректными атрибутами")
    void containsZhitkovichi() {
        Station zhitkovichi = station("Житковичи");
        assertEquals(Line.GOMEL_LUNINETS, zhitkovichi.line());
        assertEquals(StationType.INTERMEDIATE, zhitkovichi.type());
        assertEquals(238, zhitkovichi.distanceFromGomelKm());
    }
    @Test
    @DisplayName("Справочник содержит станцию Калинковичи с корректными атрибутами")
    void containsKalinkovichi() {
        Station kalinkovichi = station("Калинковичи");
        assertEquals(Line.GOMEL_KALINKOVICHI, kalinkovichi.line());
        assertEquals(StationType.DIVISIONAL, kalinkovichi.type());
        assertEquals(125, kalinkovichi.distanceFromGomelKm());
    }

    @Test
    @DisplayName("Поиск по точному имени возвращает каждую станцию справочника")
    void findByNameReturnsEachStation() {
        for (Station expected : registry.all()) {
            assertEquals(Optional.of(expected), registry.findByName(expected.name()));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"гомель-пассажирский", "ГОМЕЛЬ-ПАССАЖИРСКИЙ", "ГоМеЛь-ПаСсАжИрСкИй"})
    @DisplayName("Поиск по имени не учитывает регистр кириллицы")
    void findByNameIgnoresCase(String name) {
        assertEquals(Optional.of(station("Гомель-Пассажирский")), registry.findByName(name));
    }

    @ParameterizedTest
    @ValueSource(strings = {"  Гомель-Пассажирский  ", "\tгомель-пассажирский\n", "\u2003ГОМЕЛЬ-ПАССАЖИРСКИЙ\u2003"})
    @DisplayName("Поиск по имени удаляет пробелы по краям, в том числе вместе с изменением регистра")
    void findByNameStripsSurroundingWhitespace(String name) {
        assertEquals(Optional.of(station("Гомель-Пассажирский")), registry.findByName(name));
    }

    @Test
    @DisplayName("Поиск неизвестного имени возвращает пустой Optional")
    void findByNameReturnsEmptyForUnknownStation() {
        assertEquals(Optional.empty(), registry.findByName("Несуществующая станция"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t\n", "\u2003"})
    @DisplayName("Поиск null, пустой строки и строки из пробелов возвращает пустой Optional")
    void findByNameReturnsEmptyForMissingName(String name) {
        assertEquals(Optional.empty(), registry.findByName(name));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Гомель", "Пассажирский", "Гомель- Пассажирский"})
    @DisplayName("Поиск требует полного имени и сохраняет внутренние пробелы")
    void findByNameDoesNotMatchPartialOrAlteredName(String name) {
        assertEquals(Optional.empty(), registry.findByName(name));
    }
    
    @Test
    @DisplayName("Справочник содержит станцию Василевичи с корректными атрибутами")
    void containsVasilevichi() {
        Station vasilevichi = station("Василевичи");
        assertEquals(Line.GOMEL_KALINKOVICHI, vasilevichi.line());
        assertEquals(StationType.INTERMEDIATE, vasilevichi.type());
        assertEquals(86, vasilevichi.distanceFromGomelKm());
    }

    @Test
    @DisplayName("namesStartingWith не учитывает регистр и сортирует названия по алфавиту")
    void namesStartingWithIgnoresCaseAndSorts() {
        StationRegistry sample = new StationRegistry(List.of(
                new Station("Калинковичи", Line.GOMEL_KALINKOVICHI, StationType.DIVISIONAL, 125),
                new Station("Житковичи", Line.GOMEL_LUNINETS, StationType.INTERMEDIATE, 238),
                new Station("Демехи", Line.GOMEL_KALINKOVICHI, StationType.INTERMEDIATE, 57)
        ));

        assertEquals(List.of("Житковичи"), sample.namesStartingWith("ж"));
        assertEquals(
                List.of("Демехи", "Житковичи", "Калинковичи"),
                sample.namesStartingWith("")
        );
    }

    @Test
    @DisplayName("namesStartingWith возвращает пустой список, если префикс никому не подходит")
    void namesStartingWithReturnsEmptyWhenNothingMatches() {
        assertEquals(List.of(), new StationRegistry().namesStartingWith("Ыыы"));
    }

    @Test
    @DisplayName("namesStartingWith не принимает null")
    void namesStartingWithRejectsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> new StationRegistry().namesStartingWith(null));
    }


    @Test
    @DisplayName("Справочник содержит станцию Тереховка с корректными атрибутами")
    void containsTerekhovka() {
        Station terekhovka = station("Тереховка");
        assertEquals(Line.GOMEL_BAKHMACH, terekhovka.line());
        assertEquals(StationType.INTERMEDIATE, terekhovka.type());
        assertEquals(43, terekhovka.distanceFromGomelKm());
    }
    @Test
    @DisplayName("Справочник содержит станцию Петриков с корректными атрибутами")
    void containsPetrikov() {
        Station petrikov = station("Петриков");
        assertEquals(Line.GOMEL_KALINKOVICHI, petrikov.line());
        assertEquals(StationType.INTERMEDIATE, petrikov.type());
        assertEquals(189, petrikov.distanceFromGomelKm());
    }
    // тесты новых станций и методов добавляются в конец класса
}
