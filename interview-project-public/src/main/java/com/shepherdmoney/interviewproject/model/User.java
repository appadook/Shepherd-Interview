package com.shepherdmoney.interviewproject.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Table(name = "MyUser")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    private String name;

    private String email;

    // TODO: User's credit card
    // HINT: A user can have one or more, or none at all. We want to be able to query credit cards by user
    //       and user by a credit card.

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<CreditCard> creditCards = new ArrayList<>();

    /**
     * Adds a credit card to this user's list of credit cards and sets this user as the owner of the credit card.
     * @param creditCard the credit card to be added to this user's collection of credit cards. It must not be null.
     * @throws NullPointerException if the provided creditCard is null.
     */
    public void addCreditCard(CreditCard creditCard) {
        creditCards.add(creditCard);
        creditCard.setOwner(this);
    }
}
