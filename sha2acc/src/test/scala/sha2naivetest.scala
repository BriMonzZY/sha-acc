package sha2

import chisel3._
import chisel3.util._
import chisel3.testers._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec


class calculateEUnitTester(c: => calculateE) extends BasicTester {
  val dut = Module(c)
  dut.io.d := BigInt("a54ff53a", 16).asUInt
  dut.io.e := BigInt("510e527f", 16).asUInt
  dut.io.f := BigInt("9b05688c", 16).asUInt
  dut.io.g := BigInt("1f83d9ab", 16).asUInt
  dut.io.h := BigInt("5be0cd19", 16).asUInt
  dut.io.Kt := BigInt("428a2f98", 16).asUInt
  dut.io.Wt := BigInt("61626364", 16).asUInt
  assert(dut.io.out === BigInt("fa2a4606", 16).asUInt)
  stop()
}

class calculateAUnitTester(c: => calculateA) extends BasicTester {
  val dut = Module(c)
  dut.io.a := BigInt("6a09e667", 16).asUInt
  dut.io.b := BigInt("bb67ae85", 16).asUInt
  dut.io.c := BigInt("3c6ef372", 16).asUInt
  dut.io.d := BigInt("a54ff53a", 16).asUInt
  dut.io.e := BigInt("510e527f", 16).asUInt
  dut.io.f := BigInt("9b05688c", 16).asUInt
  dut.io.g := BigInt("1f83d9ab", 16).asUInt
  dut.io.h := BigInt("5be0cd19", 16).asUInt
  dut.io.Kt := BigInt("428a2f98", 16).asUInt
  dut.io.Wt := BigInt("61626364", 16).asUInt
  assert(dut.io.out === BigInt("5d6aebb1", 16).asUInt)
  stop()
}


class SHA2NaiveTest extends AnyFlatSpec with ChiselScalatestTester {

  behavior of "SHA2 Naive immplementation Test"

  it should "calculate E value of sha256 (with verilator backend)" in {
    test(new calculateEUnitTester(new calculateE))
    .withAnnotations(Seq(VerilatorBackendAnnotation))
    .runUntilStop()
  }

  it should "calculate A value of sha256 (with verilator backend)" in {
    test(new calculateAUnitTester(new calculateA))
    .withAnnotations(Seq(VerilatorBackendAnnotation))
    .runUntilStop()
  }
}
