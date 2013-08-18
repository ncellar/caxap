package files;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static util.ListUtils.list;

import java.nio.file.Paths;
import java.util.List;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.MethodSorters;

@RunWith(JUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RequireTests
{
  @Test public void aa_require()
  {
    RelativeSourcePath path =
      new RelativeSourcePath(Paths.get("a/b/File.java"));

    List<String> classes = list("A", "B", "C");

    Require regular = new Require(path, classes, null);
    Require _macro  = new Require(path, null, "macro");
    Require _static = new Require(path, classes, "member");

    assertEquals(classes, regular.classes());
    assertEquals(null,    _macro.classes());
    assertEquals(classes, _static.classes());

    assertEquals(null,    regular.member());
    assertEquals(null,    regular.macro());
    assertEquals("macro",  _macro.macro());
    assertEquals("member", _static.member());

    assertEquals(path, regular.relativePath());

    assertTrue(regular.isRegular()  && !regular.isStatic() && !regular.isMacro());
    assertTrue(!_macro.isRegular()  && !_macro.isStatic()  && _macro.isMacro());
    assertTrue(!_static.isRegular() && _static.isStatic()  && !_static.isMacro());

    assertEquals(regular.classChain(), "A.B.C");
    assertEquals(regular.toString(),   "a.b.File:A.B.C");
    assertEquals(_macro.toString(),    "macro a.b.File:macro");
    assertEquals(_static.toString(),   "static a.b.File:A.B.C.member");

    Require allStatic = new Require(path, classes, "*");
    Require allMacro  = new Require(path, null, "*");
    assertTrue(allStatic.requiresAllMembers());
    assertTrue(allMacro .requiresAllMembers());

    Require regular2 = new Require(path, classes, null);
    Require _macro2  = new Require(path, null, "macro");
    Require _static2 = new Require(path, classes, "member");

    assertEquals(regular, regular2);
    assertEquals(_macro,  _macro2);
    assertEquals(_static, _static2);

    assertTrue(regular.hashCode() == regular2.hashCode());
    assertTrue(_macro .hashCode() == _macro2 .hashCode());
    assertTrue(_static.hashCode() == _static2.hashCode());

    assertFalse(regular.equals(_macro));
    assertFalse(regular.equals(_static));
    assertFalse(regular.hashCode() == _macro.hashCode());
    assertFalse(regular.hashCode() == _static.hashCode());

    Require allClasses = new Require(path, list("*"), null);
    assertTrue(allClasses.requiresAllClasses());
  }
}
