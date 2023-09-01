# Observations

## 2015-01-06

Regarding the necessity of `(implicit tx: S#Tx)`:

- We only truly need the transaction if dealing with a mutable object.
- A mutable object must be "fresh", i.e. coming from the current txn or actualised from an `stm.Source`
- So there is nothing wrong keeping the transaction as a transient value of the mutable object?
- Note that we already do so in stuff like `SynthBuilder`

Also if we now enforce the use of ID writing instead of structural writing in serialization of
mutable objects, there is already a system specific value present, namely `S#ID`.

I don't see any obvious disadvantages of this radical change? On the pro side:

- API stays clean, no need to employ for example type macros to pretend it's simple

The main follow-up question is what happens to `stm.Source` and where is system `S` encoded (on the use site)?

- let's imagine that type parameter `S` disappears from `Serializer` and moves into the actual `read` method
(this seems to be compatible with 99% of all cases already)
- then a `tx.newHandle` can work with _any_ mutable object irrespectively of its current system. Yes or no?

This sounds quite radical... Imagine:

    trait Foo {
       var bar: Bar
    }

    trait Bar

How would we ensure that `f.bar = bar` is performed on compatible systems? Or (even more radical) could we
employ an automatic "impedance match" that rewrites the object? Then what happens with bar's mutable state if
it has any?

Let's be clear, we already have no full type-safety, because we don't use a system value based dependent type
but an ordinary type parameter, so `foo` and `bar` could refer to different data bases.

An automatic copying would be mind-blowing, no doubt, but how do we ensure statically that the most common
scenario, wanting to match the systems, is checked?

How about a heterogeneous map, i.e. one where we don't want to rely on the one and only type constructor
parameter for the key or value?

How about

    trait BarLike
    trait Bar[S <: Sys[S]] extends BarLike

? Or reversed for better standard readability:

    trait Bar
    trait BarS[S <: Sys[S]] extends Bar

