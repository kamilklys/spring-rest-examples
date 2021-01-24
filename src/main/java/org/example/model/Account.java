package org.example.model;

import java.util.UUID;

public class Account {

    private UUID id;
    private float balance;
    private UUID owner;

    public Account() {
    }

    public Account(UUID id, float balance, UUID owner) {
        this.id = id;
        this.balance = balance;
        this.owner = owner;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public float getBalance() {
        return balance;
    }

    public void setBalance(float balance) {
        this.balance = balance;
    }

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }
}
