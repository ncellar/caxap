package compiler.util;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.JavaFileObject;

import driver.Context;

public class ThrowingDiagnosticListener
implements DiagnosticListener<JavaFileObject>
{
  /****************************************************************************/
  @Override public void report(Diagnostic<? extends JavaFileObject> diagnostic)
  {
    throw new Error("Compilation error in file " + Context.get().currentFile
      + " : " + diagnostic.getMessage(null));
  }
}
