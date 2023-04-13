package ru.payment.calc.payment_calculator.controller;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.payment.calc.payment_calculator.controller.dto.request.GroupCalculationRequest;
import ru.payment.calc.payment_calculator.controller.dto.request.excel.ExcelDownloadRequest;
import ru.payment.calc.payment_calculator.controller.dto.response.GroupCalculationResponse;
import ru.payment.calc.payment_calculator.service.CalculationService;
import ru.payment.calc.payment_calculator.service.MyExcelService;

@Slf4j
@RestController
@RequiredArgsConstructor
public class MyController {
    private final MyExcelService excelService;
    private final CalculationService calculationService;

    @PostMapping("/calculate")
    public GroupCalculationResponse processGroups(@RequestBody GroupCalculationRequest groupCalculationRequest) {
        return calculationService.calculate(groupCalculationRequest);
    }


    @PostMapping("/download-excel")
    public ResponseEntity<byte[]> downloadExcel(@RequestBody ExcelDownloadRequest request) {
        XSSFWorkbook excel = excelService.createExcel(request);
        return ResponseEntity.ok(toByteArray(excel));
    }




    @SneakyThrows
    private byte[] toByteArray(XSSFWorkbook excel) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            excel.write(bos);
            return bos.toByteArray();
        }
    }
}
