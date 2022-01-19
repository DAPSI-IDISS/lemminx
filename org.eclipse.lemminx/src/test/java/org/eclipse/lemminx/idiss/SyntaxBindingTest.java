/**
 *  Copyright (c) 2018 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 */
package org.eclipse.lemminx.idiss;

import static org.eclipse.lemminx.utils.IOUtils.convertStreamToString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.util.*;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.eclipse.lemminx.commons.TextDocument;
import org.eclipse.lemminx.dom.*;
import org.eclipse.lemminx.dom.parser.Scanner;
import org.eclipse.lemminx.dom.parser.TokenType;
import org.eclipse.lemminx.dom.parser.XMLScanner;
import org.eclipse.lemminx.extensions.idiss.participants.diagnostics.IDISSValidator;
import org.eclipse.lsp4j.Diagnostic;
import org.junit.jupiter.api.Test;
import org.w3c.dom.*;

public class SyntaxBindingTest {

/*
	@Test
	public void testLargeFileWithScanner() {
		InputStream in = SyntaxBindingTest.class.getResourceAsStream("/referecnes/syntax-binding.syb");
		String text = convertStreamToString(in);
		long start = System.currentTimeMillis();
		Scanner scanner = XMLScanner.createScanner(text);
		TokenType token = scanner.scan();
		while (token != TokenType.EOS) {
			token = scanner.scan();
		}
		System.err
				.println("Parsed 'largeFile.xml' with XMLScanner in " + (System.currentTimeMillis() - start) + " ms.");
	}

	@Test
	public void testBigLargeFileWithScanner() {
		InputStream in = SyntaxBindingTest.class.getResourceAsStream("/xml/nasa.xml");
		String text = convertStreamToString(in);
		long start = System.currentTimeMillis();
		Scanner scanner = XMLScanner.createScanner(text);
		TokenType token = scanner.scan();
		while (token != TokenType.EOS) {
			token = scanner.scan();
		}
		System.err.println("Parsed 'nasa.xml' with XMLScanner in " + (System.currentTimeMillis() - start) + " ms.");
	}

	@Test
	public void testBigLargeFileWithDocument() {
		InputStream in = SyntaxBindingTest.class.getResourceAsStream("/xml/nasa.xml");
		String text = convertStreamToString(in);
		TextDocument document = new TextDocument(text, "nasa.xml");
		long start = System.currentTimeMillis();
		DOMParser.getInstance().parse(document, null);
		System.err.println("Parsed 'nasa.xml' with XMLParser in " + (System.currentTimeMillis() - start) + " ms.");
	}

	@Test
	public void findOneElementWithW3CAndXPath() throws XPathExpressionException {
		DOMDocument document = DOMParser.getInstance().parse("<a><b><c>XXXX</c></b></a>", "test", null);

		// Get "c" element by w3c DOM model
		DOMNode a = document.getDocumentElement();
		assertNotNull(a);
		assertEquals("a", a.getNodeName());
		assertTrue(a.isElement());

		DOMNode b = a.getFirstChild();
		assertNotNull(b);
		assertEquals("b", b.getNodeName());
		assertTrue(b.isElement());

		DOMNode c = b.getFirstChild();
		assertNotNull(c);
		assertEquals("c", c.getNodeName());
		assertTrue(c.isElement());

		// As XMLDocument implement w3c DOM model, we can use XPath.
		// Get "c" element by XPath
		XPath xPath = XPathFactory.newInstance().newXPath();
		Object result = xPath.evaluate("/a/b/c", document, XPathConstants.NODE);
		assertNotNull(result);
		assertTrue(result instanceof DOMElement);
		DOMElement elt = (DOMElement) result;
		assertEquals("c", elt.getNodeName());
		assertEquals(c, elt);
		assertTrue(c.isElement());
	}

	@Test
	public void findTextWithXPath() throws XPathExpressionException {
		DOMDocument document = DOMParser.getInstance().parse("<a><b><c>XXXX</c></b></a>", "test", null);

		XPath xPath = XPathFactory.newInstance().newXPath();
		Object result = xPath.evaluate("/a/b/c/text()", document, XPathConstants.NODE);
		assertNotNull(result);
		assertTrue(result instanceof DOMText);
		DOMText text = (DOMText) result;
		assertEquals("XXXX", text.getData());

		result = xPath.evaluate("/a/b/c/text()", document, XPathConstants.STRING);
		assertNotNull(result);
		assertEquals("XXXX", result.toString());
	}
*/

