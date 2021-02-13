package ru.payment.calc.payment_calculator.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.monitorjbl.xlsx.StreamingReader;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import reactor.core.publisher.Flux;
import ru.payment.calc.payment_calculator.model.Group;
import ru.payment.calc.payment_calculator.model.NextMonthDatesStore;
import ru.payment.calc.payment_calculator.model.request.UploadRequest;
import ru.payment.calc.payment_calculator.props.FileProps;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Controller
@RequiredArgsConstructor
public class CalcController {

    private final FileProps fileProps;
    private final InitGroupsService initGroupsService;
    private final ExcelService excelService;

    private final ConcurrentHashMap<String, UploadRequest> uploadRequestMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Workbook> workbookMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, List<Group>> groupsMap = new ConcurrentHashMap<>();

    @GetMapping("/sse")
    public String test() {
        return "sse";
    }

    @GetMapping
    public String home() {
        return "index";
    }

    @PostMapping(path = "/upload-1")
    @SneakyThrows
    @ResponseBody
    public Flux<String> getGroupInfo(@RequestParam("fileToUpload") MultipartFile file) {
        return DataBufferUtils
                .readInputStream(file::getInputStream, new DefaultDataBufferFactory(), 4096)
                .map(dataBuffer -> dataBuffer.asInputStream())
                .map(inputStream -> inputStream.toString() + "\n");
    }

    @GetMapping("/file-upload")
    public String fileUpload() {
        return "file-upload";
    }

    @PostMapping("/save")
    @ResponseBody
    @SneakyThrows
    public String save(@RequestParam("fileToUpload") MultipartFile file) {
        return new ObjectMapper().writeValueAsString(saveTempFile(file));
    }

    @PostMapping("/delete")
    @ResponseBody
    public void delete(@RequestParam("fileName") String fileName) {
        deleteFile(fileName);
    }

    @PostMapping(path = "/upload")
    public ModelAndView upload(@RequestParam("fileName") String fileName,
                               @RequestParam("dateToCalc") String dateToCalc,
                               @RequestParam("daysOff") String daysOff,
                               @RequestParam("daysFrom") String daysFrom,
                               @RequestParam("daysTo") String daysTo) {

        UploadRequest uploadRequest = UploadRequest.builder()
                .id(UUID.randomUUID())
                .fileName(fileName)
                .dateToCalc(dateToCalc)
                .daysOff(daysOff)
                .daysFrom(daysFrom)
                .daysTo(daysTo)
                .build();

        String uploadRequestId = uploadRequest.getId().toString();
        uploadRequestMap.put(uploadRequestId, uploadRequest);

        return new ModelAndView("upload", Map.of("uploadRequestId", uploadRequestId));
    }

    @PostMapping(path = "/get-workbook")
    @SneakyThrows
    @ResponseBody
    public String getWorkbook(@RequestParam("uploadRequestId") String uploadRequestId) {
        UploadRequest uploadRequest = uploadRequestMap.get(uploadRequestId);
        String fileName = uploadRequest.getFileName();
        log.info("Start processing {}", fileName);

        String sourceWorkbookDirectory = fileProps.getSourceWorkbookDirectory();
        byte[] tempFileBytes = Files.readAllBytes(Path.of(sourceWorkbookDirectory, fileName));

        Files.deleteIfExists(Path.of(sourceWorkbookDirectory, fileName));
        log.info("deleted {} from {}", fileName, sourceWorkbookDirectory);

        ByteArrayInputStream inputStream = new ByteArrayInputStream(tempFileBytes);

        Workbook workbook = StreamingReader.builder()
                .rowCacheSize(100)
                .bufferSize(4096)
                .open(inputStream);

        String workBookId = UUID.randomUUID().toString();
        workbookMap.put(workBookId, workbook);

        return workBookId;
    }

