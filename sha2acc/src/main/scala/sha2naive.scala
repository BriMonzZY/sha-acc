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

