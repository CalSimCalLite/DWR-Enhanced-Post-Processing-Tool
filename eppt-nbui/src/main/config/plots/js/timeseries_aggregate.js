/*
 * Enhanced Post Processing Tool (EPPT) Copyright (c) 2020.
 *
 * EPPT is copyrighted by the State of California, Department of Water Resources. It is licensed
 * under the GNU General Public License, version 2. This means it can be
 * copied, distributed, and modified freely, but you may not restrict others
 * in their ability to copy, distribute, and modify it. See the license below
 * for more details.
 *
 * GNU General Public License
 */

function getPlotlyAggregateSeries(datum, firstRecord, lastRecord) {
    let seriesList = [];
    let startDate = new Date(firstRecord);
    let endDate = new Date(lastRecord);
    for (let i = 0; i < datum.length; i++) {
        let tsList = datum[i]['ts_list'];
        for (let j = 0; j < tsList.length; j++) {
            let axis = 0;
            let monthlyFilters = tsList[j]['monthly_filters'];
            for (let k = 0; k < monthlyFilters.length; k++) {
                let annualFilters = monthlyFilters[k]['annual_filters'];
                for (let m = 0; m < annualFilters.length; m++) {
                    let timeSeries = annualFilters[m]['aggregate_ts'];
                    let x = [];
                    let y = [];
                    let hoverInfo = [];
                    let markerSize = [];
                    var startingDataIndex = 0;
                    for (let year = startDate.getFullYear(); year <= endDate.getFullYear(); year++) {
                        x.push(year);
                        if (timeSeries[startingDataIndex]) {
                            if (timeSeries[startingDataIndex][0] === year) {
                                y.push(timeSeries[startingDataIndex][1]);
                                hoverInfo.push('all');
                                markerSize.push(4);
                                startingDataIndex++;
                            }
                        }
                        if (!y[y.length - 2] && y[y.length - 1]) {
                            y[y.length - 2] = y[y.length - 1];
                        }
                        if (x.length > y.length) {
                            y.push(null);
                            hoverInfo.push('skip');
                            markerSize.push(0);
                        }
                    }
                    let series = seriesList[axis];
                    if (!series) {
                        series = [];
                        seriesList.push(series);
                    }
                    series.push({
                        name: tsList[j]['ts_name'],
                        x: x,
                        y: y,
                        line: {
                            color: datum[i]['scenario_color'],
                            dash: PLOTLY_LINE_DASH_STYLES[j % PLOTLY_LINE_DASH_STYLES.length],
                            shape: 'vh'
                        },
                        mode: 'lines+markers',
                        marker: {
                            size: markerSize,
                            color: darken(datum[i]['scenario_color'], 20)
                        },
                        hoverinfo: hoverInfo
                    });
                    axis++;
                }
            }
        }
    }
    return seriesList;
}

function buildLayouts(datum, yaxis, title) {
    let layoutList = [];
    for (let i = 0; i < datum.length; i++) {
        let tsList = datum[i]['ts_list'];
        for (let j = 0; j < tsList.length; j++) {
            let axis = 0;
            let monthlyFilters = tsList[j]['monthly_filters'];
            for (let k = 0; k < monthlyFilters.length; k++) {
                let annualFilters = monthlyFilters[k]['annual_filters'];
                for (let m = 0; m < annualFilters.length; m++) {
                    let series = layoutList[axis];
                    if (!series) {
                        let plotTitle = title;
                        if (annualFilters[m]['annual_period']) {
                            if (annualFilters[m]['annual_period'].indexOf('<br>') === annualFilters[m]['annual_period'].length - 4) {
                                plotTitle += '<br>' + annualFilters[m]['annual_period'].replace("<br>", "");
                            } else {
                                plotTitle += '<br>' + annualFilters[m]['annual_period'].replace("<br>", " - ");
                            }
                        } else if (annualFilters[m]['month_period']) {
                            plotTitle += '<br>' + annualFilters[m]['month_period'];
                        }
                        layoutList[axis] = {
                            font: PLOTLY_FONT,
                            yaxis: {
                                title: {
                                    text: yaxis,
                                },
                                tickformat: FORMATTER,
                                gridcolor: '#CCCCCC',
                                rangemode: 'tozero'
                            },
                            xaxis: {
                                gridcolor: '#CCCCCC',
                                tickformat: '.0f'
                            },
                            showlegend: true,
                            legend: {
                                orientation: 'h',
                                xanchor: 'center',
                                x: 0.5,
                                // y:1.01,
                                font: {
                                    size: 10,
                                }
                            },
                            title: {
                                text: plotTitle,
                                font: {
                                    size: 20,
                                }
                            },
                            margin: {
                                l: 60,
                                r: 40,
                                b: 90,
                                t: 120
                            }
                        };
                    }
                    axis++;
                }
            }
        }
    }
    return layoutList;
}

function plot(data) {
    FORMATTER = getD3Formatter(data['scenario_run_data'][0]['ts_list'][0]['monthly_filters'][0]['annual_filters'][0]['discrete_ts']);
    var datum = data['scenario_run_data'];
    var layout = buildLayouts(datum, data['units'], data['gui_link_title']);
    let plotlyAggregateSeries = getPlotlyAggregateSeries(datum, data['first_record'], data['last_record']);
    plotData(layout, plotlyAggregateSeries);
}

function plotlyCopyToClipboard(element) {
    let plot = $(element)[0];
    let layout = plot.layout;
    let data1 = plot.data;
    var text = layout['title']['text'] + '\n' + 'Date\t' + layout['yaxis']['title']['text'] + '\n';
    for (var i = 0; i < data1.length; i++) {
        text += '\t' + data1[i]['name']
    }
    text += '\n';
    let datum = data1[0];
    let xarr = datum['x'];
    for (var j = 0; j < xarr.length; j++) {
        text += xarr[j];
        for (var k = 0; k < data1.length; k++) {
            let yarr = data1[k]['y'];
            text += '\t' + yarr[j];
        }
        text += '\n';
    }
    copyTextToClipboard(text);
}