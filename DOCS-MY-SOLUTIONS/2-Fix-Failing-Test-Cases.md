# 🧪 Step 2: Fix Failing Test Cases - Documentation Summary 

## Task Background and Objective
The application has several failing test cases that need to to be fixed. 

Task:
1. Systematically identify and fix failing test cases in the backend application. 
2. Document the debugging process, understanding of the fixes and the business logic.

This document provides a structured overview of the test fixes implemented.  

The tests are grouped by their corresponding test classes.

Each entry includes the **problem description**, **root cause analysis**, **solution implemented**, and **verification outcome**.  

## BookServiceTest.java

### testFindBookById
| **Problem Description** | **Root Cause Analysis** | **Solution Implemented** | **Verification** |
|--------------------------|-------------------------|---------------------------|------------------|
| Error: NullPointerException – the test failed when trying to find a book by its ID (object was null). | The `BookMapper`, responsible for converting `Book` entities into `BookDTO`s, wasn’t mocked properly. The `BookService` got no response and crashed. | 1. Added `@Mock` to `BookMapper`.<br>2. Stubbed the `toDto()` method to return the expected `BookDTO`. | Test now runs without crashing and verifies the book ID correctly. |

---

### testSaveBook
| **Problem Description** | **Root Cause Analysis** | **Solution Implemented** | **Verification** |
|--------------------------|-------------------------|---------------------------|------------------|
| Test failed because the service returned `null` instead of a valid `BookDTO`. | `BookMapper` methods (`toEntity` and `toDto`) weren’t stubbed, so conversions returned `null`. | Stubbed `bookMapper.toEntity(bookDTO)` and `bookMapper.toDto(book)` to return valid objects. | Test passes; the saved `BookDTO` is not null and has the correct ID. |

---

### testFindBookByNonExistentId
| **Problem Description** | **Root Cause Analysis** | **Solution Implemented** | **Verification** |
|--------------------------|-------------------------|---------------------------|------------------|
| The service should handle cases where a book ID doesn’t exist. | The test didn’t simulate a missing book scenario. | Stubbed `bookRepository.findById()` to return `Optional.empty()` for the non-existent ID. | Test passes; service correctly returns nothing when book isn’t found. |

---

## TradeLegControllerTest.java

### testCreateTradeLegValidationFailure_NegativeNotional
| **Problem Description** | **Root Cause Analysis** | **Solution Implemented** | **Verification** |
|--------------------------|-------------------------|---------------------------|------------------|
| Expected message “Notional must be positive” but got an empty response. | Validation returned 400 Bad Request, but Spring’s default handler didn’t include a message body. | Added a `GlobalExceptionHandler` to return a proper validation message when validation fails. | Test passes; API returns 400 with “Notional must be positive,” and service method isn’t called. |

---

## TradeControllerTest.java

### testCreateTrade
| **Problem Description** | **Root Cause Analysis** | **Solution Implemented** | **Verification** |
|--------------------------|-------------------------|---------------------------|------------------|
| Expected HTTP 200 OK but received 201 Created. | Controller correctly returned 201 when creating a new trade, but the test expected 200. | Updated test assertion to expect `isCreated()` instead of `isOk()`. | Test passes and validates the API’s 201 Created response. |

---

### testCreateTradeValidationFailure_MissingTradeDate
| **Problem Description** | **Root Cause Analysis** | **Solution Implemented** | **Verification** |
|--------------------------|-------------------------|---------------------------|------------------|
| The test expected "Trade date is required" but initially failed because the response body was empty. | Validation correctly detected the missing trade date, but the GlobalExceptionHandler was not active in the test context, so Spring’s default handler returned 400 with no message. | Imported and enabled the existing GlobalExceptionHandler so validation errors return clear messages. | Test now passes; API returns HTTP 400 with "Trade date is required" and the service method is not called. |

---

### testCreateTradeValidationFailure_MissingBook
| **Problem Description** | **Root Cause Analysis** | **Solution Implemented** | **Verification** |
|--------------------------|-------------------------|---------------------------|------------------|
| Expected 400 Bad Request but received 201 Created. | The `TradeDTO` lacked the required validation annotation for `book` and `counterparty`. | Added `@NotNull(message = "Book and Counterparty are required")` to the bookName and counterpartyName fields in TradeDTO. | API now returns 400 when `book` or `counterparty` is missing, and the test passes. |

---

### testUpdateTrade
| **Problem Description** | **Root Cause Analysis** | **Solution Implemented** | **Verification** |
|--------------------------|-------------------------|---------------------------|------------------|
| The test failed because it called the wrong service method (saveTrade) and did not map the returned Trade to a DTO. | Test stubbed `save()` instead of `amend()` and didn’t mock mapper conversion. | Updated to stub the `amend()` method and mapper conversion from entity to DTO. | Test now passes; confirms the update endpoint returns HTTP 200 with the correct trade ID in the response. |

