// Copyright (c) 2012, the Dart project authors.  Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

library future_test;

import 'dart:async';
import 'dart:isolate';

const Duration MS = const Duration(milliseconds: 1);

testImmediate() {
  final future = new Future<String>.immediate("42");
  var port = new ReceivePort();
  future.then((x) {
    Expect.equals("42", x);
    port.close();
  });
}

testOf() {
  compare(func) {
    // Compare the results of the following two futures.
    Future f1 = new Future.of(func);
    Future f2 = new Future.immediate(null).then((_) => func());
    f2.catchError((_){});  // I'll get the error later.
    f1.then((v1) { f2.then((v2) { Expect.equals(v1, v2); }); },
            onError: (e1) {
              f2.then((_) { Expect.fail("Expected error"); },
                      onError: (e2) {
                         Expect.equals(e1.error, e2.error);
                      });
            });
  }
  Future val = new Future.immediate(42);
  Future err1 = new Future.immediateError("Error")..catchError((_){});
  Future err2 = new Future.immediateError(new AsyncError("AsyncError"))..catchError((_){});
  compare(() => 42);
  compare(() => val);
  compare(() { throw "Flif"; });
  compare(() { throw new AsyncError("AsyncFlif"); });
  compare(() => err1);
  compare(() => err2);
}

testNeverComplete() {
  final completer = new Completer<int>();
  final future = completer.future;
  future.then((v) => Expect.fails("Value not expected"));
  future.catchError((e) => Expect.fails("Value not expected"));
}

testComplete() {
  final completer = new Completer<int>();
  final future = completer.future;
  Expect.isFalse(completer.isCompleted);

  completer.complete(3);
  Expect.isTrue(completer.isCompleted);

  future.then((v) => Expect.equals(3, v));
}

// Tests for [then]

testCompleteWithSuccessHandlerBeforeComplete() {
  final completer = new Completer<int>();
  final future = completer.future;

  int value;
  future.then((int v) { value = v; });
  Expect.isNull(value);

  Expect.isFalse(completer.isCompleted);
  completer.complete(3);
  Expect.isTrue(completer.isCompleted);

  Expect.equals(3, value);
}

testCompleteWithSuccessHandlerAfterComplete() {
  final completer = new Completer<int>();
  final future = completer.future;

  int after;
  completer.complete(3);
  Expect.isNull(after);

  var port = new ReceivePort();
  future.then((int v) { after = v; })
    .then((_) {
      Expect.equals(3, after);
      port.close();
    });
}

testCompleteManySuccessHandlers() {
  final completer = new Completer<int>();
  final future = completer.future;
  int before;
  int after1;
  int after2;

  var futures = [];
  futures.add(future.then((int v) { before = v; }));
  completer.complete(3);
  futures.add(future.then((int v) { after1 = v; }));
  futures.add(future.then((int v) { after2 = v; }));

  var port = new ReceivePort();
  Future.wait(futures).then((_) {
    Expect.equals(3, before);
    Expect.equals(3, after1);
    Expect.equals(3, after2);
    port.close();
  });
}

// Tests for [catchError]

testException() {
  final completer = new Completer<int>();
  final future = completer.future;
  final ex = new Exception();

  var port = new ReceivePort();
  future
      .then((v) { throw "Value not expected"; })
      .catchError((e) {
        Expect.equals(e.error, ex);
        port.close();
      }, test: (e) => e == ex);
  completer.completeError(ex);
}

testExceptionHandler() {
  final completer = new Completer<int>();
  final future = completer.future;
  final ex = new Exception();

  var ex2;
  var done = future.catchError((e) { ex2 = e.error; });

  Expect.isFalse(completer.isCompleted);
  completer.completeError(ex);
  Expect.isTrue(completer.isCompleted);

  var port = new ReceivePort();
  done.then((_) {
    Expect.equals(ex, ex2);
    port.close();
  });
}

