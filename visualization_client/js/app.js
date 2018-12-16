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

function displayTopWord() {
	return Highcharts.chart('topWords', {
		chart: {
			type: 'packedbubble',
			height: '100%'
		},
		title: {
			text: 'Number of mentions among Trump, Bitcoin, Christmas and Snow in the last minutes'
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
		series: [{
			name: 'Trump',
			data: [{
				name: 'Trump',
				value: 0
			}]
		}, {
			name: 'Bitcoin',
			data: [{
				name: "Bitcoin",
				value: 0
			}]
		},
		{
			name: 'Football',
			data: [{
				name: "Football",
				value: 0
			}]
		}, {
			name: 'Snow',
			data: [{
				name: "Snow",
				value: 0
			}]
		}],
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

$('document').ready(function (e) {
	var pubnub = new PubNub({
		publishKey: 'pub-c-7c748e9e-6003-42be-ab7a-b92472d65f44',
		subscribeKey: 'sub-c-30f86508-cee8-11e7-91cc-2ef9da9e0d0e'
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
			let words = ['Trump', 'Bitcoin', 'Football', 'Snow'];
			words.forEach(function(w, index) {
				if(msg.message[w.toLowerCase()] != null && msg.message[w.toLowerCase()] != undefined) {
					topWord.series[index].setData([{
							name: w,
							value: msg.message[w.toLowerCase()]
						}]);
				}
			});
		},
		presence: function (presenceEvent) {
			console.log('present');
		}
	});

	chart = displayLiveCountOfTweets();
	averageCount = averageTweetCount();
	topWord = displayTopWord();
	// setInterval(function(e) {
	// 	topWord.series[0].setData([{
	// 			name: 'Trump',
	// 			value: Math.random() * 1000
	// 		}]);
	// 		topWord.series[2].setData([{
	// 			name: 'Chrismat',
	// 			value: Math.random() * 1000
	// 		}]);
	// }, 2000);

	var chart;
	var averageCount;
	var topWord;
});