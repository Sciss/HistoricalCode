/*
 *  Group.java
 *  (JCollider)
 *
 *  Copyright (c) 2004-2015 Hanns Holger Rutz. All rights reserved.
 *
 *  This software is published under the GNU Lesser General Public License v2.1+
 *
 *
 *  For further information, please contact Hanns Holger Rutz at
 *  contact@sciss.de
 */

package de.sciss.jcollider;

import java.io.IOException;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import javax.swing.tree.TreeNode;

import de.sciss.net.OSCMessage;

/**
 *	Mimics SCLang's Group class,
 *	that is, it's a client side
 *	representation of a group in the synthesis graph
 *
 *	@warning	this is a quick direct translation from SClang
 *				which is largely untested. before all methods have been
 *				thoroughly verified, excepted some of them to be wrong
 *				or behave different than expected. what certainly works
 *				is instantiation, new- and free-messages
 *
 *  @author		Hanns Holger Rutz
 *  @version	0.32, 25-Feb-08
 */
public class Group
extends Node
{
	private Node	headNode	= null;
	private Node	tailNode	= null;

	// immediately sends
	public Group( Group target )
	throws IOException
	{
		this( target, kAddToHead );
	}

	// immediately sends
	public Group( Node target, int addAction )
	throws IOException
	{
		super( target.getServer() );
		getServer().sendMsg( newMsg( target, addAction ));
	}
	
	// does not send
	private Group( Server server, int nodeID )
	{
		super( server, nodeID );
	}
	
	public Node getHeadNode()
	{
		return headNode;
	}

	public Node getTailNode()
	{
		return tailNode;
	}
	
	protected void setHeadNode( Node headNode )
	{
		this.headNode	= headNode;
	}

	protected void setTailNode( Node tailNode )
	{
		this.tailNode	= tailNode;
	}

	public OSCMessage newMsg()
	{
		return newMsg( null );
	}

	public OSCMessage newMsg( Group target )
	{
		return newMsg( target, kAddToHead );
	}

	/**
	 *	Note: this sets the group!
	 *
	 *	@throws	ClassCastException	if target is not a group and addAction
	 *								is either kAddToHead or kAddToTail
	 */
	public OSCMessage newMsg( Node target, int addAction )
	{
		if( target == null ) target = getServer().getDefaultGroup();
	
// removed 02-oct-05
//		this.setGroup( addAction == kAddToHead || addAction == kAddToTail ?
//			(Group) target : target.getGroup() );

		return( new OSCMessage( "/g_new", new Object[] {
			new Integer( this.getNodeID() ), new Integer( addAction ), new Integer( target.getNodeID() )}));
	}

	public static Group basicNew( Server server )
	{
		return Group.basicNew( server, server.nextNodeID() );
	}
	
	public static Group basicNew( Server server, int nodeID )
	{
		return new Group( server, nodeID );
	}

	public static Group after( Node aNode )
	throws IOException
	{
		return new Group( aNode, kAddAfter );
	}

	public static Group before( Node aNode )
	throws IOException
	{
		return new Group( aNode, kAddBefore );
	}

	public static Group head( Group aGroup )
	throws IOException
	{
		return new Group( aGroup, kAddToHead );
	}

	public static Group tail( Group aGroup )
	throws IOException
	{
		return new Group( aGroup, kAddToTail );
	}

	public static Group replace( Node nodeToReplace )
	throws IOException
	{
		return new Group( nodeToReplace, kAddReplace );
	}

	// for bundling
	public OSCMessage addToHeadMsg( Group aGroup )
	{
		return newMsg( aGroup, kAddToHead );
	}

	public OSCMessage addToTailMsg( Group aGroup )
	{
		return newMsg( aGroup, kAddToTail );
	}
	
	public OSCMessage addAfterMsg( Node aNode )
	{
		return newMsg( aNode, kAddAfter );
	}

	public OSCMessage addBeforeMsg( Node aNode )
	{
		return newMsg( aNode, kAddBefore );
	}
	
	public OSCMessage addReplaceMsg( Node aNode )
	{
		return newMsg( aNode, kAddReplace );
	}

	// move Nodes to this group
	public void moveNodeToHead( Node aNode )
	throws IOException
	{
// NO
//	setGroup is called by moveNodeToHeadMsg()
//		aNode.setGroup( this );
		getServer().sendMsg( moveNodeToHeadMsg( aNode ));
	}

	public void moveNodeToTail( Node aNode )
	throws IOException
	{
// NO
//	setGroup is called by moveNodeToTailMsg()
//		aNode.setGroup( this );
		getServer().sendMsg( moveNodeToTailMsg( aNode ));
	}

	public OSCMessage moveNodeToHeadMsg( Node aNode )
	{
// removed 02-oct-05
//		aNode.setGroup( this );
		return( new OSCMessage( "/g_head", new Object[] {
			new Integer( this.getNodeID() ), new Integer( aNode.getNodeID() )}));
	}

	public OSCMessage moveNodeToTailMsg( Node aNode )
	{
// removed 02-oct-05
//		aNode.setGroup( this );
		return( new OSCMessage( "/g_tail", new Object[] {
			new Integer( this.getNodeID() ), new Integer( aNode.getNodeID() )}));
	}
			
	// free my children, but this node is still playing
	public void freeAll()
	throws IOException
	{
		getServer().sendMsg( freeAllMsg() );
	}
	
	public OSCMessage freeAllMsg()
	{
		return new OSCMessage( "/g_freeAll", new Object[] { new Integer( this.getNodeID() )});
	}

	public void deepFree()
	throws IOException
	{
		getServer().sendMsg( deepFreeMsg() );
	}
	
	public OSCMessage deepFreeMsg()
	{
		return new OSCMessage( "/g_deepFree", new Object[] { new Integer( this.getNodeID() )});
	}

	public String toString()
	{
		if( getName() == null ) {
			return( "Group(" + getNodeID() + ")" );
		} else {
			return( "Group::" + getName() + "(" + getNodeID() + ")" );
		}
	}

// -------------- TreeNode interface --------------

	public TreeNode getChildAt( int childIndex )
	{
		final Enumeration children = children();
		for( int idx = 0; idx < childIndex; idx++ ) {
			children.nextElement();
		}
		return (TreeNode) children.nextElement();
	}

	public int getChildCount()
	{
		int idx = 0;
		for( Enumeration children = children(); children.hasMoreElements(); children.nextElement() ) idx++;
		return idx;
	}
	
	public int getIndex( TreeNode node )
	{
		final Enumeration children = children();
		for( int idx = 0; children.hasMoreElements(); idx++ ) {
			if( children.nextElement().equals( node )) {
				return idx;
			}
		}
		return -1;
	}
	
	public boolean getAllowsChildren()
	{
		return true;
	}
	
	public boolean isLeaf()
	{
		return false;
	}
	
	public Enumeration children()
	{
		return new ChildEnumeration( this );
	}
	
// -------------- internal classes --------------

	private static class ChildEnumeration
	implements Enumeration
	{
		private Node nextElement;
	
		protected ChildEnumeration( Group g )
		{
			nextElement = g.getHeadNode();
		}
	
		public boolean hasMoreElements()
		{
			return( nextElement != null );
		}
	
		public Object nextElement()
		{
			final Node result = nextElement;
			if( nextElement == null ) {
				throw new NoSuchElementException();
			} else {
				nextElement	= nextElement.getSuccNode();
				return result;
			}
		}
	}
}