testExceptionHandlerReturnsTrue() {
  final completer = new Completer<int>();
  final future = completer.future;
  final ex = new Exception();

  bool reached = false;
  future.catchError((e) { });
  future.catchError((e) { reached = true; }, test: (e) => false)
        .catchError((e) {});
  Expect.isFalse(completer.isCompleted);
  completer.completeError(ex);
  Expect.isTrue(completer.isCompleted);
  Expect.isFalse(reached);
}

testExceptionHandlerReturnsTrue2() {
  final completer = new Completer<int>();
  final future = completer.future;
  final ex = new Exception();

  bool reached = false;
  var done = future
      .catchError((e) { }, test: (e) => false)
      .catchError((e) { reached = true; });
  completer.completeError(ex);

  var port = new ReceivePort();
  done.then((_) {
    Expect.isTrue(reached);
    port.close();
  });
}

testExceptionHandlerReturnsFalse() {
  final completer = new Completer<int>();
  final future = completer.future;
  final ex = new Exception();

  bool reached = false;

  future.catchError((e) { });

  future.catchError((e) { reached = true; }, test: (e) => false)
        .catchError((e) { });

  completer.completeError(ex);

  Expect.isFalse(reached);
}

testFutureAsStreamCompleteAfter() {
  var completer = new Completer();
  bool gotValue = false;
  var port = new ReceivePort();
  completer.future.asStream().listen(
      (data) {
        Expect.isFalse(gotValue);
        gotValue = true;
        Expect.equals("value", data);
      },
      onDone: () {
        Expect.isTrue(gotValue);
        port.close();
      });
  completer.complete("value");
}

testFutureAsStreamCompleteBefore() {
  var completer = new Completer();
  bool gotValue = false;
  var port = new ReceivePort();
  completer.complete("value");
  completer.future.asStream().listen(
      (data) {
        Expect.isFalse(gotValue);
        gotValue = true;
        Expect.equals("value", data);
      },
      onDone: () {
        Expect.isTrue(gotValue);
        port.close();
      });
}

testFutureAsStreamCompleteImmediate() {
  bool gotValue = false;
  var port = new ReceivePort();
  new Future.immediate("value").asStream().listen(
      (data) {
        Expect.isFalse(gotValue);
        gotValue = true;
        Expect.equals("value", data);
      },
      onDone: () {
        Expect.isTrue(gotValue);
        port.close();
      });
}

testFutureAsStreamCompleteErrorAfter() {
  var completer = new Completer();
  bool gotError = false;
  var port = new ReceivePort();
  completer.future.asStream().listen(
      (data) {
        Expect.fail("Unexpected data");
      },
      onError: (error) {
        Expect.isFalse(gotError);
        gotError = true;
        Expect.equals("error", error.error);
      },
      onDone: () {
        Expect.isTrue(gotError);
        port.close();
      });
  completer.completeError("error");
}

testFutureAsStreamWrapper() {
  var completer = new Completer();
  bool gotValue = false;
  var port = new ReceivePort();
  completer.complete("value");
  completer.future
      .catchError((_) { throw "not possible"; })  // Returns a future wrapper.
      .asStream().listen(
        (data) {
          Expect.isFalse(gotValue);
          gotValue = true;
          Expect.equals("value", data);
        },
        onDone: () {
          Expect.isTrue(gotValue);
          port.close();
        });
}

testFutureWhenCompleteValue() {
  var port = new ReceivePort();
  int counter = 2;
  countDown() {
    if (--counter == 0) port.close();
  }
  var completer = new Completer();
  Future future = completer.future;
  Future later = future.whenComplete(countDown);
  later.then((v) {
    Expect.equals(42, v);
    countDown();
  });
  completer.complete(42);
}

testFutureWhenCompleteError() {
  var port = new ReceivePort();
  int counter = 2;
  countDown() {
    if (--counter == 0) port.close();
  }
  var completer = new Completer();
  Future future = completer.future;
  Future later = future.whenComplete(countDown);
  later.catchError((AsyncError e) {
    Expect.equals("error", e.error);
    countDown();
  });
  completer.completeError("error");
}

