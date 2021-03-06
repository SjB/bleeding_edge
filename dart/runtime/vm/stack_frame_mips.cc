// Copyright (c) 2013, the Dart project authors.  Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

#include "vm/globals.h"
#if defined(TARGET_ARCH_MIPS)

#include "vm/stack_frame.h"

namespace dart {

intptr_t StackFrame::PcAddressOffsetFromSp() {
  UNIMPLEMENTED();
  return 0;
}


intptr_t StackFrame::EntrypointMarkerOffsetFromFp() {
  UNIMPLEMENTED();
  return 0;
}


uword StackFrame::GetCallerFp() const {
  UNIMPLEMENTED();
  return 0;
}


uword StackFrame::GetCallerSp() const {
  UNIMPLEMENTED();
  return 0;
}


intptr_t EntryFrame::ExitLinkOffset() const {
  UNIMPLEMENTED();
  return 0;
}


intptr_t EntryFrame::SavedContextOffset() const {
  UNIMPLEMENTED();
  return 0;
}


void StackFrameIterator::SetupLastExitFrameData() {
  UNIMPLEMENTED();
}


void StackFrameIterator::SetupNextExitFrameData() {
  UNIMPLEMENTED();
}

}  // namespace dart

#endif  // defined TARGET_ARCH_MIPS
