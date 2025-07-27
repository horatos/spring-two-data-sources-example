package com.example;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

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

    public String getWithTransactionTemplate(int id) {
        EntityManager em = (id % 2 == 0) ? entityManagerOne : entityManagerTwo;
        TransactionTemplate tt = (id % 2 == 0) ? txTemplateOne : txTemplateTwo;
        return tt.execute(status -> {
            Entry entry = em.find(Entry.class, id);
            return entry == null ? null : entry.s;
        });
    }

    public void postWithTransactionTemplate(int id, String s) {
        EntityManager em = (id % 2 == 0) ? entityManagerOne : entityManagerTwo;
        TransactionTemplate tt = (id % 2 == 0) ? txTemplateOne : txTemplateTwo;

        tt.executeWithoutResult(status -> {
            Entry entry = new Entry();
            entry.id = id;
            entry.s = s;
            em.persist(entry);
        });
    }
}
