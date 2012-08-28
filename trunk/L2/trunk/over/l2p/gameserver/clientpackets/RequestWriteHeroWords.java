package l2p.gameserver.clientpackets;

import l2p.gameserver.model.Player;
import l2p.gameserver.model.entity.Hero;
import l2p.gameserver.network.GameClient;

public class RequestWriteHeroWords extends L2GameClientPacket
{
  private String _heroWords;

  protected void readImpl()
  {
    _heroWords = readS();
  }

  protected void runImpl()
  {
    Player player = ((GameClient)getClient()).getActiveChar();
    if ((player == null) || (!player.isHero())) {
      return;
    }
    if ((_heroWords == null) || (_heroWords.length() > 300)) {
      return;
    }
    Hero.getInstance().setHeroMessage(player.getObjectId(), _heroWords);
  }
}