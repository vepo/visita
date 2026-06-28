document.addEventListener('DOMContentLoaded', function () {
    const container = document.getElementById('referrer-sankey');
    const navContainer = document.getElementById('sankey-nav');
    const description = document.getElementById('sankey-description');
    const dataScript = document.getElementById('referrer-flow-data');
    if (!container || !dataScript || typeof d3 === 'undefined' || typeof d3.sankey === 'undefined') {
        return;
    }

    const originFlows = JSON.parse(dataScript.textContent).flows || [];
    const navigationStack = [];

    function dashboardContext() {
        const params = new URLSearchParams(window.location.search);
        const context = {
            startDate: params.get('startDate'),
            endDate: params.get('endDate')
        };
        const path = window.location.pathname;
        const domainMatch = path.match(/\/dashboard\/domain\/([^/?]+)/);
        const referrerMatch = path.match(/\/dashboard\/referrer\/([^/?]+)/);
        if (domainMatch) {
            context.domain = decodeURIComponent(domainMatch[1]);
        }
        if (referrerMatch) {
            context.referrer = decodeURIComponent(referrerMatch[1]);
        }
        return context;
    }

    function buildFlowsUrl(startPage) {
        const params = new URLSearchParams();
        const context = dashboardContext();
        if (context.startDate) {
            params.set('startDate', context.startDate);
        }
        if (context.endDate) {
            params.set('endDate', context.endDate);
        }
        if (context.domain) {
            params.set('domain', context.domain);
        }
        if (context.referrer) {
            params.set('referrer', context.referrer);
        }
        if (startPage) {
            params.set('startPage', startPage);
        }
        return '/dashboard/api/flows?' + params.toString();
    }

    function fetchFlows(startPage) {
        if (!startPage) {
            return Promise.resolve(originFlows);
        }
        return fetch(buildFlowsUrl(startPage))
            .then(function (response) {
                if (!response.ok) {
                    throw new Error('Falha ao carregar fluxos');
                }
                return response.json();
            });
    }

    function truncateLabel(label, maxLength) {
        if (!label || label.length <= maxLength) {
            return label;
        }
        return label.slice(0, maxLength - 1) + '…';
    }

    function renderBreadcrumb() {
        if (!navContainer) {
            return;
        }
        navContainer.innerHTML = '';

        const originButton = document.createElement('button');
        originButton.type = 'button';
        originButton.className = 'text-blue-600 hover:text-blue-800 font-medium';
        originButton.textContent = 'Origem';
        originButton.addEventListener('click', function () {
            navigationStack.length = 0;
            loadAndRender(null);
        });
        navContainer.appendChild(originButton);

        navigationStack.forEach(function (page, index) {
            const separator = document.createElement('span');
            separator.textContent = '›';
            separator.className = 'text-gray-400';
            navContainer.appendChild(separator);

            const button = document.createElement('button');
            button.type = 'button';
            button.className = 'text-blue-600 hover:text-blue-800 font-medium truncate max-w-xs';
            button.textContent = truncateLabel(page, 48);
            button.title = page;
            button.addEventListener('click', function () {
                navigationStack.splice(index + 1);
                loadAndRender(page);
            });
            navContainer.appendChild(button);
        });
    }

    function updateDescription(startPage) {
        if (!description) {
            return;
        }
        if (!startPage) {
            description.textContent = 'Principais caminhos de tráfego do referenciador de entrada até a página visitada. Clique no nome de uma página para explorar os próximos destinos.';
            return;
        }
        description.textContent = 'Visitas que chegaram a partir de ' + startPage + '. Clique no nome de uma página de destino para continuar a navegação.';
    }

    function flowGraphFromData(flows, startPage) {
        const nodes = [];
        const nodeById = new Map();
        const links = [];

        function getNode(id, label) {
            if (!nodeById.has(id)) {
                const node = { id: id, name: label, pageLabel: label };
                nodeById.set(id, node);
                nodes.push(node);
            }
            return nodeById.get(id);
        }

        if (startPage) {
            getNode('page:' + startPage, startPage);
            flows.forEach(function (flow) {
                const targetId = 'page:' + flow.page;
                getNode(targetId, flow.page);
                links.push({
                    source: 'page:' + startPage,
                    target: targetId,
                    value: flow.views
                });
            });
        } else {
            flows.forEach(function (flow) {
                const sourceId = 'ref:' + flow.referrer;
                const targetId = 'page:' + flow.page;
                getNode(sourceId, flow.referrer);
                getNode(targetId, flow.page);
                links.push({
                    source: sourceId,
                    target: targetId,
                    value: flow.views
                });
            });
        }

        return { nodes: nodes, links: links };
    }

    function renderSankey(flows, startPage) {
        container.innerHTML = '';
        renderBreadcrumb();
        updateDescription(startPage);

        if (!flows || flows.length === 0) {
            container.innerHTML = '<p class="text-gray-500 text-sm">Sem dados de fluxo para o período selecionado.</p>';
            return;
        }

        const graphData = flowGraphFromData(flows, startPage);
        const margin = { top: 8, right: 160, bottom: 8, left: 160 };
        const width = Math.max(container.clientWidth - margin.left - margin.right, 480);
        const height = Math.max(320, graphData.nodes.length * 18);

        const svg = d3.select(container)
            .append('svg')
            .attr('viewBox', [0, 0, width + margin.left + margin.right, height + margin.top + margin.bottom])
            .attr('role', 'img')
            .attr('aria-label', startPage
                ? 'Fluxo de visitas a partir de ' + startPage
                : 'Fluxo de visitas da origem para a página');

        const root = svg.append('g')
            .attr('transform', 'translate(' + margin.left + ',' + margin.top + ')');

        const sankey = d3.sankey()
            .nodeId(function (node) { return node.id; })
            .nodeAlign(d3.sankeyJustify)
            .nodeWidth(18)
            .nodePadding(12)
            .extent([[0, 0], [width, height]]);

        const graph = sankey({
            nodes: graphData.nodes,
            links: graphData.links
        });

        const sourceColors = d3.scaleOrdinal(d3.schemeTableau10);
        const sourceIds = Array.from(new Set(graph.links.map(function (link) { return link.source.id; })));
        const colorForSource = function (sourceId) {
            return sourceColors(sourceIds.indexOf(sourceId));
        };

        const tooltip = d3.select(container)
            .append('div')
            .attr('class', 'pointer-events-none absolute hidden rounded bg-gray-900 px-2 py-1 text-xs text-white shadow')
            .style('opacity', 0);

        function showTooltip(event, html) {
            tooltip
                .classed('hidden', false)
                .style('opacity', 1)
                .html(html)
                .style('left', (event.offsetX + 12) + 'px')
                .style('top', (event.offsetY + 12) + 'px');
        }

        function hideTooltip() {
            tooltip.classed('hidden', true).style('opacity', 0);
        }

        function drillDown(pageLabel) {
            navigationStack.push(pageLabel);
            loadAndRender(pageLabel);
        }

        function isDrillableNode(node) {
            if (!node.id.startsWith('page:')) {
                return false;
            }
            if (startPage && node.name === startPage) {
                return false;
            }
            return true;
        }

        root.append('g')
            .attr('fill', 'none')
            .selectAll('path')
            .data(graph.links)
            .join('path')
            .attr('d', d3.sankeyLinkHorizontal())
            .attr('stroke', function (link) { return colorForSource(link.source.id); })
            .attr('stroke-opacity', 0.35)
            .attr('stroke-width', function (link) { return Math.max(1, link.width); })
            .on('mousemove', function (event, link) {
                showTooltip(event, link.source.name + ' → ' + link.target.name + '<br><strong>' + link.value + ' visitas</strong>');
            })
            .on('mouseleave', hideTooltip);

        root.append('g')
            .selectAll('rect')
            .data(graph.nodes)
            .join('rect')
            .attr('x', function (node) { return node.x0; })
            .attr('y', function (node) { return node.y0; })
            .attr('height', function (node) { return Math.max(1, node.y1 - node.y0); })
            .attr('width', function (node) { return node.x1 - node.x0; })
            .attr('fill', function (node) {
                if (startPage && node.name === startPage) {
                    return '#2563eb';
                }
                return node.id.startsWith('ref:') ? colorForSource(node.id) : '#64748b';
            })
            .attr('opacity', 0.9)
            .attr('pointer-events', 'none');

        root.append('g')
            .attr('class', 'sankey-labels')
            .selectAll('text')
            .data(graph.nodes)
            .join('text')
            .attr('x', function (node) { return node.x0 < width / 2 ? node.x0 - 8 : node.x1 + 8; })
            .attr('y', function (node) { return (node.y0 + node.y1) / 2; })
            .attr('dy', '0.35em')
            .attr('text-anchor', function (node) { return node.x0 < width / 2 ? 'end' : 'start'; })
            .attr('class', function (node) { return isDrillableNode(node) ? 'sankey-label-drillable' : null; })
            .style('font', '12px sans-serif')
            .style('fill', function (node) {
                if (isDrillableNode(node)) {
                    return null;
                }
                if (startPage && node.name === startPage) {
                    return '#1e40af';
                }
                return '#334155';
            })
            .style('font-weight', function (node) {
                return startPage && node.name === startPage ? '600' : null;
            })
            .text(function (node) { return truncateLabel(node.name, 42); })
            .on('mousemove', function (event, node) {
                const kind = node.id.startsWith('ref:') ? 'Origem' : 'Página';
                const hint = isDrillableNode(node) ? '<br><em>Clique para explorar</em>' : '';
                showTooltip(event, kind + ': ' + node.name + '<br><strong>' + node.value + ' visitas</strong>' + hint);
            })
            .on('mouseleave', hideTooltip)
            .on('click', function (_event, node) {
                if (isDrillableNode(node)) {
                    drillDown(node.name);
                }
            })
            .append('title')
            .text(function (node) { return node.name; });
    }

    function loadAndRender(startPage) {
        container.innerHTML = '<p class="text-gray-500 text-sm">Carregando fluxos…</p>';
        fetchFlows(startPage)
            .then(function (flows) {
                renderSankey(flows, startPage);
            })
            .catch(function () {
                container.innerHTML = '<p class="text-red-600 text-sm">Não foi possível carregar os fluxos.</p>';
            });
    }

    loadAndRender(null);
});
