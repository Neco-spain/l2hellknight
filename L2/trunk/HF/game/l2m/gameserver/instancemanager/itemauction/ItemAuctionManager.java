package l2m.gameserver.instancemanager.itemauction;

import gnu.trove.TIntObjectHashMap;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import l2p.commons.dbutils.DbUtils;
import l2p.commons.time.cron.SchedulingPattern;
import l2m.gameserver.Config;
import l2m.gameserver.database.DatabaseFactory;
import l2m.gameserver.templates.StatsSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class ItemAuctionManager
{
  private static Logger _log = LoggerFactory.getLogger(ItemAuctionManager.class);
  private static ItemAuctionManager _instance;
  private final TIntObjectHashMap<ItemAuctionInstance> _managerInstances = new TIntObjectHashMap();
  private final AtomicInteger _nextId = new AtomicInteger();

  public static final ItemAuctionManager getInstance()
  {
    if (_instance == null)
    {
      _instance = new ItemAuctionManager();
      if (Config.ALT_ITEM_AUCTION_ENABLED)
        _instance.load();
    }
    return _instance;
  }

  private ItemAuctionManager()
  {
    _log.info("Initializing ItemAuctionManager");
  }

  private void load()
  {
    Connection con = null;
    PreparedStatement statement = null;
    ResultSet rset = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("SELECT auctionId FROM item_auction ORDER BY auctionId DESC LIMIT 0, 1");
      rset = statement.executeQuery();
      if (rset.next())
        _nextId.set(rset.getInt(1));
    }
    catch (SQLException e)
    {
      _log.error("ItemAuctionManager: Failed loading auctions.", e);
    }
    finally
    {
      DbUtils.closeQuietly(con, statement, rset);
    }

    File file = new File(Config.DATAPACK_ROOT, "data/item_auctions.xml");
    if (!file.exists())
    {
      _log.warn("ItemAuctionManager: Missing item_auctions.xml!");
      return;
    }

    try
    {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setValidating(false);
      factory.setIgnoringComments(true);
      DocumentBuilder builder = factory.newDocumentBuilder();

      Document doc = builder.parse(file);
      for (Node na = doc.getFirstChild(); na != null; na = na.getNextSibling()) {
        if ("list".equalsIgnoreCase(na.getNodeName()))
          for (Node nb = na.getFirstChild(); nb != null; nb = nb.getNextSibling()) {
            if (!"instance".equalsIgnoreCase(nb.getNodeName()))
              continue;
            NamedNodeMap nab = nb.getAttributes();
            int instanceId = Integer.parseInt(nab.getNamedItem("id").getNodeValue());

            if (_managerInstances.containsKey(instanceId)) {
              throw new Exception("Duplicate instanceId " + instanceId);
            }
            SchedulingPattern dateTime = new SchedulingPattern(nab.getNamedItem("schedule").getNodeValue());

            List items = new ArrayList();

            for (Node nc = nb.getFirstChild(); nc != null; nc = nc.getNextSibling()) {
              if (!"item".equalsIgnoreCase(nc.getNodeName()))
                continue;
              NamedNodeMap nac = nc.getAttributes();
              int auctionItemId = Integer.parseInt(nac.getNamedItem("auctionItemId").getNodeValue());
              int auctionLenght = Integer.parseInt(nac.getNamedItem("auctionLenght").getNodeValue());
              long auctionInitBid = Integer.parseInt(nac.getNamedItem("auctionInitBid").getNodeValue());

              int itemId = Integer.parseInt(nac.getNamedItem("itemId").getNodeValue());
              int itemCount = Integer.parseInt(nac.getNamedItem("itemCount").getNodeValue());

              if (auctionLenght < 1) {
                throw new IllegalArgumentException("auctionLenght < 1 for instanceId: " + instanceId + ", itemId " + itemId);
              }
              for (AuctionItem tmp : items) {
                if (tmp.getAuctionItemId() == auctionItemId)
                  throw new IllegalArgumentException("Dublicated auction item id " + auctionItemId + "for instanceId: " + instanceId);
              }
              StatsSet itemExtra = new StatsSet();
              NamedNodeMap nad;
              int i;
              for (Node nd = nc.getFirstChild(); nd != null; nd = nd.getNextSibling()) {
                if (!"extra".equalsIgnoreCase(nd.getNodeName()))
                  continue;
                nad = nd.getAttributes();
                for (i = nad.getLength(); i-- > 0; )
                {
                  Node n = nad.item(i);
                  if (n != null) {
                    itemExtra.set(n.getNodeName(), n.getNodeValue());
                  }
                }
              }
              AuctionItem item = new AuctionItem(auctionItemId, auctionLenght, auctionInitBid, itemId, itemCount, itemExtra);
              items.add(item);
            }

            if (items.isEmpty()) {
              throw new IllegalArgumentException("No items defined for instanceId: " + instanceId);
            }
            ItemAuctionInstance instance = new ItemAuctionInstance(instanceId, dateTime, items);
            _managerInstances.put(instanceId, instance);
          }
      }
      _log.info("ItemAuctionManager: Loaded " + _managerInstances.size() + " instance(s).");
    }
    catch (Exception e)
    {
      _log.error("ItemAuctionManager: Error while loading ItemAuctions.xml!", e);
    }
  }

  public void shutdown()
  {
    ItemAuctionInstance[] instances = (ItemAuctionInstance[])_managerInstances.getValues(new ItemAuctionInstance[_managerInstances.size()]);
    for (ItemAuctionInstance instance : instances)
      instance.shutdown();
  }

  public ItemAuctionInstance getManagerInstance(int instanceId)
  {
    return (ItemAuctionInstance)_managerInstances.get(instanceId);
  }

  public int getNextId()
  {
    return _nextId.incrementAndGet();
  }

  public void deleteAuction(int auctionId)
  {
    Connection con = null;
    PreparedStatement statement = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("DELETE FROM item_auction WHERE auctionId=?");
      statement.setInt(1, auctionId);
      statement.execute();
      statement.close();

      statement = con.prepareStatement("DELETE FROM item_auction_bid WHERE auctionId=?");
      statement.setInt(1, auctionId);
      statement.execute();
      statement.close();
    }
    catch (SQLException e)
    {
      _log.error("ItemAuctionManager: Failed deleting auction: " + auctionId, e);
    }
    finally
    {
      DbUtils.closeQuietly(con, statement);
    }
  }
}