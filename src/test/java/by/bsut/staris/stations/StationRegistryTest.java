package by.bsut.staris.stations;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;


import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

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
    void testGomelSortirovochnyExists() {
        var station = new Station("Гомель-Сортировочный", Line.GOMEL_ZHLOBIN, StationType.MARSHALLING, 6);
        org.junit.jupiter.api.Assertions.assertTrue(
            new StationRegistry().all().contains(station),
            "Станция Гомель-Сортировочный должна присутствовать в справочнике"
    );
}
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
    @DisplayName("distanceBetween возвращает модуль разности расстояний для станций одного направления")
    void distanceBetweenReturnsDistanceForSameLine() {
        assertEquals(68, registry.distanceBetween("Демехи", "Калинковичи"));
        assertEquals(68, registry.distanceBetween("Калинковичи", "Демехи"));
    }

    @Test
    @DisplayName("distanceBetween выбрасывает исключение для неизвестной станции")
    void distanceBetweenThrowsForUnknownStation() {
        assertThrows(IllegalArgumentException.class,
                () -> registry.distanceBetween("Несуществующая станция", "Демехи"));
        assertThrows(IllegalArgumentException.class,
                () -> registry.distanceBetween("Демехи", "Несуществующая станция"));
    }

    @Test
    @DisplayName("distanceBetween выбрасывает исключение для станций разных направлений")
    void distanceBetweenThrowsForDifferentLines() {
        assertThrows(IllegalArgumentException.class,
                () -> registry.distanceBetween("Гомель-Пассажирский", "Демехи"));
    }

    @Test
    @DisplayName("distanceBetween возвращает 0 для одной и той же станции и не зависит от регистра")
    void distanceBetweenReturnsZeroForSameStation() {
        assertEquals(0, registry.distanceBetween("Демехи", "Демехи"));
        assertEquals(0, registry.distanceBetween("демехи", "ДЕМЕХИ"));
    }
    @Test

    @DisplayName("countByLine считает станции по направлениям")
    void countByLineCountsStationsPerLine() {
        StationRegistry sample = new StationRegistry(List.of(
                new Station("Демехи", Line.GOMEL_KALINKOVICHI, StationType.INTERMEDIATE, 57),
                new Station("Калинковичи", Line.GOMEL_KALINKOVICHI, StationType.DIVISIONAL, 125),
                new Station("Житковичи", Line.GOMEL_LUNINETS, StationType.INTERMEDIATE, 238)
        ));

        assertEquals(
                Map.of(Line.GOMEL_KALINKOVICHI, 2L, Line.GOMEL_LUNINETS, 1L),
                sample.countByLine());
    }

    @Test
    @DisplayName("countByLine возвращает пустую карту для пустого справочника")
    void countByLineReturnsEmptyMapForEmptyRegistry() {
        assertTrue(new StationRegistry(List.of()).countByLine().isEmpty());
    }

    @Test
    @DisplayName("countByLine не включает направления без станций")
    void countByLineOmitsLinesWithoutStations() {
        StationRegistry single = new StationRegistry(List.of(
                new Station("Гомель-Пассажирский", Line.GOMEL_ZHLOBIN, StationType.PASSENGER, 0)));

        Map<Line, Long> counts = single.countByLine();

        assertEquals(Map.of(Line.GOMEL_ZHLOBIN, 1L), counts);
        assertFalse(counts.containsKey(Line.GOMEL_BRYANSK));
    }

    @Test
    @DisplayName("countByLine: сумма по направлениям равна числу станций справочника")
    void countByLineSumsToRegistrySize() {
        long total = registry.countByLine().values().stream().mapToLong(Long::longValue).sum();
        assertEquals(registry.all().size(), total);
    }

    @Test
    @DisplayName("Справочник содержит станцию Уза с корректными атрибутами")
    void containsUza() {
        Station uza = station("Уза");
        assertEquals(Line.GOMEL_ZHLOBIN, uza.line());
        assertEquals(StationType.INTERMEDIATE, uza.type());
        assertEquals(23, uza.distanceFromGomelKm());
    }

    @Test
    @DisplayName("Справочник содержит станцию Гомель-Сортировочный с корректными атрибутами")
    void containsGomelSortirovochny() {
        Station station = station("Гомель-Сортировочный");
        assertEquals(Line.GOMEL_ZHLOBIN, station.line());
        assertEquals(StationType.MARSHALLING, station.type());
        assertEquals(6, station.distanceFromGomelKm());
    }

    @Test
    @DisplayName("Справочник содержит станцию Словечно с корректными атрибутами")
    void containsSlovechno() {
        Station slovechno = station("Словечно");
        assertEquals(Line.GOMEL_KALINKOVICHI, slovechno.line());
        assertEquals(StationType.INTERMEDIATE, slovechno.type()); 
        assertEquals(194, slovechno.distanceFromGomelKm());
    }

    // блок тестов метода M1
    @Test
    @DisplayName("findByLine корректно фильтрует станции (изолированный тест)")
    void findByLineFiltersCorrectly() {
        Station station1 = new Station("Тестовая 1", Line.GOMEL_KALINKOVICHI, StationType.INTERMEDIATE, 10);
        Station station2 = new Station("Тестовая 2", Line.GOMEL_LUNINETS, StationType.INTERMEDIATE, 20);
        Station station3 = new Station("Тестовая 3", Line.GOMEL_KALINKOVICHI, StationType.INTERMEDIATE, 30);
        StationRegistry testRegistry = new StationRegistry(List.of(station1, station2, station3));
        List<Station> result = testRegistry.findByLine(Line.GOMEL_KALINKOVICHI);
        assertEquals(2, result.size());
        assertTrue(result.contains(station1));
        assertTrue(result.contains(station3));
        assertFalse(result.contains(station2));
    }

    @Test
    @DisplayName("findByLine возвращает пустой список, если станций на направлении нет")
    void findByLineReturnsEmptyListForLineWithoutStations() {
        StationRegistry emptyRegistry = new StationRegistry(List.of());
        List<Station> stations = emptyRegistry.findByLine(Line.GOMEL_LUNINETS);
        assertNotNull(stations);
        assertTrue(stations.isEmpty());
    }

    @Test
    @DisplayName("findByLine выбрасывает исключение при передаче null")
    void findByLineThrowsExceptionForNull() {
        assertThrows(IllegalArgumentException.class, 
                () -> registry.findByLine(null));
    }

    @Test
    @DisplayName("Справочник содержит станцию Светлогорск-на-Березине с корректными атрибутами")
    void containsSvetlogorskNaBerezine() {
        Station station = station("Светлогорск-на-Березине");
        assertEquals(Line.ZHLOBIN_KALINKOVICHI, station.line());
        assertEquals(StationType.INTERMEDIATE, station.type());
        assertEquals(124, station.distanceFromGomelKm());
    }

    // ==================== Дополнительные тесты для метода toMarkdownTable (Issue #32) ====================

    @Test
    @DisplayName("toMarkdownTable: для пустого справочника возвращаются только заголовок и разделитель")
    void toMarkdownTableReturnsOnlyHeaderWhenRegistryIsEmpty() {
        StationRegistry emptyRegistry = new StationRegistry(List.of());
        List<String> lines = emptyRegistry.toMarkdownTable().lines().toList();

        assertEquals(2, lines.size(), "Для пустого справочника должно быть ровно 2 строки");
        assertEquals("| Станция | Направление | Тип | Расстояние от Гомеля, км |", lines.get(0));
        assertEquals("| --- | --- | --- | --- |", lines.get(1));
    }

    @Test
    @DisplayName("toMarkdownTable: корректно формирует разметку для справочника из одной станции")
    void toMarkdownTableFormatsSingleStationRegistry() {
        Station testStation = new Station("Тестовая", Line.GOMEL_BRYANSK, StationType.FREIGHT, 50);
        StationRegistry singleRegistry = new StationRegistry(List.of(testStation));
        List<String> lines = singleRegistry.toMarkdownTable().lines().toList();

        assertEquals(3, lines.size(), "Для одной станции должно быть ровно 3 строки (заголовок, разделитель, станция)");
        assertEquals("| Тестовая | GOMEL_BRYANSK | FREIGHT | 50 |", lines.get(2));
    }

    @Test
    @DisplayName("toMarkdownTable: экранирует символ '|' в названии станции при генерации таблицы")
    void toMarkdownTableEscapesPipeCharacterInTableName() {
        Station stationWithPipe = new Station("Пункт A|B", Line.GOMEL_ZHLOBIN, StationType.INTERMEDIATE, 15);
        StationRegistry customRegistry = new StationRegistry(List.of(stationWithPipe));
        List<String> lines = customRegistry.toMarkdownTable().lines().toList();

        assertEquals("| Пункт A\\|B | GOMEL_ZHLOBIN | INTERMEDIATE | 15 |", lines.get(2));
    }

    @Test
    @DisplayName("toMarkdownTable: заменяет переносы строк в названии станции пробелами")
    void toMarkdownTableReplacesLineBreaksInTableName() {
        Station stationWithBreak = new Station("Станция\nНовая", Line.GOMEL_KALINKOVICHI, StationType.PASSENGER, 30);
        StationRegistry customRegistry = new StationRegistry(List.of(stationWithBreak));
        List<String> lines = customRegistry.toMarkdownTable().lines().toList();

        assertEquals(3, lines.size(), "Перенос строки внутри названия не должен создавать лишнюю строку в таблице");
        assertEquals("| Станция Новая | GOMEL_KALINKOVICHI | PASSENGER | 30 |", lines.get(2));
    }

    @Test
    @DisplayName("Справочник содержит станцию Рогачёв с корректными атрибутами")
    void containsRogachev() {
        Station rogachev = station("Рогачёв");
        assertEquals(Line.MOGILEV_ZHLOBIN, rogachev.line());
        assertEquals(StationType.INTERMEDIATE, rogachev.type());
        assertEquals(112, rogachev.distanceFromGomelKm());
    }

    // тесты новых станций и методов добавляются в конец класса
}