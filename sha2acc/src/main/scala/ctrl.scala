package sha2

import chisel3._
import chisel3.util._
import freechips.rocketchip.rocket._
import org.chipsalliance.cde.config._
import freechips.rocketchip.tile.HasCoreParameters
import freechips.rocketchip.rocket.constants.MemoryOpConstants


class Sha2CtrlModule(val w: Int)(implicit val p: Parameters) extends Module with HasCoreParameters with MemoryOpConstants {
  val round_size_words = 512/w // data size of each round (words)
  val rounds = 24

  val io = IO(new Bundle {
    val rocc_req_val      = Input(Bool())
    val rocc_req_rdy      = Output(Bool())
    val rocc_funct        = Input(UInt(2.W)) // Input(Bits(2.W))
    val rocc_rs1          = Input(UInt(64.W))
    val rocc_rs2          = Input(UInt(64.W))
    val rocc_rd           = Input(UInt(5.W))

    val busy              = Output(Bool())

    val dmem_req_val      = Output(Bool())
    val dmem_req_rdy      = Input(Bool())
    val dmem_req_tag      = Output(UInt(coreParams.dcacheReqTagBits.W))
    val dmem_req_addr     = Output(UInt(coreMaxAddrBits.W))
    val dmem_req_cmd      = Output(UInt(M_SZ.W))
    val dmem_req_size     = Output(UInt(log2Ceil(coreDataBytes + 1).W))

    val dmem_resp_val     = Input(Bool())
    val dmem_resp_tag     = Input(UInt(7.W))
    val dmem_resp_data    = Input(UInt(w.W))

    val sfence            = Output(Bool())

    val buffer_out  = Output(UInt(w.W))
  })

  // rocc state
  val r_idle :: r_eat_addr :: r_eat_len :: Nil = Enum(3)
  val rocc_s = RegInit(r_idle)

  val msg_addr = RegInit(0.U(64.W)) // SHA2输入消息地址
  val hash_addr= RegInit(0.U(64.W)) // SHA2输出hash地址
  val msg_len  = RegInit(0.U(64.W)) // SHA2输入消息长度
  val busy = RegInit(false.B)



  val dmem_resp_val_reg = RegNext(io.dmem_resp_val)
  val dmem_resp_tag_reg = RegNext(io.dmem_resp_tag)


  val rindex = RegInit((rounds+1).U(5.W))  // round index, a counter for absorb (Max=round_size_words-1)
  val rindex_reg = RegNext(rindex)

  val buffer_valid = RegInit(false.B)
  val buffer_cnt = RegInit(0.U(5.W))
  val read    = RegInit(0.U(32.W)) // 计算从cache读取了多少字的数据，和msg_len比较，来判断是否读取完毕
  val mindex  = RegInit(0.U(5.W)) // buffer的索引，以一次读取为单位
  // val pindex  = RegInit(0.U(log2Up(round_size_words).W)) // TODO???
  val aindex  = RegInit(0.U(log2Up(round_size_words).W))  // absorb counter
  val next_buff_val = RegInit(false.B)
  // next_buff_val := ((mindex >= (round_size_words).U) || (read >= msg_len)) && (pindex >= (round_size_words - 1).U)

  // TODO???
  val words_filled = mindex

  // memory state
  val m_idle :: m_read :: m_wait :: m_pad :: m_absorb :: Nil = Enum(5)
  val mem_s = RegInit(m_idle)
  
  // Flip-Flop buffer
  val initValues = Seq.fill(round_size_words) { 0.U(w.W) }
  val buffer = RegInit(VecInit(initValues)) // to hold the data of each round

  // signal defalut
  io.busy := busy
  io.dmem_req_val := false.B
  io.dmem_req_tag := rindex
  io.dmem_req_addr := 0.U(32.W)
  io.dmem_req_cmd:= M_XRD
  io.dmem_req_size:= log2Ceil(8).U
  io.sfence := false.B


  // start memory handler
  switch(mem_s) {
    is(m_idle) {
      // we can start filling the buffer if we aren't writing and if we got a new message
      // or the hashing started
      // and there is more to read
      // and the buffer has been abosrbed
      val canRead = true.B // FIXME
      when(canRead) {
        // start reading data from cache
        buffer_cnt := 0.U
        mindex := 0.U
        mem_s := m_read
      }.otherwise {
        mem_s := m_idle
      }
    } // end of m_idle
    is(m_read) {
      // dmem signals
      // TODO
      // // only read if we aren't writing
      // when(state =/= s_write) { 
      // }
      io.dmem_req_val := read < msg_len && mindex < round_size_words.U
      io.dmem_req_addr := msg_addr
      io.dmem_req_tag := mindex
      io.dmem_req_cmd := M_XRD // TODO: ?
      io.dmem_req_size := log2Ceil(8).U
      // read data if ready and valid
      when(io.dmem_req_rdy && io.dmem_req_val) {
        mindex := mindex + 1.U // read 1 word at a time
        msg_addr := msg_addr + 8.U // move to next word
        read := read + 8.U // read 8 bytes at a time
        mem_s := m_wait // wait until reading done
      }.otherwise {
        mem_s := m_read
      }
      // 处理消息长度为0的情况
      when(msg_len === 0.U) {
        read := 1.U
        mem_s := m_pad
      }
    } // end of m_read
    is(m_wait) {
      // the code to process read responses
      when(io.dmem_resp_val) {
        // this is a read response
        buffer(mindex - 1.U) := io.dmem_resp_data
        buffer_cnt := buffer_cnt + 1.U

        // next state
        // the buffer is not full
        when(mindex < (round_size_words).U) {
          when(read < msg_len) {
            // not sure if this case will be used but this means we haven't
            // sent all the requests yet (maybe back pressure causes this)
            when((msg_len+8.U) < read) {
              buffer_valid := false.B
              mem_s := m_absorb
            }
            mem_s := read
          }.otherwise {
            // done reading
            buffer_valid := false.B
            mem_s := m_absorb
          }
        }.otherwise {
          // we have reached the end of this chunk
          // mindex := mindex + UInt(1)
          // read := read + UInt(8)//read 8 bytes
          // 已经发送了所有的请求 we sent all the requests
          msg_addr := msg_addr + (round_size_words << 3).U
          when((msg_len < (read + 8.U))) {
            buffer_valid := false.B
            mem_s := m_absorb
          }.otherwise {
            // we have more to read eventually
            buffer_valid := true.B
            mem_s := m_idle
          }
        }
      }
    } // end of m_wait
    is(m_absorb) {
      buffer_valid := true.B
      // move to idle when we know this thread was absorbed
      when(aindex >= (round_size_words - 1).U) {
        mem_s := m_idle
      }.otherwise {
        mem_s := m_absorb
      }
    }
  }

  // decode rocc instruction
  switch(rocc_s) {
    is(r_idle) {
      when(io.rocc_req_val) {
        when(io.rocc_funct === 0.U) {
          io.rocc_req_rdy := true.B
          msg_addr := io.rocc_rs1
          hash_addr := io.rocc_rs2
          println("Msg Addr: "+msg_addr+", Hash Addr: "+hash_addr)
          io.busy := true.B
        }.elsewhen(io.rocc_funct === 1.U) {
          io.busy := true.B
          io.rocc_req_rdy := true.B
          msg_len := io.rocc_rs1
        }
      }
    }
  }

  io.rocc_req_rdy := true.B
  io.buffer_out := 0.U
}
