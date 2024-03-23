/**
 * 
 */

$(document).on('ready', function(){
	
	$.fn.setInputHasValue = function(){
		if(this.val() !== null && this.val().length > 0){
			this.closest('.input-group').addClass('input-has-value');
		} else {
			this.closest('.input-group').removeClass('input-has-value');
		}
	};
	
	$.fn.setInputHasFocus = function(focus){
		if(focus){
			this.closest('.input-group').addClass('input-has-focus');
		} else {
			this.closest('.input-group').removeClass('input-has-focus');
		}
	};
	
	$.fn.eachHasValue = function() {
		$(".floating-label input[type=text]:not('.ui-helper-hidden-accessible > input'), .floating-label input[type=password], .floating-label textarea, .floating-label select").each(function(){
			$(this).setInputHasValue();
			if($(this).hasClass('ui-state-error')){
				$(this).closest('.input-group').addClass('input-has-error');
			};
		});
		
		/* Evento onselect do Calendar não estava sendo disparado */
		$('.hasDatepicker').each(function(){
			var datepicker = this;
			var widgetName = "widget_" + this.id.replace(/:/g, "_").replace("_input", "");
			var previousDateSelectFunction = function() {};
			if (typeof PF(widgetName).cfg.behaviors === "undefined") {
				PF(widgetName).cfg.behaviors = [];
			}
			else if (typeof PF(widgetName).cfg.behaviors.dateSelect !== "undefined") {
				previousDateSelectFunction = PF(widgetName).cfg.behaviors.dateSelect;
			}
			PF(widgetName).cfg.behaviors['dateSelect'] = function() {
				$(datepicker).setInputHasValue();
				previousDateSelectFunction();
			};
		});
	}
	
	$(this).eachHasValue();
	
	$('body').on("focus", 'input[type=text], input[type=password], textarea, select', function(){
		$(this).setInputHasFocus(true);
	});
	
	$('body').on("blur", 'input[type=text], input[type=password], textarea, select', function(){
		$(this).setInputHasFocus(false);
	});
	
	$('body').on("change", 'input[type=text], input[type=password], textarea, select', function(){
		$(this).setInputHasValue();
	});
	
	$('body').on("input", 'input[type=text], input[type=password], textarea, select', function(){
		$(this).setInputHasValue();
	});
	
	/* Correção para o autofill do Chrome */
	setTimeout(function(){
		$('input:-webkit-autofill').closest('.input-group').addClass('input-has-value');
	}, 150);	
		
});

