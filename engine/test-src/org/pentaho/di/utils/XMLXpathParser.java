package org.pentaho.di.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XMLXpathParser {

  Document doc;
  XPath xpath;

  public XMLXpathParser( String xml ) throws ParserConfigurationException, SAXException, IOException {
    
    InputStream in = new ByteArrayInputStream( xml.getBytes( "UTF-8" ) );
    DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = domFactory.newDocumentBuilder();
    doc = builder.parse( in );
    xpath = XPathFactory.newInstance().newXPath();
    
    in.close();
  }

  public List<String> queryTextContent( String xpaths ) throws XPathExpressionException {
    List<String> ret = new ArrayList<String>();
    XPathExpression expr = xpath.compile( xpaths );

    Object result = expr.evaluate( doc, XPathConstants.NODESET );
    NodeList nodes = (NodeList) result;
    for ( int i = 0; i < nodes.getLength(); i++ ) {
      ret.add( nodes.item( i ).getTextContent() );
    }
    return ret;
  }
}
