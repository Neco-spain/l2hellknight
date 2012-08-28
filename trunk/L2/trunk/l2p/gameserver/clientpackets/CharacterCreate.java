package l2p.gameserver.clientpackets;

import gnu.trove.TIntIntHashMap;
import l2p.gameserver.Config;
import l2p.gameserver.dao.CharacterDAO;
import l2p.gameserver.data.xml.holder.SkillAcquireHolder;
import l2p.gameserver.instancemanager.QuestManager;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.SkillLearn;
import l2p.gameserver.model.actor.instances.player.ShortCut;
import l2p.gameserver.model.base.AcquireType;
import l2p.gameserver.model.base.ClassId;
import l2p.gameserver.model.items.ItemInstance;
import l2p.gameserver.model.items.PcInventory;
import l2p.gameserver.model.quest.Quest;
import l2p.gameserver.network.GameClient;
import l2p.gameserver.serverpackets.CharacterCreateFail;
import l2p.gameserver.serverpackets.CharacterCreateSuccess;
import l2p.gameserver.serverpackets.CharacterSelectionInfo;
import l2p.gameserver.tables.SkillTable;
import l2p.gameserver.templates.PlayerTemplate;
import l2p.gameserver.templates.item.ItemTemplate;
import l2p.gameserver.utils.ItemFunctions;
import l2p.gameserver.utils.Util;

public class CharacterCreate extends L2GameClientPacket
{
  private String _name;
  private int _sex;
  private int _classId;
  private int _hairStyle;
  private int _hairColor;
  private int _face;

  protected void readImpl()
  {
    _name = readS();
    readD();
    _sex = readD();
    _classId = readD();
    readD();
    readD();
    readD();
    readD();
    readD();
    readD();
    _hairStyle = readD();
    _hairColor = readD();
    _face = readD();
  }

  protected void runImpl()
  {
    for (ClassId cid : ClassId.VALUES)
      if ((cid.getId() == _classId) && (cid.getLevel() != 1))
        return;
    if (CharacterDAO.getInstance().accountCharNumber(((GameClient)getClient()).getLogin()) >= 8)
    {
      sendPacket(CharacterCreateFail.REASON_TOO_MANY_CHARACTERS);
      return;
    }
    if (!Util.isMatchingRegexp(_name, Config.CNAME_TEMPLATE))
    {
      sendPacket(CharacterCreateFail.REASON_16_ENG_CHARS);
      return;
    }
    if (CharacterDAO.getInstance().getObjectIdByName(_name) > 0)
    {
      sendPacket(CharacterCreateFail.REASON_NAME_ALREADY_EXISTS);
      return;
    }

    Player newChar = Player.create(_classId, _sex, ((GameClient)getClient()).getLogin(), _name, _hairStyle, _hairColor, _face);
    if (newChar == null) {
      return;
    }
    sendPacket(CharacterCreateSuccess.STATIC);

    initNewChar((GameClient)getClient(), newChar);
  }

  private void initNewChar(GameClient client, Player newChar)
  {
    PlayerTemplate template = newChar.getTemplate();

    Player.restoreCharSubClasses(newChar);

    if (Config.STARTING_ADENA > 0) {
      newChar.addAdena(Config.STARTING_ADENA);
    }
    if (!Config.STARTING_ITEMS.isEmpty()) {
      for (int key : Config.STARTING_ITEMS.keys())
      {
        ItemInstance item = ItemFunctions.createItem(key);
        item.setCount(Config.STARTING_ITEMS.get(key));
        newChar.getInventory().addItem(item);
      }
    }
    newChar.setLoc(template.spawnLoc);

    if (Config.CHAR_TITLE)
      newChar.setTitle(Config.ADD_CHAR_TITLE);
    else {
      newChar.setTitle("");
    }
    for (ItemTemplate i : template.getItems())
    {
      ItemInstance item = ItemFunctions.createItem(i.getItemId());
      newChar.getInventory().addItem(item);

      if (item.getItemId() == 5588) {
        newChar.registerShortCut(new ShortCut(11, 0, 1, item.getObjectId(), -1, 1));
      }
      if ((item.isEquipable()) && ((newChar.getActiveWeaponItem() == null) || (item.getTemplate().getType2() != 0))) {
        newChar.getInventory().equipItem(item);
      }
    }

    ItemInstance item = ItemFunctions.createItem(10650);
    item.setCount(5L);
    newChar.getInventory().addItem(item);

    item = ItemFunctions.createItem(9716);
    item.setCount(10L);
    newChar.getInventory().addItem(item);

    for (SkillLearn skill : SkillAcquireHolder.getInstance().getAvailableSkills(newChar, AcquireType.NORMAL)) {
      newChar.addSkill(SkillTable.getInstance().getInfo(skill.getId(), skill.getLevel()), true);
    }
    if (newChar.getSkillLevel(Integer.valueOf(1001)) > 0)
      newChar.registerShortCut(new ShortCut(1, 0, 2, 1001, 1, 1));
    if (newChar.getSkillLevel(Integer.valueOf(1177)) > 0)
      newChar.registerShortCut(new ShortCut(1, 0, 2, 1177, 1, 1));
    if (newChar.getSkillLevel(Integer.valueOf(1216)) > 0) {
      newChar.registerShortCut(new ShortCut(2, 0, 2, 1216, 1, 1));
    }

    newChar.registerShortCut(new ShortCut(0, 0, 3, 2, -1, 1));
    newChar.registerShortCut(new ShortCut(3, 0, 3, 5, -1, 1));
    newChar.registerShortCut(new ShortCut(10, 0, 3, 0, -1, 1));

    newChar.registerShortCut(new ShortCut(0, 10, 2, 911, 1, 1));
    newChar.registerShortCut(new ShortCut(3, 10, 2, 884, 1, 1));
    newChar.registerShortCut(new ShortCut(4, 10, 2, 885, 1, 1));

    newChar.registerShortCut(new ShortCut(0, 11, 3, 70, 0, 1));

    startTutorialQuest(newChar);

    newChar.setCurrentHpMp(newChar.getMaxHp(), newChar.getMaxMp());
    newChar.setCurrentCp(0.0D);
    newChar.setOnlineStatus(false);

    newChar.store(false);
    newChar.getInventory().store();
    newChar.deleteMe();

    client.setCharSelection(CharacterSelectionInfo.loadCharacterSelectInfo(client.getLogin()));
  }

  public static void startTutorialQuest(Player player)
  {
    Quest q = QuestManager.getQuest(255);
    if (q != null)
      q.newQuestState(player, 1);
  }
}