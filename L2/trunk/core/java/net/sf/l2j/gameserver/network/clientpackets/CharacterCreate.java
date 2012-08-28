//L2DDT
package net.sf.l2j.gameserver.network.clientpackets;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javolution.util.FastList;
import javolution.util.FastMap;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.datatables.CharNameTable;
import net.sf.l2j.gameserver.datatables.CharTemplateTable;
import net.sf.l2j.gameserver.datatables.ItemTable;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.datatables.SkillTreeTable;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.instancemanager.QuestManager;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2ShortCut;
import net.sf.l2j.gameserver.model.L2SkillLearn;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.quest.Quest;
import net.sf.l2j.gameserver.model.quest.QuestState;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.CharCreateFail;
import net.sf.l2j.gameserver.network.serverpackets.CharCreateOk;
import net.sf.l2j.gameserver.network.serverpackets.CharSelectInfo;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2Item;
import net.sf.l2j.gameserver.templates.L2PcTemplate;
import net.sf.l2j.gameserver.util.Util;

@SuppressWarnings("unused")
public final class CharacterCreate extends L2GameClientPacket
{
	private static final String _C__0B_CHARACTERCREATE = "[C] 0B CharacterCreate";
	private static Logger _log = Logger.getLogger(CharacterCreate.class.getName());

	// cSdddddddddddd
	private String _name;
    private int _race;
	private byte _sex;
	private int _classId;
	private int _int;
	private int _str;
	private int _con;
	private int _men;
	private int _dex;
	private int _wit;
	private byte _hairStyle;
	private byte _hairColor;
	private byte _face;

	@Override
	protected void readImpl()
	{
		_name      = readS();
		_race      = readD();
		_sex       = (byte)readD();
		_classId   = readD();
		_int       = readD();
		_str       = readD();
		_con       = readD();
		_men       = readD();
		_dex       = readD();
		_wit       = readD();
		_hairStyle = (byte)readD();
		_hairColor = (byte)readD();
		_face      = (byte)readD();
	}

	@Override
	protected void runImpl()
	{
        if (CharNameTable.getInstance().accountCharNumber(getClient().getAccountName()) >= Config.MAX_CHARACTERS_NUMBER_PER_ACCOUNT && Config.MAX_CHARACTERS_NUMBER_PER_ACCOUNT != 0)
        {
            if (Config.DEBUG)
                _log.fine("Max number of characters reached. Creation failed.");
            CharCreateFail ccf = new CharCreateFail(CharCreateFail.REASON_TOO_MANY_CHARACTERS);
            sendPacket(ccf);
            return;
        }
        else if (CharNameTable.getInstance().doesCharNameExist(_name))
		{
			if (Config.DEBUG)
				_log.fine("charname: "+ _name + " already exists. creation failed.");
			CharCreateFail ccf = new CharCreateFail(CharCreateFail.REASON_NAME_ALREADY_EXISTS);
			sendPacket(ccf);
			return;
		}
		else if ((_name.length() < 3) || (_name.length() > 16) || !Util.isAlphaNumeric(_name) || !isValidName(_name))
		{
			if (Config.DEBUG)
				_log.fine("charname: " + _name + " is invalid. creation failed.");
			CharCreateFail ccf = new CharCreateFail(CharCreateFail.REASON_16_ENG_CHARS);
			sendPacket(ccf);
			return;
		}
		else 
			for (String nick : Config.LIST_NOT_ALLOWED_NICKS)
	        {
	          if (this._name.equalsIgnoreCase(nick))
	          {
	        	CharCreateFail ccf = new CharCreateFail(CharCreateFail.REASON_16_ENG_CHARS);
	  			sendPacket(ccf);
	  			return;
	          }
	        }
		if (Config.DEBUG)
			_log.fine("charname: " + _name + " classId: " + _classId);

		L2PcTemplate template = CharTemplateTable.getInstance().getTemplate(_classId);
		if(template == null || template.classBaseLevel > 1)
		{
			CharCreateFail ccf = new CharCreateFail(CharCreateFail.REASON_CREATION_FAILED);
			sendPacket(ccf);
			return;
		}

		int objectId = IdFactory.getInstance().getNextId();
		L2PcInstance newChar = L2PcInstance.create(objectId, template, getClient().getAccountName(),
				_name, _hairStyle, _hairColor, _face, _sex!=0);
		newChar.setCurrentHp(newChar.getMaxHp());
		newChar.setCurrentCp(0);
		newChar.setCurrentMp(newChar.getMaxMp());
		//newChar.setMaxLoad(template.baseLoad);

		// send acknowledgement
		CharCreateOk cco = new CharCreateOk();
		sendPacket(cco);

		initNewChar(getClient(), newChar);
	}

