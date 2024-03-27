package sha2

import chisel3._
import chisel3.util._


trait ChMath {
  def ch(x: UInt, y: UInt, z: UInt): UInt = {
    (x & y) ^ ((~x).asUInt & z)
  }

  def maj(x: UInt, y: UInt, z: UInt): UInt = {
    (x & y) ^ (x & z) ^ (y & z)
  }

  def rotr(x: UInt, n: Int): UInt = {
    (x >> n).asUInt | ( x << (32 - n)).asUInt
  }

  def sum0(x: UInt): UInt = {
    rotr(x, 2) ^ rotr(x, 13) ^ rotr(x, 22)
  }

  def sum1(x: UInt): UInt = {
    rotr(x, 6) ^ rotr(x, 11) ^ rotr(x, 25)
  }

  def t1(x: UInt, y: UInt, z: UInt, v: UInt, k: UInt, w: UInt): UInt = {
    v + sum1(x) + ch(x, y, z) + k + w
  }

  def t2(x: UInt, y: UInt, z: UInt): UInt = {
    sum0(x) + maj(x, y, z)
  }

  def sig0(x: UInt): UInt = {
    rotr(x, 7) ^ rotr(x, 18) ^ (x >> 3).asUInt
  }

  def sig1(x: UInt): UInt = {
    rotr(x, 17) ^ rotr(x, 19) ^ (x >> 10).asUInt
  }
}

// E = D + t1
class calculateE extends Module with ChMath {
  val io = IO(new Bundle {
    val d  = Input(UInt(32.W))
    val e  = Input(UInt(32.W))
    val f  = Input(UInt(32.W))
    val g  = Input(UInt(32.W))
    val h  = Input(UInt(32.W))
    val Kt = Input(UInt(32.W))
    val Wt = Input(UInt(32.W))
    val out = Output(UInt(32.W))
  })

  io.out := io.d + t1(io.e, io.f, io.g, io.h, io.Kt, io.Wt)
}

// A = t1 + t2
class calculateA extends Module with ChMath {
  val io = IO(new Bundle {
    val a  = Input(UInt(32.W))
    val b  = Input(UInt(32.W))
    val c  = Input(UInt(32.W))
    val d  = Input(UInt(32.W))
    val e  = Input(UInt(32.W))
    val f  = Input(UInt(32.W))
    val g  = Input(UInt(32.W))
    val h  = Input(UInt(32.W))
    val Kt = Input(UInt(32.W))
    val Wt = Input(UInt(32.W))
    val out = Output(UInt(32.W))
  })

  io.out := t1(io.e, io.f, io.g, io.h, io.Kt, io.Wt) + t2(io.a, io.b, io.c)
}

class Hio extends Bundle {
  val a = Input(UInt(32.W))
  val b = Input(UInt(32.W))
  val c = Input(UInt(32.W))
  val d = Input(UInt(32.W))
  val e = Input(UInt(32.W))
  val f = Input(UInt(32.W))
  val g = Input(UInt(32.W))
  val h = Input(UInt(32.W))
}

// 将消息分成了16个32位的字，每个字作为一个输入端口
class MesIO extends Bundle {
  val M0 =Input(UInt(32.W))
  val M1 =Input(UInt(32.W))
  val M2 =Input(UInt(32.W))
  val M3 =Input(UInt(32.W))
  val M4 =Input(UInt(32.W))
  val M5 =Input(UInt(32.W))
  val M6 =Input(UInt(32.W))
  val M7 =Input(UInt(32.W))
  val M8 =Input(UInt(32.W))
  val M9 =Input(UInt(32.W))
  val M10 =Input(UInt(32.W))
  val M11 =Input(UInt(32.W))
  val M12 =Input(UInt(32.W))
  val M13 =Input(UInt(32.W))
  val M14 =Input(UInt(32.W))
  val M15 =Input(UInt(32.W))


