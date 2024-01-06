import os


def should_include(edge):
    n_from, n_to = edge
    return  (n_from.startswith("ser.") or n_from.startswith("java.io.")) and (n_to.startswith("ser.") or n_to.startswith("java.io."))

def compare_cgs(runtime_cg, static_cg):
    correct = 0
    wrong = 0
    for edge in static_cg:
        if should_include(edge):
            if not edge in runtime_cg: 
                wrong = wrong + 1
            else:  
                correct = correct + 1 
    
    return (correct, wrong)

def read_cg_from_file(cg_path):
    cg = set()
    with open(cg_path,"r") as f:
        for line in f:
            parts = line.split()
            cg.add((parts[0],parts[1]))
    return cg


def main():
    
    print(f"TC\tApproach\t# Correct\t# Incorrect")

    for i in range(1,10):
        runtime_cg = read_cg_from_file(f"../runtime-cgs/cats/Ser{i}-JRE1.8-output.txt")
        for approach in ("Soot","OPAL", "Seneca","Salsa"):
            for algorithm in ("CHA","RTA","0-1-CFA","1-CFA"):
                static_cg_path = f"../static-cgs/cats/Ser{i}-{approach}-{algorithm}.txt"
                if os.path.exists(static_cg_path):
                    static_cg = read_cg_from_file(static_cg_path)        
                    results = compare_cgs(runtime_cg,static_cg)
                    print(f"Ser{i}\t{approach} ({algorithm})\t{results[0]}\t{results[1]}")


if __name__ == '__main__':
    main()