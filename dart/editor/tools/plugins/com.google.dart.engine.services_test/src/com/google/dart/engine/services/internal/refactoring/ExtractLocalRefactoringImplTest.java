/*
 * Copyright (c) 2013, the Dart project authors.
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
package com.google.dart.engine.services.internal.refactoring;

import com.google.dart.engine.services.assist.AssistContext;
import com.google.dart.engine.services.change.Change;
import com.google.dart.engine.services.refactoring.ExtractLocalRefactoring;
import com.google.dart.engine.services.status.RefactoringStatus;
import com.google.dart.engine.services.status.RefactoringStatusSeverity;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Test for {@link ExtractLocalRefactoringImpl}.
 */
public class ExtractLocalRefactoringImplTest extends RefactoringImplTest {
  private ExtractLocalRefactoringImpl refactoring;
  private int selectionStart = -1;
  private int selectionEnd = -1;
  private String localName = "res";
  private boolean replaceAllOccurences = true;
  private RefactoringStatus refactoringStatus;

  public void test_bad_notPartOfFunction() throws Exception {
    parseTestUnit(
        "// filler filler filler filler filler filler filler filler filler filler",
        "int a = 1 + 2;",
        "");
    // create refactoring
    selectionStart = findOffset("1 + 2");
    selectionEnd = findOffset(";");
    createRefactoring();
    // check conditions
    assertRefactoringStatus(
        refactoringStatus,
        RefactoringStatusSeverity.FATAL,
        "Expression inside of function must be selected to activate this refactoring.");
  }

  public void test_bad_sameVariable_after() throws Exception {
    parseTestUnit(
        "// filler filler filler filler filler filler filler filler filler filler",
        "main() {",
        "  int a = 1 + 2;",
        "  var res;",
        "}");
    // create refactoring
    setSelectionString("1 + 2");
    createRefactoring();
    // TODO(scheglov) Does not work now because no "visible range" set in resolver
//    // conflicting name
//    {
//      refactoring.setLocalName("res");
//      refactoringStatus = refactoring.checkAllConditions(pm);
//      assert_warning_alreadyDefined();
//    }
//    // unique name
//    {
//      refactoring.setLocalName("uniqueName");
//      refactoringStatus = refactoring.checkAllConditions(pm);
//      assertRefactoringStatusOK(refactoringStatus);
//    }
  }

  public void test_bad_sameVariable_before() throws Exception {
    parseTestUnit(
        "// filler filler filler filler filler filler filler filler filler filler",
        "main() {",
        "  var res;",
        "  int a = 1 + 2;",
        "}");
    // create refactoring
    setSelectionString("1 + 2");
    createRefactoring();
    // TODO(scheglov) Does not work now because no "visible range" set in resolver
//    // conflicting name
//    {
//      refactoring.setLocalName("res");
//      refactoringStatus = refactoring.checkAllConditions(pm);
//      assert_warning_alreadyDefined();
//    }
//    // unique name
//    {
//      refactoring.setLocalName("uniqueName");
//      refactoringStatus = refactoring.checkAllConditions(pm);
//      assertRefactoringStatusOK(refactoringStatus);
//    }
  }

  public void test_fragmentExpression_leadingNotWhitespace() throws Exception {
    parseTestUnit(
        "// filler filler filler filler filler filler filler filler filler filler",
        "main() {",
        "  int a = 1 + 2 + 3 + 4;",
        "}");
    // create refactoring
    setSelectionString("+ 2");
    createRefactoring();
    // check conditions
    assert_fatalError_selection();
  }

  public void test_fragmentExpression_leadingPartialSelection() throws Exception {
    parseTestUnit(
        "// filler filler filler filler filler filler filler filler filler filler",
        "main() {",
        "  int a = 111 + 2 + 3 + 4;",
        "}");
    // create refactoring
    setSelectionString("11 + 2");
    createRefactoring();
    // check conditions
    assert_fatalError_selection();
  }

