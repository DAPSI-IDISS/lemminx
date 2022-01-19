/**
 *  Copyright (c) 2018 Angelo ZERR
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lemminx.extensions.idiss.participants.diagnostics;



import java.io.*;
import java.net.URISyntaxException;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import com.google.gson.Gson;
import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.commons.TextDocument;
import org.eclipse.lemminx.dom.*;
import org.eclipse.lemminx.utils.XMLPositionUtility;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import java.util.logging.Logger;

import org.apache.xerces.impl.XMLErrorReporter;
import org.apache.xerces.impl.xs.XMLSchemaLoader;
import org.apache.xerces.xni.parser.XMLEntityResolver;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.extensions.xerces.AbstractLSPErrorReporter;
import org.w3c.dom.*;

import static org.eclipse.lemminx.utils.IOUtils.convertStreamToString;

/**
 * IDISS validator utilities class.
 *
 */
public class IDISSValidator {

	private static final Logger LOGGER = Logger.getLogger(IDISSValidator.class.getName());

	private static boolean canCustomizeReporter = true;
  private static Map<String, Map> syntaxBindingReferences = null;
  private static final String SYNTAX_BINDING_REFERENCE = "/references/syntax-binding.syb";

  private static void initializeSyntaxBindingReferences(){
    syntaxBindingReferences = initializeSyntaxBindingReferences(getDOMDocument(SYNTAX_BINDING_REFERENCE));
  }
  private Range getRange(DOMElement node) {
    return XMLPositionUtility.createRange(((DOMElement) node).getStartTagCloseOffset() + 1,
      ((DOMElement) node).getEndTagOpenOffset(), node.getOwnerDocument());
  }

  public static void doDiagnostics(DOMDocument document, List <Diagnostic> diagnostics) {
    doDiagnostics(document, null, diagnostics, false, null);
  }

  public static void doDiagnostics(DOMDocument document, XMLEntityResolver entityResolver,
			List<Diagnostic> diagnostics, boolean isRelatedInformation, CancelChecker monitor) {
      if(syntaxBindingReferences == null){
        initializeSyntaxBindingReferences();
      }
      validateSyntaxBinding(document, diagnostics, syntaxBindingReferences);
	}

	/**
	 * Create the XML Schema loader to use to validate the XML Schema.
	 *
	 * @param reporter the lsp reporter.
	 * @return the XML Schema loader to use to validate the XML Schema.
	 * @throws NoSuchFieldException
	 * @throws IllegalAccessException
	 */
	private static XMLSchemaLoader createSchemaLoader(XMLErrorReporter reporter) {
		XMLSchemaLoader schemaLoader = new XMLSchemaLoader();

		// To validate XML syntax for XML Schema, we need to use the Xerces Reporter
		// (XMLErrorReporter)
		// (and not the Xerces XML ErrorHandler because we need the arguments array to
		// retrieve the attribut e name, element name, etc)

		// Xerces IDISS validator can work with Xerces reporter for IDISS error but not for
		// XML syntax (only XMLErrorHandler is allowed).
		// To fix this problem, we set the Xerces reporter with Java Reflection.
		if (canCustomizeReporter) {
			canCustomizeReporter = AbstractLSPErrorReporter.initializeReporter(schemaLoader, reporter);
		}
		return schemaLoader;
	}

  /**
   * @param  xmlPath path to the xml resource
   * @return  A {@link org.eclipse.lemminx.dom.DOMDocument} object;
   *
   * @see Class#getResourceAsStream(String)
  */
  public static DOMDocument getDOMDocument(String xmlPath){
    InputStream in = IDISSValidator.class.getResourceAsStream(xmlPath);
    String content = convertStreamToString(in);
    String xmlURI = null;
    try {
      xmlURI = IDISSValidator.class.getResource(xmlPath).toURI().toString();
    } catch (URISyntaxException e) {
      e.printStackTrace();
    }
    TextDocument textDocument = new TextDocument(content, xmlURI);
    //long start = System.currentTimeMillis();
    return DOMParser.getInstance().parse(textDocument, null);
    //System.err.println("Parsed 'syntax-binding.syb' with XMLParser in " + (System.currentTimeMillis() - start) + " ms.");
  }

  private static Diagnostic toDiagnostic(@Nonnull DOMNode node, @Nonnull String message, @Nonnull DiagnosticSeverity severity) {
    Range range = null;
    try{
      TextDocument textDocument = node.getOwnerDocument().getTextDocument();
      // diagnostic.setRange(XMLPositionUtility.createRange(node.getStart(), node.getEnd(), node.getOwnerDocument()));
      /*
      node.getStart();
      diagnostic.setRange(new Range(new Position(problem.getLineNumber() - 1, problem.getColumnNumber() - 1),
        new Position(problem.getLineNumber() - 1, problem.getColumnNumber())));
      */
      range = new Range(textDocument.positionAt(node.getStart()), textDocument.positionAt(node.getEnd()));
    } catch (BadLocationException e) {
      e.printStackTrace();
    }
    return toDiagnostic(node, message, severity, range);
  }

  private static Diagnostic toDiagnostic(@Nonnull DOMNode node, @Nonnull String message, @Nonnull DiagnosticSeverity severity, @Nonnull Range range) {
    Diagnostic diagnostic = new Diagnostic();
    diagnostic.setMessage(message);
    diagnostic.setSeverity(severity);
    diagnostic.setRange(range);
    return diagnostic;
  }

