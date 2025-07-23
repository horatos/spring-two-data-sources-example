package com.example;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.SQLException;

/**
 * Service handling operations that require a TransactionTemplate and JPA.
 */
@Service
public class TxService {

    private final TransactionTemplate txTemplateOne;
    private final TransactionTemplate txTemplateTwo;

    @PersistenceContext(unitName = "emfOne")
    private EntityManager entityManagerOne;

    @PersistenceContext(unitName = "emfTwo")
    private EntityManager entityManagerTwo;

    @Autowired
    public TxService(@Qualifier("txTemplateOne") TransactionTemplate txTemplateOne,
                     @Qualifier("txTemplateTwo") TransactionTemplate txTemplateTwo) {
        this.txTemplateOne = txTemplateOne;
        this.txTemplateTwo = txTemplateTwo;
    }

    public String getWithTransactionTemplate(int id) throws SQLException {
        EntityManager em = (id % 2 == 0) ? entityManagerOne : entityManagerTwo;
        TransactionTemplate tt = (id % 2 == 0) ? txTemplateOne : txTemplateTwo;
        try {
            return tt.execute(status -> {
                Entry entry = em.find(Entry.class, id);
                if (entry == null) {
                    return null;
                } else {
                    return entry.s;
                }
            });
        } catch (RuntimeException e) {
            if (e.getCause() instanceof SQLException se) {
                throw se;
            }
            throw e;
        }
    }

    public void postWithTransactionTemplate(int id, String s) throws SQLException {
        EntityManager em = (id % 2 == 0) ? entityManagerOne : entityManagerTwo;
        TransactionTemplate tt = (id % 2 == 0) ? txTemplateOne : txTemplateTwo;
        try {
            tt.executeWithoutResult(status -> {
                Entry entry = new Entry();
                entry.id = id;
                entry.s = s;
                em.persist(entry);
            });
        } catch (RuntimeException e) {
            if (e.getCause() instanceof SQLException se) {
                throw se;
            }
            throw e;
        }
    }
}
