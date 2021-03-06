package edu.yale.library.ladybird.persistence.dao;

import edu.yale.library.ladybird.entity.AccessconditionTarget;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 *
 */
public class AcessConditionTargetDaoTest extends AbstractPersistenceTest {

    {
        TestDaoInitializer.injectFields(this);
    }

    @Before
    public void init() {
        initDB();
    }

    @After
    public void stop() throws SQLException {
        //TODO
    }

    @Inject
    private AccessconditionTargetDAO dao;

    @Test
    public void testSave() {
        final AccessconditionTarget item = new AccessconditionTarget();
        item.setLabel("target label");
        item.setDate(new Date());
        item.setUserId(0);
        List list = null;
        try {
            dao.save(item);
            list = dao.findAll();

        } catch (Throwable e) {
            e.printStackTrace();
            fail("Error testing saving or finding item");
        }

        assertEquals("Item count incorrect", list.size(), 1);
        final AccessconditionTarget itemDB = (AccessconditionTarget) list.get(0);
        assertEquals("Value mismatch", itemDB.getLabel(), "target label");
        assertEquals("Value mismatch", itemDB.getUserId(), 0);

    }

}
