// ADS I Class Project
// Chisel Introduction
//
// Chair of Electronic Design Automation, RPTU in Kaiserslautern
// File created on 18/10/2022 by Tobias Jauch (@tojauch)

package readserial

import chisel3._
import chisel3.util._


/** controller class */
class Controller extends Module {

  val io = IO(new Bundle {

    val reset_n = Input(Bool())
    val rxd = Input(Bool())
    val cnt_s = Input(Bool())

    val cnt_en = Output(Bool())
    val valid = Output(Bool())


  })

  // internal variables

  // Internal state: false = IDLE, true = RECEIVE
  val receiving = RegInit(false.B)
  val validReg = RegInit(false.B)

  // State machine logic

  when(io.reset_n) { // if reset is received i.e. reset_n equals 1/true
    receiving := false.B  // set receiving to false
    validReg := false.B   // set valid to false

  }.otherwise {

    // Valid is only high for one clock cycle
    validReg := false.B //set valid to false.

    when(!receiving) {

      // Wait for start bit (0)
      when(!io.rxd) {
        receiving := true.B // set receiving to true (this also sends cnt_en to counter to start counting)
      }

    }.otherwise {

      // if Eight data bits have been received i.e. cnt_s equals 1
      when(io.cnt_s) {
        receiving := false.B // set receiving state to false
        validReg := true.B // and set valid reg to true.
      }
    }
  }

  // Outputs
  io.cnt_en := receiving // we map internal "receiving" state to cnt_en so it becomes high when the controller is receiving
  io.valid := validReg  // we map internal valid reg to output valid.

}


/** counter class */
class Counter extends Module {

  val io = IO(new Bundle {
    val reset_n = Input(Bool())
    val cnt_en = Input(Bool())
    val cnt_s = Output(Bool())
  })

  // internal variables
  // 3-bit counter: counts from 0 to 7
  val count = RegInit(0.U(3.W))

  // state machine

  // Counter functionality
  when(io.reset_n) { // if reset is received i.e. reset_n equals 1/true
    count := 0.U      // set internal counter to zero.

  }.elsewhen(io.cnt_en) {

    when(count === 7.U) { // if counter reaches maximum value
      count := 0.U  // set it to zero again.
    }.otherwise {
      count := count + 1.U  // otherwise increment counter.
    }
  }

  // Indicates that 8 bits have been received
  io.cnt_s := (count === 7.U) // if counter reaches max value that is 7 in our case (we count from 0-7 for 8 bits) output is set.

}

/** shift register class */
class ShiftRegister extends Module {

  val io = IO(new Bundle {

    val rxd = Input(Bool())
    val data = Output(UInt(8.W))
  })

  // internal variables

  // 8-bit shift register
  val shiftReg = RegInit(0.U(8.W))

  // functionality
  // Shift incoming serial bit into the register
  // MSB is transmitted first so we left shift the register and place it in bit-0.
  shiftReg := Cat(shiftReg(6, 0), io.rxd)

  // Parallel output
  io.data := shiftReg

}

/**
 * The last warm-up task deals with a more complex component. Your goal is to design a serial receiver.
 * It scans an input line (“serial bus”) named rxd for serial transmissions of data bytes. A transmission
 * begins with a start bit ‘0’ followed by 8 data bits. The most significant bit (MSB) is transmitted first.
 * There is no parity bit and no stop bit. After the last data bit has been transferred a new transmission
 * (beginning with a start bit, ‘0’) may immediately follow. If there is no new transmission the bus line
 * goes high (‘1’, this is considered the “idle” bus signal). In this case the receiver waits until the next
 * transmission begins. The outputs of the design are an 8-bit parallel data signal and a valid signal.
 * The valid signal goes high (‘1’) for one clock cycle after the last serial bit has been transmitted,
 * indicating that a new data byte is ready.
 */
class ReadSerial extends Module {

  val io = IO(new Bundle {

    val reset_n = Input(Bool())
    val rxd = Input(Bool())

    val data = Output(UInt(8.W))
    val valid = Output(Bool())
  })


  // instanciation of modules

  val controller = Module(new Controller)
  val counter = Module(new Counter)
  val shiftRegister = Module(new ShiftRegister)

  // connections between modules

  // Controller connections
  controller.io.reset_n := io.reset_n   // current module reset_n is assigned to reset_n input of controller
  controller.io.rxd := io.rxd           // current module rxd is assigned to rxd input of controller
  controller.io.cnt_s := counter.io.cnt_s // counter outupu cnt_s is assigned to cnt_s input of controller

  // Counter connections
  counter.io.reset_n := io.reset_n  // current module reset_n is assigned to reset_n input of counter
  counter.io.cnt_en := controller.io.cnt_en // controller output cnt_en is assigned to cnt_en input of counter

  // Shift register connections
  shiftRegister.io.rxd := io.rxd // current module rxd is assigned to rxd input of shift register

  // global I/O 

  // Global outputs
  io.data := shiftRegister.io.data // data output from shift register is assigned to data out of current module
  io.valid := controller.io.valid // valid output from controller is assigned to valid out of current module
}
