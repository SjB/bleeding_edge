// Copyright (c) 2013, the Dart project authors.  Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

library test_utils;

import 'dart:async';

import '../lib/src/utils.dart';
import '../lib/src/mock_clock.dart' as mock_clock;

export '../lib/src/utils.dart';

/// Wraps [input] to provide a timeout. If [input] completes before
/// [milliseconds] have passed, then the return value completes in the same way.
/// However, if [milliseconds] pass before [input] has completed, [onTimeout] is
/// run and its result is passed to [input] (with chaining, if it returns a
/// [Future]).
///
/// Note that timing out will not cancel the asynchronous operation behind
/// [input].
Future timeout(Future input, int milliseconds, onTimeout()) {
  var completer = new Completer();
  var timer = new Timer(new Duration(milliseconds: milliseconds), () {
    chainToCompleter(new Future.of(onTimeout), completer);
  });
  input.then((value) {
    if (completer.isCompleted) return;
    timer.cancel();
    completer.complete(value);
  }).catchError((e) {
    if (completer.isCompleted) return;
    timer.cancel();
    completer.completeError(e.error, e.stackTrace);
  });
  return completer.future;
}

/// Returns a [Future] that will complete in [milliseconds].
Future sleep(int milliseconds) {
  var completer = new Completer();
  mock_clock.newTimer(new Duration(milliseconds: milliseconds), () {
    completer.complete();
  });
  return completer.future;
}
