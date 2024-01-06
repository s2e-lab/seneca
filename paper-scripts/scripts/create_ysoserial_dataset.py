"""
This script aims to inspect the source code of ysoserial.
FInd all @Dependencies annotations, and extract their string values.

It thens download their JAR from maven central.

"""

import os
import shutil

import javalang
from javalang.tree import ElementArrayValue, Literal

from download_jars import download


def parse_ysoserial(folder_location):
    """
    Finds all java files in the folder location
    @param folder_location:  where the ysoserial repository was cloned.
    @return: the dictionary:
     <name, list of dependencies>
    """

    #     finds all java files in the folder location
    payload_files = []
    for root, dirs, files in os.walk(folder_location):
        for file in files:
            if file.endswith(".java") and root.endswith("payloads") and not ("/test/") in root:
                payload_files.append(os.path.join(root, file))

    #     for each java file, find all @Dependencies annotations
    #     and extract their string values
    dependencies = {}
    for file in payload_files:
        with open(file, 'r') as f:
            tree = javalang.parse.parse(f.read())
            for path, node in tree.filter(javalang.tree.Annotation):
                if node.name == "Dependencies":
                    for child in node.children:

                        if type(child) == ElementArrayValue:
                            for literal in child.children[0]:
                                # add to the dictionary
                                if file not in dependencies:
                                    dependencies[file] = []
                                dependencies[file].append(literal.value.replace("\"", ""))
                        elif type(child) == Literal:
                            # add to the dictionary
                            if file not in dependencies:
                                dependencies[file] = []
                            dependencies[file].append(child.value.replace("\"", ""))

    return dependencies


if __name__ == "__main__":
    ysoserial_location = os.path.expanduser("~/Documents/ForkedTools/ysoserial")
    dependencies_per_file = parse_ysoserial(ysoserial_location)
    for file_name, dependencies in dependencies_per_file.items():
        class_name = os.path.basename(file_name).replace(".java", "")
        output_folder = f"../dataset/vulnerabilities/{class_name}"
        print(f"Downloading dependencies for {class_name}")
        for dependency in dependencies:
            print("\t", end="")
            download(dependency.replace("jar:jdk15:","").split(":"), output_folder, False)

        # finds all jar files in output_folder
        to_delete = []
        for root, dirs, files in os.walk(output_folder):
            for file in files:
                if file.endswith(".jar"):
                    # copy one folder up
                    shutil.move(os.path.join(root, file), os.path.join(output_folder, file))

                    # mark for deletion
                    if root != output_folder: to_delete.append(root)
        # delete the folders
        for folder in to_delete: shutil.rmtree(folder)


