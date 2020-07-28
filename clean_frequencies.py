import pickle

f = open('working_experiment/final_filtered.csv')
lines = f.readlines()
filtered=[]
ok = 0
damaged=0
hf = lines[0]
lines = lines[1:]
for line in lines:
	line_fields = line.split(",")
	fr = line_fields[0]
	freqs = [0, 2201000, 2200000, 2100000, 2000000, 1900000, 1800000, 1700000, 1600000, 1500000, 1400000, 1300000,1200000]
	fr = int(fr)
	if fr in freqs:
		filtered.append(line)
		ok = ok + 1
	else:
		damaged = damaged + 1

print("Damaged %d, OK %d" %(damaged,ok))
fo = open("working_experiment/freq.csv", 'w')
#pickle.dump(filtered,fo)
fo.write(hf)
for line in filtered:
	fo.write(line)
	fo.flush()

fo.close()
