PrimeFaces.widget.Dialog.prototype.applyFocus = function() {
	var firstInput = this.jq
			.find(':not(:submit):not(:button):input:visible:enabled:first');
	if (!firstInput.hasClass('hasDatepicker')) {
		firstInput.focus();
	}
};