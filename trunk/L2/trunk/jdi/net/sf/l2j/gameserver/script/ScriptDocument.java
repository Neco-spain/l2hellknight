package net.sf.l2j.gameserver.script;

import java.io.IOException;
import java.io.InputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class ScriptDocument
{
  private Document _document;
  private String _name;

  public ScriptDocument(String name, InputStream input)
  {
    _name = name;

    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    try
    {
      DocumentBuilder builder = factory.newDocumentBuilder();
      _document = builder.parse(input);
    }
    catch (SAXException sxe)
    {
      Exception x = sxe;
      if (sxe.getException() != null)
        x = sxe.getException();
      x.printStackTrace();
    }
    catch (ParserConfigurationException pce)
    {
      pce.printStackTrace();
    }
    catch (IOException ioe)
    {
      ioe.printStackTrace();
    }
  }

  public Document getDocument()
  {
    return _document;
  }

  public String getName()
  {
    return _name;
  }

  public String toString()
  {
    return _name;
  }
}