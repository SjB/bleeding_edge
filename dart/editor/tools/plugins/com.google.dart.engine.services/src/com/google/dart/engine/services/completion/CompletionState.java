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
  boolean areLiteralsAllowed;
  boolean areLiteralsProhibited;

  void includesLiterals() {
    if (!areLiteralsProhibited) {
      areLiteralsAllowed = true;
    }
  }

  void includesUndefinedTypes() {
    isVoidAllowed = true;
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