  public void test_fragmentExpression_leadingWhitespace() throws Exception {
    parseTestUnit(
        "// filler filler filler filler filler filler filler filler filler filler",
        "main() {",
        "  int a = 1 + 2 + 3 + 4;",
        "}");
    // create refactoring
    setSelectionString(" 2 + 3");
    createRefactoring();
    // apply refactoring
    assertSuccessfulRefactoring(
        "// filler filler filler filler filler filler filler filler filler filler",
        "main() {",
        "  var res =  2 + 3;",
        "  int a = 1 +res + 4;",
        "}");
  }

  public void test_fragmentExpression_notAssociativeOperator() throws Exception {
    parseTestUnit(
        "// filler filler filler filler filler filler filler filler filler filler",
        "main() {",
        "  int a = 1 - 2 - 3 - 4;",
        "}");
    // create refactoring
    setSelectionString("2 - 3");
    createRefactoring();
    // check conditions
    assert_fatalError_selection();
  }

  public void test_fragmentExpression_OK() throws Exception {
    parseTestUnit(
        "// filler filler filler filler filler filler filler filler filler filler",
        "main() {",
        "  int a = 1 + 2 + 3 + 4;",
        "}");
    // create refactoring
    setSelectionString("2 + 3");
    createRefactoring();
    // apply refactoring
    assertSuccessfulRefactoring(
        "// filler filler filler filler filler filler filler filler filler filler",
        "main() {",
        "  var res = 2 + 3;",
        "  int a = 1 + res + 4;",
        "}");
  }

  public void test_fragmentExpression_trailingNotWhitespace() throws Exception {
    parseTestUnit(
        "// filler filler filler filler filler filler filler filler filler filler",
        "main() {",
        "  int a = 1 + 2 + 3 + 4;",
        "}");
    // create refactoring
    setSelectionString("2 + 3 +");
    createRefactoring();
    // check conditions
    assert_fatalError_selection();
  }

  public void test_fragmentExpression_trailingPartialSelection() throws Exception {
    parseTestUnit(
        "// filler filler filler filler filler filler filler filler filler filler",
        "main() {",
        "  int a = 1 + 2 + 3 + 444;",
        "}");
    // create refactoring
    setSelectionString("2 + 3 + 44");
    createRefactoring();
    // check conditions
    assert_fatalError_selection();
  }

  public void test_fragmentExpression_trailingWhitespace() throws Exception {
    parseTestUnit(
        "// filler filler filler filler filler filler filler filler filler filler",
        "main() {",
        "  int a = 1 + 2 + 3 + 4;",
        "}");
    // create refactoring
    setSelectionString("2 + 3 ");
    createRefactoring();
    // apply refactoring
    assertSuccessfulRefactoring(
        "// filler filler filler filler filler filler filler filler filler filler",
        "main() {",
        "  var res = 2 + 3 ;",
        "  int a = 1 + res+ 4;",
        "}");
  }

  public void test_getRefactoringName() throws Exception {
    parseTestUnit(
        "// filler filler filler filler filler filler filler filler filler filler",
        "main() {",
        "  print(1 + 2);",
        "}");
    // create refactoring
    setSelectionString("1 + 2");
    createRefactoring();
    // access
    assertEquals("Extract Local Variable", refactoring.getRefactoringName());
  }

  public void test_guessNames_fragmentExpression() throws Exception {
    parseTestUnit(
        "// filler filler filler filler filler filler filler filler filler filler",
        "class TreeItem {}",
        "TreeItem getSelectedItem() => null;",
        "process(arg) {}",
        "main() {",
        "  process(111 + 222 + 333 + 444); // marker",
        "}");
    // create refactoring
    setSelectionString("222 + 333");
    createRefactoring();
    // no guesses
    String[] names = refactoring.guessNames();
    assertThat(names).isEmpty();
  }

  public void test_guessNames_singleExpression() throws Exception {
    parseTestUnit(
        "// filler filler filler filler filler filler filler filler filler filler",
        "class TreeItem {}",
        "TreeItem getSelectedItem() => null;",
        "process(arg) {}",
        "main() {",
        "  process(getSelectedItem()); // marker",
        "}");
    // create refactoring
    selectionStart = findOffset("getSelectedItem()); // marker");
    selectionEnd = findOffset("); // marker");
    createRefactoring();
    // check guesses
    // TODO(scheglov) implement and test
//    String[] names = refactoring.guessNames();
//    assertThat(names).contains("selectedItem", "item", "arg", "treeItem");
  }

