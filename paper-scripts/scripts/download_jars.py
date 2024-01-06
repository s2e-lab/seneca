import csv
import subprocess
import xml.etree.ElementTree as ET

import requests

from utils import create_folder

MAVEN_URL = "https://repo1.maven.org/maven2"
DOWNLOAD_FOLDER = '../dataset/maven/libraries'


def load(file_path: str) -> []:
    libraries = set()
    # Open the CSV file
    with open(file_path, 'r') as f:
        reader = csv.reader(f)
        # iterate over each row
        for row in reader:
            parts = row[0].split("/")
            group_id = parts[-2]
            artifact_id = parts[-1]
            libraries.add((group_id, artifact_id))

    return libraries


def download_dependencies(pom):
    print(f"Downloading the dependencies for {pom.split('/')[-1].replace('.pom', '')}")
    # The command to copy the dependencies to the target/dependency/ directory
    mvn_dependency_copy = f"mvn -f {pom} dependency:copy-dependencies"

    # Execute the command
    process = subprocess.Popen(mvn_dependency_copy, shell=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
    output, error = process.communicate()

    # Check if the command was successful
    if process.returncode != 0:
        print(f"Error in downloading the dependencies for {pom}")
        if error: print("Error message: " + error.decode("utf-8"))


def parse_metadata(group_id, artifact_id):
    results = []
    # URL encode the groupId
    group_id_url = group_id.replace(".", "/")

    # Download the maven-metadata.xml
    url = f"https://repo.maven.apache.org/maven2/{group_id_url}/{artifact_id}/maven-metadata.xml"
    r = requests.get(url)

    # save the XML
    filepath = f"{DOWNLOAD_FOLDER}/{group_id}-{artifact_id}-maven-metadata.xml"
    with open(filepath, 'wb') as f:
        f.write(r.content)

    # Parse the XML
    try:
        tree = ET.parse(filepath)
        root = tree.getroot()
        for v in root.iterfind("versioning/versions/version"):
            results.append((group_id, artifact_id, v.text))
    except:
        print(f"Error in parsing the maven-metadata.xml for {group_id}:{artifact_id}")
    return results


def download(library, download_folder=DOWNLOAD_FOLDER, include_dependencies=True):
    """
    Downloads the specified library from maven central.
    @param library: a tuple of (group_id, artifact_id, version)
    @param download_folder: where to save the artifact
    @param include_dependencies:  whether to download the dependencies of the artifact (true by default)
    @return:
    """
    group_id, artifact_id, version = library
    print(f"Downloading {group_id}:{artifact_id}:{version}")
    maven_path = f"{group_id.replace('.', '/')}/{artifact_id}/{version}/{artifact_id}-{version}"

    # make an HTTP request to the url to download the jar
    r = requests.get(f"{MAVEN_URL}/{maven_path}.jar")

    # save the jar
    create_folder(f"{download_folder}/{group_id}-{artifact_id}-{version}")
    filepath = f"{download_folder}/{group_id}-{artifact_id}-{version}/{group_id}-{artifact_id}-{version}"
    with open(f"{filepath}.jar", 'wb') as f:
        f.write(r.content)

    # save the pom
    r = requests.get(f"{MAVEN_URL}/{maven_path}.pom")
    with open(f"{filepath}.pom", 'wb') as f:
        f.write(r.content)

    if include_dependencies: include_dependencies(f"{filepath}.pom")


def process_libraries(libs):
    for library in libs:
        group_id, artifact_id = library
        all_versions = parse_metadata(group_id, artifact_id)
        all_versions.reverse()  # reverse the list to get the latest versions first
        # downloads the latest 10 versions
        for i in range(10):
            if i < len(all_versions):
                download(all_versions[i])


if __name__ == '__main__':
    libraries = load('../dataset/maven/artifacts.csv')
    create_folder(DOWNLOAD_FOLDER)
    process_libraries(libraries)