  private static Map initializeSyntaxBindingReferences(DOMDocument document) {
    Map<String, Map> refCandidate = new HashMap<String, Map>();
    validateSyntaxBinding(document,null, refCandidate);
    return refCandidate;
  }

  private static<K, V> Map<K, V> clone(Map<K, V> original)
  {
    Gson gson = new Gson();
    String jsonString = gson.toJson(original);

    return gson.fromJson(jsonString, Map.class);
  }

  /**
   * @param syntaxBindingReference if the reference is empty the reference will be filled from the results
   * */
  public static void validateSyntaxBinding(DOMDocument document, List<Diagnostic> diagnostics, Map<String, Map> syntaxBindingReference ) {
    Boolean fillReference = Boolean.FALSE;
    Map<String, Map> syntaxBindingReferenceCopy = null;
    if (syntaxBindingReference.isEmpty()) {
      fillReference = Boolean.TRUE;
    } else {
      syntaxBindingReferenceCopy = clone(syntaxBindingReference);
    }

    DOMNode documentElement = document.getDocumentElement();
    if (documentElement != null) {
      // every semanticNode defined can be gathered by ID
      NodeList allSemanticNodes = documentElement.getChildNodes();
      int length = allSemanticNodes.getLength();
      for (int i = 0; i < allSemanticNodes.getLength(); i++) {
        Map<String, Object> semanticMap = new HashMap<String, Object>(7);
        Node semanticNode = allSemanticNodes.item(i);
        if (!(semanticNode instanceof Text)) { // expected are solely semantic children at this level (either attributes or child elements 'xml')
          if (semanticNode instanceof DOMAttr) {
            String localName = semanticNode.getLocalName();
            //2DO: trigger diagnostic: strangely the root element is never taken by
            // likely attributes are never part of the children() result

            String value = ((DOMAttr) semanticNode).getValue();
            String attrNodeValue = ((DOMAttr) semanticNode).getNodeValue();
          } else if (semanticNode instanceof DOMElement) {
            String localName = semanticNode.getLocalName();
            //2DO: trigger diagnostic: if local name is not semantic

            // *** VALIDATE THE ATTRIBUTES OF SEMANTIC
            NamedNodeMap semanticAttrNodes = semanticNode.getAttributes();
            // fetch the ID in the beginning to identify the correct reference
            Attr idAttr = (Attr) semanticAttrNodes.getNamedItem("id");
            String idValue = idAttr.getNodeValue();
            Map<String, Object> semanticRef = null;
            if (fillReference) { // run on reference document
              syntaxBindingReference.put(idValue, semanticMap);
            } else { // run on test document
              semanticRef = syntaxBindingReferenceCopy.get(idValue);
            }
            int attCount = semanticAttrNodes.getLength();
            for (int a = 0; a < attCount; a++) {
              Attr attr = (Attr) semanticAttrNodes.item(a);
              String attrName = attr.getNodeName();
              if (fillReference && attrName.equals("id")) { // this ID was added earlier already
                continue;
              }
              String attrValue = attr.getNodeValue();
              if (fillReference) { // run on reference document
                semanticMap.put(attrName, attrValue);
              } else {
                // validate attributes of semantic element
                if (semanticRef.containsKey(attrName)) {
                  // does it have the correct name
                  String refValue = (String) semanticRef.get(attrName);
                  if (!refValue.equals(attrValue)) {
                    diagnostics.add(toDiagnostic((DOMAttr) attr, "Attribute value must be '" + refValue + "'!", DiagnosticSeverity.Error));
                  }
                } else if(!(attrName.equals("id"))) { // give error message on semanticNode that this attribute should not exist
                  diagnostics.add(toDiagnostic((DOMAttr) attr, "Attribute name '" + attrValue + "' is unknown!", DiagnosticSeverity.Error));
                }
              }
            }
            // *** VALIDATE THE SUB ELEMENTS OF SEMANTIC
            NodeList semanticChildren = semanticNode.getChildNodes();
            int xmlCount = semanticChildren.getLength();
            if (xmlCount > 0) {
              Collection<String> xmlPaths = new ArrayDeque<String>(xmlCount);
              for (int x = 0; x < xmlCount; x++) {
                DOMElement xmlNode = (DOMElement) semanticChildren.item(x);
                NamedNodeMap xmlAttrNodes = xmlNode.getAttributes();
                int numAttrs = xmlAttrNodes.getLength();
                for (int a = 0; a < numAttrs; a++) {
                  Attr attr = (Attr) xmlAttrNodes.item(a);
                  String attrName = attr.getNodeName();
                  String attrValue = attr.getNodeValue();
                  if (attrName.equals("path")) {
                    if (fillReference) { // run on reference document
                      xmlPaths.add(attrValue);
                    } else {
                      // check path
                      if (semanticRef.containsKey("xmlChildren")) {
                        // does it have the correct name
                        xmlPaths = (Collection) semanticRef.get("xmlChildren");
                        if (!xmlPaths.contains(attrValue)) {
                          diagnostics.add(toDiagnostic((DOMAttr) attr, "Xpath value '" + attrValue + "' does not exist!", DiagnosticSeverity.Error));
                        }
                      }
                    }
                  } else {
                    // validate attributes of XML element
                    // 2DO
                  }
                }
                semanticMap.put("xmlChildren", xmlPaths);
              }

            }
          } else {
            //2DO: Diagnostic there should be no Text node on this level!
            continue;
          }
        }
        // System.out.println("SemanticRefs: " + syntaxBindingReference.toString());
      }
    }
  }
}
