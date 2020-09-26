package ru.payment.calc.payment_calculator.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ru.payment.calc.payment_calculator.utils.DayOfWeek;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class Group {
    private String sheetName;
    private double pricePerHour;
    private String groupId;
    private String groupLevel;
    private String teacherOne;
    private String teacherTwo;
    private double classDurationOne;
    private double classDurationTwo;
    private String classStartTime;
    private List<DayOfWeek> classDaysOne;
    private List<DayOfWeek> classDaysTwo;
    private List<Student> studentsInfo;
    private List<String> scheduleDays;
    private boolean isIndividual;
    private boolean isMonWenFr;
    private boolean isTueTh;
    private boolean isSat;
    private boolean isOtherSchedule;
    private String daysFilterString;
    private double nextMonthHours;

    public List<DayOfWeek> getClassDaysOne() {
        if (this.classDaysOne == null) {
            this.classDaysOne = new ArrayList<>();
        }
        return this.classDaysOne;
    }

    public List<DayOfWeek> getClassDaysTwo() {
        if(this.classDaysTwo == null) {
            this.classDaysTwo = new ArrayList<>();
        }
        return this.classDaysTwo;
    }

    public List<Student> getStudentsInfo() {
        if(this.studentsInfo == null) {
            this.studentsInfo = new ArrayList<>();
        }
        return this.studentsInfo;
    }

    public List<String> getScheduleDays() {
        if(this.scheduleDays == null) {
            this.scheduleDays = new ArrayList<>();
        }
        return this.scheduleDays;
    }
}
