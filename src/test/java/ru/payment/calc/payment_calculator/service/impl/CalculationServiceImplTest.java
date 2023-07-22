package ru.payment.calc.payment_calculator.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import ru.payment.calc.payment_calculator.config.ObjectMapperConfig;
import ru.payment.calc.payment_calculator.config.props.DebtThreshold;
import ru.payment.calc.payment_calculator.controller.dto.request.GroupCalculationRequest;
import ru.payment.calc.payment_calculator.controller.dto.response.GroupCalculationResponse;
import ru.payment.calc.payment_calculator.controller.dto.response.GroupResponse;
import ru.payment.calc.payment_calculator.controller.dto.response.StudentResponse;
import ru.payment.calc.payment_calculator.mapper.GroupMapperImpl;
import ru.payment.calc.payment_calculator.mapper.StudentMapperImpl;
import ru.payment.calc.payment_calculator.service.CalculationService;
import ru.payment.calc.payment_calculator.service.NextMonthHoursService;

import java.io.IOException;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {CalculationServiceImpl.class, GroupMapperImpl.class, StudentMapperImpl.class})
@Import(ObjectMapperConfig.class)
public class CalculationServiceImplTest {

    private static final BigDecimal DEBT_THRESHOLD = BigDecimal.valueOf(-0.03);

    @MockBean
    private NextMonthHoursService nextMonthHoursService;

    @MockBean
    private DebtThreshold debtThreshold;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CalculationService calculationService;


    @BeforeEach
    public void setUp() {
        when(debtThreshold.getThreshold()).thenReturn(DEBT_THRESHOLD);
    }


    @Test
    public void testCalculation() {
        // given
        when(nextMonthHoursService.calcNextMonthHours(any(), any())).thenReturn(BigDecimal.valueOf(20));

        // when
        GroupCalculationResponse result = calculationService.calculate(mockCalculationRequest());

        // then
        assertThat(result)
                .isNotNull()
                .extracting(GroupCalculationResponse::getMonWedFr)
                .extracting(groups -> groups.get(0))
                .extracting(GroupResponse::getStudents)
                .satisfies(students -> {
                    assertStudent(students.get(0), false, "20.00", "9000.00");
                    assertStudent(students.get(1), false, "20.00", "8550.00");
                    assertStudent(students.get(2), false, "20.00", "9000.00");
                    assertStudent(students.get(3), false, "0.00", "0.00");
                    assertStudent(students.get(4), false, "20.01", "9004.50");
                    assertStudent(students.get(5), false, "20.03", "9013.50");
                    assertStudent(students.get(6), true, "20.04", "9018.00");
                    assertStudent(students.get(7), true, "26.67", "12001.50");
                    assertStudent(students.get(8), true, "40.00", "18000.00");
                });
    }

    private void assertStudent(StudentResponse studentResponse, boolean hasDebt, String hours, String money) {
        assertThat(studentResponse)
                .as("Check %s", studentResponse.getName())
                .isNotNull()
                .satisfies(student -> {
                    assertThat(student.isHasDebt())
                            .as("Check debt")
                            .isEqualTo(hasDebt);
                    assertThat(student.getHoursToPay())
                            .as("Check scale of HoursToPay")
                            .hasScaleOf(2)
                            .as("Check HoursToPay")
                            .isEqualTo(hours);
                    assertThat(student.getMoneyToPay())
                            .as("Check scale of MoneyToPay")
                            .hasScaleOf(2)
                            .as("Check MoneyToPay")
                            .isEqualTo(money);
                });
    }

    private GroupCalculationRequest mockCalculationRequest() {
        try {
            return objectMapper.readValue(new ClassPathResource("/request/calculation-request.json").getFile(), GroupCalculationRequest.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
