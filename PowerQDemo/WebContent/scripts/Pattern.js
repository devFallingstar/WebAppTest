var NORMAL_FONT_SIZE = 12;
var SMALL_FONT_SIZE = 10;
var CANVAS_DIV = 'divPattern';
var BLACK_COLOR = '#000000';
var KEYWORD_COLOR = '#CC0033';
var FUNCTION_COLOR = '#0099CC';
var PREDICATE_COLOR = '#996600';
var NODE_BG_COLOR = '#CCCCCC';
var FUNCTION_BG_COLOR = '#FFCC99';
var PREDICATE_BG_COLOR = 'FFCC99';
var LINE_COLOR = '#CCCCCC';
var PATTERN_BORDER_COLOR = '#FF006E';
var PATTERN_BG_COLOR = '#FFFFFF';

Pattern = function (id) {
	
	//init
	this.canvas_Div_ID = CANVAS_DIV + id;
	this.XML = patternXML[id];
	this.canvasDiv = document.getElementById(this.canvas_Div_ID);
    this.canvas = new Raphael(this.canvasDiv, "100%", "100%");    
    this.textRuler = this.canvas.text(-10000, -10000, '').attr({ fill: 'none', stroke: 'none' }); 

    // some constants
    this.fontItalic = 'italic';
    this.fontBold = 'bold';
    this.fontNormal = 'normal';
    //this.boldFontSymbol = "_b_";
    //this.italicFontSymbol = "_i_";

    this.keywordFontSymbol = "_k_";
    this.functionFontSymbol = "_f_";
    this.predicateFontSymbol = "_p_";
    
    this.padding = 30;
    this.typeMargin = 10;
    //this.ratioMarginMax = 0;

}

Pattern.prototype.drawPattern = function () {
    
    var parser = new XmlLayoutParser(this);    
    var maxWidthHeight = parser.startParser(this.XML);

    // update div size according to pattern size
    this.updatePatternSize(maxWidthHeight);
    $("#" + this.canvas_Div_ID).draggable();
}

// draw a box
Pattern.prototype.drawBox = function (x, y, width, height, nodeType, drawBG) {

	if(nodeType == OBJECT_NODE)
	{
		this.drawRect(x, y, width, height, drawBG);
	}
	else if(nodeType == RELATIONSHIP_NODE)
	{
		this.drawDiam(x, y, width, height, drawBG);
	}
	else if(nodeType == MIXED_NODE)
	{
		this.drawHexa(x, y, width, height, drawBG);
	}
	

    //x += this.padding;
    //y += this.padding;
    //var rectangle = this.canvas.rect(x, y, width, height);
    //rectangle.attr({ fill: NODE_BG_COLOR, 'stroke-width': 0});//, stroke: '#999999', 'stroke-width': 2 });
}

Pattern.prototype.updatePatternSize = function (maxWidthHeight) {
    var newWidth = maxWidthHeight[0] + 2 * this.padding;
    var newHeight = maxWidthHeight[1] + 2 * this.padding;
    this.canvasDiv.style.width = newWidth + "px";
    this.canvasDiv.style.height = newHeight + "px";
}

// draw a rectangle
Pattern.prototype.drawRect = function (x, y, width, height, drawBG) {
	x += this.padding;
    y += this.padding;
    var rectangle = this.canvas.rect(x, y, width, height);
    rectangle.attr({ fill: drawBG,  'stroke-width': 0});
}


// draw a diamond
Pattern.prototype.drawDiam = function (x, y, width, height, drawBG) {
	x += this.padding;
    y += this.padding;
    
    var p1X, p1Y, p2X, p2Y, p3X, p3Y, p4X, p4Y;

    var ratioMargin = (width*height)/(4*this.typeMargin);
    
    if(width>height)
    {
    	p1X = x + width/2;
    	p1Y = y - this.typeMargin;
    	p2X = x + width + ratioMargin;
    	p2Y = y + height/2;
    	p3X = x + width/2;
    	p3Y = y + height + this.typeMargin;
    	p4X = x - ratioMargin;
    	p4Y = y + height/2;
    }
    else
    {
    	p1X = x = width/2;
    	p1Y = y - ratioMargin;
    	p2X = x + width + this.typeMargin;
    	p2Y = y + height/2;
    	p3X = x + width/2;
    	p3Y = y + height + ratioMargin;
    	p4X = x - this.typeMargin;
    	p4Y = y + height/2;

    }
       
    var diamond = this.canvas.path("M" + p1X + "," + p1Y + "L" + p2X + "," + p2Y + "L" + p3X + "," + p3Y + "L" + p4X + "," + p4Y + "z");
    diamond.attr({ fill: drawBG,  'stroke-width': 0});
}


