package l2rt.gameserver.model;

import l2rt.database.DatabaseUtils;
import l2rt.database.FiltredPreparedStatement;
import l2rt.database.L2DatabaseFactory;
import l2rt.database.ThreadConnection;
import l2rt.gameserver.model.L2Macro.L2MacroCmd;
import l2rt.gameserver.network.serverpackets.SendMacroList;
import l2rt.util.GArray;
import l2rt.util.Strings;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MacroList
{
	private static Logger _log = Logger.getLogger(MacroList.class.getName());

	private long ownerStoreId = 0;
	private int _revision, _macroId;
	private HashMap<Integer, L2Macro> _macroses = new HashMap<Integer, L2Macro>();

	public MacroList(L2Player owner)
	{
		ownerStoreId = owner.getStoredId();
		_revision = 1;
		_macroId = 1000;
	}

	public int getRevision()
	{
		return _revision;
	}

	public L2Macro[] getAllMacroses()
	{
		return _macroses.values().toArray(new L2Macro[_macroses.size()]);
	}

	public L2Macro getMacro(int id)
	{
		return _macroses.get(id - 1);
	}

	public void registerMacro(L2Macro macro)
	{
		if(macro.id == 0)
		{
			macro.id = _macroId++;
			while(_macroses.get(macro.id) != null)
				macro.id = _macroId++;
			_macroses.put(macro.id, macro);
			registerMacroInDb(macro);
		}
		else
		{
			L2Macro old = _macroses.put(macro.id, macro);
			if(old != null)
				deleteMacroFromDb(old);
			registerMacroInDb(macro);
		}
		sendUpdate();
	}

	public void deleteMacro(int id)
	{
		L2Macro toRemove = _macroses.get(id);
		if(toRemove != null)
			deleteMacroFromDb(toRemove);
		_macroses.remove(id);
		//		L2ShortCut[] allShortCuts = _owner.getAllShortCuts();
		//		for(L2ShortCut sc : allShortCuts) {
		//			if(sc.getId() == id && sc.getType() == L2ShortCut.TYPE_MACRO)
		//				_owner.sendPacket(new ShortCutRegister(sc.getSlot(), 0, 0, 0, sc.getPage()));
		//		}
		sendUpdate();
	}

	public void sendUpdate()
	{
		L2Player player = getPlayer();
		if(player == null)
			return;

		_revision++;
		L2Macro[] all = getAllMacroses();
		if(all.length == 0)
			player.sendPacket(new SendMacroList(_revision, all.length, null));
		else
			for(L2Macro m : all)
				player.sendPacket(new SendMacroList(_revision, all.length, m));
	}

	private void registerMacroInDb(L2Macro macro)
	{
		L2Player player = getPlayer();
		if(player == null)
			return;

		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("REPLACE INTO character_macroses (char_obj_id,id,icon,name,descr,acronym,commands) values(?,?,?,?,?,?,?)");
			statement.setInt(1, player.getObjectId());
			statement.setInt(2, macro.id);
			statement.setInt(3, macro.icon);
			statement.setString(4, macro.name);
			statement.setString(5, macro.descr);
			statement.setString(6, macro.acronym);
			StringBuffer sb = new StringBuffer();
			for(L2MacroCmd cmd : macro.commands)
			{
				sb.append(cmd.type).append(',');
				sb.append(cmd.d1).append(',');
				sb.append(cmd.d2);
				if(cmd.cmd != null && cmd.cmd.length() > 0)
					sb.append(',').append(cmd.cmd);
				sb.append(';');
			}
			statement.setString(7, sb.toString());
			statement.execute();
		}
		catch(Exception e)
		{
			_log.log(Level.WARNING, "could not store macro: " + macro.toString(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	/**
	 * @param shortcut
	 */
	private void deleteMacroFromDb(L2Macro macro)
	{
		L2Player player = getPlayer();
		if(player == null)
			return;

		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			statement = con.prepareStatement("DELETE FROM character_macroses WHERE char_obj_id=? AND id=?");
			statement.setInt(1, player.getObjectId());
			statement.setInt(2, macro.id);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.log(Level.WARNING, "could not delete macro:", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	public void restore()
	{
		L2Player player = getPlayer();
		if(player == null)
			return;

		_macroses.clear();
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT char_obj_id, id, icon, name, descr, acronym, commands FROM character_macroses WHERE char_obj_id=?");
			statement.setInt(1, player.getObjectId());
			rset = statement.executeQuery();
			while(rset.next())
				try
				{
					int id = rset.getInt("id");
					int icon = rset.getInt("icon");
					String name = Strings.stripSlashes(rset.getString("name"));
					String descr = Strings.stripSlashes(rset.getString("descr"));
					String acronym = Strings.stripSlashes(rset.getString("acronym"));
					GArray<L2MacroCmd> commands = new GArray<L2MacroCmd>();
					StringTokenizer st1 = new StringTokenizer(rset.getString("commands"), ";");
					while(st1.hasMoreTokens())
					{
						StringTokenizer st = new StringTokenizer(st1.nextToken(), ",");
						int type = Integer.parseInt(st.nextToken());
						int d1 = Integer.parseInt(st.nextToken());
						int d2 = Integer.parseInt(st.nextToken());
						String cmd = "";
						if(st.hasMoreTokens())
							cmd = st.nextToken();
						L2MacroCmd mcmd = new L2MacroCmd(commands.size(), type, d1, d2, cmd);
						commands.add(mcmd);
					}

					L2Macro m = new L2Macro(id, icon, name, descr, acronym, commands.toArray(new L2MacroCmd[commands.size()]));
					_macroses.put(m.id, m);
				}
				catch(NoSuchElementException e)
				{
					// skip incorrect macros from parsing
					_log.warning(player.getName() + "/" + player.getObjectId() + ": bad macros parsing (NoSuchElementException) - check database manualy");
					e.printStackTrace();
				}
				catch(NumberFormatException e)
				{
					// skip incorrect macros from parsing
					_log.warning(player.getName() + "/" + player.getObjectId() + ": bad macros parsing (NumberFormatException) - check database manualy");
					e.printStackTrace();
				}
		}
		catch(Exception e)
		{
			_log.log(Level.WARNING, "could not restore shortcuts:", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
	}

	public L2Player getPlayer()
	{
		return L2ObjectsStorage.getAsPlayer(ownerStoreId);
	}
}