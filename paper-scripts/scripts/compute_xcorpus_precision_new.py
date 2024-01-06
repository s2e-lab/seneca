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
#       python3.9 compare_callgraphs.py batik ../results/rq2 ../dataset/build
#       python3.9 compare_callgraphs.py htmlunit ../results/rq2 ../dataset/build
#       python3.9 compare_callgraphs.py log4j ../results/rq2 ../dataset/build
#       python3.9 compare_callgraphs.py jpf ../results/rq2 ../dataset/build
#       python3.9 compare_callgraphs.py jgraph ../results/rq2 ../dataset/build
#       python3.9 compare_callgraphs.py james ../results/rq2 ../dataset/build
################################################################


import os
import re
import sys

from utils import RUNTIME_CG_FOLDER, STATIC_CG_FOLDER, read_exclusions, is_application_edge, is_extension_edge, \
    is_java_io_edge, is_classloader_edge, is_excluded_edge
from utils import get_all_classes
from utils import read_cg_from_file
from utils import save_csv


# Comparison for CGs
# We consider only call-graph edges originating from application code
# Comparison as used by Smaragdakis et 2015
def compare_cgs(project, testcase, approach, policy, runtime_cg, static_cg, node_to_scope) -> dict:
    correct = 0
    incorrect = 0  # All incorrect edges (in the static CG but not present in the runtime CG)
    library_incorrect = 0  # Edge is deemed as incorrect because it is from the java.io. package
    clinit_incorrect = 0  # Edge is deemed as incorrect because it is a class initializer method
    exclusion_incorrect = 0  # Edge is deemed as incorrect because the class was excluded from the analysis

    excluded_packages = read_exclusions(project)
    package_regex = re.compile("|".join(excluded_packages))
    mismatches = []
    all_relevant_static_nodes = set()
    all_runtime_nodes = set([x for x in runtime_cg])
    all_runtime_nodes.update([x for y in runtime_cg.values() for x in y])

    for from_node, to_node_set in static_cg.items():
        for to_node in to_node_set:
            edge = (from_node, to_node)
            if is_application_edge(edge, node_to_scope) or is_extension_edge(edge, node_to_scope):
                if from_node in runtime_cg and to_node in runtime_cg[from_node]:
                    correct += 1
                else:
                    incorrect += 1
                    if is_java_io_edge(edge) or "model.ObjectInputStream" in edge[0] or "model.ObjectInputStream" in edge[1]:
                        library_incorrect += 1
                    elif is_classloader_edge(edge):
                        clinit_incorrect += 1
                    elif is_excluded_edge(edge, package_regex):
                        exclusion_incorrect += 1
                    else:
                        all_relevant_static_nodes.add(from_node)
                        all_relevant_static_nodes.add(to_node)
                        mismatches.append([project, testcase, approach, policy, from_node, to_node])

    # total incorrect after disregarding irrelevant edges
    actual_incorrect = incorrect - library_incorrect - clinit_incorrect - exclusion_incorrect
    missing_nodes = ";".join(list(all_relevant_static_nodes - all_runtime_nodes))

    # return correct, incorrect, library_incorrect, clinit_incorrect, exclusion_incorrect, actual_incorrect, mismatches, missing_nodes
    return {
        "correct": correct,
        "incorrect": incorrect,
        "library_incorrect": library_incorrect,
        "clinit_incorrect": clinit_incorrect,
        "exclusion_incorrect": exclusion_incorrect,
        "actual_incorrect": actual_incorrect,
        "mismatches": mismatches,
        "missing_nodes": missing_nodes
    }


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
        "# Correct", "# Incorrect",
        "# java.io. Incorrect", "# <clinit> Incorrect",
        "#Excluded missing edges",
        "# Actual (minus java.io.*/<clinit>/excluded)",
        "Missed relevant nodes",
        "# Missing relevant nodes (Nodes in SCG not in RCG)"
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

    print(f"Runtime CGs: {len(runtime_cgs)}")
    merged_runtime_cg = {}
    merged_static_cg = {}
    # merge the runtime call graphs
    for _, runtime_cg in runtime_cgs.items():
        for from_node, to_node_set in runtime_cg.items():
            if from_node not in merged_runtime_cg:
                merged_runtime_cg[from_node] = set()
            merged_runtime_cg[from_node].update(to_node_set)

    print(f"Merged Runtime CG: {len(merged_runtime_cg)}")
    # traverse the dir with static call graphs and merge them
    for root, dirs, files in os.walk(STATIC_CG_FOLDER + project):
        for file in files:
            # check the files which are end with specific extension
            if file.endswith(".txt"):
                # Tracks the scopes for nodes
                node_to_scope = compute_nodes_scopes(project, jar_folder)
                static_cg = read_cg_from_file(os.path.join(root, file), node_to_scope)
                parts = file.split("-")
                approach, tc_name = parts[0], parts[-1]
                policy = file.replace("SENECA-", "")\
                    .replace("SALSA-", "")\
                    .replace("WALA-", "")\
                    .replace("-" + tc_name,"")
                if f"{approach}_{policy}" not in merged_static_cg:
                    merged_static_cg[f"{approach}_{policy}"] = {}
                for from_node, to_node_set in static_cg.items():
                    if from_node not in merged_static_cg[f"{approach}_{policy}"]:
                        merged_static_cg[f"{approach}_{policy}"][from_node] = set()
                    merged_static_cg[f"{approach}_{policy}"][from_node].update(to_node_set)

    for approach_policy, static_cg in merged_static_cg.items():
        approach, policy = approach_policy.split("_")
        result = compare_cgs(project, "ALL", approach, policy, merged_runtime_cg, static_cg, node_to_scope)
        # add the metrics to list
        rows_results.append([
            project, tc_name, approach, policy,
            result["correct"], result["incorrect"], result["library_incorrect"],
            result["clinit_incorrect"], result["exclusion_incorrect"], result["actual_incorrect"],
            result["missing_nodes"], len(result["missing_nodes"])
        ])
        rows_differences = rows_differences + result["mismatches"]


    # save the results
    save_csv(f"{results_folder}/NEW_{project}-precision.csv", rows_results)
    save_csv(f"{results_folder}/NEW_{project}-differences.csv", rows_differences)


if __name__ == '__main__':
    if len(sys.argv) != 4:
        print("ERROR! Missing argument for project name, results folder, and/or jar folder", file=sys.stderr)
    else:
        main(sys.argv[1], sys.argv[2], sys.argv[3])


# python3.9 compute_xcorpus_precision_new.py batik ../results/rq2 ../dataset/build