    @GetMapping(value = "/init-groups", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @ResponseBody
    public Flux<ServerSentEvent<Group>> initGroups(@RequestParam("uploadRequestId") String uploadRequestId, @RequestParam("workbookId") String workbookId) {
        log.info("Start initializing groups");
        log.info("uploadRequestId: {}, workbookId: {}", uploadRequestId, workbookId);
        UploadRequest uploadRequest = uploadRequestMap.get(uploadRequestId);
        NextMonthDatesStore nextMonthDatesStore = getNextMonthDatesStore(
                uploadRequest.getDateToCalc(),
                uploadRequest.getDaysOff(),
                uploadRequest.getDaysFrom(),
                uploadRequest.getDaysTo());

        uploadRequest.setMonth(getNextMonthTitle(nextMonthDatesStore));
        uploadRequest.setYear(nextMonthDatesStore.getNextMonthDate().getYear());

        Workbook workbook = workbookMap.get(workbookId);
        workbookMap.remove(workbookId);
        log.info("removed workbook {} from workbookMap", workbookId);

        List<Group> groups = new ArrayList<>();
        String groupListUUID = UUID.randomUUID().toString();
        return Flux.mergeSequential(
                initGroupsService.init(workbook, nextMonthDatesStore)
                .doOnNext(sseEvent -> groups.add(sseEvent.data())),
                Flux.just(ServerSentEvent
                        .<Group>builder()
                        .id(groupListUUID)
                        .event("the-end")
                        .data(new Group())
                        .build())
                        .doOnComplete(() -> {
                            log.info("groupListUUID: {}", groupListUUID);
                            log.info("groups size: {}", groups.size());
                            groupsMap.put(groupListUUID, groups);
                        }));
    }

    @SneakyThrows
    @PostMapping("/create-workbook")
    @ResponseBody
    public String createWorkbook(@RequestParam("uploadRequestId") String uploadRequestId, @RequestParam("groupListUUID") String groupListUUID) {

        List<Group> groups = groupsMap.get(groupListUUID);
        UploadRequest uploadRequest = uploadRequestMap.get(uploadRequestId);

        groupsMap.remove(groupListUUID);
        log.info("removed groupList {} from groupsMap", groupListUUID);

        log.info("Start creating Excel Workbook");
        XSSFWorkbook resultWB = excelService.createExcel(groups, uploadRequest.getMonth());
        log.info("Excel Workbook created");

        String tempFilePath = saveTempWorkbook(resultWB);
        log.info("Saved workbook: " + tempFilePath);
        return tempFilePath;
    }

    @SneakyThrows
    @GetMapping("/download/{workbookId}")
    public ResponseEntity<byte[]> downloadWorkbook(@PathVariable("workbookId") String workbookId, @RequestParam("uploadRequestId") String uploadRequestId) {
        log.info("Reading bytes from Temp Workbook");
        String resultWorkbookDirectory = fileProps.getResultWorkbookDirectory();
        String tempFilePath = Paths.get(resultWorkbookDirectory, workbookId + ".xlsx").toString();
        byte[] bytes = readTempWorkbook(tempFilePath);

        UploadRequest uploadRequest = uploadRequestMap.get(uploadRequestId);
        uploadRequestMap.remove(uploadRequestId);
        log.info("removed uploadRequest {} from uploadRequestMap", uploadRequestId);

        String finalFileName = "Расчет квитанций - " + uploadRequest.getMonth() + " - " + uploadRequest.getYear() + ".xlsx";
        HttpHeaders httpHeaders = getHttpHeaders(finalFileName);

        Files.deleteIfExists(Path.of(tempFilePath));
        log.info("Result report {}.xlsx was downloaded and deleted successfully", workbookId);

        return ResponseEntity
                .ok()
                .headers(httpHeaders)
                .body(bytes);
    }

    @GetMapping("/clear")
    @ResponseBody
    @SneakyThrows
    public void cleanData() {
        workbookMap.clear();
        log.info("cleared workbookMap");
        uploadRequestMap.clear();
        log.info("cleared uploadRequestMap");
        groupsMap.clear();
        log.info("cleared groupsMap");
        FileSystemUtils.deleteRecursively(Path.of(fileProps.getSourceWorkbookDirectory()));
        log.info("deleted source workbook directory");
        FileSystemUtils.deleteRecursively(Path.of(fileProps.getResultWorkbookDirectory()));
        log.info("deleted result workbook directory");
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

    @SneakyThrows
    private String saveTempFile(MultipartFile file) {
        UUID uuid = UUID.randomUUID();
        log.info("Start writing the file with UUID {}", uuid);
        Path directory = Files.createDirectories(Paths.get(fileProps.getSourceWorkbookDirectory()));
        String fileName = uuid + ".xlsx";
        Path tempFile = directory.resolve(fileName);
        file.transferTo(tempFile);
        log.info("File with UUID {} created", uuid);
        return fileName;
    }

    @SneakyThrows
    private void deleteFile(String fileName) {
        Files.deleteIfExists(Paths.get(fileProps.getSourceWorkbookDirectory(), fileName));
    }

    private String saveTempWorkbook(XSSFWorkbook workBook) throws IOException {
        UUID uuid = UUID.randomUUID();
        log.info("Start writing the file with UUID {}", uuid);
        Path directory = Files.createDirectories(Paths.get(fileProps.getResultWorkbookDirectory()));
        Path tempFile = directory.resolve(uuid + ".xlsx");
        String tempFilePath = tempFile.toString();
        try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(tempFilePath))) {
            workBook.write(out);
        }
        log.info("File with UUID {} created", uuid);
        return uuid.toString();
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
