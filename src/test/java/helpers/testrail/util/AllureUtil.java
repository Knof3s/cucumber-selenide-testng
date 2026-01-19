package helpers.testrail.util;

import lombok.extern.log4j.Log4j2;
import util.DateTimeUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Утилитный класс для работы с отчетами Allure.
 * Содержит методы для очистки папки результатов, генерации отчетов, а также логирования и обработки отчетов Allure.
 */
@Log4j2
public class AllureUtil {

    /**
     * Очищает папку с результатами Allure (allure-results).
     * Удаляет все файлы и подкаталоги в папке.
     * Логирует успешность операции.
     */
    public static void clearAllureResults() {
        Path allureResultsPath = Paths.get("target", "allure-results");

        if (!Files.exists(allureResultsPath)) {
            log.info("Папка allure-results отсутствует, очистка не требуется");
            return;
        }

        // закрыть ресурсы
        try (Stream<Path> paths = Files.walk(allureResultsPath)) {
            paths.sorted(Comparator.reverseOrder()) // Удаляем файлы перед папками
                    .map(Path::toFile)
                    .forEach(file -> {
                        if (file.delete()) {
                            log.debug("Удалён: {}", file);
                        } else {
                            log.warn("Не удалось удалить: {}", file);
                        }
                    });
        } catch (IOException e) {
            log.error("Ошибка при очистке папки allure-results", e);
            return;
        }

        // Проверяем, осталась ли папка и есть ли в ней файлы
        if (Files.exists(allureResultsPath)) {
            try (Stream<Path> remainingFiles = Files.list(allureResultsPath)) {
                List<Path> remaining = remainingFiles.toList();
                if (!remaining.isEmpty()) {
                    log.warn("Не удалось полностью очистить папку allure-results. Остались файлы:");
                    remaining.forEach(file -> log.warn(" - {}", file));
                } else {
                    log.info("Папка allure-results успешно очищена");
                }
            } catch (IOException e) {
                log.error("Ошибка при проверке содержимого папки allure-results", e);
            }
        } else {
            log.info("Папка allure-results была полностью удалена");
        }
    }

    /**
     * Формирует имя отчета Allure с текущей датой и временем.
     * Формат: "allure-report [yyyy-MM-dd HH-mm-ss].html"
     *
     * @return строка с именем файла отчета Allure.
     */
    public static String getAllureReportNameWithDate() {
        String currentDate = DateTimeUtils.getCurrentDate("yyyy-MM-dd HH-mm-ss");
        return String.format("allure-report [%s].html", currentDate);
    }

    /**
     * Логирует информацию о сгенерированном отчете Allure, включая путь к файлу и его предварительное содержание.
     *
     * @param allureReport файл отчета Allure.
     */
    public static void loggingAllureReport(File allureReport) {
        if (allureReport.exists()) {
            log.info("Отчет Allure сгенерирован успешно. Путь к отчету: {}", allureReport.getAbsolutePath());

            // Логируем краткое содержание отчета, если он не пустой
            try (BufferedReader reader = new BufferedReader(new FileReader(allureReport))) {
                String line;
                StringBuilder preview = new StringBuilder();
                int previewLines = 5;  // Количество строк для предварительного просмотра
                int lineCounter = previewLines;
                while ((line = reader.readLine()) != null && lineCounter > 0) {
                    preview.append(line).append("\n");
                    lineCounter--;
                }

                if (!preview.isEmpty()) {
                    log.info("Содержание отчета (первые {} строк): \n{}", previewLines, preview.toString());
                } else {
                    log.warn("Отчет Allure пуст.");
                }
            } catch (IOException e) {
                log.error("Ошибка при чтении отчета Allure для логирования: ", e);
            }

            // Логируем дату и время генерации отчета
            log.info("Дата и время генерации отчета: {}", LocalDateTime.now());
        } else {
            log.error("Ошибка при генерации отчета Allure. Отчет не найден.");
        }
    }

    /**
     * Генерирует отчет Allure, используя файлы в папке "target/allure-results".
     * Если папка пуста, сначала запускает генерацию файлов результатов.
     *
     * @return файл с отчетом Allure.
     */
    public static File generateAllureReport() {
        try {
            // Проверяем, существует ли папка с результатами тестов
            File resultsDir = new File("target/allure-results");
            if (resultsDir.exists() && resultsDir.isDirectory()) {
                String[] files = resultsDir.list();
                log.info("Файлы в target/allure-results: {}", Arrays.toString(files));

                // Если папка пуста, генерируем отчет
                if (files == null || files.length == 0) {
                    log.warn("Папка target/allure-results пуста! Запускаем генерацию отчета.");
                    generateAllureResults();
                }

            } else {
                log.warn("Папка target/allure-results не найдена или пуста! Запускаем генерацию отчета.");
                generateAllureResults();
            }

            String command = "allure generate target/allure-results --single-file --clean";
            ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", command);
            builder.redirectErrorStream(true);
            builder.start().waitFor();

            // Проверяем, создался ли файл отчета
            File reportFile = new File("allure-report/index.html");
            if (reportFile.exists()) {
                log.info("Allure-отчет успешно создан: {}", reportFile.getAbsolutePath());
            } else {
                log.warn("Ошибка: Allure-отчет не был создан!");
            }

            return reportFile;

        } catch (Exception e) {
            log.error("Ошибка при генерации Allure-отчета: {}", e.getMessage());
        }

        return new File("allure-report/index.html");
    }

    /**
     * Генерирует папку "allure-results" с результатами тестов, если она пуста или отсутствует.
     * Запускает процесс генерации результатов тестов через Maven.
     */
    public static void generateAllureResults() {
        // Здесь можно вызвать процесс или команду для генерации отчета, если папка пуста
        log.info("Запуск команды для генерации allure-results");
        String command = "mvn clean test -Dallure.results.directory=target/allure-results";
        try {
            ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", command);
            builder.redirectErrorStream(true);
            Process process = builder.start();
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            log.error("Ошибка при запуске команды генерации allure-results: {}", e.getMessage());
        }
    }
}
