/*****************************************************************************
                    SAXEventHandler Object
*****************************************************************************/
var RESULT_ROOT_SYMBOL = "_1_r";  
var OBJECT_NODE_SYMBOL = "_0_o";
var RELATIONSHIP_NODE_SYMBOL = "_0_r";
var MIXED_NODE_SYMBOL = "_0_m";
var OBJECT_NODE = 0;
var RELATIONSHIP_NODE = 1;
var MIXED_NODE = 2;

SAXEventHandler = function (pattern) { 

    this.characterData = "";

    this.vMargine = 20;
    this.hMargine = 80;
    this.oneLineWidth = 100;
    //this.autoFlowWidthByLetter = 10;
    this.xmlTreeDepthMax = 10;

    this.pattern = pattern;
    this.traverseLevel = 0;
    this.attrBuffer = new Array();
    this.leafBuffer = null;

    this.beginBorder = new Array();
    for (var i = 0; i < this.xmlTreeDepthMax; i++) this.beginBorder[i] = 0;

    this.endBorderMax = 0;


    this.rightBorder = new Array();
    for (var i = 0; i < this.xmlTreeDepthMax; i++) this.rightBorder[i] = 0;

    this.buffer4Children = new Array();
    for (var i = 0; i < 10; i++) this.buffer4Children[i] = new Array();


    this.maxWidthHeight = [0,0];
}

SAXEventHandler.prototype.characters = function (data, start, length) {    

    this.characterData += data.substr(start, length);
    this.leafBuffer = data.substr(start, length);
}  // end function characters


SAXEventHandler.prototype.startElement = function (name, atts) {    

    this._handleCharacterData();

    var attrNum = atts.getLength();

    /*if(attrNum > 0)
    {
        var attrStr="";
        for(var i = 0; i < attrNum; i++)
        {
            attrStr += atts.getValue(i).replace(/ and /g, "\n");
            attrStr += "\n";
        }
        var lastSlashN = attrStr.lastIndexOf("\n");
        this.attrBuffer.push(attrStr.substr(0, lastSlashN));
    }*/
    var attrStr;
    if(attrNum > 0)
    {
        attrStr = atts.getValue(0).replace(/ and /g, "\n");
        for(var i = 1; i < attrNum; i++)
        {
            attrStr += "\n";
            attrStr += atts.getValue(i).replace(/ and /g, "\n");
        }
        //attrStr = atts.getValue(0).replace(/ and /g, "\n");
    }
    else
    {
        attrStr = null;
    }
    this.attrBuffer.push(attrStr);
    
    this.traverseLevel++;

}  // end function startElement

SAXEventHandler.prototype.endElement = function (name) {

    this._handleCharacterData();

    //place endElement event handling code below this line

    this.traverseLevel--;

    // see whether it is a leaf node
    var isLeaf = false;
    if (this.leafBuffer != null) {
        isLeaf = true;
        this.leafBuffer = null;
    }

    var textBlockName = name;
    var textBlockAttr = this.attrBuffer.pop();
    /*var attr = this.attrBuffer.pop();

    if(attr != null)
    {
        textBlock = name + "\n" + attr;
    }*/


    var nodeType = -1;
    if(textBlockName.indexOf(OBJECT_NODE_SYMBOL) != -1)
    {
        nodeType = OBJECT_NODE;
        textBlockName = textBlockName.replace(OBJECT_NODE_SYMBOL, "");
    }
    else if(textBlockName.indexOf(RELATIONSHIP_NODE_SYMBOL) != -1)
    {
        nodeType = RELATIONSHIP_NODE;
        textBlockName = textBlockName.replace(RELATIONSHIP_NODE_SYMBOL, "");
    }
    else if(textBlockName.indexOf(MIXED_NODE_SYMBOL) != -1)
    {
        nodeType = MIXED_NODE;
        textBlockName = textBlockName.replace(MIXED_NODE_SYMBOL, "");
    }

    var isRoot = false;
    if(textBlockName.indexOf(RESULT_ROOT_SYMBOL) != -1)
    {
        isRoot = true;
        textBlockName = textBlockName.replace(RESULT_ROOT_SYMBOL, "");
    }


    var size = this.pattern.measureSize(textBlockName, NORMAL_FONT_SIZE);

    var width = size[0] ;
    var height = size[1];

    if(textBlockAttr != null)
    {
        var attrSize = this.pattern.measureSize(textBlockAttr, SMALL_FONT_SIZE);
        if(width < attrSize[0])
        {
            width = attrSize[0];
        }
        height += attrSize[1];
    }


    var centerX, centerY;
    var topBorder = this.beginBorder[this.traverseLevel];

    centerX = this.getLeftPosition() + width / 2;
    if(isLeaf)
    {
        centerY = this.endBorderMax + height/2;
        this.endBorderMax += height + this.vMargine;
    }
    else
    {
        centerY = this.getInternalNodeCenterPostition();
    }
    this.beginBorder[this.traverseLevel] = this.endBorderMax;

    var boxX, boxY;
    var textX, textY;
    
    boxX = centerX - size[0] / 2;
    boxY = centerY - height / 2;
    //begin to draw
    this.drawBox(boxX, boxY, size[0], size[1], nodeType, NODE_BG_COLOR);
    this.drawText(centerX, centerY, width, height, textBlockName, textBlockAttr);
    this.drawParentChildLine(centerX, centerY, width, height);

    this.drawBoundingBox(centerX, centerY, width, height, topBorder, isLeaf, isRoot);

    for (var i = this.traverseLevel + 1; i < this.beginBorder.length; i++) {
        if (this.beginBorder[i] < this.beginBorder[this.traverseLevel]) {
            this.beginBorder[i] = this.beginBorder[this.traverseLevel];
        }
    }

}  // end function endElement

