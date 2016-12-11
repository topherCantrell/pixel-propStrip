OBJ     
    NEO_API1 : "NeoPixelPlateAPI"
    NEO_API2 : "NeoPixelPlateAPI" 

pub init(neoAPIptr_1, neoAPIptr_2)
  NEO_API1.init(neoAPIptr_1)
  NEO_API2.init(neoAPIptr_2)

PUB setPalette(pal)
  NEO_API1.setPalette(pal)
  NEO_API2.setPalette(pal)

PUB waitRenderRaster(mode, pixBuf, width, height, xofs, yofs) | p, x,y

  NEO_API1.renderRaster(mode, pixBuf, width, height, xofs, yofs)
  NEO_API2.renderRaster(mode, pixBuf, width, height, xofs, yofs)
  repeat while NEO_API1.getCommand<>0 or NEO_API2.getCommand<>0
  