  def apply(idx: Int): UInt = {
    idx match {
      case 0 => M0
      case 1 => M1
      case 2 => M2
      case 3 => M3
      case 4 => M4
      case 5 => M5
      case 6 => M6
      case 7 => M7
      case 8 => M8
      case 9 => M9
      case 10 => M10
      case 11 => M11
      case 12 => M12
      case 13 => M13
      case 14 => M14
      case 15 => M15
      case _ => 0.U
    }
  }
}

class calculateStep extends Module with ChMath {
  val io = IO(new Bundle {
    val Kt = Input(UInt(32.W))
    val Wt = Input(UInt(32.W))
    val hin = new Hio
    val hout = Flipped(new Hio)
  })

  io.hout.a := t1(io.hin.e, io.hin.f, io.hin.g, io.hin.h, io.Kt, io.Wt) + t2(io.hin.a, io.hin.b, io.hin.c)//a
  io.hout.b := io.hin.a//b
  io.hout.c := io.hin.b//c
  io.hout.d := io.hin.c//d
  io.hout.e := io.hin.d + t1(io.hin.e, io.hin.f, io.hin.g, io.hin.h, io.Kt, io.Wt)//e
  io.hout.f := io.hin.e//f
  io.hout.g := io.hin.f//g
  io.hout.h := io.hin.g//h
}

object sha256K {
  val values = List(
    "428a2f98", "71374491", "b5c0fbcf", "e9b5dba5", "3956c25b", "59f111f1", "923f82a4", "ab1c5ed5",
    "d807aa98", "12835b01", "243185be", "550c7dc3", "72be5d74", "80deb1fe", "9bdc06a7", "c19bf174",
    "e49b69c1", "efbe4786", "0fc19dc6", "240ca1cc", "2de92c6f", "4a7484aa", "5cb0a9dc", "76f988da",
    "983e5152", "a831c66d", "b00327c8", "bf597fc7", "c6e00bf3", "d5a79147", "06ca6351", "14292967",
    "27b70a85", "2e1b2138", "4d2c6dfc", "53380d13", "650a7354", "766a0abb", "81c2c92e", "92722c85",
    "a2bfe8a1", "a81a664b", "c24b8b70", "c76c51a3", "d192e819", "d6990624", "f40e3585", "106aa070",
    "19a4c116", "1e376c08", "2748774c", "34b0bcb5", "391c0cb3", "4ed8aa4a", "5b9cca4f", "682e6ff3",
    "748f82ee", "78a5636f", "84c87814", "8cc70208", "90befffa", "a4506ceb", "bef9a3f7", "c67178f2")
  def apply(x: Int): UInt = {
    BigInt(values(x), 16).asUInt(32.W)
  }
}

class calculateAll(stages: Int) extends Module with ChMath {
  val io = IO(new Bundle {
    val hin = new Hio
    val hout = Flipped(new Hio)
    val M = new MesIO
  })
  def calcW(idx: Int): UInt = {
    val m = idx match {
      case idx if 0 until 16 contains idx => io.M(idx)
      case idx if 16 until 63 contains idx => sig1(calcW(idx-2)) + calcW(idx-7) + sig0(calcW(idx-15)) + calcW(idx-16)
    }
    return m
  }
  val consts = sha256K
  val steps = Seq.fill(stages)(Module(new calculateStep()).io)
  steps(0).hin := io.hin
  steps(0).Kt := consts(0)
  steps(0).Wt := calcW(0)
  for(i <- 1 until stages) {
    steps(i).hin := steps(i - 1).hout
    steps(i).Kt := consts(i)
    steps(i).Wt := calcW(i)
  }
  io.hout := steps(stages - 1).hout
}

class HregisterIn extends Module {
  val io = IO(new Bundle {
    val hin = new Hio // 输入 a b c ...
    val hout = Flipped(new Hio) // 输出 a b c ...
    val inc = Input(Bool())
    val ld = Input(Bool())
    val init = Input(Bool())
    val start = Input(Bool())
  })

  def Reg32(value: String): UInt = {
    RegInit(BigInt(value, 16).asUInt)
  }

