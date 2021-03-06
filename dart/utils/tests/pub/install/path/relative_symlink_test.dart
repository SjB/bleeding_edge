// Copyright (c) 2013, the Dart project authors.  Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

import 'dart:io';

import '../../../../../pkg/pathos/lib/path.dart' as path;

import '../../../../pub/exit_codes.dart' as exit_codes;
import '../../test_pub.dart';

main() {
  // Pub uses NTFS junction points to create links in the packages directory.
  // These (unlike the symlinks that are supported in Vista and later) do not
  // support relative paths. So this test, by design, will not pass on Windows.
  // So just skip it.
  if (Platform.operatingSystem == "windows") return;

  initConfig();
  integration("generates a symlink with a relative path if the dependency "
              "path was relative", () {
    dir("foo", [
      libDir("foo"),
      libPubspec("foo", "0.0.1")
    ]).scheduleCreate();

    dir(appPath, [
      pubspec({
        "name": "myapp",
        "dependencies": {
          "foo": {"path": "../foo"}
        }
      })
    ]).scheduleCreate();

    schedulePub(args: ["install"],
        output: new RegExp(r"Dependencies installed!$"));

    dir("moved").scheduleCreate();

    // Move the app and package. Since they are still next to each other, it
    // should still be found.
    scheduleRename("foo", path.join("moved", "foo"));
    scheduleRename(appPath, path.join("moved", appPath));

    dir("moved", [
      dir(packagesPath, [
        dir("foo", [
          file("foo.dart", 'main() => "foo";')
        ])
      ])
    ]).scheduleValidate();
  });
}
