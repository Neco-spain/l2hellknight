/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2js;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.eclipse.persistence.config.PersistenceUnitProperties;

import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPDataSource;
import com.l2js.versionning.Version.CoreVersion;

/**
 * @author lord_rex & UnAfraid
 */
public final class L2DatabaseFactory
{
	protected static final Logger	_log	= Logger.getLogger(L2DatabaseFactory.class.getName());

	public static enum ProviderType
	{
		MySql, MsSql
	}

	private static final class SingletonHolder
	{
		private static final L2DatabaseFactory	INSTANCE	= new L2DatabaseFactory();
	}

	public static L2DatabaseFactory getInstance()
	{
		return SingletonHolder.INSTANCE;
	}

	public static void close(Connection con)
	{
		if (con == null)
			return;

		try
		{
			con.close();
		}
		catch (SQLException e)
		{
			_log.log(Level.WARNING, "L2DatabaseFactory: Failed to close database connection!", e);
		}
	}

	private static final CoreVersion	_bonecp	= new CoreVersion(BoneCP.class);

	private final ProviderType			_providerType;
	private final BoneCPDataSource		_source;
	private final EntityManagerFactory	_entityManagerFactory;

	private L2DatabaseFactory()
	{
		_log.info("Initializing BoneCP [ version: " + _bonecp.getVersionNumber() + ", databaseDriver -> "
				+ Config.DATABASE_DRIVER + ", jdbcUrl -> " + Config.DATABASE_URL + ", maxConnectionsPerPartition -> "
				+ Config.DATABASE_MAX_CONNECTIONS + ", username -> " + Config.DATABASE_LOGIN + ", password -> "
				+ Config.DATABASE_PASSWORD + " ]");

		try
		{
			if (Config.DATABASE_MAX_CONNECTIONS < 10)
			{
				Config.DATABASE_MAX_CONNECTIONS = 10;
				_log.log(Level.WARNING, "at least " + Config.DATABASE_MAX_CONNECTIONS + " db connections are required.");
			}

			_source = new BoneCPDataSource();
			// _source.setAutoCommitOnClose(true);
			_source.getConfig().setDefaultAutoCommit(true);

			// _source.setInitialPoolSize(10);
			_source.getConfig().setPoolAvailabilityThreshold(10);
			//_source.setMinPoolSize(10);
			_source.getConfig().setMinConnectionsPerPartition(10);
			//_source.setMaxPoolSize(Config.DATABASE_MAX_CONNECTIONS);
			_source.getConfig().setMaxConnectionsPerPartition(Config.DATABASE_MAX_CONNECTIONS);

			_source.setPartitionCount(3);

			_source.setAcquireRetryAttempts(0); // try to obtain connections indefinitely (0 = never quit)
			_source.setAcquireRetryDelayInMs(500); // 500 miliseconds wait before try to acquire connection again

			// if pool is exhausted
			_source.setAcquireIncrement(5); // if pool is exhausted, get 5 more connections at a time
			// cause there is a "long" delay on acquire connection
			// so taking more than one connection at once will make connection pooling
			// more effective.

			_source.setConnectionTimeoutInMs(0);

			// this "connection_test_table" is automatically created if not already there
			//_source.setAutomaticTestTable("connection_test_table");
			//_source.setTestConnectionOnCheckin(false);

			// testing OnCheckin used with IdleConnectionTestPeriod is faster than testing on checkout

			_source.setIdleConnectionTestPeriodInMinutes(1); // test idle connection every 60 sec
			// _source.setMaxIdleTime(1800); // 0 = idle connections never expire
			_source.setIdleMaxAgeInSeconds(1800);
			// *THANKS* to connection testing configured above
			// but I prefer to disconnect all connections not used
			// for more than 1 hour

			_source.setTransactionRecoveryEnabled(true);

			// enables statement caching, there is a "semi-bug" in c3p0 0.9.0 but in 0.9.0.2 and later it's fixed
			//_source.setMaxStatementsPerConnection(100);

			//_source.setBreakAfterAcquireFailure(false); // never fail if any way possible
			// setting this to true will make
			// c3p0 "crash" and refuse to work
			// till restart thus making acquire
			// errors "FATAL" ... we don't want that
			// it should be possible to recover
			_source.setDriverClass(Config.DATABASE_DRIVER);
			_source.setJdbcUrl(Config.DATABASE_URL);
			_source.setUsername(Config.DATABASE_LOGIN);
			_source.setPassword(Config.DATABASE_PASSWORD);

			/* Test the connection */
			_source.getConnection().close();

			if (Config.DATABASE_DRIVER.toLowerCase().contains("microsoft"))
				_providerType = ProviderType.MsSql;
			else
				_providerType = ProviderType.MySql;

			// ====================================================================================
			// initialization of the EclipseLink JPA
			//

			final Map<Object, Object> props = new HashMap<Object, Object>();
			props.put(PersistenceUnitProperties.NON_JTA_DATASOURCE, _source);
			
			EntityManagerFactory	emf = null;
			try
			{
				emf = Persistence.createEntityManagerFactory("default", props);;	

			}
			catch (Exception e)
			{
				_log.log(Level.WARNING, "", e);
			}
			_entityManagerFactory = emf;
			// test the entity manager
			_entityManagerFactory.createEntityManager().close();
		}
		catch (Exception e)
		{
			throw new Error("L2DatabaseFactory: Failed to init database connections: " + e, e);
		}
	}

	public void shutdown() throws Exception
	{
		try
		{
			_entityManagerFactory.close();
		}
		catch (Throwable t)
		{
			_log.log(Level.SEVERE, "", t);
		}

		_source.close();
	}

	public String safetyString(String... whatToCheck)
	{
		// NOTE: Use brace as a safty percaution just incase name is a reserved word
		String braceLeft = "`";
		String braceRight = "`";
		if (getProviderType() == ProviderType.MsSql)
		{
			braceLeft = "[";
			braceRight = "]";
		}

		String result = "";
		for (String word : whatToCheck)
		{
			if (!result.isEmpty())
				result += ", ";

			result += braceLeft + word + braceRight;
		}
		return result;
	}

	public Connection getConnection()
	{
		return getConnection(null);
	}

	public Connection getConnection(Connection con)
	{
		while (con == null)
		{
			try
			{
				con = _source.getConnection();
				//con = new L2DatabaseFactoryConnectionWrapper(_source.getConnection());
			}
			catch (SQLException e)
			{
				_log.log(Level.SEVERE, "L2DatabaseFactory: Failed to retrieve database connection!", e);
			}
		}

		return con;
	}

	public EntityManager getEntityManager()
	{
		return _entityManagerFactory.createEntityManager();
	}

	public int getBusyConnectionCount() throws SQLException
	{
		return _source.getTotalLeased();
	}

	public ProviderType getProviderType()
	{
		return _providerType;
	}

	// =========================================================
	// Method - Public
	public final String prepQuerySelect(String[] fields, String tableName, String whereClause,
			boolean returnOnlyTopRecord)
	{
		String msSqlTop1 = "";
		String mySqlTop1 = "";
		if (returnOnlyTopRecord)
		{
			if (getProviderType() == ProviderType.MsSql)
				msSqlTop1 = " Top 1 ";
			if (getProviderType() == ProviderType.MySql)
				mySqlTop1 = " Limit 1 ";
		}
		String query = "SELECT " + msSqlTop1 + safetyString(fields) + " FROM " + tableName + " WHERE " + whereClause
				+ mySqlTop1;
		return query;
	}
}
