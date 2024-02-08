package sha3

import chisel3._
import chisel3.util._


class IotaModule(val w: Int = 64) extends Module {
  val io = IO(new Bundle {
    val round   = Input(UInt(5.W))
    val state_i = Input(Vec(25, UInt(w.W)))
    val state_o = Output(Vec(25, UInt(w.W)))
  })

  //TODO: c code uses look up table for this
  for(i <- 0 until 5) {
    for(j <- 0 until 5) {
      if(i !=0 || j!=0)
        io.state_o(i*5+j) := io.state_i(i*5+j)
    }
  }
  //val const = ROUND_CONST.value(io.round)
  val const = IOTA.round_const(io.round)
  io.state_o(0) := io.state_i(0) ^ const
/*
  io.state_o(0) := Cat(io.state_i(0)(63) ^ const(6),
                       io.state_i(0)(62,32),
                       io.state_i(0)(31) ^ const(5),
                       io.state_i(0)(30,16),
                       io.state_i(0)(15) ^ const(4),
                       io.state_i(0)(14,8),
                       io.state_i(0)( 7) ^ const(3),
                       io.state_i(0)(6,4),
                       io.state_i(0)( 3) ^ const(2),
                       io.state_i(0)( 2),
                       io.state_i(0)( 1) ^ const(1),
                       io.state_i(0)( 0) ^ const(0))
*/
}