  public void test_guessNames_stringPart() throws Exception {
    parseTestUnit(
        "// filler filler filler filler filler filler filler filler filler filler",
        "f() {",
        "  var s = 'Hello Bob... welcome to Dart!';",
        "}");
    // create refactoring
    setSelectionString("Hello Bob");
    createRefactoring();
    // check guesses
    // TODO(scheglov) implement and test
//    String[] names = refactoring.guessNames();
//    assertThat(names).contains("helloBob", "bob");
  }

  public void test_occurences_disableOccurences() throws Exception {
    parseTestUnit(
        "// filler filler filler filler filler filler filler filler filler filler",
        "int foo() => 42;",
        "main() {",
        "  int a = 1 + foo();",
        "  int b = 2 +  foo(); // marker",
        "}");
    // create refactoring
    selectionStart = findOffset("  foo();") + 2;
    selectionEnd = findOffset("; // marker");
    replaceAllOccurences = false;
    createRefactoring();
    // apply refactoring
    assertSuccessfulRefactoring(
        "// filler filler filler filler filler filler filler filler filler filler",
        "int foo() => 42;",
        "main() {",
        "  int a = 1 + foo();",
        "  var res = foo();",
        "  int b = 2 +  res; // marker",
        "}");
  }

  public void test_occurences_fragmentExpression() throws Exception {
    parseTestUnit(
        "// filler filler filler filler filler filler filler filler filler filler",
        "int foo() => 42;",
        "main() {",
        "  int a = 11 + 2 + foo() + 3;",
        "  int b = 12 +  2 + foo() + 3; // marker",
        "}");
    // create refactoring
    selectionStart = findOffset("  2 +") + 2;
    selectionEnd = findOffset("; // marker");
    createRefactoring();
    // apply refactoring
    assertSuccessfulRefactoring(
        "// filler filler filler filler filler filler filler filler filler filler",
        "int foo() => 42;",
        "main() {",
        "  var res = 2 + foo() + 3;",
        "  int a = 11 + res;",
        "  int b = 12 +  res; // marker",
        "}");
  }

  public void test_occurences_singleExpression() throws Exception {
    parseTestUnit(
        "// filler filler filler filler filler filler filler filler filler filler",
        "int foo() => 42;",
        "main() {",
        "  int a = 1 + foo();",
        "  int b = 2 +  foo(); // marker",
        "}");
    // create refactoring
    selectionStart = findOffset("  foo();") + 2;
    selectionEnd = findOffset("; // marker");
    createRefactoring();
    // apply refactoring
    assertSuccessfulRefactoring(
        "// filler filler filler filler filler filler filler filler filler filler",
        "int foo() => 42;",
        "main() {",
        "  var res = foo();",
        "  int a = 1 + res;",
        "  int b = 2 +  res; // marker",
        "}");
  }

  public void test_occurences_useDominator() throws Exception {
    parseTestUnit(
        "// filler filler filler filler filler filler filler filler filler filler",
        "main() {",
        "  if (true) {",
        "    print(42);",
        "  } else {",
        "    print(42);",
        "  }",
        "}");
    // create refactoring
    selectionStart = findOffset("42");
    selectionEnd = findOffset("42);") + "42".length();
    createRefactoring();
    // apply refactoring
    assertSuccessfulRefactoring(
        "// filler filler filler filler filler filler filler filler filler filler",
        "main() {",
        "  var res = 42;",
        "  if (true) {",
        "    print(res);",
        "  } else {",
        "    print(res);",
        "  }",
        "}");
  }

