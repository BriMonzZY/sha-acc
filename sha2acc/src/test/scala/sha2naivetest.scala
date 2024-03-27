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

class wcalcUnitTester(c: Wcalc) extends AnyFlatSpec with ChiselScalatestTester {
  val M = List(
    BigInt("61626364", 16), BigInt("62636465", 16), BigInt("63646566", 16), BigInt("64656667", 16),
    BigInt("65666768", 16), BigInt("66676869", 16), BigInt("6768696a", 16), BigInt("68696a6b", 16),
    BigInt("696a6b6c", 16), BigInt("6a6b6c6d", 16), BigInt("6b6c6d6e", 16), BigInt("6c6d6e6f", 16),
    BigInt("6d6e6f70", 16), BigInt("6e6f7071", 16), BigInt("80000000", 16), BigInt("00000000", 16)
  )

  def rotr(x: BigInt, n: Int): BigInt = {
    ((x >> n) | ( x << (32 - n))) & BigInt("ffffffff", 16)
  }

  def sig0(x: BigInt): BigInt = {
    (rotr(x, 7) ^ rotr(x, 18) ^ (x >> 3)) & BigInt("ffffffff", 16)
  }

  def sig1(x: BigInt): BigInt = {
    (rotr(x, 17) ^ rotr(x, 19) ^ (x >> 10)) & BigInt("ffffffff", 16)
  }
  def calcW(idx: Int): BigInt = {
    idx match {
      case idx if 0 until 16 contains idx => M(idx)
      case idx if 16 until 64 contains idx => (sig1(calcW(idx - 2)) + calcW(idx - 7) + sig0(calcW(idx - 15)) + calcW(idx - 16)) & BigInt("ffffffff", 16)
    }
  }

  // 输入消息
  c.io.W.M0.poke(M(0))
  c.io.W.M1.poke(M(1))
  c.io.W.M2.poke(M(2))
  c.io.W.M3.poke(M(3))
  c.io.W.M4.poke(M(4))
  c.io.W.M5.poke(M(5))
  c.io.W.M6.poke(M(6))
  c.io.W.M7.poke(M(7))
  c.io.W.M8.poke(M(8))
  c.io.W.M9.poke(M(9))
  c.io.W.M10.poke(M(10))
  c.io.W.M11.poke(M(11))
  c.io.W.M12.poke(M(12))
  c.io.W.M13.poke(M(13))
  c.io.W.M14.poke(M(14))
  c.io.W.M15.poke(M(15))
  c.io.ld.poke(true.B)
  c.io.inc.poke(false.B)
  c.clock.step(1)
  for (i <- 0 until 64) {
    c.io.ld.poke(false.B)
    c.io.inc.poke(true.B)
    c.io.out.expect(calcW(i))
    c.clock.step(1)
  }
}

class sha256UnitTester(c: Sha256Calc) extends AnyFlatSpec with ChiselScalatestTester {
  val M = List(
    BigInt("61626364", 16), BigInt("62636465", 16), BigInt("63646566", 16), BigInt("64656667", 16),
    BigInt("65666768", 16), BigInt("66676869", 16), BigInt("6768696a", 16), BigInt("68696a6b", 16),
    BigInt("696a6b6c", 16), BigInt("6a6b6c6d", 16), BigInt("6b6c6d6e", 16), BigInt("6c6d6e6f", 16),
    BigInt("6d6e6f70", 16), BigInt("6e6f7071", 16), BigInt("80000000", 16), BigInt("00000000", 16)
  )
  // 输入消息
  c.io.M.M0.poke(M(0))
  c.io.M.M1.poke(M(1))
  c.io.M.M2.poke(M(2))
  c.io.M.M3.poke(M(3))
  c.io.M.M4.poke(M(4))
  c.io.M.M5.poke(M(5))
  c.io.M.M6.poke(M(6))
  c.io.M.M7.poke(M(7))
  c.io.M.M8.poke(M(8))
  c.io.M.M9.poke(M(9))
  c.io.M.M10.poke(M(10))
  c.io.M.M11.poke(M(11))
  c.io.M.M12.poke(M(12))
  c.io.M.M13.poke(M(13))
  c.io.M.M14.poke(M(14))
  c.io.M.M15.poke(M(15))

  c.io.init.poke(false.B)
  c.io.start.poke(false.B)
  c.clock.step(2)

