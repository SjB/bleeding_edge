// Copyright (c) 2013, the Dart project authors.  Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

#include "vm/globals.h"
#if defined(TARGET_ARCH_MIPS)

// Only build the simulator if not compiling for real MIPS hardware.
#if !defined(HOST_ARCH_MIPS)

#include "vm/simulator.h"

#include "vm/assembler.h"
#include "vm/constants_mips.h"
#include "vm/disassembler.h"

namespace dart {

DEFINE_FLAG(int, stop_sim_at, 0, "Address to stop simulator at.");


// This macro provides a platform independent use of sscanf. The reason for
// SScanF not being implemented in a platform independent way through
// OS in the same way as SNPrint is that the Windows C Run-Time
// Library does not provide vsscanf.
#define SScanF sscanf  // NOLINT


// The SimulatorDebugger class is used by the simulator while debugging
// simulated MIPS code.
class SimulatorDebugger {
 public:
  explicit SimulatorDebugger(Simulator* sim);
  ~SimulatorDebugger();

  void Stop(Instr* instr, const char* message);
  void Debug();
  char* ReadLine(const char* prompt);

 private:
  Simulator* sim_;

  bool GetValue(char* desc, uint32_t* value);
  bool GetFValue(char* desc, double* value);

  // Set or delete a breakpoint. Returns true if successful.
  bool SetBreakpoint(Instr* breakpc);
  bool DeleteBreakpoint(Instr* breakpc);

