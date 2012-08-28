package l2p.gameserver.clientpackets;

import l2p.commons.lang.ArrayUtils;
import l2p.gameserver.data.xml.holder.SkillAcquireHolder;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.SkillLearn;
import l2p.gameserver.model.base.AcquireType;
import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.network.GameClient;
import l2p.gameserver.serverpackets.AcquireSkillInfo;
import l2p.gameserver.tables.SkillTable;

public class RequestAquireSkillInfo extends L2GameClientPacket
{
  private int _id;
  private int _level;
  private AcquireType _type;

  protected void readImpl()
  {
    _id = readD();
    _level = readD();
    _type = ((AcquireType)ArrayUtils.valid(AcquireType.VALUES, readD()));
  }

  protected void runImpl()
  {
    Player player = ((GameClient)getClient()).getActiveChar();
    if ((player == null) || (player.getTransformation() != 0) || (SkillTable.getInstance().getInfo(_id, _level) == null) || (_type == null)) {
      return;
    }
    NpcInstance trainer = player.getLastNpc();
    if (((trainer == null) || (player.getDistance(trainer.getX(), trainer.getY()) > 200.0D)) && (!player.isGM())) {
      return;
    }
    SkillLearn skillLearn = SkillAcquireHolder.getInstance().getSkillLearn(player, _id, _level, _type);
    if (skillLearn == null) {
      return;
    }
    sendPacket(new AcquireSkillInfo(_type, skillLearn));
  }
}