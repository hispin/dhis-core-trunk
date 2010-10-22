/*
 * Copyright (C) 2007  Camptocamp
 *
 * This file is part of MapFish Client
 *
 * MapFish Client is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MapFish Client is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MapFish Client.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * @requires core/GeoStat.js
 */

mapfish.GeoStat.Symbol = OpenLayers.Class(mapfish.GeoStat, {

    colors: [
        new mapfish.ColorRgb(255, 255, 0),
        new mapfish.ColorRgb(255, 0, 0)
    ],

    method: mapfish.GeoStat.Distribution.CLASSIFY_BY_QUANTILS,

    numClasses: 5,
	
	minSize: 3,
	
	maxSize: 15,
	
	minVal: null,
	
	maxVal: null,

    defaultSymbolizer: {'fillOpacity': 1},

    classification: null,

    colorInterpolation: null,

    initialize: function(map, options) {
        mapfish.GeoStat.prototype.initialize.apply(this, arguments);
    },

    updateOptions: function(newOptions) {
        var oldOptions = OpenLayers.Util.extend({}, this.options);
        this.addOptions(newOptions);
        if (newOptions) {
            this.setClassification();
        }
    },
    
    createColorInterpolation: function() {
        var initialColors = this.colors;
        var numColors = this.classification.bins.length;
		var mapLegendType = ACTIVEPANEL == GLOBALS.config.organisationUnitAssignment ?
            GLOBALS.config.map_legend_type_automatic : Ext.getCmp('maplegendtype_cb2').getValue();
		
		if (mapLegendType == GLOBALS.config.map_legend_type_automatic) {
			this.colorInterpolation = proportionalSymbol.colorInterpolation = mapfish.ColorRgb.getColorsArrayByRgbInterpolation(initialColors[0], initialColors[1], numColors);
			for (var i = 0; i < proportionalSymbol.imageLegend.length && i < this.colorInterpolation.length; i++) {
				proportionalSymbol.imageLegend[i].color = this.colorInterpolation[i].toHexString();
			}
		}
		else if (mapLegendType == GLOBALS.config.map_legend_type_predefined) {
			this.colorInterpolation = proportionalSymbol.colorInterpolation;
			for (var i = 0; i < proportionalSymbol.imageLegend.length && i < proportionalSymbol.colorInterpolation.length; i++) {
				proportionalSymbol.imageLegend[i].color = proportionalSymbol.colorInterpolation[i].toHexString();
			}
		}
    },

    setClassification: function() {
        var values = [];

        for (var i = 0; i < this.layer.features.length; i++) {
           // values.push(this.layer.features[i].attributes[this.colorIndicator]);
           values.push(this.layer.features[i].attributes.value);
        }
        
        var distOptions = {
            'labelGenerator' : this.options.labelGenerator
        };
        var dist = new mapfish.GeoStat.Distribution(values, distOptions);

		// this.minVal = dist.minVal;
        // this.maxVal = dist.maxVal;

        this.classification = dist.classify(
            this.method,
            this.numClasses,
            null
        );
        this.createColorInterpolation();
    },

    applyClassification: function(options) {
        this.updateOptions(options);

		// var calculateRadius = OpenLayers.Function.bind(
            // function(feature) {
                // var value = feature.attributes[this.sizeIndicator];
                // var size = (value - this.minVal) / (this.maxVal - this.minVal) *
                           // (this.maxSize - this.minSize) + this.minSize;
                // return size;
            // }, this
        // );
        // this.extendStyle(null,
            // {'pointRadius': '${calculateRadius}'},
            // {'calculateRadius': calculateRadius}
        // );
    
        var boundsArray = this.classification.getBoundsArray();         
        var rules = new Array(boundsArray.length-1);
        for (var i = 0; i < boundsArray.length-1; i++) {
            var rule = new OpenLayers.Rule({
                symbolizer: {fillColor: this.colorInterpolation[i].toHexString()},
                filter: new OpenLayers.Filter.Comparison({
                    type: OpenLayers.Filter.Comparison.BETWEEN,
                    property: this.indicator,
                    lowerBoundary: boundsArray[i],
                    upperBoundary: boundsArray[i + 1]
                })
            });
            rules[i] = rule;
        }
        this.extendStyle(rules);
        mapfish.GeoStat.prototype.applyClassification.apply(this, arguments);
    },

    updateLegend: function() {
        if (!this.legendDiv) {
            return;
        }

        this.legendDiv.update("");
        for (var i = 0; i < this.classification.bins.length; i++) {
            var element = document.createElement("div");
            element.style.backgroundColor = this.colorInterpolation[i].toHexString();
            element.style.width = "30px";
            element.style.height = "15px";
            element.style.cssFloat = "left";
            element.style.marginRight = "10px";
            this.legendDiv.appendChild(element);

            element = document.createElement("div");
            element.innerHTML = this.classification.bins[i].label;
            this.legendDiv.appendChild(element);

            element = document.createElement("div");
            element.style.clear = "left";
            this.legendDiv.appendChild(element);
        }
    },

    CLASS_NAME: "mapfish.GeoStat.Symbol"
});
