package com.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.SQLException;

/**
 * Simple component acting as a controller in a web application.
 */
@Component
public class Controller {

    private final Service service;
    private final TxService txService;

    @Autowired
    public Controller(Service service, TxService txService) {
        this.service = service;
        this.txService = txService;
    }

    public String get(int id) throws SQLException {
        return service.get(id);
    }

    public void post(int id, String s) throws SQLException {
        service.post(id, s);
    }

    public String getWithTransactionTemplate(int id) throws SQLException {
        return txService.getWithTransactionTemplate(id);
    }

    public void postWithTransactionTemplate(int id, String s) throws SQLException {
        txService.postWithTransactionTemplate(id, s);
    }
}
