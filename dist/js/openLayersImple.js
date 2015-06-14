var map;
var markers;

function init() {
	map = new OpenLayers.Map("canvas");
	var mapnik = new OpenLayers.Layer.OSM();
    var layerCycleMap = new OpenLayers.Layer.OSM("OpenCycleMap");
	var fromProjection = new OpenLayers.Projection("EPSG:4326"); // Transform
																	// from WGS
																	// 1984
	var toProjection = new OpenLayers.Projection("EPSG:900913"); // to
																	// Spherical
																	// Mercator
																	// Projection
	var position = new OpenLayers.LonLat(139.76, 0).transform(
			fromProjection, toProjection);
	var zoom = 2
	 var jpl_wms = new OpenLayers.Layer.WMS( "NASA Global Mosaic",
             "http://wms.jpl.nasa.gov/wms.cgi", 
             {layers: "modis,global_mosaic"});
	map.addLayer(mapnik);
	map.setCenter(position, zoom);

	markers = new OpenLayers.Layer.Markers("Markers");
	map.addLayer(markers);


}

function showMarker(lon, lat) {
	var marker = new OpenLayers.Marker(new OpenLayers.LonLat(lon, lat)
			.transform(new OpenLayers.Projection("EPSG:4326"),
					new OpenLayers.Projection("EPSG:900913")));
	markers.addMarker(marker);
}
