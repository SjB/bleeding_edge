// Copyright (c) 2013, the Dart project authors.  Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

library descriptor.async;

import 'dart:async';
import 'dart:io' as io;

import '../../descriptor.dart' as descriptor;
import '../../scheduled_test.dart';
import '../utils.dart';
import 'utils.dart';

/// A descriptor that wraps a [Future<Entry>] and forwards all asynchronous
/// operations to the result of the future. It's designed for use when the
/// full filesystem description isn't known when initializing the schedule.
///
/// Async descriptors don't support [load], since their names aren't
/// synchronously available.
class Async extends descriptor.Entry {
  /// The [Future] that will complete to the [Entry] this descriptor is
  /// wrapping.
  final Future<descriptor.Entry> future;

  Async(this.future)
      : super('<async descriptor>');

  Future create([String parent]) =>
    schedule(() => future.then((entry) => entry.create(parent)));

  Future validate([String parent]) =>
    schedule(() => future.then((entry) => entry.validate(parent)));

  Stream<List<int>> load(String path) => errorStream("Async descriptors don't "
      "support load().");

  Stream<List<int>> read() => errorStream("Async descriptors don't support "
      "read().");

  String describe() => "async descriptor";
}
