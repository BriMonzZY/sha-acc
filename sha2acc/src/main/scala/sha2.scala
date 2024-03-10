package sha2

import chisel3._
import chisel3.util._
import freechips.rocketchip.tile._
import org.chipsalliance.cde.config._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.rocket.{TLBConfig, HellaCacheReq}


case object Sha2WidthP extends Field[Int]
case object Sha2Stages extends Field[Int]
case object Sha2FastMem extends Field[Boolean]
case object Sha2BufferSram extends Field[Boolean]

/*
 * Use a Blackbox verilog version of the inner SHA2 accelerator
 */
case object Sha2BlackBox extends Field[Boolean](false)
/*
 * Enable specific printf's. This is used to demonstrate MIDAS
 */
case object Sha2PrintfEnable extends Field[Boolean](false)



class WrapBundle(nPTWPorts: Int)(implicit p: Parameters) extends Bundle {
  val io = new RoCCIO(nPTWPorts, 0)
  val clock = Input(Clock())
  val reset = Input(UInt(1.W))
}
// class Sha2BlackBox(implicit p: Parameters) extends BlackBox with HasBlackBoxResource {
//   val io = IO(new WrapBundle(0))
//   addResource("/vsrc/Sha3BlackBox.v")
// }


class Sha2Accel(opcodes: OpcodeSet)(implicit p: Parameters) extends LazyRoCC(
  opcodes = opcodes ) {
  override lazy val module = new Sha2AccelModuleImp(this)
}

class Sha2AccelModuleImp(outer: Sha3Accel)(implicit p: Parameters) extends LazyRoCCModuleImp(outer) {
  val w = p(Sha2WidthP)
  val s = p(Sha2Stages)
}

// class WithSha2BlackBox extends Config((site, here, up) => {
//   case Sha3BlackBox => true
// })

class WithSha2Printf extends Config((site, here, up) => {
  case Sha2PrintfEnable => true
})

class WithSha2Accel extends Config((site, here, up) => {
  case Sha2WidthP => 64
  case Sha2Stages => 2
  case Sha2FastMem => false 
  case Sha2BufferSram => false
  case Sha2BlackBox => false
  case BuildRoCC => up(BuildRoCC) ++ Seq(
    (p: Parameters) => {
      val sha3 = LazyModule.apply(new Sha2Accel(OpcodeSet.custom3)(p))
      sha3
    }
  )
})