  val a = Reg32("6a09e667")
  val b = Reg32("bb67ae85")
  val c = Reg32("3c6ef372")
  val d = Reg32("a54ff53a")
  val e = Reg32("510e527f")
  val f = Reg32("9b05688c")
  val g = Reg32("1f83d9ab")
  val h = Reg32("5be0cd19")

  val a1 = Reg32("6a09e667")
  val b1 = Reg32("bb67ae85")
  val c1 = Reg32("3c6ef372")
  val d1 = Reg32("a54ff53a")
  val e1 = Reg32("510e527f")
  val f1 = Reg32("9b05688c")
  val g1 = Reg32("1f83d9ab")
  val h1 = Reg32("5be0cd19")

  def RV(value: String): UInt = {
    BigInt(value, 16).asUInt
  }
  val Hinit = RegInit(VecInit(Seq(
    "6a09e667",
    "bb67ae85",
    "3c6ef372",
    "a54ff53a",
    "510e527f",
    "9b05688c",
    "1f83d9ab",
    "5be0cd19") map RV))

  io.hout.a <> a
  io.hout.b <> b
  io.hout.c <> c
  io.hout.d <> d
  io.hout.e <> e
  io.hout.f <> f
  io.hout.g <> g
  io.hout.h <> h

  when(io.init) {
    a := Hinit(0)
    b := Hinit(1)
    c := Hinit(2)
    d := Hinit(3)
    e := Hinit(4)
    f := Hinit(5)
    g := Hinit(6)
    h := Hinit(7)

    a1 := Hinit(0)
    b1 := Hinit(1)
    c1 := Hinit(2)
    d1 := Hinit(3)
    e1 := Hinit(4)
    f1 := Hinit(5)
    g1 := Hinit(6)
    h1 := Hinit(7)

  }.elsewhen(io.inc) {
    a := io.hin.a
    b := io.hin.b
    c := io.hin.c
    d := io.hin.d
    e := io.hin.e
    f := io.hin.f
    g := io.hin.g
    h := io.hin.h
  }
  when(io.ld) {
    a := a1 + a
    b := b1 + b
    c := c1 + c
    d := d1 + d
    e := e1 + e
    f := f1 + f
    g := g1 + g
    h := h1 + h
  }

  when(io.start) {
    a1 := a
    b1 := b
    c1 := c
    d1 := d
    e1 := e
    f1 := f
    g1 := g
    h1 := h
  }
}

class Kmemory extends Module {
  val io = IO(new Bundle {
    val K = Output(UInt(32.W))
    val A = Input(UInt(6.W))
  })
  def RV(value: String): UInt = {
    BigInt(value, 16).asUInt
  }
  val Kmem = RegInit(VecInit(Seq(
    "428a2f98", "71374491", "b5c0fbcf", "e9b5dba5", "3956c25b", "59f111f1", "923f82a4", "ab1c5ed5",
    "d807aa98", "12835b01", "243185be", "550c7dc3", "72be5d74", "80deb1fe", "9bdc06a7", "c19bf174",
    "e49b69c1", "efbe4786", "0fc19dc6", "240ca1cc", "2de92c6f", "4a7484aa", "5cb0a9dc", "76f988da",
    "983e5152", "a831c66d", "b00327c8", "bf597fc7", "c6e00bf3", "d5a79147", "06ca6351", "14292967",
    "27b70a85", "2e1b2138", "4d2c6dfc", "53380d13", "650a7354", "766a0abb", "81c2c92e", "92722c85",
    "a2bfe8a1", "a81a664b", "c24b8b70", "c76c51a3", "d192e819", "d6990624", "f40e3585", "106aa070",
    "19a4c116", "1e376c08", "2748774c", "34b0bcb5", "391c0cb3", "4ed8aa4a", "5b9cca4f", "682e6ff3",
    "748f82ee", "78a5636f", "84c87814", "8cc70208", "90befffa", "a4506ceb", "bef9a3f7", "c67178f2") map RV))
  io.K := Kmem(io.A)
}

