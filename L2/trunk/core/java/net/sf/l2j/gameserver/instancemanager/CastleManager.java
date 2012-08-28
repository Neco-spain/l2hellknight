package net.sf.l2j.gameserver.instancemanager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import javolution.util.FastList;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.SevenSigns;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2ClanMember;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.Castle;


public class CastleManager
{
    // =========================================================
    private static CastleManager _instance;
    public static final CastleManager getInstance()
    {
        if (_instance == null)
        {
    		System.out.println("Initializing CastleManager");
            _instance = new CastleManager();
            _instance.load();
        }
        return _instance;
    }
    // =========================================================


    // =========================================================
    // Data Field
    private List<Castle> _castles;

    // =========================================================
    // Constructor
    private static final int _castleCirclets[] = { 0, 6838, 6835, 6839, 6837, 6840, 6834, 6836, 8182, 8183 };
    public CastleManager() {}

    // =========================================================
    // Method - Public

    public final int findNearestCastleIndex(L2Object obj)
    {
        int index = getCastleIndex(obj);
        if (index < 0)
        {
            double closestDistance = 99999999;
            double distance;
            Castle castle;
            for (int i = 0; i < getCastles().size(); i++)
            {
                castle = getCastles().get(i);
                if (castle == null) continue;
                distance = castle.getDistance(obj);
                if (closestDistance > distance)
                {
                    closestDistance = distance;
                    index = i;
                }
            }
        }
        return index;
    }

    // =========================================================
    // Method - Private
    private final void load()
    {
        java.sql.Connection con = null;
        try
        {
            PreparedStatement statement;
            ResultSet rs;

            con = L2DatabaseFactory.getInstance().getConnection();

            statement = con.prepareStatement("Select id from castle order by id");
            rs = statement.executeQuery();

            while (rs.next())
            {
                getCastles().add(new Castle(rs.getInt("id")));
            }

            statement.close();

            System.out.println("Loaded: " + getCastles().size() + " castles");
        }
        catch (Exception e)
        {
            System.out.println("Exception: loadCastleData(): " + e.getMessage());
            e.printStackTrace();
        }

        finally {try { con.close(); } catch (Exception e) {}}
    }

    // =========================================================
    // Property - Public

    public final Castle getCastleById(int castleId)
    {
    	for (Castle temp : getCastles())
    	{
    		if (temp.getCastleId() == castleId)
    			return temp;
    	}
        return null;
    }

    public final Castle getCastleByOwner(L2Clan clan)
    {
    	for (Castle temp : getCastles())
    	{
    		if (temp.getOwnerId() == clan.getClanId())
    			return temp;
    	}
        return null;
    }

    public final Castle getCastle(String name)
    {
    	for (Castle temp : getCastles())
    	{
    		if (temp.getName().equalsIgnoreCase(name.trim()))
    			return temp;
    	}
        return null;
    }

    public final Castle getCastle(int x, int y, int z)
    {
    	for (Castle temp : getCastles())
    	{
    		if (temp.checkIfInZone(x, y, z))
    			return temp;
    	}
        return null;
    }

    public final Castle getCastle(L2Object activeObject) { return getCastle(activeObject.getX(), activeObject.getY(), activeObject.getZ()); }

    public final int getCastleIndex(int castleId)
    {
        Castle castle;
        for (int i = 0; i < getCastles().size(); i++)
        {
            castle = getCastles().get(i);
            if (castle != null && castle.getCastleId() == castleId) return i;
        }
        return -1;
    }

    public final int getCastleIndex(L2Object activeObject)
    {
    	return getCastleIndex(activeObject.getX(), activeObject.getY(), activeObject.getZ());
    }

    public final int getCastleIndex(int x, int y, int z)
    {
        Castle castle;
        for (int i = 0; i < getCastles().size(); i++)
        {
            castle = getCastles().get(i);
            if (castle != null && castle.checkIfInZone(x, y, z)) return i;
        }
        return -1;
    }

    public final List<Castle> getCastles()
    {
        if (_castles == null) _castles = new FastList<Castle>();
        return _castles;
    }

	public final void validateTaxes(int sealStrifeOwner)
	{
		int maxTax;
		switch(sealStrifeOwner)
		{
			case SevenSigns.CABAL_DUSK:
				maxTax = 5;
				break;
			case SevenSigns.CABAL_DAWN:
				maxTax = 25;
				break;
			default: // no owner
				maxTax = 15;
				break;
		}
		for(Castle castle : _castles)
			if(castle.getTaxPercent() > maxTax)
				castle.setTaxPercent(maxTax);
	}

	int _castleId = 1; // from this castle
	public int getCirclet()
	{
		return getCircletByCastleId(_castleId);
	}

	public int getCircletByCastleId(int castleId)
	{
		if (castleId > 0 && castleId < 10)
			return _castleCirclets[castleId];

		return 0;
	}

	// remove this castle's circlets from the clan
	public void removeCirclet(L2Clan clan, int castleId)
	{
		for (L2ClanMember member : clan.getMembers())
			removeCirclet(member, castleId);
	}
	public void removeCirclet(L2ClanMember member, int castleId)
	{
		if (member == null) return;
		L2PcInstance player = member.getPlayerInstance();
		int circletId = getCircletByCastleId(castleId);

		if (circletId != 0)
		{
			// online-player circlet removal
			if (player != null)
			{
				try
				{
					L2ItemInstance circlet = player.getInventory().getItemByItemId(circletId);
					if (circlet != null)
					{
						if (circlet.isEquipped())
							player.getInventory().unEquipItemInSlotAndRecord(circlet.getEquipSlot());
						player.destroyItemByItemId("CastleCircletRemoval", circletId, 1, player, true);
					}
					return;
				} catch (NullPointerException e)
				{
					// continue removing offline
				}
			}
			// else offline-player circlet removal
			java.sql.Connection con = null;
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement("DELETE FROM items WHERE owner_id = ? and item_id = ?");
				statement.setInt(1, member.getObjectId());
				statement.setInt(2, circletId);
				statement.execute();
				statement.close();
			}
			catch (Exception e)
			{
				System.out.println("Failed to remove castle circlets offline for player "+member.getName());
				e.printStackTrace();
			}
			finally
			{
				try { con.close(); } catch (Exception e) {}
			}
		}
	}
}
