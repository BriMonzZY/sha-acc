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


class ChiTests extends AnyFlatSpec with ChiselScalatestTester {

  behavior of "Chi Module"

  it should "pass random test" in {
	test(new ChiModule(64))
    // .withAnnotations(Seq(VerilatorBackendAnnotation, WriteVcdAnnotation)) { c =>
    // .withAnnotations(Seq(VcsBackendAnnotation, WriteFsdbAnnotation)) { c =>
    // .withAnnotations(Seq(VcsBackendAnnotation)) { c =>
    .withAnnotations(Seq(VerilatorBackendAnnotation)) { c =>
      println("start test: ")
      val rand = new scala.util.Random(1)
      val w = 4
      val maxInt = 1 << (5*5*w)
      val state = Array.fill(5*5){BigInt(rand.nextInt(1 << w))}
      val out_state = Array.fill(5*5){BigInt(0)}
      for(i <- 0 until 5) {
        for(j <- 0 until 5) {
          // out_state(i*5+j) = state(i*5+j) ^ (~state(i*5+((j+1)%5)) & state(i*5+((j+2)%5)))
          out_state(i*5+j) = state(i*5+j) ^ ((~state(((i+1)%5)*5+((j)%5))) & state(((i+2)%5)*5+((j)%5)))
        }
      }
      for(i <- 0 until 25) {
        c.io.state_i(i).poke(state(i))
      }
      c.clock.step(1)
      for(i <- 0 until 25) {
        c.io.state_o(i).expect(out_state(i))
      }
	  }
  }
}
