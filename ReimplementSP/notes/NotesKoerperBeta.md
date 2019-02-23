# Keeping track of the playing background

Basically -- think of `ScalaCollider-Voices` -- we need a feedback mechanism such as `LocalIn`, `LocalOut`,
and naturally this does not work with the way patterns are conceived (pull-based, bottom-up).

On the other hand, FScape / Akka Stream _can_ incorporate feedback loops, although buffering may be a problem.

Another possibility is to introduce an ambiguity for patterns to be instantiated both bottom-up or top-down,
and then have state mutating patterns that can be used very much as in the original koerper-beta program in
top-down fashion.

----------------

The problem for the voice management is that it depends on the duration (instead of an event count), so if we
implement voice management in a pattern:

    VoiceMgr(id, dur)
  
we actually would need a time input

    VoiceMgr(id, dur, time)

which is difficult and nasty.

----------------

## 14-Nov-2018

Event types in SC:

- `note` (play a synth)
- `set` (set controls)
- `on` (play a synth, but do not automatically stop it)
- `off` (stop a synth)
- `rest`
- `monoNote` (`Pmono`)


Not interesting:

- `group` (create group); we need higher level mechanism
- `midi`
- `bus` (write to control bus)
- `audioBus`, `controlBus` (allocate)
- `alloc`, `allocRead`, `cue`, `free`, `gen`, `load`, `read`, `sine1` etc. (buffer)
- `setProperties` (? some hackish thing)
- `tree`
- `phrase`

Since we do not want to expose node-ids, we need to introduce an addressing system for `set`, `on`, `off`.
But also, we do not want to expose any "view" objects whatsoever, to ideally we find a way to
say that parameters are provided by a "push-based" "pattern".

-------

If we follow the pattern, "inverted pattern" route, clearly we need some branching functions that
give good semantics regarding the lazy/eager use of both branches. For example, in `SoundPattern`,
`timbreFromCoord(lastCoord)` must be on the "top level" in the sense that we ensure a value from
`lastCoord` is polled, but we may want to conditionally avoid actually doing the spatial search in the octree.

```
val minSyncLen: Int = ???

val frame: Pat[(Int, Traj)] = ???
val lastCoord : Pat[Coord] = frame.bubble.flatMap { frameIn =>
  val traj  : Pat[(Int, Pat[Coord], Int)] = frameIn._2
  val coord : Pat[Pat[Coord]] = traj._2
  coord.map(_.takeRight(1)).flatten
}
val age       : Pat[Int]      = frame.bubble.flatMap(_._2._3)
val shouldSync: Pat[Boolean]  = age sig_== minSyncLen

def timbreFromCoord(coord: Pat[Coord]): Pat[Int] =  ???

val timbre    : Pat[Int]      = timbreFromCoord(lastCoord)
val durTimbre : Pat[Double]   = ???

If (shouldSync) {
  Bind(Bind.Map(
    "play"    -> "foreground",
    "timbre"  -> timbre
  ))
} Else {
  Bind(Bind.Map(
    "play"    -> "background",
    "timbre"  -> timbre
  ))
}

```

Note that we can still use "normal" pull-based patterns to an extent, as `reset` allows us to lazily rebuild
the access to external inputs (e.g. attributes). So a "push-based" use case could perhaps normally expand a pattern,
but pull after knowingly having prepared the input element which thus will have `hasNext == true` after the reset.
At least for the first stage of experimentation this might be enough, and no pattern would have to be extended by
an alternative push-based expansion stream. Needless to say, calling `reset` for each input value would probably
make it impossible to use nested patterns for the background synth?

The problem is we have to (?) "think forward" here, which is contrary to how patterns work. The if-else indicates
this problem.

------

```
If (shouldSync) {
  Bind(Bind.Map(
    "play"    -> "foreground",
    "timbre"  -> timbre
  ))
} Else {
  val amp = lastCoord(3).linLin(-1, +1, 0, 1).clip(0, 1)
  Bind(Bind.Map(
    "play"    -> "background",
    "timbre"  -> timbre,
    "amp"     -> amp
  ))
}
```

This won't fly (patterns on the RHS in a bind). Should we attempt to model event types?

```
val voiceId: Pat[Int] = frame.bubble.flatMap(_._1)

If (shouldSync) {
  ...
} Else {
  val vm    = VoiceMgr(numVoices = ???)
  val voice = vm.
  val amp   = lastCoord(3).linLin(-1, +1, 0, 1).clip(0, 1)
  Bind(Bind.Map(
    "play"    -> "background",
    "timbre"  -> timbre,
    "amp"     -> amp
  ))
}
```