testFutureWhenCompleteValueNewError() {
  var port = new ReceivePort();
  int counter = 2;
  countDown() {
    if (--counter == 0) port.close();
  }
  var completer = new Completer();
  Future future = completer.future;
  Future later = future.whenComplete(() {
    countDown();
    throw "new error";
  });
  later.catchError((AsyncError e) {
    Expect.equals("new error", e.error);
    countDown();
  });
  completer.complete(42);
}

testFutureWhenCompleteErrorNewError() {
  var port = new ReceivePort();
  int counter = 2;
  countDown() {
    if (--counter == 0) port.close();
  }
  var completer = new Completer();
  Future future = completer.future;
  Future later = future.whenComplete(() {
    countDown();
    throw "new error";
  });
  later.catchError((AsyncError e) {
    Expect.equals("new error", e.error);
    countDown();
  });
  completer.completeError("error");
}

testFutureWhenCompletePreValue() {
  var port = new ReceivePort();
  int counter = 2;
  countDown() {
    if (--counter == 0) port.close();
  }
  var completer = new Completer();
  Future future = completer.future;
  completer.complete(42);
  Timer.run(() {
    Future later = future.whenComplete(countDown);
    later.then((v) {
      Expect.equals(42, v);
      countDown();
    });
  });
}

testFutureWhenValueFutureValue() {

  var port = new ReceivePort();
  int counter = 3;
  countDown(int expect) {
    Expect.equals(expect, counter);
    if (--counter == 0) port.close();
  }
  var completer = new Completer();
  completer.future.whenComplete(() {
    countDown(3);
    var completer2 = new Completer();
    new Timer(MS * 10, () {
      countDown(2);
      completer2.complete(37);
    });
    return completer2.future;
  }).then((v) {
    Expect.equals(42, v);
    countDown(1);
  });

  completer.complete(42);
}

testFutureWhenValueFutureError() {
  var port = new ReceivePort();
  int counter = 3;
  countDown(int expect) {
    Expect.equals(expect, counter);
    if (--counter == 0) port.close();
  }
  var completer = new Completer();
  completer.future.whenComplete(() {
    countDown(3);
    var completer2 = new Completer();
    new Timer(MS * 10, () {
      countDown(2);
      completer2.completeError("Fail");
    });
    return completer2.future;
  }).then((v) {
    Expect.fail("should fail async");
  }, onError: (AsyncError e) {
    Expect.equals("Fail", e.error);
    countDown(1);
  });

  completer.complete(42);
}

testFutureWhenErrorFutureValue() {
  var port = new ReceivePort();
  int counter = 3;
  countDown(int expect) {
    Expect.equals(expect, counter);
    if (--counter == 0) port.close();
  }
  var completer = new Completer();
  completer.future.whenComplete(() {
    countDown(3);
    var completer2 = new Completer();
    new Timer(MS * 10, () {
      countDown(2);
      completer2.complete(37);
    });
    return completer2.future;
  }).then((v) {
    Expect.fail("should fail async");
  }, onError: (AsyncError e) {
    Expect.equals("Error", e.error);
    countDown(1);
  });

  completer.completeError("Error");
}

testFutureWhenErrorFutureError() {
  var port = new ReceivePort();
  int counter = 3;
  countDown(int expect) {
    Expect.equals(expect, counter);
    if (--counter == 0) port.close();
  }
  var completer = new Completer();
  completer.future.whenComplete(() {
    countDown(3);
    var completer2 = new Completer();
    new Timer(MS * 10, () {
      countDown(2);
      completer2.completeError("Fail");
    });
    return completer2.future;
  }).then((v) {
    Expect.fail("should fail async");
  }, onError: (AsyncError e) {
    Expect.equals("Fail", e.error);
    countDown(1);
  });

  completer.completeError("Error");
}

testFutureThenThrowsAsync() {
  final completer = new Completer<int>();
  final future = completer.future;
  AsyncError error = new AsyncError(42, "st");

  var port = new ReceivePort();
  future.then((v) {
    throw error;
  }).catchError((AsyncError e) {
    Expect.identical(error, e);
    port.close();
  });
  completer.complete(0);
}

