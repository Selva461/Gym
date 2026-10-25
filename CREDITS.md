# Credits

## Exercise demonstration images

The bundled demonstrations in `app/src/main/assets/demo/` come from the
[Free Exercise DB](https://github.com/yuhonas/free-exercise-db), released into the
public domain under [the Unlicense](https://unlicense.org/). They have been
re-encoded to WebP at 420px to fit in an APK.

That licence is the reason they are here. The commercial exercise animations
people usually have in mind — GymVisual and similar — are somebody's product, sold
under licence, and this project will not ship those however convenient it would be.
Public domain work it can ship, and does.

273 of the 486 catalogue lifts have one (140 from the first pass, 133 added in a
second pass where every pair of frames was looked at against the lift's name). Matching was done on name and a match was
only accepted when the equipment, the angle, the grip and the movement noun all
agreed, because a near-miss here is not a cosmetic problem: "Barbell row" scores
0.82 against "Upright Barbell Row" and they are different exercises working
different muscles. Showing someone the wrong movement and letting them learn it is
worse than showing them none, so the lifts with no confident match get the muscle
map and the written guide instead.

## Everything else

The exercise catalogue, the food catalogue, the written guides, the muscle map and
the app itself are original to this project. Nutrition values are estimates; see
the notes in README.md and in the app.
