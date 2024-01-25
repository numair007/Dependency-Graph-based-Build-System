import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Callable;

// BuildNode class representing nodes in the build graph
class BuildNode {
    private final String componentName;
    private final List<BuildNode> dependencies;

    public BuildNode(String componentName) {
        this.componentName = componentName;
        this.dependencies = new ArrayList<>();
    }

    public String getComponentName() {
        return componentName;
    }

    public List<BuildNode> getDependencies() {
        return dependencies;
    }

    public void addDependency(BuildNode dependency) {
        dependencies.add(dependency);
    }
}

// BuildGraph class representing the build graph
class BuildGraph {
    private final List<BuildNode> nodes;

    public BuildGraph() {
        this.nodes = new ArrayList<>();
    }

    public BuildNode addNode(String componentName) {
        BuildNode node = new BuildNode(componentName);
        nodes.add(node);
        return node;
    }

    public void addDependency(String fromComponent, String toComponent) {
        BuildNode fromNode = getNode(fromComponent);
        BuildNode toNode = getNode(toComponent);

        if (fromNode != null && toNode != null) {
            fromNode.addDependency(toNode);
        }
    }

    public BuildNode getNode(String componentName) {
        for (BuildNode node : nodes) {
            if (node.getComponentName().equals(componentName)) {
                return node;
            }
        }
        return null;
    }

    public List<BuildNode> getNodes() {
        return nodes;
    }
}

// BuildSystem class with added features
public class BuildSystem {
    private final BuildGraph buildGraph;
    private final Set<String> changedComponents;
    private final ExecutorService executorService;

    public BuildSystem() {
        this.buildGraph = new BuildGraph();
        this.changedComponents = new HashSet<>();
        this.executorService = Executors.newFixedThreadPool(3); // Example: 3 threads for parallelization
        initializeGraph();
    }

    private void initializeGraph() {
        BuildNode compileJava = buildGraph.addNode("Compile Java");
        BuildNode compileResources = buildGraph.addNode("Compile Resources");
        BuildNode runTests = buildGraph.addNode("Run Tests");
        BuildNode packageJar = buildGraph.addNode("Package JAR");
        BuildNode deploy = buildGraph.addNode("Deploy");

        buildGraph.addDependency("Compile Java", "Run Tests");
        buildGraph.addDependency("Compile Resources", "Package JAR");
        buildGraph.addDependency("Run Tests", "Package JAR");
        buildGraph.addDependency("Package JAR", "Deploy");
    }

    public List<BuildNode> getNodesForUI() {
        return new ArrayList<>(buildGraph.getNodes());
    }

    public void buildComponent(String componentName) {
        BuildNode node = buildGraph.getNode(componentName);

        if (node != null) {
            if (!changedComponents.contains(componentName)) {
                System.out.println("Skipping build for unchanged component: " + componentName);
                return;
            }

            System.out.println("Building: " + node.getComponentName());

            List<BuildNode> dependencies = node.getDependencies();
            List<Callable<Void>> tasks = new ArrayList<>();

            // Build dependencies in parallel
            for (BuildNode dependency : dependencies) {
                tasks.add(() -> {
                    buildComponent(dependency.getComponentName());
                    return null;
                });
            }

            try {
                executorService.invokeAll(tasks); // Parallelize the build process
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                e.printStackTrace();
            }

            // Simulate file changes for the current component
            System.out.println("Simulating changes for files associated with: " + componentName);
            // ... (Add logic to track file changes)

            // Mark the component as built
            changedComponents.remove(componentName);
        }
    }

    public void markComponentChanged(String componentName) {
        changedComponents.add(componentName);
    }

    public void shutdown() {
        try {
            executorService.shutdown();
            executorService.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            if (!executorService.isTerminated()) {
                System.err.println("ExecutorService did not terminate");
            }
        }
    }

    public static void main(String[] args) {
        BuildSystem buildSystem = new BuildSystem();
        List<BuildNode> nodesForUI = buildSystem.getNodesForUI();

        System.out.println("Nodes for UI:");
        for (BuildNode node : nodesForUI) {
            System.out.println("Node: " + node.getComponentName());
            System.out.println("Dependencies: " + node.getDependencies().stream()
                    .map(BuildNode::getComponentName)
                    .reduce((s1, s2) -> s1 + ", " + s2)
                    .orElse("None"));
            System.out.println();
        }

        // Mark changes in components
        buildSystem.markComponentChanged("Compile Java");
        buildSystem.markComponentChanged("Compile Resources");

        System.out.println("Building Components with Changes:");
        buildSystem.buildComponent("Compile Java");

        // Shutdown the executor service when the build is complete
        buildSystem.shutdown();
    }
}
