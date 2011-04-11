jQuery(document).ready(function() {
	validation2('addDataSetForm', function(form) {
		form.submit()
	}, {
		'beforeValidateHandler' : function() {
			selectAllById('selectedList')
		},
		'rules' : getValidationRules("dataSet")
	});

	checkValueIsExist("name", "validateDataSet.action");
	checkValueIsExist("shortName", "validateDataSet.action");
	checkValueIsExist("code", "validateDataSet.action");
});