// draw a hexagon
Pattern.prototype.drawHexa = function (x, y, width, height, drawBG) {
	x += this.padding;
    y += this.padding;
   
    var hexagon = this.canvas.path("M"+ x + "," + y + "h" + width + "l" + this.typeMargin + "," + (height)/2 + "l-" + this.typeMargin + "," + (height/2) + "h-" + width + "l-" + this.typeMargin + ",-" + (height/2) + "z");
    hexagon.attr({ fill: drawBG, 'stroke-width': 0});
}


// draw text
Pattern.prototype.drawText = function (x, y, content, fontSize) {
    x += this.padding;
    y += this.padding;
    var text = this.canvas.text(x, y, content);
    text.attr({ 'font-size': fontSize, "font": "Arial" }).toFront();
}

// draw line
Pattern.prototype.drawLine = function (startX, startY, endX, endY) {
    startX += this.padding;
    startY += this.padding;
    endX += this.padding;
    endY += this.padding;
    var line = this.canvas.path("M " + startX + " " + startY + " l " + (endX - startX) + " " + (endY - startY) + " z");
    line.attr({ stroke: LINE_COLOR }).toBack();
}

Pattern.prototype.measureSize = function (text, fontSize) {
    var size = new Array();

    var displayableText = text.replace("_k_", "").replace("_f_", "").replace("_p_", "");
            
    size[0] = this.getTextWidth(displayableText, fontSize);

    size[1] = this.getTextHeight(displayableText, fontSize);
    
    return size;
}

Pattern.prototype.getTextWidth = function (text, fontSize)
{
    this.textRuler.attr({ text: text, 'font-size': fontSize, "font-weight": this.fontBold, "font": "Arial" });
    var bbox = this.textRuler.getBBox();
    return bbox.width;
}



Pattern.prototype.getTextHeight = function (text, fontSize) {
    this.textRuler.attr({ text: text, 'font-size': fontSize, "font-weight": this.fontBold, "font": "Arial" });
    var bbox = this.textRuler.getBBox();
    return bbox.height;
}

