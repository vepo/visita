document.addEventListener('DOMContentLoaded', function() {
    // Extrair dados do elemento script
    const dataScript = document.getElementById('visitas-data');
    const visitasData = {
        visitasDiarias: JSON.parse(dataScript.textContent).visitasDiarias.sort((o1, o2) => o1.data.localeCompare(o2.data))
    };
    
    // Processar dados
    const dates = [];
    const views = [];
    const p70DurationValues = [];
    const p90DurationValues = [];
    const avgDurationValues = [];
    
    for (let i = 0; i < visitasData.dailyViews.length; i++) {
        const item = visitasData.dailyViews[i];
        const [year, month, day] = item.date.split('-');
        dates.push(`${day}/${month}/${year}`);
        views.push(item.views);
        
        // Converter tempo de string HH:MM:SS para segundos
        p70DurationValues.push(item.p70Duration_sec);
        p90DurationValues.push(item.p90Duration_sec);
        avgDurationValues.push(item.avgDuration_sec);
    }
    
    // Função para formatar segundos para HH:MM:SS
    function formatSeconds(seconds) {
        if (!seconds && seconds !== 0) return '0s';
        
        const hours = Math.floor(seconds / 3600);
        const minutes = Math.floor((seconds % 3600) / 60);
        const secs = Math.floor(seconds % 60);
        
        if (hours > 0) {
            return `${hours}:${minutes.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
        } else if (minutes > 0) {
            return `${minutes}:${secs.toString().padStart(2, '0')}`;
        } else {
            return `${secs}s`;
        }
    }
    
    // 1. Gráfico de Número de Visitas (barras)
    const ctxVisitas = document.getElementById('daily-views-chart');
    if (ctxVisitas) {
        const dailyViewsChart = new Chart(ctxVisitas, {
            type: 'bar',
            data: {
                labels: dates,
                datasets: [{
                    label: 'Visitas',
                    data: views,
                    backgroundColor: 'rgba(59, 130, 200, 0.7)',
                    borderColor: 'rgba(59, 130, 200, 1)',
                    borderWidth: 1,
                    borderRadius: 4,
                    hoverBackgroundColor: 'rgba(59, 130, 200, 0.9)'
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        display: true,
                        position: 'top',
                    },
                    tooltip: {
                        callbacks: {
                            label: function(context) {
                                return `Visitas: ${context.parsed.y}`;
                            }
                        }
                    }
                },
                scales: {
                    x: {
                        grid: {
                            display: false
                        },
                        ticks: {
                            maxRotation: 45,
                            minRotation: 45
                        }
                    },
                    y: {
                        beginAtZero: true,
                        grid: {
                            color: 'rgba(0, 0, 0, 0.05)'
                        },
                        ticks: {
                            precision: 0
                        },
                        title: {
                            display: true,
                            text: 'Número de Visitas'
                        }
                    }
                }
            }
        });
    }
    
    // 2. Gráfico de Métricas de Tempo (linhas)
    const ctxTempo = document.getElementById('avgDurationChart');
    if (ctxTempo) {
        const avgDurationChart = new Chart(ctxTempo, {
            type: 'line',
            data: {
                labels: dates,
                datasets: [
                    {
                        label: 'Tempo Médio',
                        data: avgDurationValues,
                        borderColor: 'rgba(59, 130, 200, 1)',
                        backgroundColor: 'rgba(59, 130, 200, 0.1)',
                        borderWidth: 2,
                        tension: 0.3,
                        fill: true,
                        pointRadius: 4,
                        pointHoverRadius: 6
                    },
                    {
                        label: 'P70',
                        data: p70DurationValues,
                        borderColor: 'rgba(255, 99, 132, 1)',
                        backgroundColor: 'rgba(255, 99, 132, 0.1)',
                        borderWidth: 2,
                        tension: 0.3,
                        fill: false,
                        borderDash: [5, 5],
                        pointRadius: 4,
                        pointHoverRadius: 6
                    },
                    {
                        label: 'P90',
                        data: p90DurationValues,
                        borderColor: 'rgba(75, 192, 192, 1)',
                        backgroundColor: 'rgba(75, 192, 192, 0.1)',
                        borderWidth: 2,
                        tension: 0.3,
                        fill: false,
                        borderDash: [10, 5],
                        pointRadius: 4,
                        pointHoverRadius: 6
                    }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        display: true,
                        position: 'top',
                    },
                    tooltip: {
                        callbacks: {
                            label: function(context) {
                                const value = context.parsed.y;
                                return `${context.dataset.label}: ${formatSeconds(value)}`;
                            }
                        }
                    }
                },
                scales: {
                    x: {
                        grid: {
                            display: false
                        },
                        ticks: {
                            maxRotation: 45,
                            minRotation: 45
                        }
                    },
                    y: {
                        beginAtZero: true,
                        grid: {
                            color: 'rgba(0, 0, 0, 0.05)'
                        },
                        title: {
                            display: true,
                            text: 'Tempo (segundos)'
                        },
                        ticks: {
                            callback: function(value) {
                                return formatSeconds(value);
                            }
                        }
                    }
                }
            }
        });
    }
});
