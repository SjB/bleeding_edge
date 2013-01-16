/*
 * Copyright (c) 2012, the Dart project authors.
 * 
 * Licensed under the Eclipse Public License v1.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.dart.java2dart;

import com.google.common.base.Joiner;
import com.google.common.io.Files;
import com.google.dart.engine.ast.ASTNode;
import com.google.dart.engine.ast.CompilationUnit;
import com.google.dart.engine.utilities.io.PrintStringWriter;
import com.google.dart.java2dart.util.ToFormattedSourceVisitor;

import junit.framework.TestCase;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;

/**
 * Test for general Java semantics to Dart translation.
 */
public class SemanticTest extends TestCase {

  static void printFormattedSource(ASTNode node) {
    String source = getFormattedSource(node);
    String[] lines = StringUtils.split(source, '\n');
    for (int i = 0; i < lines.length; i++) {
      String line = lines[i];
      System.out.print("\"");
      System.out.print(line);
      if (i != lines.length - 1) {
        System.out.println("\",");
      } else {
        System.out.println("\"");
      }
    }
  }

  /**
   * @return the formatted Dart source dump of the given {@link ASTNode}.
   */
  private static String getFormattedSource(ASTNode node) {
    PrintStringWriter writer = new PrintStringWriter();
    node.accept(new ToFormattedSourceVisitor(writer));
    return writer.toString();
  }

  /**
   * @return the single {@link String} with "\n" separated lines.
   */
  private static String toString(String... lines) {
    return Joiner.on("\n").join(lines);
  }

  private File tmpFolder;

  public void test_buildSingleDartUnit() throws Exception {
    setFileLines(
        "test/Main.java",
        toString(
            "// filler filler filler filler filler filler filler filler filler filler",
            "package test;",
            "public class Main {",
            "  static void foo() {}",
            "}",
            ""));
    setFileLines(
        "test/Second.java",
        toString(
            "// filler filler filler filler filler filler filler filler filler filler",
            "package test;",
            "public class Second {",
            "  static void bar() {",
            "    Main.foo();",
            "  }",
            "}",
            ""));
    Context context = new Context();
    context.addSourceFolder(tmpFolder);
    context.addSourceFiles(tmpFolder);
    CompilationUnit unit = context.translate();
    assertEquals(
        toString(
            "class Main {",
            "  static void foo() {",
            "  }",
            "}",
            "class Second {",
            "  static void bar() {",
            "    Main.foo();",
            "  }",
            "}"),
        getFormattedSource(unit));
  }

  public void test_configureRenameField() throws Exception {
    setFileLines(
        "test/A.java",
        toString(
            "// filler filler filler filler filler filler filler filler filler filler",
            "package test;",
            "public class A {",
            "  int foo;",
            "  static void foo() {}",
            "}",
            ""));
    setFileLines(
        "test/B.java",
        toString(
            "// filler filler filler filler filler filler filler filler filler filler",
            "package test;",
            "public class B {",
            "  static void bar() {",
            "    print(A.foo);",
            "  }",
            "}",
            ""));
    Context context = new Context();
    context.addSourceFolder(tmpFolder);
    context.addSourceFiles(tmpFolder);
    context.addRename("Ltest/A;.foo", "myField");
    CompilationUnit unit = context.translate();
    assertEquals(
        toString(
            "class A {",
            "  int myField = 0;",
            "  static void foo() {",
            "  }",
            "}",
            "class B {",
            "  static void bar() {",
            "    print(A.myField);",
            "  }",
            "}"),
        getFormattedSource(unit));
  }

  public void test_configureRenameMethod() throws Exception {
    setFileLines(
        "test/A.java",
        toString(
            "// filler filler filler filler filler filler filler filler filler filler",
            "package test;",
            "public class A {",
            "  static void foo() {}",
            "  static void foo(int p) {}",
            "}",
            ""));
    setFileLines(
        "test/B.java",
        toString(
            "// filler filler filler filler filler filler filler filler filler filler",
            "package test;",
            "public class B {",
            "  static void bar() {",
            "    A.foo(42);",
            "  }",
            "}",
            ""));
    Context context = new Context();
    context.addSourceFolder(tmpFolder);
    context.addSourceFiles(tmpFolder);
    context.addRename("Ltest/A;.foo(I)", "fooWithInt");
    CompilationUnit unit = context.translate();
    assertEquals(
        toString(
            "class A {",
            "  static void foo() {",
            "  }",
            "  static void fooWithInt(int p) {",
            "  }",
            "}",
            "class B {",
            "  static void bar() {",
            "    A.fooWithInt(42);",
            "  }",
            "}"),
        getFormattedSource(unit));
  }

