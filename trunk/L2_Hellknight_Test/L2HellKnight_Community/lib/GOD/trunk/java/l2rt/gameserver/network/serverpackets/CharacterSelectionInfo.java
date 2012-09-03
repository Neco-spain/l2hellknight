package l2rt.gameserver.network.serverpackets;

import java.sql.ResultSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import l2rt.Config;
import l2rt.database.DatabaseUtils;
import l2rt.database.FiltredPreparedStatement;
import l2rt.database.L2DatabaseFactory;
import l2rt.database.ThreadConnection;
import l2rt.gameserver.instancemanager.PlayerManager;
import l2rt.gameserver.model.CharSelectInfoPackage;
import l2rt.gameserver.tables.CharTemplateTable;
import l2rt.gameserver.templates.L2PlayerTemplate;
import l2rt.gameserver.model.base.Experience;
import l2rt.util.AutoBan;
import l2rt.util.GArray;

public class CharacterSelectionInfo extends L2GameServerPacket
{
	// d (SdSddddddddddffdQdddddddddddddddddddddddddddddddddddddddffdddchhd)
	private static Logger _log = Logger.getLogger(CharacterSelectionInfo.class.getName());
	private String _loginName;
	private int _sessionId;
	private CharSelectInfoPackage[] _characterPackages;

	public CharacterSelectionInfo(String loginName, int sessionId)
	{
		_sessionId = sessionId;
		_loginName = loginName;
		_characterPackages = loadCharacterSelectInfo(loginName);
		if(getClient() != null)
		{
			getClient().setCharSelection(_characterPackages);
		}
	}

	public CharSelectInfoPackage[] getCharInfo()
	{
		return _characterPackages;
	}

	@Override
	protected final void writeImpl()
	{
		int size = _characterPackages != null ? _characterPackages.length : 0;
		writeC(0x09);
		writeD(size);
		writeD(0x07); // Максимальное количество персонажей на сервере
		writeC(0x00); // Разрешает или запрещает создание игроков
		writeC(0x01);
		writeD(0x00);

		long lastAccess = 0L;
		int lastUsed = -1;
		for(int i = 0; i < size; i++)
		{
			if(lastAccess < _characterPackages[i].getLastAccess())
			{
				lastAccess = _characterPackages[i].getLastAccess();
				lastUsed = i;
			}
		}
		for(int i = 0; i < size; i++)
		{
			CharSelectInfoPackage charInfoPackage = _characterPackages[i];
			writeS(charInfoPackage.getName());
			writeD(charInfoPackage.getCharId()); // ?
			writeS(_loginName);
			writeD(_sessionId);
			writeD(charInfoPackage.getClanId());
			writeD(0x00); // ??
			writeD(charInfoPackage.getSex());
			writeD(charInfoPackage.getRace());
			writeD(charInfoPackage.getClassId());
			writeD(0x01); // active ??
			writeD(charInfoPackage.getX());
			writeD(charInfoPackage.getY());
			writeD(charInfoPackage.getZ());
			writeF(charInfoPackage.getCurrentHp());
			writeF(charInfoPackage.getCurrentMp());
			writeD(charInfoPackage.getSp());
			writeQ(charInfoPackage.getExp());
			writeF((float)(charInfoPackage.getExp() - Experience.LEVEL[charInfoPackage.getLevel()]) / (float)(Experience.LEVEL[(charInfoPackage.getLevel() + 1)] - Experience.LEVEL[charInfoPackage.getLevel()]));
			writeD(charInfoPackage.getLevel());
			writeD(charInfoPackage.getKarma());
			writeD(charInfoPackage.getPk());
			writeD(charInfoPackage.getPvP());
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			for(byte PAPERDOLL_ID : UserInfo.PAPERDOLL_ORDER)
			{
				writeD(charInfoPackage.getPaperdollItemId(PAPERDOLL_ID));
			}
			writeD(charInfoPackage.getHairStyle());
			writeD(charInfoPackage.getHairColor());
			writeD(charInfoPackage.getFace());
			writeF(charInfoPackage.getMaxHp()); // hp max
			writeF(charInfoPackage.getMaxMp()); // mp max
			writeD(charInfoPackage.getDeleteTimer());
			writeD(charInfoPackage.getClassId());
			writeD(i == lastUsed ? 1 : 0);
			writeC(Math.min(charInfoPackage.getEnchantEffect(), 127));
			writeH(0x00); // TODO AugmentationId
			writeH(0x00);//??
			writeD(0x00); // TODO TransformationId
			
			writeD(0);
			writeD(0);
			writeD(0);
			writeD(0);
			writeF(0D);
			writeF(0D);

			writeD(charInfoPackage.getVit());
			writeD(charInfoPackage.getAccessLevel() > -100 ? 0x01 : 0x00);// Активен ли.
		}
	}