  @Test
  public void validationTest() {
    // we start with an invalid test file and the diagnostics parameter would be filled
    InputStream in = SyntaxBindingTest.class.getResourceAsStream("/references/syntax-binding.syb");
    String content = convertStreamToString(in);
    TextDocument textDocument = new TextDocument(content, "syntax-binding.syb");
    long start = System.currentTimeMillis();
    DOMDocument document = DOMParser.getInstance().parse(textDocument, null);

    List < Diagnostic > diagnostics = new ArrayList<Diagnostic>();

    IDISSValidator.doDiagnostics(document, diagnostics);
  }

	@Test
	public void siblingTests() throws XPathExpressionException {
    InputStream in = SyntaxBindingTest.class.getResourceAsStream("/references/syntax-binding.syb");
		String content = convertStreamToString(in);
		TextDocument textDocument = new TextDocument(content, "syntax-binding.syb");
		long start = System.currentTimeMillis();
    DOMDocument document = DOMParser.getInstance().parse(textDocument, null);
		System.err.println("Parsed 'syntax-binding.syb' with XMLParser in " + (System.currentTimeMillis() - start) + " ms.");
		DOMNode documentElement = document.getDocumentElement();
      String docName = documentElement.getNodeName();
    assertNotNull(documentElement);
    DOMNode s1 = documentElement.getFirstChild();
      String s1Name = s1.getNodeName();
		assertNotNull(s1);
    DOMNode s2 = s1.getNextSibling();
    assertTrue(s2.getNodeName().equals("semanticb"));
      String s2Name = s2.getNodeName();
		DOMNode x1 = s2.getFirstChild();
		assertNotNull(x1);
    assertTrue(x1.getNodeName().equals("xmlb"));
      String xName = x1.getNodeName();
    DOMNode t = x1.getFirstChild();
    assertNotNull(t);
    assertTrue(t.isText());
    DOMText text = (DOMText) t;
		assertEquals("@format = ”102”", text.getData());


    // extract from the dom
    // 1) every semantic with attributes - list with XML "Paths" as Strings
    // 2) every XML with attributes the path (to be separated by /)

    if(documentElement != null){
      // every semantic defined can be gathered by ID
      Map<String, Map> semanticRefs = new HashMap<String, Map>(5);
      Node semantic = documentElement.getFirstChild();
      String semanticName = semantic.getNodeName();
      NodeList allSemanticNodes = documentElement.getChildNodes();
      Node firstX = semantic.getFirstChild();
      String firstXName = firstX.getNodeName();
      int length = allSemanticNodes.getLength();
      for(int i = 0; i < allSemanticNodes.getLength();i++){
        Map<String, Object> semanticRef = new HashMap<String, Object>(7);
        Node n = allSemanticNodes.item(i);
        if(n instanceof Text)
          continue;
        else {
          String localName = n.getLocalName();
          String name = n.getNodeName();
          if(n instanceof DOMAttr){
            String value = ((DOMAttr) n).getValue();
            String attrNodeValue = ((DOMAttr) n).getNodeValue();
          }else if(n instanceof DOMElement){
            NodeList xmlNodes = n.getChildNodes();
            int xmlCount = xmlNodes.getLength();
            if(xmlCount > 0){
              Collection<String> xmlPaths = new ArrayDeque<String>(xmlCount);
              for (int x = 0; x < xmlCount; x++){
                DOMElement xmlNode = (DOMElement) xmlNodes.item(x);
                NamedNodeMap xmlAttrNodes = xmlNode.getAttributes();
                int numAttrs = xmlAttrNodes.getLength();
                for (int a = 0; a < numAttrs; a++) {
                  Attr attr = (Attr) xmlAttrNodes.item(a);
                  String attrName = attr.getNodeName();
                  String attrValue = attr.getNodeValue();
                  if (attrName.equals("path")) {
                    xmlPaths.add(attrValue);
                  }
                }
              }
              semanticRef.put("xmlChildren", xmlPaths);
            }
            NamedNodeMap semanticAttrNodes = n.getAttributes();
            int attCount = semanticAttrNodes.getLength();
            for (int a = 0; a < attCount; a++){
              Attr attr = (Attr) semanticAttrNodes.item(a);
              String attrName = attr.getNodeName();
              String attrValue = attr.getNodeValue();
              semanticRef.put(attrName, attrValue);
              if(attrName.equals("id")){
                semanticRefs.put(attrValue, semanticRef);
              }
            }
          }
        }
      }
      System.out.println("SemanticRefs: " + semanticRefs.toString());
    }
	}
/*
	@Test
	public void findElementListWithXPath() throws XPathExpressionException {
		DOMDocument document = DOMParser.getInstance().parse("<a><b><c>XXXX</c><c>YYYY</c></b></a>", "test", null);

		XPath xPath = XPathFactory.newInstance().newXPath();
		Object result = xPath.evaluate("/a/b//c", document, XPathConstants.NODESET);
		assertNotNull(result);
		assertTrue(result instanceof NodeList);
		NodeList elts = (NodeList) result;
		assertEquals(2, elts.getLength());

	}

	@Test
	public void testDOMAsDTD() {
		String content = "<!ELEMENT";

		// .xml file extension
		DOMDocument xml = DOMParser.getInstance().parse(content, "test.xml", null);
		assertFalse(xml.isDTD());
		DOMNode element = xml.getChild(0);
		assertTrue(element.isElement());

		// .unknown file extension
		DOMDocument unknown = DOMParser.getInstance().parse(content, "test.unknown", null);
		assertFalse(unknown.isDTD());
		DOMNode unknownElement = unknown.getChild(0);
		assertTrue(unknownElement.isElement());

		// .dtd file extension
		DOMDocument dtd = DOMParser.getInstance().parse(content, "test.dtd", null);
		assertTrue(dtd.isDTD());
		DOMNode dtdDocType = dtd.getChild(0);
		assertTrue(dtdDocType.isDoctype());
		DOMNode dtdElementDecl = dtdDocType.getChild(0);
		assertTrue(dtdElementDecl.isDTDElementDecl());

		// .ent file extension
		DOMDocument ent = DOMParser.getInstance().parse(content, "test.ent", null);
		assertTrue(ent.isDTD());
		DOMNode entDocType = ent.getChild(0);
		assertTrue(entDocType.isDoctype());
		DOMNode entElementDecl = entDocType.getChild(0);
		assertTrue(entElementDecl.isDTDElementDecl());

		// .mod file extension
		DOMDocument mod = DOMParser.getInstance().parse(content, "test.mod", null);
		assertTrue(mod.isDTD());
		DOMNode modDocType = mod.getChild(0);
		assertTrue(modDocType.isDoctype());
		DOMNode modElemmodDecl = modDocType.getChild(0);
		assertTrue(modElemmodDecl.isDTDElementDecl());
	}

	@Test
	public void defaultNamespaceURI() {
		String xml = "<beans xmlns=\"http://www.springframework.org/schema/beans\"\r\n"
				+ "       xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n"
				+ "       xmlns:camel=\"http://camel.apache.org/schema/spring\"\r\n"
				+ "       xsi:schemaLocation=\"\r\n"
				+ "       http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd\r\n"
				+ "       http://camel.apache.org/schema/spring http://camel.apache.org/schema/spring/camel-spring.xsd\r\n"
				+ "    \">" + "<bean /><camel:camelContext>";
		DOMDocument dom = DOMParser.getInstance().parse(xml, "test.xml", null);

		DOMElement bean = (DOMElement) dom.getDocumentElement().getFirstChild();
		assertNull(bean.getPrefix());
		assertEquals("http://www.springframework.org/schema/beans", bean.getNamespaceURI());

		DOMElement camel = (DOMElement) bean.getNextSibling();
		assertEquals("camel", camel.getPrefix());
		assertEquals("http://camel.apache.org/schema/spring", camel.getNamespaceURI());

	}

	@Test
	public void noDefaultNamespaceURI() {
		String xml = "<b:beans xmlns:b=\"http://www.springframework.org/schema/beans\"\r\n"
				+ "       xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n"
				+ "       xmlns:camel=\"http://camel.apache.org/schema/spring\"\r\n"
				+ "       xsi:schemaLocation=\"\r\n"
				+ "       http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd\r\n"
				+ "       http://camel.apache.org/schema/spring http://camel.apache.org/schema/spring/camel-spring.xsd\r\n"
				+ "    \">" + "<bean /><camel:camelContext>";
		DOMDocument dom = DOMParser.getInstance().parse(xml, "test.xml", null);

		DOMElement bean = (DOMElement) dom.getDocumentElement().getFirstChild();
		assertNull(bean.getPrefix());
		assertNull(bean.getNamespaceURI());

		DOMElement camel = (DOMElement) bean.getNextSibling();
		assertEquals("camel", camel.getPrefix());
		assertEquals("http://camel.apache.org/schema/spring", camel.getNamespaceURI());

	}
  */
}
