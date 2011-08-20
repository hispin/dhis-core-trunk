/*
 * Copyright (C) 2007-2008  Camptocamp|
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
 * @requires core/GeoStat/Centroid.js
 * @requires core/Color.js
 */

Ext.namespace('mapfish.widgets', 'mapfish.widgets.geostat');

mapfish.widgets.geostat.Centroid = Ext.extend(Ext.Panel, {

    layer: null,

    format: null,

    url: null,

    featureSelection: true,

    nameAttribute: null,

    indicator: null,

    indicatorText: null,

    coreComp: null,

    classificationApplied: false,

    ready: false,

    border: false,

    loadMask: false,

    labelGenerator: null,

    colorInterpolation: false,

    newUrl: false,

    legend: false,

	bounds: false,

    mapView: false,

    mapData: false,
    
    labels: false,
    
    valueType: false,
    
    selectFeatures: false,
    
    organisationUnitSelection: false,
    
    updateValues: false,
    
    isDrillDown: false,

	imageLegend: false,
    
    stores: false,
    
    infrastructuralPeriod: false,
    
    featureOptions: {},
    
    cmp: {},
    
    initComponent: function() {
    
        this.initProperties();
        
        this.createItems();
        
        this.addItems();
        
        this.createSelectFeatures();
        
		mapfish.widgets.geostat.Centroid.superclass.initComponent.apply(this);
    },
    
    setUrl: function(url) {
        this.url = url;
        this.coreComp.setUrl(this.url);
    },

    requestSuccess: function(request) {
        this.ready = true;

        if (this.loadMask && this.rendered) {
            this.loadMask.hide();
        }
    },

    requestFailure: function(request) {
        OpenLayers.Console.error(G.i18n.ajax_request_failed);
    },
    
    getColors: function() {
        var startColor = new mapfish.ColorRgb();
        startColor.setFromHex(this.cmp.startColor.getValue());
        var endColor = new mapfish.ColorRgb();
        endColor.setFromHex(this.cmp.endColor.getValue());
        return [startColor, endColor];
    },
    
    initProperties: function() {
        this.legend = {
            value: G.conf.map_legendset_type_predefined
        };
        
        this.organisationUnitSelection = {
            parent: {
                id: null,
                name: null,
                level: null
            },
            level: {
                level: null,
                name: null
            },
            setValues: function(pid, pn, pl, ll, ln) {
                this.parent.id = pid || this.parent.id;
                this.parent.name = pn || this.parent.name;
                this.parent.level = pl || this.parent.level;
                this.level.level = ll || this.level.level;
                this.level.name = ln || this.level.name;
            },
            getValues: function() {
                return {
                    parent: {
                        id: this.parent.id,
                        name: this.parent.name,
                        level: this.parent.level
                    },
                    level: {
                        level: this.level.level,
                        name: this.level.name
                    }                    
                };
            },
            setValuesOnDrillDown: function(pid, pn) {
                this.parent.id = pid;
                this.parent.name = pn;
                this.parent.level = this.level.level;
                this.level.level++;
                this.level.name = G.stores.organisationUnitLevel.getAt(
                    G.stores.organisationUnitLevel.find('level', this.level.level)).data.name;
                
                return [this.parent.name, this.level.name];
            }                
        };
        
        this.valueType = {
            value: G.conf.map_value_type_indicator,
            setIndicator: function() {
                this.value = G.conf.map_value_type_indicator;
            },
            setDatElement: function() {
                this.value = G.conf.map_value_type_dataelement;
            },
            isIndicator: function() {
                return this.value == G.conf.map_value_type_indicator;
            },
            isDataElement: function() {
                return this.value == G.conf.map_value_type_dataelement;
            }
        };
        
        this.stores = {
            mapLegendTypeIcon: new Ext.data.ArrayStore({
                fields: ['name', 'css'],
                data: [
                    ['0','ux-ic-icon-maplegend-type-0'],
                    ['1','ux-ic-icon-maplegend-type-1'],
                    ['2','ux-ic-icon-maplegend-type-2'],
                    ['3','ux-ic-icon-maplegend-type-3'],
                    ['4','ux-ic-icon-maplegend-type-4'],
                    ['5','ux-ic-icon-maplegend-type-5'],
                    ['6','ux-ic-icon-maplegend-type-6'],
                    ['7','ux-ic-icon-maplegend-type-7'],
                    ['8','ux-ic-icon-maplegend-type-8'],
                    ['9','ux-ic-icon-maplegend-type-9']
                ]
            }),
            indicatorsByGroup: new Ext.data.JsonStore({
                url: G.conf.path_mapping + 'getIndicatorsByIndicatorGroup' + G.conf.type,
                root: 'indicators',
                fields: ['id', 'name', 'shortName'],
                idProperty: 'id',
                sortInfo: {field: 'name', direction: 'ASC'},
                autoLoad: false,
                isLoaded: false,
                listeners: {
                    'load': function(store) {
                        store.isLoaded = true;
                        store.each(
                            function fn(record) {
                                var name = record.get('name');
                                name = name.replace('&lt;', '<').replace('&gt;', '>');
                                record.set('name', name);
                            }
                        );
                    }
                }
            }),
            dataElementsByGroup: new Ext.data.JsonStore({
                url: G.conf.path_mapping + 'getDataElementsByDataElementGroup' + G.conf.type,
                root: 'dataElements',
                fields: ['id', 'name', 'shortName'],
                sortInfo: {field: 'name', direction: 'ASC'},
                autoLoad: false,
                isLoaded: false,
                listeners: {
                    'load': function(store) {
                        store.isLoaded = true;
                        store.each(
                            function fn(record) {
                                var name = record.get('name');
                                name = name.replace('&lt;', '<').replace('&gt;', '>');
                                record.set('name', name);
                            }
                        );
                    }
                }
            }),
            periodsByType: new Ext.data.JsonStore({
                url: G.conf.path_mapping + 'getPeriodsByPeriodType' + G.conf.type,
                root: 'periods',
                fields: ['id', 'name'],
                autoLoad: false,
                isLoaded: false,
                listeners: {
                    'load': G.func.storeLoadListener
                }
            })
        };
    },
    
    createItems: function() {
        
        this.cmp.mapValueType = new Ext.form.ComboBox({
            fieldLabel: G.i18n.mapvaluetype,
            editable: false,
            valueField: 'id',
            displayField: 'name',
            mode: 'local',
            triggerAction: 'all',
            width: G.conf.combo_width,
            value: G.conf.map_value_type_indicator,
            store: new Ext.data.ArrayStore({
                fields: ['id', 'name'],
                data: [
                    [G.conf.map_value_type_indicator, 'Indicator'],
                    [G.conf.map_value_type_dataelement, 'Data element']
                ]
            }),
            listeners: {
                'select': {
                    scope: this,
                    fn: function(cb) {
                        this.valueType.value = cb.getValue();
                        this.prepareMapViewValueType();
                        this.classify(false, true);
                        
                        this.window.cmp.reset.enable();
                    }
                }
            }
        });
        
        this.cmp.indicatorGroup = new Ext.form.ComboBox({
            fieldLabel: G.i18n.indicator_group,
            typeAhead: true,
            editable: false,
            valueField: 'id',
            displayField: 'name',
            mode: 'remote',
            forceSelection: true,
            triggerAction: 'all',
            selectOnFocus: true,
            width: G.conf.combo_width,
            store: G.stores.indicatorGroup,
            listeners: {
                'select': {
                    scope: this,
                    fn: function(cb) {
                        this.cmp.indicator.clearValue();
                        this.stores.indicatorsByGroup.setBaseParam('indicatorGroupId', cb.getValue());
                        this.stores.indicatorsByGroup.load();
                        
                        this.window.cmp.reset.enable();
                    }
                }
            }
        });
        
        this.cmp.indicator = new Ext.form.ComboBox({
            fieldLabel: G.i18n.indicator,
            typeAhead: true,
            editable: false,
            valueField: 'id',
            displayField: 'shortName',
            mode: 'remote',
            forceSelection: true,
            triggerAction: 'all',
            selectOnFocus: true,
            width: G.conf.combo_width,
            store: this.stores.indicatorsByGroup,
            currentValue: null,
            lockPosition: false,
            listeners: {
                'select': {
                    scope: this,
                    fn: function(cb) {
                        this.updateValues = true;
                        
                        Ext.Ajax.request({
                            url: G.conf.path_mapping + 'getMapLegendSetByIndicator' + G.conf.type,
                            method: 'POST',
                            params: {indicatorId: cb.getValue()},
                            scope: this,
                            success: function(r) {
                                var mapLegendSet = Ext.util.JSON.decode(r.responseText).mapLegendSet[0];
                                if (mapLegendSet.id) {
                                    
                                    function load() {
                                        this.cmp.mapLegendSet.setValue(mapLegendSet.id);
                                        this.applyPredefinedLegend();
                                    }
                                    
                                    if (!G.stores.predefinedMapLegendSet.isLoaded) {
                                        G.stores.predefinedMapLegendSet.load({scope: this, callback: function() {
                                            load.call(this);
                                        }});
                                    }
                                    else {
                                        load.call(this);
                                    }
                                }
                                
                                this.classify(false, cb.lockPosition);
                                G.util.setLockPosition(cb);
                            }
                        });
                        
                        this.window.cmp.reset.enable();
                    }
                }
            }
        });
        
        this.cmp.dataElementGroup = new Ext.form.ComboBox({
            fieldLabel: G.i18n.dataelement_group,
            typeAhead: true,
            editable: false,
            valueField: 'id',
            displayField: 'name',
            mode: 'remote',
            forceSelection: true,
            triggerAction: 'all',
            selectOnFocus: true,
            width: G.conf.combo_width,
            store: G.stores.dataElementGroup,
            listeners: {
                'select': {
                    scope: this,
                    fn: function(cb) {
                        this.cmp.dataElement.clearValue();
                        this.stores.dataElementsByGroup.setBaseParam('dataElementGroupId', cb.getValue());
                        this.stores.dataElementsByGroup.load();
                        
                        this.window.cmp.reset.enable();
                    }
                }
            }
        });
        
        this.cmp.dataElement = new Ext.form.ComboBox({
            fieldLabel: G.i18n.dataelement,
            typeAhead: true,
            editable: false,
            valueField: 'id',
            displayField: 'shortName',
            mode: 'remote',
            forceSelection: true,
            triggerAction: 'all',
            selectOnFocus: true,
            width: G.conf.combo_width,
            store: this.stores.dataElementsByGroup,
            lockPosition: false,
            listeners: {
                'select': {
                    scope: this,
                    fn: function(cb) {
                        this.updateValues = true;
                        
                        Ext.Ajax.request({
                            url: G.conf.path_mapping + 'getMapLegendSetByDataElement' + G.conf.type,
                            method: 'POST',
                            params: {dataElementId: cb.getValue()},
                            scope: this,
                            success: function(r) {
                                var mapLegendSet = Ext.util.JSON.decode(r.responseText).mapLegendSet[0];
                                if (mapLegendSet.id) {
                                    
                                    function load() {
                                        this.cmp.mapLegendSet.setValue(mapLegendSet.id);
                                        this.applyPredefinedLegend();
                                    }
                                    
                                    if (!G.stores.predefinedMapLegendSet.isLoaded) {
                                        G.stores.predefinedMapLegendSet.load({scope: this, callback: function() {
                                            load.call(this);
                                        }});
                                    }
                                    else {
                                        load.call(this);
                                    }
                                }
                                
                                this.classify(false, cb.lockPosition);
                                G.util.setLockPosition(cb);
                            }
                        });
                        
                        this.window.cmp.reset.enable();
                    }
                }
            }
        });
        
        this.cmp.periodType = new Ext.form.ComboBox({
            fieldLabel: G.i18n.period_type,
            typeAhead: true,
            editable: false,
            valueField: 'name',
            displayField: 'displayName',
            mode: 'remote',
            forceSelection: true,
            triggerAction: 'all',
            selectOnFocus: true,
            width: G.conf.combo_width,
            store: G.stores.periodType,
            listeners: {
                'select': {
                    scope: this,
                    fn: function(cb) {
                        this.cmp.period.clearValue();
                        this.stores.periodsByType.setBaseParam('name', cb.getValue());
                        this.stores.periodsByType.load();
                        
                        this.window.cmp.reset.enable();
                    }
                }
            }
        });
        
        this.cmp.period = new Ext.form.ComboBox({
            fieldLabel: G.i18n.period,
            typeAhead: true,
            editable: false,
            valueField: 'id',
            displayField: 'name',
            mode: 'remote',
            forceSelection: true,
            triggerAction: 'all',
            selectOnFocus: true,
            width: G.conf.combo_width,
            store: this.stores.periodsByType,
            lockPosition: false,
            listeners: {
                'select': {
                    scope: this,
                    fn: function(cb) {
                        this.updateValues = true;
                        
                        this.classify(false, cb.lockPosition);                        
                        G.util.setLockPosition(cb);
                        
                        this.window.cmp.reset.enable();
                    }
                }
            }
        });
        
        this.cmp.startDate = new Ext.form.DateField({
            fieldLabel: G.i18n.start_date,
            format: 'Y-m-d',
            hidden: true,
            width: G.conf.combo_width,
            listeners: {
                'select': {
                    scope: this,
                    fn: function(df, date) {
                        this.updateValues = true;
                        this.cmp.endDate.setMinValue(date);
                        this.classify(false, true);
                        
                        this.window.cmp.reset.enable();
                    }
                }
            }
        });
        
        this.cmp.endDate = new Ext.form.DateField({
            fieldLabel: G.i18n.end_date,
            format: 'Y-m-d',
            hidden: true,
            width: G.conf.combo_width,
            listeners: {
                'select': {
                    scope: this,
                    fn: function(df, date) {
                        this.updateValues = true;
                        this.cmp.startDate.setMaxValue(date);
                        this.classify(false, true);
                        
                        this.window.cmp.reset.enable();
                    }
                }
            }
        });
        
        this.cmp.mapLegendSet = new Ext.form.ComboBox({
            editable: false,
            valueField: 'id',
            displayField: 'name',
            mode: 'remote',
            fieldLabel: G.i18n.legendset,
            triggerAction: 'all',
            width: G.conf.combo_width,
            store: G.stores.predefinedImageMapLegendSet,
            listeners: {
                'select': {
                    scope: this,
                    fn: function() {
                        this.applyPredefinedLegend();                        
                        this.window.cmp.reset.enable();
                    }
                }
            }
        });
        
        this.cmp.level = new Ext.form.ComboBox({
            fieldLabel: G.i18n.level,
            editable: false,
            valueField: 'level',
            displayField: 'name',
            mode: 'remote',
            forceSelection: true,
            triggerAction: 'all',
            selectOnFocus: true,
            fieldLabel: G.i18n.level,
            width: G.conf.combo_width,
            store: G.stores.organisationUnitLevel,
            listeners: {
                'select': {
                    scope: this,
                    fn: function() {
                        this.formValidation.validateForm.call(this);                        
                        this.window.cmp.reset.enable();
                    }
                }
            }
        });
        
        this.cmp.parent = new Ext.tree.TreePanel({
            bodyStyle: 'padding-left:2px; background-color:#fff',
            height: 315,
            width: 255,
            autoScroll: true,
            lines: false,
            loader: new Ext.tree.TreeLoader({
                dataUrl: G.conf.path_mapping + 'getOrganisationUnitChildren' + G.conf.type
            }),
            root: {
                id: G.system.rootNode.id,
                text: G.system.rootNode.name,
                level: G.system.rootNode.level,
                hasChildrenWithCoordinates: G.system.rootNode.hasChildrenWithCoordinates,
                nodeType: 'async',
                draggable: false,
                expanded: true
            },
            widget: this,
            isSelected: false,
            reset: function() {
                if (this.getSelectionModel().getSelectedNode()) {
                    this.getSelectionModel().getSelectedNode().unselect();
                }                
                this.collapseAll();
                this.getRootNode().expand();
                this.isSelected = false;
                this.widget.window.cmp.apply.disable();
            },
            listeners: {
                'click': {
                    scope: this,
                    fn: function(n) {
                        this.cmp.parent.selectedNode = n;
                        this.cmp.parent.isSelected = true;
                        this.formValidation.validateForm.call(this);
                        
                        this.window.cmp.reset.enable();
                    }
                }
            }
        });
    },
    
    addItems: function() {    
        
        this.items = [
            {
                xtype: 'panel',
                layout: 'column',
                width: 570,
                items: [
                    {
                        xtype: 'form',
                        width: 270,
                        items: [
                            { html: '<div class="window-info">Data options</div>' },
                            this.cmp.mapValueType,
                            this.cmp.indicatorGroup,
                            this.cmp.indicator,
                            this.cmp.dataElementGroup,
                            this.cmp.dataElement,
                            this.cmp.periodType,
                            this.cmp.period,
                            this.cmp.startDate,
                            this.cmp.endDate,
                            { html: '<div class="thematic-br">' },
                            { html: '<div class="window-info">Legend options</div>' },
                            this.cmp.mapLegendSet
                        ]
                    },
                    {
                        xtype: 'panel',
                        width: 270,
                        bodyStyle: 'padding:0 0 0 8px;',
                        items: [
                            { html: '<div class="window-info">' + G.i18n.organisation_unit_level + '</div>' },                            
                            {
                                xtype: 'panel',
                                layout: 'form',
                                items: [
                                    this.cmp.level
                                ]
                            },                            
                            { html: '<div class="thematic-br"></div><div class="thematic-br"></div>' },                            
                            { html: '<div class="window-info">Parent organisation unit</div>' },                            
                            this.cmp.parent
                        ]
                    }
                ]
            }
        ];
    },
    
    createSelectFeatures: function() {
        var scope = this;
        
        var onHoverSelect = function onHoverSelect(feature) {
            if (feature.attributes.name) {
                document.getElementById('featuredatatext').innerHTML =
                    '<div style="' + G.conf.feature_data_style_name + '">' + feature.attributes.name + '</div>' +
                    '<div style="' + G.conf.feature_data_style_value + '">' + feature.attributes.value + '</div>';
            }
            else {
                document.getElementById('featuredatatext').innerHTML = '';
            }
        };
        
        var onHoverUnselect = function onHoverUnselect(feature) {
            if (feature.attributes.name) {
                document.getElementById('featuredatatext').innerHTML = 
                    '<div style="' + G.conf.feature_data_style_empty + '">' + G.i18n.no_feature_selected + '</div>';
            }
            else {
                document.getElementById('featuredatatext').innerHTML = '';
            }
        };
        
        this.selectFeatures = new OpenLayers.Control.newSelectFeature(
            this.layer, {
                onHoverSelect: onHoverSelect,
                onHoverUnselect: onHoverUnselect
            }
        );
        
        G.vars.map.addControl(this.selectFeatures);
        this.selectFeatures.activate();
    },
    
    prepareMapViewValueType: function() {
        var obj = {};
        if (this.valueType.isIndicator()) {
            this.cmp.indicatorGroup.show();
            this.cmp.indicator.show();
            this.cmp.dataElementGroup.hide();
            this.cmp.dataElement.hide();
            obj.components = {
                valueTypeGroup: this.cmp.indicatorGroup,
                valueType: this.cmp.indicator
            };
            obj.stores = {
                valueTypeGroup: G.stores.indicatorGroup,
                valueType: this.stores.indicatorsByGroup
            };
            obj.mapView = {
                valueTypeGroup: 'indicatorGroupId',
                valueType: 'indicatorId'
            };
        }
        else if (this.valueType.isDataElement()) {
            this.cmp.indicatorGroup.hide();
            this.cmp.indicator.hide();
            this.cmp.dataElementGroup.show();
            this.cmp.dataElement.show();
            obj.components = {
                valueTypeGroup: this.cmp.dataElementGroup,
                valueType: this.cmp.dataElement
            };
            obj.stores = {
                valueTypeGroup: G.stores.dataElementGroup,
                valueType: this.stores.dataElementsByGroup
            };
            obj.mapView = {
                valueTypeGroup: 'dataElementGroupId',
                valueType: 'dataElementId'
            };
        }
        return obj;
    },
    
    prepareMapViewDateType: function() {
        var obj = {};
        if (G.system.mapDateType.isFixed()) {
            this.cmp.periodType.show();
            this.cmp.period.show();
            this.cmp.startDate.hide();
            this.cmp.endDate.hide();
            obj.components = {
                c1: this.cmp.periodType,
                c2: this.cmp.period
            };
            obj.stores = {
                c1: G.stores.periodType,
                c2: this.stores.periodsByType
            };
            obj.mapView = {
                c1: 'periodTypeId',
                c2: 'periodId'
            };
        }
        else if (G.system.mapDateType.isStartEnd()) {
            this.cmp.periodType.hide();
            this.cmp.period.hide();
            this.cmp.startDate.show();
            this.cmp.endDate.show();
            obj.components = {
                c1: this.cmp.startDate,
                c2: this.cmp.endDate
            };
            obj.mapView = {
                c1: 'startDate',
                c2: 'endDate'
            };
        }
        return obj;
    },
    
    setMapView: function() {
        var obj = this.prepareMapViewValueType();
        
        function valueTypeGroupStoreCallback() {
            obj.components.valueTypeGroup.setValue(this.mapView[obj.mapView.valueTypeGroup]);
            obj.stores.valueType.setBaseParam(obj.mapView.valueTypeGroup, obj.components.valueTypeGroup.getValue());
            obj.stores.valueType.load({scope: this, callback: function() {
                obj.components.valueType.setValue(this.mapView[obj.mapView.valueType]);
                obj.components.valueType.currentValue = this.mapView[obj.mapView.valueType];
                
                obj = this.prepareMapViewDateType();
                if (G.system.mapDateType.isFixed()) {
                    if (obj.stores.c1.isLoaded) {
                        dateTypeGroupStoreCallback.call(this);
                    }
                    else {
                        obj.stores.c1.load({scope: this, callback: function() {
                            dateTypeGroupStoreCallback.call(this);
                        }});
                    }
                }
                else if (G.system.mapDateType.isStartEnd()) {
                    obj.components.c1.setValue(new Date(this.mapView[obj.mapView.c1]));
                    obj.components.c2.setValue(new Date(this.mapView[obj.mapView.c2]));
                    
                    this.setMapViewLegend();
                }                
            }});
        }
        
        function dateTypeGroupStoreCallback() {
            obj.components.c1.setValue(this.mapView[obj.mapView.c1]);
            
            obj.stores.c2.setBaseParam('name', this.mapView[obj.mapView.c1]);
            obj.stores.c2.load({scope: this, callback: function() {
                obj.components.c2.setValue(this.mapView[obj.mapView.c2]);
                obj.components.c2.currentValue = this.mapView[obj.mapView.c2];
                obj.components.c2.lockPosition = true;
                
                this.setMapViewLegend();
            }});
        }

        if (obj.stores.valueTypeGroup.isLoaded) {
            valueTypeGroupStoreCallback.call(this);
        }
        else {
            obj.stores.valueTypeGroup.load({scope: this, callback: function() {
                valueTypeGroupStoreCallback.call(this);
            }});
        }
    },
    
    setMapViewLegend: function() {
        function predefinedMapLegendSetStoreCallback() {
            this.cmp.mapLegendSet.setValue(this.mapView.mapLegendSetId);
            this.applyPredefinedLegend(true);
        }
        
        if (G.stores.predefinedMapLegendSet.isLoaded) {
            predefinedMapLegendSetStoreCallback.call(this);
        }
        else {
            G.stores.predefinedMapLegendSet.load({scope: this, callback: function() {
                predefinedMapLegendSetStoreCallback.call(this);
            }});
        }
    },
    
    setMapViewMap: function() {
        this.organisationUnitSelection.setValues(this.mapView.parentOrganisationUnitId, this.mapView.parentOrganisationUnitName,
            this.mapView.parentOrganisationUnitLevel, this.mapView.organisationUnitLevel, this.mapView.organisationUnitLevelName);
            
        this.cmp.parent.reset();
        this.cmp.parent.selectedNode = {attributes: {
            id: this.mapView.parentOrganisationUnitId,
            text: this.mapView.parentOrganisationUnitName,
            level: this.mapView.parentOrganisationUnitLevel
        }};
            
        G.stores.organisationUnitLevel.load({scope: this, callback: function() {
            this.cmp.level.setValue(this.mapView.organisationUnitLevel);
            G.vars.activePanel.setCentroid();
            this.loadGeoJson();
        }});
    },
	
	applyPredefinedLegend: function(isMapView) {
        this.legend.value = G.conf.map_legendset_type_predefined;
		var mls = this.cmp.mapLegendSet.getValue();
		Ext.Ajax.request({
			url: G.conf.path_mapping + 'getMapLegendsByMapLegendSet' + G.conf.type,
			method: 'POST',
			params: {mapLegendSetId: mls},
            scope: this,
			success: function(r) {
				var mapLegends = Ext.util.JSON.decode(r.responseText).mapLegends;
				this.symbolizerInterpolation = [];
				this.bounds = [];
				for (var i = 0; i < mapLegends.length; i++) {
					if (this.bounds[this.bounds.length-1] != mapLegends[i].startValue) {
						if (this.bounds.length !== 0) {
							this.symbolizerInterpolation.push('blank');
						}
						this.bounds.push(mapLegends[i].startValue);
					}
					this.symbolizerInterpolation.push(mapLegends[i].image);
					this.bounds.push(mapLegends[i].endValue);
				}
                
                if (isMapView) {
                    this.setMapViewMap();
                }
                else {
                    this.classify(false, true);
                }                   
			}
		});
	},
    
    formValidation: {
        validateForm: function(exception) {
            if (this.cmp.mapValueType.getValue() == G.conf.map_value_type_indicator) {
                if (!this.cmp.indicator.getValue()) {
                    if (exception) {
                        Ext.message.msg(false, G.i18n.form_is_not_complete);
                    }
                    return false;
                }
            }
            else if (this.cmp.mapValueType.getValue() == G.conf.map_value_type_dataelement) {
                if (!this.cmp.dataElement.getValue()) {
                    if (exception) {
                        Ext.message.msg(false, G.i18n.form_is_not_complete);
                    }
                    return false;
                }
            }

            if (G.system.mapDateType.isFixed()) {
                if (!this.cmp.period.getValue()) {
                    if (exception) {
                        Ext.message.msg(false, G.i18n.form_is_not_complete);
                    }
                    return false;
                }
            }
            else {
                if (!this.cmp.startDate.getValue() || !this.cmp.endDate.getValue()) {
                    if (exception) {
                        Ext.message.msg(false, G.i18n.form_is_not_complete);
                    }
                    return false;
                }
            }

            if (!this.cmp.parent.selectedNode || !this.cmp.level.getValue()) {
                if (exception) {
                    Ext.message.msg(false, G.i18n.form_is_not_complete);
                }
                this.window.cmp.apply.disable();
                return false;
            }
            
            if (this.cmp.parent.selectedNode.attributes.level > this.cmp.level.getValue()) {
                if (exception) {
                    Ext.message.msg(false, 'Invalid parent organisation unit');
                }
                this.window.cmp.apply.disable();
                return false;
            }
            
            if (!this.cmp.mapLegendSet.getValue()) {
                if (exception) {
                    Ext.message.msg(false, G.i18n.form_is_not_complete);
                }
                return false;
            }
            
            if (this.cmp.parent.isSelected) {
                this.window.cmp.apply.enable();
            }
            
            return true;
        }
    },
    
    formValues: {
		getAllValues: function() {
			return {
                mapValueType: this.cmp.mapValueType.getValue(),
                indicatorGroupId: this.valueType.isIndicator() ? this.cmp.indicatorGroup.getValue() : null,
                indicatorId: this.valueType.isIndicator() ? this.cmp.indicator.getValue() : null,
				indicatorName: this.valueType.isIndicator() ? this.cmp.indicator.getRawValue() : null,
                dataElementGroupId: this.valueType.isDataElement() ? this.cmp.dataElementGroup.getValue() : null,
                dataElementId: this.valueType.isDataElement() ? this.cmp.dataElement.getValue() : null,
				dataElementName: this.valueType.isDataElement() ? this.cmp.dataElement.getRawValue() : null,
                mapDateType: G.system.mapDateType.value,
                periodTypeId: G.system.mapDateType.isFixed() ? this.cmp.periodType.getValue() : null,
                periodId: G.system.mapDateType.isFixed() ? this.cmp.period.getValue() : null,
                periodName: G.system.mapDateType.isFixed() ? this.cmp.period.getRawValue() : null,
                startDate: G.system.mapDateType.isStartEnd() ? this.cmp.startDate.getRawValue() : null,
                endDate: G.system.mapDateType.isStartEnd() ? this.cmp.endDate.getRawValue() : null,
                parentOrganisationUnitId: this.organisationUnitSelection.parent.id,
                parentOrganisationUnitLevel: this.organisationUnitSelection.parent.level,
                parentOrganisationUnitName: this.organisationUnitSelection.parent.name,
                organisationUnitLevel: this.organisationUnitSelection.level.level,
                organisationUnitLevelName: this.organisationUnitSelection.level.name,
                mapLegendSetId: this.cmp.mapLegendSet.getValue(),
                longitude: G.vars.map.getCenter().lon,
                latitude: G.vars.map.getCenter().lat,
                zoom: parseFloat(G.vars.map.getZoom())
			};
		},
        
        getLegendInfo: function() {
            return {
                name: this.valueType.isIndicator() ? this.cmp.indicator.getRawValue() : this.cmp.dataElement.getRawValue(),
                time: G.system.mapDateType.isFixed() ? this.cmp.period.getRawValue() : this.cmp.startDate.getRawValue() + ' to ' + this.cmp.endDate.getRawValue(),
                map: this.organisationUnitSelection.level.name + ' / ' + this.organisationUnitSelection.parent.name
            };
        },
        
        getImageExportValues: function() {
			return {
				mapValueTypeValue: this.cmp.mapValueType.getValue() == G.conf.map_value_type_indicator ?
					this.cmp.indicator.getRawValue() : this.cmp.dataElement.getRawValue(),
				dateValue: G.system.mapDateType.isFixed() ?
					this.cmp.period.getRawValue() : new Date(this.cmp.startDate.getRawValue()).format('Y M j') + ' - ' + new Date(this.cmp.endDate.getRawValue()).format('Y M j')
			};
		},
        
        clearForm: function(clearLayer) {
            this.cmp.mapValueType.setValue(G.conf.map_value_type_indicator);
            this.valueType.setIndicator();
            this.prepareMapViewValueType();
            this.cmp.indicatorGroup.clearValue();
            this.cmp.indicator.clearValue();
            this.cmp.dataElementGroup.clearValue();
            this.cmp.dataElement.clearValue();
            
            G.system.mapDateType.setFixed();
            this.prepareMapViewDateType();
            this.cmp.periodType.clearValue();
            this.cmp.period.clearValue();
            this.cmp.startDate.reset();
            this.cmp.endDate.reset();
            
            this.cmp.mapLegendSet.clearValue();
            
            this.cmp.level.clearValue();
            this.cmp.parent.reset();
            
            this.window.cmp.apply.disable();
            this.window.cmp.reset.disable();
            
            if (clearLayer) {            
                document.getElementById(this.legendDiv).innerHTML = '';                
                this.layer.destroyFeatures();
                this.layer.setVisibility(false);
            }
        }
	},
    
    loadGeoJson: function() {
        G.vars.mask.msg = G.i18n.loading_geojson;
        G.vars.mask.show();
        G.vars.activeWidget = this;
        this.updateValues = true;
        
        this.setUrl(G.conf.path_mapping + 'getGeoJson.action?' +
            'parentId=' + this.organisationUnitSelection.parent.id +
            '&level=' + this.organisationUnitSelection.level.level
        );
    },

    classify: function(exception, lockPosition) {
        if (this.formValidation.validateForm.apply(this, [exception])) {
            G.vars.mask.msg = G.i18n.aggregating_map_values;
            G.vars.mask.show();
            
            G.vars.lockPosition = lockPosition;
            
            if (this.mapView) {
                if (this.mapView.longitude && this.mapView.latitude && this.mapView.zoom) {
                    var point = G.util.getTransformedPointByXY(this.mapView.longitude, this.mapView.latitude);
                    G.vars.map.setCenter(new OpenLayers.LonLat(point.x, point.y), this.mapView.zoom);
                    G.vars.lockPosition = true;
                }
                this.mapView = false;
            }
            
            if (this.updateValues) {
                var dataUrl = this.valueType.isIndicator() ? 'getIndicatorMapValues' : 'getDataElementMapValues';
                var params = {
                    id: this.valueType.isIndicator() ? this.cmp.indicator.getValue() : this.cmp.dataElement.getValue(),
                    periodId: G.system.mapDateType.isFixed() ? this.cmp.period.getValue() : null,
                    startDate: G.system.mapDateType.isStartEnd() ? new Date(this.cmp.startDate.getValue()).format('Y-m-d') : null,
                    endDate: G.system.mapDateType.isStartEnd() ? new Date(this.cmp.endDate.getValue()).format('Y-m-d') : null,
                    parentId: this.organisationUnitSelection.parent.id,
                    level: this.organisationUnitSelection.level.level
                };
                
                Ext.Ajax.request({
                    url: G.conf.path_mapping + dataUrl + G.conf.type,
                    method: 'POST',
                    params: params,
                    scope: this,
                    success: function(r) {
                        var mapvalues = Ext.util.JSON.decode(r.responseText).mapValues;
                        
                        if (!this.layer.features.length) {
                            Ext.message.msg(false, 'No coordinates found');
                            G.vars.mask.hide();
                            return;
                        }
                        
                        if (mapvalues.length === 0) {
                            Ext.message.msg(false, G.i18n.current_selection_no_data);
                            G.vars.mask.hide();
                            return;
                        }

                        for (var i = 0; i < mapvalues.length; i++) {
                            for (var j = 0; j < this.layer.features.length; j++) {
                                if (mapvalues[i].orgUnitName == this.layer.features[j].attributes.name) {
                                    this.layer.features[j].attributes.value = parseFloat(mapvalues[i].value);
                                    this.layer.features[j].attributes.labelString = this.layer.features[j].attributes.name + ' (' + this.layer.features[j].attributes.value + ')';
                                    this.layer.features[j].attributes.name = G.util.cutString(this.layer.features[j].attributes.name, 30);
                                    break;
                                }
                            }
                        }
                        
                        this.updateValues = false;
                        this.applyValues();
                    }
                });
            }
            else {
                this.applyValues();
            }
        }
    },

    applyValues: function() {
		var options = {
            indicator: 'value',
            method: G.conf.classify_by_equal_intervals
        };
        
        G.vars.activeWidget = this;        
        this.coreComp.applyClassification(options, this);
        this.classificationApplied = true;
        
        G.vars.mask.hide();
    },
    
    onRender: function(ct, position) {
        mapfish.widgets.geostat.Centroid.superclass.onRender.apply(this, arguments);

		var coreOptions = {
            'layer': this.layer,
            'format': this.format,
            'url': this.url,
            'requestSuccess': this.requestSuccess.createDelegate(this),
            'requestFailure': this.requestFailure.createDelegate(this),
            'featureSelection': this.featureSelection,
            'nameAttribute': this.nameAttribute,
            'legendDiv': this.legendDiv,
            'labelGenerator': this.labelGenerator
        };

        this.coreComp = new mapfish.GeoStat.Centroid(this.map, coreOptions);
    }
});

Ext.reg('centroid', mapfish.widgets.geostat.Centroid);
