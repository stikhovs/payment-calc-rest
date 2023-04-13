package ru.payment.calc.payment_calculator.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.payment.calc.payment_calculator.controller.dto.request.excel.ExcelDownloadRequest;
import ru.payment.calc.payment_calculator.service.ExcelService;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ExcelController {

    private final ExcelService excelService;

    @PostMapping("/download-excel")
    public ResponseEntity<byte[]> downloadExcel(@RequestBody ExcelDownloadRequest request) {
        byte[] result = excelService.createExcel(request);
        return ResponseEntity.ok(result);
    }

}