    public static boolean isValidName(String text)
    {
            boolean result = true;
            String test = text;
            Pattern pattern;
            try
            {
                pattern = Pattern.compile(Config.CNAME_TEMPLATE);
            }
            catch (PatternSyntaxException e) // case of illegal pattern
            {
            	_log.warning("ERROR : Character name pattern of config is wrong!");
                pattern = Pattern.compile(".*");
            }
            Matcher regexp = pattern.matcher(test);
            if (!regexp.matches())
            {
                    result = false;
            }
            return result;
    }

	private void initNewChar(L2GameClient client, L2PcInstance newChar)
	{
		if (Config.DEBUG) _log.fine("Character init start");
		L2World.getInstance().storeObject(newChar);

		L2PcTemplate template = newChar.getTemplate();

		newChar.addAdena("Init", Config.STARTING_ADENA, null, false);

		newChar.setXYZInvisible(template.spawnX, template.spawnY, template.spawnZ);

		if(Config.ENABLE_NEWCHAR_TITLE)
		{
			newChar.setTitle(Config.NEW_CHAR_TITLE);
		}
		else
		{
			newChar.setTitle("");
		}
		
		L2ShortCut shortcut;
		//add attack shortcut
		shortcut = new L2ShortCut(0,0,3,2,-1,1);
		newChar.registerShortCut(shortcut);
		//add take shortcut
		shortcut = new L2ShortCut(3,0,3,5,-1,1);
		newChar.registerShortCut(shortcut);
		//add sit shortcut
		shortcut = new L2ShortCut(10,0,3,0,-1,1);
		newChar.registerShortCut(shortcut);

		ItemTable itemTable = ItemTable.getInstance();
		L2Item[] items = template.getItems();
		for (int i = 0; i < items.length; i++)
		{
			L2ItemInstance item = newChar.getInventory().addItem("Init", items[i].getItemId(), 1, newChar, null);
			if (item.getItemId()==5588){
			    //add tutbook shortcut
			    shortcut = new L2ShortCut(11,0,1,item.getObjectId(),-1,1);
			    newChar.registerShortCut(shortcut);
			}
			if (item.isEquipable()){
			  if (newChar.getActiveWeaponItem() == null || !(item.getItem().getType2() != L2Item.TYPE2_WEAPON))
			    newChar.getInventory().equipItemAndRecord(item);
			}
		}
	    if (Config.ITEMS_ON_CREATE_CHAR != null && !Config.ITEMS_ON_CREATE_CHAR.isEmpty())
	    {
	    for (int i : Config.ITEMS_ON_CREATE_CHAR.keys())
	      {
	        Integer item_id = i;
	        Integer item_count = Config.ITEMS_ON_CREATE_CHAR.get(i);
	        if ((item_id == null) || (item_count == null) || (Config.ITEMS_ON_CREATE_CHAR == null)) {
	          continue;
	        }
	        newChar.getInventory().addItem("ItemsOnCreate", item_id.intValue(), item_count.intValue(), newChar, null);
	      }
	    }
		
		L2SkillLearn[] startSkills = SkillTreeTable.getInstance().getAvailableSkills(newChar, newChar.getClassId());
		for (int i = 0; i < startSkills.length; i++)
		{
			newChar.addSkill(SkillTable.getInstance().getInfo(startSkills[i].getId(), startSkills[i].getLevel()), true);
			if (startSkills[i].getId()==1001 || startSkills[i].getId()==1177){
			    shortcut = new L2ShortCut(1,0,2,startSkills[i].getId(),1,1);
			    newChar.registerShortCut(shortcut);
			}
			if (startSkills[i].getId()==1216){
			    shortcut = new L2ShortCut(10,0,2,startSkills[i].getId(),1,1);
			    newChar.registerShortCut(shortcut);
			}
			if (Config.DEBUG)
				_log.fine("adding starter skill:" + startSkills[i].getId()+ " / "+ startSkills[i].getLevel());
		}
		L2GameClient.saveCharToDisk(newChar);
		newChar.deleteMe(); // release the world of this character and it's inventory
        startTutorialQuest(newChar);
		// send char list

		CharSelectInfo cl =	new CharSelectInfo(client.getAccountName(), client.getSessionId().playOkID1);
		client.getConnection().sendPacket(cl);
        client.setCharSelection(cl.getCharInfo());
        if (Config.DEBUG) _log.fine("Character init end");
	}
	
	public void startTutorialQuest(L2PcInstance player)
	{
		QuestState qs = player.getQuestState("255_Tutorial");
		Quest q = null;
		if (qs == null)
			q = QuestManager.getInstance().getQuest("255_Tutorial");
		if (q != null)
			q.newQuestState(player);
	}

	@Override
	public String getType()
	{
		return _C__0B_CHARACTERCREATE;
	}
}
