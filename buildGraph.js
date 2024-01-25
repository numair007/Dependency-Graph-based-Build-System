// Example build graph data (replace it with your actual data)
const buildGraphData = {
    nodes: [
        { id: 0, name: "Compile Java" },
        { id: 1, name: "Compile Resources" },
        { id: 2, name: "Run Tests" },
        { id: 3, name: "Package JAR" },
        { id: 4, name: "Deploy" }
    ],
    links: [
        { source: 0, target: 2 },
        { source: 1, target: 3 },
        { source: 2, target: 3 },
        { source: 3, target: 4 }
    ]
};

// D3.js code for rendering the build graph
const svg = d3.select("#buildGraph"),
    width = window.innerWidth,
    height = window.innerHeight;

svg.attr("width", width).attr("height", height);

// Create edges (lines) for links with arrowheads
const link = svg.selectAll("line")
    .data(buildGraphData.links)
    .enter().append("line")
    .attr("stroke", "#999")
    .attr("stroke-width", 2)
    .attr("marker-end", "url(#arrow)");

// Add arrowhead to each line
link.append("svg:marker")
    .attr("id", "arrow")
    .attr("viewBox", "0 -5 10 10")
    .attr("refX", 8)
    .attr("refY", 0)
    .attr("markerWidth", 8)
    .attr("markerHeight", 8)
    .attr("orient", "auto")
    .append("path")
    .attr("d", "M0,-5L10,0L0,5");

// Create nodes (circles) for nodes
const node = svg.selectAll("circle")
    .data(buildGraphData.nodes)
    .enter().append("circle")
    .attr("r", 20)
    .attr("fill", "blue")
    .on("mouseover", handleMouseOver)
    .on("mouseout", handleMouseOut);

// Tooltip handling functions
function handleMouseOver(event, d) {
    const tooltip = document.getElementById("tooltip");
    tooltip.innerHTML = d.name;
    tooltip.style.left = (event.pageX + 10) + "px";
    tooltip.style.top = (event.pageY - 10) + "px";
    tooltip.style.display = "block";
}

function handleMouseOut() {
    const tooltip = document.getElementById("tooltip");
    tooltip.style.display = "none";
}

// Force simulation setup
const simulation = d3.forceSimulation(buildGraphData.nodes)
    .force("link", d3.forceLink(buildGraphData.links).id(d => d.id).distance(100).strength(1))
    .force("charge", d3.forceManyBody().strength(-300))
    .force("center", d3.forceCenter(width / 2, height / 2));

// Zooming and Panning
const zoom = d3.zoom()
    .scaleExtent([0.1, 5])
    .on("zoom", handleZoom);

svg.call(zoom);

// Zoom handler function
function handleZoom(event) {
    svg.attr("transform", event.transform);
}

// Tick function for simulation
simulation.on("tick", () => {
    // Update position of edges
    link
        .attr("x1", d => Math.max(0, Math.min(width, d.source.x)))
        .attr("y1", d => Math.max(0, Math.min(height, d.source.y)))
        .attr("x2", d => Math.max(0, Math.min(width, d.target.x)))
        .attr("y2", d => Math.max(0, Math.min(height, d.target.y)));

    // Update position of nodes
    node
        .attr("cx", d => Math.max(20, Math.min(width - 20, d.x)))
        .attr("cy", d => Math.max(20, Math.min(height - 20, d.y)));
});
