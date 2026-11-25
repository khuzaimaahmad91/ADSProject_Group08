// ADS I Class Project
// Chisel Introduction
//
// Chair of Electronic Design Automation, RPTU in Kaiserslautern
// File created on 18/10/2022 by Tobias Jauch (@tojauch)

package adder

import chisel3._
import chisel3.util._


/** 
  * Half Adder Class 
  * 
  * Your task is to implement a basic half adder as presented in the lecture.
  * Each signal should only be one bit wide (inputs and outputs).
  * There should be no delay between input and output signals, we want to have
  * a combinational behaviour of the component.
  */
class HalfAdder extends Module{
  
  val io = IO(new Bundle {

    val a  = Input(UInt(1.W)) // input variable a
    val b  = Input(UInt(1.W)) // input variable b
    val s  = Output(UInt(1.W)) // output variable s = sum
    val co  = Output(UInt(1.W)) // output variable co = carry out
    })

  io.s := io.a ^ io.b  // adds two inputs
  io.co := io.a & io.b // generates carry for two inputs.

}

/** 
  * Full Adder Class 
  * 
  * Your task is to implement a basic full adder. The component's behaviour should 
  * match the characteristics presented in the lecture. In addition, you are only allowed 
  * to use two half adders (use the class that you already implemented) and basic logic 
  * operators (AND, OR, ...).
  * Each signal should only be one bit wide (inputs and outputs).
  * There should be no delay between input and output signals, we want to have
  * a combinational behaviour of the component.
  */
class FullAdder extends Module{

  val io = IO(new Bundle {

    val a = Input(UInt(1.W)) // input variable a
    val b = Input(UInt(1.W)) // input variable b
    val ci = Input(UInt(1.W)) // input variable carry in
    val s = Output(UInt(1.W)) // output variable s = sum
    val co = Output(UInt(1.W)) // output variable co = carry out
    })

    // Instantiate two half adders
    val halfAdder1 = Module(new HalfAdder)
    val halfAdder2 = Module(new HalfAdder)

    // First half adder: adds a and b
    halfAdder1.io.a := io.a
    halfAdder1.io.b := io.b

    // Second half adder: adds first sum and carry-in
    halfAdder2.io.a := halfAdder1.io.s
    halfAdder2.io.b := io.ci

    // Final outputs
    io.s := halfAdder2.io.s
    io.co := halfAdder1.io.co | halfAdder2.io.co

}

/** 
  * 4-bit Adder class 
  * 
  * Your task is to implement a 4-bit ripple-carry-adder. The component's behaviour should 
  * match the characteristics presented in the lecture.  Remember: An n-bit adder can be 
  * build using one half adder and n-1 full adders.
  * The inputs and the result should all be 4-bit wide, the carry-out only needs one bit.
  * There should be no delay between input and output signals, we want to have
  * a combinational behaviour of the component.
  */
class FourBitAdder extends Module{

  val io = IO(new Bundle {

    val a = Input(UInt(4.W)) // input variable a
    val b = Input(UInt(4.W)) // input variable b
    val s = Output(UInt(4.W)) // output variable s = sum
    val co = Output(UInt(1.W)) // output variable co = carry out

    })

  val halfAdder = Module(new HalfAdder)
  val fullAdder1 = Module(new FullAdder)
  val fullAdder2 = Module(new FullAdder)
  val fullAdder3 = Module(new FullAdder)

  val sum = Wire(Vec(4, UInt(1.W)))

  // Bit 0: Half Adder
  halfAdder.io.a := io.a(0)
  halfAdder.io.b := io.b(0)
  sum(0) := halfAdder.io.s //sum of half adder is 1st bit of sum result

  // Bit 1: First Full Adder
  fullAdder1.io.a := io.a(1)
  fullAdder1.io.b := io.b(1)
  fullAdder1.io.ci := halfAdder.io.co // cary out of half adder is carry input to 1st full adder
  sum(1) := fullAdder1.io.s // sum out of 1st full adder is 2nd bit of sum result

  // Bit 2: Second Full Adder
  fullAdder2.io.a := io.a(2)
  fullAdder2.io.b := io.b(2)
  fullAdder2.io.ci := fullAdder1.io.co // cary out of 1st full adder is carry input to 2nd full adder
  sum(2) := fullAdder2.io.s // sum out of 2nd full adder is 3rd bit of sum result

  // Bit 3: Third Full Adder
  fullAdder3.io.a := io.a(3)
  fullAdder3.io.b := io.b(3)
  fullAdder3.io.ci := fullAdder2.io.co // cary out of 2nd full adder is carry input to 3rd full adder
  sum(3) := fullAdder3.io.s // sum out of 3rd full adder is 4th bit of sum result
  io.co := fullAdder3.io.co // cary out of 3rd full adder is final carry out

  io.s := sum.asUInt // final sum assignment

}
