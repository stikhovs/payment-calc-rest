package ru.payment.calc.payment_calculator.controller;

import com.monitorjbl.xlsx.StreamingReader;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import ru.payment.calc.payment_calculator.model.Group;
import ru.payment.calc.payment_calculator.model.NextMonthDatesStore;
import ru.payment.calc.payment_calculator.props.CellProps;
import ru.payment.calc.payment_calculator.service.ExcelService;
import ru.payment.calc.payment_calculator.service.InitGroupsService;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
@RequiredArgsConstructor
@Slf4j
public class CalcController {

    private final CellProps cellProps;
    private final InitGroupsService initGroupsService;
    private final ExcelService excelService;

    @GetMapping
    public String home() {
        return "index";
    }

    @PostMapping(path = "/upload", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @SneakyThrows
    @ResponseBody
    public ResponseEntity<byte[]> getGroupInfo(@RequestParam("fileToUpload") MultipartFile file,
                                               @RequestParam("dateToCalc") String dateToCalc,
                                               @RequestParam("daysOff") String daysOff,
                                               @RequestParam("daysFrom") String daysFrom,
                                               @RequestParam("daysTo") String daysTo) {
        log.info("Start processing {}", file.getResource().getFilename());
        InputStream inputStream = file.getInputStream();
        Workbook workbook = StreamingReader.builder()
                .rowCacheSize(100)
                .bufferSize(4096)
                .open(inputStream);

        log.info("Start initializing groups");
        NextMonthDatesStore nextMonthDatesStore = getNextMonthDatesStore(dateToCalc, daysOff, daysFrom, daysTo);

        List<Group> groupList = initGroupsService.init(workbook, nextMonthDatesStore);

        log.info("Group initializing completed");
        String month = getNextMonthTitle(nextMonthDatesStore);

        log.info("Start creating Excel Workbook");
        XSSFWorkbook resultWB = excelService.createExcel(groupList, month);
        log.info("Excel Workbook created");

        String tempFilePath = saveTempWorkbook(resultWB);

        log.info("Reading bytes from Temp Workbook");
        byte[] bytes = readTempWorkbook(tempFilePath);

        log.info("Deleting Temp Workbook");
        Files.deleteIfExists(Path.of(tempFilePath));

        String finalFileName = "Расчет квитанций - " + month + " - " + nextMonthDatesStore.getNextMonthDate().getYear() + ".xlsx";
        HttpHeaders httpHeaders = getHttpHeaders(finalFileName);

        return ResponseEntity
                .ok()
                .headers(httpHeaders)
                .body(bytes);
    }

    private NextMonthDatesStore getNextMonthDatesStore(String dateToCalc, String daysOff, String daysFrom, String daysTo) {
        LocalDate nextMonthDate = parseDate(dateToCalc);
        log.info("Next month date: {}", nextMonthDate);

        List<LocalDate> daysOffList = parseDates(daysOff);
        log.info("Days off: {}", daysOffList);

        Set<Pair<LocalDate, LocalDate>> daysToChange = getDaysToChange(daysFrom, daysTo);
        log.info("Days to change: {}", daysToChange);

        return NextMonthDatesStore.builder()
                .nextMonthDate(nextMonthDate)
                .daysOff(Set.copyOf(daysOffList))
                .datesToChange(daysToChange)
                .build();
    }

    private LocalDate parseDate(String date) {
        return LocalDate.parse(date, DateTimeFormatter.ISO_DATE);
    }

    private List<LocalDate> parseDates(String dates) {
        if (StringUtils.isNotBlank(dates)) {
            return Stream.of(dates.split(","))
                    .map(date -> LocalDate.parse(date, DateTimeFormatter.ISO_DATE))
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    private Set<Pair<LocalDate, LocalDate>> getDaysToChange(String daysFrom, String daysTo) {
        List<LocalDate> daysFromList = parseDates(daysFrom);
        List<LocalDate> daysToList = parseDates(daysTo);

        HashSet<Pair<LocalDate, LocalDate>> result = new HashSet<>();
        for (int i = 0; i < daysFromList.size(); i++) {
            Pair<LocalDate, LocalDate> datePair = Pair.of(daysFromList.get(i), daysToList.get(i));
            result.add(datePair);
        }
        return result;
    }

    private String saveTempWorkbook(XSSFWorkbook workBook) throws IOException {
        UUID uuid = UUID.randomUUID();
        log.info("Start writing the file with UUID {}", uuid);
        Path directory = Files.createDirectories(Paths.get("temp"));
        Path tempFile = directory.resolve("temp - " + uuid + ".xlsx");
        String tempFilePath = tempFile.toString();
        try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(tempFilePath))) {
            workBook.write(out);
        }
        log.info("File with UUID {} created", uuid);
        return tempFilePath;
    }

    private String getNextMonthTitle(NextMonthDatesStore nextMonthDatesStore) {
        return StringUtils.capitalize(nextMonthDatesStore.getNextMonthDate().getMonth().getDisplayName(TextStyle.FULL_STANDALONE, new Locale("ru")));
    }

    private byte[] readTempWorkbook(String tempFilePath) throws IOException {
        try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(ResourceUtils.getFile(tempFilePath)))) {
            return IOUtils.toByteArray(in);
        }
    }

    private HttpHeaders getHttpHeaders(String finalFileName) {
        ContentDisposition attachment = ContentDisposition.builder("attachment")
                .filename(finalFileName, StandardCharsets.UTF_8)
                .build();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentDisposition(attachment);
        return httpHeaders;
    }

}
