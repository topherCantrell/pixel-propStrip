
def parseHex(value):
    value = value.replace("_","")
    return int(value,16)

def addLong(data,value):
    data.append((value>>0)  & 0xFF)
    data.append((value>>8)  & 0xFF)
    data.append((value>>16) & 0xFF)
    data.append((value>>24) & 0xFF)
    
def addWord(data,value):
    data.append((value>>0)  & 0xFF)
    data.append((value>>8)  & 0xFF)
    
def parseParams(par):
    ret = {}
    pes = par.split(" ")
    #print pes
    for pe in pes:
        if pe=="":
            continue
        p = pe.split("=")
        ret[p[0]] = p[1]        
    #print ret
    return ret        
    
def printComment(m):
    print "    ' "+m
    pass
    
def printData(data):
    pos = -1
    for d in data:
        pos = pos + 1
        if pos==0:
            s = "  byte "
        s = s + "$"+ format(d,'02x')
        if pos==15:
            pos = -1        
            print s
            s = ""
        else:
            s = s + ","
    if s!="":
        print s[0:-1]  

    
def parseScript(raw):
    
    lines = []
    for r in raw:
        r = r.strip()
        if ";" in r:
            r = r[0:r.index(";")].strip()
        if len(r)>0:
            lines.append(r)
            
    data = []
        
    charMap = {}
    
    x=0
    while x<len(lines):
        line = lines[x]    
        x=x+1
        if line.startswith("#Palette"):
            printComment(line)
            params = parseParams(line[8:]) 
            addr = 0
            if "start" in params:
                addr = int(params["start"])
            y = x
            while y<len(lines) and not lines[y].startswith("#"):
                y = y + 1        
            cnt = y - x 
                    
            # 0A_00_XX_CC  X=Address, C=number of entries      
            printData([cnt,addr,0,0x0A])
             
            data = []            
            for y in xrange(cnt):
                addLong(data,parseHex(lines[x]))
                x=x+1
                
            printData(data)
            
            continue
        
        if line.startswith("#Chars"):
            printComment(line)
            params = parseParams(line[6:]) 
            
            chars = params["chars"]
            values = params["values"].split(",")            
            
            for z in xrange(len(chars)):
                charMap[chars[z]] = int(values[z],16)            
            
            continue   
        
        if line.startswith("#DrawBytes"):        
            printComment(line)
            params = parseParams(line[10:])             
            ox = 0
            oy = 0        
            
            if "x" in params:
                ox = int(params["x"])
            if "y" in params:
                oy = int(params["y"])            
                       
            pixdat = []
            y = x
            while y<len(lines) and not lines[y].startswith("#"):
                pixdat.append(lines[y].replace(' ',''))
                y = y + 1        
            height = y - x        
            
            width = len(pixdat[0])     
            
            # 02_00_XX_YY   one-byte-pixels, X=xOfs, Y=yOfs
            # ww_ww_hh_hh   w=dataWidth, h=dataHeight
            printData([oy,ox,0,0x02])
            printData([width&255, (width>>8)&255, height&255, (height>>8)&255])
            
            data = [] 
            for i in xrange(height):
                for j in xrange(width):                
                    data.append(charMap[pixdat[i][j]])
                x = x + 1
                
            printData(data)
             
            continue
        
        if line.startswith("#DrawLast"):
            printComment(line)
            params = parseParams(line[10:])             
            ox = 0
            oy = 0        
            
            if "x" in params:
                ox = int(params["x"])
            if "y" in params:
                oy = int(params["y"])
            
            # 20_00_XX_YY (one-byte-pixels ... use last data)     
            printData([oy,ox,0,0x20])
                
            continue        
        
        if line.startswith("#Delay"):
            printComment(line)
            params = parseParams(line[6:])
            
            dv = int(params["ms"])
            
            # 0B_DD_DD_DD  D=millisecond delay
            printData([dv&255,(dv>>8)&255,(dv>>16)&255,0x0B])            
            
            continue
        
        if line.startswith("#Restart"):
            printComment(line)
            
            # 0C_00_00_00 (Restart)
            printData([0,0,0,0x0C])
            
            continue
        
        if line.startswith("#Repeat"):
            printComment(line)
            params = parseParams(line[7:])
            cnt = int(params["count"])
            
            # 0D_CC_CC_CC (Repeat) C=Count
            printData([cnt&255,(cnt>>8)&255,(cnt>>16)&255,0x0B])            
            
            continue
        
        if line.startswith("#Next"):
            printComment(line)
            
            #' 0E_00_00_00 (Next)
            printData([0,0,0,0x0E])
            
            continue        
        
        raise Exception("UNKNOWN '"+line+"' on line "+str(x))     
            
    
    printComment("END")
    printData([0xFF,0xFF,0xFF,0xFF])
    
if __name__ == "__main__":
    
    with open("Sequence.txt") as f:
        raw = f.readlines()
    
    parseScript(raw)
                  