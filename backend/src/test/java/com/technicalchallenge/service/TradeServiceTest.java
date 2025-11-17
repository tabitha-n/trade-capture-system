package com.technicalchallenge.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.technicalchallenge.dto.TradeDTO;
import com.technicalchallenge.dto.TradeLegDTO;
import com.technicalchallenge.model.Book;
import com.technicalchallenge.model.Cashflow;
import com.technicalchallenge.model.Counterparty;
import com.technicalchallenge.model.LegType;
import com.technicalchallenge.model.Schedule;
import com.technicalchallenge.model.Trade;
import com.technicalchallenge.model.TradeLeg;
import com.technicalchallenge.model.TradeStatus;
import com.technicalchallenge.repository.CashflowRepository;
import com.technicalchallenge.repository.TradeLegRepository;
import com.technicalchallenge.repository.TradeRepository;
import com.technicalchallenge.repository.TradeStatusRepository;
import com.technicalchallenge.repository.BookRepository;
import com.technicalchallenge.repository.CounterpartyRepository;
import com.technicalchallenge.repository.ScheduleRepository;
import com.technicalchallenge.repository.LegTypeRepository;;

@ExtendWith(MockitoExtension.class)
class TradeServiceTest {

    @Mock
    private TradeRepository tradeRepository;

    @Mock
    private TradeLegRepository tradeLegRepository;

    @Mock
    private CashflowRepository cashflowRepository;

    @Mock
    private TradeStatusRepository tradeStatusRepository;

    @Mock
    private AdditionalInfoService additionalInfoService;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private CounterpartyRepository counterpartyRepository;

    @Mock
    private ScheduleRepository scheduleRepository;

    @Mock
    private LegTypeRepository legTypeRepository;

    @InjectMocks
    private TradeService tradeService;

    private TradeDTO tradeDTO;
    private Trade trade;

    @BeforeEach
    void setUp() {
        // Set up test data
        tradeDTO = new TradeDTO();
        tradeDTO.setTradeId(100001L);
        tradeDTO.setTradeDate(LocalDate.of(2025, 1, 15));
        tradeDTO.setTradeStartDate(LocalDate.of(2025, 1, 17));
        tradeDTO.setTradeMaturityDate(LocalDate.of(2026, 1, 17));

        TradeLegDTO leg1 = new TradeLegDTO();
        leg1.setNotional(BigDecimal.valueOf(1000000));
        leg1.setRate(0.05);

        TradeLegDTO leg2 = new TradeLegDTO();
        leg2.setNotional(BigDecimal.valueOf(1000000));
        leg2.setRate(0.0);

        tradeDTO.setTradeLegs(Arrays.asList(leg1, leg2));

        trade = new Trade();
        trade.setId(1L);
        trade.setTradeId(100001L);
        trade.setVersion(1);
    }

    @Test
    void testCreateTrade_Success() {
        // When we run the line of code 'BookRepository.findByBookName', return a valid Book object called 'book'
        Book book = new Book();
        book.setBookName("Test Book");
        tradeDTO.setBookName("Test Book");
        when(bookRepository.findByBookName(any(String.class))).thenReturn(Optional.of(book));

        Counterparty counterparty = new Counterparty();
        counterparty.setName("Test Counterparty");
        tradeDTO.setCounterpartyName("Test Counterparty");
        when(counterpartyRepository.findByName(any(String.class))).thenReturn(Optional.of(counterparty));


        TradeStatus tradeStatus= new TradeStatus();
        tradeStatus.setTradeStatus("NEW");
        tradeDTO.setTradeStatus("NEW");
        when(tradeStatusRepository.findByTradeStatus("NEW")).thenReturn(Optional.of(tradeStatus));

        TradeLeg savedLeg = new TradeLeg();
        savedLeg.setLegId(1L);
        when(tradeLegRepository.save(any(TradeLeg.class))).thenReturn(savedLeg);

         // Given
        when(tradeRepository.save(any(Trade.class))).thenReturn(trade);

        // When
        Trade result = tradeService.createTrade(tradeDTO);

        // Then
        assertNotNull(result);
        assertEquals(100001L, result.getTradeId());
        verify(tradeRepository).save(any(Trade.class));
    }

