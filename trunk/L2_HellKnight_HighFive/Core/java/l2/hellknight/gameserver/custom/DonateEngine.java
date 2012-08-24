package l2.hellknight.gameserver.custom;

import javolution.text.TypeFormat;
import l2.hellknight.gameserver.datatables.TeleportLocationTable;
import l2.hellknight.gameserver.model.L2Object;
import l2.hellknight.gameserver.model.L2World;
import l2.hellknight.gameserver.model.L2WorldRegion;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * @author l2open-team
 */
public class DonateEngine
{
	public static final class RemoteDonateDaemon extends Thread
	{
		private static final Logger _log = Logger.getLogger(TeleportLocationTable.class.getName());
		
		/**
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run()
		{
			for (;;)
			{
				try
				{
					Thread.sleep(ITEMS_REQUEST_DELAY);
				}
				catch (final InterruptedException e)
				{
				}
				// get all players if error continue to next time not use SQL
				final ArrayList<L2PcInstance> _allPlayers = new ArrayList<L2PcInstance>();
				try
				{
					_allPlayers.clear();
					for (final L2WorldRegion regions[] : L2World.getInstance().getAllWorldRegions())
						for (final L2WorldRegion r : regions)
							if (r.isActive())
								for (final L2Object object : r.getVisibleObjects().values())
									if (object instanceof L2PcInstance)
										_allPlayers.add((L2PcInstance) object);
					// if not found player skill
					if (_allPlayers.isEmpty())
						continue;
				}
				catch (final Exception e)
				{
					_log.warning("DonateError: ");
					continue;
				}
				Connection conn = null;
				try
				{
					conn = DriverManager.getConnection(DATABASE_URL + "?" + "user=" + DATABASE_LOGIN + "&password=" + DATABASE_PASSWORD);
					final PreparedStatement prepareStatement = conn.prepareStatement("SELECT char_name, item_id, item_count FROM donate_items");
					final ResultSet rset = prepareStatement.executeQuery();
					while (rset.next())
					{
						final String name = rset.getString("char_name");
						final int itemId = rset.getInt("item_id");
						final long itemCount = rset.getLong("item_count");
						try
						{
							// found player
							for (final L2PcInstance player : _allPlayers)
							{
								if (player == null)
									continue;
								// if names equals give item
								if (player.getName().equals(name))
								{
									player.addItem("DONATE-SYSTEM", itemId, itemCount, null, true);
									_log.warning("DONATE-SYSTEM: AddItem(" + itemId + ", " + itemCount + ") to player " + player.getName());
									// if added now delete from base
									final PreparedStatement prepareStatement2 = conn.prepareStatement("DELETE FROM donate_items WHERE char_name=? AND item_id=?");
									prepareStatement2.setString(1, name);
									prepareStatement2.setInt(2, itemId);
									prepareStatement2.execute();
									prepareStatement2.close();
								}
							}
						}
						catch (final Exception e)
						{
							_log.warning("DonateError: ");
							continue;
						}
					}
					rset.close();
					prepareStatement.close();
				}
				catch (final SQLException ex)
				{
					// handle any errors
					_log.warning("SQLException: " + ex.getMessage());
					_log.warning("SQLState: " + ex.getSQLState());
					_log.warning("VendorError: " + ex.getErrorCode());
				}
				catch (final Exception ex)
				{
					// handle any errors
					_log.warning("Exception: ");
				}
				finally
				{
					try
					{
						if (conn != null)
						{
							conn.commit();
							conn.close();
						}
					}
					catch (final SQLException e)
					{
					}
				}
			}
		}
	}
	
	private boolean ENABLED;
	public static String DATABASE_URL;
	public static String DATABASE_LOGIN;
	public static String DATABASE_PASSWORD;
	public static int ITEMS_REQUEST_DELAY;
	static final Logger _log = Logger.getLogger(DonateEngine.class.getName());
	public static final String OPTIONS_FILE = "./config/donatesystem.properties";
	private static DonateEngine _instance;
	
	public static DonateEngine getInstance()
	{
		if (_instance == null)
			_instance = new DonateEngine();
		return _instance;
	}
	
	public DonateEngine()
	{
		_log.info("Donate-system loaded.");
		try
		{
			final Properties settings = new Properties();
			final InputStream is = new FileInputStream(new File(OPTIONS_FILE));
			settings.load(is);
			is.close();
			ENABLED = TypeFormat.parseBoolean(settings.getProperty("Enabled", "False"));
			DATABASE_URL = settings.getProperty("URL", "jdbc:mysql://localhost/donate");
			DATABASE_LOGIN = settings.getProperty("Login", "root");
			DATABASE_PASSWORD = settings.getProperty("Password", "");
			ITEMS_REQUEST_DELAY = TypeFormat.parseInt(settings.getProperty("RequestItemsDelay", "300")) * 1000;
		}
		catch (final Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + OPTIONS_FILE + " File.");
		}
		if (!this.ENABLED)
			return;
		final Thread _daemon = new RemoteDonateDaemon();
		_daemon.setDaemon(true);
		_daemon.setPriority(Thread.MIN_PRIORITY);
		_daemon.start();
	}
}
