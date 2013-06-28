<!DOCTYPE html> 
<html> 
<head>
	<meta charset="utf-8">
	<title>Live Tracker</title>
</head>
<body>
<?php
if (isset($_GET['user']) && file_exists('track_'.$_GET['user'].'.gpx')) {
	$gpx_log = 'track_'.$_GET['user'].'.gpx';
	echo "<iframe style='display: block; width: 1000px; height: 600px; margin-left: auto; margin-right: auto;' src='http://embedded.freemap.sk/?load=http://nodla.wz.sk/livetracker/$gpx_log&amp;nomarker&amp;layers=T' scrolling='no' marginheight='0' marginwidth='0' frameborder='0'></iframe>\n";
}
?>
</body>
</html>