  // init
  c.io.init.poke(true.B)
  c.io.start.poke(false.B)
  c.clock.step(1)

  // start
  c.io.init.poke(false.B)
  c.io.start.poke(true.B)
  c.clock.step(1)

  c.io.start.poke(false.B)
  c.io.init.poke(false.B)
  c.clock.step(70)

  c.io.hout.a.expect(BigInt("85e655d6", 16))
  c.io.hout.b.expect(BigInt("417a1795", 16))
  c.io.hout.c.expect(BigInt("3363376a", 16))
  c.io.hout.d.expect(BigInt("624cde5c", 16))
  c.io.hout.e.expect(BigInt("76e09589", 16))
  c.io.hout.f.expect(BigInt("cac5f811", 16))
  c.io.hout.g.expect(BigInt("cc4b32c1", 16))
  c.io.hout.h.expect(BigInt("f20e533a", 16))

  // 输入第二部分消息
  c.io.M.M0.poke(BigInt("00000000", 16))
  c.io.M.M1.poke(BigInt("00000000", 16))
  c.io.M.M2.poke(BigInt("00000000", 16))
  c.io.M.M3.poke(BigInt("00000000", 16))
  c.io.M.M4.poke(BigInt("00000000", 16))
  c.io.M.M5.poke(BigInt("00000000", 16))
  c.io.M.M6.poke(BigInt("00000000", 16))
  c.io.M.M7.poke(BigInt("00000000", 16))
  c.io.M.M8.poke(BigInt("00000000", 16))
  c.io.M.M9.poke(BigInt("00000000", 16))
  c.io.M.M10.poke(BigInt("00000000", 16))
  c.io.M.M11.poke(BigInt("00000000", 16))
  c.io.M.M12.poke(BigInt("00000000", 16))
  c.io.M.M13.poke(BigInt("00000000", 16))
  c.io.M.M14.poke(BigInt("00000000", 16))
  c.io.M.M15.poke(BigInt("000001c0", 16))

  // start
  c.io.init.poke(false.B)
  c.io.start.poke(true.B)
  c.clock.step(1)

  c.io.start.poke(false.B)
  c.io.init.poke(false.B)
  c.clock.step(70)

  c.io.hout.a.expect(BigInt("248d6a61", 16))
  c.io.hout.b.expect(BigInt("d20638b8", 16))
  c.io.hout.c.expect(BigInt("e5c02693", 16))
  c.io.hout.d.expect(BigInt("0c3e6039", 16))
  c.io.hout.e.expect(BigInt("a33ce459", 16))
  c.io.hout.f.expect(BigInt("64ff2167", 16))
  c.io.hout.g.expect(BigInt("f6ecedd4", 16))
  c.io.hout.h.expect(BigInt("19db06c1", 16))
}

class sha256NaiveUnitTester(c: Sha256Naive) extends AnyFlatSpec with ChiselScalatestTester {
  def writeNaive(address: BigInt, data: BigInt): UInt = {
    c.io.slave.addr.poke(address >> 2)
    c.io.slave.wdata.poke(data)
    c.io.slave.wr.poke(true.B)
    c.clock.step(1)
    c.io.slave.wr.poke(false.B)
    return 0.U
  }

  def readNaive(address: BigInt, expected: BigInt): UInt = {
    c.io.slave.addr.poke(address >> 2)
    c.io.slave.rd.poke(true.B)
    c.clock.step(1)
    c.io.slave.rd.poke(false.B)
    c.io.slave.rdata.expect(expected)
    return 0.U
  }

  writeNaive(BigInt("00", 16), BigInt("01", 16))
  writeNaive(BigInt("00", 16), BigInt("00", 16))

  // 输入消息
  writeNaive(BigInt("04", 16), BigInt("61626364", 16))
  writeNaive(BigInt("08", 16), BigInt("62636465", 16))
  writeNaive(BigInt("0C", 16), BigInt("63646566", 16))
  writeNaive(BigInt("10", 16), BigInt("64656667", 16))
  writeNaive(BigInt("14", 16), BigInt("65666768", 16))
  writeNaive(BigInt("18", 16), BigInt("66676869", 16))
  writeNaive(BigInt("1C", 16), BigInt("6768696a", 16))
  writeNaive(BigInt("20", 16), BigInt("68696a6b", 16))
  writeNaive(BigInt("24", 16), BigInt("696a6b6c", 16))
  writeNaive(BigInt("28", 16), BigInt("6a6b6c6d", 16))
  writeNaive(BigInt("2C", 16), BigInt("6b6c6d6e", 16))
  writeNaive(BigInt("30", 16), BigInt("6c6d6e6f", 16))
  writeNaive(BigInt("34", 16), BigInt("6d6e6f70", 16))
  writeNaive(BigInt("38", 16), BigInt("6e6f7071", 16))
  writeNaive(BigInt("3C", 16), BigInt("80000000", 16))
  writeNaive(BigInt("40", 16), BigInt("00000000", 16))

