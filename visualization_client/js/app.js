function displayLiveCountOfTweets() {

	return Highcharts.chart('countOfTweet', {
		chart: {
			type: 'spline',
			animation: Highcharts.svg, // don't animate in old IE
			marginRight: 10,
			events: {
				load: function () {
					console.log('loaded');
					//set up the updating of the chart each second
					// var series = this.series[0];
					// setInterval(function () {
					// 	var x = (new Date()).getTime() - 10000, // current time
					// 		y = 0;
					// 	series.addPoint([x, y], true, true);
					// }, 1000);
				}
			}
		},

		time: {
			useUTC: false
		},

		title: {
			text: 'Live Count of Tweets Per Second'
		},
		xAxis: {
			type: 'datetime',
			tickPixelInterval: 150
		},
		yAxis: {
			title: {
				text: 'Tweet Counts'
			},
			plotLines: [{
				value: 0,
				width: 1,
				color: '#808080'
			}]
		},
		tooltip: {
			headerFormat: '<b>{series.name}</b><br/>',
			pointFormat: '{point.x:%Y-%m-%d %H:%M:%S}<br/>{point.y:.2f}'
		},
		legend: {
			enabled: false
		},
		exporting: {
			enabled: false
		},
		series: [{
			name: 'Count of tweets',
			data: (function () {
				// generate an array of random data
				var data = [],
					time = (new Date()).getTime(),
					i;
	
				for (i = -29; i <= -10; i += 1) {
					data.push({
						x: time + i * 1000,
						y: 0
					});
				}
				return data;
			}())
		}]
	});
}

function displayTopWord(series, words) {
	return Highcharts.chart('topWords', {
		chart: {
			type: 'packedbubble',
			height: '100%'
		},
		title: {
			text: 'Number of mentions among words in the last minutes'
		},
		tooltip: {
			useHTML: true,
			pointFormat: '<b>{point.name}</b> in {point.y} tweets'
		},
		plotOptions: {
			packedbubble: {
				dataLabels: {
					enabled: true,
					format: '{point.name}',
					filter: {
						property: 'y',
						operator: '>',
						value: 250
					},
					style: {
						color: 'black',
						textOutline: 'none',
						fontWeight: 'normal'
					}
				},
				minPointSize: 5
			}
		},
		series: series,
		responsive: {
			rules: [{
				condition: {
					maxWidth: 500
				},
				chartOptions: {
					legend: {
						align: 'right',
						verticalAlign: 'middle',
						layout: 'vertical'
					}
				}
			}]
		}
	});
	
}

function averageTweetCount() {
	var options = {
		  useEasing: true, 
		  useGrouping: true, 
		  separator: ',', 
		  decimal: '.', 
	};
	var demo = new CountUp('averageTweetPerSecond', 0, 0, 0, 2.5, options);
	if (!demo.error) {
	  demo.start();
	} else {
	  console.error(demo.error);
	}

	return demo;
}

function updateListWord(mentions) {
	if(mentions && mentions.length > 0) {
		let lis = [];
		mentions.forEach(function(m, index) {
			lis.push('<li>' +m.word + ': '+ m.count + ' tweets</li>');
		});

		$('#topWordsDetail').html(lis);
	}
}

$('document').ready(function (e) {
	var pubnub = new PubNub({
		publishKey: 'your key goes here',
		subscribeKey: 'your key goes here'
	});

	pubnub.subscribe({
		channels: ['my_channel'],
	});

	pubnub.addListener({
		status: function (statusEvent) {
			if (statusEvent.category === "PNConnectedCategory") {
				console.log('connected');
			}
		},
		message: function (msg) {
			console.log(msg.message);
			if(msg.message == undefined || msg.message == null) {
				return;
			}

			let x = (new Date()).getTime() - 10000; // current time
			let y = msg.message.count;
			chart.series[0].addPoint([x, y], true, true);

			//Update average number
			if(msg.message.avg != null && msg.message.avg != undefined) {
				averageCount.update(parseFloat(msg.message.avg));
			}

			//Update word count
			let words = ['Trump', 'Bitcoin', 'Football', 'Snow', 'Iphone', 'Christmas'];
			let mentions = [];
			words.forEach(function(w, index) {
				if(msg.message[w.toLowerCase()] != null && msg.message[w.toLowerCase()] != undefined) {
					mentions.push({word: w, count: msg.message[w.toLowerCase()]});
				}
			});

			if(mentions.length == 0) {
				return;
			}
			//Sort by count descending
			mentions.sort(function(a, b) {
				return b.count - a.count;
			});

			//Update chart
			if(!topWord) {
				let initialSeries = [];
				mentions.forEach(function(m, index) {
					initialSeries.push({
						name: m.word,
						data: [{
							name: m.word,
							value: m.count
						}]
					});
				});
				topWord = displayTopWord(initialSeries, words);
			} else {
				mentions.forEach(function(m, index) {
					topWord.series[index].setData([{
						name: m.word,
						value: m.count
					}]);
				});
			}
			updateListWord(mentions);
		},
		presence: function (presenceEvent) {
			console.log('present');
		}
	});

	chart = displayLiveCountOfTweets();
	averageCount = averageTweetCount();

	var chart;
	var averageCount;
	var topWord;
});