SAXEventHandler.prototype.drawParentChildLine = function (centerX, centerY, width, height) {
    var startX = centerX + width / 2;
    var startY = centerY;
    var children = this.buffer4Children[this.traverseLevel + 1];
    for (var i = 0; i < children.length; i = i + 2) {
        var endX = children[i];
        var endY = children[i + 1];
        this.pattern.drawLine(startX, startY, endX, endY);
    }

    this.buffer4Children[this.traverseLevel + 1] = new Array();

    var endX4Parent = centerX - width /2;
    var endY4Parent = centerY;
    this.buffer4Children[this.traverseLevel].push(endX4Parent);
    this.buffer4Children[this.traverseLevel].push(endY4Parent);
}
SAXEventHandler.prototype.drawBox = function (x, y, width, height, nodeType, drawBG) {
    this.pattern.drawBox(x, y, width, height, nodeType, NODE_BG_COLOR);
}
SAXEventHandler.prototype.drawText = function (centerX, centerY, width, height, title, content) {
    this.pattern.drawTextWithFormat(centerX, centerY, title, content);
}

SAXEventHandler.prototype.drawBoundingBox = function (centerX, centerY, width, height, topBorder, isLeaf, visiable) {

    // leaf node will increase the rightBorder
    if (isLeaf) {
        var newRightBorder = centerX + width / 2;
        this.rightBorder[this.traverseLevel] = newRightBorder;
        for (var i = this.traverseLevel - 1; i >= 0; i--) {
            if (this.rightBorder[i] < newRightBorder) {
                this.rightBorder[i] = newRightBorder;
            }
        }
    }

    // calcualte the bounding box
    var boxX1 = centerX - width / 2;
    var boxY1 = topBorder;
    
    
    var boxWidth = this.rightBorder[this.traverseLevel] - boxX1;
    var boxHeight = this.endBorderMax - this.vMargine - boxY1;

    // if the box should be shown
    if (visiable) {
        this.pattern.drawNonfilledBox(boxX1, boxY1, boxWidth, boxHeight);

    }


    // update max width and max height
    var maxWidthSofar = this.rightBorder[this.traverseLevel] - boxX1;
    var maxHeightSofar = this.endBorderMax - this.vMargine - boxY1;
    
    if (maxWidthSofar > this.maxWidthHeight[0]) {
        this.maxWidthHeight[0] = maxWidthSofar;
    }
    if (maxHeightSofar > this.maxWidthHeight[1]) {
        this.maxWidthHeight[1] = maxHeightSofar;
    }
    
}

SAXEventHandler.prototype.getLeftPosition = function () {
    return (this.traverseLevel) * this.hMargine + (this.traverseLevel) * this.oneLineWidth;
}


SAXEventHandler.prototype.getInternalNodeCenterPostition = function () {
    var beginPosition = this.beginBorder[this.traverseLevel];
    var endPosition = this.endBorderMax - this.vMargine;
    var middlePosition = Math.ceil((beginPosition + endPosition) / 2.0);
    return middlePosition;
}

SAXEventHandler.prototype.endDocument = function () {
    /*****************************************************************************
    function:  endDocument

    author: djoham@yahoo.com

    description:
        Fires at the end of the document
    *****************************************************************************/

    this._handleCharacterData();

    //place endDocument event handling code below this line

}  // end function endDocument



SAXEventHandler.prototype.processingInstruction = function (target, data) {
    /*****************************************************************************
    function:  processingInstruction

    author: djoham@yahoo.com

    description:
        Fires when a processing Instruction is found

        In the following processing instruction:
        <?xml version=\"1.0\"?>

        target == xml
        data == version"1.0"
    *****************************************************************************/
    this._handleCharacterData();

    //place processingInstruction event handling code below this line


}  // end function processingInstruction


