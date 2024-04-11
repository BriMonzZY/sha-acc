package sha2

import chisel3._
import chisel3.util._
import freechips.rocketchip.rocket._
import org.chipsalliance.cde.config._
import freechips.rocketchip.tile.HasCoreParameters
import freechips.rocketchip.rocket.constants.MemoryOpConstants


class Sha2CtrlModule(val w: Int)(implicit val p: Parameters) extends Module with HasCoreParameters with MemoryOpConstants {
  // val round_size_words = 512/w // data size of each round (words)
  val round_size_words = 8 // 512bit计算一次
  val hash_size_words = 256/w
  val bytes_per_word = w/8

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

    val aindex            = Output(UInt(log2Up(round_size_words).W))

    val buffer_out  = Output(UInt(w.W))

    val calc_valid        = Output(Bool())
    val dpathMessageIn    = Flipped(new MesIO)
    val hash_finish       = Input(Bool())
    val dpath_init        = Output(Bool())
    val windex            = Output(UInt(log2Up(hash_size_words+1).W))
  })

  // rocc state
  val r_idle :: r_eat_addr :: r_eat_len :: r_finish :: Nil = Enum(4)
  val rocc_s = RegInit(r_idle)

  // haser state
  val s_idle :: s_hash :: s_wait :: s_write :: Nil = Enum(4)
  val state = RegInit(s_idle)

  val msg_addr = RegInit(0.U(64.W)) // SHA2输入消息地址
  val hash_addr= RegInit(0.U(64.W)) // SHA2输出hash地址
  val msg_len  = RegInit(0.U(64.W)) // SHA2输入消息长度
  val busy = RegInit(false.B)
  val calc_valid = RegInit(false.B) // dpath是否可以开始计算
  val dpath_init = RegInit(false.B) // dpath初始化

  val dmem_resp_val_reg = RegNext(io.dmem_resp_val)
  val dmem_resp_tag_reg = RegNext(io.dmem_resp_tag)


  // val rindex = RegInit((rounds+1).U(5.W))  // round index, a counter for absorb (Max=round_size_words-1)
  // val rindex_reg = RegNext(rindex)

  val buffer_valid = RegInit(false.B)
  val buffer_cnt = RegInit(0.U(5.W))
  val read    = RegInit(0.U(32.W)) // 计算从cache读取了多少字的数据，和msg_len比较，来判断是否读取完毕
  val hashed = RegInit(0.U(32.W)) // count how many words in total have been hashed
  val mindex  = RegInit(0.U(5.W)) // buffer的索引，以一次读取为单位
  val pindex  = RegInit(0.U(32.W)) // 填充用的数据buffer索引
  val aindex  = RegInit(0.U(log2Up(round_size_words).W))  // absorb counter
  val windex  = RegInit(0.U(log2Up(hash_size_words+1).W))
  val writes_done = RegInit(VecInit(Seq.fill(hash_size_words){false.B})) // 用于判断是否写入完毕
  val remain_byte = RegInit(0.U(32.W)) // 剩余的字节数，用于填充
  val last_data = RegInit(0.U(32.W)) // 用于填充
  val last_round = RegInit(false.B)
  // val next_buff_val = RegInit(false.B)
  // next_buff_val := ((mindex >= (round_size_words).U) || (read >= msg_len)) && (pindex >= (round_size_words - 1).U)

  // 已经接收了多少数据（mindex）用于赋值pindex
  val words_filled = mindex

  // memory state
  val m_idle :: m_read :: m_wait :: m_pad :: m_absorb :: Nil = Enum(5)
  val mem_s = RegInit(m_idle)


  io.aindex := RegNext(aindex)

  
  // Flip-Flop buffer
  val initValues = Seq.fill(round_size_words) { 0.U(w.W) }
  val buffer = RegInit(VecInit(initValues)) // to hold the data of each round
  val Mreg = RegInit(VecInit(Seq.fill(16)(0.U(32.W))))
  dontTouch(buffer)

  io.dpathMessageIn(0) := Mreg(0)
  io.dpathMessageIn(1) := Mreg(1)
  io.dpathMessageIn(2) := Mreg(2)
  io.dpathMessageIn(3) := Mreg(3)
  io.dpathMessageIn(4) := Mreg(4)
  io.dpathMessageIn(5) := Mreg(5)
  io.dpathMessageIn(6) := Mreg(6)
  io.dpathMessageIn(7) := Mreg(7)
  io.dpathMessageIn(8) := Mreg(8)
  io.dpathMessageIn(9) := Mreg(9)
  io.dpathMessageIn(10) := Mreg(10)
  io.dpathMessageIn(11) := Mreg(11)
  io.dpathMessageIn(12) := Mreg(12)
  io.dpathMessageIn(13) := Mreg(13)
  io.dpathMessageIn(14) := Mreg(14)
  io.dpathMessageIn(15) := Mreg(15)

  // signal defalut
  io.busy := busy
  io.dmem_req_val := false.B
  io.dmem_req_tag := 0.U
  io.dmem_req_addr := 0.U(32.W)
  io.dmem_req_cmd:= M_XRD
  io.dmem_req_size:= log2Ceil(8).U
  io.calc_valid := calc_valid
  calc_valid := false.B
  io.dpath_init := dpath_init
  dpath_init := false.B
  io.windex := windex


  // start memory handler
  switch(mem_s) {
    is(m_idle) { // 0
      // we can start filling the buffer if we aren't writing and if we got a new message
      // or the hashing started
      // and there is more to read
      // and the buffer has been abosrbed
      val canRead = busy && ((read < msg_len) || (read === msg_len && msg_len === 0.U)) &&
          !buffer_valid && buffer_cnt === 0.U

      // printf("[sha2acc] in m_idle state\n")

      when(canRead) {
        // start reading data from cache
        buffer_cnt := 0.U
        mindex := 0.U
        mem_s := m_read
      }.otherwise {
        mem_s := m_idle
      }
    } // end of m_idle
    is(m_read) { // 1
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

      printf("[sha2acc] in m_read state, mindex: %d, msg_addr: %x, read: %d, round_size_words: %d\n", mindex, msg_addr, read, round_size_words.U)

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
        pindex := words_filled
      }
    } // end of m_read
    is(m_wait) { // 2
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
            when(read > (msg_len + 8.U)) {
              buffer_valid := false.B
              mem_s := m_absorb
            }
            // 继续读取下一个数据
            printf("[sha2acc] m_wait to m_read state\n")
            mem_s := m_read
          }.otherwise { // 已经读取了所有的消息
            // done reading
            buffer_valid := false.B
            last_data := buffer(mindex - 1.U)
            mem_s := m_pad
            pindex := words_filled
          }
        }.otherwise { // 一轮512bit已经读完
          printf("[sha2acc] m_wait to m_absorb state\n")
          // message not done yet, but buffer is full, so absorb current data in the buffer
          msg_addr := msg_addr + (round_size_words << 3).U // 1 word = 8 bytes, so left shift 3 (i.e. times 8)
          buffer_valid := false.B
          mem_s := m_absorb

          // 计算这一轮前将buffer的数据copy到Mreg
          for(i <- 0 until 8) {
            Mreg(i*2) := Cat(buffer(i)(7, 0), buffer(i)(15, 8), buffer(i)(23, 16), buffer(i)(31, 24))
            Mreg(i*2+1) := Cat(buffer(i)(39, 32), buffer(i)(47, 40), buffer(i)(55, 48), buffer(i)(63, 56))
          }
        }
      }
    } // end of m_wait
    // 如果读不到一整个数据块，则做数据填充
    is(m_pad) { // 3
      when(pindex < ( mindex + 1.U)) { // (16.U-(msg_len%64.U)/4.U
        when(pindex === mindex) { // 给Mreg赋初值
          for(i <- 0 until 8) {
            Mreg(i*2) := Cat(buffer(i)(7, 0), buffer(i)(15, 8), buffer(i)(23, 16), buffer(i)(31, 24))
            Mreg(i*2+1) := Cat(buffer(i)(39, 32), buffer(i)(47, 40), buffer(i)(55, 48), buffer(i)(63, 56))
          }
          last_data := Mreg(((msg_len%64.U)/4.U - 1.U))
        }.otherwise {

        }
        mem_s := m_pad
        pindex := pindex + 1.U
      }.otherwise {
        // 填充长度数据
        val pad_num = (msg_len % 64.U)/4.U
        Mreg(15) := msg_len * 8.U
        Mreg(pad_num) := Mreg(pad_num) & ("xffffffff".U << (4.U-msg_len%4.U))*8.U | ("x80".U << (4.U-msg_len%4.U-1.U)*8.U)

        printf("[sha2acc] finish padding message\n")
        mem_s := m_absorb
        last_round := true.B
      }
    }
    is(m_absorb) { // 4
      buffer_valid := true.B

      when(io.hash_finish) {
        mem_s := m_idle
      }.otherwise {
        mem_s := m_absorb
      }
    }
  }

  switch(state) {
    is(s_idle) {
      val canAbsorb = busy && buffer_valid && hashed <= msg_len
      when(canAbsorb) {
        busy := true.B
        state := s_hash
      } .otherwise {
        state := s_idle
      }
    }
    is(s_hash) {
      state := s_wait
      calc_valid := true.B // 开始计算hash
    }
    is(s_wait) {
      when(io.hash_finish) { // 当dpath计算完毕，则进入s_write状态
        windex := 0.U
        when(last_round) {
          state := s_write
          last_round := false.B
        }.otherwise {
          buffer_valid := false.B
          buffer_cnt := 0.U
          state := s_idle
        }
      }
    }
    // 最后一轮计算出hash值后写入内存
    is(s_write) {
      // request
      io.dmem_req_val := windex < hash_size_words.U
      io.dmem_req_addr := hash_addr
      io.dmem_req_tag := round_size_words.U + windex
      io.dmem_req_cmd := M_XWR

      when(io.dmem_req_rdy) {
        windex := windex + 1.U
        hash_addr := hash_addr + 8.U
      }

      // response
      when(dmem_resp_val_reg) {
        when(dmem_resp_tag_reg(4, 0) >= round_size_words.U) {
          writes_done(dmem_resp_tag_reg(4, 0) - round_size_words.U) := true.B
        }
      }
      when(writes_done.reduce(_&&_)) {
        // 所有写入均完成
        writes_done := VecInit(Seq.fill(hash_size_words){false.B})
        buffer_valid := false.B
        buffer_cnt := 0.U
        busy := false.B
        windex := hash_size_words.U
        state := s_idle
      }.otherwise {
        state := s_write
      }
    }
  }

  // decode rocc instruction
  switch(rocc_s) {
    is(r_idle) {
      io.rocc_req_rdy := !busy
      when(io.rocc_req_val && !busy) {
        when(io.rocc_funct === 0.U) {
          io.rocc_req_rdy := true.B
          msg_addr := io.rocc_rs1
          hash_addr := io.rocc_rs2
          io.busy := true.B
        }.elsewhen(io.rocc_funct === 1.U) {
          dpath_init := true.B // dpath初始化
          busy := true.B
          io.busy := true.B
          io.rocc_req_rdy := true.B
          msg_len := io.rocc_rs1
          remain_byte := msg_len % (512/8).U
          printf("[sha2acc] msg addr: %x, hash addr: %x\n", msg_addr, hash_addr)
          println("[sha2acc] msg len: %x", msg_len)
        }
      }
    }
  }

  io.rocc_req_rdy := true.B
  io.buffer_out := 0.U
}
