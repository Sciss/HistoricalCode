# NBodyJS

[![Build Status](https://travis-ci.org/Sciss/NBodyJS.svg?branch=master)](https://travis-ci.org/Sciss/NBodyJS)

An HTML5/JavaScript n-body simulator... written in Scala.
This is a fork from [github.com/ccampo133/NBodyJS](https://github.com/ccampo133/NBodyJS),
departing from the original Scala code and converting the recent Kotlin version back to Scala.

The [sbt-extras](https://github.com/paulp/sbt-extras) script by Paul Phillips, made available
under a BSD-style license, is included, so you can simply build by running:

    ./sbt fastOptJS

Then to see the demo in the browser:

    xdg-open demo.html

I haven't published the Scala version to GH pages yet, but you can see the (basically identical,
although of course less elegant ;) Kotlin version as a
[demo on GitHub pages](http://ccampo133.github.com/NBodyJS).

Below are some explanations from the original read-me:

## Background

In an n-body gravity simulator, the magnitude of the force `F[i]` acting on each body `i`, in a system of `n` bodies, is determined by Newton's law of universal gravitation,

    F[i] = Sum(G  * (m[i] * m[j]) / r[i, j]^2), where j = 1....n and j != i

where `G` is the gravitational constant, `m[i]` is the mass of body `i`, `m[j]` is the mass of body `j`, and `r[i, j]` is the distance from `i` to `j`.  Of course, force is a vector quantity, so the force vector with magnitude `F[i]` is just a unit vector in the direction from `i` to `j`, scaled by `F[i]`.

In the case of gravity simulations, the forces due to gravity get exceedingly large as bodies get closer to each other (due to the 1/r<sup>2</sup> nature of the gravitational force), and hence bodies tend to rapidly accelerate and zoom off when they seemingly "collide". To compensate for this, a softening length `R^2` has been applied in the gravity calculation to prevent bodies from zipping off into the abyss when they get near each-other.  Also, `G` has been set to 1 for simplicity in NBodyJS.

To compute the positions and velocities of all of the bodies, NBodyJS utilizes the [**Velocity Verlet**](http://en.wikipedia.org/wiki/Verlet_integration#Velocity_Verlet) method of numerical integration.  Verlet integration is a second-order method, which offers more stability than simpler, first order solutions such as [Euler's method](http://en.wikipedia.org/wiki/Euler_method), yet at no significant additional computational cost.

For the algorithm to work, first you need to specify the initial conditions of the simulation; namely the initial positions and velocities of all the bodies at some start time `t`.  Then, the Velocity Verlet algorithm consists of the following steps:

1. Calculate the acceleration `a(t)` of each body due to gravity (or whatever force(s) you want) at time `t`, using Newton's laws.
2. Calculate the positon `x(t + dt)` of each body at time `t + dt` (where `dt` is a small time step), using the approximation `x(t + dt) = x(t) + v(t)*dt + a(t) * dt^2 / 2`.
3. Calculate the acceleration `a(t + dt)`, again using Newton's laws and the forces in play.
4. Calculate the velocity `v(t + dt)` using the approximation `v(t + dt) = v(t) + (a(t) + a(t + dt)) * dt / 2`

This algorithm is executed every time you take a time step, `dt`.  Since every body exerts a force on every other body, computing the accelerations is done in O(N<sup>2</sup>) time.  There are quicker, yet similarily accurate algorithms out there, so feel free to implement your own version if you feel like experimenting!  Also note that this method can be used to integrate Newton's laws for nearly any force(s), not just gravity!

NBodyJS utilizes the HTML5 canvas element for display, and [`kotlin-js`](https://kotlinlang.org/docs/tutorials/javascript/kotlin-to-javascript/kotlin-to-javascript.html) to transpile Kotlin to Javascript.

You can view NBodyJS in action on [GitHub Pages](http://ccampo133.github.io/NBodyJS)!

For more information on N-body simulations in general, see [Scholarpedia](http://www.scholarpedia.org/article/N-body_simulations_(gravitational)).

## Controls

* Click and fling to add new particles.
* Hold-click and use the scroll wheel (or up/down arrows) to increase/decrease mass (and radius).
* Press 't' to toggle trails, 'p' to pause, 'c' to clear, and 'r' reset to the initial conditions.

