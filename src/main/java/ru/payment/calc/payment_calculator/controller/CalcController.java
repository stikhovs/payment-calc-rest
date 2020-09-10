package ru.payment.calc.payment_calculator.controller;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import ru.payment.calc.payment_calculator.model.Group;
import ru.payment.calc.payment_calculator.props.CellProps;
import ru.payment.calc.payment_calculator.service.InitGroupsService;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class CalcController {

    private final CellProps cellProps;
    private final InitGroupsService initGroupsService;

    @ResponseBody
    @GetMapping("/test")
    public String test() {
        return cellProps.getGroupInfoCells().toString();
    }

    @GetMapping("/home")
    public String home() {
        return "home";
    }


    @PostMapping("/upload")
    @SneakyThrows
    public ModelAndView getGroupInfo(@RequestParam("fileToUpload") MultipartFile file) {
        XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream());
        List<Group> groupList = initGroupsService.init(workbook);
        /*List<XSSFSheet> sheets = new ArrayList<>();

        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            XSSFSheet sheet = workbook.getSheetAt(i);
            if(!workbook.isSheetHidden(i)) {
                sheets.add(sheet);
            }
        }*/

        ModelAndView modelAndView = new ModelAndView("groups", Map.of("groupList", groupList));

        return modelAndView;
    }

    @GetMapping("/init")
    public ModelAndView initGroupInfo(@RequestParam("workbook") XSSFWorkbook workbook) {
        List<Group> groupList = initGroupsService.init(workbook);
        return new ModelAndView("groupInfo", Map.of("groupInfoList", groupList));
    }


    private double getPrice(XSSFSheet sheet) {
        XSSFCell cell = sheet.getRow(0).getCell(0);
        return Double.parseDouble(cell.toString());
    }

}