  public void test_occurences_whenComment() throws Exception {
    parseTestUnit(
        "// filler filler filler filler filler filler filler filler filler filler",
        "int foo() => 42;",
        "main() {",
        "  /*int a = 1 + foo();*/",
        "  int b = 2 +  foo(); // marker",
        "}");
    // create refactoring
    selectionStart = findOffset("  foo();") + 2;
    selectionEnd = findOffset("; // marker");
    createRefactoring();
    // apply refactoring
    assertSuccessfulRefactoring(
        "// filler filler filler filler filler filler filler filler filler filler",
        "int foo() => 42;",
        "main() {",
        "  /*int a = 1 + foo();*/",
        "  var res = foo();",
        "  int b = 2 +  res; // marker",
        "}");
  }

  public void test_occurences_whenSpace() throws Exception {
    parseTestUnit(
        "// filler filler filler filler filler filler filler filler filler filler",
        "int foo(String s) => 42;",
        "main() {",
        "  int a = 1 + foo('has space');",
        "  int b = 2 +  foo('has space'); // marker",
        "}");
    // create refactoring
    selectionStart = findOffset("  foo('has space');") + 2;
    selectionEnd = findOffset("; // marker");
    createRefactoring();
    // apply refactoring
    assertSuccessfulRefactoring(
        "// filler filler filler filler filler filler filler filler filler filler",
        "int foo(String s) => 42;",
        "main() {",
        "  var res = foo('has space');",
        "  int a = 1 + res;",
        "  int b = 2 +  res; // marker",
        "}");
  }

  public void test_singleExpression() throws Exception {
    parseTestUnit(
        "// filler filler filler filler filler filler filler filler filler filler",
        "main() {",
        "  print(1 + 2);",
        "}");
    // create refactoring
    setSelectionString("1 + 2");
    createRefactoring();
    // apply refactoring
    assertSuccessfulRefactoring(
        "// filler filler filler filler filler filler filler filler filler filler",
        "main() {",
        "  var res = 1 + 2;",
        "  print(res);",
        "}");
  }

  public void test_singleExpression_getter() throws Exception {
    parseTestUnit(
        "// filler filler filler filler filler filler filler filler filler filler",
        "class A {",
        "  int get foo => 42;",
        "}",
        "main() {",
        "  A a = new A();",
        "  int b = 1 + a.foo; // marker",
        "}");
    // create refactoring
    selectionStart = findOffset("a.foo;");
    selectionEnd = findOffset("; // marker");
    createRefactoring();
    // apply refactoring
    assertSuccessfulRefactoring(
        "// filler filler filler filler filler filler filler filler filler filler",
        "class A {",
        "  int get foo => 42;",
        "}",
        "main() {",
        "  A a = new A();",
        "  var res = a.foo;",
        "  int b = 1 + res; // marker",
        "}");
  }

  public void test_singleExpression_leadingNotWhitespace() throws Exception {
    parseTestUnit(
        "// filler filler filler filler filler filler filler filler filler filler",
        "main() {",
        "  int a = 12 + 345; // marker",
        "}");
    // create refactoring
    setSelectionString("+ 345");
    createRefactoring();
    // check conditions
    assert_fatalError_selection();
  }

  public void test_singleExpression_leadingWhitespace() throws Exception {
    parseTestUnit(
        "// filler filler filler filler filler filler filler filler filler filler",
        "main() {",
        "  int a = 1 + 2; // marker",
        "}");
    // create refactoring
    setSelectionString(" 1 + 2");
    createRefactoring();
    // apply refactoring
    assertSuccessfulRefactoring(
        "// filler filler filler filler filler filler filler filler filler filler",
        "main() {",
        "  var res =  1 + 2;",
        "  int a =res; // marker",
        "}");
  }

  /**
   * We use here knowledge how exactly <code>1 + 2 + 3 + 4</code> is parsed. We know that
   * <code>1 + 2</code> will be separate and complete {@link DartBinaryExpression}, so can be
   * handled as single expression.
   */
  public void test_singleExpression_partOfBinaryExpression() throws Exception {
    parseTestUnit(
        "// filler filler filler filler filler filler filler filler filler filler",
        "main() {",
        "  int a = 1 + 2 + 3 + 4;",
        "}");
    // create refactoring
    setSelectionString("1 + 2");
    createRefactoring();
    // apply refactoring
    assertSuccessfulRefactoring(
        "// filler filler filler filler filler filler filler filler filler filler",
        "main() {",
        "  var res = 1 + 2;",
        "  int a = res + 3 + 4;",
        "}");
  }

