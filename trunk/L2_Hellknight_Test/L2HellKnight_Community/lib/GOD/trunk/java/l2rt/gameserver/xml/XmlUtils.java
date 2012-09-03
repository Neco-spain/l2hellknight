package l2rt.gameserver.xml;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.File;

/**
 * @author : Ragnarok
 * @date : 28.12.10    8:14
 */
public class XmlUtils {

    public static SAXReader createReader() {
        SAXReader reader = new SAXReader();
        reader.setValidation(false);
        reader.setIgnoreComments(true);
        return reader;
    }

    @Deprecated
    public static SAXReader createReader(boolean ignoreComments) {
        SAXReader reader = new SAXReader();
        reader.setValidation(false);
        reader.setIgnoreComments(ignoreComments);
        return reader;
    }

    public static Document readFile(File file) throws DocumentException {
        return createReader().read(file);
    }
    @Deprecated
    public static Document readFile(File file, boolean ignoreComments) throws DocumentException {
        return createReader(ignoreComments).read(file);
    }

    public static int getIntValue(Element e, String name, int def) {
        try {
            return Integer.parseInt(e.attributeValue(name));
        } catch (NumberFormatException nfe) {
            return def;
        }
    }

    public static long getLongValue(Element e, String name, long def) {
        try {
            return Long.parseLong(e.attributeValue(name));
        } catch (NumberFormatException nfe) {
            return def;
        }
    }

    public static boolean getBooleanValue(Element e, String name, boolean def) {
        if(name == null || e.attributeValue(name) == null)
            return def;
        return Boolean.parseBoolean(e.attributeValue(name));
    }

    public static int[] getIntArray(Element e, String name, String delimeter, int[] def) {
        if(name == null || e.attributeValue(name) == null)
            return def;
        int[] args = new int[e.attributeValue(name).split(delimeter).length];
        try {
            int i =0;
            for(String s : e.attributeValue(name).split(delimeter)) {
                args[i++] = Integer.parseInt(s);
            }
        } catch (NumberFormatException nfe) {
            nfe.printStackTrace();
            return def;
        }
        return args;
    }
}
