package sha2

import chisel3._
import chisel3.util._
import org.chipsalliance.cde.config.Parameters

class Sha2DpathModule(val w: Int)(implicit p: Parameters) extends Module {
  val hash_size_words = 256/w
  val bytes_per_word = w/8

  val io = IO(new Bundle {
    val init = Input(Bool())
    val start = Input(Bool())
    val outvalid = Output(Bool())
    val message_in = new MesIO
    val hash_out = Output(Vec(hash_size_words, UInt(w.W)))
  })

  val calc = Module(new Sha256Calc)

  calc.io.init := io.init
  io.outvalid := calc.io.outvalid
  calc.io.start := io.start
  calc.io.M <> io.message_in

  val calcout = calc.io.hout
  // 将calc的输出合并为64位的输出
  // 并将最低位的字节移到最高位
  for(i <- 0 until hash_size_words) {
    io.hash_out(i) := Cat(calcout(2*i+1)(7, 0), calcout(2*i+1)(15, 8), calcout(2*i+1)(23, 16), calcout(2*i+1)(31, 24), calcout(2*i)(7, 0), calcout(2*i)(15, 8), calcout(2*i)(23, 16), calcout(2*i)(31, 24))
  }
}
