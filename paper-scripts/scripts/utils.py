import csv
import os
import re
import zipfile

# Paths to the folders with the runtime and static call graphs
RUNTIME_CG_FOLDER = "../runtime-cgs/"
STATIC_CG_FOLDER = "../static-cgs/"
# Path to the exclusion file used to compute the static call graphs
# EXCLUSIONS_FILE = "/Users/joanna/Google Drive/Research Assistant/Projects/Weaknesses/Dissertation/dodo/Salsa-Seneca/src/main/resources/exclusions+regex.txt"
EXCLUSIONS_FILE = "/Users/joanna/Documents/Portfolio/Bitbucket/dodo/dodo/Salsa-Seneca/src/main/resources/exclusions+regex.txt"
EXCLUSIONS_FILE_XERCES = "/Users/joanna/Documents/Portfolio/Bitbucket/dodo/dodo/Salsa-Seneca/src/main/resources/exclusions+regex-xerces.txt"

# String formatting helpers
SENECA_FILE_NAME_REGEX = "(?P<approach>.*)-(?P<policy>.*)-(TC_?P<class_name>.*).txt"
RUNTIME_CGS = "{class_name}-jvmti-output.txt"

# read a Jar file and return a set of all class names
def get_all_classes(jar_file_path: str) -> set[str]:
    class_names = set()
    with zipfile.ZipFile(jar_file_path, "r") as jar_file:
        files = jar_file.infolist()
        for file in files:
            if file.filename.endswith(".class"):
                class_names.add(file.filename.replace(".class", "").replace("/", "."))
    return class_names





# create /libraries folder if it doesn't exist
def create_folder(folder_path):
    if not os.path.exists(folder_path):
        os.makedirs(folder_path)


# Parses the exclusion file
def read_exclusions(project: str):
    exclusion_file = EXCLUSIONS_FILE if project != "xerces" else EXCLUSIONS_FILE_XERCES
    excluded_packages = []
    with open(exclusion_file, "r") as f:
        for line in f:
            package = line.replace("\/.*", ".*").replace("\/", "\.").strip()
            excluded_packages.append(package)

    return excluded_packages


def is_application_edge(edge, node_to_scope) -> bool:
    class_name = edge[0].split(":")[0]
    return (class_name in node_to_scope and node_to_scope[class_name] == "Application") \
        or class_name.startswith("edu.rit.se.") \
        or class_name.startswith("testcases.TC_")


def is_extension_edge(edge, node_to_scope) -> bool:
    class_name = edge[0].split(":")[0]
    return (class_name in node_to_scope and node_to_scope[class_name] == "Extension") \
        or class_name.startswith("edu.rit.se.") \
        or class_name.startswith("testcases.TC_")


def is_java_io_edge(edge) -> bool:
    return "java.io" in edge[0] or "java.io" in edge[1]


# True if it is an edge from/to a class loader initializer (<clinit>)
def is_classloader_edge(edge) -> bool:
    return ":<clinit>" in edge[0] or ":<clinit>" in edge[1]


# True if the edge is excluded based on the exclusions file
def is_excluded_edge(edge, regex_excluded_packages) -> bool:
    return regex_excluded_packages.match(edge[0]) or regex_excluded_packages.match(edge[1])


# We consider only call-graph edges originating from application code
# Comparison as used by Smaragdakis et 2015
def compare_application_only(project, testcase, approach, policy, runtime_cg, static_cg, node_to_scope) -> dict:
    correct = 0
    incorrect = 0  # All incorrect edges (in the runtime CG but not present in the static CG)
    library_incorrect = 0  # Edge is deemed as incorrect because it is from the java.io. package
    clinit_incorrect = 0  # Edge is deemed as incorrect because it is a class initializer method
    exclusion_incorrect = 0  # Edge is deemed as incorrect because the class was excluded from the analysis

    excluded_packages = read_exclusions(project)
    package_regex = re.compile("|".join(excluded_packages))
    mismatches = []
    all_relevant_runtime_nodes = set()
    all_static_nodes = set([x for x in static_cg])
    all_static_nodes.update([x for y in static_cg.values() for x in y])

    for from_node, to_node_set in runtime_cg.items():
        for to_node in to_node_set:
            edge = (from_node, to_node)
            if is_application_edge(edge, node_to_scope) or is_extension_edge(edge, node_to_scope):
                if from_node in static_cg and to_node in static_cg[from_node]:
                    correct += 1
                else:
                    incorrect += 1
                    if is_java_io_edge(edge):
                        library_incorrect += 1
                    elif is_classloader_edge(edge):
                        clinit_incorrect += 1
                    elif is_excluded_edge(edge, package_regex):
                        exclusion_incorrect += 1
                    else:
                        all_relevant_runtime_nodes.add(from_node)
                        all_relevant_runtime_nodes.add(to_node)
                        mismatches.append([project, testcase, approach, policy, from_node, to_node])

    # total incorrect after disregarding irrelevant edges
    actual_incorrect = incorrect - library_incorrect - clinit_incorrect - exclusion_incorrect
    missing_nodes = ";".join(list(all_relevant_runtime_nodes - all_static_nodes))

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


# Comparison for CGs
def compare_cgs(project, testcase, approach, policy, runtime_cg, static_cg, node_to_scope) -> dict:
    return compare_application_only(project, testcase, approach, policy, runtime_cg, static_cg, node_to_scope)


# Parses a Call Graph that has the following format:
# from_node to_node from_node_scope to_node_scope
def parse_cg(file_path, node_to_scope=None):
    cg = {}
    with open(file_path, "r") as f:
        for line in f:
            parts = line.split()
            from_node, to_node = parts[0].strip(), parts[1].strip()
            if from_node not in cg:
                cg[from_node] = set()
            cg[from_node].add(to_node)
            if node_to_scope and len(parts) > 2:
                node_to_scope[from_node] = parts[2].split(":")[0]
                node_to_scope[to_node] = parts[3]
    return cg


# Parse the traces from our custom-built tool
# Creates a call graph only for the main thread
def parse_jvmti_trace(file_path):
    # Method Entry:
    # Format: ">[<thread_name>] <class_name>.<method_name>(<method_signature>)<return_type>"

    pattern_in = re.compile(r">\[(.*)\] (.*)\.(.*)\(.*\).*")
    program_stack = []
    cg = dict()
    with open(file_path, "r") as f:
        for line in f:
            if line.startswith(">[main]"):  # method entry on main thread
                m = pattern_in.match(line)
                thread_name, class_name, method_name = m.group(1, 2, 3)
                method_call = class_name[1:].replace("/", ".") + ":" + method_name
                # checks program_stack is not empty
                if program_stack:
                    method_caller = program_stack[-1]
                    from_node, to_node = method_caller.strip(), method_call.strip()
                    # adds the edge
                    if from_node not in cg:
                        cg[from_node] = set()
                    cg[from_node].add(to_node)
                program_stack.append(method_call)
            elif line.startswith("<[main]") and program_stack:  # method exit on main thread
                program_stack.pop()

    return cg


# Parses a file (trace or call graph)
def read_cg_from_file(file_path, node_to_scope=None):
    if RUNTIME_CG_FOLDER in file_path:
        return parse_jvmti_trace(file_path)
    return parse_cg(file_path, node_to_scope)


def save_csv(csv_filepath: str, rows: list) -> None:
    """
        Saves the results into a CSV file
        @param csv_file: a path to a CSV file where to save the results in CSV format
        @param rows: the list of strings  to be saved (each element is a row)
    """
    with open(csv_filepath, 'w') as f:
        writer = csv.writer(f)
        for row in rows:
            writer.writerow(row)
