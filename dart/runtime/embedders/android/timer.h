// Copyright (c) 2012, the Dart project authors.  Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

#ifndef EMBEDDERS_ANDROID_TIMER_H_
#define EMBEDDERS_ANDROID_TIMER_H_

#include <time.h>

class Timer {
 public:
  Timer();
  void reset();
  void update();
  double now();
  float elapsed();

 private:
  float elapsed_;
  double last_time_;
};

#endif  // EMBEDDERS_ANDROID_TIMER_H_