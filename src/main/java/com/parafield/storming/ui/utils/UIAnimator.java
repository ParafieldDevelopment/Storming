package com.parafield.storming.ui.utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.function.Consumer;

/**
 * Utility for UI animations in Swing.
 */
public class UIAnimator {

    /**
     * Generic property animator.
     * 
     * @param start Starting value.
     * @param target Target value.
     * @param durationMs Duration in ms.
     * @param setter Consumer to apply the current value.
     * @param onComplete Optional runnable to call when finished.
     */
    public static void animate(float start, float target, int durationMs, Consumer<Float> setter, Runnable onComplete) {
        final float distance = target - start;
        if (distance == 0) {
            setter.accept(target);
            if (onComplete != null) onComplete.run();
            return;
        }

        final long startTime = System.currentTimeMillis();
        Timer timer = new Timer(10, null);
        timer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                long elapsed = System.currentTimeMillis() - startTime;
                float progress = Math.min(1.0f, (float) elapsed / durationMs);
                
                // Cubic Ease-Out: f(t) = 1 - (1 - t)^3
                float eased = 1.0f - (float) Math.pow(1.0f - progress, 3);
                
                float current = start + (distance * eased);
                setter.accept(current);
                
                if (progress >= 1.0f) {
                    ((Timer)e.getSource()).stop();
                    if (onComplete != null) onComplete.run();
                }
            }
        });
        timer.start();
    }

    /**
     * Animates a JSplitPane divider from its current position to a target position.
     */
    public static void animateSplit(JSplitPane splitPane, int target, int durationMs) {
        animate(splitPane.getDividerLocation(), target, durationMs, 
            val -> splitPane.setDividerLocation(val.intValue()), null);
    }

    /**
     * Animates a JSplitPane divider relative to its right/bottom edge.
     * Useful when the split pane itself is resizing.
     */
    public static void animateSplitTrailing(JSplitPane splitPane, int fixedSize, int durationMs) {
        animate(0.0f, 1.0f, durationMs, progress -> {
            int currentSize = (splitPane.getOrientation() == JSplitPane.HORIZONTAL_SPLIT) 
                ? splitPane.getWidth() : splitPane.getHeight();
            
            if (currentSize > 0) {
                int target = currentSize - (int)(fixedSize * progress);
                splitPane.setDividerLocation(target);
            }
        }, null);
    }
}