Pattern.prototype.drawTextWithFormat = function (x, y, title, text) {
    x += this.padding;
    y += this.padding;

    var displayableTitle = title.replace("_k_", "").replace("_f_", "").replace("_p_", "");
    var sizeTitle = this.measureSize(displayableTitle, NORMAL_FONT_SIZE);
    var sizeText;

    if(text != null)
    {
        var displayableText = text.replace("_k_", "").replace("_f_", "").replace("_p_", "");
        sizeText = this.measureSize(displayableText, SMALL_FONT_SIZE);    
    }
    else
    {
        sizeText = new Array();
        sizeText[0] = 0;
        sizeText[1] = 1;
    }  

    var finalWidth = (sizeTitle[0] > sizeText[0]) ? sizeTitle[0] : sizeText[0];
    var finalHeight = sizeTitle[1] + sizeText[1];

    var letterHeight = this.getTextHeight("randomText", NORMAL_FONT_SIZE);
    var spaceWidth = this.getTextWidth("i", NORMAL_FONT_SIZE);

    var xPos = x - sizeTitle[0] / 2;;
    var yPos = y - finalHeight / 2 + letterHeight / 2;
    
    var isItalic = this.fontNormal;
    var isBold = this.fontNormal;
    var fontColor = BLACK_COLOR;
    /*if (title.indexOf(this.italicFontSymbol) != -1) {                
        isItalic = this.fontItalic;
        fontColor = FUNCTION_COLOR;
        title = title.replace(/_i_/g, "");
    }
    if (title.indexOf(this.boldFontSymbol) != -1) {
        isBold = this.fontBold;                
        fontColor = KEYWORD_COLOR;
        title = title.replace(/_b_/g, "");
    }*/
    if(title.indexOf(this.keywordFontSymbol) != -1)
    {
        fontColor = KEYWORD_COLOR;
        isBold = this.fontBold;
        title = title.replace(/_k_/g, "");
    }
            
    var textDrawn = this.canvas.text(xPos, yPos, title);
    textDrawn.attr({ 'text-anchor': 'start', 'font-size': NORMAL_FONT_SIZE, fill: fontColor, "font-weight": isBold, "font-style": isItalic, "font": "Arial" }).toFront();
    yPos = yPos + letterHeight / 2;

    if(text != null)
    {
        letterHeight = this.getTextHeight("randomText", SMALL_FONT_SIZE);
        spaceWidth = this.getTextWidth("i", SMALL_FONT_SIZE);
        yPos = yPos + letterHeight / 2;

        // split the text by \n
        var lines = text.split("\n");
        
        for (var i = 0; i < lines.length; i++) {
            var lineWidth = this.getTextWidth(lines[i].replace("_k_", "").replace("_f_", "").replace("_p_", ""), SMALL_FONT_SIZE);
            xPos = x - lineWidth / 2;
            // for each line, split by " "
            var lineSegments = lines[i].split(" ");
            for (var j = 0; j < lineSegments.length; j++) {
                var tempText = lineSegments[j];
                isItalic = this.fontNormal;
                isBold = this.fontNormal;
                fontColor = BLACK_COLOR;
                /*if (tempText.indexOf(this.italicFontSymbol) != -1) {                
                    isItalic = this.fontItalic;
                    fontColor = FUNCTION_COLOR;
                    tempText = tempText.replace(/_i_/g, "");
                }
                if (tempText.indexOf(this.boldFontSymbol) != -1) {
                    isBold = this.fontBold;                
                    fontColor = KEYWORD_COLOR;
                    tempText = tempText.replace(/_b_/g, "");
                }*/
                if(tempText.indexOf(this.keywordFontSymbol) != -1)
                {
                    isBold = this.fontBold;
                    fontColor = KEYWORD_COLOR;
                    tempText = tempText.replace(/_k_/g, "");
                }
                if(tempText.indexOf(this.functionFontSymbol) != -1)
                {
                    isBold = this.fontBold; 
                    fontColor = FUNCTION_COLOR;
                    tempText = tempText.replace(/_f_/g, "");
                }
                if(tempText.indexOf(this.predicateFontSymbol) != -1)
                {
                    isBold = this.fontBold; 
                    fontColor = PREDICATE_COLOR;
                    tempText = tempText.replace(/_p_/g, "");
                }

                textDrawn = this.canvas.text(xPos, yPos, tempText);
                textDrawn.attr({ 'text-anchor': 'start', 'font-size': SMALL_FONT_SIZE, fill: fontColor, "font-weight": isBold, "font-style": isItalic, "font": "Arial" }).toFront();

                xPos = xPos + textDrawn.getBBox().width + spaceWidth;
            }
            yPos = yPos + letterHeight;
        }
    }    
}

Pattern.prototype.drawText = function (x, y, text, fontSize) {
}
// draw a non-filled box
Pattern.prototype.drawNonfilledBox = function (x, y, width, height) {
    x += this.padding;
    y += this.padding;

    var rectangle = this.canvas.rect(x-this.typeMargin, y-1.5*this.typeMargin, width+2*this.typeMargin, height+3*this.typeMargin);
    rectangle.attr({ stroke: PATTERN_BORDER_COLOR, 'stroke-width': 1, fill: PATTERN_BG_COLOR });
    

    //effect
    rectangle.hover(function () {
        rectangle.attr({ stroke: PATTERN_BORDER_COLOR, "stroke-width": 3 });
        },
        function () {
            rectangle.attr({ stroke: PATTERN_BORDER_COLOR, "stroke-width": 1 });
        });
    
    rectangle.node.onmouseover = function () {
        this.style.cursor = 'pointer';
    }
   
    rectangle.toBack();
        
}