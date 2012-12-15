// Copyright (c) 2012, the Dart project authors.  Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

import 'dart:isolate';
import 'dart:io';

// Tests that an isolate's keeps message handling working after
// throwing an unhandled exception, if there is a top level callback
// method that returns whether to continue handling messages or not.
// This test verifies that a default-named callback function is called
// when no callback is specified in Isolate.spawnFunction.

// Note: this test will hang if an uncaught exception isn't handled,
// either by an error in the callback or it returning false.

void entry() {
  port.receive((message, replyTo) {
    if (message == 'throw exception') {
      throw new RuntimeError('ignore this exception');
    }
    replyTo.call('hello');
    port.close();
  });
}

bool _unhandledExceptionCallback(IsolateUnhandledException e) {
  return e.source.message == 'ignore this exception';
}

void main() {
  var isolate_port = spawnFunction(entry);

  // Send a message that will cause an ignorable exception to be thrown.
  Future f = isolate_port.call('throw exception');
  f.onComplete((future) {
    // Exception wasn't ignored as it was supposed to be.
    print('failed handling exception');
    exit(1);
  });

  // Verify that isolate can still handle messages.
  isolate_port.call('hi').onComplete((future) {
    if (future.exception != null) {
      print('unhandled exception: ${future.exception}');
      exit(1);
    }
    if (future.value != 'hello') {
      print('unexpected response: ${future.value}');
      exit(1);
    }
    exit(0);
  });
}