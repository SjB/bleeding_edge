package com.google.dart.engine.services.completion;

import com.google.dart.engine.ast.ASTNode;

/**
 * @coverage com.google.dart.engine.services.completion
 */
class CompletionState {
  boolean isForMixin;
  boolean isVoidAllowed;
  boolean isDynamicAllowed;
  boolean isSourceDeclarationStatic;
  boolean isVarAllowed;
  boolean areLiteralsAllowed;
  boolean areLiteralsProhibited;
  boolean areOperatorsAllowed;

  void includesLiterals() {
    if (!areLiteralsProhibited) {
      areLiteralsAllowed = true;
    }
  }

  void includesOperators() {
    areOperatorsAllowed = true;
  }

  void includesUndefinedDeclarationTypes() {
    isVoidAllowed = true;
    isDynamicAllowed = true;
  }

  void includesUndefinedTypes() {
    isVarAllowed = true;
    isDynamicAllowed = true;
  }

  void mustBeMixin() {
    isForMixin = true;
  }

  void prohibitsLiterals() {
    areLiteralsAllowed = false;
    areLiteralsProhibited = true;
  }

  void setContext(ASTNode base) {
    base.accept(new ContextAnalyzer(this, base));
  }

  void sourceDeclarationIsStatic(boolean state) {
    isSourceDeclarationStatic = state;
  }
}
