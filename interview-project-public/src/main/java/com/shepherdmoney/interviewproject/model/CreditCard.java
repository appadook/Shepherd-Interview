package com.shepherdmoney.interviewproject.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.net.ssl.SSLSession;
import java.time.LocalDate;
import java.util.*;

@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class CreditCard {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    private String issuanceBank;

    private String number;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "creditCard")
    private List<BalanceHistory> balanceHistory = new ArrayList<>();


    /**
     * Adds a new balance entry to this credit card and ensures that the balance history
     * is kept in chronological order.
     * @param date the date of the balance entry, must not be null
     * @param balance the balance amount for the given date
     * @throws NullPointerException if the date is null
     */
    public void addBalanceEntry(LocalDate date, double balance) {
        BalanceHistory newEntry = new BalanceHistory();
        newEntry.setDate(date);
        newEntry.setBalance(balance);
        this.balanceHistory.add(newEntry);
        this.balanceHistory.sort((e1, e2) -> e2.getDate().compareTo(e1.getDate()));

        fillDateGaps();
    }

    // TODO: Credit card's owner. For detailed hint, please see User class
    // Some field here <> owner;


    // TODO: Credit card's balance history. It is a requirement that the dates in the balanceHistory 
    //       list must be in chronological order, with the most recent date appearing first in the list. 
    //       Additionally, the last object in the "list" must have a date value that matches today's date, 
    //       since it represents the current balance of the credit card.
    //       This means that if today is 04-16, and the list begin as empty, you receive a payload for 04-13,
    //       you should fill the list up until 04-16. For example:
    //       [
    //         {date: '2023-04-10', balance: 800},
    //         {date: '2023-04-11', balance: 1000},
    //         {date: '2023-04-12', balance: 1200},
    //         {date: '2023-04-13', balance: 1100},
    //         {date: '2023-04-16', balance: 900},
    //       ]

    /**
     * Sets the owner of this credit card.
     * @param user the user to set as the owner of this credit card, must not be null
     * @throws NullPointerException if the user is null
     */
    public void setOwner(User user) {
        this.user = user;
    }

    /**
     * Fills any gaps in the balance history between the most recent entry date and today.
     * For each missing date, a new BalanceHistory entry is created with the same balance
     * as the last recorded entry.
     * These new entries are added in chronological order up to the current date.
     */
    private void fillDateGaps() {
        if (balanceHistory.isEmpty()) return;

        BalanceHistory lastEntry = balanceHistory.get(0);
        LocalDate lastDate = lastEntry.getDate();
        LocalDate today = LocalDate.now();

        while (!lastDate.isEqual(today)) {
            lastDate = lastDate.plusDays(1);
            BalanceHistory newEntry = new BalanceHistory();
            newEntry.setDate(lastDate);
            newEntry.setBalance(lastEntry.getBalance());
            newEntry.setCreditCard(this);
            balanceHistory.add(0, newEntry);
        }
    }

    /**
     * Retrieves the current balance of the credit card based on the most recent balance entry.
     * @return the current balance for today's date, or 0.0 if no entry exists
     */
    public double getCurrentBalance() {
        LocalDate today = LocalDate.now();
        return balanceHistory.stream()
                .filter(e -> e.getDate().isEqual(today))
                .findFirst()
                .map(BalanceHistory::getBalance)
                .orElse(0.0);
    }


    // ADDITIONAL NOTE: For the balance history, you can use any data structure that you think is appropriate.
    //        It can be a list, array, map, pq, anything. However, there are some suggestions:
    //        1. Retrieval of a balance of a single day should be fast
    //        2. Traversal of the entire balance history should be fast
    //        3. Insertion of a new balance should be fast
    //        4. Deletion of a balance should be fast
    //        5. It is possible that there are gaps in between dates (note the 04-13 and 04-16)
    //        6. In the condition that there are gaps, retrieval of "closest **previous**" balance date should also be fast. Aka, given 4-15, return 4-13 entry tuple
}