SAXEventHandler.prototype.setDocumentLocator = function (locator) {
    /*****************************************************************************
    function:  setDocumentLocator

    author: djoham@yahoo.com

    description:
        This is the first event ever called by the parser.

        locator is a reference to the actual parser object that is parsing
        the XML text. Normally, you won't need to trap for this error
        or do anything with the locator object, but if you do need to,
        this is how you get a reference to the object
    *****************************************************************************/

    this._handleCharacterData();

    //place setDocumentLocator event handling code below this line


}  // end function setDocumentLocator





SAXEventHandler.prototype.startDocument = function () {
    /*****************************************************************************
    function:  startDocument

    author: djoham@yahoo.com

    description:
        Fires at the start of the document
    *****************************************************************************/

    this._handleCharacterData();

    //place startDocument event handling code below this line


}  // end function startDocument


/*****************************************************************************
                    SAXEventHandler Object Lexical Handlers
*****************************************************************************/


SAXEventHandler.prototype.comment = function (data, start, length) {
    /*****************************************************************************
    function:  comment

    author: djoham@yahoo.com

    description:
        Fires when a comment is found

        data is your full XML string
        start is the beginning of the XML character data being reported to you
        end is the end of the XML character data being reported to you

        the data can be retrieved using the following code:
        var data = data.substr(start, length);
    *****************************************************************************/
    this._handleCharacterData();

    //place comment event handling code below this line


}  // end function comment


SAXEventHandler.prototype.endCDATA = function () {
    /*****************************************************************************
    function:  endCDATA

    author: djoham@yahoo.com

    description:
        Fires at the end of a CDATA element
    *****************************************************************************/
    this._handleCharacterData();

    //place endCDATA event handling code below this line


}  // end function endCDATA


SAXEventHandler.prototype.startCDATA = function () {
    /*****************************************************************************
    function:  startCDATA

    author: djoham@yahoo.com

    description:
        Fires at the start of a CDATA element
    *****************************************************************************/
    this._handleCharacterData();

    //place startCDATA event handling code below this line


}  // end function startCDATA


/*****************************************************************************
                    SAXEventHandler Object Error Interface
*****************************************************************************/


SAXEventHandler.prototype.error = function (exception) {
    /*****************************************************************************
    function:  error

    author: djoham@yahoo.com

    description:
        Fires when an error is found.

        Information about the exception can be found by calling
        exception.getMessage()
        exception.getLineNumber()
        exception.getColumnNumber()
    *****************************************************************************/
    this._handleCharacterData();

    //place error event handling code below this line


}  // end function error


SAXEventHandler.prototype.fatalError = function (exception) {
    /*****************************************************************************
    function:  fatalError

    author: djoham@yahoo.com

    description:
        Fires when a  fatal error is found.

        Information about the exception can be found by calling
        exception.getMessage()
        exception.getLineNumber()
        exception.getColumnNumber()
    *****************************************************************************/
    this._handleCharacterData();

    //place fatalError event handling code below this line


}  // end function fatalError


SAXEventHandler.prototype.warning = function (exception) {
    /*****************************************************************************
    function:  warning

    author: djoham@yahoo.com

    description:
        Fires when a warning is found.

        Information about the exception can be found by calling
        exception.getMessage()
        exception.getLineNumber()
        exception.getColumnNumber()
    *****************************************************************************/
    this._handleCharacterData();

    //place warning event handling code below this line


}  // end function warning


/*****************************************************************************
                   SAXEventHandler Object Internal Functions
*****************************************************************************/


SAXEventHandler.prototype._fullCharacterDataReceived = function (fullCharacterData) {
    /*****************************************************************************
    function:  _fullCharacterDataReceived

    author: djoham@yahoo.com

    description:
        this function is called when we know we are finished getting
        all of the character data. If you need to be sure you handle
        your text processing when you have all of the character data,
        your code for that handling should go here

        fullCharacterData contains all of the character data for the element
    *****************************************************************************/

    //place character (text) event handling code below this line


}  // end function _fullCharacterDataReceived


SAXEventHandler.prototype._handleCharacterData = function () {
    /*****************************************************************************
    function:  _handleCharacterData

    author: djoham@yahoo.com

    description:
        This internal function is called at the beginning of every event, with the exception
        of the characters event.  It fires the internal event this._fullCharacterDataReceived
        if there is any data in the this.characterData internal variable.
        It then resets the this.characterData variable to blank

        Generally, you will not need to modify this function
    *****************************************************************************/

    // call the function that lets the user know that all of the text data has been received
    // but only if there is data.
    if (this.characterData != "") {
        this._fullCharacterDataReceived(this.characterData);
    }

    //reset the characterData variable
    this.characterData = "";

}  // end function _handleCharacterData

