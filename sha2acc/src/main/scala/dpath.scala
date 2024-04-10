package sha2

import chisel3._
import chisel3.util._
import org.chipsalliance.cde.config.Parameters

class Sha2DpathModule(val w: Int)(implicit p: Parameters) extends Module {

  val io = IO(new Bundle {
    val init = Input(Bool())
    val start = Input(Bool())
    val outvalid = Output(Bool())
    val message_in = new MesIO
    val hash_out = Flipped(new Hio)
  })

  val calc = Module(new Sha256Calc)

  calc.io.init := io.init
  io.outvalid := calc.io.outvalid
  calc.io.start := io.start
  calc.io.M <> io.message_in
  io.hash_out <> calc.io.hout
  dontTouch(io.hash_out)
}