testFutureCatchThrowsAsync() {
  final completer = new Completer<int>();
  final future = completer.future;
  AsyncError error = new AsyncError(42, "st");

  var port = new ReceivePort();
  future.catchError((AsyncError e) {
    throw error;
  }).catchError((AsyncError e) {
    Expect.identical(error, e);
    port.close();
  });
  completer.completeError(0);
}

testFutureCatchRethrowsAsync() {
  final completer = new Completer<int>();
  final future = completer.future;
  AsyncError error;

  var port = new ReceivePort();
  future.catchError((AsyncError e) {
    error = e;
    throw e;
  }).catchError((AsyncError e) {
    Expect.identical(error, e);
    port.close();
  });
  completer.completeError(0);
}

testFutureWhenThrowsAsync() {
  final completer = new Completer<int>();
  final future = completer.future;
  AsyncError error = new AsyncError(42, "st");

  var port = new ReceivePort();
  future.whenComplete(() {
    throw error;
  }).catchError((AsyncError e) {
    Expect.identical(error, e);
    port.close();
  });
  completer.complete(0);
}

testCompleteWithAsyncError() {
  final completer = new Completer<int>();
  final future = completer.future;
  AsyncError error = new AsyncError(42, "st");

  var port = new ReceivePort();
  future.catchError((AsyncError e) {
    Expect.identical(error, e);
    port.close();
  });

  completer.completeError(error);
}

testChainedFutureValue() {
  final completer = new Completer();
  final future = completer.future;
  var port = new ReceivePort();

  future.then((v) => new Future.immediate(v * 2))
        .then((v) {
          Expect.equals(42, v);
          port.close();
        });
  completer.complete(21);
}

testChainedFutureValueDelay() {
  final completer = new Completer();
  final future = completer.future;
  var port = new ReceivePort();

  future.then((v) => new Future.delayed(const Duration(milliseconds: 10),
                                        () => v * 2))
        .then((v) {
          Expect.equals(42, v);
          port.close();
        });
  completer.complete(21);
}

testChainedFutureValue2Delay() {
  var port = new ReceivePort();

  new Future.delayed(const Duration(milliseconds: 10))
    .then((v) {
      Expect.isNull(v);
      port.close();
    });
}
testChainedFutureError() {
  final completer = new Completer();
  final future = completer.future;
  var port = new ReceivePort();

  future.then((v) => new Future.immediateError("Fehler"))
        .then((v) { Expect.fail("unreachable!"); }, onError: (e) {
          Expect.equals("Fehler", e.error);
          port.close();
        });
  completer.complete(21);
}

main() {
  testImmediate();
  testOf();
  testNeverComplete();

  testComplete();
  testCompleteWithSuccessHandlerBeforeComplete();
  testCompleteWithSuccessHandlerAfterComplete();
  testCompleteManySuccessHandlers();
  testCompleteWithAsyncError();

  testException();
  testExceptionHandler();
  testExceptionHandlerReturnsTrue();
  testExceptionHandlerReturnsTrue2();
  testExceptionHandlerReturnsFalse();

  testFutureAsStreamCompleteAfter();
  testFutureAsStreamCompleteBefore();
  testFutureAsStreamCompleteImmediate();
  testFutureAsStreamCompleteErrorAfter();
  testFutureAsStreamWrapper();

  testFutureWhenCompleteValue();
  testFutureWhenCompleteError();
  testFutureWhenCompleteValueNewError();
  testFutureWhenCompleteErrorNewError();

  testFutureWhenValueFutureValue();
  testFutureWhenErrorFutureValue();
  testFutureWhenValueFutureError();
  testFutureWhenErrorFutureError();

  testFutureThenThrowsAsync();
  testFutureCatchThrowsAsync();
  testFutureWhenThrowsAsync();
  testFutureCatchRethrowsAsync();

  testChainedFutureValue();
  testChainedFutureValueDelay();
  testChainedFutureError();
}


