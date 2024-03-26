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
