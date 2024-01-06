import json

import os
def parse_gadget_inspector(file_path):
    results = []
    with open(file_path, 'r') as f:
        content = f.read()

        vulnerable_paths = content.split('\n\n')
        for p in vulnerable_paths:
            path_id = 1
            nodes = p.strip().split('\n')
            results.append({
                "id": path_id,
                "path": []
            })
            for n in nodes:
                results[-1].append({
                    "method": n.strip().split(" ")[0]
                })


    return results


def parse_seneca(file_path: str) -> []:
    """
    Parse the seneca output file and print the vulnerable paths.
    @rtype: object
    @param file_path:
    @return: a list of vulnerable paths
    """
    with open(file_path, 'r') as f:
        vulnerable_paths = json.loads(f.read())
    return vulnerable_paths


def list_by_size():
    # list folders in dataset/vulnerabilities
    root = "../dataset/vulnerabilities"
    folders = [x for x in os.listdir(root) if not x.startswith("_") and os.path.isdir(os.path.join(root, x))]
    sizes = {}
    # list files in each folder
    for folder in folders:
        sizes[folder] = 0
        jar_files = [x for x in os.listdir(os.path.join(root, folder)) if x.endswith(".jar")]
        for jar_file in jar_files:
            # get size of jar file
            sizes[folder] += os.path.getsize(os.path.join(root, folder, jar_file))


    # print sorted by values
    for k, v in sorted(sizes.items(), key=lambda item: item[1]):
        print(f"{k}\t{v}")
        # print(f"YSoSerialTestCases.{k.upper()},")

# ifmain
if __name__ == '__main__':
    # parse_gadget_inspector('../results/rq4/gadget-inspector_batik-testcases-1.0.txt')


    # recursively list all files in a folder
    rq4_folder = '../results/rq4'
    vulnerable_paths_files = [ os.path.join(rq4_folder,x) for x in os.listdir(rq4_folder) if x.endswith(".json") ]
    results = []
    for file in vulnerable_paths_files:
        vulnerable_paths = parse_seneca(file)
        project, approach, policy = file.replace("_vuln_paths.json","").split("_")

        if len(vulnerable_paths) == 0:
            results.append("\t".join( [os.path.basename(project), approach, policy, "0",""]))
        for p in vulnerable_paths:
            results.append("\t".join([os.path.basename(project), approach, policy, str(len(p["path"])), p["path"][-1]["method"]]))
            # print(p["id"], len(p["path"]))
            # for n in p["path"]:
            #     print("\t",n["method"],sep="")


    # save on a file
    with open(os.path.join(rq4_folder,"aggregated_results.tsv"), "w") as f:
        f.write("\n".join(results))



