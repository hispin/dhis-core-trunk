var validationRules = {
	"user" : {
		"username" : {
			"required" : true,
			"rangelength" : [ 2, 140 ],
			"firstletteralphabet" : true,
			"alphanumeric" : true
		},
		"firstName" : {
			"required" : true,
			"rangelength" : [ 2, 140 ]
		},
		"surname" : {
			"required" : true,
			"rangelength" : [ 2, 140 ]
		},
		"password" : {
			"required" : true,
			"password" : true,
			"notequalto" : "#username",
			"rangelength" : [ 8, 35 ]
		},
		"rawPassword" : {
			"required" : true,
			"password" : true,
			"rangelength" : [ 8, 35 ]
		},
		"retypePassword" : {
			"required" : true,
			"equalTo" : "#rawPassword"
		},
		"email" : {
			"email" : true,
			"rangelength" : [ 0, 160 ]
		},
		"phoneNumber" : {
			"rangelength" : [ 0, 80 ]
		},
		"roleValidator" : {
			"required" : true
		}
	},
	"role" : {
		"name" : {
			"required" : true,
			"rangelength" : [ 2, 140 ]
		},
		"description" : {
			"required" : true,
			"rangelength" : [ 2, 210 ]
		}
	},
	"userGroup" : {
		"name" : {
			"required" : true,
			"rangelength" : [ 2, 210 ],
			"alphanumericwithbasicpuncspaces" : true,
			"firstletteralphabet" : true
		},
		"memberValidator" : {
			"required" : true
		}
	},
	"organisationUnit" : {
		"name" : {
			"required" : true,
			"rangelength" : [ 2, 160 ]
		},
		"shortName" : {
			"required" : true,
			"rangelength" : [ 2, 25 ]
		},
		"code" : {
			"required" : true,
			"rangelength" : [ 0, 25 ]
		},
		"openingDate" : {
			"required" : true
		},
		"url" : {
			"url" : true,
			"rangelength" : [ 0, 255 ]
		},
		"contactPerson" : {
			"rangelength" : [ 0, 255 ]
		},
		"address" : {
			"rangelength" : [ 0, 255 ]
		},
		"email" : {
			"email" : true,
			"rangelength" : [ 0, 250 ]
		},
		"phoneNumber" : {
			"rangelength" : [ 0, 255 ]
		}
	},
	"organisationUnitGroup" : {
		"name" : {
			"required" : true,
			"rangelength" : [ 2, 160 ]
		}
	},
	"organisationUnitGroupSet" : {
		"name" : {
			"required" : true,
			"rangelength" : [ 2, 230 ]
		},
		"description" : {
			"required" : true,
			"rangelength" : [ 2, 255 ]
		}
	},
	"dataEntry" : {
		"name" : {
			"required" : true,
			"rangelength" : [ 4, 100 ]
		}
	},
	"section" : {
		"sectionName" : {
			"required" : true,
			"rangelength" : [ 2, 160 ]
		},
		"selectedList" : {
			"required" : true
		}
	},
	"dataSet" : {
		"name" : {
			"required" : true,
			"alphanumericwithbasicpuncspaces" : true,
			"firstletteralphabet" : false,
			"rangelength" : [ 4, 150 ]
		},
		"shortName" : {
			"required" : true,
			"alphanumericwithbasicpuncspaces" : true,
			"firstletteralphabet" : false,
			"rangelength" : [ 2, 25 ]
		},
		"code" : {
			"alphanumericwithbasicpuncspaces" : true,
			"notOnlyDigits" : false,
			"rangelength" : [ 0, 25 ]
		},
		"frequencySelect" : {
			"required" : true
		}
	},
	"sqlView" : {
		"name" : {
			"required" : true,
			"rangelength" : [ 2, 50 ]
		},
		"description" : {
			"required" : true,
			"rangelength" : [ 2, 255 ]
		},
		"sqlquery" : {
			"required" : true,
			"rangelength" : [ 1, 255 ]
		}
	},
	"dataLocking" : {
		"selectedPeriods" : {
			"required" : true
		},
		"selectedDataSets" : {
			"required" : true
		}
	},
	"dataBrowser" : {
		"periodTypeId" : {
			"required" : true
		},
		"mode" : {
			"required" : true
		}
	},
	"minMax" : {
		"dataSetIds" : {
			"required" : true
		}
	},
	"concept" : {
		"name" : {
			"required" : true,
			"rangelength" : [ 3, 10 ]
		}
	},
	"dataElement" : {
		"name" : {
			"required" : true,
			"rangelength" : [ 3, 150 ],
			"alphanumericwithbasicpuncspaces" : true,
			"notOnlyDigits" : true
		},
		"shortName" : {
			"required" : true,
			"rangelength" : [ 2, 25 ],
			"alphanumericwithbasicpuncspaces" : true,
			"notOnlyDigits" : true
		},
		"alternativeName" : {
			"rangelength" : [ 3, 150 ],
			"alphanumericwithbasicpuncspaces" : true,
			"notOnlyDigits" : true
		},
		"code" : {
			"rangelength" : [ 0, 25 ],
			"alphanumericwithbasicpuncspaces" : true,
			"notOnlyDigits" : false
		},
		"description" : {
			"rangelength" : [ 3, 250 ],
			"alphanumericwithbasicpuncspaces" : true,
			"notOnlyDigits" : true
		},
		"url" : {
			"url" : true,
			"rangelength" : [ 0, 255 ]
		}
	},
	"dateElementCategoryCombo" : {
		"name" : {
			"required" : true,
			"rangelength" : [ 2, 160 ]
		},
		"selectedList" : {
			"required" : true
		}
	},
	"dateElementCategory" : {
		"name" : {
			"required" : true,
			"rangelength" : [ 2, 160 ]
		},
		"conceptId" : {
			"required" : true
		},
		"memberValidator" : {
			"required" : true
		}
	},
	"dataElementGroup" : {
		"name" : {
			"required" : true,
			"alphanumericwithbasicpuncspaces" : true,
			"notOnlyDigits" : true,
			"firstletteralphabet" : true,
			"rangelength" : [ 3, 150 ]
		}
	},
	"dataElementGroupSet" : {
		"name" : {
			"required" : true,
			"rangelength" : [ 2, 230 ]
		}
	},
	"dataDictionary" : {
		"name" : {
			"required" : true,
			"rangelength" : [ 2, 160 ]
		},
		"description" : {
			"rangelength" : [ 0, 255 ]
		},
		"region" : {
			"rangelength" : [ 0, 255 ]
		},
		"memberValidator" : {
			"required" : true
		},
		"memberValidatorIn" : {
			"required" : true
		}
	},
	"indicator" : {
		"name" : {
			"required" : true,
			"rangelength" : [ 3, 150 ],
			"alphanumericwithbasicpuncspaces" : true,
			"nostartwhitespace" : true
		},
		"shortName" : {
			"required" : true,
			"rangelength" : [ 2, 25 ],
			"alphanumericwithbasicpuncspaces" : true,
			"firstletteralphabet" : true
		},
		"alternativeName" : {
			"rangelength" : [ 3, 150 ],
			"alphanumericwithbasicpuncspaces" : true,
			"firstletteralphabet" : true
		},
		"code" : {
			"rangelength" : [ 0, 25 ],
			"alphanumericwithbasicpuncspaces" : true,
			"notOnlyDigits" : false
		},
		"description" : {
			"rangelength" : [ 3, 250 ],
			"alphanumericwithbasicpuncspaces" : true,
			"firstletteralphabet" : true
		},
		"url" : {
			"url" : true,
			"rangelength" : [ 0, 255 ]
		},
		"indicatorTypeId" : {
			"required" : true
		},
		"denominator" : {
			"required" : true
		}
	},
	"indicatorGroup" : {
		"name" : {
			"required" : true,
			"rangelength" : [ 3, 150 ],
			"alphanumericwithbasicpuncspaces" : true
		}
	},
	"indicatorGroupSet" : {
		"name" : {
			"required" : true,
			"rangelength" : [ 2, 230 ]
		}
	},
	"indicatorType" : {
		"name" : {
			"required" : true,
			"rangelength" : [ 3, 150 ],
			"alphanumericwithbasicpuncspaces" : true
		},
		"factor" : {
			"required" : true,
			"rangelength" : [ 1, 10 ],
			"digits" : true
		}
	},
	"validationRule" : {
		"name" : {
			"required" : true,
			"rangelength" : [ 2, 160 ]
		},
		"description" : {
			"rangelength" : [ 2, 160 ]
		},
		"periodTypeName" : {
			"required" : true
		},
		"operator" : {
			"required" : true
		},
		"leftSideExpression" : {
			"required" : true
		},
		"rightSideExpression" : {
			"required" : true
		}
	},
	"validationRuleGroup" : {
		"name" : {
			"required" : true,
			"rangelength" : [ 2, 160 ]
		},
		"description" : {
			"rangelength" : [ 2, 160 ]
		}
	}
}
