# Architecture — M0/M1

## Dependency direction

```text
+----------------+     +--------------------+     +-----------------+     +----------+
|  engine-core   |<----|  engine-renderer   |<----| engine-android  |<----| demo-app |
| fixed timestep |     | GL resource owners |     | Android surface |     | activity |
+----------------+     +--------------------+     +-----------------+     +----------+
```

Dependencies point inward toward renderer-independent code. There are no circular module dependencies. The initial split deliberately avoids empty modules whose APIs have not yet been designed by a working use case.

## Thread and ownership rules

- Android lifecycle and fallback widgets run on the UI thread.
- Every GLES call, including `glDelete*`, runs on the `GLSurfaceView` render thread.
- `TriangleRenderer.fromAssets` reads the two small built-in shader assets before the view is created. Later general asset decoding belongs to an asynchronous M4 pipeline.
- `GlShaderProgram` owns its program ID. `GlMesh` owns its VAO, VBO, and EBO IDs. Both have idempotent `close` methods.
- `TriangleRenderer` recreates GPU objects in `onSurfaceCreated`; old context names are not retained for future frames.
- `EngineSurfaceView.shutdown` queues idempotent cleanup during Activity destruction. If Android has already destroyed the EGL context, that context remains the final owner of its GPU allocations.

## Error behavior

The manifest declares GLES 3.1, the Activity checks the device's declared version before creating a surface, and the renderer independently checks the actual context version. The latter is authoritative. Shader, mesh, and rendering failures are logged and posted to the UI callback, which stops drawing and displays a fallback.

## Coordinates and timing

M1 uses normalized clip-space positions with aspect correction in the vertex shader. The simulation step is 1/60 second. Frame deltas are capped at 250 ms and at most eight catch-up updates run per frame; dropped time is measured by `FixedStepLoop.droppedSeconds`.

## M2 design constraints

Math types and transforms must remain independent of GLES. Camera and mesh APIs must not require gameplay code to issue GL calls. Generalized material and buffer APIs should be extracted from the working triangle only when the first 2D/3D consumer makes the requirements concrete.
