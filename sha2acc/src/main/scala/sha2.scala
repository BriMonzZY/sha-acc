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
case object Sha2PrintfEnable extends Field[Boolean]


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

  val ctrl = Module(new Sha2CtrlModule(64)(p))
  val dpath = Module(new Sha2DpathModule(64)(p))

  // rocc interface to controller
  ctrl.io.rocc_req_val <> io.cmd.valid
  ctrl.io.rocc_funct <> io.cmd.bits.inst.funct
  ctrl.io.rocc_rs1 <> io.cmd.bits.rs1
  ctrl.io.rocc_rs2 <> io.cmd.bits.rs2
  ctrl.io.rocc_rd <> io.cmd.bits.inst.rd
  io.cmd.ready := ctrl.io.rocc_req_rdy
  io.busy := ctrl.io.busy

  val status = RegEnable(io.cmd.bits.status, io.cmd.fire)
  val dmem_data = Wire(Bits())

  when(io.cmd.fire) {
    printf("[sha2acc] rs1: %x rs2: %x\n", io.cmd.bits.rs1, io.cmd.bits.rs2)
  }

  // memory controll
  def sha2_dmem_ctrl(req: DecoupledIO[HellaCacheReq]) {
    req.valid := ctrl.io.dmem_req_val
    ctrl.io.dmem_req_rdy := req.ready
    req.bits.tag := ctrl.io.dmem_req_tag
    req.bits.addr := ctrl.io.dmem_req_addr
    req.bits.cmd := ctrl.io.dmem_req_cmd
    req.bits.size := ctrl.io.dmem_req_size
    req.bits.data := dmem_data
    req.bits.signed := false.B
    req.bits.dprv := status.dprv // effective prv for data accesses
    req.bits.dv := status.dv // effective v for data accesses
    req.bits.phys := false.B
  }
  sha2_dmem_ctrl(io.mem.req)

  dmem_data := dpath.io.hash_out(ctrl.io.windex) // 写入MEM的hash输出

  // memory response
  ctrl.io.dmem_resp_val <> io.mem.resp.valid
  ctrl.io.dmem_resp_tag <> io.mem.resp.bits.tag
  ctrl.io.dmem_resp_data <> io.mem.resp.bits.data

  dpath.io.start := ctrl.io.calc_valid
  ctrl.io.dpathMessageIn <> dpath.io.message_in
  ctrl.io.hash_finish := dpath.io.outvalid
  dpath.io.init := ctrl.io.dpath_init
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
