package net.sf.l2j.gameserver.network.serverpackets;

public class TutorialCloseHtml extends L2GameServerPacket
{
  private static final String _S__a3_TUTORIALCLOSEHTML = "[S] a3 TutorialCloseHtml";

  protected void writeImpl()
  {
    writeC(163);
  }

  public String getType()
  {
    return "[S] a3 TutorialCloseHtml";
  }
}