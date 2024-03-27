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
  assert(dut.io.out === BigInt("fa2a4606", 16).asUInt, "dut value: %x\n", dut.io.out)
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

class calculateStepUnitTester(c: => calculateStep) extends BasicTester {
  val dut = Module(c)
  dut.io.hin.a := BigInt("6a09e667", 16).asUInt
  dut.io.hin.b := BigInt("bb67ae85", 16).asUInt
  dut.io.hin.c := BigInt("3c6ef372", 16).asUInt
  dut.io.hin.d := BigInt("a54ff53a", 16).asUInt
  dut.io.hin.e := BigInt("510e527f", 16).asUInt
  dut.io.hin.f := BigInt("9b05688c", 16).asUInt
  dut.io.hin.g := BigInt("1f83d9ab", 16).asUInt
  dut.io.hin.h := BigInt("5be0cd19", 16).asUInt
  dut.io.Kt := BigInt("428a2f98", 16).asUInt
  dut.io.Wt := BigInt("61626364", 16).asUInt
  assert(dut.io.hout.a === BigInt("5d6aebb1", 16).asUInt)
  assert(dut.io.hout.b === BigInt("6a09e667", 16).asUInt)
  assert(dut.io.hout.c === BigInt("bb67ae85", 16).asUInt)
  assert(dut.io.hout.d === BigInt("3c6ef372", 16).asUInt)
  assert(dut.io.hout.e === BigInt("fa2a4606", 16).asUInt)
  assert(dut.io.hout.f === BigInt("510e527f", 16).asUInt)
  assert(dut.io.hout.g === BigInt("9b05688c", 16).asUInt)
  assert(dut.io.hout.h === BigInt("1f83d9ab", 16).asUInt)
  stop()
}

class calculateAllUnitTester(c: calculateAll) extends AnyFlatSpec with ChiselScalatestTester {
  c.io.hin.a.poke(BigInt("6a09e667", 16).asUInt)
  c.io.hin.b.poke(BigInt("bb67ae85", 16).asUInt)
  c.io.hin.c.poke(BigInt("3c6ef372", 16).asUInt)
  c.io.hin.d.poke(BigInt("a54ff53a", 16).asUInt)
  c.io.hin.e.poke(BigInt("510e527f", 16).asUInt)
  c.io.hin.f.poke(BigInt("9b05688c", 16).asUInt)
  c.io.hin.g.poke(BigInt("1f83d9ab", 16).asUInt)
  c.io.hin.h.poke(BigInt("5be0cd19", 16).asUInt)

  c.io.M.M0.poke(BigInt("61626364", 16).asUInt)
  c.io.M.M1.poke(BigInt("62636465", 16).asUInt)
  c.io.M.M2.poke(BigInt("63646566", 16).asUInt)
  c.io.M.M3.poke(BigInt("64656667", 16).asUInt)
  c.io.M.M4.poke(BigInt("65666768", 16).asUInt)
  c.io.M.M5.poke(BigInt("66676869", 16).asUInt)
  c.io.M.M6.poke(BigInt("6768696a", 16).asUInt)
  c.io.M.M7.poke(BigInt("68696a6b", 16).asUInt)
  c.io.M.M8.poke(BigInt("696a6b6c", 16).asUInt)
  c.io.M.M9.poke(BigInt("6a6b6c6d", 16).asUInt)
  c.io.M.M10.poke(BigInt("6b6c6d6e", 16).asUInt)
  c.io.M.M11.poke(BigInt("6c6d6e6f", 16).asUInt)
  c.io.M.M12.poke(BigInt("6d6e6f70", 16).asUInt)
  c.io.M.M13.poke(BigInt("6e6f7071", 16).asUInt)
  c.io.M.M14.poke(BigInt("80000000", 16).asUInt)
  c.io.M.M15.poke(BigInt("00000000", 16).asUInt)

  c.clock.step(1)

  c.io.hout.a.expect(BigInt("c3486194", 16).asUInt)
  c.io.hout.b.expect(BigInt("dd16cbb3", 16).asUInt)
  c.io.hout.c.expect(BigInt("d68e6457", 16).asUInt)
  c.io.hout.d.expect(BigInt("101a4861", 16).asUInt)
  c.io.hout.e.expect(BigInt("1496a54f", 16).asUInt)
  c.io.hout.f.expect(BigInt("9162aded", 16).asUInt)
  c.io.hout.g.expect(BigInt("9243f8af", 16).asUInt)
  c.io.hout.h.expect(BigInt("839a0fc9", 16).asUInt)
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

  it should "calculate sha256 step (with verilator backend)" in {
    test(new calculateStepUnitTester(new calculateStep))
    .withAnnotations(Seq(VerilatorBackendAnnotation))
    .runUntilStop()
  }

  it should "calculate sha256 ALL (with verilator backend)" in {
    test(new calculateAll(16))
    .withAnnotations(Seq(VerilatorBackendAnnotation)) { c =>
      new calculateAllUnitTester(c)
    }
  }
}
