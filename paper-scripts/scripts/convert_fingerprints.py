import json


# This scripts converts the fingerprints (JSON files) generated by Reif et al's study to text 
# The format matches our format (each line is an edge with the source/dst method signatures)

FINGERPRINTS = [
    "../fingerprints/Ser8/Soot/CHA/cg.json",
    "../fingerprints/Ser8/Soot/RTA/cg.json",
    
    "../fingerprints/Ser9/Soot/CHA/cg.json",
    "../fingerprints/Ser9/Soot/RTA/cg.json",
    
    "../fingerprints/Ser1/OPAL/RTA/cg.json",
    "../fingerprints/Ser2/OPAL/RTA/cg.json",
    "../fingerprints/Ser5/OPAL/RTA/cg.json",
    "../fingerprints/Ser7/OPAL/RTA/cg.json",
    "../fingerprints/Ser9/OPAL/RTA/cg.json",
]

OUTPUT_FOLDER = "../static-cgs/cats"

def method_to_str(method):
    declaring_class = method["declaringClass"].replace("/",".")[1:len(method["declaringClass"])-1]
    name = method["name"]
    return f"{declaring_class}:{name}"

def main():
    cg_stats = dict()
    # serializes the call graphs
    for filepath in FINGERPRINTS:
        results = set()
        nodes = set()
        edges = set()
        with open(filepath) as f: 
            data = json.load(f) 

            for metadata in data["reachableMethods"]:
                method = metadata["method"]
                declaring_class = method["declaringClass"]
                source = method_to_str(method)
                nodes.add(source)
                for callsite in metadata["callSites"]:
                    for target in callsite["targets"]:
                        dest = method_to_str(target)
                        edge_txt = f'{source}\t{dest}\n'
                        results.add(edge_txt)
                        nodes.add(dest)
                        edges.add(edge_txt)

        output_file = filepath.replace("../fingerprints/","").replace("/","-").replace("-cg.json",".txt")
        cg_stats[output_file] = {"num_nodes":len(nodes), "num_edges": len(edges)}
        print("Saving", output_file)
        # with open(f"{OUTPUT_FOLDER}/{output_file}","w") as out:
        #     for edge in results:
        #         out.write(edge)
    
    # saves the stats
    print("Saving Stats")
    with open(f"{OUTPUT_FOLDER}/cg_stats-OPAL-Soot.txt","w") as out:
        for output_file in cg_stats:
            test_case, approach , policy = output_file.replace(".txt","").split("/")[-1].split("-")
            num_nodes = cg_stats[output_file]["num_nodes"]
            num_edges = cg_stats[output_file]["num_edges"]
            out.write(f"{test_case}\t{approach}\t{policy}\t{num_nodes}\t{num_edges}\n")
        

            


if __name__ == '__main__':
    main()