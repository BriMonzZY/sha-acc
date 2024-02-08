package sha3

import chisel3._
import chisel3.util._
import freechips.rocketchip.tile._
import org.chipsalliance.cde.config._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.rocket.{TLBConfig, HellaCacheReq}


case object Sha3WidthP extends Field[Int]
case object Sha3Stages extends Field[Int]
case object Sha3FastMem extends Field[Boolean]
case object Sha3BufferSram extends Field[Boolean]

/*
 * Use a Blackbox verilog version of the inner SHA3 accelerator
 */
case object Sha3BlackBox extends Field[Boolean](false)
/*
 * Enable specific printf's. This is used to demonstrate MIDAS
 */
case object Sha3PrintfEnable extends Field[Boolean](false)



class WrapBundle(nPTWPorts: Int)(implicit p: Parameters) extends Bundle {
  val io = new RoCCIO(nPTWPorts, 0)
  val clock = Input(Clock())
  val reset = Input(UInt(1.W))
}
class Sha3BlackBox(implicit p: Parameters) extends BlackBox with HasBlackBoxResource {
  val io = IO(new WrapBundle(0))
  addResource("/vsrc/Sha3BlackBox.v")
}


class Sha3Accel(opcodes: OpcodeSet)(implicit p: Parameters) extends LazyRoCC(
  opcodes = opcodes ) {
  override lazy val module = new Sha3AccelModuleImp(this)
}

class Sha3AccelModuleImp(outer: Sha3Accel)(implicit p: Parameters) extends LazyRoCCModuleImp(outer) {
  val w = p(Sha3WidthP)
  val s = p(Sha3Stages)

  if (p(Sha3BlackBox)) {
    val sha3bb = Module(new Sha3BlackBox)
    io <> sha3bb.io.io
    sha3bb.io.clock := clock
    sha3bb.io.reset := reset.asUInt
  } else {

    val ctrl = Module(new CtrlModule(w,s)(p))

    ctrl.io.rocc_req_val <> io.cmd.valid
    ctrl.io.rocc_funct   <> io.cmd.bits.inst.funct
    ctrl.io.rocc_rs1     <> io.cmd.bits.rs1
    ctrl.io.rocc_rs2     <> io.cmd.bits.rs2
    ctrl.io.rocc_rd      <> io.cmd.bits.inst.rd
    io.cmd.ready := ctrl.io.rocc_req_rdy
    io.busy := ctrl.io.busy

    val status = RegEnable(io.cmd.bits.status, io.cmd.fire())
    val dmem_data = Wire(Bits())
    def dmem_ctrl(req: DecoupledIO[HellaCacheReq]) {
      req.valid := ctrl.io.dmem_req_val
      ctrl.io.dmem_req_rdy := req.ready
      req.bits.tag := ctrl.io.dmem_req_tag
      req.bits.addr := ctrl.io.dmem_req_addr
      req.bits.cmd := ctrl.io.dmem_req_cmd
      req.bits.size := ctrl.io.dmem_req_size
      req.bits.data := dmem_data
      req.bits.signed := false.B
      req.bits.dprv := status.dprv
      req.bits.dv := status.dv
      req.bits.phys := false.B
    }

    dmem_ctrl(io.mem.req)

    ctrl.io.dmem_resp_val  <> io.mem.resp.valid
    ctrl.io.dmem_resp_tag  <> io.mem.resp.bits.tag
    ctrl.io.dmem_resp_data := io.mem.resp.bits.data

    val dpath = Module(new DpathModule(w,s)(p))

    dpath.io.message_in <> ctrl.io.buffer_out
    dmem_data := dpath.io.hash_out(ctrl.io.windex)

    // ctrl.io <> dpath.io
    dpath.io.absorb := ctrl.io.absorb
    dpath.io.init := ctrl.io.init
    dpath.io.write := ctrl.io.write
    dpath.io.round := ctrl.io.round
    dpath.io.stage := ctrl.io.stage
    dpath.io.aindex := ctrl.io.aindex
  }
}

class WithSha3BlackBox extends Config((site, here, up) => {
  case Sha3BlackBox => true
})

class WithSha3Printf extends Config((site, here, up) => {
  case Sha3PrintfEnable => true
})

class WithSha3Accel extends Config((site, here, up) => {
  case Sha3WidthP => 64
  case Sha3Stages => 2
  case Sha3FastMem => false 
  case Sha3BufferSram => false
  case Sha3BlackBox => false
  case BuildRoCC => up(BuildRoCC) ++ Seq(
    (p: Parameters) => {
      val sha3 = LazyModule.apply(new Sha3Accel(OpcodeSet.custom2)(p))
      sha3
    }
  )
})
