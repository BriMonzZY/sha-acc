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


class ThetaTests extends AnyFlatSpec with ChiselScalatestTester {

  behavior of "Theta Module"

  it should "test the basic theta circuit" in {
  test(new ThetaModule(64))
    // .withAnnotations(Seq(VerilatorBackendAnnotation, WriteVcdAnnotation)) { c =>
    // .withAnnotations(Seq(VcsBackendAnnotation, WriteFsdbAnnotation)) { c =>
    // .withAnnotations(Seq(VcsBackendAnnotation)) { c =>
    .withAnnotations(Seq(VerilatorBackendAnnotation)) { c =>
      def ROTL(x: BigInt, y: Int, w: Int) = (((x) << (y)) | ((x) >> (w - (y))))
      val rand = new scala.util.Random(1)
      val w = 4
      val in_state = Array.fill(5*5){BigInt(rand.nextInt(1 <<w))} // random number between 0 ~ 15
      val out_state = Array.fill(5*5){BigInt(0)}
      val bc = Array.fill(5){BigInt(0)}

      // for(i <- 0 until 5) {
      //   bc(i) = in_state(0*5+i) ^ in_state(1*5+i) ^ in_state(2*5+i) ^ in_state(3*5+i) ^ in_state(4*5+i)
      // }
      for(i <- 0 until 5) {
        bc(i) = in_state(i*5+0) ^ in_state(i*5+1) ^ in_state(i*5+2) ^ in_state(i*5+3) ^ in_state(i*5+4)
      }

      for(i <- 0 until 5) {
        val t = bc((i+4)%5) ^ ROTL(bc((i+1)%5), 1, 64)
        // print("cxh debug: t(%d)=%d\n", i, t)
        for(j <- 0 until 5) {
          out_state(i*5+j) = in_state(i*5+j) ^ t
        }
      }

      for (i <- 0 until 25) {
        c.io.state_i(i).poke(in_state(i))
      }
      c.clock.step(1)
      for(i <- 0 until 25) {
        c.io.state_o(i).expect(out_state(i))
      }
    }
  }
}

