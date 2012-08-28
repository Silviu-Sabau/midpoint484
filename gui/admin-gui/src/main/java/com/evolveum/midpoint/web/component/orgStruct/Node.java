/*
 * Copyright (c) 2012 Evolveum
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://www.opensource.org/licenses/cddl1 or
 * CDDLv1.0.txt file in the source code distribution.
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 *
 * Portions Copyrighted 2012 [name of copyright owner]
 */
package com.evolveum.midpoint.web.component.orgStruct;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;

import wickettree.AbstractTree;
import wickettree.ITreeProvider;
import wickettree.AbstractTree.State;

/**
 * @author mserbak
 */
public class Node<T> extends StyledLinkLabel<T> {

	private static final long serialVersionUID = 1L;

	private AbstractTree<T> tree;

	public Node(String id, AbstractTree<T> tree, IModel<T> model) {
		super(id, tree, model);

		this.tree = tree;
	}

	/**
	 * Clickable if node can be expanded/collapsed, i.e. has children.
	 * 
	 * @see ITreeProvider#hasChildren(Object)
	 */
	@Override
	protected boolean isClickable() {
		T t = getModelObject();

		return tree.getProvider().hasChildren(t);
	}

	/**
	 * Toggle the node's {@link State} on click.
	 */
	@Override
	protected void onClick(AjaxRequestTarget target) {
		T t = getModelObject();
		if (tree.getState(t) == State.EXPANDED) {
			tree.collapse(t);
		} else {
			tree.expand(t);
		}
		target.appendJavaScript("initMenuButtons()");
	}

	/**
	 * Delegates to others methods depending wether the given model is a folder,
	 * expanded, collapsed or selected.
	 * 
	 * @see ITreeProvider#hasChildren(Object)
	 * @see AbstractTree#getState(Object)
	 * @see #isSelected()
	 * @see #getOpenStyleClass()
	 * @see #getClosedStyleClass()
	 * @see #getOtherStyleClass(Object)
	 * @see #getSelectedStyleClass()
	 */
	@Override
	protected String getStyleClass() {
		T t = getModelObject();

		String styleClass;
		if (tree.getProvider().hasChildren(t)) {
			if (tree.getState(t) == State.EXPANDED) {
				styleClass = getOpenStyleClass();
			} else {
				styleClass = getClosedStyleClass();
			}
		} else {
			styleClass = getOtherStyleClass(t);
		}

		if (isSelected()) {
			styleClass += " " + getSelectedStyleClass();
		}

		return styleClass;
	}

	/**
	 * Optional attribute which decides if an additional "selected" style class
	 * should be rendered.
	 * 
	 * @return defaults to <code>false</code>
	 */
	protected boolean isSelected() {
		return false;
	}

	/**
	 * Get a style class for anything other than closed or open folders.
	 */
	protected String getOtherStyleClass(T t) {
		return "tree-folder-other";
	}

	protected String getClosedStyleClass() {
		return "tree-folder-closed";
	}

	protected String getOpenStyleClass() {
		return "tree-folder-open";
	}

	/**
	 * Get a style class to render for a selected folder.
	 * 
	 * @see #isSelected()
	 */
	protected String getSelectedStyleClass() {
		return "selected";
	}


	protected String getButtonStyle(T t) {
		String buttonStyleClass;
		
		if (tree.getProvider().hasChildren(t)) {
			buttonStyleClass = "treeButtonMenu orgUnitButton";
		} else {
			buttonStyleClass = "treeButtonMenu userButton";
		}
		return buttonStyleClass;
	}

	@Override
	protected String getButtonStyleClass() {
		T t = getModelObject();
		return getButtonStyle(t);
	}
}