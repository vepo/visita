document.addEventListener('DOMContentLoaded', function() {
    // Extrair dados do elemento script
    const dataScript = document.getElementById('visitas-data');
    const visitasData = {
        visitasDiarias: JSON.parse(dataScript.textContent).visitasDiarias.sort((o1, o2) => o1.data > o2.data)
    };
    
    // Processar dados
    const datas = [];
    const visitas = [];
    const avg70Values = [];
    const avg90Values = [];
    const tempoMedioValues = [];
    
    for (let i = 0; i < visitasData.visitasDiarias.length; i++) {
        const item = visitasData.visitasDiarias[i];
        datas.push(item.data);
        visitas.push(item.visitas);
        
        // Converter tempo de string HH:MM:SS para segundos
        avg70Values.push(item.avg70_sec);
        avg90Values.push(item.avg90_sec);
        tempoMedioValues.push(item.tempoMedio_sec);
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
    const ctxVisitas = document.getElementById('visitasDiariasChart');
    if (ctxVisitas) {
        const visitasDiariasChart = new Chart(ctxVisitas, {
            type: 'bar',
            data: {
                labels: datas,
                datasets: [{
                    label: 'Visitas',
                    data: visitas,
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
    const ctxTempo = document.getElementById('tempoMedioChart');
    if (ctxTempo) {
        const tempoMedioChart = new Chart(ctxTempo, {
            type: 'line',
            data: {
                labels: datas,
                datasets: [
                    {
                        label: 'Tempo Médio',
                        data: tempoMedioValues,
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
                        data: avg70Values,
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
                        data: avg90Values,
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