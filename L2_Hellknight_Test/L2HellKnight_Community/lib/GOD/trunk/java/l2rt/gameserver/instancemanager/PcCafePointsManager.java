package l2rt.gameserver.instancemanager;

import l2rt.Config;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2Zone.ZoneType;
import l2rt.gameserver.model.base.ClassId;
import l2rt.gameserver.network.serverpackets.ExPCCafePointInfo;
import l2rt.gameserver.network.serverpackets.SystemMessage;
import l2rt.util.Rnd;

public class PcCafePointsManager
{
        private static PcCafePointsManager _instance;
        public static PcCafePointsManager getInstance()
        {
                if(_instance == null)
                        _instance = new PcCafePointsManager();
                return _instance;
        }
        public PcCafePointsManager()
        {}
        public void givePcCafePoint(final L2Player player, final long givedexp)
        {
                if(!Config.BANG_POINT_ENABLE)
                        return;
                if(player.isInZone(ZoneType.peace_zone) || player.isInZone(ZoneType.battle_zone) || player.isInZone(ZoneType.Siege))
                        return;
                if(player.getPcBangPoints() >= Config.BANG_POINT_MAX_COUNT)
                {
                        final SystemMessage sm = new SystemMessage(SystemMessage.THE_MAXIMUM_ACCUMULATION_ALLOWED_OF_PC_CAFE_POINTS_HAS_BEEN_EXCEEDED_YOU_CAN_NO_LONGER_ACQUIRE);
                        player.sendPacket(sm);
                        return;
                }
                int _points = (int) (givedexp * 0.0001 * Config.BANG_POINT_RATE);
                if(player.getActiveClassId() == ClassId.archmage.getId() || player.getActiveClassId() == ClassId.soultaker.getId() || player.getActiveClassId() == ClassId.stormScreamer.getId() || player.getActiveClassId() == ClassId.mysticMuse.getId())
                        _points /= 2;
                if(Config.BANG_RANDOM_POINT_ENABLE)
                        _points = Rnd.get(_points / 2, _points);
                boolean doublepoint = false;
                SystemMessage sm = null;
                if(_points > 0)
                {
                        if(Config.BANG_POINT_DOUBLE_ENABLE && Rnd.get(100) < Config.BANG_POINT_DUAL_CHANCE)
                        {
                                _points *= 2;
                                sm = new SystemMessage(SystemMessage.DOUBLE_POINTS_YOU_AQUIRED_S1_PC_BANG_POINT);
                                doublepoint = true;
                        }
                        else
                        {
                                sm = new SystemMessage(SystemMessage.YOU_HAVE_ACQUIRED_S1_PC_CAFE_POINTS);
                        }
                        if(player.getPcBangPoints() + _points > Config.BANG_POINT_MAX_COUNT)
                                _points = Config.BANG_POINT_MAX_COUNT - player.getPcBangPoints();
                        sm.addNumber(_points);
                        player.sendPacket(sm);
                        player.setPcBangPoints(player.getPcBangPoints() + _points);
                        player.sendPacket(new ExPCCafePointInfo(player.getPcBangPoints(), _points, true, doublepoint, 1));
                }
        }
}