  public void test_constructor_configureNames() throws Exception {
    setFileLines(
        "test/Test.java",
        toString(
            "// filler filler filler filler filler filler filler filler filler filler",
            "package test;",
            "public class Test {",
            "  Test() {",
            "    print(0);",
            "  }",
            "  Test(int p) {",
            "    print(1);",
            "  }",
            "  Test(double p) {",
            "    print(2);",
            "  }",
            "  static void main() {",
            "    new Test();",
            "    new Test(2);",
            "    new Test(3.0);",
            "  }",
            "}",
            ""));
    Context context = new Context();
    context.addSourceFolder(tmpFolder);
    context.addSourceFiles(tmpFolder);
    // configure names for constructors
    context.addRename("Ltest/Test;.(I)", "forInt");
    context.addRename("Ltest/Test;.(D)", "forDouble");
    // do translate
    CompilationUnit unit = context.translate();
    assertEquals(
        toString(
            "class Test {",
            "  Test() {",
            "    _jtd_constructor_0_impl();",
            "  }",
            "  _jtd_constructor_0_impl() {",
            "    print(0);",
            "  }",
            "  Test.forInt(int p) {",
            "    _jtd_constructor_1_impl(p);",
            "  }",
            "  _jtd_constructor_1_impl(int p) {",
            "    print(1);",
            "  }",
            "  Test.forDouble(double p) {",
            "    _jtd_constructor_2_impl(p);",
            "  }",
            "  _jtd_constructor_2_impl(double p) {",
            "    print(2);",
            "  }",
            "  static void main() {",
            "    new Test();",
            "    new Test.forInt(2);",
            "    new Test.forDouble(3.0);",
            "  }",
            "}"),
        getFormattedSource(unit));
  }

  public void test_constructor_defaultNames() throws Exception {
    setFileLines(
        "test/Test.java",
        toString(
            "// filler filler filler filler filler filler filler filler filler filler",
            "package test;",
            "public class Test {",
            "  Test() {",
            "    print(0);",
            "  }",
            "  Test(int p) {",
            "    print(1);",
            "  }",
            "  Test(double p) {",
            "    print(2);",
            "  }",
            "  static void main() {",
            "    new Test();",
            "    new Test(2);",
            "    new Test(3.0);",
            "  }",
            "}",
            ""));
    Context context = new Context();
    context.addSourceFolder(tmpFolder);
    context.addSourceFiles(tmpFolder);
    CompilationUnit unit = context.translate();
    assertEquals(
        toString(
            "class Test {",
            "  Test() {",
            "    _jtd_constructor_0_impl();",
            "  }",
            "  _jtd_constructor_0_impl() {",
            "    print(0);",
            "  }",
            "  Test.con1(int p) {",
            "    _jtd_constructor_1_impl(p);",
            "  }",
            "  _jtd_constructor_1_impl(int p) {",
            "    print(1);",
            "  }",
            "  Test.con2(double p) {",
            "    _jtd_constructor_2_impl(p);",
            "  }",
            "  _jtd_constructor_2_impl(double p) {",
            "    print(2);",
            "  }",
            "  static void main() {",
            "    new Test();",
            "    new Test.con1(2);",
            "    new Test.con2(3.0);",
            "  }",
            "}"),
        getFormattedSource(unit));
  }

