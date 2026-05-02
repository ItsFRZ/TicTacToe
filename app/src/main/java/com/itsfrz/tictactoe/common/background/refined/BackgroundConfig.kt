package com.itsfrz.tictactoe.common.background.refined

// ════════════════════════════════════════════════════════════════════════════
//  ① CONFIG  ─  tune these to change the feel
// ════════════════════════════════════════════════════════════════════════════
 
internal object BackgroundConfig {
    // Physics
    const val GRAVITY        = 440f    // px/s²  (increase → heavier feel)
    const val DRAG_H         = 0.50f   // horizontal air resistance
    const val DRAG_V         = 0.10f   // vertical air resistance
    const val BOUNCE_DAMP    = 0.54f   // energy kept per bounce (0=dead, 1=super ball)
    const val MIN_PARTICLES  = 8       // always-on emoji count
    const val MAX_PARTICLES  = 22      // cap after tapping
 
    // Water
    const val WATER_Y_FRAC   = 0.725f  // fraction of screen height where water starts
    const val STRIPE_H       = 5f      // px height of each colour band
    const val DITHER_W       = 4f      // px width of dither checkers
    const val PAL_SPEED      = 1.4f    // palette cycle speed
 
    // Mountains scroll speed per layer (world-units per second)
    val MTN_SCROLL = floatArrayOf(0.018f, 0.048f, 0.095f)
 
    // Scan-line
    const val SCANLINE_ALPHA = 0.06f   // subtle; raise to 0.15 for heavy CRT look
    const val SCANLINE_GAP   = 4f      // pixels between scan lines
 
    // Retro pixel step (larger = chunkier pixel art)
    const val PIXEL_STEP     = 8f
}