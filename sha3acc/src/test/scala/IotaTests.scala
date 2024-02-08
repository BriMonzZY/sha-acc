package sha3

import chisel3._
import chisel3.util._
import chisel3.testers._
import chiseltest._
import org.chipsalliance.cde.config._
import freechips.rocketchip.tile._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.rocket._
import org.scalatest.flatspec.AnyFlatSpec


class IotaTests extends AnyFlatSpec with ChiselScalatestTester {

  behavior of "Iota Module"

  it should "test the basic iota circuit" in {
	test(new IotaModule(64))
    // .withAnnotations(Seq(VerilatorBackendAnnotation, WriteVcdAnnotation)) { c =>
    // .withAnnotations(Seq(VcsBackendAnnotation, WriteFsdbAnnotation)) { c =>
    // .withAnnotations(Seq(VcsBackendAnnotation)) { c =>
    .withAnnotations(Seq(VerilatorBackendAnnotation)) { c =>
      val w = 64
      val maxInt = 1 << (5*5*w)
      val round = 0
      val state = Array.fill(5*5){BigInt(3)}
      val out_state = Array.fill(5*5){BigInt(3)}

      out_state(0) = state(0) ^ BigInt(1)

      for(i <- 0 until 25) {
        c.io.state_i(i).poke(state(i))
      }
      c.io.round.poke(round)
      c.clock.step(1)
      for(i <- 0 until 25) {
        c.io.state_o(i).expect(out_state(i))
      }
	  }
  }
}
