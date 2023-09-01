    val x: S#Var[A] = ???
    
    x()  // no context
    
    implicit val ctx: Context[Long] = ???
    
    val time: Long = ???
    
    x.applyAt(time)
    
    in(ctx) { x() }
    
We have two base types: collections and single variables.
Collection translates to `BiGroup`, single variable translates
to `BiPin`.

A more difficult (?) case is the lifting of a collection
such as `AttrMap`. Because with a `rangeQuery` for a span,
we would end up with nothing more than a `Iterator[S#Tx, (K, V)]`,
so we lose the performance of the map. 
Just lifting the values is simpler.

A similar problem arises with `Scans` because again that is
a map. Perhaps the problem is less pronounced because the size
of that collection will typically be very small, so O(N) is
not too bad. But it remains a principle issue.

The thing is, elements in a `BiGroup` are themselves unordered.
So whenever we introduce an ordering, such as integer index
or string key, we need another dimension in addition to the
two dimensions used for the `Span`, so values can go from
`Iterable` to `IndexedSeq` or `Map`. The current implementation
with `SkipList.Map` already requires that there is a key
ordering so that is not a new constraint.

The follow up problem is that with `SkipOctree`, we cannot
just insert coordinates between coordinates (same problem
as addressed in the mark-vs-full tree). So we would need a
static address of the key. This could be the `hashCode`
integer, if we accept that we might need to handle hash
collisions (again, we already do that (?) in `SkipList.Map`).

An entry in a map is more like a `S#Var[Option[A]]`. The
three dimensional octree we would only need if we were to
iterate over the keys?

    map.putIn   (context)(key, value)   // (1)
    map.removeIn(context)(key)          // (2)
    map.keysIteratorIn(context)         // (3)

This would roughly translate to

    // (1)
    keyTree.add(Span.From(context.time), key)  // but have to check if it exists already...
    valueBiPin(context).add(context.time, value)
    
    // (2)
    val floor = keyTree.floor(context.time - 1)
    keyTree.remove(Span.From(floor, key)
    keyTree.add(Span(floor, context.time), key)
    
    // (3)
    keyTree.intersect(context.time)
    
What API do we need? Obviously we need to merge LucreData and LucreSTM
to a certain extent.

    trait Txn[S <: Sys[S]] {
      def newMap[K, V]  : S#Map[K ,V]   // or stm.Map[K, V]
      def newSet[A]     : S#Set[A]      // or stm.Set[A]
      def newIterable[A]: S#Iterable[A] // or stm.Iterable[A] // aka multi-set
    }
    
We would have to attach events to that but could simplify because we do not want
to track element changes through the collection any longer.

Can we aggregate contexts? `map.putIn((context1, context2))(key, value)`?
The simplest solution here is to allow multiple contexts for `get` but only
a single context for `put`, and if aggregates are needed one must make
a new auxiliary context.

The ideal maximal definition of context is akin to CSS selectors. That is
to say, for example,
"make object X red if it appears inside Timeline Y inside any other Timeline".
That is not only complex, but would require that all contexts that were
ever used in assignments be stored somehow with the object. Like so

    x.contexts: Iterator[S#Tx, ContextKey]

This is a too project for now. For now, what we need is an individual context
for a timeline. We can already support a hierarchy like `map.getIn(c1, c2) _`
that linearly checks `c1`, then `c2`, then `()`. Since the number of contexts
in a query will likely be small or zero, this should be sufficient. So
`(c1, c2, ...)` form a union of all assigned values in the query ("c1 or c2").
We could even (if we find it useful) support intersection ("c1 and c2") that
is more CSS-like, where we simply concatenate the context keys for the store
(in some defined order, e.g. lexicographically).

The store would look like this:

    plain-key             ->  v1
    plain-key ++ c1       ->  v2
    plain-key ++ c2       ->  v3
    plain-key ++ c1 ++ c2 ->  v4

Then both `map.getIn(c1 & c2)` and `map.getIn(c2 & c1)` would find `v4`, 
`map.getIn(c1 | c2)` would find `v2`, and `map.getIn(c2 | c1)` would find `v3`.

Think of CSS `A, B` versus `A > B`.

The context keys would probably just be `Int`, as supplied by one of the methods
normally invoked as part of `tx.newID`. Or we have an independent counter.

It should be possible to create arbitrary, "nominal", contexts. Such as

    val myGroup = tx.newContext()
    myVar.updateIn(myGroup)(value)
    
That is, a context may be just a category, it does not necessarily need to
be attached to a dimension such as time. But how would the time-based context
look like?

    val myGroup = tx.newContext(0L)
    myGroup.position = 1234L    // ephemeral!
    myVar.applyIn(myGroup)()

So

    trait Txn[S <: Sys[S]] {
      def newContext(): Context[Unit]
      def newContext[A](pos0: A)(implicit ord: Ordering[A])
    }

The `Timeline` group becomes an `Ensemble` or `Folder` viewed through a `Long` context.
Old API:

    def iterator(implicit tx: S#Tx): data.Iterator[S#Tx, Leaf[S, Elem]]
    
    def intersect(time: Long     )(implicit tx: S#Tx): Iterator[S#Tx, Leaf[S, Elem]]
    def intersect(span: SpanLikeV)(implicit tx: S#Tx): Iterator[S#Tx, Leaf[S, Elem]]   // needed?
    
    def eventBefore(time: Long)(implicit tx: S#Tx): Option[Long]
    def eventAfter (time: Long)(implicit tx: S#Tx): Option[Long]
    def eventsAt   (time: Long)(implicit tx: S#Tx): (Iterator[S#Tx, Leaf[S, Elem]], Iterator[S#Tx, Leaf[S, Elem]])
    
    def add   (span: Expr[S, SpanLikeV], elem: Elem)(implicit tx: S#Tx): TimedElem[S, Elem]
    def remove(span: Expr[S, SpanLikeV], elem: Elem)(implicit tx: S#Tx): Boolean

I don't think the `intersect(Span)` is needed at all, at least it is not used in the current
implementation of SoundProcesses. It is used only once in Mellite, for removing a span of
objects on a timeline.

With the context rewriting `intersect(Long)` becomes `folder.in(context).iterator`.

Should be possible to store 'contextualised' objects, e.g. `(folder, context)`. These will be
cheap objects to serialize, and they may be re-created multiple times.

**? Can a working system be created in just one week ?**

Would would need to be inside:

- context
- transparent elem/obj/attr-map
- not necessarily: revised serialization
- not necessarily: object dependencies / new events
- not necessarily: general indices
