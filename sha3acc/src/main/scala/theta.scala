package sha3

import chisel3._
import chisel3.util._

/*
uint64_t t, bc[5];
for (i = 0; i < 5; i++)
  bc[i] = st[i] ^ st[i + 5] ^ st[i + 10] ^ st[i + 15] ^ st[i + 20];

for (i = 0; i < 5; i++) {
  t = bc[(i + 4) % 5] ^ ROTL64(bc[(i + 1) % 5], 1);
  for (j = 0; j < 25; j += 5)
    st[j + i] ^= t;
}
*/

class ThetaModule(val w: Int = 64) extends Module {
  val io = IO(new Bundle {
    val state_i = Input(Vec(25, UInt(w.W)))
    val state_o = Output(Vec(25, UInt(w.W)))
  })

  val bc = Wire(Vec(5, UInt(w.W)))
  for(i <- 0 until 5) {
    bc(i) := io.state_i(i*5+0) ^ io.state_i(i*5+1) ^ io.state_i(i*5+2) ^ io.state_i(i*5+3) ^ io.state_i(i*5+4)
  }
  for(i <- 0 until 5) {
    val t = Wire(UInt(w.W))
    t := bc((i+4)%5) ^ common.ROTL(bc((i+1)%5), 1.U, w.U)
    for (j <- 0 until 5) {
      io.state_o(i*5+j) := io.state_i(i*5+j) ^ t
    }
  }
}

class Parity extends Module {
  val io = new Bundle {
    val in = Input(Vec(5, Bool()))
    val res = Output(Bool())
  }
  io.res := io.in(0) ^ io.in(1) ^ io.in(2) ^ io.in(3) ^ io.in(4)
}
