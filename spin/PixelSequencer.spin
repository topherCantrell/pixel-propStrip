VAR

    ' If sequencer is running in a separate COG
    long   PixelSequencerStack[64]
    long   PixelSequencerCOG
            
    ' Palette
    long   pal[256]   ' Use for one-byte mode

    ' Up to 8 nested repeat-counters
    long   repeatAddr[8]
    long   repeatCnt[8]
    long   repeatInd             

OBJ
    'NEO_API : "NeoPixelPlateAPI"
    NEO_API  : "MultiNeoPixelPlateAPI"    

pub init(neoAPIptr_1, neoAPIptr_2)
  ' Currently there is no COG running
  NEO_API.init(neoAPIptr_1, neoAPIptr_2)
    
  PixelSequencerCOG := -1    
  
pub startSequencer(ptr)
  ' Only start one COG
  if PixelSequencerCOG == -1
    PixelSequencerCOG := cognew(sequencer(ptr), @PixelSequencerStack)

pub stopSequencer
  ' Only stop if one is running
  if PixelSequencerCOG > -1
    cogstop(PixelSequencerCOG)
    PixelSequencerCOG := -1



    
pub sequencer(ptr) | og, w, cmd, addr, ct, p, i,x , y, lastDraw, lastWidth, lastHeight

  ' TODO
  ' Commands for 4-byte-pixel mode. (Not supporting 4-bit-pixel mode in sequencer)

  ' Just a default 4-color palette
  pal[0] := $00_00_00
  pal[1] := $0F_00_00
  pal[2] := $00_0F_00
  pal[3] := $00_00_0F

  NEO_API.setPalette(@pal)  
    
  og := ptr
  repeatInd := -1

  repeat  
    w := long[ptr]
    ptr := ptr + 4
    cmd := w>>24

    
    if cmd==$02
      ' 02_00_XX_YY   one-byte-pixels, X=xOfs, Y=yOfs
      ' ww_ww_hh_hh   w=dataWidth, h=dataHeight
      '
      x := (w>>8) & $FF
      y := w & $FF        
      w := long[ptr]                                                    
      ptr := ptr + 4
      lastHeight := w & $FF_FF
      lastWidth := (w>>16) & $FF_FF         
      lastDraw := ptr

      NEO_API.waitRenderRaster(2, ptr, lastWidth, lastHeight, x, y)
      
      ptr := ptr + lastHeight*lastWidth
      next

      
    if cmd==$20
      ' 20_00_XX_YY (one-byte-pixels ... use last data)
      '
      x := (w>>8) & $FF
      y := w & $FF
      NEO_API.waitRenderRaster(2, lastDraw, lastWidth, lastHeight, x, y)

        
    if cmd==$0A
      ' 0A_00_XX_CC  X=Address, C=number of entries
      '
      addr := (w>>8) & $FF  ' Offset entry in pal
      ct := w & $FF         ' Number of entries
      p := @pal + addr*4 
      repeat i from 1 to ct
        long[p] := long[ptr]
        p := p + 4
        ptr := ptr + 4
      next

      
    if cmd==$0B
      ' 0B_DD_DD_DD  D=millisecond delay
      '
      PauseMSec(w & $FF_FF_FF)
      next
      

    if cmd==$0C
      ' 0C_00_00_00 (Restart)
      '
      repeatInd := -1
      ptr := og
      next
      

    if cmd==$0D
      ' 0D_CC_CC_CC (Repeat) C=Count
      '
      repeatInd := repeatInd + 1
      repeatAddr[repeatInd] := ptr
      repeatCnt[repeatInd] := w & $FF_FF_FF
      next
      

    if cmd==$0E
      ' 0E_00_00_00 (Next)
      '
      repeatCnt[repeatInd] := repeatCnt[repeatInd] -1
      if repeatCnt[repeatInd] > 0
        ptr := repeatAddr[repeatInd]
      else
        repeatInd := repeatInd - 1
      next

      
    if cmd==$FF
      ' FF_FF_FF_FF (End)
      return                                 
  
PRI PauseMSec(Duration)
  waitcnt(((clkfreq / 1_000 * Duration - 3932) #> 381) + cnt)