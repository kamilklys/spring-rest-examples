package org.example.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import org.example.model.Account;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private List<Account> accounts = new LinkedList<>();

    private ObjectMapper objectMapper = new ObjectMapper();

    ExecutorService executorService = Executors.newFixedThreadPool(10);

    @PostMapping
    public Account createAccount(@RequestBody Account accountBody) {
        Account account = new Account(UUID.randomUUID(), accountBody.getBalance(), accountBody.getOwner());
        accounts.add(account);
        return account;
    }

    @PutMapping
    public Account updateAccount(@RequestBody Account accountBody) {
        if (accountBody.getId() != null) {
            Account account = getAccountsXml(accountBody.getId()).get(0);
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
        Account account = getAccountsXml(id).get(0);
        return updateAccountCollection(applyPatchToAccount(patch, account));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        getAccountsXml(null).removeIf(a -> a.getId().equals(id));
    }

    private Account applyPatchToAccount(JsonPatch patch, Account account) throws JsonPatchException, JsonProcessingException {
        JsonNode patched = patch.apply(objectMapper.convertValue(account, JsonNode.class));
        return objectMapper.treeToValue(patched, Account.class);
    }

    @PostMapping(value = "/top/up/{id}", consumes = MediaType.TEXT_PLAIN_VALUE)
    public UUID changeAccountBalanceAsync(@PathVariable UUID id, @RequestBody String amount) {
        executorService.submit(() -> {
            try {
                Thread.sleep(10000);
                getAccountsXml(id).get(0).setBalance(Integer.parseInt(amount));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        return id;
    }

    @GetMapping(value = "/{accountId}")
    public Account getAccount(@PathVariable UUID accountId) throws HttpClientErrorException {
        return this.accounts.stream().filter(a -> a.getId().equals(accountId)).findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account with given ID not found"));
    }

    @GetMapping(produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public List<Account> getAccountsXml(@RequestParam(required = false) UUID id) {
        if (id != null) {
            return this.accounts.stream().filter(a -> a.getId().equals(id)).collect(Collectors.toList());
        }
        return accounts;
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