	public static CharSelectInfoPackage[] loadCharacterSelectInfo(String loginName)
	{
		CharSelectInfoPackage charInfopackage;
		GArray<CharSelectInfoPackage> characterList = new GArray<CharSelectInfoPackage>();
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet pl_rset = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM characters AS c LEFT JOIN character_subclasses AS cs ON (c.obj_Id=cs.char_obj_id AND cs.isBase=1) WHERE account_name=? LIMIT 7");
			statement.setString(1, loginName);
			pl_rset = statement.executeQuery();
			while(pl_rset.next()) // fills the package
			{
				charInfopackage = restoreChar(pl_rset, pl_rset);
				if(charInfopackage != null)
				{
					characterList.add(charInfopackage);
				}
			}
		}
		catch(Exception e)
		{
			_log.log(Level.WARNING, "could not restore charinfo:", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, pl_rset);
		}
		return characterList.toArray(new CharSelectInfoPackage[characterList.size()]);
	}
	
	private static CharSelectInfoPackage restoreChar(ResultSet chardata, ResultSet charclass)
	{
		CharSelectInfoPackage charInfopackage = null;
		try
		{
			int objectId = chardata.getInt("obj_Id");
			int classid = charclass.getInt("class_id");
			boolean female = chardata.getInt("sex") == 1;
			L2PlayerTemplate templ = CharTemplateTable.getInstance().getTemplate(classid, female);
			if(templ == null)
			{
				_log.log(Level.WARNING, "restoreChar fail | templ == null | objectId: " + objectId + " | classid: " + classid + " | female: " + female);
				return null;
			}
			String name = chardata.getString("char_name");
			charInfopackage = new CharSelectInfoPackage(objectId, name);
			charInfopackage.setLevel(charclass.getInt("level"));
			charInfopackage.setMaxHp(charclass.getInt("maxHp"));
			charInfopackage.setCurrentHp(charclass.getDouble("curHp"));
			charInfopackage.setMaxMp(charclass.getInt("maxMp"));
			charInfopackage.setCurrentMp(charclass.getDouble("curMp"));
			charInfopackage.setX(chardata.getInt("x"));
			charInfopackage.setY(chardata.getInt("y"));
			charInfopackage.setZ(chardata.getInt("z"));
			charInfopackage.setPk(chardata.getInt("pkkills"));
			charInfopackage.setPvP(chardata.getInt("pvpkills"));
			charInfopackage.setFace(chardata.getInt("face"));
			charInfopackage.setHairStyle(chardata.getInt("hairstyle"));
			charInfopackage.setHairColor(chardata.getInt("haircolor"));
			charInfopackage.setVit(chardata.getInt("vitality"));
			charInfopackage.setSex(female ? 1 : 0);
			charInfopackage.setExp(charclass.getLong("exp"));
			charInfopackage.setSp(charclass.getInt("sp"));
			charInfopackage.setClanId(chardata.getInt("clanid"));
			charInfopackage.setKarma(chardata.getInt("karma"));
			charInfopackage.setRace(templ.race.ordinal());
			charInfopackage.setClassId(classid);
			long deletetime = chardata.getLong("deletetime");
			int deletedays = 0;
			if(Config.DELETE_DAYS > 0)
			{
				if(deletetime > 0)
				{
					deletetime = (int) (System.currentTimeMillis() / 1000 - deletetime);
					deletedays = (int) (deletetime / 3600 / 24);
					if(deletedays >= Config.DELETE_DAYS)
					{
						PlayerManager.deleteFromClan(objectId, charInfopackage.getClanId());
						PlayerManager.deleteCharByObjId(objectId);
						return null;
					}
					deletetime = Config.DELETE_DAYS * 3600 * 24 - deletetime;
				}
				else
				{
					deletetime = 0;
				}
			}
			charInfopackage.setDeleteTimer((int) deletetime);
			charInfopackage.setLastAccess(chardata.getLong("lastAccess") * 1000L);
			charInfopackage.setAccessLevel(chardata.getInt("accesslevel"));
			if(charInfopackage.getAccessLevel() < 0 && !AutoBan.isBanned(objectId))
			{
				charInfopackage.setAccessLevel(0);
			}
		}
		catch(Exception e)
		{
			_log.log(Level.INFO, "", e);
		}
		return charInfopackage;
	}
}