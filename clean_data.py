import pickle
import argparse

parser = argparse.ArgumentParser(description="Clean Data",formatter_class=argparse.RawDescriptionHelpFormatter)
parser.add_argument("-path", "--path",      type=str, help="Path to experiment set")
parser.add_argument("-file", "--file",      type=str, help="CSV File Name")
args = parser.parse_args()
path=args.path
file=args.file

f = open('%s/%s' %(path,file))
lines = f.readlines()
filtered=[]
ok = 0
damaged=0



damaged=0
ok=0
fields_filtered=[]
for line in lines:
	line_fields = line.split(",")
	if len(line_fields) == 11:
		ok=ok+1
		fields_filtered.append(line)
	else:
		damaged = damaged + 1


print("Fields Ok %d, Damaged %d" %(ok,damaged))

#pickle.dump(filtered,fo)
#200
filtered=[]
for line in fields_filtered:
	line_fields = line.split(",")
	fr = line_fields[0]
	freqs = [0, 2201000, 2200000, 2100000, 2000000, 1900000, 1800000, 1700000, 1600000, 1500000, 1400000, 1300000,1200000]
	fr = int(fr)
	if fr in freqs:
		filtered.append(line)
		ok = ok + 1
	else:
		damaged = damaged + 1

print("Frequency Damaged %d, OK %d" %(damaged,ok))
fo = open("%s/%s.clean" %(path,file), 'w')
for line in filtered:
	fo.write(line)
	fo.flush()

fo.close()
