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
    private final AnnotatedService annotatedService;

    @Autowired
    public Controller(Service service, TxService txService, AnnotatedService annotatedService) {
        this.service = service;
        this.txService = txService;
        this.annotatedService = annotatedService;
    }

    public String get(int id) throws SQLException {
        return service.get(id);
    }

    public void post(int id, String s) throws SQLException {
        service.post(id, s);
    }

    public String getWithAnnotations(int id) throws SQLException {
        if (id % 2 == 0) {
            return annotatedService.getFromOne(id);
        } else {
            return annotatedService.getFromTwo(id);
        }
    }

    public void postWithAnnotations(int id, String s) throws SQLException {
        if (id % 2 == 0) {
            annotatedService.postToOne(id, s);
        } else {
            annotatedService.postToTwo(id, s);
        }
    }

    public String getWithTransactionTemplate(int id) throws SQLException {
        return txService.getWithTransactionTemplate(id);
    }

    public void postWithTransactionTemplate(int id, String s) throws SQLException {
        txService.postWithTransactionTemplate(id, s);
    }
}
