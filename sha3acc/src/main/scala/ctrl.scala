package sha3

import chisel3._
import chisel3.util._
import freechips.rocketchip.rocket._
import org.chipsalliance.cde.config._
import freechips.rocketchip.tile.HasCoreParameters
import freechips.rocketchip.rocket.constants.MemoryOpConstants



class CtrlModule(val w: Int, val s: Int)(implicit val p: Parameters) extends Module with HasCoreParameters with MemoryOpConstants {
  val r = 2*256
  val c = 25*w - r // 25 words, 64 bits per word, 1600 bits in total
  val round_size_words = c/w // data size of each round
  val rounds = 24 // 12 + 2l
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

    val sfence            = Output(Bool())

    // Sha3 Specific signals
    val round       = Output(UInt(5.W))
    val stage       = Output(UInt(log2Up(s).W))
    val absorb      = Output(Bool())
    val aindex      = Output(UInt(log2Up(round_size_words).W))
    val init        = Output(Bool())
    val write       = Output(Bool())
    val windex      = Output(UInt(log2Up(hash_size_words+1).W))

    val buffer_out  = Output(UInt(w.W))
  })

  // RoCC HANDLER
  // rocc pipe state
  val r_idle :: r_eat_addr :: r_eat_len :: Nil = Enum(3)

  val msg_addr = RegInit(0.U(64.W))
  val hash_addr= RegInit(0.U(64.W))
  val msg_len  = RegInit(0.U(64.W))
  val busy = RegInit(false.B)

  val rocc_s = RegInit(r_idle)

  // register inputs
  // val rocc_req_val_reg = RegNext(io.rocc_req_val)
  // val rocc_funct_reg = RegInit(0.U(2.W))
  // rocc_funct_reg := io.rocc_funct
  // val rocc_rs1_reg = RegInit(io.rocc_rs1)
  // val rocc_rs2_reg = RegInit(io.rocc_rs2)
  // val rocc_rd_reg = RegInit(io.rocc_rd)

  val dmem_resp_val_reg = RegNext(io.dmem_resp_val)
  val dmem_resp_tag_reg = RegNext(io.dmem_resp_tag)

  // memory pipe state
  val fast_mem = p(Sha3FastMem)
  val m_idle :: m_read :: m_wait :: m_pad :: m_absorb :: Nil = Enum(5)
  val mem_s = RegInit(m_idle)

	// SRAM Buffer
  val buffer_sram = p(Sha3BufferSram)
  val buffer_mem = Mem(round_size_words, UInt(w.W))
  // Flip-Flop buffer
	val initValues = Seq.fill(round_size_words) { 0.U(w.W) }
	val buffer = RegInit(VecInit(initValues)) // to hold the data of each round

	val buffer_raddr = RegInit(0.U(log2Up(round_size_words).W))
  val buffer_wen = Wire(Bool()); buffer_wen := false.B // Defaut value of buffer_wen
  val buffer_waddr = Wire(UInt(w.W)); buffer_waddr := 0.U
  val buffer_wdata = Wire(UInt(w.W)); buffer_wdata := 0.U
  val buffer_rdata = Bits(w.W);
  if(buffer_sram) {
    when(buffer_wen) { buffer_mem.write(buffer_waddr, buffer_wdata) }
    buffer_rdata := buffer_mem(buffer_raddr)
  }

  // This is used to prevent the pad index from advancing if waiting for the sram to read
  // SRAM reads take 1 cycle
  val wait_for_sram = RegInit(true.B)

  val buffer_valid = RegInit(false.B)
  val buffer_count = RegInit(0.U(5.W))
  val read    = RegInit(0.U(32.W))  // count how many words in total have been read from memory, compare with msg_len to determine if the entire message is all read
  val hashed  = RegInit(0.U(32.W))  // count how many words in total have been hashed
  val areg    = RegInit(false.B)  // a flag to indicate if we are doing absorb
  val mindex  = RegInit(0.U(5.W)) // the index for buffer, determine if the buffer is full
  val windex  = RegInit(0.U(log2Up(hash_size_words+1).W))
  val aindex  = RegInit(0.U(log2Up(round_size_words).W))  // absorb counter
  val pindex  = RegInit(0.U(log2Up(round_size_words).W))
  val writes_done = RegInit(VecInit(Seq.fill(hash_size_words){false.B}))
  val next_buff_val = RegInit(false.B)
  if(fast_mem) {
    next_buff_val := (buffer_count >= mindex) && (pindex >= (round_size_words - 1).U)
  } else {
    next_buff_val := ((mindex >= (round_size_words).U) || (read >= msg_len)) && (pindex >= (round_size_words - 1).U)
  }

  // Note that the output of io.aindex is delayed by 1 cycle
  io.aindex     := RegNext(aindex)
  io.absorb     := areg
  areg          := false.B
  if(buffer_sram) {
    // when(areg) {
    // Note that the aindex used here is one cycle behind that is passed to the datapath (out of phase)
      buffer_raddr := aindex
    // }.elsewhen(mem_s === m_pad){
    when(mem_s === m_pad) {
      buffer_raddr := pindex
    }
    io.buffer_out := buffer_rdata
  } else {
    // Note that this uses the index that is passed to the datapath (in phase)
    io.buffer_out := buffer(io.aindex)
  }
  io.windex := windex

  // misc padding signals
  val first_pad = "b0000_0110".U
  val last_pad  = "b1000_0000".U
  val both_pad  = first_pad | last_pad
  // last word with message in it
  val words_filled = // if(fast_mem) {
    Mux(mindex > 0.U, mindex - 1.U, mindex)
  //}else{
    //mindex
  //}

  // last byte with message in it
  val byte_offset = (msg_len)%bytes_per_word.U

  // hasher state
  val s_idle :: s_absorb :: s_finish_abs :: s_hash :: s_write :: Nil = Enum(5)
  val state = RegInit(s_idle)

  val rindex = RegInit((rounds+1).U(5.W))  // round index, a counter for absorb (Max=round_size_words-1)
  // val sindex = RegInit(0.U((log2Ceil(s)+1).W)) // stage index, a counter for hash
  val sindex = RegInit(0.U(log2Up(s).W))
    
  // default
  io.rocc_req_rdy := false.B
  io.init := false.B
  io.busy := busy
  io.round := rindex
  io.stage := sindex
  io.write := true.B
  io.dmem_req_val := false.B
  io.dmem_req_tag := rindex
  io.dmem_req_addr := 0.U(32.W)
  io.dmem_req_cmd:= M_XRD
  io.dmem_req_size:= log2Ceil(8).U
  io.sfence := false.B

  val rindex_reg = RegNext(rindex)

  // decode the rocc instruction
  switch(rocc_s) {
    is(r_idle) {
      io.rocc_req_rdy := !busy
      when(io.rocc_req_val && !busy){
        when(io.rocc_funct === 0.U) {
          io.rocc_req_rdy := true.B
          msg_addr  := io.rocc_rs1
          hash_addr := io.rocc_rs2
          println("Msg Addr: "+msg_addr+", Hash Addr: "+hash_addr)
          io.busy := true.B
        } .elsewhen(io.rocc_funct === 1.U) {
          busy := true.B
          io.rocc_req_rdy := true.B
          io.busy := true.B
          msg_len := io.rocc_rs1
        }
      }
    }
  }

  // END RoCC HANDLER
  // START MEM HANDLER


  switch(mem_s) {
    // 初始化计数，跳转m_read状态
    is(m_idle){
      // we can start filling the buffer if we aren't writing and if we got a new message
      // or the hashing started
      // and there is more to read
      // and the buffer has been absorbed
      val canRead = busy && ((read < msg_len || (read === msg_len && msg_len === 0.U)) &&
                    (!buffer_valid && buffer_count === 0.U))
      when(canRead){
        // start reading data
        // buffer := Vec.fill(round_size_words){Bits(0,W)}
        buffer_count := 0.U
        mindex := 0.U
        mem_s := m_read
      }.otherwise{
        mem_s := m_idle
      }
    }  // end of m_idle
    // 给dmem发出valid信号和地址、标签，并跳转到m_wait状态
    is(m_read) {
      // dmem signals
      // only read if we aren't writing
      when(state =/= s_write) {
        io.dmem_req_val := read < msg_len && mindex < round_size_words.U
        io.dmem_req_addr := msg_addr
        io.dmem_req_tag := mindex
        io.dmem_req_cmd := M_XRD
        io.dmem_req_size := log2Ceil(8).U

        // read data if ready and valid
        when(io.dmem_req_rdy && io.dmem_req_val) {
          mindex := mindex + 1.U // read 1 word each time
          msg_addr := msg_addr + 8.U
          read := read + 8.U // read 8 bytes each time
          if(!fast_mem) {
            mem_s := m_wait  // wait until reading done
          }
        }.otherwise {
          if(!fast_mem) {
            mem_s := m_read
          }
        }
        //TODO: don't like special casing this
        when(msg_len === 0.U) {
          read := 1.U
          if(!fast_mem) {
            mem_s := m_pad
            pindex := words_filled
          }
        }
      }
      // if(fast_mem) {
      //   //next state
      //   when(mindex < UInt(round_size_words - 1)){
      //     //TODO: in pad check buffer_count ( or move on to next thread?)
      //     when(msg_len > read){
      //       //not sure if this case will be used but this means we haven't
      //       //sent all the requests yet (maybe back pressure causes this)
      //       when((msg_len+UInt(8)) < read){
      //         buffer_valid := Bool(false)
      //         mem_s := m_pad
      //         pindex := words_filled
      //       }
      //       mem_s := m_read
      //     }.otherwise{
      //       //its ok we didn't send them all because the message wasn't big enough
      //       buffer_valid := Bool(false)
      //       mem_s := m_pad
      //         pindex := words_filled
      //     }
      //   }.otherwise{
      //     when(mindex < UInt(round_size_words) &&
      //         !(io.dmem_req_rdy && io.dmem_req_val)){
      //       //we are still waiting to send the last request
      //       mem_s := m_read
      //     }.otherwise{
      //       //we have reached the end of this chunk
      //       mindex := mindex + UInt(1)
      //       msg_addr := msg_addr + UInt(8)
      //       read := read + UInt(8)//read 8 bytes
      //       //we sent all the requests
      //       when((msg_len < (read+UInt(8) ))){
      //         //but the buffer still isn't full
      //         buffer_valid := Bool(false)
      //         mem_s := m_pad
      //         pindex := words_filled
      //       }.otherwise{
      //         //we have more to read eventually
      //         mem_s := m_idle
      //       }
      //     }
      //   }
      // }
    } // end of m_read
    // 接收dmem返回的数据（写入buffer），如果需要继续接收就回到m_read状态，否则跳转到m_wait状态
    is(m_wait) {
      // the code to process read responses
      when(io.dmem_resp_val) {
        //This is read response
        if(buffer_sram) {
          buffer_wen := true.B
          buffer_waddr := mindex - 1.U
          buffer_wdata := io.dmem_resp_data
        } else {
          buffer(mindex - 1.U) := io.dmem_resp_data
        }
        buffer_count := buffer_count + 1.U

        // next state
        // the buffer is not full
        when(mindex < (round_size_words-1).U) {
          // TODO: in pad check buffer_count ( or move on to next thread?)
          // 
          when(msg_len > read) {
            // not sure if this case will be used but this means we haven't
            // sent all the requests yet (maybe back pressure causes this)
            // 不确定是否会使用这种情况，但这意味着我们还没有发送所有请求（可能是反压导致的）
            when((msg_len+8.U) < read) {
              buffer_valid := false.B
              mem_s := m_pad
              pindex := words_filled
            }
            mem_s := m_read
          } .otherwise {
            // its ok we didn't send them all because the message wasn't big enough
            buffer_valid := false.B
            mem_s := m_pad
            pindex := words_filled
          }
        }.otherwise{
          // 发送最后一个请求
          when(mindex < (round_size_words).U &&
              !(io.dmem_req_rdy && io.dmem_req_val)){
            // we are still waiting to send the last request
            mem_s := m_read
          }.otherwise {
            // we have reached the end of this chunk
            // mindex := mindex + UInt(1)
            // read := read + UInt(8)//read 8 bytes
            // 已经发送了所有的请求 we sent all the requests
            msg_addr := msg_addr + (round_size_words << 3).U
            when((msg_len < (read+8.U))){
              //but the buffer still isn't full
              buffer_valid := false.B
              mem_s := m_pad
              pindex := words_filled
            }.otherwise {
              // 我们最终还有更多的东西要读 we have more to read eventually
              buffer_valid := true.B
              mem_s := m_idle
            }
          }
        }
      }
    } // end of m_wait
    is(m_pad) {
      //local signals
      //make sure we have received all the responses for this message
      //TODO: update next_buff_val to use pindex
      buffer_valid := next_buff_val

      // 只有当我们已经将mem-resp写入时，才更新缓冲区 only update the buffer if we have already written the mem resp to the word
      when(! (buffer_count < mindex && (pindex >= buffer_count)) ){
        //set everything to 0000_000 after end of message first
        when(pindex > words_filled && pindex < (round_size_words-1).U){
          //there is a special case where we need to pad on a word boundary
          //when we have to put in first_pad here rather than just all zeros
          when(byte_offset === 0.U && (pindex === words_filled+1.U)
              && mindex =/= 0.U){
            if(buffer_sram){
              buffer_wen := true.B
              buffer_waddr := pindex
              buffer_wdata := Cat(0.U((w-8).W),first_pad)
            }else{
              buffer(pindex) := Cat(0.U((w-8).W),first_pad)
            }
          }.otherwise{
            if(buffer_sram){
              buffer_wen := true.B
              buffer_waddr := pindex
              buffer_wdata := 0.U(w.W)
            }else{
              buffer(pindex) := 0.U(w.W)
            }
          }
        }.elsewhen(pindex === (round_size_words -1).U){
          //this is normally when we write the last_pad but we might end up writing both_pad
          //we write both pad if we filled all of the words
            //and all but one of the bytes
          when(words_filled === (round_size_words - 1).U){
            when(byte_offset === (bytes_per_word -1).U){
              //together with the first pad
              if(buffer_sram){
                when(wait_for_sram === false.B){
                  buffer_wen := true.B
                  buffer_waddr := pindex
                  buffer_wdata := Cat(both_pad, buffer_rdata(55,0))
                }
              }else{
                buffer(pindex) := Cat(both_pad, buffer(pindex)(55,0))
              }
            }.elsewhen(byte_offset === 0.U){
              //do nothing since we hit the exact size of the word
            }
          }.otherwise{
            //at the end of the last word
            //we clear the word if we didn't fill it
            when(words_filled < (round_size_words - 1).U){
              if(buffer_sram){
                buffer_wen := true.B
                buffer_waddr := pindex
                buffer_wdata := Cat(last_pad, 0.U(((bytes_per_word-1)*8).W))
              }else{
                buffer(pindex) := Cat(last_pad, 0.U(((bytes_per_word-1)*8).W))
              }
            }.otherwise{
              if(buffer_sram){
                when(wait_for_sram === false.B){
                  buffer_wen := true.B
                  buffer_waddr := pindex
                  buffer_wdata := Cat(last_pad, buffer_rdata((bytes_per_word-1)*8-1,0))
                }
              }else{
                buffer(pindex) := Cat(last_pad, buffer(pindex)((bytes_per_word-1)*8-1,0))
              }
            }
          }
        }.elsewhen(pindex === words_filled){
          //normally this is when we need to write the first_pad
          when(byte_offset =/= 0.U) {
            //not last byte so we put first pad here
            when(byte_offset === 1.U){
              if(buffer_sram){
                when(wait_for_sram === false.B){
                  //SRAM was allowed 1 cycle to read
                  buffer_wen := true.B
                  buffer_waddr := pindex
                  buffer_wdata := Cat(first_pad,buffer_rdata(7,0))
                  //the pindex is still the same as last cycle and buffer_rdata contains buffer(pindex)
                }
              }else{
                buffer(pindex) := Cat(first_pad,buffer(pindex)(7,0))
              }
            }.elsewhen(byte_offset === 2.U){
              if(buffer_sram){
                when(wait_for_sram === false.B){
                  buffer_wen := true.B
                  buffer_waddr := pindex
                  buffer_wdata := Cat(first_pad,buffer_rdata(15,0))
                }
              }else{
                buffer(pindex) := Cat(first_pad,buffer(pindex)(15,0))
              }
            }.elsewhen(byte_offset === 3.U){
              if(buffer_sram){
                when(wait_for_sram === false.B){
                  buffer_wen := true.B
                  buffer_waddr := pindex
                  buffer_wdata := Cat(first_pad,buffer_rdata(23,0))
                }
              }else{
                buffer(pindex) := Cat(first_pad,buffer(pindex)(23,0))
              }
            }.elsewhen(byte_offset === 4.U){
              if(buffer_sram){
                when(wait_for_sram === false.B){
                  buffer_wen := true.B
                  buffer_waddr := pindex
                  buffer_wdata := Cat(first_pad,buffer_rdata(31,0))
                }
              }else{
                buffer(pindex) := Cat(first_pad,buffer(pindex)(31,0))
              }
            }.elsewhen(byte_offset === 5.U){
              if(buffer_sram){
                when(wait_for_sram === false.B){
                  buffer_wen := true.B
                  buffer_waddr := pindex
                  buffer_wdata := Cat(first_pad,buffer_rdata(39,0))
                }
              }else{
                buffer(pindex) := Cat(first_pad,buffer(pindex)(39,0))
              }
            }.elsewhen(byte_offset === 6.U){
              if(buffer_sram){
                when(wait_for_sram === false.B){
                  buffer_wen := true.B
                  buffer_waddr := pindex
                  buffer_wdata := Cat(first_pad,buffer_rdata(47,0))
                }
              }else{
                buffer(pindex) := Cat(first_pad,buffer(pindex)(47,0))
              }
            }.elsewhen(byte_offset === 7.U){
              if(buffer_sram){
                when(wait_for_sram === false.B){
                  buffer_wen := true.B
                  buffer_waddr := pindex
                  buffer_wdata := Cat(first_pad,buffer_rdata(55,0))
                }
              }else{
                buffer(pindex) := Cat(first_pad,buffer(pindex)(55,0))
              }
            }
          }.otherwise{
            //this is only valid if we didn't fill any words
            when(mindex === 0.U && byte_offset === 0.U){
              if(buffer_sram){
                buffer_wen := true.B
                buffer_waddr := pindex
                buffer_wdata := Cat(0.U((w-8).W),first_pad)
              }else{
                buffer(pindex) := Cat(0.U((w-8).W),first_pad)
              }
            }
          }
        }
      }

      //next state
      when(next_buff_val){
      //we have received all responses so the buffer is as full as it will get
        mindex := 0.U//reset this for absorb
        when(areg){
          //we already started absorbing so skip to idle and go to next thread
          buffer_count := 0.U
          mindex := 0.U//reset this for absorb
          mem_s := m_idle
          pindex := 0.U
          wait_for_sram := true.B
        }.otherwise{
          mem_s := m_absorb
          pindex := 0.U
          wait_for_sram := true.B
        }
      }.otherwise{
        //don't move pindex if we haven't received a response for this index
        when(buffer_count < mindex && (pindex >= buffer_count) ){
          mem_s := m_pad
        }.otherwise{
          if(buffer_sram){
            //With SRAM, we need to increment pindex every other cycle
            when(wait_for_sram === false.B){
              pindex := pindex + 1.U
              wait_for_sram := true.B
            }.otherwise{
              wait_for_sram := false.B
            }
          }
          else{
            //Register buffer does not need to wait a cycle
            pindex := pindex + 1.U
          }
          mem_s := m_pad
        }
      }
    }  // end of m_pad
    is(m_absorb) {
      buffer_valid := true.B
      // move to idle when we know this thread was absorbed
      when(aindex >= (round_size_words-1).U) {
        mem_s := m_idle
      }
    }
  }
  // the code to process read responses
  // if(fast_mem){
  //   when(io.dmem_resp_val) {
  //     when(io.dmem_resp_tag(4,0) < UInt(round_size_words)){
  //       //This is read response
  //       if(buffer_sram){
  //         buffer_wen := Bool(true)
  //         buffer_waddr := io.dmem_resp_tag(4,0)
  //         buffer_wdata := io.dmem_resp_data
  //       }else{
  //         buffer(io.dmem_resp_tag(4,0)) := io.dmem_resp_data
  //       }
  //       buffer_count := buffer_count + UInt(1)
  //     }
  //   }
  //   when(buffer_count >= (mindex) &&
  //        (mindex >= UInt(round_size_words))){// ||
  //        //read(i) > msg_len(i))){
  //     when(read > msg_len){
  //       //padding needed
  //     }.otherwise{
  //       //next cycle the buffer will be valid
  //       buffer_valid := Bool(true)
  //     }
  //   }
  // }
  //END MEM HANDLER


  switch(state) {
    is(s_idle) {
      val canAbsorb = busy && (rindex_reg >= rounds.U && buffer_valid && hashed <= msg_len)
      when(canAbsorb) {
        busy  := true.B
        state := s_absorb
      }.otherwise{
        state := s_idle
      }
    }
    is(s_absorb) {
      io.write := !areg
      areg := true.B
      aindex := aindex + 1.U
      when(io.aindex >= (round_size_words-1).U) {
        rindex := 0.U
        sindex := 0.U
        aindex := 0.U
        // Delayed 1 cycle for sram
        // areg := false.B
        buffer_valid := false.B
        buffer_count := 0.U
        hashed := hashed + (8*round_size_words).U
        state := s_finish_abs
      }.otherwise{
        state := s_absorb
      }
    }
    is(s_finish_abs) {
      //There is a 1 cycle delay for absorb to finish (since the SRAM read is delayed by 1 cycle)
      areg  := false.B
      state := s_hash
    }
    is(s_hash) {
      when(rindex < rounds.U) {
        when(sindex < (s-1).U) {
          sindex := sindex + 1.U
          io.round := rindex
          io.stage := sindex
          io.write := false.B
          state := s_hash
        } .otherwise {
        sindex := 0.U
        rindex := rindex + 1.U
        io.round := rindex
        io.write := false.B
        state := s_hash
        }
      } .otherwise {
        io.write := true.B
        when(hashed > msg_len || (hashed === msg_len && rindex === rounds.U)){
          windex := 0.U
          state := s_write
        } .otherwise {
          state := s_idle
        }
      }
    }
    is(s_write) {
      // we are writing
      // request
      io.dmem_req_val := windex < hash_size_words.U
      io.dmem_req_addr := hash_addr
      io.dmem_req_tag := round_size_words.U + windex
      io.dmem_req_cmd := M_XWR

      when(io.dmem_req_rdy){
        windex := windex + 1.U
        hash_addr := hash_addr + 8.U
      }

      //response
      when(dmem_resp_val_reg) {
      // when(io.dmem_resp_val) {
        //there is a response from memory
        when(dmem_resp_tag_reg(4,0) >= round_size_words.U) {
          //this is a response to a write
          writes_done(dmem_resp_tag_reg(4,0) - round_size_words.U) := true.B
        }
      }
      when(writes_done.reduce(_&&_)) {
        //all the writes have been responded to
        //this is essentially reset time
        busy := false.B
        writes_done := VecInit(Seq.fill(hash_size_words){false.B})
        windex := hash_size_words.U
        rindex := (rounds+1).U
        msg_addr := 0.U
        hash_addr := 0.U
        msg_len := 0.U
        hashed := 0.U
        read := 0.U
        buffer_valid := false.B
        buffer_count := 0.U
        io.init := true.B
        state := s_idle
      }.otherwise{
        state := s_write
      }
    }
  }
}
