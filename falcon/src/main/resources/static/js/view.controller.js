/**
 * Created by isurul on 14/3/2017.
 */
angular
    .module('watchdogView', [])
    .controller('view', function ($http, $scope, $timeout) {
        var self = this;
        var treeData;
        var timeoutPromise;

        drawLegend();

        tick();

        function tick() {
            $http.get('/watchdogclient/view').then(function (response) {
                treeData = response.data[0];
                drawTree();
               timeoutPromise = $timeout(tick, 1000);
            });
        }

        $scope.$on('$locationChangeStart', function() {
            $timeout.cancel(timeoutPromise)
        });

        function drawTree() {


        /*var treeData =
                  {
                    "name": "Top Level",
                    "value": 10,
                    "type": "black",
                    "level": "red",
                    "children": [
                      {
                        "name": "Level 2: A",
                        "value": 15,
                        "type": "grey",
                        "level": "red",
                        "children": [
                          {
                            "name": "Son of A",
                            "value": 5,
                            "type": "steelblue",
                            "level": "orange"
                          },
                          {
                            "name": "Daughter of A",
                            "value": 8,
                            "type": "steelblue",
                            "level": "red"
                          }
                        ]
                      },
                      {
                        "name": "Level 2: B",
                        "value": 10,
                        "type": "grey",
                        "level": "green"
                      }
                    ]
                  };*/


           // set the dimensions and margins of the diagram
           var margin = {top: 20, right: 100, bottom: 30, left: 110},
               width = 660 - margin.left - margin.right,
               height = 500 - margin.top - margin.bottom;

           // declares a tree layout and assigns the size
           var treemap = d3.tree()
               .size([height, width]);

           //  assigns the data to a hierarchy using parent-child relationships
           var nodes = d3.hierarchy(treeData, function(d) {
               return d.children;
             });

           // maps the node data to the tree layout
           nodes = treemap(nodes);

           // append the svg object to the body of the page
           // appends a 'group' element to 'svg'
           // moves the 'group' element to the top left margin
            d3.select("#main-view").select("svg").remove();
           var svg = d3.select("#main-view").append("svg")
                 .attr("width", width + margin.left + margin.right)
                 .attr("height", height + margin.top + margin.bottom),
               g = svg.append("g")
                 .attr("transform",
                       "translate(" + margin.left + "," + margin.top + ")");

           // adds the links between the nodes
           var link = g.selectAll(".link")
               .data( nodes.descendants().slice(1))
             .enter().append("path")
               .attr("class", "link")
               .style("stroke", function(d) { return d.data.level; })
               .attr("d", function(d) {
                  return "M" + d.y + "," + d.x
                    + "C" + (d.y + d.parent.y) / 2 + "," + d.x
                    + " " + (d.y + d.parent.y) / 2 + "," + d.parent.x
                    + " " + d.parent.y + "," + d.parent.x;
                  });

           // adds each node as a group
           var node = g.selectAll(".node")
               .data(nodes.descendants())
             .enter().append("g")
               .attr("class", function(d) {
                 return "node" +
                   (d.children ? " node--internal" : " node--leaf"); })
               .attr("transform", function(d) {
                 return "translate(" + d.y + "," + d.x + ")"; });

           // adds symbols as nodes
           /*node.append("path")
             .style("stroke", function(d) { return d.data.type; })
             .style("fill", function(d) { return d.data.level; })
             .attr("d", d3.symbol()
                .size(function(d) { return d.data.value * 30; } )
                .type(function(d) { if
                  (d.data.value >= 9) { return d3.symbolCross; } else if
                  (d.data.value <= 9) { return d3.symbolDiamond;}
                }));*/
           node.append("circle")
                               .attr("r", function(d) { return d.data.value; })
                               .style("stroke-width", function(d) { return d.data.outer; })
                               .style("stroke", function(d) { return d.data.type; })
                               .style("fill", function(d) { return d.data.level; });

           // adds the text to the node
           node.append("text")
             .attr("dy", ".35em")
             .attr("x", function(d) { return d.children ?
               (d.data.value + 4) * -1 : d.data.value + 4 })
             .style("text-anchor", function(d) {
               return d.children ? "end" : "start"; })
             .text(function(d) { return d.data.name; });
        }

        function drawLegend() {
            var circleX = 21;
            var circleY = 15;
            var circleY2 = 21;
            var circleR = 15;
            var circleS = 4;
            var textX = 45;
            var textY = 20;

            var svgWidth = 150;
            var svgHeight = 50;

            var tempSvg =  d3.select("#view-legend").append("svg").attr("width", 200).attr("height", svgHeight);
            tempSvg.append("text").attr("x", 0).attr("y", 30).attr("font-size", "15px")
                .attr("fill", "grey").text("COMPONENT STATES");

            tempSvg =  d3.select("#view-legend").append("svg").attr("width", svgWidth).attr("height", svgHeight);
            tempSvg.append("circle").attr("cx", circleX).attr("cy", circleY).attr("r", circleR)
                .style("fill", "red");
            tempSvg.append("text").attr("x", textX).attr("y", textY).attr("fill", "grey").text("CLOSED");

            tempSvg =  d3.select("#view-legend").append("svg").attr("width", svgWidth).attr("height", svgHeight);
            tempSvg.append("circle").attr("cx", circleX).attr("cy", circleY).attr("r", circleR)
                .style("fill", "orange");
            tempSvg.append("text").attr("x", textX).attr("y", textY).attr("fill", "grey").text("SUSPENDED");

            tempSvg =  d3.select("#view-legend").append("svg").attr("width", svgWidth).attr("height", svgHeight);
            tempSvg.append("circle").attr("cx", circleX).attr("cy", circleY).attr("r", circleR)
                .style("fill", "yellow");
            tempSvg.append("text").attr("x", textX).attr("y", textY).attr("fill", "grey").text("INITIALIZING");

            tempSvg =  d3.select("#view-legend").append("svg").attr("width", svgWidth).attr("height", svgHeight);
            tempSvg.append("circle").attr("cx", circleX).attr("cy", circleY).attr("r", circleR)
                .style("fill", "blue").style("float", "left");
            tempSvg.append("text").attr("x", textX).attr("y", textY).attr("fill", "grey").text("CONNECTING");

            tempSvg =  d3.select("#view-legend").append("svg").attr("width", svgWidth).attr("height", svgHeight);
            tempSvg.append("circle").attr("cx", circleX).attr("cy", circleY).attr("r", circleR)
                .style("fill", "green").style("float", "left");
            tempSvg.append("text").attr("x", textX).attr("y", textY).attr("fill", "grey").text("CONNECTED");

            tempSvg =  d3.select("#view-legend").append("svg").attr("width", 200).attr("height", svgHeight);
            tempSvg.append("text").attr("x", 0).attr("y", 30).attr("font-size", "15px")
                .attr("fill", "grey").text("PREVIOUS STATES");

            tempSvg =  d3.select("#view-legend").append("svg").attr("width", svgWidth).attr("height", svgHeight);
            tempSvg.append("circle").attr("cx", circleX).attr("cy", circleY2).attr("r", circleR)
                .style("fill", "#2a2a2a").style("stroke", "yellow").style("stroke-width", circleS);
            tempSvg.append("text").attr("x", textX).attr("y", textY).attr("fill", "grey").text("CLOSED");

            tempSvg =  d3.select("#view-legend").append("svg").attr("width", svgWidth).attr("height", svgHeight);
            tempSvg.append("circle").attr("cx", circleX).attr("cy", circleY2).attr("r", circleR)
                .style("fill", "#2a2a2a").style("stroke", "darkgreen").style("stroke-width", circleS);
            tempSvg.append("text").attr("x", textX).attr("y", textY).attr("fill", "grey").text("CONNECTED");
        }

    });