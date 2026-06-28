document.addEventListener('DOMContentLoaded', function () {
    const container = document.getElementById('referrer-sankey');
    const dataScript = document.getElementById('referrer-flow-data');
    if (!container || !dataScript || typeof d3 === 'undefined' || typeof d3.sankey === 'undefined') {
        return;
    }

    const flows = JSON.parse(dataScript.textContent).flows || [];
    if (flows.length === 0) {
        container.innerHTML = '<p class="text-gray-500 text-sm">Sem dados de fluxo para o período selecionado.</p>';
        return;
    }

    const nodes = [];
    const nodeById = new Map();
    const links = [];

    function getNode(id, label) {
        if (!nodeById.has(id)) {
            const node = { id: id, name: label };
            nodeById.set(id, node);
            nodes.push(node);
        }
        return nodeById.get(id);
    }

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

    const margin = { top: 8, right: 160, bottom: 8, left: 160 };
    const width = Math.max(container.clientWidth - margin.left - margin.right, 480);
    const height = Math.max(320, nodes.length * 18);

    container.innerHTML = '';

    const svg = d3.select(container)
        .append('svg')
        .attr('viewBox', [0, 0, width + margin.left + margin.right, height + margin.top + margin.bottom])
        .attr('role', 'img')
        .attr('aria-label', 'Fluxo de visitas da origem para a página');

    const root = svg.append('g')
        .attr('transform', 'translate(' + margin.left + ',' + margin.top + ')');

    const sankey = d3.sankey()
        .nodeId(function (node) { return node.id; })
        .nodeAlign(d3.sankeyJustify)
        .nodeWidth(18)
        .nodePadding(12)
        .extent([[0, 0], [width, height]]);

    const graph = sankey({
        nodes: nodes,
        links: links
    });

    const referrerColors = d3.scaleOrdinal(d3.schemeTableau10);
    const referrerIds = Array.from(new Set(graph.links.map(function (link) { return link.source.id; })));
    const colorForReferrer = function (referrerId) {
        return referrerColors(referrerIds.indexOf(referrerId));
    };

    function truncateLabel(label, maxLength) {
        if (!label || label.length <= maxLength) {
            return label;
        }
        return label.slice(0, maxLength - 1) + '…';
    }

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

    root.append('g')
        .attr('fill', 'none')
        .selectAll('path')
        .data(graph.links)
        .join('path')
        .attr('d', d3.sankeyLinkHorizontal())
        .attr('stroke', function (link) { return colorForReferrer(link.source.id); })
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
            return node.id.startsWith('ref:') ? colorForReferrer(node.id) : '#64748b';
        })
        .attr('opacity', 0.9)
        .on('mousemove', function (event, node) {
            const kind = node.id.startsWith('ref:') ? 'Origem' : 'Página';
            showTooltip(event, kind + ': ' + node.name + '<br><strong>' + node.value + ' visitas</strong>');
        })
        .on('mouseleave', hideTooltip);

    root.append('g')
        .style('font', '12px sans-serif')
        .style('fill', '#334155')
        .selectAll('text')
        .data(graph.nodes)
        .join('text')
        .attr('x', function (node) { return node.x0 < width / 2 ? node.x0 - 8 : node.x1 + 8; })
        .attr('y', function (node) { return (node.y0 + node.y1) / 2; })
        .attr('dy', '0.35em')
        .attr('text-anchor', function (node) { return node.x0 < width / 2 ? 'end' : 'start'; })
        .text(function (node) { return truncateLabel(node.name, 42); })
        .append('title')
        .text(function (node) { return node.name; });
});
