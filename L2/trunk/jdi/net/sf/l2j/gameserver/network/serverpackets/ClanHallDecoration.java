package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.entity.ClanHall;
import net.sf.l2j.gameserver.model.entity.ClanHall.ClanHallFunction;

public class ClanHallDecoration extends L2GameServerPacket
{
  private static final String _S__F7_AGITDECOINFO = "[S] F7 AgitDecoInfo";
  private ClanHall _clanHall;
  private ClanHall.ClanHallFunction _function;

  public ClanHallDecoration(ClanHall ClanHall)
  {
    _clanHall = ClanHall;
  }

  protected final void writeImpl()
  {
    writeC(247);
    writeD(_clanHall.getId());

    _function = _clanHall.getFunction(3);
    if ((_function == null) || (_function.getLvl() == 0))
      writeC(0);
    else if (((_clanHall.getGrade() == 0) && (_function.getLvl() < 220)) || ((_clanHall.getGrade() == 1) && (_function.getLvl() < 160)) || ((_clanHall.getGrade() == 2) && (_function.getLvl() < 260)) || ((_clanHall.getGrade() == 3) && (_function.getLvl() < 300)))
    {
      writeC(1);
    }
    else writeC(2);

    _function = _clanHall.getFunction(4);
    if ((_function == null) || (_function.getLvl() == 0)) {
      writeC(0);
      writeC(0);
    } else if (((_clanHall.getGrade() != 0) && (_clanHall.getGrade() != 1)) || ((_function.getLvl() < 25) || ((_clanHall.getGrade() == 2) && (_function.getLvl() < 30)) || ((_clanHall.getGrade() == 3) && (_function.getLvl() < 40))))
    {
      writeC(1);
      writeC(1);
    } else {
      writeC(2);
      writeC(2);
    }

    _function = _clanHall.getFunction(5);
    if ((_function == null) || (_function.getLvl() == 0))
      writeC(0);
    else if (((_clanHall.getGrade() == 0) && (_function.getLvl() < 25)) || ((_clanHall.getGrade() == 1) && (_function.getLvl() < 30)) || ((_clanHall.getGrade() == 2) && (_function.getLvl() < 40)) || ((_clanHall.getGrade() == 3) && (_function.getLvl() < 50)))
    {
      writeC(1);
    }
    else writeC(2);

    _function = _clanHall.getFunction(1);
    if ((_function == null) || (_function.getLvl() == 0))
      writeC(0);
    else if (_function.getLvl() < 2)
      writeC(1);
    else
      writeC(2);
    writeC(0);

    _function = _clanHall.getFunction(8);
    if ((_function == null) || (_function.getLvl() == 0))
      writeC(0);
    else if (_function.getLvl() <= 1)
      writeC(1);
    else {
      writeC(2);
    }
    _function = _clanHall.getFunction(2);
    if ((_function == null) || (_function.getLvl() == 0))
      writeC(0);
    else if (((_clanHall.getGrade() == 0) && (_function.getLvl() < 2)) || (_function.getLvl() < 3))
      writeC(1);
    else {
      writeC(2);
    }
    _function = _clanHall.getFunction(6);
    if ((_function == null) || (_function.getLvl() == 0)) {
      writeC(0);
      writeC(0);
    } else if (((_clanHall.getGrade() == 0) && (_function.getLvl() < 2)) || ((_clanHall.getGrade() == 1) && (_function.getLvl() < 4)) || ((_clanHall.getGrade() == 2) && (_function.getLvl() < 5)) || ((_clanHall.getGrade() == 3) && (_function.getLvl() < 8)))
    {
      writeC(1);
      writeC(1);
    } else {
      writeC(2);
      writeC(2);
    }

    _function = _clanHall.getFunction(7);
    if ((_function == null) || (_function.getLvl() == 0))
      writeC(0);
    else if (_function.getLvl() <= 1)
      writeC(1);
    else {
      writeC(2);
    }
    _function = _clanHall.getFunction(2);
    if ((_function == null) || (_function.getLvl() == 0))
      writeC(0);
    else if (((_clanHall.getGrade() == 0) && (_function.getLvl() < 2)) || (_function.getLvl() < 3))
      writeC(1);
    else
      writeC(2);
    writeD(0);
    writeD(0);
  }

  public String getType()
  {
    return "[S] F7 AgitDecoInfo";
  }
}