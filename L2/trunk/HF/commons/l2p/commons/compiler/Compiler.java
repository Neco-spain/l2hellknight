package l2m.commons.compiler;

import java.io.File;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import org.eclipse.jdt.internal.compiler.tool.EclipseCompiler;
import org.eclipse.jdt.internal.compiler.tool.EclipseFileManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Compiler
{
  private static final Logger _log = LoggerFactory.getLogger(Compiler.class);

  private static final JavaCompiler javac = new EclipseCompiler();
  private final DiagnosticListener<JavaFileObject> listener;
  private final StandardJavaFileManager fileManager;
  private final MemoryClassLoader memClassLoader;
  private final MemoryJavaFileManager memFileManager;

  public Compiler()
  {
    listener = new DefaultDiagnosticListener(null);
    fileManager = new EclipseFileManager(Locale.getDefault(), Charset.defaultCharset());
    memClassLoader = new MemoryClassLoader();
    memFileManager = new MemoryJavaFileManager(fileManager, memClassLoader);
  }

  public boolean compile(File[] files)
  {
    List options = new ArrayList();
    options.add("-Xlint:all");
    options.add("-warn:none");

    options.add("-g");

    Writer writer = new StringWriter();
    JavaCompiler.CompilationTask compile = javac.getTask(writer, memFileManager, listener, options, null, fileManager.getJavaFileObjects(files));

    return compile.call().booleanValue();
  }

  public boolean compile(Collection<File> files)
  {
    return compile((File[])files.toArray(new File[files.size()]));
  }

  public MemoryClassLoader getClassLoader()
  {
    return memClassLoader;
  }

  private class DefaultDiagnosticListener implements DiagnosticListener<JavaFileObject> {
    private DefaultDiagnosticListener() {
    }

    public void report(Diagnostic<? extends JavaFileObject> diagnostic) {
      Compiler._log.error(new StringBuilder().append(((JavaFileObject)diagnostic.getSource()).getName()).append(diagnostic.getPosition() == -1L ? "" : new StringBuilder().append(":").append(diagnostic.getLineNumber()).append(",").append(diagnostic.getColumnNumber()).toString()).append(": ").append(diagnostic.getMessage(Locale.getDefault())).toString());
    }
  }
}