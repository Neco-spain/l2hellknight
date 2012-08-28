package l2p.gameserver.serverpackets;

import l2p.gameserver.GameTimeController;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.base.ClassId;
import l2p.gameserver.model.base.Race;
import l2p.gameserver.utils.Location;

public class CharSelected extends L2GameServerPacket
{
  private int _sessionId;
  private int char_id;
  private int clan_id;
  private int sex;
  private int race;
  private int class_id;
  private String _name;
  private String _title;
  private Location _loc;
  private double curHp;
  private double curMp;
  private int _sp;
  private int level;
  private int karma;
  private int _int;
  private int _str;
  private int _con;
  private int _men;
  private int _dex;
  private int _wit;
  private int _pk;
  private long _exp;

  public CharSelected(Player cha, int sessionId)
  {
    _sessionId = sessionId;

    _name = cha.getName();
    char_id = cha.getObjectId();
    _title = cha.getTitle();
    clan_id = cha.getClanId();
    sex = cha.getSex();
    race = cha.getRace().ordinal();
    class_id = cha.getClassId().getId();
    _loc = cha.getLoc();
    curHp = cha.getCurrentHp();
    curMp = cha.getCurrentMp();
    _sp = cha.getIntSp();
    _exp = cha.getExp();
    level = cha.getLevel();
    karma = cha.getKarma();
    _pk = cha.getPkKills();
    _int = cha.getINT();
    _str = cha.getSTR();
    _con = cha.getCON();
    _men = cha.getMEN();
    _dex = cha.getDEX();
    _wit = cha.getWIT();
  }

  protected final void writeImpl()
  {
    writeC(11);

    writeS(_name);
    writeD(char_id);
    writeS(_title);
    writeD(_sessionId);
    writeD(clan_id);
    writeD(0);
    writeD(sex);
    writeD(race);
    writeD(class_id);
    writeD(1);
    writeD(_loc.x);
    writeD(_loc.y);
    writeD(_loc.z);

    writeF(curHp);
    writeF(curMp);
    writeD(_sp);
    writeQ(_exp);
    writeD(level);
    writeD(karma);
    writeD(_pk);
    writeD(_int);
    writeD(_str);
    writeD(_con);
    writeD(_men);
    writeD(_dex);
    writeD(_wit);
    for (int i = 0; i < 30; i++) {
      writeD(0);
    }
    writeF(0.0D);
    writeF(0.0D);

    writeD(GameTimeController.getInstance().getGameTime());
    writeD(0);
    writeD(0);
    writeC(0);
    writeH(0);
    writeH(0);
    writeD(0);
  }
}