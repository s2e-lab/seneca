from utils import RUNTIME_CG_FOLDER
from utils import read_cg_from_file

missing_edges = [
    # ("org.apache.log4j.helpers.Loader:getTCL", "java.lang.reflect.Method:invoke"),
    # ("org.apache.log4j.spi.ThrowableInformation:getThrowableStrRep", "org.apache.log4j.Hierarchy:getThrowableRenderer"),
    # ("org.apache.log4j.DefaultThrowableRenderer:render","java.util.ArrayList:<init>"),
]
# project_folder = "log4j/"
# filename = "Seneca-0-1-CFA-TC_LoggingEvent_Map.txt"
# filename = "Salsa-0-1-CFA-TC_LoggingEvent_Array.txt"
# filename = "TC_LoggingEvent_Array-jvmti-output.txt"


def compute_slice(call_graph, edge):
    # slice computation initialization
    cg_slice = set()
    cg_slice.add(edge)
    prior_nodes = call_graph.get(edge[0], set())
    visited_edges = set(edge)  # keeps track of visited edges, in case of (mutual-)recursion.
    while len(prior_nodes) > 0:
        current_prior_edges = set()
        for prior_edge in prior_nodes:
            if prior_edge not in visited_edges:
                cg_slice.add(prior_edge)
                visited_edges.add(prior_edge)
                current_prior_edges = current_prior_edges.union(call_graph.get(prior_edge[0], set()))
        prior_nodes = current_prior_edges
    return cg_slice


def print_slice_as_dot(cg_slice, missing_edge):
    print("digraph G {")
    print("\trankdir = LR;")
    print("\tnode[style = filled, fillcolor = \"white\", shape = box, margin = 0.02, width = 0, height = 0];")
    print('\t"', missing_edge[0], "\"[fillcolor=\"salmon\"]; \"", missing_edge[1], "\"[fillcolor=\"salmon\"]; ", sep="")
    for path_edge in cg_slice:
        print('\t"', path_edge[0], '" -> "', path_edge[1], '"', sep="")
    print("}")


def main():
    # static_cg_filepath = STATIC_CG_FOLDER + project_folder + filename
    # static_cg = read_cg_from_file(static_cg_filepath)

    cg_filepath = RUNTIME_CG_FOLDER + project_folder + filename
    original_call_graph = read_cg_from_file(cg_filepath)
    # a backward call graph is represented as:
    #   cg[to_node] = { (from_node_1, to_node), ..., (from_node_n,to_node_n)}
    call_graph = dict()
    for edge in original_call_graph:
        from_node_list = call_graph.get(edge[1], set())
        from_node_list.add(edge)
        call_graph[edge[1]] = from_node_list

    for edge in missing_edges:
        cg_slice = compute_slice(call_graph, edge)
        print_slice_as_dot(cg_slice=cg_slice, missing_edge=edge)


if __name__ == '__main__':
    main()
