/**
 * Drawing dashboard graphs
 */
angular
    .module('watchdogNocDashboard', [])
    .controller('nocDashboard', ['$scope', '$http', '$rootScope', function ($scope, $http, $rootScope) {
        var self = this;
        var manageView;
        $scope.clientId = "";
        $scope.results = [];
        var treeData;
        var blockData;
        var cpuHistory;

        var tpsHeight;
        var tpsNeedleAngle = 0;
        var sysMetrics;

        drawLostMessages();
        tpsHeight = drawTps();

        $rootScope.$watch('nodeTree', function () {
            treeData = $rootScope.nodeTree;
            if (treeData != null) {
                drawTree();
            }
        }, true);

        $rootScope.$watch('nodeBlock', function () {
            blockData = $rootScope.nodeBlock;
            if (blockData != null) {
                drawBlocks();
            }
        }, true);

        $rootScope.$watch('cpuHistory', function () {
            cpuHistory = $rootScope.cpuHistory;
            if (cpuHistory != null) {
                drawSystemLoadAverage(tpsHeight);
            }
        }, true);

        $rootScope.$watch('sysMetrics', function () {
            sysMetrics = $rootScope.sysMetrics;
            if (sysMetrics != null) {
                drawTps();
            }
        }, true);


        function drawBlocks() {
            var dataGateway = blockData.block.GATEWAY;
            var dataOms = blockData.block.OMS;
            var dataDfix = blockData.block.DFIX;
            var dataAura = blockData.block.AURA;
            var dataTxnQ = blockData.block.TRANSAC;
            var nodeLinks = blockData.links;
            var linkClasses = [];
            var blockStateToClasses = {
                "CONNECTED": "block-connected", "CONNECTING": "block-connecting",
                "INITIALIZING": "block-initializing", "SUSPENDED": "block-suspended", "CLOSED": "block-closed"
            };

            function getClassFromState(state) {
                return blockStateToClasses[state];
            }

            var svg = d3.select("#dashboard-blocks svg");
            var margin = {top: 0, right: 0, bottom: 0, left: 0};
            var width = parseInt(svg.style("width")) - margin.right - margin.left;
            var height = width * 0.7;
            var zoomFactor = width * 0.0035;

            d3.select("#dashboard-blocks").select("svg").remove();
            svg = d3.select("#dashboard-blocks").append("svg")
                .attr("width", width + margin.right + margin.left)
                .attr("height", height + margin.top + margin.bottom)
                .append("g")
                .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

            var rh = height * 0.09,
                rw = width * 0.22;

            var shGateway = (height - dataGateway.length * rh) / (dataGateway.length + 1);
            var shOms = (height - dataAura.length * rh - dataOms.length * rh) / ((dataOms.length + dataAura.length) + 1);
            var shDfix = (height - dataDfix.length * rh) / (dataDfix.length + 1);

            var links = [];
            var linkStateToClasses = {"CONNECTED": "link-connected", "CLOSED": "link-closed"};

            function calculateLinks(source, target, shSource, shTarget) {
                for (i = 0; i < source.length; i++) {
                    for (j = 0; j < target.length; j++) {
                        sourceX = (((width - 3 * rw) / 6) + (width / 6) * (source[i].x - 1)) + rw;
                        sourceY = source[i].y * shSource + (source[i].y - 1) * rh + rh / 2;
                        var targetX, targetY;
                        if (shTarget != null) {
                            targetX = (((width - 3 * rw) / 6) + (width / 6) * (target[j].x - 1));
                            targetY = target[j].y * shTarget + (target[j].y - 1 ) * rh + rh / 2;
                        } else {
                            targetX = (((width - 3 * rw) / 6) + (width / 6) * (target[j].x - 1));
                            targetY = (height - 1.5 * rh);
                        }
                        links.push([[sourceX, sourceY], [targetX, targetY]]);
                        linkClasses.push(linkStateToClasses[nodeLinks[source[i].text][target[j].text]]);
                    }
                }
            }

            calculateLinks(dataGateway, dataOms, shGateway, shOms);
            calculateLinks(dataOms, dataDfix, shOms, shDfix);
            calculateLinks(dataGateway, dataAura, shGateway, null);
            calculateLinks(dataGateway, dataTxnQ, shGateway, null);

            //draw connecting lines
            var lineGenerator = d3.line();
            var diagonal = function (node) {
                source = node[0];
                target = node[1];
                return "M" + target[0] + "," + target[1]
                    + "C" + (target[0] + source[0]) / 2 + "," + target[1]
                    + " " + (target[0] + source[0]) / 2 + "," + source[1]
                    + " " + source[0] + "," + source[1];
            };
            for (i = 0; i < links.length; i++) {
                var link = links[i];
                svg.append('path')
                    .attr('d', lineGenerator(link))
                    // .attr('d', diagonal(link))
                    .attr('fill', 'none')
                    .classed(linkClasses[i], true);
            }

            //draw components
            var gGateway = svg.selectAll('.gatewayModule')
                .data(dataGateway)
                .enter()
                .append("g")
                .attr("class", "gatewayModule")
                .attr("transform", function (d) {
                    return "translate(" +
                        (((width - 3 * rw) / 6) + (width / 6) * (d.x - 1)) + "," +
                        (d.y * shGateway + (d.y - 1) * rh) + ")";
                });
            gGateway.append("rect")
                .attr("rx", 2 * zoomFactor)
                .attr("ry", 2 * zoomFactor)
                .attr("width", rw)
                .attr("height", rh)
                .attr("class", function (d) {
                    return blockStateToClasses[d.state];
                });
            gGateway.append("text")
                .style("fill", "black")
                .style("font-size", (7.5 * zoomFactor) + "px")
                .style("text-anchor", "middle")
                .attr("x", rw / 2)
                .attr("y", rh / 1.5)
                .text(function (d) {
                    return d.text;
                });

            var gOms = svg.selectAll('.omsModule')
                .data(dataOms)
                .enter()
                .append("g")
                .attr("class", "omsModule")
                .attr("transform", function (d) {
                    return "translate(" +
                        (((width - 3 * rw) / 6) + (width / 6) * (d.x - 1)) + "," +
                        (d.y * shOms + (d.y - 1 ) * rh) + ")";
                });
            gOms.append("rect")
                .attr("rx", 2 * zoomFactor)
                .attr("ry", 2 * zoomFactor)
                .attr("width", rw)
                .attr("height", rh)
                .attr("class", function (d) {
                    return blockStateToClasses[d.state];
                });
            gOms.append("text")
                .style("fill", "black")
                .style("font-size", (7.5 * zoomFactor) + "px")
                .style("text-anchor", "middle")
                .attr("x", rw / 2)
                .attr("y", rh / 1.5)
                .text(function (d) {
                    return d.text;
                });

            var gDfix = svg.selectAll('.dfixModule')
                .data(dataDfix)
                .enter()
                .append("g")
                .attr("class", "dfixModule")
                .attr("transform", function (d) {
                    return "translate(" +
                        (((width - 3 * rw) / 6) + (width / 6) * (d.x - 1)) + "," +
                        (d.y * shDfix + (d.y - 1) * rh) + ")";
                });
            gDfix.append("rect")
                .attr("rx", 2 * zoomFactor)
                .attr("ry", 2 * zoomFactor)
                .attr("width", rw)
                .attr("height", rh)
                .attr("class", function (d) {
                    return blockStateToClasses[d.state];
                });
            gDfix.append("text")
                .style("fill", "black")
                .style("font-size", (7.5 * zoomFactor) + "px")
                .style("text-anchor", "middle")
                .attr("x", rw / 2)
                .attr("y", rh / 1.5)
                .text(function (d) {
                    return d.text;
                });

            var gTxnQ = svg.selectAll('.txnqModule')
                .data(dataTxnQ)
                .enter()
                .append("g")
                .attr("class", "txnqModule")
                .attr("transform", function (d) {
                    return "translate(" +
                        (((width - 3 * rw) / 6) + (width / 6) * (d.x - 1)) + "," +
                        (height - 2 * rh) + ")";
                });
            gTxnQ.append("rect")
                .attr("rx", 2 * zoomFactor)
                .attr("ry", 2 * zoomFactor)
                .attr("width", rw)
                .attr("height", rh)
                .attr("class", function (d) {
                    return blockStateToClasses[d.state];
                });
            gTxnQ.append("text")
                .style("fill", "black")
                .style("font-size", (7.5 * zoomFactor) + "px")
                .style("text-anchor", "middle")
                .attr("x", rw / 2)
                .attr("y", rh / 1.5)
                .text(function (d) {
                    return d.text;
                });

            var gAura = svg.selectAll('.auraModule')
                .data(dataAura)
                .enter()
                .append("g")
                .attr("class", "auraModule")
                .attr("transform", function (d) {
                    return "translate(" +
                        (((width - 3 * rw) / 6) + (width / 6) * (d.x - 1)) + "," +
                        (height - 2 * rh) + ")";
                });
            gAura.append("rect")
                .attr("rx", 2 * zoomFactor)
                .attr("ry", 2 * zoomFactor)
                .attr("width", rw)
                .attr("height", rh)
                .attr("class", function (d) {
                    return blockStateToClasses[d.state];
                });
            gAura.append("text")
                .style("fill", "black")
                .style("font-size", (7.5 * zoomFactor) + "px")
                .style("text-anchor", "middle")
                .attr("x", rw / 2)
                .attr("y", rh / 1.5)
                .text(function (d) {
                    return d.text;
                });
        }

        function drawTree() {
            var svg = d3.select("#dashboard-view svg");
            var margin = {top: 40, right: 30, bottom: 30, left: 30};
            var width = parseInt(svg.style("width")) - margin.right - margin.left;
            var height = width * 0.8 - margin.top - margin.bottom;
            var zoomFactor = width * 0.0035;

            var treemap = d3.tree()
                .size([width, height]);

            var nodes = d3.hierarchy(treeData, function (d) {
                return d.children;
            });

            nodes = treemap(nodes);

            d3.select("#dashboard-view").select("svg").remove();
            svg = d3.select("#dashboard-view").append("svg")
                .attr("width", width + margin.left + margin.right)
                .attr("height", height + margin.top + margin.bottom),
                g = svg.append("g")
                    .attr("transform",
                        "translate(" + margin.left + "," + margin.top + ")");

            // adds the links between the nodes
            var link = g.selectAll(".link")
                .data(nodes.descendants().slice(1))
                .enter().append("path")
                .attr("class", "link")
                .style("stroke", function (d) {
                    return d.data.level;
                })
                .attr("d", function (d) {
                    return "M" + d.x + "," + d.y
                        + "C" + d.x + "," + (d.y + d.parent.y) / 2
                        + " " + d.parent.x + "," + (d.y + d.parent.y) / 2
                        + " " + d.parent.x + "," + d.parent.y;
                });

            // adds each node as a group
            var node = g.selectAll(".node")
                .data(nodes.descendants())
                .enter().append("g")
                .attr("class", function (d) {
                    return "node" +
                        (d.children ? " node--internal" : " node--leaf");
                })
                .attr("transform", function (d) {
                    return "translate(" + d.x + "," + d.y + ")";
                });

            // adds symbols as nodes
            node.append("circle")
                .attr("r", function (d) {
                    return d.data.value * zoomFactor;
                })
                .style("stroke-width", function (d) {
                    return d.data.outer;
                })
                .style("stroke", function (d) {
                    return d.data.type;
                })
                .style("fill", function (d) {
                    return d.data.level;
                });

            // adds the text to the node
            node.append("text")
            // .attr("dy", ".35em")
                .attr("y", function (d) {
                    return d.children ?
                        (d.data.value + 7 * zoomFactor) * -1 : d.data.value / 2
                })
                .attr("x", function (d) {
                    return 0;
                })
                .style("text-anchor", function (d) {
                    return d.children ? "middle" : "middle";
                })
                .style("font-size", function (d) {
                    return d.children ? (8 * zoomFactor) + "px" : (12 * zoomFactor) + "px";
                })
                .text(function (d) {
                    return d.data.name;
                });
        }

        function drawSystemLoadAverage(tpsHeight) {
            var svg = d3.select("#dashboard-load svg");
            var margin = {top: 10, right: 20, bottom: 20, left: 40};
            var width = parseInt(svg.style("width")) - margin.right - margin.left;
            var height = tpsHeight - margin.top - margin.bottom;

            // set the ranges
            var x = d3.scaleLinear().range([0, width]);
            var y = d3.scaleLinear().range([height, 0]);
            var z = d3.scaleOrdinal(d3.schemeCategory10);

            var valueline = d3.line()
                .curve(d3.curveBasis)
                .x(function (d) {
                    return x(d.tick);
                })
                .y(function (d) {
                    return y(d.cpu);
                });

            d3.select("#dashboard-load").select("svg").remove();
            svg = d3.select("#dashboard-load").append("svg")
                .attr("width", width + margin.left + margin.right)
                .attr("height", height + margin.top + margin.bottom)
                .append("g")
                .attr("transform",
                    "translate(" + margin.left + "," + margin.top + ")");

            var data = [];
            var maxX = 1;
            for (var node in cpuHistory) {
                if (cpuHistory.hasOwnProperty(node)) {
                    var tempNode = {};
                    var nodeData = [];
                    cpuHistory[node].forEach(function (d, i) {
                        maxX = node.length > maxX ? cpuHistory[node].length : maxX;
                        var dataPoint = {};
                        dataPoint.tick = i;
                        dataPoint.cpu = d;
                        nodeData.push(dataPoint);
                    });
                    tempNode.id = node;
                    tempNode.values = nodeData;
                    data.push(tempNode);
                }
            }

            x.domain([0, maxX - 1]);
            y.domain([0, 100]);
            z.domain(data.map(function (c) {
                return c.id;
            }));

            var lineGroup = svg.selectAll(".lineGroup")
                .data(data)
                .enter().append("g")
                .attr("class", "lineGroup");

            lineGroup.append("path")
                .attr("class", "line")
                .attr("d", function (d) {
                    return valueline(d.values);
                })
                .attr("fill", "none")
                // .attr("stroke", "steelblue")
                .attr("stroke-width", "1.5px")
                .style("stroke", function (d) {
                    return z(d.id);
                });

            lineGroup.append("text")
                .datum(function (d) {
                    return {id: d.id, value: d.values[d.values.length - 1]};
                })
                .attr("transform", function (d) {
                    return "translate(" + x(d.value.tick) + "," + y(d.value.cpu) + ")";
                })
                .attr("x", -20)
                .attr("dy", "0.35em")
                .style("font", "8px sans-serif")
                .text(function (d) {
                    return d.id;
                });

            svg.append("g")
                .attr("transform", "translate(0," + height + ")")
                .call(d3.axisBottom(x));

            svg.append("g")
                .call(d3.axisLeft(y))
            /*.append("text")
             .attr("transform", "rotate(-90)")
             .attr("y", 6)
             .attr("dy", "0.71em")
             .attr("fill", "#000")
             .text("cpu %")*/;
        }

        function drawTps() {
            var svg = d3.select("#dashboard-tps svg");
            var margin = {top: 10, right: 10, bottom: 10, left: 10};
            var width = parseInt(svg.style("width")) - margin.right - margin.left;
            var height = width * 0.5 - margin.top - margin.bottom;

            svg.attr("height", height + margin.top + margin.bottom);

            d3.select("#dashboard-tps svg").select("svg").remove();
            svg = d3.select("#dashboard-tps svg").append("svg");

            var arc = d3.arc()
                .innerRadius(height * 0.55)
                .outerRadius(height)
                .cornerRadius(0)
                .padAngle(0);

            var color = ["#62ff00", "#69fc00", "#79f800",
                "#91f200", "#a7ec00", "#c0e500",
                "#d9db00", "#eecf00", "#fcc300",
                "#ffb300", "#ff9800", "#ff8100",
                "#ff6500", "#ff4c00", "#ff3300",
                "#ff2000", "#ff1400"];

            var tpsLevels = [];
            for (var x in color) {
                tpsLevels.push(1);
            }

            var pie = d3.pie()
                .startAngle(-Math.PI / 2)
                .endAngle(Math.PI / 2)
                .sort(null)
                .value(function (d) {
                    return d
                });

            var arcs = svg.selectAll('.arc')
                .data(pie(tpsLevels))
                .enter()
                .append('path')
                .attr("d", arc)
                .attr("transform",
                    "translate(" + (margin.left + margin.right + width) / 2 + ", " + (height + margin.bottom) + ")")
                .style("fill", function (d, i) {
                    return color[i]
                });

            var dataOmsLength = blockData != null ? blockData.block.OMS.length : 2;
            var tpsValue = 0;
            if (sysMetrics != null) {
                tpsValue = sysMetrics["tps"];
            }
            var needle = svg.selectAll(".needle")
                .data([1])
                .enter()
                /*.append('line')
                .attr("x1", (40 + width) / 2)
                .attr("x2", ((40 + width) / 2) + (height * -0.80))
                .attr("y1", height + margin.bottom)
                .attr("y2", height + margin.bottom)
                .style("stroke", "black")
                .style("stroke-width", 2)*/
                .append("svg:image")
                .attr('x', 40)
                .attr('y', height + 5 - (width * 0.25))
                .attr('width', width * 0.5)
                .attr('height', width * 0.5)
                .attr("xlink:href", "/img/needle.png")
                .transition()
                .duration(10000)
                .attrTween('transform', function () {
                    r = 180 * tpsValue / (dataOmsLength * 3000);
                    r = (r > 170) ? 170 : r;//180 doesn't look good
                    return tweenNeedle(r);
                });

            function tweenNeedle(currentAngle) {
                var prevAngle = tpsNeedleAngle;
                tpsNeedleAngle = currentAngle;
                return d3.interpolateString(
                    "rotate(" + prevAngle + "," + ((20 + width) / 2) + "," + (height + margin.bottom) + ")",
                    "rotate(" + tpsNeedleAngle + "," + ((20 + width) / 2) + "," + (height + margin.bottom) + ")");
            }
            svg.append("text")
                .style("fill", "#6c6777")
                .style("font-size", "14px")
                .style("text-anchor", "middle")
                .attr("x", (margin.left + margin.right + width) / 2)
                .attr("y", height + margin.bottom)
                .text("--" + tpsValue + "--");

            return height + margin.top + margin.bottom;
        }

        function drawLostMessages() {
            var barData = [
                {"letter": "OMS-1", "frequency": ".08167"},
                {"letter": "OMS-2", "frequency": ".01492"},
                {"letter": "OMS-3", "frequency": ".09167"},
                {"letter": "GATEWAY-1", "frequency": ".04167"},
                {"letter": "GATEWAY-1", "frequency": ".06167"},
                {"letter": "GATEWAY-2", "frequency": ".02167"},
                {"letter": "DFIX-1", "frequency": ".04167"},
                {"letter": "DFIX-2", "frequency": ".03167"}
            ];

            var svg = d3.select("#dashboard-lost-messages svg");
            var margin = {top: 20, right: 20, bottom: 30, left: 40};
            var width = parseInt(svg.style("width")) - margin.right - margin.left;
            var height = 230 - margin.top - margin.bottom;

            svg.attr("height", height + margin.top + margin.bottom);

            /*var svg = d3.select("#dashboard-lost-messages").append("svg")
             .attr("width", width + margin.right + margin.left)
             .attr("height", height + margin.top + margin.bottom);*/

            var x = d3.scaleBand().rangeRound([0, width]).padding(0.1),
                y = d3.scaleLinear().rangeRound([height, 0]);

            var g = svg.append("g")
                .attr("transform", "translate(" + margin.left + "," + margin.top + ")");


            data = barData;

            x.domain(data.map(function (d) {
                return d.letter;
            }));
            y.domain([0, d3.max(data, function (d) {
                return d.frequency;
            })]);

            g.append("g")
                .attr("class", "axis axis--x")
                .attr("transform", "translate(0," + height + ")")
                .call(d3.axisBottom(x))
                .selectAll("text")
                .style("text-anchor", "end")
                .attr("dx", "-.8em")
                .attr("dy", ".15em")
                .attr("transform", "rotate(-35)");
            g.append("g")
                .attr("class", "axis axis--y")
                .call(d3.axisLeft(y).ticks(10, "%"))
                .append("text")
                .attr("transform", "rotate(-90)")
                .attr("y", 6)
                .attr("dy", "0.71em")
                .attr("text-anchor", "end")
                .text("Frequency");

            g.selectAll(".bar")
                .data(data)
                .enter().append("rect")
                .attr("class", "bar")
                .attr("x", function (d) {
                    return x(d.letter);
                })
                .attr("y", function (d) {
                    return y(d.frequency);
                })
                .attr("width", x.bandwidth())
                .attr("height", function (d) {
                    return height - y(d.frequency);
                });
        }

    }]);