  public void test_ensureFieldInitializer() throws Exception {
    setFileLines(
        "test/Test.java",
        toString(
            "// filler filler filler filler filler filler filler filler filler filler",
            "package test;",
            "public class Test {",
            "  boolean booleanF;",
            "  byte byteF;",
            "  char charF;",
            "  short shortF;",
            "  int intF;",
            "  long longF;",
            "  float floatF;",
            "  double doubleF;",
            "}",
            ""));
    Context context = new Context();
    context.addSourceFolder(tmpFolder);
    context.addSourceFiles(tmpFolder);
    CompilationUnit unit = context.translate();
    assertEquals(
        toString(
            "class Test {",
            "  bool booleanF = false;",
            "  int byteF = 0;",
            "  int charF = 0;",
            "  int shortF = 0;",
            "  int intF = 0;",
            "  int longF = 0;",
            "  double floatF = 0.0;",
            "  double doubleF = 0.0;",
            "}"),
        getFormattedSource(unit));
  }

  // TODO(scheglov) does not work yet
  public void test_enum_constantWithSubclass() throws Exception {
    setFileLines(
        "test/Test.java",
        toString(
            "// filler filler filler filler filler filler filler filler filler filler",
            "package test;",
            "public enum Test {",
            "  EOF(5) {",
            "    void foo() {",
            "      print(2);",
            "    }",
            "  };",
            "  private Test() {",
            "  }",
            "  private Test(int p) {",
            "  }",
            "  void foo() {",
            "    print(1);",
            "  }",
            "}"));
    Context context = new Context();
    context.addSourceFolder(tmpFolder);
    context.addSourceFiles(tmpFolder);
    CompilationUnit unit = context.translate();
    printFormattedSource(unit);
    assertEquals(
        toString(
            "class Test {",
            "  static final Test ONE = new Test();",
            "  static final Test TWO = new Test.withPriority(2);",
            "  Test() {",
            "    _jtd_constructor_0_impl();",
            "  }",
            "  _jtd_constructor_0_impl() {",
            "  }",
            "  Test.withPriority(int p) {",
            "    _jtd_constructor_1_impl(p);",
            "  }",
            "  _jtd_constructor_1_impl(int p) {",
            "  }",
            "}"),
        getFormattedSource(unit));
  }

  public void test_enum_twoConstructors() throws Exception {
    setFileLines(
        "test/Test.java",
        toString(
            "// filler filler filler filler filler filler filler filler filler filler",
            "package test;",
            "public enum Test {",
            "  ONE(), TWO(2);",
            "  private Test() {",
            "  }",
            "  private Test(int p) {",
            "  }",
            "}"));
    Context context = new Context();
    context.addSourceFolder(tmpFolder);
    context.addSourceFiles(tmpFolder);
    context.addRename("Ltest/Test;.(Ljava/lang/String;II)", "withPriority");
    CompilationUnit unit = context.translate();
    assertEquals(
        toString(
            "class Test {",
            "  static final Test ONE = new Test();",
            "  static final Test TWO = new Test.withPriority(2);",
            "  Test() {",
            "    _jtd_constructor_0_impl();",
            "  }",
            "  _jtd_constructor_0_impl() {",
            "  }",
            "  Test.withPriority(int p) {",
            "    _jtd_constructor_1_impl(p);",
            "  }",
            "  _jtd_constructor_1_impl(int p) {",
            "  }",
            "}"),
        getFormattedSource(unit));
  }

  public void test_giveUniqueName_methodField() throws Exception {
    setFileLines(
        "test/Test.java",
        toString(
            "// filler filler filler filler filler filler filler filler filler filler",
            "package test;",
            "public class Test {",
            "  private int value;",
            "  public int value() {",
            "    return value;",
            "  }",
            "  public void bar() {",
            "    value();",
            "  }",
            "}",
            ""));
    Context context = new Context();
    context.addSourceFolder(tmpFolder);
    context.addSourceFiles(tmpFolder);
    CompilationUnit unit = context.translate();
    assertEquals(
        toString(
            "class Test {",
            "  int value2 = 0;",
            "  int value() {",
            "    return value2;",
            "  }",
            "  void bar() {",
            "    value();",
            "  }",
            "}"),
        getFormattedSource(unit));
  }

