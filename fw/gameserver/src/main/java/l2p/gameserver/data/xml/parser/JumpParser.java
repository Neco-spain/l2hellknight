package l2p.gameserver.data.xml.parser;

import l2p.commons.data.xml.AbstractDirParser;
import l2p.gameserver.Config;
import l2p.gameserver.data.xml.holder.JumpHolder;
import l2p.gameserver.model.jump.JumpLocation;
import l2p.gameserver.utils.Location;
import org.dom4j.Element;

import java.io.File;

/**
 * @author : Ragnarok
 * @date : 16.01.12  15:44
 */
public class JumpParser extends AbstractDirParser<JumpHolder> {
    private static JumpParser ourInstance = new JumpParser();

    public static JumpParser getInstance() {
        return ourInstance;
    }

    private JumpParser() {
        super(JumpHolder.getInstance());
    }

    @Override
    public File getXMLDir() {
        return new File(Config.DATAPACK_ROOT, "data/jumps/");
    }

    @Override
    public boolean isIgnored(File f) {
        return false;
    }

    @Override
    public String getDTDFileName() {
        return "jumps.dtd";
    }

    @Override
    protected void readData(Element rootElement) throws Exception {
        for (Element loc : rootElement.elements("loc")) {
            String zoneName = loc.attributeValue("zone");
            int loc_id = Integer.parseInt(loc.attributeValue("id"));
            boolean isLast = loc.attributeValue("is_last") != null && loc.attributeValue("is_last").equals("true");

            int[] routes = new int[0];
            if (!isLast) {
                String parts[] = loc.attributeValue("routes").split(" ");
                routes = new int[parts.length];
                for (int i = 0; i < parts.length; i++) {
                    routes[i] = Integer.parseInt(parts[i]);
                }
            }
            int x = Integer.parseInt(loc.attributeValue("x"));
            int y = Integer.parseInt(loc.attributeValue("y"));
            int z = Integer.parseInt(loc.attributeValue("z"));
            JumpLocation location = new JumpLocation(zoneName, loc_id, isLast, routes, new Location(x, y, z));
            getHolder().addLocation(location);
        }
    }
}
