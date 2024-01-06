import glob
import csv
import matplotlib.pyplot as plt
from matplotlib_venn import venn2, venn3, venn3_circles, venn2_circles, venn2_unweighted, venn3_unweighted


def parse_csv(csv_file):
    # key = <project, approach>
    results = dict()
    projects = set()
    approaches = set()
    policies = set()

    # data has headers, use DictReader
    with open(csv_file, newline='') as csvfile:
        reader = csv.DictReader(csvfile)
        for row in reader:
            project = row["Project"]
            approach = row["Approach"]
            policy = row["Policy"]
            from_node = row["FromNode"]
            to_node = row["ToNode"]

            # add to results
            key = (project, approach, policy)
            if key not in results:
                results[key] = set()
            results[key].add((from_node, to_node))
            # add to sets
            projects.add(project)
            approaches.add(approach)
            policies.add(policy)


    return results, projects, approaches, policies



def plot_venn(set_list, set_names, title=""):
    # Create the Venn diagram
    if len(set_list) == 3:
        venn2_unweighted(set_list, set_names,  set_colors=("orange", "blue", "red"), alpha=0.7)
        venn3(set_list,linestyle="solid", linewidth=2)
    if len(set_list) == 2:
        venn2(set_list, set_names,  set_colors=("blue", "red"), alpha=0.7)
        venn2_circles(set_list, linestyle="solid", linewidth=1)


    # Display the plot
    plt.title(title)
    plt.show()





def find_csv_files(folder):
    return glob.glob(f"{folder}/*-differences.csv", recursive=True)



if __name__ == "__main__":


    csv_files = find_csv_files("../results/rq1/")
    for csv_file in csv_files:
        results, projects, approaches, policies = parse_csv(csv_file)
        for policy in policies:
            for project in projects:
                print(project)
                seneca = (project, "SENECA", policy)
                salsa = (project, "SALSA", policy)
                wala = (project, "WALA", policy)

                plot_venn([results.get(seneca, set()), results[salsa]], (f"SENECA {policy}", f"SALSA {policy}"), title=project)
                plot_venn([results.get(seneca, set()), results[wala]], (f"SENECA {policy}", f"WALA {policy}"), title=project)




# # Sample sets
# set1 = {1, 2, 3, 4}
# set2 = {3, 4, 5, 6}
# set3 = {5, 6, 7, 8}
# set_names = ("WALA", "Salsa", "SENECA")
# plot_venn([set1, set2,set3], set_names, title="Sample Venn Diagram")