  public void test_giveUniqueName_methods() throws Exception {
    setFileLines(
        "test/Test.java",
        toString(
            "// filler filler filler filler filler filler filler filler filler filler",
            "package test;",
            "public class Test {",
            "  static void foo() {}",
            "  static void foo(int p) {}",
            "  static void foo(double p) {}",
            "  static void bar() {",
            "    foo(42);",
            "  }",
            "}",
            ""));
    Context context = new Context();
    context.addSourceFolder(tmpFolder);
    context.addSourceFiles(tmpFolder);
    CompilationUnit unit = context.translate();
    assertEquals(
        toString(
            "class Test {",
            "  static void foo() {",
            "  }",
            "  static void foo2(int p) {",
            "  }",
            "  static void foo3(double p) {",
            "  }",
            "  static void bar() {",
            "    foo2(42);",
            "  }",
            "}"),
        getFormattedSource(unit));
  }

  public void test_giveUniqueName_variableInitializer() throws Exception {
    File file = setFileLines(
        "test/Test.java",
        toString(
            "// filler filler filler filler filler filler filler filler filler filler",
            "package test;",
            "public class Test {",
            "  static int foo() {return 42;}",
            "  static void bar() {",
            "    int foo = foo();",
            "    baz(foo);",
            "  }",
            "  static void baz(int p) {}",
            "}",
            ""));
    Context context = new Context();
    context.addSourceFolder(tmpFolder);
    context.addSourceFile(file);
    CompilationUnit unit = context.translate();
    assertEquals(
        toString(
            "class Test {",
            "  static int foo() {",
            "    return 42;",
            "  }",
            "  static void bar() {",
            "    int foo2 = foo();",
            "    baz(foo2);",
            "  }",
            "  static void baz(int p) {",
            "  }",
            "}"),
        getFormattedSource(unit));
  }

  public void test_importStatic() throws Exception {
    setFileLines(
        "test/A.java",
        toString(
            "// filler filler filler filler filler filler filler filler filler filler",
            "package test;",
            "public class A {",
            "  public static final int ZERO;",
            "}",
            ""));
    setFileLines(
        "test/B.java",
        toString(
            "// filler filler filler filler filler filler filler filler filler filler",
            "package test;",
            "import static test.A.ZERO;",
            "public class B {",
            "  int myInstanceField;",
            "  static int myStaticField;",
            "  void main() {",
            "    print(A.ZERO);",
            "    print(ZERO);",
            "    this.myInstanceField = 1;",
            "    myInstanceField = 2;",
            "    myInstanceField = ZERO;",
            "    myInstanceField = ZERO + 1;",
            "    myInstanceField = 2 + ZERO;",
            "    myInstanceField = 1 + 2 + 3 + ZERO;",
            "    myStaticField = 3;",
            "  }",
            "}",
            ""));
    Context context = new Context();
    context.addSourceFolder(tmpFolder);
    context.addSourceFiles(tmpFolder);
    CompilationUnit unit = context.translate();
    printFormattedSource(unit);
    assertEquals(
        toString(
            "class A {",
            "  static int ZERO = 0;",
            "}",
            "class B {",
            "  int myInstanceField = 0;",
            "  static int myStaticField = 0;",
            "  void main() {",
            "    print(A.ZERO);",
            "    print(A.ZERO);",
            "    this.myInstanceField = 1;",
            "    myInstanceField = 2;",
            "    myInstanceField = A.ZERO;",
            "    myInstanceField = A.ZERO + 1;",
            "    myInstanceField = 2 + A.ZERO;",
            "    myInstanceField = 1 + 2 + 3 + A.ZERO;",
            "    myStaticField = 3;",
            "  }",
            "}"),
        getFormattedSource(unit));
  }

  public void test_redirectingConstructorInvocation() throws Exception {
    setFileLines(
        "test/Test.java",
        toString(
            "// filler filler filler filler filler filler filler filler filler filler",
            "package test;",
            "public class Test {",
            "  public Test() {",
            "    this(42);",
            "  }",
            "  public Test(int p) {",
            "  }",
            "}",
            ""));
    Context context = new Context();
    context.addSourceFolder(tmpFolder);
    context.addSourceFiles(tmpFolder);
    CompilationUnit unit = context.translate();
    assertEquals(
        toString(
            "class Test {",
            "  Test() {",
            "    _jtd_constructor_0_impl();",
            "  }",
            "  _jtd_constructor_0_impl() {",
            "    _jtd_constructor_1_impl(42);",
            "  }",
            "  Test.con1(int p) {",
            "    _jtd_constructor_1_impl(p);",
            "  }",
            "  _jtd_constructor_1_impl(int p) {",
            "  }",
            "}"),
        getFormattedSource(unit));
  }