---

### testUpdateTradeIdMismatch
| **Problem Description** | **Root Cause Analysis** | **Solution Implemented** | **Verification** |
|--------------------------|-------------------------|---------------------------|------------------|
| The test expected 400 Bad Request when the path ID did not match the request body ID. | The `updateTrade()` method didn’t check if path ID matched body ID. | Added a check in the update endpoint to return 400 Bad Request with the message "Trade ID in path must match Trade ID in request body" if the IDs do not match. | Test now passes; ensures the service is not called when IDs mismatch and the API returns the correct error message. |

---

### testDeleteTrade
| **Problem Description** | **Root Cause Analysis** | **Solution Implemented** | **Verification** |
|--------------------------|-------------------------|---------------------------|------------------|
| Expected 204 No Content but received 200 OK. | Controller did not return the correct HTTP status after deletion i.e. delete endpoint returned OK instead of no-content. | Updated controller to return `ResponseEntity.noContent().build()` when delete succeeds and adjusted test. | API now returns 204 No Content; test passes. |

---

## TradeServiceTest.java

### testCreateTrade_Success
| **Problem Description** | **Root Cause Analysis** | **Solution Implemented** | **Verification** |
|--------------------------|-------------------------|---------------------------|------------------|
| Test failed due to NullPointerException when creating a trade. | Dependent repositories and services e.g. BookRepository,  etc were not mocked. The service tried to access these objects but received null. | Stubbed all dependent repository/service calls to return valid objects e.g. 'Mock Book lookup'. | Test now passes; confirms that a trade can be successfully created, all dependent objects are correctly stubbed, and tradeRepository.save() is called. |

---

### testCreateTrade_InvalidDates_ShouldFail
| **Problem Description** | **Root Cause Analysis** | **Solution Implemented** | **Verification** |
|--------------------------|-------------------------|---------------------------|------------------|
| The test was failing because it expected an incorrect exception message "Wrong error message" when the trade start date was earlier than the trade date. | The createTrade() method in TradeService correctly checks if tradeStartDate is before the main trade date and throws a RuntimeException with the message "Start date cannot be before trade date". The test assertion didn’t match this message. | I updated the test to assert the correct exception message: "Start date cannot be before trade date". | Test now passes. It confirms that the service correctly rejects trades where the start date is before the trade date and throws a clear error message. |

---

### testAmendTrade_Success
| **Problem Description** | **Root Cause Analysis** | **Solution Implemented** | **Verification** |
|--------------------------|-------------------------|---------------------------|------------------|
| The test failed when the service tried to save trade legs while amending a trade. | The repository method tradeLegRepository.save() was not mocked, so the service crashed with a NullPointerException. | I added a mock for tradeLegRepository.save() so it returns a TradeLeg object during the test. | Test now passes; we know that amendTrade() works and saves both trades (old trade & new trade) and their trade legs correctly. |

---

### testCashflowGeneration_MonthlySchedule

| **Category**             | **Details** |
|--------------------------|-------------|
| **Problem Description**  | The test was failing because cashflows were not being generated or persisted for each leg of the trade. The assertion `assertEquals(1, 12)` was incorrect and the method call to generate cashflows was missing. |
| **Root Cause Analysis**  | The test did not mock the necessary repositories (`BookRepository`, `CounterpartyRepository`, `TradeStatusRepository`, `TradeRepository`, `TradeLegRepository`, `CashflowRepository`), so `tradeService.createTrade(tradeDTO)` could not fully execute. Additionally, the `TradeLeg` objects lacked a valid schedule, and the cashflow generation logic was never triggered. |
| **Solution Implemented** | - Mocked all required repositories to return valid objects:<br> &nbsp;&nbsp;• `BookRepository` returned a `Book`<br> &nbsp;&nbsp;• `CounterpartyRepository` returned a `Counterparty`<br> &nbsp;&nbsp;• `TradeStatusRepository` returned a `TradeStatus`<br> &nbsp;&nbsp;• `TradeRepository` returned a `Trade`<br> &nbsp;&nbsp;• `TradeLegRepository` returned a `TradeLeg` with a monthly schedule<br> &nbsp;&nbsp;• `CashflowRepository` verified for saves<br> - Assigned a `Schedule` of `"1M"` to the `TradeLeg`<br> - Executed `tradeService.createTrade(tradeDTO)` to trigger cashflow creation<br> - Corrected the assertion to verify the total number of cashflows saved: 24 (2 legs × 12 months) |
| **Verification / Result** | The test now passes. It confirms that `createTrade()` generates monthly cashflows for each trade leg and saves the expected number of cashflows to the repository. The test validates both the correctness of cashflow generation and repository interactions. |

---




