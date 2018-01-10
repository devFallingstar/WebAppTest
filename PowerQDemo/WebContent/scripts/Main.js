var pattern;

$(document).ready(function () {

	for(var i = 0; i < patternXML.length; i++)
	{
		pattern = new Pattern(i);
    	pattern.drawPattern();
	}
});