  public void test_superConstructorInvocation() throws Exception {
    setFileLines(
        "test/A.java",
        toString(
            "// filler filler filler filler filler filler filler filler filler filler",
            "package test;",
            "public class A {",
            "  A(int p) {}",
            "  A(double p) {}",
            "}",
            ""));
    setFileLines(
        "test/B.java",
        toString(
            "// filler filler filler filler filler filler filler filler filler filler",
            "package test;",
            "public class B extends A {",
            "  B() {",
            "    super(1.0);",
            "    print(2);",
            "  }",
            "}",
            ""));
    Context context = new Context();
    context.addSourceFolder(tmpFolder);
    context.addSourceFiles(tmpFolder);
    CompilationUnit unit = context.translate();
    assertEquals(
        toString(
            "class A {",
            "  A.con1(int p) {",
            "    _jtd_constructor_0_impl(p);",
            "  }",
            "  _jtd_constructor_0_impl(int p) {",
            "  }",
            "  A.con2(double p) {",
            "    _jtd_constructor_1_impl(p);",
            "  }",
            "  _jtd_constructor_1_impl(double p) {",
            "  }",
            "}",
            "class B extends A {",
            "  B() : super.con2(1.0) {",
            "    _jtd_constructor_2_impl();",
            "  }",
            "  _jtd_constructor_2_impl() {",
            "    print(2);",
            "  }",
            "}"),
        getFormattedSource(unit));
  }

  public void test_superMethodInvocation() throws Exception {
    setFileLines(
        "test/A.java",
        toString(
            "// filler filler filler filler filler filler filler filler filler filler",
            "package test;",
            "public class A {",
            "  void test(int p) {}",
            "}",
            ""));
    setFileLines(
        "test/B.java",
        toString(
            "// filler filler filler filler filler filler filler filler filler filler",
            "package test;",
            "public class B extends A {",
            "  void test() {",
            "    print(1);",
            "    super.test(2);",
            "    print(3);",
            "  }",
            "}",
            ""));
    Context context = new Context();
    context.addSourceFolder(tmpFolder);
    context.addSourceFiles(tmpFolder);
    CompilationUnit unit = context.translate();
    assertEquals(
        toString(
            "class A {",
            "  void test(int p) {",
            "  }",
            "}",
            "class B extends A {",
            "  void test() {",
            "    print(1);",
            "    super.test(2);",
            "    print(3);",
            "  }",
            "}"),
        getFormattedSource(unit));
  }

  public void test_varArgs() throws Exception {
    setFileLines(
        "test/Test.java",
        toString(
            "// filler filler filler filler filler filler filler filler filler filler",
            "package test;",
            "public class A {",
            "  void test(int errorCode, Object ...args) {",
            "  }",
            "  void main() {",
            "    test(-1);",
            "    test(-1, 2, 3.0);",
            "  }",
            "}"));
    Context context = new Context();
    context.addSourceFolder(tmpFolder);
    context.addSourceFiles(tmpFolder);
    CompilationUnit unit = context.translate();
    assertEquals(
        toString(
            "class A {",
            "  void test(int errorCode, List<Object> args) {",
            "  }",
            "  void main() {",
            "    test(-1, []);",
            "    test(-1, [2, 3.0]);",
            "  }",
            "}"),
        getFormattedSource(unit));
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    tmpFolder = Files.createTempDir();
  }

  @Override
  protected void tearDown() throws Exception {
    FileUtils.deleteDirectory(tmpFolder);
    super.tearDown();
  }

  /**
   * Sets the content of the file with given path relative to {@link #tmpFolder}.
   */
  private File setFileLines(String path, String content) throws Exception {
    File toFile = new File(tmpFolder, path);
    Files.createParentDirs(toFile);
    Files.write(content, toFile, Charsets.UTF_8);
    return toFile;
  }
}