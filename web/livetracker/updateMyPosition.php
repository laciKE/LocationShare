<?php
if (isset($_POST["user"])) {
	$user=trim(addslashes(strip_tags($_POST['user'])));
	$gpx_log = 'track_'.$user.'.gpx';
	if (!file_exists($gpx_log) || isset($_POST['clear'])) {
		$gpx = simplexml_load_file('template.gpx');	
		$gpx->trk->name = $user;
	} else {
		$gpx = simplexml_load_file($gpx_log);
	}
	
	if (isset($_POST['lat']) && isset($_POST['lon'])
		&& preg_match('/\-?[0-9]+\.?[0-9]*/', $_POST['lat']) 
		&& preg_match('/\-?[0-9]+\.?[0-9]*/', $_POST['lon'])) {

		$trkpt = $gpx->trk->trkseg->addChild('trkpt');
		$trkpt->addAttribute('lat', $_POST['lat']);
		$trkpt->addAttribute('lon', $_POST['lon']);
		
		if (isset($_POST['timestamp'])){
			date_default_timezone_set('UTC');
			$timestamp = $_POST['timestamp'] / 1000;
			$date = date('Y-m-d',$timestamp).'T'.date('H:i:s',$timestamp).'Z';
			$trkpt->addChild('time', $date);
		}
		
		if (isset($_POST['altitude']) && ($_POST['altitude'] > 0.01)){
			$trkpt->addChild('ele',$_POST['altitude']);
		}
		
		if (isset($_POST['message'])) {
			$message=trim(addslashes(strip_tags($_POST['message'])));
			$trkpt->addChild('message',$message);
		}
		
		//print_r($gpx);
		$gpx->asXML('log.gpx');
		rename('log.gpx', $gpx_log);
	}
		

	//$uloz = "update ".isset($_POST['lat'])." ".isset($_POST['lon'])." ".$lat." # ".$lon." # ".$altitude." # ".$timestamp." # ".$message." # ".$user."\n";
	//$file=fopen("position.txt", "a");
	//fwrite($file, $uloz);
	//fclose($file);
	}
?>
