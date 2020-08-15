import pickle

f = open('working_experiment/kenan.csv')
lines = f.readlines()
filtered=[]
damaged=0
ok=0
for line in lines:
	line_fields = line.split(",")
	if len(line_fields) == 11:
		ok=ok+1
		filtered.append(line)
	else:
		damaged = damaged + 1


print("Ok %d, Damaged %d" %(ok,damaged))
fo = open("working_experiment/final_filtered.csv", 'w')
#pickle.dump(filtered,fo)
for line in filtered:
	fo.write(line)
	fo.flush()

fo.close()
