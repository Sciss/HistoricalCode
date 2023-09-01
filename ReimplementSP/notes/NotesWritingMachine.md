# Writing Machine

(i.e. departing from version 2, wr\_t\_ng m\_ch\_n\_)

- so we are "behind" the development in Körper regarding the OscNode.
- for queries across the net, we use "transaction identifiers" (`Long`)
- we have `class AudioFileRef(val f: File, val numFrames: Long)` with a use-count,
  so the files can be safely disposed of when no longer used. This is probably something we want in SP as well (?)
- we have two 'algorithms' per instance (Pi)
- we have "motions" that would be translatable to serialised pattern streams
- we have a "state" that indicates busy state. we would map that to a `Runner` somehow
- we need resilience, i.e. restarting stuff or carrying on when something seems to time-out

```
def iterate(iterId: Int)(implicit tx: InTxn): Future[Unit] = {
  log(s"iterate($iterId)")
  
  for {
    _     <- dbFill()
    instr <- atomic { implicit tx => phSelectOverwrite  ()            }
    mat   <- atomic { implicit tx => dbFindMatch        (instr)       }
    _     <- atomic { implicit tx => performOverwrite   (instr, mat)  }
  } yield {
  
    log("iterate() - done")
    ()
  }
}
```

- `dbAppend` etc. will translate into running FScape objects (they are implemented that way already)
- the challenge is composability, that is wiring up different code objects through re-use.
