var map;
var markers;

/**
 * Transform from WGS 1984
 */
var fromProjection = new OpenLayers.Projection("EPSG:4326");
/**
 * to Spherical Mercator Projection
 */
var toProjection = new OpenLayers.Projection("EPSG:900913");

function init() {
	map = new OpenLayers.Map("canvas");
	var layerCycleMap = new OpenLayers.Layer.OSM.CycleMap("CycleMap");
	var position = new OpenLayers.LonLat(139.76, 0).transform(fromProjection,
			toProjection);
	var zoom = 2
	map.addLayer(layerCycleMap);
	map.setCenter(position, zoom);

	markers = new OpenLayers.Layer.Markers("Markers");

	map.addLayer(markers);

}

function showMarker(lon, lat, id, name, date) {
	var marker = new OpenLayers.Marker(new OpenLayers.LonLat(lon, lat)
			.transform(fromProjection, toProjection), new OpenLayers.Icon(
			'http://maps.google.com/mapfiles/ms/micons/blue.png'));
	// here add mouseover event
	var popup;
	marker.events.register('mouseover', marker, function(evt) {
		popup = new OpenLayers.Popup.FramedCloud("Popup",
				new OpenLayers.LonLat(lon, lat).transform(fromProjection,
						toProjection), null, "<p>" + id + "</p><p>" + name
						+ "</p><p>" + date + "</p>", null, true);
		map.addPopup(popup);
	});
	// here add mouseout event
	marker.events.register('mouseout', marker, function(evt) {
		popup.hide();
	});

	markers.addMarker(marker);
}
