################################################################
# This script compares static and dynamic call graphs of a program.
# Author: Joanna C S Santos (joannacss@nd.edu)
# Output:
#   - $RESULTS_FOLDER/${project_name}-results.csv
#       The results of the comparison
#   - $RESULTS_FOLDER/${project_name}-differences.csv
#       Any found differences (i.e., edges that are missing in the static call graph)
# Usage:
#       `python3.9 compare_callgraphs.py <project_name> <results_folder>`
# Sample usages:
#       python3.9 compare_callgraphs.py batik ../results/rq1 ../dataset/build
#       python3.9 compare_callgraphs.py htmlunit ../results/rq1 ../dataset/build
#       python3.9 compare_callgraphs.py log4j ../results/rq1 ../dataset/build
#       python3.9 compare_callgraphs.py jpf ../results/rq1 ../dataset/build
#       python3.9 compare_callgraphs.py jgraph ../results/rq1 ../dataset/build
#       python3.9 compare_callgraphs.py james ../results/rq1 ../dataset/build
################################################################


import os
import sys

from utils import RUNTIME_CG_FOLDER, STATIC_CG_FOLDER
from utils import compare_cgs
from utils import get_all_classes
from utils import read_cg_from_file
from utils import save_csv


def compute_nodes_scopes(project, jar_folder):
    jar_file_path = f"{jar_folder}{os.sep}{project}-testcases.jar"
    node_to_scope = {k: "Application" for k in get_all_classes(jar_file_path)}
    # iterates over the jar_folder
    for root, dirs, files in os.walk(f"{jar_folder}{os.sep}lib{os.sep}{project}-testcases"):
        for file in files:
            if file.endswith(".jar"):
                # get the jar file name
                jar_file_path = os.path.join(root, file)
                # update the node_to_scope
                for class_name in get_all_classes(jar_file_path):
                    node_to_scope[class_name] = "Extension"

    return node_to_scope


def main(project, results_folder, jar_folder):
    # initialize with header for comparison metrics
    rows_results = [[
        "Project", "TC", "Approach", "Policy",
        "# Correct", "# Missed",
        "# java.io. Missed", "# <clinit> Missed",
        "#Excluded missing edges",
        "# Actual (minus java.io.*/<clinit>/excluded)",
        "Missed relevant nodes",
        "# Missing relevant nodes (in RCG not in SCG)"
    ]]

    # initialize with header for the differences
    rows_differences = [[
        "Project", "TC", "Approach", "Policy", "FromNode", "ToNode"
    ]]

    # {project_name: cg}
    runtime_cgs = {}

    # traverse the dir with runtime call graphs
    for root, dirs, files in os.walk(RUNTIME_CG_FOLDER + project):
        for file in files:
            if file.endswith("-output.txt"):
                runtime_cg = read_cg_from_file(os.path.join(root, file))
                tc_name = file.split("-")[0]
                runtime_cgs[project + "_" + tc_name] = runtime_cg

    # traverse the dir with static call graphs
    for root, dirs, files in os.walk(STATIC_CG_FOLDER + project):
        for file in files:
            # check the files which are end with specific extension
            if file.endswith(".txt"):
                # Tracks the scopes for nodes
                node_to_scope = compute_nodes_scopes(project, jar_folder)

                static_cg = read_cg_from_file(os.path.join(root, file), node_to_scope)
                parts = file.split("-")
                approach, tc_name = parts[0], parts[-1]
                policy = file.replace("SENECA-", "").replace("SALSA-", "").replace("WALA-", "").replace("-" + tc_name,
                                                                                                        "")
                tc_name = tc_name.replace(".txt", "")
                runtime_cg = runtime_cgs[project + "_" + tc_name]

                result = compare_cgs(project, tc_name, approach, policy, runtime_cg, static_cg, node_to_scope)
                # add the metrics to list
                rows_results.append([
                    project, tc_name, approach, policy,
                    result["correct"], result["incorrect"], result["library_incorrect"],
                    result["clinit_incorrect"], result["exclusion_incorrect"], result["actual_incorrect"],
                    result["missing_nodes"], len(result["missing_nodes"])
                ])
                rows_differences = rows_differences + result["mismatches"]

    # save the results
    save_csv(f"{results_folder}/{project}-results.csv", rows_results)
    save_csv(f"{results_folder}/{project}-differences.csv", rows_differences)


if __name__ == '__main__':
    if len(sys.argv) != 4:
        print("ERROR! Missing argument for project name, results folder, and/or jar folder", file=sys.stderr)
    else:
        main(sys.argv[1], sys.argv[2], sys.argv[3])
