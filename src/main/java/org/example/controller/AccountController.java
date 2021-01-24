package org.example.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import org.example.model.Account;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
public class AccountController {

    private List<Account> accounts = new LinkedList<>();

    private ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping
    public List<Account> getAccounts() {
        return accounts;
    }

    @PostMapping
    public Account createAccount(@RequestBody Account accountBody) {
        Account account = new Account(UUID.randomUUID(), accountBody.getBalance(), accountBody.getOwner());
        accounts.add(account);
        return account;
    }

    @PutMapping
    public Account updateAccount(@RequestBody Account accountBody) {
        if (accountBody.getId() != null) {
            Account account = getAccounts().stream().filter(a -> a.getId().equals(accountBody.getId())).findFirst()
                    .orElseThrow(() -> new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Account with given ID not found"));
            account.setOwner(accountBody.getOwner());
            account.setBalance(accountBody.getBalance());
            return account;
        } else {
            Account newAccount = new Account(UUID.randomUUID(), accountBody.getBalance(), accountBody.getOwner());
            accounts.add(newAccount);
            return newAccount;
        }
    }

    @PatchMapping(path = "/{id}", consumes = "application/json-patch+json")
    public Account patchAccount(@RequestBody JsonPatch patch, @PathVariable UUID id) throws JsonPatchException, JsonProcessingException {
        Account account = getAccounts().stream().filter(a -> a.getId().equals(id)).findFirst()
                .orElseThrow(() -> new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Account with given ID not found"));
        return updateAccountCollection(applyPatchToAccount(patch, account));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        getAccounts().removeIf(a -> a.getId().equals(id));
    }

    private Account applyPatchToAccount(JsonPatch patch, Account account) throws JsonPatchException, JsonProcessingException {
        JsonNode patched = patch.apply(objectMapper.convertValue(account, JsonNode.class));
        return objectMapper.treeToValue(patched, Account.class);
    }

    private Account getAccount(UUID id) {
        return this.accounts.stream().filter(a -> a.getId().equals(id)).findFirst().orElse(null);
    }

    private Account addAccount(Account account) {
        this.accounts.add(account);
        return account;
    }

    private Account updateAccountCollection(Account account) {
        accounts = this.accounts.stream().map(a -> a.getId().equals(account.getId()) ? account : a).collect(Collectors.toList());
        return account;
    }
}
