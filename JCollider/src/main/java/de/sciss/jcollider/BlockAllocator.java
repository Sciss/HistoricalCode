/*
 *  BlockAllocator.java
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

import java.util.List;

/**
 *	The <code>BlockAllocator</code> interface allows the use of different
 *	implementations of allocators for audio and control busses by the
 *	<code>Server</code> class. Busses are identified by their address or
 *	position (an integer number) and a number of channels. An instance of
 *	of a block allocator is obtained through a helper class that implements
 *	the <code>Factory</code> sub interface.
 *
 *	@author		Hanns Holger Rutz
 *  @version	0.28, 24-Jul-06
 */
public interface BlockAllocator
{
	/**
	 *	Allocates a new bus.
	 *
	 *	@param	numChannels		the number of
	 *							channels to allocate. must
	 *							be greater than zero.
	 *	@return	the address or index of the bus which is the
	 *			index of the first channel. returns <code>-1</code>
	 *			if the allocator ran out of busses.
	 */
	public int alloc( int numChannels );
	
	/**
	 *	Frees a bus, that is makes it available again for
	 *	new re-allocation.
	 *
	 *	@param	address		the index of the bus
	 */
	public void free( int address );
	
	/**
	 *	Queries a list of allocated blocks.
	 *	This can be usefull for debugging purposes.
	 *
	 *	@return	a list whose elements are instances of <code>Block</code>
	 */
	public List getAllocatedBlocks();

	/**
	 *	This sub interface describes a class which is
	 *	capable of producing a new <code>BlockAllocator</code>.
	 */
	public interface Factory
	{
		/**
		 *	Creates a new block allocator instance of the given size.
		 *	Bus indices start at zero.
		 *
		 *	@param	size	the number of channels the allocator manages
		 *	@return			a new empty allocator of the given size
		 */
		public BlockAllocator create( int size );

		/**
		 *	Creates a new block allocator instance of the given size,
		 *	with Bus indices starting at a given index.
		 *
		 *	@param	size	the number of channels the allocator manages
		 *	@param	pos		the first bus index to use. this is usefull for example
		 *					to return private audio busses which begin right after the hardware
		 *					wired busses.
		 *	@return			a new empty allocator of the given size, starting
		 *					at the given index
		 *
		 *	@see			ServerOptions#getFirstPrivateBus()
		 */
		public BlockAllocator create( int size, int pos );
	}

	/**
	 *	This sub interface describes a class which represents
	 *	a block of indices in a <code>BlockAllocator</code>.
	 *
	 *	@see	BlockAllocator#getAllocatedBlocks()
	 */
	public interface Block
	{
		/**
		 *	Queries the address or index of the block.
		 *
		 *	@return	the index at which the block starts
		 */
		public int getAddress();

		/**
		 *	Queries the number of channels in the block.
		 *
		 *	@return	the size or number of channels that the block occupies
		 */
		public int getSize();
	}
}
