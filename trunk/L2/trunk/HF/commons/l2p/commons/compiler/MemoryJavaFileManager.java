package l2m.commons.compiler;

import java.io.IOException;
import java.net.URI;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager.Location;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardJavaFileManager;

public class MemoryJavaFileManager extends ForwardingJavaFileManager<StandardJavaFileManager>
{
  private MemoryClassLoader cl;

  public MemoryJavaFileManager(StandardJavaFileManager sjfm, MemoryClassLoader xcl)
  {
    super(sjfm);
    cl = xcl;
  }

  public JavaFileObject getJavaFileForOutput(JavaFileManager.Location location, String className, JavaFileObject.Kind kind, FileObject sibling)
    throws IOException
  {
    MemoryByteCode mbc = new MemoryByteCode(className.replace('/', '.').replace('\\', '.'), URI.create("file:///" + className.replace('.', '/').replace('\\', '/') + kind.extension));
    cl.addClass(mbc);

    return mbc;
  }

  public ClassLoader getClassLoader(JavaFileManager.Location location)
  {
    return cl;
  }
}