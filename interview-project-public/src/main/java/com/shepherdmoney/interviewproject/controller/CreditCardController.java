package com.shepherdmoney.interviewproject.controller;

import com.shepherdmoney.interviewproject.model.BalanceHistory;
import com.shepherdmoney.interviewproject.model.CreditCard;
import com.shepherdmoney.interviewproject.model.User;
import com.shepherdmoney.interviewproject.repository.CreditCardRepository;
import com.shepherdmoney.interviewproject.repository.UserRepository;
import com.shepherdmoney.interviewproject.vo.request.AddCreditCardToUserPayload;
import com.shepherdmoney.interviewproject.vo.request.UpdateBalancePayload;
import com.shepherdmoney.interviewproject.vo.response.CreditCardView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
public class CreditCardController {
    // TODO: wire in CreditCard repository here (~1 line)

    @Autowired
    CreditCardRepository creditCardRepository;

    @Autowired
    UserRepository userRepository;


    /**
     * Associates a new credit card with a user identified by the user ID provided in the payload.
     * This method takes user and credit card details from the payload, creates a new CreditCard entity,
     * sets the user as the owner of the credit card, and saves it to the database. If the specified user
     * does not exist, it returns a BadRequest response.
     * @param payload contains the user ID and the credit card number to associate.
     * @return ResponseEntity with the ID of the newly created credit card if successful, or BadRequest if the user does not exist.
     */
    @PostMapping("/credit-card")
    public ResponseEntity<Integer> addCreditCardToUser(@RequestBody AddCreditCardToUserPayload payload) {
        // TODO: Create a credit card entity, and then associate that credit card with user with given userId
        //       Return 200 OK with the credit card id if the user exists and credit card is successfully associated with the user
        //       Return other appropriate response code for other exception cases
        //       Do not worry about validating the card number, assume card number could be any arbitrary format and length
        Optional<User> userOptional = userRepository.findById(payload.getUserId());
        if (!userOptional.isPresent()) {
            return ResponseEntity.badRequest().body(400);
        }

        User user = userOptional.get();
        CreditCard creditCard = new CreditCard();
        creditCard.setNumber(payload.getCardNumber());
        creditCard.setOwner(user);
        creditCardRepository.save(creditCard);
        return ResponseEntity.ok(creditCard.getId());

    }

    /**
     * Retrieves all credit cards associated with a specified user ID.
     * This method finds all credit cards linked to the given user ID and constructs a list of CreditCardView
     * objects to provide essential credit card details. If the user has no credit cards, an empty list is returned.
     * @param userId the ID of the user whose credit cards are to be retrieved.
     * @return ResponseEntity containing a list of CreditCardView objects, or an empty list if no credit cards are found.
     */

    @GetMapping("/credit-card:all")
    public ResponseEntity<List<CreditCardView>> getAllCardOfUser(@RequestParam int userId) {
        // TODO: return a list of all credit card associated with the given userId, using CreditCardView class
        //       if the user has no credit card, return empty list, never return null
        List<CreditCard> cards = creditCardRepository.findByUserId(userId);
        List<CreditCardView> cardViews = cards.stream()
                .map(card -> CreditCardView.builder()
                        .issuanceBank(card.getIssuanceBank())
                        .number(card.getNumber())
                        .build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(cardViews);
    }

    /**
     * Retrieves the user ID associated with a given credit card number.
     * This method searches for a credit card by its number. If found, it returns the ID of the user who owns the credit card.
     * If no credit card is found, or if the card is not associated with any user, a BadRequest is returned.
     * @param creditCardNumber the number of the credit card whose associated user ID is required.
     * @return ResponseEntity containing the user ID if found, or BadRequest if no such credit card exists or it is not linked to any user.
     */
    @GetMapping("/credit-card:user-id")
    public ResponseEntity<Integer> getUserIdForCreditCard(@RequestParam String creditCardNumber) {
        // TODO: Given a credit card number, efficiently find whether there is a user associated with the credit card
        //       If so, return the user id in a 200 OK response. If no such user exists, return 400 Bad Request
        Optional<CreditCard> card = creditCardRepository.findByNumber(creditCardNumber);
        if (!card.isPresent() || card.get().getUser() == null) {
            return ResponseEntity.badRequest().body(200);
        }
        return ResponseEntity.ok(card.get().getUser().getId());
    }

    /**
     * Updates the balance history for credit cards based on the provided transaction payloads.
     * Each payload contains a credit card number, a date, and a new balance amount. This method updates the balance history
     * of each card, adding new entries or updating existing ones as necessary. It also ensures that all entries are correctly
     * ordered by date and fills any gaps in the balance history up to the current date. A response is generated for each update,
     * indicating success or failure (e.g., if a credit card number is not found).
     *
     * @param payload an array of UpdateBalancePayload, each representing an update to be applied to a credit card's balance history.
     * @return ResponseEntity containing a summary of the results for each transaction processed.
     */

    @PostMapping("/credit-card:update-balance")
    public ResponseEntity<String> updateCreditCardBalances(@RequestBody UpdateBalancePayload[] payload) {
        //TODO: Given a list of transactions, update credit cards' balance history.
        //      1. For the balance history in the credit card
        //      2. If there are gaps between two balance dates, fill the empty date with the balance of the previous date
        //      3. Given the payload `payload`, calculate the balance different between the payload and the actual balance stored in the database
        //      4. If the different is not 0, update all the following budget with the difference
        //      For example: if today is 4/12, a credit card's balanceHistory is [{date: 4/12, balance: 110}, {date: 4/10, balance: 100}],
        //      Given a balance amount of {date: 4/11, amount: 110}, the new balanceHistory is
        //      [{date: 4/12, balance: 120}, {date: 4/11, balance: 110}, {date: 4/10, balance: 100}]
        //      This is because
        //      1. You would first populate 4/11 with previous day's balance (4/10), so {date: 4/11, amount: 100}
        //      2. And then you observe there is a +10 difference
        //      3. You propagate that +10 difference until today
        //      Return 200 OK if update is done and successful, 400 Bad Request if the given card number
        //        is not associated with a card.
        // Retrieve the credit card using the number provided in the payload
        StringBuilder responseMessage = new StringBuilder();

        for (UpdateBalancePayload singlePayload : payload) {
            Optional<CreditCard> cardOpt = creditCardRepository.findByNumber(singlePayload.getCreditCardNumber());
            if (!cardOpt.isPresent()) {
                responseMessage.append("Credit card not found for number: ").append(singlePayload.getCreditCardNumber()).append(". ");
                continue;
            }

            CreditCard card = cardOpt.get();
            LocalDate balanceDate = singlePayload.getBalanceDate();
            double balanceAmount = singlePayload.getBalanceAmount();


            Optional<BalanceHistory> existingEntryOpt = card.getBalanceHistory().stream()
                    .filter(b -> b.getDate().isEqual(balanceDate))
                    .findFirst();

            if (existingEntryOpt.isPresent()) {
                existingEntryOpt.get().setBalance(balanceAmount);
            } else {
                BalanceHistory newEntry = new BalanceHistory();
                newEntry.setDate(balanceDate);
                newEntry.setBalance(balanceAmount);
                newEntry.setCreditCard(card);
                card.getBalanceHistory().add(newEntry);
                card.getBalanceHistory().sort(Comparator.comparing(BalanceHistory::getDate).reversed());
            }

            creditCardRepository.save(card);
            responseMessage.append("Balance updated successfully for card number: ").append(singlePayload.getCreditCardNumber()).append(". ");
        }

        return ResponseEntity.ok(responseMessage.toString());
    }
    
}
