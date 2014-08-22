package edu.yale.library.ladybird.kernel.db;


import edu.yale.library.ladybird.kernel.AppConfigException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;


/**
 *
 */
public final class DerbySchemaUtil {
    private static final Logger logger = LoggerFactory.getLogger(DerbySchemaUtil.class);

    /**
     * Init schema
     *
     * @throws edu.yale.library.ladybird.kernel.AppConfigException
     */
    protected void init() {
        try {
            final Connection conn = DriverManager.getConnection(Config.PROTOCOL
                    + Config.DB + ";create=true", Config.PROPS);
            logger.debug("Connected to DB and created a schema: " + Config.DB);
            conn.setAutoCommit(false);
            final Statement statement = conn.createStatement();

            logger.trace("Creating table(s)");

            final SchemaBean schemaBean = new SchemaBean();
            final Map<String, String> m = schemaBean.getSchema();

            if (m == null || 0 == m.size()) {
                throw new AppConfigException("Schema empty");
            }
            Iterator it = m.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry p = (Map.Entry) it.next();
                logger.trace("Executing create statement for table={}", p.getKey());
                statement.execute(p.getValue().toString());
            }
            conn.commit();
            logger.debug("Created table(s)");
        } catch (SQLException e) {
            throw new AppConfigException(e);
        }
    }

    /**
     * Remove schema
     *
     * @throws edu.yale.library.ladybird.kernel.AppConfigException
     */
    protected void killSchema() {
        try {
            final Connection conn = DriverManager.getConnection(Config.PROTOCOL
                    + Config.DB + ";create=false", Config.PROPS);
            conn.setAutoCommit(false);
            final Statement statement = conn.createStatement();

            logger.debug("Killing table(s)");

            final SchemaBean schemaBean = new SchemaBean();
            final Map<String, String> m = schemaBean.getKillSchema();

            if (m == null || 0 == m.size()) {
                throw new AppConfigException("Schema empty");
            }
            Iterator it = m.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry p = (Map.Entry) it.next();
                logger.trace("Executing drop statement for table={}",  p.getKey());
                statement.execute(p.getValue().toString());
            }
            conn.commit();
        } catch (SQLException e) {
            throw new AppConfigException(e);
        }
    }

    /**
     * General config settings
     */
    private static final class Config {
        static final String PROTOCOL = "jdbc:derby:";
        static final String DB = "memory:pamoja";
        static final String DB_USER = "pamoja";
        static final Properties PROPS = new Properties();

        static {
            PROPS.put("user", Config.DB_USER);
        }

        private Config() {
        }
    }


}
