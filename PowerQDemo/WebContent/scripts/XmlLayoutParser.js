XmlLayoutParser = function (pattern) {
    this.pattern = pattern;
}

XmlLayoutParser.prototype.startParser = function(xml) {
    var parser = new SAXDriver();
    var eventHandler = new SAXEventHandler(this.pattern);
    parser.setDocumentHandler(eventHandler);
    parser.setLexicalHandler(eventHandler);
    parser.setErrorHandler(eventHandler);
    parser.parse(xml);

    return eventHandler.maxWidthHeight;

}