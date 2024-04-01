package sha2

import chisel3._
import chisel3.util._
import freechips.rocketchip.tile._
import org.chipsalliance.cde.config._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.rocket.{TLBConfig, HellaCacheReq}


/*
 * Enable specific printf's.
 */
case object Sha2PrintfEnable extends Field[Boolean](false)


// For Verilog IO
class WrapBundle(implicit p: Parameters) extends Bundle {
  val io = new RoCCIO(0, 0) // no ptw / fpu
  val clock = Input(Clock())
  val reset = Input(UInt(1.W))
}

class Sha2Accel(opcodes: OpcodeSet)(implicit p: Parameters) extends LazyRoCC(
  opcodes = opcodes ) {
  override lazy val module = new Sha2AccelModuleImp(this)
}

class Sha2AccelModuleImp(outer: Sha2Accel)(implicit p: Parameters) extends LazyRoCCModuleImp(outer) {
  
  // val cmd = Queue(io.cmd) // depth = 1
  val cmd = io.cmd
  val funct = cmd.bits.inst.funct
  when(cmd.fire()) {
    printf("[Sha2Accel] Received SHA2 Accelerator Command\n")
    printf("[Sha2Accel] rs1: %x rs2: %x funct:%d\n", cmd.bits.rs1, cmd.bits.rs2, cmd.bits.inst.funct)
  }

  cmd.ready := true.B
}

class WithSha2Printf extends Config((site, here, up) => {
  case Sha2PrintfEnable => true
})

class WithSha2Accel extends Config((site, here, up) => {
  case BuildRoCC => up(BuildRoCC) ++ Seq(
    (p: Parameters) => {
      val sha2 = LazyModule.apply(new Sha2Accel(OpcodeSet.custom1)(p))
      sha2
    }
  )
})
