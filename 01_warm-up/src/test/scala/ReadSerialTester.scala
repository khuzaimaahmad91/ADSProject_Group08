// ADS I Class Project
// Chisel Introduction
//
// Chair of Electronic Design Automation, RPTU in Kaiserslautern
// File created on 18/10/2022 by Tobias Jauch (@tojauch)

package readserial

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec


/**
 * read serial tester
 */
class ReadSerialTester extends AnyFlatSpec with ChiselScalatestTester {

  "ReadSerial" should "work" in {
    test(new ReadSerial).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>

      // Helper function to initialize the receiver
      def initialize(): Unit = {
        dut.io.reset_n.poke(true.B) // reset the system "active high reset"
        dut.io.rxd.poke(true.B) // sent '1' on rxd line.
        dut.clock.step() // clock step

        dut.io.reset_n.poke(false.B) // reset signal low
        dut.io.valid.expect(false.B) // valid signal should be low.
      }

      // Helper function to transmit one byte (MSB first)
      def sendByte(value: Int): Unit = {

        // Start bit
        dut.io.rxd.poke(false.B) // start bit '0' for starting the receive sequence.
        dut.clock.step()

        // Send 8 data bits, MSB first
        for (i <- 7 to 0 by -1) {
          val bit = (value >> i) & 1  // shift the value then and by 0x01

          dut.io.rxd.poke(bit.B) // bit sent to the receiver.
          dut.clock.step()
        }

        // Check received byte
        dut.io.data.expect(value.U) // after all bits sent expect the 'data' out to be equal to 'value' transmitted.
        dut.io.valid.expect(true.B) // expect valid signal to be high.

      }

      // ----------------------------------
      // TC1: Idle bus
      // ----------------------------------

      initialize()

      for (i <- 0 until 10) {
        dut.io.rxd.poke(true.B)
        dut.clock.step()
        dut.io.valid.expect(false.B)
      }

      // ----------------------------------
      // TC2: Normal transmission
      // ----------------------------------

      sendByte(204) // binary "1100 1100 is transmitted"
      // Valid should only be high for one cycle
      dut.io.rxd.poke(true.B) // transmit 1 as a dummy value for this clock cycle.
      dut.clock.step() // clock step
      dut.io.valid.expect(false.B) // valid signal low.

      // ----------------------------------
      // TC3: All zeros
      // ----------------------------------

      sendByte(0)

      // ----------------------------------
      // TC4: All ones
      // ----------------------------------

      sendByte(255)

      // ----------------------------------
      // TC5: Alternating bits
      // ----------------------------------

      sendByte(170) // binary "1010 1010 is transmitted"

      // ----------------------------------
      // TC6: Incomplete transmission
      // ----------------------------------

      //initialize()

      // Start bit
      dut.io.rxd.poke(false.B)
      dut.clock.step()

      val num = 127

      // Only 7 data bits
      // for each bit starting from MSB shift number starting by 7 and then decrease the shift number
      // then and with '1' or '0000 0001' to have just last bit.
      for (i <- 0 to 6 by 1) {
        dut.io.rxd.poke(((num >> (7-i))&1).B)
        dut.clock.step()
      }

      dut.io.valid.expect(false.B) // valid should be low/false as only 7 bits are received
      dut.io.rxd.poke(((num)&1).B) // transmitted 8th bit
      dut.clock.step()
      dut.io.valid.expect(true.B) // now valid should be high/true
      dut.io.data.expect(num.U) // data should be equal to "num=127"

      // ----------------------------------
      // TC7: Back-to-back transmissions
      // ----------------------------------

      //initialize()

      val byte1 = 170 // First byte
      val byte2 = 85 // 2nd byte

      sendByte(byte1) // transmit 1st byte
      sendByte(byte2) // transmit 2nd byte

      // ----------------------------------
      // TC9: Reset during transmission
      // ----------------------------------

      //initialize()

      // Start transmission
      dut.io.rxd.poke(false.B)
      dut.clock.step()

      // Send only 3 data bits
      for (i <- 0 until 3) {
        dut.io.rxd.poke(true.B)
        dut.clock.step()
      }

      // Assert reset
      dut.io.reset_n.poke(true.B)
      dut.clock.step()

      dut.io.valid.expect(false.B)

      // Release reset
      dut.io.reset_n.poke(false.B)

      // ----------------------------------
      // TC10: Transmission after reset
      // ----------------------------------

      sendByte(204) // binary "1100 1100 is transmitted"

    }
  }
}

