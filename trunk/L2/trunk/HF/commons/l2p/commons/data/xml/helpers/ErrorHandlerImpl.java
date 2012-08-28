package l2m.commons.data.xml.helpers;

import l2m.commons.data.xml.AbstractParser;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class ErrorHandlerImpl
  implements ErrorHandler
{
  private AbstractParser<?> _parser;

  public ErrorHandlerImpl(AbstractParser<?> parser)
  {
    _parser = parser;
  }

  public void warning(SAXParseException exception)
    throws SAXException
  {
    _parser.warn("File: " + _parser.getCurrentFileName() + ":" + exception.getLineNumber() + " warning: " + exception.getMessage());
  }

  public void error(SAXParseException exception)
    throws SAXException
  {
    _parser.error("File: " + _parser.getCurrentFileName() + ":" + exception.getLineNumber() + " error: " + exception.getMessage());
  }

  public void fatalError(SAXParseException exception)
    throws SAXException
  {
    _parser.error("File: " + _parser.getCurrentFileName() + ":" + exception.getLineNumber() + " fatal: " + exception.getMessage());
  }
}