  writeNaive(BigInt("00", 16), BigInt("02", 16))
  writeNaive(BigInt("00", 16), BigInt("00", 16))
  c.clock.step(70)
  readNaive(BigInt("00", 16), 4)
  readNaive(BigInt("44", 16), BigInt("85e655d6", 16))
  readNaive(BigInt("48", 16), BigInt("417a1795", 16))
  readNaive(BigInt("4C", 16), BigInt("3363376a", 16))
  readNaive(BigInt("50", 16), BigInt("624cde5c", 16))
  readNaive(BigInt("54", 16), BigInt("76e09589", 16))
  readNaive(BigInt("58", 16), BigInt("cac5f811", 16))
  readNaive(BigInt("5C", 16), BigInt("cc4b32c1", 16))
  readNaive(BigInt("60", 16), BigInt("f20e533a", 16))

  writeNaive(BigInt("04", 16), BigInt("00000000", 16))
  writeNaive(BigInt("08", 16), BigInt("00000000", 16))
  writeNaive(BigInt("0C", 16), BigInt("00000000", 16))
  writeNaive(BigInt("10", 16), BigInt("00000000", 16))
  writeNaive(BigInt("14", 16), BigInt("00000000", 16))
  writeNaive(BigInt("18", 16), BigInt("00000000", 16))
  writeNaive(BigInt("1C", 16), BigInt("00000000", 16))
  writeNaive(BigInt("20", 16), BigInt("00000000", 16))
  writeNaive(BigInt("24", 16), BigInt("00000000", 16))
  writeNaive(BigInt("28", 16), BigInt("00000000", 16))
  writeNaive(BigInt("2C", 16), BigInt("00000000", 16))
  writeNaive(BigInt("30", 16), BigInt("00000000", 16))
  writeNaive(BigInt("34", 16), BigInt("00000000", 16))
  writeNaive(BigInt("38", 16), BigInt("00000000", 16))
  writeNaive(BigInt("3C", 16), BigInt("00000000", 16))
  writeNaive(BigInt("40", 16), BigInt("000001c0", 16))

  writeNaive(BigInt("00", 16), BigInt("02", 16))
  writeNaive(BigInt("00", 16), BigInt("00", 16))
  c.clock.step(70)
  readNaive(BigInt("00", 16), 4)
  readNaive(BigInt("44", 16), BigInt("248d6a61", 16))
  readNaive(BigInt("48", 16), BigInt("d20638b8", 16))
  readNaive(BigInt("4C", 16), BigInt("e5c02693", 16))
  readNaive(BigInt("50", 16), BigInt("0c3e6039", 16))
  readNaive(BigInt("54", 16), BigInt("a33ce459", 16))
  readNaive(BigInt("58", 16), BigInt("64ff2167", 16))
  readNaive(BigInt("5C", 16), BigInt("f6ecedd4", 16))
  readNaive(BigInt("60", 16), BigInt("19db06c1", 16))

  c.clock.step(4)
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

  it should "W calculator (with verilator backend)" in {
    test(new Wcalc)
    .withAnnotations(Seq(VerilatorBackendAnnotation)) { c =>
      new wcalcUnitTester(c)
    }
  }

  it should "SHA256 Calculator (with verilator backend)" in {
    test(new Sha256Calc)
    .withAnnotations(Seq(VerilatorBackendAnnotation)) { c =>
    // .withAnnotations(Seq(VcsBackendAnnotation)) { c =>
      new sha256UnitTester(c)
    }
  }

  it should "SHA256 Naive calculator (with verilator backend)" in {
    test(new Sha256Naive)
    .withAnnotations(Seq(VerilatorBackendAnnotation)) { c =>
      new sha256NaiveUnitTester(c)
    }
  }
}
