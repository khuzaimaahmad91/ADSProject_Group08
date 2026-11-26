// ADS I Class Project
// Chisel Introduction
//
// Chair of Electronic Design Automation, RPTU in Kaiserslautern
// File created on 18/10/2022 by Tobias Jauch (@tojauch)

package readserial

import chisel3._
import chisel3.util._


/** controller class */
class Controller extends Module{
  
  val io = IO(new Bundle {
    /* 
     * TODO: Define IO ports of a the component as stated in the documentation
     */
      val reset_n  = Input(UInt(1.W))
      val rxd  = Input(UInt(1.W))
      val cnt_s  = Input(UInt(4.W)) 
      val cnt_en  = Output(UInt(1.W))
      val valid  = Output(UInt(1.W))      
    })

  // internal variables
  /* 
   * TODO: Define internal variables (registers and/or wires), if needed
   */
    val receive_flag = RegInit(0.U(1.W))
    io.cnt_en := 0.U
    io.valid := 0.U
  // state machine
  /* 
   * TODO: Describe functionality if the controller as a state machine
   */
  when(io.reset_n === 1.U){
    receive_flag := 0.U
  } .elsewhen(receive_flag === 0.U){
    when(io.rxd === 0.U){
    receive_flag := 1.U
    }
  } .otherwise{
    when(io.cnt_s === 8.U){
      io.valid := 1.U

       // Check whether another transmission starts immediately
      when(io.rxd === 0.U) {
        receive_flag := 1.U
      }.otherwise {
        receive_flag := 0.U
      }

    } .otherwise{
    io.cnt_en := 1.U
    }
  }
}


/** counter class */
class Counter extends Module{
  
  val io = IO(new Bundle {
    /* 
     * TODO: Define IO ports of a the component as stated in the documentation
     */
      val reset_n  = Input(UInt(1.W))
      val cnt_en  = Input(UInt(1.W))
      val cnt_s  = Output(UInt(4.W))  
    })

  // internal variables
  /* 
   * TODO: Define internal variables (registers and/or wires), if needed
   */
  val cnt = RegInit(0.U(4.W))
  // state machine
  /* 
   * TODO: Describe functionality if the counter as a state machine
   */
  
  when(io.reset_n === 1.U){
    cnt := 0.U
  }.elsewhen(io.cnt_en === 1.U){
    cnt := cnt + 1.U
  }.otherwise{
    cnt := 0.U
  }
  io.cnt_s := cnt
}

/** shift register class */
class ShiftRegister extends Module{
  
  val io = IO(new Bundle {
    /* 
     * TODO: Define IO ports of a the component as stated in the documentation
     */
    val rxd  = Input(UInt(1.W))
    val cnt_en = Input(UInt(1.W))
    val reset_n = Input(UInt(1.W))
    val data  = Output(UInt(8.W))
    })

  // internal variables
  /* 
   * TODO: Define internal variables (registers and/or wires), if needed
   */
    val shift_Reg = RegInit(0.U(8.W))
    
  // functionality
  /* 
   * TODO: Describe functionality if the shift register
   */
  when(io.reset_n === 1.U){
    shift_Reg := 0.U
  }.elsewhen(io.cnt_en === 1.U){
    shift_Reg := Cat(shift_Reg(6, 0), io.rxd)
  }
  io.data := shift_Reg
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
class ReadSerial extends Module{
  
  val io = IO(new Bundle {
    /* 
     * TODO: Define IO ports of a the component as stated in the documentation
     */
    val rxd = Input(UInt(1.W))
    val reset_n  = Input(UInt(1.W))
    val valid  = Output(UInt(1.W))
    val data  = Output(UInt(8.W))
    })


  // instanciation of modules
  /* 
   * TODO: Instanciate the modules that you need
   */
    val control = Module(new Controller)
    val shiftReg = Module(new ShiftRegister)
    val count = Module(new Counter)
  // connections between modules
  /* 
   * TODO: connect the signals between the modules
   */
    control.io.reset_n := io.reset_n
    control.io.rxd := io.rxd
    control.io.cnt_s := count.io.cnt_s

    count.io.reset_n := io.reset_n
    count.io.cnt_en := control.io.cnt_en

    shiftReg.io.cnt_en := control.io.cnt_en
    shiftReg.io.reset_n := io.reset_n 
    shiftReg.io.rxd := io.rxd
  // global I/O 
  /* 
   * TODO: Describe output behaviour based on the input values and the internal signals
   */
  io.data := shiftReg.io.data 
  io.valid := control.io.valid
}
