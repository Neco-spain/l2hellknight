package items;

import java.util.*;
import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.handler.IItemHandler;
import l2rt.gameserver.handler.ItemHandler;
import l2rt.gameserver.model.*;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.network.serverpackets.L2GameServerPacket;
import l2rt.gameserver.network.serverpackets.SystemMessage;
import l2rt.gameserver.xml.ItemTemplates;
import l2rt.util.Rnd;

public class EventBox
    implements IItemHandler, ScriptFile
{

    public EventBox()
    {
    }

    @SuppressWarnings("unchecked")
	public synchronized void useItem(L2Playable playable, L2ItemInstance item, Boolean ctrl)
    {
        if(!playable.isPlayer())
            return;
        L2Player activeChar = playable.getPlayer();
        if(!activeChar.isQuestContinuationPossible(true))
            return;
        Map items = new HashMap();
        long count = 0L;
        do
        {
            count++;
            getGroupItem(activeChar, _dropscrolls, items);
            getGroupItem(activeChar, _dropother, items);
            getGroupItem(activeChar, _droparmor, items);
            getGroupItem(activeChar, _dropweapon, items);
            getGroupItem(activeChar, _dropjew, items);
            getGroupItem(activeChar, _dropadena, items);
        } while(ctrl.booleanValue() && item.getCount() > count && activeChar.isQuestContinuationPossible(false));
        activeChar.getInventory().destroyItem(item, count, true);
        activeChar.sendPacket(new L2GameServerPacket[] {
            SystemMessage.removeItems(item.getItemId(), count)
        });
        java.util.Map.Entry e;
        for(Iterator i$ = items.entrySet().iterator(); i$.hasNext(); activeChar.sendPacket(new L2GameServerPacket[] {
    SystemMessage.obtainItems(((Integer)e.getKey()).intValue(), ((Long)e.getValue()).longValue(), 0)
}))
            e = (java.util.Map.Entry)i$.next();

    }

    @SuppressWarnings("unchecked")
	public void getGroupItem(L2Player activeChar, L2DropData dropData[], Map report)
    {
        long count = 0L;
        L2DropData arr$[] = dropData;
        int len$ = arr$.length;
        for(int i$ = 0; i$ < len$; i$++)
        {
            L2DropData d = arr$[i$];
            if((double)Rnd.get(1, 0xf4240) <= d.getChance() * 0.29999999999999999D)
            {
                count = Rnd.get(d.getMinDrop(), d.getMaxDrop());
                L2ItemInstance item = ItemTemplates.getInstance().createItem(d.getItemId());
                item.setCount(count);
                activeChar.getInventory().addItem(item);
                Long old = (Long)report.get(Integer.valueOf(d.getItemId()));
                report.put(Integer.valueOf(d.getItemId()), Long.valueOf(old == null ? count : old.longValue() + count));
            }
        }

    }

    public final int[] getItemIds()
    {
        return _itemIds;
    }

    public void onLoad()
    {
        ItemHandler.getInstance().registerItemHandler(this);
    }

    public void onReload()
    {
    }

    public void onShutdown()
    {
    }

    private static final int _itemIds[] = {
        8570
    };
    protected static final L2DropData _dropscrolls[] = {
        new L2DropData(10554, 1L, 1L, 333D, 1), new L2DropData(10553, 1L, 1L, 333D, 1), new L2DropData(10565, 1L, 1L, 333D, 1), new L2DropData(10586, 1L, 1L, 333D, 1), new L2DropData(10558, 1L, 1L, 333D, 1), new L2DropData(10570, 1L, 1L, 333D, 1), new L2DropData(10567, 1L, 1L, 333D, 1), new L2DropData(10592, 1L, 1L, 333D, 1), new L2DropData(10577, 1L, 1L, 333D, 1), new L2DropData(10561, 1L, 1L, 333D, 1), 
        new L2DropData(10560, 1L, 1L, 333D, 1), new L2DropData(10552, 1L, 1L, 333D, 1), new L2DropData(10572, 1L, 1L, 333D, 1), new L2DropData(10564, 1L, 1L, 333D, 1), new L2DropData(10569, 1L, 1L, 333D, 1), new L2DropData(10573, 1L, 1L, 333D, 1), new L2DropData(10566, 1L, 1L, 333D, 1), new L2DropData(10563, 1L, 1L, 333D, 1), new L2DropData(10571, 1L, 1L, 333D, 1), new L2DropData(10556, 1L, 1L, 333D, 1), 
        new L2DropData(10574, 1L, 1L, 333D, 1), new L2DropData(10587, 1L, 1L, 333D, 1), new L2DropData(10578, 1L, 1L, 333D, 1), new L2DropData(10593, 1L, 1L, 333D, 1), new L2DropData(10589, 1L, 1L, 333D, 1), new L2DropData(10555, 1L, 1L, 333D, 1), new L2DropData(10576, 1L, 1L, 333D, 1), new L2DropData(10559, 1L, 1L, 333D, 1), new L2DropData(10557, 1L, 1L, 333D, 1), new L2DropData(10591, 1L, 1L, 333D, 1), 
        new L2DropData(10585, 1L, 1L, 333D, 1), new L2DropData(10588, 1L, 1L, 333D, 1), new L2DropData(10575, 1L, 1L, 333D, 1), new L2DropData(10568, 1L, 1L, 333D, 1), new L2DropData(10594, 1L, 1L, 333D, 1), new L2DropData(10562, 1L, 1L, 333D, 1), new L2DropData(10595, 1L, 1L, 333D, 1), new L2DropData(57, 1L, 0x4c4b40L, 800000D, 1)
    };
    protected static final L2DropData _dropother[] = {
        new L2DropData(2133, 1L, 33L, 10000D, 1), new L2DropData(2134, 1L, 10L, 10000D, 1), new L2DropData(6622, 1L, 1L, 5000D, 1), new L2DropData(9912, 1L, 100L, 10000D, 1), new L2DropData(9625, 1L, 1L, 5000D, 1), new L2DropData(9626, 1L, 1L, 5000D, 1), new L2DropData(9627, 1L, 1L, 5000D, 1), new L2DropData(956, 1L, 10L, 6666D, 1), new L2DropData(6576, 1L, 2L, 3333D, 1), new L2DropData(955, 1L, 5L, 6666D, 1), 
        new L2DropData(6575, 1L, 2L, 3333D, 1), new L2DropData(952, 1L, 5L, 6666D, 1), new L2DropData(6574, 1L, 1L, 3333D, 1), new L2DropData(951, 1L, 3L, 6666D, 1), new L2DropData(6573, 1L, 1L, 3333D, 1), new L2DropData(948, 1L, 2L, 6666D, 1), new L2DropData(6572, 1L, 1L, 3333D, 1), new L2DropData(947, 1L, 1L, 6666D, 1), new L2DropData(6571, 1L, 1L, 3333D, 1), new L2DropData(730, 1L, 1L, 6666D, 1), 
        new L2DropData(6570, 1L, 1L, 3333D, 1), new L2DropData(729, 1L, 1L, 6666D, 1), new L2DropData(6569, 1L, 1L, 3333D, 1), new L2DropData(960, 1L, 1L, 6666D, 1), new L2DropData(6578, 1L, 1L, 3333D, 1), new L2DropData(959, 1L, 1L, 6666D, 1), new L2DropData(6577, 1L, 1L, 3333D, 1), new L2DropData(5577, 1L, 1L, 1500D, 1), new L2DropData(5578, 1L, 1L, 1500D, 1), new L2DropData(5579, 1L, 1L, 1500D, 1), 
        new L2DropData(5580, 1L, 1L, 1200D, 1), new L2DropData(5581, 1L, 1L, 1200D, 1), new L2DropData(5582, 1L, 1L, 1200D, 1), new L2DropData(5908, 1L, 1L, 900D, 1), new L2DropData(5911, 1L, 1L, 900D, 1), new L2DropData(5914, 1L, 1L, 900D, 1), new L2DropData(9570, 1L, 1L, 600D, 1), new L2DropData(9571, 1L, 1L, 600D, 1), new L2DropData(9572, 1L, 1L, 600D, 1), new L2DropData(13546, 1L, 1L, 3333D, 1), 
        new L2DropData(13547, 1L, 1L, 3333D, 1), new L2DropData(20029, 1L, 1L, 3333D, 1), new L2DropData(57, 1L, 0x4c4b40L, 700000D, 1)
    };
    protected static final L2DropData _droparmor[] = {
        new L2DropData(2399, 1L, 1L, 900D, 1), new L2DropData(2404, 1L, 1L, 900D, 1), new L2DropData(2417, 1L, 1L, 900D, 1), new L2DropData(5740, 1L, 1L, 900D, 1), new L2DropData(5724, 1L, 1L, 900D, 1), new L2DropData(2398, 1L, 1L, 900D, 1), new L2DropData(2403, 1L, 1L, 900D, 1), new L2DropData(2416, 1L, 1L, 900D, 1), new L2DropData(5736, 1L, 1L, 900D, 1), new L2DropData(5720, 1L, 1L, 900D, 1), 
        new L2DropData(2392, 1L, 1L, 900D, 1), new L2DropData(2417, 1L, 1L, 900D, 1), new L2DropData(5723, 1L, 1L, 900D, 1), new L2DropData(5739, 1L, 1L, 900D, 1), new L2DropData(2391, 1L, 1L, 900D, 1), new L2DropData(2416, 1L, 1L, 900D, 1), new L2DropData(5719, 1L, 1L, 900D, 1), new L2DropData(5735, 1L, 1L, 900D, 1), new L2DropData(2381, 1L, 1L, 900D, 1), new L2DropData(2417, 1L, 1L, 900D, 1), 
        new L2DropData(5738, 1L, 1L, 900D, 1), new L2DropData(5722, 1L, 1L, 900D, 1), new L2DropData(110, 1L, 1L, 900D, 1), new L2DropData(358, 1L, 1L, 900D, 1), new L2DropData(2380, 1L, 1L, 900D, 1), new L2DropData(2416, 1L, 1L, 900D, 1), new L2DropData(5734, 1L, 1L, 900D, 1), new L2DropData(5718, 1L, 1L, 900D, 1), new L2DropData(2400, 1L, 1L, 600D, 1), new L2DropData(2405, 1L, 1L, 600D, 1), 
        new L2DropData(547, 1L, 1L, 600D, 1), new L2DropData(5770, 1L, 1L, 600D, 1), new L2DropData(5782, 1L, 1L, 600D, 1), new L2DropData(2407, 1L, 1L, 600D, 1), new L2DropData(512, 1L, 1L, 600D, 1), new L2DropData(5779, 1L, 1L, 600D, 1), new L2DropData(5767, 1L, 1L, 600D, 1), new L2DropData(2408, 1L, 1L, 600D, 1), new L2DropData(2418, 1L, 1L, 600D, 1), new L2DropData(5773, 1L, 1L, 600D, 1), 
        new L2DropData(5785, 1L, 1L, 600D, 1), new L2DropData(2409, 1L, 1L, 600D, 1), new L2DropData(2419, 1L, 1L, 600D, 1), new L2DropData(5776, 1L, 1L, 600D, 1), new L2DropData(5788, 1L, 1L, 600D, 1), new L2DropData(2393, 1L, 1L, 600D, 1), new L2DropData(547, 1L, 1L, 600D, 1), new L2DropData(5769, 1L, 1L, 600D, 1), new L2DropData(5781, 1L, 1L, 600D, 1), new L2DropData(2385, 1L, 1L, 600D, 1), 
        new L2DropData(2389, 1L, 1L, 600D, 1), new L2DropData(512, 1L, 1L, 600D, 1), new L2DropData(5766, 1L, 1L, 600D, 1), new L2DropData(5778, 1L, 1L, 600D, 1), new L2DropData(2394, 1L, 1L, 600D, 1), new L2DropData(2418, 1L, 1L, 600D, 1), new L2DropData(5772, 1L, 1L, 600D, 1), new L2DropData(5784, 1L, 1L, 600D, 1), new L2DropData(2395, 1L, 1L, 600D, 1), new L2DropData(2419, 1L, 1L, 600D, 1), 
        new L2DropData(5775, 1L, 1L, 600D, 1), new L2DropData(5787, 1L, 1L, 600D, 1), new L2DropData(2382, 1L, 1L, 600D, 1), new L2DropData(547, 1L, 1L, 600D, 1), new L2DropData(5768, 1L, 1L, 600D, 1), new L2DropData(5780, 1L, 1L, 600D, 1), new L2DropData(365, 1L, 1L, 600D, 1), new L2DropData(388, 1L, 1L, 600D, 1), new L2DropData(512, 1L, 1L, 600D, 1), new L2DropData(5765, 1L, 1L, 600D, 1), 
        new L2DropData(5777, 1L, 1L, 600D, 1), new L2DropData(641, 1L, 1L, 600D, 1), new L2DropData(374, 1L, 1L, 600D, 1), new L2DropData(2418, 1L, 1L, 600D, 1), new L2DropData(5771, 1L, 1L, 600D, 1), new L2DropData(5783, 1L, 1L, 600D, 1), new L2DropData(2498, 1L, 1L, 600D, 1), new L2DropData(2383, 1L, 1L, 600D, 1), new L2DropData(2419, 1L, 1L, 600D, 1), new L2DropData(5774, 1L, 1L, 600D, 1), 
        new L2DropData(5786, 1L, 1L, 600D, 1), new L2DropData(6383, 1L, 1L, 450D, 1), new L2DropData(6386, 1L, 1L, 450D, 1), new L2DropData(6385, 1L, 1L, 450D, 1), new L2DropData(6384, 1L, 1L, 450D, 1), new L2DropData(6379, 1L, 1L, 450D, 1), new L2DropData(6382, 1L, 1L, 450D, 1), new L2DropData(6381, 1L, 1L, 450D, 1), new L2DropData(6380, 1L, 1L, 450D, 1), new L2DropData(6373, 1L, 1L, 450D, 1), 
        new L2DropData(6378, 1L, 1L, 450D, 1), new L2DropData(6374, 1L, 1L, 450D, 1), new L2DropData(6375, 1L, 1L, 450D, 1), new L2DropData(6376, 1L, 1L, 450D, 1), new L2DropData(6377, 1L, 1L, 450D, 1), new L2DropData(9432, 1L, 1L, 250D, 1), new L2DropData(9437, 1L, 1L, 250D, 1), new L2DropData(9438, 1L, 1L, 250D, 1), new L2DropData(9439, 1L, 1L, 250D, 1), new L2DropData(9440, 1L, 1L, 250D, 1), 
        new L2DropData(9425, 1L, 1L, 250D, 1), new L2DropData(9428, 1L, 1L, 250D, 1), new L2DropData(9429, 1L, 1L, 250D, 1), new L2DropData(9430, 1L, 1L, 250D, 1), new L2DropData(9431, 1L, 1L, 250D, 1), new L2DropData(9416, 1L, 1L, 250D, 1), new L2DropData(9421, 1L, 1L, 250D, 1), new L2DropData(9422, 1L, 1L, 250D, 1), new L2DropData(9423, 1L, 1L, 250D, 1), new L2DropData(9424, 1L, 1L, 250D, 1), 
        new L2DropData(9441, 1L, 1L, 250D, 1), new L2DropData(57, 1L, 0x4c4b40L, 700000D, 1)
    };
    protected static final L2DropData _dropweapon[] = {
        new L2DropData(7883, 1L, 1L, 450D, 1), new L2DropData(287, 1L, 1L, 450D, 1), new L2DropData(79, 1L, 1L, 450D, 1), new L2DropData(234, 1L, 1L, 450D, 1), new L2DropData(97, 1L, 1L, 450D, 1), new L2DropData(171, 1L, 1L, 450D, 1), new L2DropData(175, 1L, 1L, 450D, 1), new L2DropData(210, 1L, 1L, 450D, 1), new L2DropData(7901, 1L, 1L, 450D, 1), new L2DropData(268, 1L, 1L, 450D, 1), 
        new L2DropData(7889, 1L, 1L, 450D, 1), new L2DropData(80, 1L, 1L, 400D, 1), new L2DropData(150, 1L, 1L, 400D, 1), new L2DropData(98, 1L, 1L, 400D, 1), new L2DropData(212, 1L, 1L, 400D, 1), new L2DropData(288, 1L, 1L, 400D, 1), new L2DropData(235, 1L, 1L, 400D, 1), new L2DropData(2504, 1L, 1L, 400D, 1), new L2DropData(269, 1L, 1L, 400D, 1), new L2DropData(7894, 1L, 1L, 400D, 1), 
        new L2DropData(7884, 1L, 1L, 250D, 1), new L2DropData(7895, 1L, 1L, 250D, 1), new L2DropData(305, 1L, 1L, 250D, 1), new L2DropData(289, 1L, 1L, 250D, 1), new L2DropData(270, 1L, 1L, 250D, 1), new L2DropData(236, 1L, 1L, 250D, 1), new L2DropData(213, 1L, 1L, 250D, 1), new L2DropData(164, 1L, 1L, 250D, 1), new L2DropData(151, 1L, 1L, 250D, 1), new L2DropData(81, 1L, 1L, 250D, 1), 
        new L2DropData(2500, 1L, 1L, 250D, 1), new L2DropData(7902, 1L, 1L, 250D, 1), new L2DropData(8679, 1L, 1L, 200D, 1), new L2DropData(8680, 1L, 1L, 200D, 1), new L2DropData(8681, 1L, 1L, 200D, 1), new L2DropData(8682, 1L, 1L, 200D, 1), new L2DropData(8683, 1L, 1L, 200D, 1), new L2DropData(8684, 1L, 1L, 200D, 1), new L2DropData(8685, 1L, 1L, 200D, 1), new L2DropData(8686, 1L, 1L, 200D, 1), 
        new L2DropData(8687, 1L, 1L, 200D, 1), new L2DropData(8688, 1L, 1L, 200D, 1), new L2DropData(8678, 1L, 1L, 200D, 1), new L2DropData(6579, 1L, 1L, 200D, 1), new L2DropData(6364, 1L, 1L, 200D, 1), new L2DropData(6365, 1L, 1L, 200D, 1), new L2DropData(6366, 1L, 1L, 200D, 1), new L2DropData(6367, 1L, 1L, 200D, 1), new L2DropData(6369, 1L, 1L, 200D, 1), new L2DropData(6370, 1L, 1L, 200D, 1), 
        new L2DropData(6371, 1L, 1L, 200D, 1), new L2DropData(6372, 1L, 1L, 200D, 1), new L2DropData(7575, 1L, 1L, 200D, 1), new L2DropData(6368, 1L, 1L, 200D, 1), new L2DropData(9449, 1L, 1L, 150D, 1), new L2DropData(9443, 1L, 1L, 150D, 1), new L2DropData(9444, 1L, 1L, 150D, 1), new L2DropData(9445, 1L, 1L, 150D, 1), new L2DropData(9446, 1L, 1L, 150D, 1), new L2DropData(9447, 1L, 1L, 150D, 1), 
        new L2DropData(9448, 1L, 1L, 150D, 1), new L2DropData(9450, 1L, 1L, 150D, 1), new L2DropData(9442, 1L, 1L, 150D, 1), new L2DropData(10252, 1L, 1L, 150D, 1), new L2DropData(10253, 1L, 1L, 150D, 1), new L2DropData(10217, 1L, 1L, 100D, 1), new L2DropData(10215, 1L, 1L, 100D, 1), new L2DropData(10222, 1L, 1L, 100D, 1), new L2DropData(10223, 1L, 1L, 100D, 1), new L2DropData(10218, 1L, 1L, 100D, 1), 
        new L2DropData(10219, 1L, 1L, 100D, 1), new L2DropData(10220, 1L, 1L, 100D, 1), new L2DropData(10221, 1L, 1L, 100D, 1), new L2DropData(10216, 1L, 1L, 100D, 1), new L2DropData(5233, 1L, 1L, 450D, 1), new L2DropData(5705, 1L, 1L, 200D, 1), new L2DropData(5706, 1L, 1L, 250D, 1), new L2DropData(8938, 1L, 1L, 150D, 1), new L2DropData(6580, 1L, 1L, 150D, 1), new L2DropData(10004, 1L, 1L, 150D, 1), 
        new L2DropData(10415, 1L, 1L, 100D, 1), new L2DropData(57, 1L, 0x4c4b40L, 700000D, 1)
    };
    protected static final L2DropData _dropjew[] = {
        new L2DropData(918, 1L, 1L, 900D, 1), new L2DropData(856, 1L, 1L, 900D, 1), new L2DropData(887, 1L, 1L, 900D, 1), new L2DropData(935, 1L, 1L, 900D, 1), new L2DropData(873, 1L, 1L, 900D, 1), new L2DropData(904, 1L, 1L, 900D, 1), new L2DropData(921, 1L, 1L, 900D, 1), new L2DropData(859, 1L, 1L, 900D, 1), new L2DropData(117, 1L, 1L, 900D, 1), new L2DropData(927, 1L, 1L, 900D, 1), 
        new L2DropData(865, 1L, 1L, 900D, 1), new L2DropData(896, 1L, 1L, 900D, 1), new L2DropData(926, 1L, 1L, 900D, 1), new L2DropData(864, 1L, 1L, 900D, 1), new L2DropData(895, 1L, 1L, 900D, 1), new L2DropData(930, 1L, 1L, 600D, 1), new L2DropData(868, 1L, 1L, 600D, 1), new L2DropData(899, 1L, 1L, 600D, 1), new L2DropData(934, 1L, 1L, 600D, 1), new L2DropData(872, 1L, 1L, 600D, 1), 
        new L2DropData(903, 1L, 1L, 600D, 1), new L2DropData(933, 1L, 1L, 600D, 1), new L2DropData(871, 1L, 1L, 600D, 1), new L2DropData(902, 1L, 1L, 600D, 1), new L2DropData(924, 1L, 1L, 450D, 1), new L2DropData(862, 1L, 1L, 450D, 1), new L2DropData(893, 1L, 1L, 450D, 1), new L2DropData(920, 1L, 1L, 250D, 1), new L2DropData(858, 1L, 1L, 250D, 1), new L2DropData(889, 1L, 1L, 250D, 1), 
        new L2DropData(9456, 1L, 1L, 150D, 1), new L2DropData(9455, 1L, 1L, 150D, 1), new L2DropData(9457, 1L, 1L, 150D, 1), new L2DropData(57, 1L, 0x4c4b40L, 800000D, 1)
    };
    protected static final L2DropData _dropadena[] = {
        new L2DropData(57, 1L, 0x4c4b40L, 900000D, 1), new L2DropData(6673, 33L, 333L, 45000D, 1), new L2DropData(5575, 1L, 0xf4240L, 50000D, 1), new L2DropData(4037, 1L, 33L, 5000D, 1)
    };

}