  // Undo and redo all breakpoints. This is needed to bracket disassembly and
  // execution to skip past breakpoints when run from the debugger.
  void UndoBreakpoints();
  void RedoBreakpoints();
};


SimulatorDebugger::SimulatorDebugger(Simulator* sim) {
  sim_ = sim;
}


SimulatorDebugger::~SimulatorDebugger() {
}


void SimulatorDebugger::Stop(Instr* instr, const char* message) {
  OS::Print("Simulator hit %s\n", message);
  Debug();
}


static Register LookupCpuRegisterByName(const char* name) {
  static const char* kNames[] = {
      "r0",  "r1",  "r2",  "r3",
      "r4",  "r5",  "r6",  "r7",
      "r8",  "r9",  "r10", "r11",
      "r12", "r13", "r14", "r15",
      "r16", "r17", "r18", "r19",
      "r20", "r21", "r22", "r23",
      "r24", "r25", "r26", "r27",
      "r28", "r29", "r30", "r31",

      "zr",  "at",  "v0",  "v1",
      "a0",  "a1",  "a2",  "a3",
      "t0",  "t1",  "t2",  "t3",
      "t4",  "t5",  "t6",  "t7",
      "s0",  "s1",  "s2",  "s3",
      "s4",  "s5",  "s6",  "s7",
      "t8",  "t9",  "k0",  "k1",
      "gp",  "sp",  "fp",  "ra"
  };
  static const Register kRegisters[] = {
      R0,  R1,  R2,  R3,
      R4,  R5,  R6,  R7,
      R8,  R9,  R10, R11,
      R12, R13, R14, R15,
      R16, R17, R18, R19,
      R20, R21, R22, R23,
      R24, R25, R26, R27,
      R28, R29, R30, R31,

      ZR,  AT,  V0,  V1,
      A0,  A1,  A2,  A3,
      T0,  T1,  T2,  T3,
      T4,  T5,  T6,  T7,
      S0,  S1,  S2,  S3,
      S4,  S5,  S6,  S7,
      T8,  T9,  K0,  K1,
      GP,  SP,  FP,  RA
  };
  ASSERT(ARRAY_SIZE(kNames) == ARRAY_SIZE(kRegisters));
  for (unsigned i = 0; i < ARRAY_SIZE(kNames); i++) {
    if (strcmp(kNames[i], name) == 0) {
      return kRegisters[i];
    }
  }
  return kNoRegister;
}


static FRegister LookupFRegisterByName(const char* name) {
  int reg_nr = -1;
  bool ok = SScanF(name, "f%d", &reg_nr);
  if (ok && (0 <= reg_nr) && (reg_nr < kNumberOfFRegisters)) {
    return static_cast<FRegister>(reg_nr);
  }
  return kNoFRegister;
}


bool SimulatorDebugger::GetValue(char* desc, uint32_t* value) {
  Register reg = LookupCpuRegisterByName(desc);
  if (reg != kNoRegister) {
    *value = sim_->get_register(reg);
    return true;
  }
  if ((desc[0] == '*')) {
    uint32_t addr;
    if (GetValue(desc + 1, &addr)) {
      if (Simulator::IsIllegalAddress(addr)) {
        return false;
      }
      *value = *(reinterpret_cast<uint32_t*>(addr));
      return true;
    }
  }
  if (strcmp("pc", desc) == 0) {
    *value = sim_->get_pc();
    return true;
  }
  bool retval = SScanF(desc, "0x%x", value) == 1;
  if (!retval) {
    retval = SScanF(desc, "%x", value) == 1;
  }
  return retval;
}


bool SimulatorDebugger::GetFValue(char* desc, double* value) {
  FRegister freg = LookupFRegisterByName(desc);
  if (freg != kNoFRegister) {
    *value = sim_->get_fregister(freg);
    return true;
  }
  if ((desc[0] == '*')) {
    uint32_t addr;
    if (GetValue(desc + 1, &addr)) {
      if (Simulator::IsIllegalAddress(addr)) {
        return false;
      }
      *value = *(reinterpret_cast<float*>(addr));
      return true;
    }
  }
  return false;
}


bool SimulatorDebugger::SetBreakpoint(Instr* breakpc) {
  // Check if a breakpoint can be set. If not return without any side-effects.
  if (sim_->break_pc_ != NULL) {
    return false;
  }

  // Set the breakpoint.
  sim_->break_pc_ = breakpc;
  sim_->break_instr_ = breakpc->InstructionBits();
  // Not setting the breakpoint instruction in the code itself. It will be set
  // when the debugger shell continues.
  return true;
}


bool SimulatorDebugger::DeleteBreakpoint(Instr* breakpc) {
  if (sim_->break_pc_ != NULL) {
    sim_->break_pc_->SetInstructionBits(sim_->break_instr_);
  }

  sim_->break_pc_ = NULL;
  sim_->break_instr_ = 0;
  return true;
}


void SimulatorDebugger::UndoBreakpoints() {
  if (sim_->break_pc_ != NULL) {
    sim_->break_pc_->SetInstructionBits(sim_->break_instr_);
  }
}


void SimulatorDebugger::RedoBreakpoints() {
  if (sim_->break_pc_ != NULL) {
    sim_->break_pc_->SetInstructionBits(Instr::kBreakPointInstruction);
  }
}


void SimulatorDebugger::Debug() {
  intptr_t last_pc = -1;
  bool done = false;
  bool decoded = true;

#define COMMAND_SIZE 63
#define ARG_SIZE 255

#define STR(a) #a
#define XSTR(a) STR(a)

  char cmd[COMMAND_SIZE + 1];
  char arg1[ARG_SIZE + 1];
  char arg2[ARG_SIZE + 1];

  // make sure to have a proper terminating character if reaching the limit
  cmd[COMMAND_SIZE] = 0;
  arg1[ARG_SIZE] = 0;
  arg2[ARG_SIZE] = 0;

  // Undo all set breakpoints while running in the debugger shell. This will
  // make them invisible to all commands.
  UndoBreakpoints();

  while (!done) {
    if (last_pc != sim_->get_pc()) {
      last_pc = sim_->get_pc();
      decoded = Disassembler::Disassemble(last_pc, last_pc + Instr::kInstrSize);
    }
    char* line = ReadLine("sim> ");
    if (line == NULL) {
      break;
    } else {
      // Use sscanf to parse the individual parts of the command line. At the
      // moment no command expects more than two parameters.
      int args = SScanF(line,
                        "%" XSTR(COMMAND_SIZE) "s "
                        "%" XSTR(ARG_SIZE) "s "
                        "%" XSTR(ARG_SIZE) "s",
                        cmd, arg1, arg2);
      if ((strcmp(cmd, "h") == 0) || (strcmp(cmd, "help") == 0)) {
        OS::Print("c/cont -- continue execution\n"
                  "disasm -- disassemble instrs at current pc location\n"
                  "  other variants are:\n"
                  "    disasm <address>\n"
                  "    disasm <address> <number_of_instructions>\n"
                  "  by default 10 instrs are disassembled\n"
                  "del -- delete breakpoints\n"
                  "gdb -- transfer control to gdb\n"
                  "h/help -- print this help string\n"
                  "break <address> -- set break point at specified address\n"
                  "p/print <reg or value or *addr> -- print integer value\n"
                  "pf/printfloat <freg or *addr> -- print float value\n"
                  "po/printobject <*reg or *addr> -- print object\n"
                  "si/stepi -- single step an instruction\n"
                  "unstop -- if current pc is a stop instr make it a nop\n"
                  "q/quit -- Quit the debugger and exit the program\n");
      } else if ((strcmp(cmd, "quit") == 0) || (strcmp(cmd, "q") == 0)) {
        OS::Print("Quitting\n");
        OS::Exit(0);
      } else if ((strcmp(cmd, "si") == 0) || (strcmp(cmd, "stepi") == 0)) {
        if (decoded) {
          sim_->InstructionDecode(reinterpret_cast<Instr*>(sim_->get_pc()));
        } else {
          OS::Print("Instruction could not be decoded. Stepping disabled.\n");
        }
      } else if ((strcmp(cmd, "c") == 0) || (strcmp(cmd, "cont") == 0)) {
        if (decoded) {
          // Execute the one instruction we broke at with breakpoints disabled.
          sim_->InstructionDecode(reinterpret_cast<Instr*>(sim_->get_pc()));
          // Leave the debugger shell.
          done = true;
        } else {
          OS::Print("Instruction could not be decoded. Cannot continue.\n");
        }
      } else if ((strcmp(cmd, "p") == 0) || (strcmp(cmd, "print") == 0)) {
        if (args == 2) {
          uint32_t value;
          if (GetValue(arg1, &value)) {
            OS::Print("%s: %u 0x%x\n", arg1, value, value);
          } else {
            OS::Print("%s unrecognized\n", arg1);
          }
        } else {
          OS::Print("print <reg or value or *addr>\n");
        }
      } else if ((strcmp(cmd, "pf") == 0) ||
                 (strcmp(cmd, "printfloat") == 0)) {
        if (args == 2) {
          double dvalue;
          if (GetFValue(arg1, &dvalue)) {
            uint64_t long_value = bit_cast<uint64_t, double>(dvalue);
            OS::Print("%s: %llu 0x%llx %.8g\n",
                arg1, long_value, long_value, dvalue);
          } else {
            OS::Print("%s unrecognized\n", arg1);
          }
        } else {
          OS::Print("printfloat <dreg or *addr>\n");
        }
      } else if ((strcmp(cmd, "po") == 0) ||
                 (strcmp(cmd, "printobject") == 0)) {
        if (args == 2) {
          uint32_t value;
          // Make the dereferencing '*' optional.
          if (((arg1[0] == '*') && GetValue(arg1 + 1, &value)) ||
              GetValue(arg1, &value)) {
            if (Isolate::Current()->heap()->Contains(value)) {
              OS::Print("%s: \n", arg1);
#if defined(DEBUG)
              const Object& obj = Object::Handle(
                  reinterpret_cast<RawObject*>(value));
              obj.Print();
#endif  // defined(DEBUG)
            } else {
              OS::Print("0x%x is not an object reference\n", value);
            }
          } else {
            OS::Print("%s unrecognized\n", arg1);
          }
        } else {
          OS::Print("printobject <*reg or *addr>\n");
        }
      } else if (strcmp(cmd, "disasm") == 0) {
        uint32_t start = 0;
        uint32_t end = 0;
        if (args == 1) {
          start = sim_->get_pc();
          end = start + (10 * Instr::kInstrSize);
        } else if (args == 2) {
          if (GetValue(arg1, &start)) {
            // no length parameter passed, assume 10 instructions
            if (Simulator::IsIllegalAddress(start)) {
              // If start isn't a valid address, warn and use PC instead
              OS::Print("First argument yields invalid address: 0x%x\n", start);
              OS::Print("Using PC instead");
              start = sim_->get_pc();
            }
            end = start + (10 * Instr::kInstrSize);
          }
        } else {
          uint32_t length;
          if (GetValue(arg1, &start) && GetValue(arg2, &length)) {
            if (Simulator::IsIllegalAddress(start)) {
              // If start isn't a valid address, warn and use PC instead
              OS::Print("First argument yields invalid address: 0x%x\n", start);
              OS::Print("Using PC instead\n");
              start = sim_->get_pc();
            }
            end = start + (length * Instr::kInstrSize);
          }
        }

        Disassembler::Disassemble(start, end);
      } else if (strcmp(cmd, "gdb") == 0) {
        OS::Print("relinquishing control to gdb\n");
        OS::DebugBreak();
        OS::Print("regaining control from gdb\n");
      } else if (strcmp(cmd, "break") == 0) {
        if (args == 2) {
          uint32_t addr;
          if (GetValue(arg1, &addr)) {
            if (!SetBreakpoint(reinterpret_cast<Instr*>(addr))) {
              OS::Print("setting breakpoint failed\n");
            }
          } else {
            OS::Print("%s unrecognized\n", arg1);
          }
        } else {
          OS::Print("break <addr>\n");
        }
      } else if (strcmp(cmd, "del") == 0) {
        if (!DeleteBreakpoint(NULL)) {
          OS::Print("deleting breakpoint failed\n");
        }
      } else if (strcmp(cmd, "unstop") == 0) {
        intptr_t stop_pc = sim_->get_pc() - Instr::kInstrSize;
        Instr* stop_instr = reinterpret_cast<Instr*>(stop_pc);
        if (stop_instr->IsBreakPoint()) {
          stop_instr->SetInstructionBits(Instr::kNopInstruction);
        } else {
          OS::Print("Not at debugger stop.\n");
        }
      } else {
        OS::Print("Unknown command: %s\n", cmd);
      }
    }
    delete[] line;
  }

  // Add all the breakpoints back to stop execution and enter the debugger
  // shell when hit.
  RedoBreakpoints();

#undef COMMAND_SIZE
#undef ARG_SIZE

#undef STR
#undef XSTR
}


char* SimulatorDebugger::ReadLine(const char* prompt) {
  char* result = NULL;
  char line_buf[256];
  int offset = 0;
  bool keep_going = true;
  fprintf(stdout, "%s", prompt);
  fflush(stdout);
  while (keep_going) {
    if (fgets(line_buf, sizeof(line_buf), stdin) == NULL) {
      // fgets got an error. Just give up.
      if (result != NULL) {
        delete[] result;
      }
      return NULL;
    }
    int len = strlen(line_buf);
    if (len > 1 &&
        line_buf[len - 2] == '\\' &&
        line_buf[len - 1] == '\n') {
      // When we read a line that ends with a "\" we remove the escape and
      // append the remainder.
      line_buf[len - 2] = '\n';
      line_buf[len - 1] = 0;
      len -= 1;
    } else if ((len > 0) && (line_buf[len - 1] == '\n')) {
      // Since we read a new line we are done reading the line. This
      // will exit the loop after copying this buffer into the result.
      keep_going = false;
    }
    if (result == NULL) {
      // Allocate the initial result and make room for the terminating '\0'
      result = new char[len + 1];
      if (result == NULL) {
        // OOM, so cannot readline anymore.
        return NULL;
      }
    } else {
      // Allocate a new result with enough room for the new addition.
      int new_len = offset + len + 1;
      char* new_result = new char[new_len];
      if (new_result == NULL) {
        // OOM, free the buffer allocated so far and return NULL.
        delete[] result;
        return NULL;
      } else {
        // Copy the existing input into the new array and set the new
        // array as the result.
        memmove(new_result, result, offset);
        delete[] result;
        result = new_result;
      }
    }
    // Copy the newly read line into the result.
    memmove(result + offset, line_buf, len);
    offset += len;
  }
  ASSERT(result != NULL);
  result[offset] = '\0';
  return result;
}


void Simulator::InitOnce() {
}


Simulator::Simulator() {
  // Setup simulator support first. Some of this information is needed to
  // setup the architecture state.
  // We allocate the stack here, the size is computed as the sum of
  // the size specified by the user and the buffer space needed for
  // handling stack overflow exceptions. To be safe in potential
  // stack underflows we also add some underflow buffer space.
  stack_ = new char[(Isolate::GetSpecifiedStackSize() +
                     Isolate::kStackSizeBuffer +
                     kSimulatorStackUnderflowSize)];
  icount_ = 0;
  delay_slot_ = false;
  break_pc_ = NULL;
  break_instr_ = 0;

  // Setup architecture state.
  // All registers are initialized to zero to start with.
  for (int i = 0; i < kNumberOfCpuRegisters; i++) {
    registers_[i] = 0;
  }
  pc_ = 0;
  // The sp is initialized to point to the bottom (high address) of the
  // allocated stack area.
  registers_[SP] = StackTop();
}


Simulator::~Simulator() {
  Isolate* isolate = Isolate::Current();
  if (isolate != NULL) {
    isolate->set_simulator(NULL);
  }
}


// Get the active Simulator for the current isolate.
Simulator* Simulator::Current() {
  Simulator* simulator = Isolate::Current()->simulator();
  if (simulator == NULL) {
    simulator = new Simulator();
    Isolate::Current()->set_simulator(simulator);
  }
  return simulator;
}


// Sets the register in the architecture state. It will also deal with updating
// Simulator internal state for special registers such as PC.
void Simulator::set_register(Register reg, int32_t value) {
  if (reg != R0) {
    registers_[reg] = value;
  }
}


// Get the register from the architecture state. This function does handle
// the special case of accessing the PC register.
int32_t Simulator::get_register(Register reg) const {
  if (reg == R0) {
    return 0;
  }
  return registers_[reg];
}


void Simulator::set_fregister(FRegister reg, double value) {
  ASSERT((reg >= 0) && (reg < kNumberOfFRegisters));
  fregisters_[reg] = value;
}


double Simulator::get_fregister(FRegister reg) const {
  ASSERT((reg >= 0) && (reg < kNumberOfFRegisters));
  return fregisters_[reg];
}


void Simulator::HandleIllegalAccess(uword addr, Instr* instr) {
  uword fault_pc = get_pc();
  // The debugger will not be able to single step past this instruction, but
  // it will be possible to disassemble the code and inspect registers.
  char buffer[128];
  snprintf(buffer, sizeof(buffer),
           "illegal memory access at 0x%"Px", pc=0x%"Px"\n",
           addr, fault_pc);
  SimulatorDebugger dbg(this);
  dbg.Stop(instr, buffer);
  // The debugger will return control in non-interactive mode.
  FATAL("Cannot continue execution after illegal memory access.");
}


void Simulator::UnalignedAccess(const char* msg, uword addr, Instr* instr) {
  // The debugger will not be able to single step past this instruction, but
  // it will be possible to disassemble the code and inspect registers.
  char buffer[64];
  snprintf(buffer, sizeof(buffer),
           "unaligned %s at 0x%"Px", pc=%p\n", msg, addr, instr);
  SimulatorDebugger dbg(this);
  dbg.Stop(instr, buffer);
  // The debugger will return control in non-interactive mode.
  FATAL("Cannot continue execution after unaligned access.");
}


// Returns the top of the stack area to enable checking for stack pointer
// validity.
uword Simulator::StackTop() const {
  // To be safe in potential stack underflows we leave some buffer above and
  // set the stack top.
  return reinterpret_cast<uword>(stack_) +
      (Isolate::GetSpecifiedStackSize() + Isolate::kStackSizeBuffer);
}


void Simulator::Format(Instr* instr, const char* format) {
  OS::PrintErr("Simulator - unknown instruction: %s\n", format);
  UNIMPLEMENTED();
}


int8_t Simulator::ReadB(uword addr) {
  int8_t* ptr = reinterpret_cast<int8_t*>(addr);
  return *ptr;
}


uint8_t Simulator::ReadBU(uword addr) {
  uint8_t* ptr = reinterpret_cast<uint8_t*>(addr);
  return *ptr;
}


int16_t Simulator::ReadH(uword addr, Instr* instr) {
  if ((addr & 1) == 0) {
    int16_t* ptr = reinterpret_cast<int16_t*>(addr);
    return *ptr;
  }
  UnalignedAccess("signed halfword read", addr, instr);
  return 0;
}


uint16_t Simulator::ReadHU(uword addr, Instr* instr) {
  if ((addr & 1) == 0) {
    uint16_t* ptr = reinterpret_cast<uint16_t*>(addr);
    return *ptr;
  }
  UnalignedAccess("unsigned halfword read", addr, instr);
  return 0;
}


int Simulator::ReadW(uword addr, Instr* instr) {
  if ((addr & 3) == 0) {
    intptr_t* ptr = reinterpret_cast<intptr_t*>(addr);
    return *ptr;
  }
  UnalignedAccess("read", addr, instr);
  return 0;
}


void Simulator::WriteB(uword addr, uint8_t value) {
  uint8_t* ptr = reinterpret_cast<uint8_t*>(addr);
  *ptr = value;
}


void Simulator::WriteH(uword addr, uint16_t value, Instr* instr) {
  if ((addr & 1) == 0) {
    uint16_t* ptr = reinterpret_cast<uint16_t*>(addr);
    *ptr = value;
    return;
  }
  UnalignedAccess("halfword write", addr, instr);
}


void Simulator::WriteW(uword addr, int value, Instr* instr) {
  if ((addr & 3) == 0) {
    intptr_t* ptr = reinterpret_cast<intptr_t*>(addr);
    *ptr = value;
    return;
  }
  UnalignedAccess("write", addr, instr);
}


bool Simulator::OverflowFrom(int32_t alu_out,
                             int32_t left, int32_t right, bool addition) {
  bool overflow;
  if (addition) {
               // Operands have the same sign.
    overflow = ((left >= 0 && right >= 0) || (left < 0 && right < 0))
               // And operands and result have different sign.
               && ((left < 0 && alu_out >= 0) || (left >= 0 && alu_out < 0));
  } else {
               // Operands have different signs.
    overflow = ((left < 0 && right >= 0) || (left >= 0 && right < 0))
               // And first operand and result have different signs.
               && ((left < 0 && alu_out >= 0) || (left >= 0 && alu_out < 0));
  }
  return overflow;
}


void Simulator::DecodeSpecial(Instr* instr) {
  ASSERT(instr->OpcodeField() == SPECIAL);
  switch (instr->FunctionField()) {
    case ADDU: {
      ASSERT(instr->SaField() == 0);
      // Format(instr, "addu 'rd, 'rs, 'rt");
      int32_t rs_val = get_register(instr->RsField());
      int32_t rt_val = get_register(instr->RtField());
      set_register(instr->RdField(), rs_val + rt_val);
      break;
    }
    case AND: {
      ASSERT(instr->SaField() == 0);
      // Format(instr, "and 'rd, 'rs, 'rt");
      int32_t rs_val = get_register(instr->RsField());
      int32_t rt_val = get_register(instr->RtField());
      set_register(instr->RdField(), rs_val & rt_val);
      break;
    }
    case BREAK: {
      SimulatorDebugger dbg(this);
      dbg.Stop(instr, "breakpoint");
      break;
    }
    case DIV: {
      ASSERT(instr->RdField() == 0);
      ASSERT(instr->SaField() == 0);
      // Format(instr, "div 'rs, 'rt");
      int32_t rs_val = get_register(instr->RsField());
      int32_t rt_val = get_register(instr->RtField());
      if (rt_val == 0) {
        // Results are unpredictable.
        set_hi_register(0);
        set_lo_register(0);
        // TODO(zra): Drop into the debugger here.
        break;
      }

      if ((rs_val == static_cast<int32_t>(0x80000000)) &&
          (rt_val == static_cast<int32_t>(0xffffffff))) {
        set_lo_register(0x80000000);
        set_hi_register(0);
      } else {
        set_lo_register(rs_val / rt_val);
        set_hi_register(rs_val % rt_val);
      }
      break;
    }
    case DIVU: {
      ASSERT(instr->RdField() == 0);
      ASSERT(instr->SaField() == 0);
      // Format(instr, "divu 'rs, 'rt");
      uint32_t rs_val = get_register(instr->RsField());
      uint32_t rt_val = get_register(instr->RtField());
      if (rt_val == 0) {
        // Results are unpredictable.
        set_hi_register(0);
        set_lo_register(0);
        // TODO(zra): Drop into the debugger here.
        break;
      }

      set_lo_register(rs_val / rt_val);
      set_hi_register(rs_val % rt_val);
      break;
    }
    case MFHI: {
      ASSERT(instr->RsField() == 0);
      ASSERT(instr->RtField() == 0);
      ASSERT(instr->SaField() == 0);
      // Format(instr, "mfhi 'rd");
      set_register(instr->RdField(), get_hi_register());
      break;
    }
    case MFLO: {
      ASSERT(instr->RsField() == 0);
      ASSERT(instr->RtField() == 0);
      ASSERT(instr->SaField() == 0);
      // Format(instr, "mflo 'rd");
      set_register(instr->RdField(), get_lo_register());
      break;
    }
    case SLL: {
      ASSERT(instr->RsField() == 0);
      if ((instr->RdField() == R0) &&
          (instr->RtField() == R0) &&
          (instr->SaField() == 0)) {
        // Format(instr, "nop");
        // Nothing to be done for NOP.
      } else {
        int32_t rt_val = get_register(instr->RtField());
        int sa = instr->SaField();
        set_register(instr->RdField(), rt_val << sa);
      }
      break;
    }
    case JR: {
      ASSERT(instr->RtField() == R0);
      ASSERT(instr->RdField() == R0);
      ASSERT(!delay_slot_);
      // Format(instr, "jr'hint 'rs");
      uword next_pc = get_register(instr->RsField());
      ExecuteDelaySlot();
      pc_ = next_pc - Instr::kInstrSize;  // Account for regular PC increment.
      break;
    }
    default: {
      OS::PrintErr("DecodeSpecial: 0x%x\n", instr->InstructionBits());
      UNIMPLEMENTED();
      break;
    }
  }
}


void Simulator::DecodeSpecial2(Instr* instr) {
  ASSERT(instr->OpcodeField() == SPECIAL2);
  switch (instr->FunctionField()) {
    case CLO: {
      ASSERT(instr->SaField() == 0);
      ASSERT(instr->RtField() == instr->RdField());
      // Format(instr, "clo 'rd, 'rs");
      int32_t rs_val = get_register(instr->RsField());
      int32_t bitcount = 0;
      while (rs_val < 0) {
        bitcount++;
        rs_val <<= 1;
      }
      set_register(instr->RdField(), bitcount);
      break;
    }
    case CLZ: {
      ASSERT(instr->SaField() == 0);
      ASSERT(instr->RtField() == instr->RdField());
      // Format(instr, "clz 'rd, 'rs");
      int32_t rs_val = get_register(instr->RsField());
      int32_t bitcount = 0;
      if (rs_val != 0) {
        while (rs_val > 0) {
          bitcount++;
          rs_val <<= 1;
        }
      } else {
        bitcount = 32;
      }
      set_register(instr->RdField(), bitcount);
      break;
    }
    default: {
      OS::PrintErr("DecodeSpecial2: 0x%x\n", instr->InstructionBits());
      UNIMPLEMENTED();
      break;
    }
  }
}


void Simulator::InstructionDecode(Instr* instr) {
  switch (instr->OpcodeField()) {
    case SPECIAL: {
      DecodeSpecial(instr);
      break;
    }
    case SPECIAL2: {
      DecodeSpecial2(instr);
      break;
    }
    case ADDIU: {
      // Format(instr, "addiu 'rt, 'rs, 'imms");
      int32_t rs_val = get_register(instr->RsField());
      int32_t imm_val = instr->SImmField();
      int32_t res = rs_val + imm_val;
      // Rt is set even on overflow.
      set_register(instr->RtField(), res);
      break;
    }
    case ANDI: {
      // Format(instr, "andi 'rt, 'rs, 'immu");
      int32_t rs_val = get_register(instr->RsField());
      set_register(instr->RtField(), rs_val & instr->UImmField());
      break;
    }
    case LB: {
      // Format(instr, "lb 'rt, 'imms('rs)");
      int32_t base_val = get_register(instr->RsField());
      int32_t imm_val = instr->SImmField();
      uword addr = base_val + imm_val;
      if (Simulator::IsIllegalAddress(addr)) {
        HandleIllegalAccess(addr, instr);
      } else {
        int32_t res = ReadB(addr);
        set_register(instr->RtField(), res);
      }
      break;
    }
    case LBU: {
      // Format(instr, "lbu 'rt, 'imms('rs)");
      int32_t base_val = get_register(instr->RsField());
      int32_t imm_val = instr->SImmField();
      uword addr = base_val + imm_val;
      if (Simulator::IsIllegalAddress(addr)) {
        HandleIllegalAccess(addr, instr);
      } else {
        int32_t res = ReadBU(addr);
        set_register(instr->RtField(), res);
      }
      break;
    }
    case LH: {
      // Format(instr, "lh 'rt, 'imms('rs)");
      int32_t base_val = get_register(instr->RsField());
      int32_t imm_val = instr->SImmField();
      uword addr = base_val + imm_val;
      if (Simulator::IsIllegalAddress(addr)) {
        HandleIllegalAccess(addr, instr);
      } else {
        int32_t res = ReadH(addr, instr);
        set_register(instr->RtField(), res);
      }
      break;
    }
    case LHU: {
      // Format(instr, "lhu 'rt, 'imms('rs)");
      int32_t base_val = get_register(instr->RsField());
      int32_t imm_val = instr->SImmField();
      uword addr = base_val + imm_val;
      if (Simulator::IsIllegalAddress(addr)) {
        HandleIllegalAccess(addr, instr);
      } else {
        int32_t res = ReadHU(addr, instr);
        set_register(instr->RtField(), res);
      }
      break;
    }
    case LUI: {
      ASSERT(instr->RsField() == 0);
      set_register(instr->RtField(), instr->UImmField() << 16);
      break;
    }
    case LW: {
      // Format(instr, "lw 'rt, 'imms('rs)");
      int32_t base_val = get_register(instr->RsField());
      int32_t imm_val = instr->SImmField();
      uword addr = base_val + imm_val;
      if (Simulator::IsIllegalAddress(addr)) {
        HandleIllegalAccess(addr, instr);
      } else {
        int32_t res = ReadW(addr, instr);
        set_register(instr->RtField(), res);
      }
      break;
    }
    case ORI: {
      // Format(instr, "ori 'rt, 'rs, 'immu");
      int32_t rs_val = get_register(instr->RsField());
      set_register(instr->RtField(), rs_val | instr->UImmField());
      break;
    }
    case SB: {
      // Format(instr, "sb 'rt, 'imms('rs)");
      int32_t rt_val = get_register(instr->RtField());
      int32_t base_val = get_register(instr->RsField());
      int32_t imm_val = instr->SImmField();
      uword addr = base_val + imm_val;
      if (Simulator::IsIllegalAddress(addr)) {
        HandleIllegalAccess(addr, instr);
      } else {
        WriteB(addr, rt_val & 0xff);
      }
      break;
    }
    case SH: {
      // Format(instr, "sh 'rt, 'imms('rs)");
      int32_t rt_val = get_register(instr->RtField());
      int32_t base_val = get_register(instr->RsField());
      int32_t imm_val = instr->SImmField();
      uword addr = base_val + imm_val;
      if (Simulator::IsIllegalAddress(addr)) {
        HandleIllegalAccess(addr, instr);
      } else {
        WriteH(addr, rt_val & 0xffff, instr);
      }
      break;
    }
    case SW: {
      // Format(instr, "sw 'rt, 'imms('rs)");
      int32_t rt_val = get_register(instr->RtField());
      int32_t base_val = get_register(instr->RsField());
      int32_t imm_val = instr->SImmField();
      uword addr = base_val + imm_val;
      if (Simulator::IsIllegalAddress(addr)) {
        HandleIllegalAccess(addr, instr);
      } else {
        WriteW(addr, rt_val, instr);
      }
      break;
    }
    default: {
      OS::PrintErr("Undecoded instruction: 0x%x at %p\n",
                    instr->InstructionBits(), instr);
      UNIMPLEMENTED();
      break;
    }
  }
  pc_ += Instr::kInstrSize;
}


void Simulator::ExecuteDelaySlot() {
  ASSERT(pc_ != kEndSimulatingPC);
  delay_slot_ = true;
  icount_++;
  if (icount_ == FLAG_stop_sim_at) {
    UNIMPLEMENTED();
  }
  Instr* instr = Instr::At(pc_ + Instr::kInstrSize);
  InstructionDecode(instr);
  delay_slot_ = false;
}


void Simulator::Execute() {
  if (FLAG_stop_sim_at == 0) {
    // Fast version of the dispatch loop without checking whether the simulator
    // should be stopping at a particular executed instruction.
    while (pc_ != kEndSimulatingPC) {
      icount_++;
      Instr* instr = Instr::At(pc_);
      InstructionDecode(instr);
    }
  } else {
    // FLAG_stop_sim_at is at the non-default value. Stop in the debugger when
    // we reach the particular instruction count.
    while (pc_ != kEndSimulatingPC) {
      icount_++;
      if (icount_ == FLAG_stop_sim_at) {
        UNIMPLEMENTED();
      } else {
        Instr* instr = Instr::At(pc_);
        InstructionDecode(instr);
      }
    }
  }
}


int64_t Simulator::Call(int32_t entry,
                        int32_t parameter0,
                        int32_t parameter1,
                        int32_t parameter2,
                        int32_t parameter3) {
  // Save the SP register before the call so we can restore it.
  int32_t sp_before_call = get_register(SP);

  // Setup parameters.
  set_register(A0, parameter0);
  set_register(A1, parameter1);
  set_register(A2, parameter2);
  set_register(A3, parameter3);

  // Make sure the activation frames are properly aligned.
  int32_t stack_pointer = sp_before_call;
  static const int kFrameAlignment = OS::ActivationFrameAlignment();
  if (kFrameAlignment > 0) {
    stack_pointer = Utils::RoundDown(stack_pointer, kFrameAlignment);
  }
  set_register(SP, stack_pointer);

  // Prepare to execute the code at entry.
  set_pc(entry);
  // Put down marker for end of simulation. The simulator will stop simulation
  // when the PC reaches this value. By saving the "end simulation" value into
  // RA the simulation stops when returning to this call point.
  set_register(RA, kEndSimulatingPC);

  // Remember the values of callee-saved registers.
  // The code below assumes that r9 is not used as sb (static base) in
  // simulator code and therefore is regarded as a callee-saved register.
  int32_t r16_val = get_register(R16);
  int32_t r17_val = get_register(R17);
  int32_t r18_val = get_register(R18);
  int32_t r19_val = get_register(R19);
  int32_t r20_val = get_register(R20);
  int32_t r21_val = get_register(R21);
  int32_t r22_val = get_register(R22);
  int32_t r23_val = get_register(R23);

  // Setup the callee-saved registers with a known value. To be able to check
  // that they are preserved properly across dart execution.
  int32_t callee_saved_value = icount_;
  set_register(R16, callee_saved_value);
  set_register(R17, callee_saved_value);
  set_register(R18, callee_saved_value);
  set_register(R19, callee_saved_value);
  set_register(R20, callee_saved_value);
  set_register(R21, callee_saved_value);
  set_register(R22, callee_saved_value);
  set_register(R23, callee_saved_value);

  // Start the simulation
  Execute();

  // Check that the callee-saved registers have been preserved.
  ASSERT(callee_saved_value == get_register(R16));
  ASSERT(callee_saved_value == get_register(R17));
  ASSERT(callee_saved_value == get_register(R18));
  ASSERT(callee_saved_value == get_register(R19));
  ASSERT(callee_saved_value == get_register(R20));
  ASSERT(callee_saved_value == get_register(R21));
  ASSERT(callee_saved_value == get_register(R22));
  ASSERT(callee_saved_value == get_register(R23));

  // Restore callee-saved registers with the original value.
  set_register(R16, r16_val);
  set_register(R17, r17_val);
  set_register(R18, r18_val);
  set_register(R19, r19_val);
  set_register(R20, r20_val);
  set_register(R21, r21_val);
  set_register(R22, r22_val);
  set_register(R23, r23_val);

  // Restore the SP register and return R1:R0.
  set_register(SP, sp_before_call);
  return Utils::LowHighTo64Bits(get_register(V0), get_register(V1));
}

}  // namespace dart

#endif  // !defined(HOST_ARCH_MIPS)

#endif  // defined TARGET_ARCH_MIPS