// 计算 Wt
class Wcalc extends Module with ChMath {
  val io = IO(new Bundle {
    val W = new MesIO
    val ld = Input(Bool())
    val inc = Input(Bool())
    val out = Output(UInt(32.W)) // 输出当前的 W 值
  })
  val Wreg = RegInit(VecInit(Seq(
    0.U(32.W), 0.U(32.W), 0.U(32.W), 0.U(32.W),
    0.U(32.W), 0.U(32.W), 0.U(32.W), 0.U(32.W),
    0.U(32.W), 0.U(32.W), 0.U(32.W), 0.U(32.W),
    0.U(32.W), 0.U(32.W), 0.U(32.W), 0.U(32.W)
  )))
  io.out <> Wreg(0)
  when(io.ld) { // 初始化时将消息的值加载到 W 中
    for(i <- 0 until 16) {
      Wreg(i) := io.W(i)
    }
  } .elsewhen(io.inc)  {
    for(i <- 0 until 15) { // 移位 Wt 来更新 Wt 丢弃已经计算并且没用的 Wt
      Wreg(i) := Wreg(i + 1)
    }
    Wreg(15) := sig1(Wreg(14)) + Wreg(9) + sig0(Wreg(1)) + Wreg(0) // W16
  }
}

class Sha256Calc extends Module {
  val io = IO(new Bundle {
    val M = new MesIO // 消息输入
    val hout = Flipped(new Hio) // 结果输出 a b c d ... h
    val start = Input(Bool())
    val ready = Output(Bool())
    val init = Input(Bool())
  })
  val H0 = Module(new HregisterIn)
  val Kt = Module(new Kmemory)
  val Wt = Module(new Wcalc)
  val step = Module(new calculateStep)
  val s_idle :: s_work :: Nil = Enum(2)
  val count = RegInit(0.U(7.W))
  val state = RegInit(s_idle)
  val ready = RegInit(false.B)
  val inc = RegInit(false.B)  // 状态为 s_work 时使能
  val h0ld = RegInit(false.B)  // 状态从 s_work 切换到 s_idle 时使能一个周期

  H0.io.ld := h0ld
  step.io.Kt <> Kt.io.K
  Kt.io.A <> count
  step.io.Wt <> Wt.io.out
  Wt.io.W <> io.M
  Wt.io.ld <> (io.start & (state === s_idle)) // 状态为 s_idle 并且 start 信号有效时使能
  H0.io.init <> io.init
  step.io.hin := H0.io.hout
  H0.io.hin <> step.io.hout
  io.ready <> ready
  Wt.io.inc <> inc
  H0.io.inc <> inc
  H0.io.start <> (io.start & (state === s_idle)) // 状态为 s_idle 并且 start 信号有效时使能

  io.hout <> H0.io.hout

  switch(state) {
    is (s_idle) {
      inc := false.B
      ready := true.B
      h0ld := false.B
      count := 0x0.U(6.W)
      when(io.start) {
        state := s_work
        ready := false.B
        inc := true.B
      }
    }
    is (s_work) {
      count := count + 1.U(6.W)
      when(io.init) {
        state := s_idle
      }
      when(count === 63.U) {
        state := s_idle
        count := 0.U
        inc := false.B
        h0ld:=true.B
      }
    }
  }
}

class NaiveSlave(aw: Int, dw: Int) extends Bundle {
  val addr = Input(UInt(aw.W))
  val rd = Input(Bool())
  val rdata = Output(UInt(dw.W))
  val wr = Input(Bool())
  val wdata = Input(UInt(dw.W))
}

class Sha256Naive extends Module {
  val io = IO(new Bundle {
    val slave = new NaiveSlave(6, 32)
  })
  val rdata = RegInit(0.U(32.W))
  val wdata = RegInit(0.U(32.W))
  val base_index = RegInit(0.U(8.W))

