package l2p.gameserver.database;

import l2p.commons.dbcp.BasicDataSource;
import l2p.gameserver.Config;

import java.sql.Connection;
import java.sql.SQLException;


public class DatabaseFactory extends BasicDataSource {
    private static DatabaseFactory _instance = new DatabaseFactory();

    public static DatabaseFactory getInstance() throws SQLException {
        return _instance;
    }

    public DatabaseFactory() {
        super(Config.DATABASE_DRIVER, Config.DATABASE_URL, Config.DATABASE_LOGIN, Config.DATABASE_PASSWORD, Config.DATABASE_MAX_CONNECTIONS, Config.DATABASE_MAX_CONNECTIONS, Config.DATABASE_MAX_IDLE_TIMEOUT, Config.DATABASE_IDLE_TEST_PERIOD, false);
    }

    public DatabaseFactory(String driver, String url, String login, String pass, int maxconn, int maxIdle, int idleTime, int idleTest, boolean prepared) throws SQLException {
        super(driver, url, login, pass, maxconn, maxIdle, idleTime, idleTest, prepared);
    }

    @Override
    public Connection getConnection() throws SQLException {
        return getConnection(null);
    }
}