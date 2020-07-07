import sys

l = 0
f = open(sys.argv[1], 'r')

def getSortableNumber(number):
   numStr = str(number)
   while(len(numStr) != 3):
      numStr = "0" + numStr
   return numStr

for line in f:
   l += 1
   sl = getSortableNumber(l)
   filename = "q%s.rq" % sl
   fo = open(filename, "w")
   fo.write("SELECT * WHERE {\n")
   fo.write("  "+line)
   fo.write("}")
   fo.close
f.close()