  val calc = Module(new Sha256Calc)
  val Mreg0 = RegInit(0.U(32.W))
  val Mreg1 = RegInit(0.U(32.W))
  val Mreg2 = RegInit(0.U(32.W))
  val Mreg3 = RegInit(0.U(32.W))
  val Mreg4 = RegInit(0.U(32.W))
  val Mreg5 = RegInit(0.U(32.W))
  val Mreg6 = RegInit(0.U(32.W))
  val Mreg7 = RegInit(0.U(32.W))
  val Mreg8 = RegInit(0.U(32.W))
  val Mreg9 = RegInit(0.U(32.W))
  val Mreg10 = RegInit(0.U(32.W))
  val Mreg11 = RegInit(0.U(32.W))
  val Mreg12 = RegInit(0.U(32.W))
  val Mreg13 = RegInit(0.U(32.W))
  val Mreg14 = RegInit(0.U(32.W))
  val Mreg15 = RegInit(0.U(32.W))
  val init = RegInit(false.B)
  val start = RegInit(0.U(2.W))

  io.slave.rdata := rdata
  calc.io.init := init
  calc.io.start := start(0) | start(1)
  calc.io.M(0) := Mreg0
  calc.io.M(1) := Mreg1
  calc.io.M(2) := Mreg2
  calc.io.M(3) := Mreg3
  calc.io.M(4) := Mreg4
  calc.io.M(5) := Mreg5
  calc.io.M(6) := Mreg6
  calc.io.M(7) := Mreg7
  calc.io.M(8) := Mreg8
  calc.io.M(9) := Mreg9
  calc.io.M(10) := Mreg10
  calc.io.M(11) := Mreg11
  calc.io.M(12) := Mreg12
  calc.io.M(13) := Mreg13
  calc.io.M(14) := Mreg14
  calc.io.M(15) := Mreg15
  init := 0.U
  start := (start >> 1.U).asUInt

  when(io.slave.wr) {
    base_index := io.slave.addr
    wdata := io.slave.wdata
    switch(base_index) {
      is(0.U) {
        init := wdata(0)
        when(wdata(1) === true.B) {
          start := 3.U(2.W)
        } .otherwise {
          start := 0.U(2.W)
        }
      }
      is(1.U) {Mreg0 := wdata}
      is(2.U) {Mreg1 := wdata}
      is(3.U) {Mreg2 := wdata}
      is(4.U) {Mreg3 := wdata}
      is(5.U) {Mreg4 := wdata}
      is(6.U) {Mreg5 := wdata}
      is(7.U) {Mreg6 := wdata}
      is(8.U) {Mreg7 := wdata}
      is(9.U) {Mreg8 := wdata}
      is(10.U) {Mreg9 := wdata}
      is(11.U) {Mreg10 := wdata}
      is(12.U) {Mreg11 := wdata}
      is(13.U) {Mreg12 := wdata}
      is(14.U) {Mreg13 := wdata}
      is(15.U) {Mreg14 := wdata}
      is(16.U) {Mreg15 := wdata}
    }
  } .elsewhen(io.slave.rd) {
    switch(io.slave.addr) {
      is(0.U) {
        rdata := Cat(calc.io.ready, start(0) | start(1), init)
      }
      is(1.U) {rdata := Mreg0}
      is(2.U) {rdata := Mreg1}
      is(3.U) {rdata := Mreg2}
      is(4.U) {rdata := Mreg3}
      is(5.U) {rdata := Mreg4}
      is(6.U) {rdata := Mreg5}
      is(7.U) {rdata := Mreg6}
      is(8.U) {rdata := Mreg7}
      is(9.U) {rdata := Mreg8}
      is(10.U) {rdata := Mreg9}
      is(11.U) {rdata := Mreg10}
      is(12.U) {rdata := Mreg11}
      is(13.U) {rdata := Mreg12}
      is(14.U) {rdata := Mreg13}
      is(15.U) {rdata := Mreg14}
      is(16.U) {rdata := Mreg15}
      is(17.U) {rdata := calc.io.hout.a}
      is(18.U) {rdata := calc.io.hout.b}
      is(19.U) {rdata := calc.io.hout.c}
      is(20.U) {rdata := calc.io.hout.d}
      is(21.U) {rdata := calc.io.hout.e}
      is(22.U) {rdata := calc.io.hout.f}
      is(23.U) {rdata := calc.io.hout.g}
      is(24.U) {rdata := calc.io.hout.h}
    }
  }
}
