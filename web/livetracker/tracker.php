<?php
if (isset($_GET['user'])) {
	$gpx_log = 'track_'.$_GET['user'].'.gpx';
	if (!file_exists($gpx_log) || isset($_GET['clear'])) {
		$gpx = simplexml_load_file('template.gpx');	
		$gpx->trk->name = $_GET['user'];
	} else {
		$gpx = simplexml_load_file($gpx_log);
	}
	
	if (isset($_GET['lat']) && isset($_GET['lon']) 
		&& preg_match('/\-?[0-9]+\.?[0-9]*/', $_GET['lat']) 
		&& preg_match('/\-?[0-9]+\.?[0-9]*/', $_GET['lon'])) {

		$trkpt = $gpx->trk->trkseg->addChild('trkpt');
		$trkpt->addAttribute('lat', $_GET['lat']);
		$trkpt->addAttribute('lon', $_GET['lon']);

		if (isset($_GET['timestamp'])){
			date_default_timezone_set('UTC');
			$timestamp = $_GET['timestamp'] / 1000;
			$date = date('Y-m-d',$timestamp).'T'.date('H:i:s',$timestamp).'Z';
			$trkpt->addChild('time', $date);
		}

		if (isset($_GET['altitude']) && ($_GET['altitude'] > 0.01)){
			$trkpt->addChild('ele',$_GET['altitude']);
		}	
	}

	//print_r($gpx);
	$gpx->asXML('log.gpx');
	rename('log.gpx', $gpx_log);
}
