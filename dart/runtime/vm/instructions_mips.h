// Copyright (c) 2013, the Dart project authors.  Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
// Classes that describe assembly patterns as used by inline caches.

#ifndef VM_INSTRUCTIONS_MIPS_H_
#define VM_INSTRUCTIONS_MIPS_H_

#ifndef VM_INSTRUCTIONS_H_
#error Do not include instructions_mips.h directly; use instructions.h instead.
#endif

#include "vm/constants_mips.h"
#include "vm/object.h"

namespace dart {

class CallPattern : public ValueObject {
 public:
  CallPattern(uword pc, const Code& code);

  uword TargetAddress() const;
  void SetTargetAddress(uword target_address) const;

 private:
  uword Back(int n) const;
  int DecodePoolIndex();
  const uword* end_;
  const int pool_index_;
  const Array& object_pool_;

  DISALLOW_COPY_AND_ASSIGN(CallPattern);
};


class JumpPattern : public ValueObject {
 public:
  explicit JumpPattern(uword pc);

  static const int kLengthInBytes = 3*Instr::kInstrSize;

  int pattern_length_in_bytes() const {
    return kLengthInBytes;
  }

  bool IsValid() const;
  uword TargetAddress() const;
  void SetTargetAddress(uword target_address) const;

 private:
  const uword pc_;

  DISALLOW_COPY_AND_ASSIGN(JumpPattern);
};

}  // namespace dart

#endif  // VM_INSTRUCTIONS_MIPS_H_