  public void test_singleExpression_trailingComment() throws Exception {
    parseTestUnit(
        "// filler filler filler filler filler filler filler filler filler filler",
        "main() {",
        "  int a = 12 /*abc*/ + 345;",
        "}");
    // create refactoring
    setSelectionString("12 /*abc*/");
    createRefactoring();
    // apply refactoring
    assertSuccessfulRefactoring(
        "// filler filler filler filler filler filler filler filler filler filler",
        "main() {",
        "  var res = 12 /*abc*/;",
        "  int a = res + 345;",
        "}");
  }

  public void test_singleExpression_trailingNotWhitespace() throws Exception {
    parseTestUnit(
        "// filler filler filler filler filler filler filler filler filler filler",
        "main() {",
        "  int a = 12 + 345; // marker",
        "}");
    // create refactoring
    setSelectionString("12 +");
    createRefactoring();
    // check conditions
    assert_fatalError_selection();
  }

  public void test_singleExpression_trailingWhitespace() throws Exception {
    parseTestUnit(
        "// filler filler filler filler filler filler filler filler filler filler",
        "main() {",
        "  int a = 1 + 2 ; // marker",
        "}");
    // create refactoring
    setSelectionString("1 + 2 ");
    createRefactoring();
    // failed
    assert_fatalError_selection();
  }

  public void test_stringLiteral() throws Exception {
    parseTestUnit(
        "// filler filler filler filler filler filler filler filler filler filler",
        "main() {",
        "  print('abcdefgh');",
        "}");
    // create refactoring
    setSelectionString("cde");
    createRefactoring();
    // apply refactoring
    assertSuccessfulRefactoring(
        "// filler filler filler filler filler filler filler filler filler filler",
        "main() {",
        "  var res = 'cde';",
        "  print('ab${res}fgh');",
        "}");
  }

  /**
   * Checks that all conditions are <code>OK</code> and applying {@link Change} to the
   * {@link #testUnit} is same source as given lines.
   */
  protected final void assertSuccessfulRefactoring(String... lines) throws Exception {
    assertRefactoringStatus(refactoringStatus, RefactoringStatusSeverity.OK, null);
    Change change = refactoring.createChange(pm);
    assertChangeResult(change, makeSource(lines));
  }

  /**
   * Asserts that {@link refactoringStatus} has fatal error caused by selection.
   */
  private void assert_fatalError_selection() throws Exception {
    RefactoringStatus status = refactoring.checkInitialConditions(pm);
    assertRefactoringStatus(
        status,
        RefactoringStatusSeverity.FATAL,
        "Expression must be selected to activate this refactoring.");
  }

  private void assert_warning_alreadyDefined() {
    assertRefactoringStatus(
        refactoringStatus,
        RefactoringStatusSeverity.WARNING,
        "A variable with name 'res' is already defined in the visible scope.");
  }

  /**
   * Creates {@link ExtractLocalRefactoring} in {@link #refactoring}.
   */
  private void createRefactoring() throws Exception {
    int selectionLength = selectionEnd - selectionStart;
    AssistContext context = new AssistContext(testUnit, selectionStart, selectionLength);
    refactoring = new ExtractLocalRefactoringImpl(context);
    refactoring.setLocalName(localName);
    refactoring.setReplaceAllOccurrences(replaceAllOccurences);
    refactoringStatus = refactoring.checkAllConditions(pm);
  }

  /**
   * Prints result of {@link #refactoring} in the way ready to parse into test expectations.
   */
  @SuppressWarnings("unused")
  private void printRefactoringResultSource() throws Exception {
    Change change = refactoring.createChange(pm);
    String changedCode = getTestSourceChangeResult(change);
    printSourceLines(changedCode);
  }

  /**
   * Sets selection to the start of the first occurrence of the given string.
   */
  private void setSelectionString(String pattern) {
    selectionStart = findOffset(pattern);
    selectionEnd = findEnd(pattern);
  }
}