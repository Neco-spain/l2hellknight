package l2rt.gameserver.network.clientpackets;

import java.sql.SQLException;

import l2rt.Config;
import l2rt.database.L2DatabaseFactory;
import l2rt.database.mysql;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.instancemanager.PlayerManager;
import l2rt.gameserver.instancemanager.QuestManager;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2ShortCut;
import l2rt.gameserver.model.L2SkillLearn;
import l2rt.gameserver.model.base.ClassId;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.model.quest.Quest;
import l2rt.gameserver.network.L2GameClient;
import l2rt.gameserver.network.serverpackets.CharacterSelectionInfo;
import l2rt.gameserver.tables.CharNameTable;
import l2rt.gameserver.tables.SkillTable;
import l2rt.gameserver.tables.SkillTreeTable;
import l2rt.gameserver.templates.L2Item;
import l2rt.gameserver.templates.L2PlayerTemplate;
import l2rt.gameserver.xml.ItemTemplates;
import l2rt.util.Util;
import l2rt.gameserver.model.base.Experience;

public class CharacterCreate extends L2GameClientPacket
{
	// cSdddddddddddd
	private String _name;
	private int _sex;
	private int _classId;
	private int _hairStyle;
	private int _hairColor;
	private int _face;
	
	
	
	
	@Override
	public void readImpl()
	{
		_name = readS();
		readD(); // race
		_sex = readD();
		_classId = readD();
		readD(); // int
		readD(); // str
		readD(); // con
		readD(); // men
		readD(); // dex
		readD(); // wit
		_hairStyle = readD();
		_hairColor = readD();
		_face = readD();
	}

	@Override
	public void runImpl()
	{
		for(ClassId cid : ClassId.values())
			if(cid.getId() == _classId && cid.getLevel() != 1)
				return;
		if(CharNameTable.getInstance().accountCharNumber(getClient().getLoginName()) >= 8)
		{
			sendPacket(Msg.CharacterCreateFail_REASON_TOO_MANY_CHARACTERS);
			return;
		}
		if(!Util.isMatchingRegexp(_name, Config.CNAME_TEMPLATE))
		{
			sendPacket(Msg.CharacterCreateFail_REASON_16_ENG_CHARS);
			return;
		}
		else if(CharNameTable.getInstance().doesCharNameExist(_name))
		{
			sendPacket(Msg.CharacterCreateFail_REASON_NAME_ALREADY_EXISTS);
			return;
		}

		L2Player newChar = L2Player.create(_classId, (byte) _sex, getClient().getLoginName(), _name, (byte) _hairStyle, (byte) _hairColor, (byte) _face);
		if(newChar == null)
			return;
		newChar.setConnected(false);

		sendPacket(Msg.CharacterCreateSuccess);

		initNewChar(getClient(), newChar);
	}
	
	private void initNewChar(L2GameClient client, L2Player newChar)
	{

		L2PlayerTemplate template = newChar.getTemplate();

		L2Player.restoreCharSubClasses(newChar);

		if(Config.STARTING_ADENA > 0)
			newChar.addAdena(Config.STARTING_ADENA);

		newChar.setXYZInvisible(template.spawnLoc);
		newChar.setLevel(80);
		newChar.addExp(Experience.LEVEL[80]);
		newChar.setNoble(true);

		if(Config.CHAR_TITLE)
			newChar.setTitle(Config.ADD_CHAR_TITLE);
		else
			newChar.setTitle("");

		ItemTemplates itemTable = ItemTemplates.getInstance();
		for(L2Item i : template.getItems())
		{
			L2ItemInstance item = itemTable.createItem(i.getItemId());
			newChar.getInventory().addItem(item);

			if(item.getItemId() == 5588) // tutorial book
				newChar.registerShortCut(new L2ShortCut(11, 0, L2ShortCut.TYPE_ITEM, item.getObjectId(), -1));

			if(item.isEquipable() && (newChar.getActiveWeaponItem() == null || item.getItem().getType2() != L2Item.TYPE2_WEAPON))
				newChar.getInventory().equipItem(item, false);
		}

		// Scroll of Escape: Kamael Village
		L2ItemInstance item = itemTable.createItem(9716);
		item.setCount(10);
		newChar.getInventory().addItem(item);

		// Adventurer's Scroll of Escape
		item = itemTable.createItem(10650);
		item.setCount(5);
		newChar.getInventory().addItem(item);

		for(L2SkillLearn skill : SkillTreeTable.getInstance().getAvailableSkills(newChar, newChar.getClassId()))
			newChar.addSkill(SkillTable.getInstance().getInfo(skill.id, skill.skillLevel), true);

		if(newChar.getSkillLevel(1001) > 0) // Soul Cry
			newChar.registerShortCut(new L2ShortCut(1, 0, L2ShortCut.TYPE_SKILL, 1001, 1));
		if(newChar.getSkillLevel(1177) > 0) // Wind Strike
			newChar.registerShortCut(new L2ShortCut(1, 0, L2ShortCut.TYPE_SKILL, 1177, 1));
		if(newChar.getSkillLevel(1216) > 0) // Self Heal
			newChar.registerShortCut(new L2ShortCut(2, 0, L2ShortCut.TYPE_SKILL, 1216, 1));

		// add attack, take, sit shortcut
		newChar.registerShortCut(new L2ShortCut(0, 0, L2ShortCut.TYPE_ACTION, 2, -1));
		newChar.registerShortCut(new L2ShortCut(3, 0, L2ShortCut.TYPE_ACTION, 5, -1));
		newChar.registerShortCut(new L2ShortCut(10, 0, L2ShortCut.TYPE_ACTION, 0, -1));
		
		// add PA for created character
		
		if (Config.SERVICES_RATE_CREATE_CHARACTER_PA != 0 & !(newChar.getBonus() != null))
		{
		try
		{
			mysql.setEx(L2DatabaseFactory.getInstanceLogin(), "UPDATE `accounts` SET `bonus`=?,`bonus_expire`=UNIX_TIMESTAMP()+" + Config.SERVICES_RATE_CREATE_CHARACTER_PA + "*24*60*60 WHERE `login`=?", Config.SERVICES_RATE_CREATE_CHARACTER_PA_VALUE, newChar.getAccountName());
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		}
		
		
		
		startTutorialQuest(newChar);

		newChar.setCurrentHpMp(newChar.getMaxHp(), newChar.getMaxMp());

		PlayerManager.saveCharToDisk(newChar);
		newChar.deleteMe(); // release the world of this character and it's inventory

		client.setCharSelection(CharacterSelectionInfo.loadCharacterSelectInfo(client.getLoginName()));
	}

	public static void startTutorialQuest(L2Player player)
	{
		Quest q = QuestManager.getQuest(255);
		if(q != null)
			q.newQuestState(player, Quest.CREATED);
	}
}