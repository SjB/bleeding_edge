// Copyright (c) 2011, the Dart project authors.  Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

import 'mock_compiler.dart';
import '../../../sdk/lib/_internal/compiler/implementation/js/js.dart' as jsAst;
import '../../../sdk/lib/_internal/compiler/implementation/js/js.dart' show js;

void testExpression(String expression, [String expect = ""]) {
  jsAst.Node node = js[expression];
  MockCompiler compiler = new MockCompiler();
  String jsText =
      jsAst.prettyPrint(node,
                        compiler,
                        allowVariableMinification: false).getText();
  if (expect == "") {
    Expect.stringEquals(expression, jsText);
  } else {
    Expect.stringEquals(expect, jsText);
  }
}

void testError(String expression, [String expect = ""]) {
  bool doCheck(exception) {
    Expect.isTrue(exception.toString().contains(expect));
    return true;
  }
  Expect.throws(() => js[expression], doCheck);
}
    


void main() {
  // Asterisk indicates deviations from real JS.
  // Simple var test.
  testExpression('var a = ""');
  // Parse and print will normalize whitespace.
  testExpression(' var  a  =  "" ', 'var a = ""');
  // *We don't do operator prescedence.
  testError('x = a + b * c', 'Mixed + and *');
  // But we can chain binary operators (with left associativity).
  testExpression('x = a + b + c');
  // We can cope with relational operators and non-relational.
  testExpression('a + b == c + d');
  // The prettyprinter will insert braces where needed.
  testExpression('a + (b == c) + d');
  // We can handle () for calls.
  testExpression('foo(bar)');
  testExpression('foo(bar, baz)');
  // *But we can't handle chained calls without parentheses.
  testError('foo(bar)(baz)');
  // The prettyprinter understands chained calls without extra parentheses.
  testExpression('(foo(bar))(baz)', 'foo(bar)(baz)');
  // Chains of dotting and calls.
  testExpression('foo.bar(baz)');
  // String literal.
  testExpression('var x = "fisk"');
  // String literal with \n.
  testExpression(r'var x = "\n"');
  // String literal with escaped quote.
  testExpression(r'var x = "\""');
  // *No clever escapes.
  testError(r'var x = "\x42"', 'escapes allowed in string literals');
  // Operator new.
  testExpression('new Foo()');
  // New with dotted access.
  testExpression('new Frobinator.frobinate()');
  // The prettyprinter is smarter than we are.
  testExpression('(new Frobinator()).frobinate()',
                 'new Frobinator().frobinate()');
  // *We want a bracket on 'new'.
  testError('new Foo');
  testError('(new Foo)');
  // Bogus operators.
  testError('a +++ b', 'Unknown operator');
  // This isn't perl.  There are rules.
  testError('a <=> b', 'Unknown operator');
  // Typeof.
  testExpression('typeof foo == "number"');
  // *Strange relation.
  testError('a < b < c', 'RELATION');
  // Chained var.
  testExpression('var x = 0, y = 1.2, z = 42');
  // Empty object literal.
  testExpression('foo({}, {})');
  // *Can't handle non-empty object literals
  testError('foo({meaning: 42})', 'Expected RBRACE');
  // Literals.
  testExpression('x(false, true, null)');
  // *We should really throw here.
  testExpression('var false = 42');
  testExpression('var new = 42');
  testExpression('var typeof = 42');
  // Malformed decimal
  testError('var x = 42.', "Unparseable number");
  testError('var x = 1.1.1', "Unparseable number");
  // More unary.
  testExpression('x = !x');
  testExpression('!x == false');
  testExpression('var foo = void 0');
  testExpression('delete foo.bar');
  testExpression('delete foo');
  testError('x typeof y', 'Unparsed junk');
  testExpression('x &= ~mask');
  // Adjacent tokens.
  testExpression('foo[x[bar]]');
  // *We don't do array literals.
  testError('beebop([1, 2, 3])');
  // Prefix ++ etc.
  testExpression("++x");
  testExpression("++foo.bar");
  testExpression("+x");
  testExpression("+foo.bar");
  testExpression("-x");
  testExpression("-foo.bar");
  testExpression("--x");
  testExpression("--foo.bar");
  // Postfix ++ etc.
  testExpression("x++");
  testExpression("foo.bar++");
  testExpression("x--");
  testExpression("foo.bar--");
  // Both!
  testExpression("++x++");
  testExpression("++foo.bar++");
  testExpression("--x--");
  testExpression("--foo.bar--");
  // *We can't handle stacked unary operators.
  testError("x++ ++");
  testError("++ typeof x");
  // ++ used as a binary operator.
  testError("x++ ++ 42");
  // Shift operators.
  testExpression("x << 5");
  testExpression("x << y + 1");
  testExpression("x <<= y + 1");
}