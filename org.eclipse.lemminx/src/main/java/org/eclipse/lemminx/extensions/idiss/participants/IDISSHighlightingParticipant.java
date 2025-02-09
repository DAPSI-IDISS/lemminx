/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.extensions.idiss.participants;

import java.util.List;

import org.eclipse.lemminx.dom.DOMAttr;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMNode;
import org.eclipse.lemminx.extensions.idiss.utils.IDISSUtils;
import org.eclipse.lemminx.extensions.idiss.utils.IDISSUtils.BindingType;
import org.eclipse.lemminx.services.extensions.IHighlightingParticipant;
import org.eclipse.lemminx.utils.DOMUtils;
import org.eclipse.lemminx.utils.XMLPositionUtility;
import org.eclipse.lsp4j.DocumentHighlight;
import org.eclipse.lsp4j.DocumentHighlightKind;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

/**
 * IDISS highlight participant
 * 
 * @author Angelo ZERR
 *
 */
public class IDISSHighlightingParticipant implements IHighlightingParticipant {

	@Override
	public void findDocumentHighlights(DOMNode node, Position position, int offset, List<DocumentHighlight> highlights,
			CancelChecker cancelChecker) {
		// IDISS highlight applicable only for IDISS file
		DOMDocument document = node.getOwnerDocument();
		if (!DOMUtils.isIDISS(document)) {
			return;
		}
		// Highlight works only when attribute is selected (origin or target attribute)
		DOMAttr attr = node.findAttrAt(offset);
		if (attr == null || attr.getNodeAttrValue() == null) {
			return;
		}
		// Try to get the binding from the origin attribute
		BindingType bindingType = IDISSUtils.getBindingType(attr);
		if (bindingType != BindingType.NONE) {
			// It's an origin attribute, highlight the origin and target attribute
			DOMAttr originAttr = attr;
			highlights
					.add(new DocumentHighlight(XMLPositionUtility.createRange(originAttr.getNodeAttrValue().getStart(),
							originAttr.getNodeAttrValue().getEnd(), document), DocumentHighlightKind.Read));
			// Search target attributes only in the XML Schema and not in xs:include since
			// LSP highlighting works only for a given file
			boolean searchInExternalSchema = false;
			IDISSUtils.searchXSTargetAttributes(originAttr, bindingType, true, searchInExternalSchema,
					(targetNamespacePrefix, targetAttr) -> {
						highlights.add(new DocumentHighlight(
								XMLPositionUtility.createRange(targetAttr.getNodeAttrValue().getStart(),
										targetAttr.getNodeAttrValue().getEnd(), targetAttr.getOwnerDocument()),
								DocumentHighlightKind.Write));
					});

		} else if (IDISSUtils.isXSTargetElement(attr.getOwnerElement())) {
			// It's an target attribute, highlight all origin attributes linked to this
			// target attribute
			DOMAttr targetAttr = attr;
			highlights.add(new DocumentHighlight(
					XMLPositionUtility.createRange(targetAttr.getNodeAttrValue().getStart(),
							targetAttr.getNodeAttrValue().getEnd(), targetAttr.getOwnerDocument()),
					DocumentHighlightKind.Write));
			IDISSUtils.searchXSOriginAttributes(targetAttr,
					(origin, target) -> highlights.add(new DocumentHighlight(
							XMLPositionUtility.createRange(origin.getNodeAttrValue().getStart(),
									origin.getNodeAttrValue().getEnd(), origin.getOwnerDocument()),
							DocumentHighlightKind.Read)),
					cancelChecker);
		}
	}

}