    @Test
    void testCreateTrade_InvalidDates_ShouldFail() {
        // Given - This test is intentionally failing for candidates to fix
        tradeDTO.setTradeStartDate(LocalDate.of(2025, 1, 10)); // Before trade date

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            tradeService.createTrade(tradeDTO);
        });

        // This assertion is intentionally wrong - candidates need to fix it
        assertEquals("Start date cannot be before trade date", exception.getMessage());
    }

    @Test
    void testCreateTrade_InvalidLegCount_ShouldFail() {
        // Given
        tradeDTO.setTradeLegs(Arrays.asList(new TradeLegDTO())); // Only 1 leg

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            tradeService.createTrade(tradeDTO);
        });

        assertTrue(exception.getMessage().contains("exactly 2 legs"));
    }

    @Test
    void testGetTradeById_Found() {
        // Given
        when(tradeRepository.findByTradeIdAndActiveTrue(100001L)).thenReturn(Optional.of(trade));

        // When
        Optional<Trade> result = tradeService.getTradeById(100001L);

        // Then
        assertTrue(result.isPresent());
        assertEquals(100001L, result.get().getTradeId());
    }

    @Test
    void testGetTradeById_NotFound() {
        // Given
        when(tradeRepository.findByTradeIdAndActiveTrue(999L)).thenReturn(Optional.empty());

        // When
        Optional<Trade> result = tradeService.getTradeById(999L);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void testAmendTrade_Success() {
        // Given
        when(tradeRepository.findByTradeIdAndActiveTrue(100001L)).thenReturn(Optional.of(trade));
        when(tradeStatusRepository.findByTradeStatus("AMENDED")).thenReturn(Optional.of(new com.technicalchallenge.model.TradeStatus()));
        when(tradeRepository.save(any(Trade.class))).thenReturn(trade);

        TradeLeg savedLeg = new TradeLeg();
        savedLeg.setLegId(1L);
        when(tradeLegRepository.save(any(TradeLeg.class))).thenReturn(savedLeg);

        // When
        Trade result = tradeService.amendTrade(100001L, tradeDTO);

        // Then
        assertNotNull(result);
        verify(tradeRepository, times(2)).save(any(Trade.class)); // Save old and new
    }

    @Test
    void testAmendTrade_TradeNotFound() {
        // Given
        when(tradeRepository.findByTradeIdAndActiveTrue(999L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            tradeService.amendTrade(999L, tradeDTO);
        });

        assertTrue(exception.getMessage().contains("Trade not found"));
    }

    // This test has a deliberate bug for candidates to find and fix
    @Test
    void testCashflowGeneration_MonthlySchedule() {
        Book book = new Book();
        book.setBookName("Test Book");
        tradeDTO.setBookName("Test Book");
        when(bookRepository.findByBookName(any(String.class))).thenReturn(Optional.of(book));

        // When we run the line of code 'additionalInfoService.getCounterpartyByName', return a valid Counterparty object called 'counterparty'
        Counterparty counterparty = new Counterparty();
        counterparty.setName("Test Counterparty");
        tradeDTO.setCounterpartyName("Test Counterparty");
        when(counterpartyRepository.findByName(any(String.class))).thenReturn(Optional.of(counterparty));


        TradeStatus tradeStatus= new TradeStatus();
        tradeStatus.setTradeStatus("NEW");
        tradeDTO.setTradeStatus("NEW");
        when(tradeStatusRepository.findByTradeStatus("NEW")).thenReturn(Optional.of(tradeStatus));

         // Given
        when(tradeRepository.save(any(Trade.class))).thenReturn(trade);

        Schedule schedule = new Schedule();
        schedule.setSchedule("1M");

        TradeLeg leg = new TradeLeg();
        leg.setLegId(1L);
        leg.setNotional(BigDecimal.valueOf(1000000));
        leg.setCalculationPeriodSchedule(schedule);
        when(tradeLegRepository.save(any(TradeLeg.class))).thenReturn(leg);

        // When - method call is missing

        Trade saved = tradeService.createTrade(tradeDTO);

        assertNotNull(saved);
        verify(cashflowRepository, times(24)).save(any()); // Expecting 12 monthly cashflows for each leg so 12 * 2 = 24
    }

    // Unit test for the private method 'calculateCashflowValue'
    @Test
    void testCalculateCashflowValue_FixedLeg_ShouldPassWithoutException() throws Exception {
        // Arrange
        TradeLeg leg = new TradeLeg();
        leg.setNotional(BigDecimal.valueOf(10_000_000)); // 1,000,000 notional
        leg.setRate(3.5);                              // 3.5% interest rate

        LegType fixedType = new LegType();
        fixedType.setType("Fixed");
        leg.setLegRateType(fixedType);

        // Access private method via reflection
        Method method = TradeService.class.getDeclaredMethod("calculateCashflowValue", TradeLeg.class, int.class);
        method.setAccessible(true);

        // Act
        BigDecimal result = (BigDecimal) method.invoke(tradeService, leg, 3); // Quarterly (3 months)
        result = result.setScale(2, RoundingMode.HALF_UP);

        // Assert
        BigDecimal expected = BigDecimal.valueOf(87_500.0).setScale(2, RoundingMode.HALF_UP); // 1,000,000 * 0.05 * 3 / 12
        assertNotNull(result, "Result should not be null");
        assertEquals(0, expected.compareTo(result), "Calculated value should match expected");
    }

    // Integration test for cashflow generation
    @Test
    void testCashflowGeneration_calculateCashflowValue() {
        // --- Reference data setup ---
        Book book = new Book(); book.setBookName("Test Book");
        tradeDTO.setBookName("Test Book");
        when(bookRepository.findByBookName("Test Book")).thenReturn(Optional.of(book));

        Counterparty counterparty = new Counterparty(); counterparty.setName("Test Counterparty");
        tradeDTO.setCounterpartyName("Test Counterparty");
        when(counterpartyRepository.findByName("Test Counterparty")).thenReturn(Optional.of(counterparty));

        TradeStatus tradeStatus = new TradeStatus(); tradeStatus.setTradeStatus("NEW");
        tradeDTO.setTradeStatus("NEW");
        when(tradeStatusRepository.findByTradeStatus("NEW")).thenReturn(Optional.of(tradeStatus));

        Schedule schedule = new Schedule(); schedule.setSchedule("1M");
        when(scheduleRepository.findBySchedule("1M")).thenReturn(Optional.of(schedule));

        LegType fixed = new LegType();
        fixed.setType("Fixed");
        when(legTypeRepository.findByType("Fixed")).thenReturn(Optional.of(fixed));

        // --- Dates: one year apart → 12 monthly payments per leg ---
        tradeDTO.setTradeDate(LocalDate.of(2025, 1, 1));
        tradeDTO.setTradeStartDate(LocalDate.of(2025, 1, 1));
        tradeDTO.setTradeMaturityDate(LocalDate.of(2026, 1, 1));

        // --- Configure both legs: Fixed, monthly, 10M notional, 3.5% rate ---
        tradeDTO.getTradeLegs().forEach(leg -> {
            leg.setCalculationPeriodSchedule("1M");
            leg.setLegType("Fixed");
            leg.setNotional(BigDecimal.valueOf(10_000_000));
            leg.setRate(3.5);
        });

        // --- Mock save behavior ---
        when(tradeRepository.save(any(Trade.class))).thenAnswer(inv -> inv.getArgument(0));
        when(tradeLegRepository.save(any(TradeLeg.class))).thenAnswer(inv -> inv.getArgument(0));

        // Capture all saved cashflows
        ArgumentCaptor<Cashflow> cfCaptor =
                ArgumentCaptor.forClass(Cashflow.class);
        when(cashflowRepository.save(cfCaptor.capture())).thenAnswer(inv -> inv.getArgument(0));

        // --- Act ---
        Trade saved = tradeService.createTrade(tradeDTO);

        // --- Assert count ---
        assertNotNull(saved);
        // 1M schedule over 12 months, 2 legs => 24 cashflows total
        verify(cashflowRepository, times(24)).save(any());

        // --- Assert values ---
        BigDecimal expected = BigDecimal.valueOf((10_000_000d * 0.035d * 1d) / 12d)
                                        .setScale(2, RoundingMode.HALF_UP);

        for (Cashflow cf : cfCaptor.getAllValues()) {
            BigDecimal actual = cf.getPaymentValue().setScale(2, RoundingMode.HALF_UP);
            assertEquals(0, actual.compareTo(expected),
                    "Cashflow value should match expected: " + expected + " but got " + actual);
        }
    }
}
