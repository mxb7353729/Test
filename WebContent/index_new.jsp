<?xml version="1.0" encoding="UTF-8" ?>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>Insert title here</title>
<link rel="stylesheet" href="css/ol.css">
<SCRIPT type="text/javascript" src="js/ol-debug.js"></SCRIPT>
</head>
<body>
<div id="map" class="map"></div>
<script>
	var layer = new ol.layer.Vector({
	    source: new ol.source.Vector()
	});
	var raster = new ol.layer.Tile({ 
	 	source: new ol.source.XYZ({
	         url: 'http://a.map.icttic.cn:81/engine?st=GetImage&box={x},{y}&lev={z}&type=vect&uid=yiweihang'//地图
	         	//http://a.map.icttic.cn:81//engine?st=GetImage&box=15,7&lev=4&type=tran&uid=ctticB6156A2D23AEF0F07EF8E2337558F372//混合地图
	         	//http://a.map.icttic.cn:81//engine?st=GetImage&box=15,7&lev=4&type=sate&uid=ctticB6156A2D23AEF0F07EF8E2337558F372//影像地图
	     })
	   });
	
    var map = new ol.Map({
        view: new ol.View({
        	center: ol.proj.transform(
                    [104, 30], 'EPSG:4326', 'EPSG:3857'),
            zoom: 10       //用于缩放视图的初始分辨率
        }),
      
        layers: [raster, layer],
       // renderer: 'canvas',
   
        target: 'map'
    });
  
    // 在地图上添加一个圆
    
    //var _geometry = new ol.geom.Circle(center:[104, 32],radius:8))
   // var circle = new ol.Feature({
    //    geometry: _geometry,
   // });
   //var anchor = new ol.Overlay({
	//	    element: document.getElementById('marker'),
	// });
   
   //anchor.setPosition([105.388, 30.09]);
   // 然后添加到map上
   //map.addOverlay(anchor);
   var marker = new ol.Feature({
        geometry: new ol.geom.Point(ol.proj.transform(
              [105.388, 30.09], 'EPSG:4326', 'EPSG:3857'))
    })
   marker.setStyle(new ol.style.Icon({
       image: new ol.style.Icon({
    	   src: 'http://a.map.icttic.cn:81/images/marker.png'
       		})
   }));
   layer.getSource().addFeature(marker);
   
   
   /*var circle = new ol.Feature({
        geometry: new ol.geom.Point(ol.proj.transform(
              [104, 30], 'EPSG:4326', 'EPSG:3857'))
    })
    circle.setStyle(new ol.style.Style({
        image: new ol.style.Circle({
            radius: 20,
            fill: new ol.style.Fill({
                color: 'yellow'
            })
        })
    }));

    layer.getSource().addFeature(circle);
    
    
    
 // 在地图上添加一个点
    var point = new ol.Feature({
        geometry: new ol.geom.Point(ol.proj.transform(
              [104.08, 30.07], 'EPSG:4326', 'EPSG:3857'))
    })
    point.setStyle(new ol.style.Style({
        image: new ol.style.Circle({
            radius: 5,
            fill: new ol.style.Fill({
                color: 'yellow'
            })
        })
    }));

    layer.getSource().addFeature(point);
    
    
 // 在地图上添加一个多边形
    var tranformFn = ol.proj.getTransform('EPSG:4326', 'EPSG:3857');//转换坐标
    var geometry = new ol.geom.Polygon(
   		 [[[104.15, 30.06],
		    [104.08, 30.27],
		    [104.18, 30.57],
		    [104.388, 30.07]]])
    geometry.applyTransform(tranformFn);
    var polygon = new ol.Feature({
        geometry: geometry
    })
    //var poly = polygon.getGeometry();
    
    polygon.setStyle(new ol.style.Style({
        fill: new ol.style.Fill({
        	color: 'red'
        }),
	}));

    layer.getSource().addFeature(polygon);*/
    
    // 添加一个绘制的面使用的layer
  /*  var polygonLayer = new ol.layer.Vector({
        source: new ol.source.Vector(),
        style: new ol.style.Style({
            stroke: new ol.style.Stroke({
                color: 'red',
                size: 1
            })
        })
    })
    map.addLayer(polygonLayer);

    map.addInteraction(new ol.interaction.Draw({
        type: 'Polygon',
        source: polygonLayer.getSource()    // 注意设置source，这样绘制好的线，就会添加到这个source里
    }));*/

</script>
<a class="overlay" id="marker" target="_blank" href="http://a.map.icttic.cn:81/images/marker.png">Vienna</a>
</body>
</html>