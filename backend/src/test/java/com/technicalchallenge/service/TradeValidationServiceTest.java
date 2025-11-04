package com.technicalchallenge.service;

import com.technicalchallenge.dto.TradeDTO;
import com.technicalchallenge.dto.TradeLegDTO;
import com.technicalchallenge.dto.ValidationResult;
import com.technicalchallenge.model.ApplicationUser;
import com.technicalchallenge.model.Book;
import com.technicalchallenge.model.Counterparty;
import com.technicalchallenge.model.UserProfile;
import com.technicalchallenge.repository.ApplicationUserRepository;
import com.technicalchallenge.repository.BookRepository;
import com.technicalchallenge.repository.CounterpartyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TradeValidationServiceTest {


    // set up the service with mocked repositories
    @InjectMocks
    private TradeValidationService validationService; 

    @Mock
    private BookRepository bookRepository;

    @Mock
    private CounterpartyRepository counterpartyRepository;

    @Mock
    private ApplicationUserRepository userRepository;

    // ------------------- Date Validation -------------------
    @Test
    void tradeDateTooOld_shouldFail() {
        TradeDTO trade = new TradeDTO();
        trade.setTradeDate(LocalDate.now().minusDays(40));

        ValidationResult result = validationService.validateTradeBusinessRules(trade);
        assertFalse(result.isValid());
        assertTrue(result.getErrors().contains("Trade date cannot be more than 30 days in the past"));
    }

    @Test
    void startDateBeforeTradeDate_shouldFail() {
        TradeDTO trade = new TradeDTO();
        trade.setTradeDate(LocalDate.now());
        trade.setTradeStartDate(LocalDate.now().minusDays(1));

        ValidationResult result = validationService.validateTradeBusinessRules(trade);
        assertFalse(result.isValid());
        assertTrue(result.getErrors().contains("Trade start date cannot be before trade date"));
    }

    @Test
    void maturityDateBeforeStartDate_shouldFail() {
        TradeDTO trade = new TradeDTO();
        trade.setTradeStartDate(LocalDate.now());
        trade.setTradeMaturityDate(LocalDate.now().minusDays(1));

        ValidationResult result = validationService.validateTradeBusinessRules(trade);
        assertFalse(result.isValid());
        assertTrue(result.getErrors().contains("Trade maturity date cannot be before start date"));
    }

    // ------------------- User Privilege -------------------
    @Test
    void traderCanTerminate_shouldPass() {
        ApplicationUser trader = new ApplicationUser();
        UserProfile profile = new UserProfile();
        profile.setUserType("TRADER");
        trader.setUserProfile(profile);

        when(userRepository.findByLoginId("trader1")).thenReturn(Optional.of(trader));

        boolean allowed = validationService.validateUserPrivileges("trader1", "TERMINATE", new TradeDTO());
        assertTrue(allowed);
    }

    @Test
    void salesCannotTerminate_shouldFail() {
        ApplicationUser sales = new ApplicationUser();
        UserProfile profile = new UserProfile();
        profile.setUserType("SALES");
        sales.setUserProfile(profile);

        when(userRepository.findByLoginId("sales1")).thenReturn(Optional.of(sales));

        boolean allowed = validationService.validateUserPrivileges("sales1", "TERMINATE", new TradeDTO());
        assertFalse(allowed);
    }

    // ------------------- Trade Leg Consistency -------------------
    @Test
    void floatingLegWithoutIndex_shouldFail() {
        TradeLegDTO leg1 = new TradeLegDTO();
        leg1.setLegType("Floating");
        leg1.setPayReceiveFlag("PAY");

        TradeLegDTO leg2 = new TradeLegDTO();
        leg2.setLegType("Floating");
        leg2.setPayReceiveFlag("RECEIVE");

        TradeDTO trade = new TradeDTO();
        trade.setTradeLegs(Arrays.asList(leg1, leg2));
        trade.setTradeMaturityDate(LocalDate.now().plusDays(10));

        ValidationResult result = validationService.validateTradeLegConsistency(trade);
        assertFalse(result.isValid());
        assertTrue(result.getErrors().contains("Floating leg must have an index specified"));
    }

    @Test
    void fixedLegWithoutRate_shouldFail() {
        TradeLegDTO leg1 = new TradeLegDTO();
        leg1.setLegType("Fixed");
        leg1.setRate(null);
        leg1.setPayReceiveFlag("PAY");

        TradeLegDTO leg2 = new TradeLegDTO();
        leg2.setLegType("Fixed");
        leg2.setRate(0.0);
        leg2.setPayReceiveFlag("RECEIVE");

        TradeDTO trade = new TradeDTO();
        trade.setTradeLegs(Arrays.asList(leg1, leg2));
        trade.setTradeMaturityDate(LocalDate.now().plusDays(10));

        ValidationResult result = validationService.validateTradeLegConsistency(trade);
        assertFalse(result.isValid());
        assertTrue(result.getErrors().contains("Fixed leg must have a valid rate"));
    }

    // ------------------- Entity Status Validation -------------------
    @Test
    void inactiveBook_shouldFail() {
        Book book = new Book();
        book.setActive(false);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        TradeDTO trade = new TradeDTO();
        trade.setBookId(1L);

        ValidationResult result = validationService.validateTradeBusinessRules(trade);
        assertFalse(result.isValid());
        assertTrue(result.getErrors().contains("Book is not active"));
    }

    @Test
    void missingCounterparty_shouldFail() {
        when(counterpartyRepository.findById(1L)).thenReturn(Optional.empty());

        TradeDTO trade = new TradeDTO();
        trade.setCounterpartyId(1L);

        ValidationResult result = validationService.validateTradeBusinessRules(trade);
        assertFalse(result.isValid());
        assertTrue(result.getErrors().contains("Counterparty not found"));
    }
}
