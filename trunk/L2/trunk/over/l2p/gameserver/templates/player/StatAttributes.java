package l2p.gameserver.templates.player;

public class StatAttributes
{
  private final int _int;
  private final int _str;
  private final int _con;
  private final int _men;
  private final int _dex;
  private final int _wit;

  public StatAttributes(int _int, int str, int con, int men, int dex, int wit)
  {
    this._int = _int;
    _str = str;
    _con = con;
    _men = men;
    _dex = dex;
    _wit = wit;
  }

  public int getINT()
  {
    return _int;
  }

  public int getSTR()
  {
    return _str;
  }

  public int getCON()
  {
    return _con;
  }

  public int getMEN()
  {
    return _men;
  }

  public int getDEX()
  {
    return _dex;
  }

  public int getWIT()
  {
    return _wit;
  }
}