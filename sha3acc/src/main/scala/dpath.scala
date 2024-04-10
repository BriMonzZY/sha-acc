// authors: Brimonzzy
package sha3

import chisel3._
import chisel3.util._
import org.chipsalliance.cde.config.Parameters

class DpathModule(val w: Int, val s: Int)(implicit p: Parameters) extends Module {
  // constants
  val r = 2*256
  val c = 25*w - r
  val rounds = 24 // 12 + 2l
  val hash_size_words = 256/w
  val round_size_words = c/w
  val bytes_per_word = w/8

  val io = IO(new Bundle {
    val init   = Input(Bool())
    val write  = Input(Bool())
    val absorb = Input(Bool())
    val round  = Input(UInt(5.W))
    val stage  = Input(UInt(log2Ceil(s).W))
    val aindex = Input(UInt(log2Ceil(round_size_words).W))
    val message_in = Input(Bits(w.W))
    val hash_out = Output(Vec(hash_size_words, UInt(w.W)))
  })

  val initValues = Seq.fill(25) {0.U(w.W)}
  val state = RegInit(VecInit(initValues))

  // submodules
  val theta = Module(new ThetaModule(w)).io
  val rhopi = Module(new RhoPiModule(w)).io
  val chi   = Module(new ChiModule(w)).io
  val iota  = Module(new IotaModule(w)).io

  // default
  theta.state_i := VecInit(initValues)
  iota.round := 0.U

  // connect submodules to each other
  if(s==1) { // 无流水线
    theta.state_i := state
    rhopi.state_i <> theta.state_o
    chi.state_i <> rhopi.state_o
    iota.state_i <> chi.state_o
    state := iota.state_o
  }
  if(s==2) { // 2级流水线
    // stage 1
    theta.state_i := state
    rhopi.state_i <> theta.state_o
    // stage 2
    chi.state_i := state
    iota.state_i <> chi.state_o
  }
  if(s==4) { // 4级流水线
    // stage 1
    theta.state_i := state
    // stage 2
    rhopi.state_i := state
    // stage 3
    chi.state_i := state
    // stage 4
    iota.state_i := state
  }

  iota.round := io.round


  switch(io.stage) {
    is(0.U) {
      if(s==1) {
        state := iota.state_o
      } else if(s==2) {
        state := rhopi.state_o
      } else if(s==4) {
        state := theta.state_o
      }
    }
    is(1.U) {
      if(s==2){
        state := iota.state_o
      } else if(s==4) {
        state := rhopi.state_o
      }
    }
    is(2.U) {
      if(s==4) {
        state := chi.state_o
      }
    }
    is(3.U) {
      if(s==4) {
        state := iota.state_o
      }
    }
  }


  when(io.absorb){
    state := state
    if(p(Sha3PrintfEnable)){
      printf("SHA3 finished an iteration with index %d and message %x\n", io.aindex, io.message_in)
    }
    when(io.aindex < round_size_words.U) {
      // 吸收 阶段的计算 Si ^ Pi
      state((io.aindex%5.U)*5.U+(io.aindex/5.U)) :=
        state((io.aindex%5.U)*5.U+(io.aindex/5.U)) ^ io.message_in
    }
  }

  // val hash_res = Wire(Vec(hash_size_words, UInt(w.W)))
  for(i <- 0 until hash_size_words) {
    io.hash_out(i) := state(i*5)
  }

  // keep state from changing while we write
  when(io.write) {
    state := state
  }

  // initialize state to 0 for new hashes or at reset
  when(io.init) {
    state := VecInit(initValues)
  }

  when(reset.asBool){
    state := VecInit(initValues)
  }
}
